package hestia.exchange;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.pmw.tinylog.Logger;

import github.soltaufintel.amalia.base.FileService;
import github.soltaufintel.amalia.base.StringService;
import github.soltaufintel.amalia.rest.REST;
import hestia.HestiaWebapp;
import hestia.base.IBranch;
import hestia.environment.Environment;
import hestia.environment.EnvironmentDAO;
import hestia.git.GitRepository;
import hestia.otc.model.MonitoredTargetDAO;
import hestia.persist.GsonFactory;
import hestia.persist.IRepository;
import hestia.prometheus.alert.AlertGroupDAO;

public class ExchangeService {

    /**
     * Pull data from cloud
     */
    public void pull() {
        if (HestiaWebapp.config.isCustomer()) {
            throw new IllegalStateException("Hestia is not in customer mode.");
        }
        if (HestiaWebapp.config.getCloudInstance() == null) {
            throw new IllegalStateException("Please set env var CLOUD.");
        }
        var key = HestiaWebapp.config.getCustomerKey();
        if (StringService.isNullOrEmpty(key)) {
            throw new IllegalStateException("Please set ent var CUSTOMERKEY.");
        }
        
        // load data
        var url = HestiaWebapp.config.getCloudInstance() + "/x/pull/" + key;
        Logger.info("[exchange] pull | URL: " + url);
        String json = new REST(url).get().response();
        Logger.info("[exchange] pull | response: " + json); // XXX debug
        
        // einfach mal wegspeichern
        var file = new File(HestiaWebapp.config.getBaseFolder(), "received/" + ts() + ".json");
        FileService.savePlainTextFile(file, json);
        Logger.info("[exchange] pull | saved to " + file.getAbsolutePath());
        
        // parse data
        ExchangeData data = GsonFactory.create().fromJson(json, ExchangeData.class);
        Logger.info("[exchange] pull | tag: " + data.getTag());
        
        // save data
        setData(data);
    }

    /**
     * Serve data for customer
     * @param customerKey secret customer key
     * @return JSON, null if no data was found
     */
    public String serve(String customerKey) {
        Logger.info("[exchange] serve " + customerKey);
        File folder = new File(HestiaWebapp.config.getBaseFolder(), "x/" + customerKey);
        if (!folder.isDirectory()) {
            Logger.error("[exchange] serve | folder does not exist: " + folder.getAbsolutePath());
            return null;
        }
        File[] files = folder.listFiles();
        if (files == null) {
            return null;
        }
        int highest = 0;
        File found = null;
        for (File file : files) {
            // Datei mit hoechster k Nr. finden
            String name = file.getName();
            Logger.info("XXX - " + name); // XXX spaeter raus!
            if (name.startsWith("k") && name.endsWith(".json")) {
                try {
                    name = name.substring(1);
                    name = name.substring(0, name.length() - ".json".length());
                    int num = Integer.parseInt(name);
                    if (num > highest) {
                        highest = num;
                        found = file;
                    }
                } catch (Exception e) {
                }
            }
        }
        if (found == null) {
            Logger.error("[exchange] serve | no file found");
            return null;
        }
        Logger.info("[exchange] serve | file found: " + found.getAbsolutePath());
        return FileService.loadPlainTextFile(found);
    }
    
    /**
     * Push data to cloud server
     */
    public void push(IBranch branch, String customerKey, String tag) {
        var ci = HestiaWebapp.config.getCloudInstance();
        if (StringService.isNullOrEmpty(ci)) {
            throw new RuntimeException("Cloud instance is not set");
        }
        Logger.info("[exchange] push | tag: " + tag + " | branch: " + branch + " | customer key: " + customerKey);
        var data = getData(branch, tag);
        var json = GsonFactory.create().toJson(data);
        var url = ci + "/x/receive/" + customerKey + "/" + tag;
        Logger.info("[exchange] push | POST " + url);
        Logger.debug("[exchange] push | JSON: " + json);
        new REST(url).post(json).close();
    }
    
    /**
     * Cloud server receives data from Burg
     * @param customerKey -
     * @param tag -
     * @param body JSON
     */
    public void receive(String customerKey, String tag, String body) {
        Logger.info("[exchange] receive | customer key: " + customerKey + " | tag: " + tag);
        if (customerKey == null || customerKey.contains("/") || customerKey.contains("..")
                || tag == null || tag.contains("/") || tag.contains("..")) {
            throw new IllegalArgumentException();
        }
        Logger.debug("[exchange] receive | body: " + body);
        try {
            File file = new File(HestiaWebapp.config.getBaseFolder(), "x/" + customerKey + "/" + tag + ".json");
            Logger.info("[exchange] receive | saved as " + file.getAbsolutePath() + ", " + file.isFile());
            FileService.savePlainTextFile(file, body);
        } catch (Exception e) {
            Logger.error(e);
        }
    }
    
    // TODO wenn ich das hier mache, darf sonst keiner aufs Repo dieses Branches zugreifen! sync...
    public ExchangeData getData(IBranch branch, String tag) {
        var data = new ExchangeData();
        data.setTag(tag);
        data.setFiles(new HashMap<>());
        IRepository repo = HestiaWebapp.config.getRepository(branch);
        if (repo instanceof GitRepository git) {
            git.getRepo().selectCommit(tag);
            git.getRepo().pull();
        }
        try {
            var dao1 = new EnvironmentDAO(repo);
            var dao2 = new MonitoredTargetDAO(repo);
            var dao3 = new AlertGroupDAO(repo);
            data.put(dao1.getFile());
            for (Environment env : dao1.load()) {
                data.put(dao2.getFile(env.getId()));
                data.put(dao3.getFile(env.getId()));
            }
        } finally {
            if (repo instanceof GitRepository git) {
                git.getRepo().switchToBranch(branch.getBranch());
                git.getRepo().pull();
            }
        }
        return data;
    }
        
    /**
     * Receive exchange data from other Hestia instance
     * @param data -
     */
    public void setData(ExchangeData data) {
        var base = HestiaWebapp.config.getBaseFolder();
        var targetFolder = base;
        var backupFolder = new File(base, "backup/" + ts());
        List<File> backupList = new ArrayList<>();
        for (Entry<String, String> e : data.getFiles().entrySet()) {
            var dn = e.getKey();
            var json = e.getValue();
            var file = new File(targetFolder, dn);
            if (file.isFile()) {
                FileService.copyFile(file, backupFolder);
                backupList.add(new File(backupFolder, file.getName()));
            }
            FileService.savePlainTextFile(file, json);
            Logger.info("saved: " + file.getAbsolutePath());
        }
        Logger.info("backup files: " + backupList.stream().map(i -> i.getAbsolutePath()).collect(Collectors.joining(", ")));
    }
    
    private String ts() {
        return StringService.now().replace(":", "").replace(" ", "-");
    }

    public static File file(String customerKey, String tag) {
        return new File(HestiaWebapp.config.getBaseFolder(), customerKey + "/" + tag + ".json");
    }
}
