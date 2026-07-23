package hestia.exchange;

import java.io.File;
import java.util.HashMap;
import java.util.Map.Entry;

import org.pmw.tinylog.Logger;

import com.google.gson.Gson;

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
        var key= "4711";
        var url = "http://cloud/pull/" + key;
        String json = new REST(url).get().response();
        // TODO
    }

    /**
     * Serve data for customer
     * @param key secret customer key
     * @param branch ?
     * @return JSON
     */
    public String serve(String key, IBranch branch) {
        Logger.info("[exchange] serve " + key);
        // TODO vom key zum tag
        var data = getData(branch, "???");
        var json = new Gson().toJson(data);
        return json;
    }
    
    /**
     * Push data to cloud server
     */
    public void push(IBranch branch, String tag) {
        var ci = HestiaWebapp.config.getCloudInstance();
        if (StringService.isNullOrEmpty(ci)) {
            throw new RuntimeException("Cloud instance is not set");
        }
        Logger.info("[exchange] push " + tag + " (branch: " + branch + ")");
        var data = getData(branch, tag);
        var json = GsonFactory.create().toJson(data);
        Logger.info(json);
        var url = ci + "/x/receive/" + tag;
        Logger.info("POST " + url);
        new REST(url).post(json).close();
    }
    
    /**
     * Cloud server receives data from Burg
     * @param tag -
     * @param body JSON
     */
    public void receive(String tag, String body) {
        Logger.info("[exchange] receive " + tag);
        Logger.info(body);
        try {
            ExchangeData data = GsonFactory.create().fromJson(body, ExchangeData.class);
            Logger.info(data.getFiles().keySet());
//          setData(data, tag);
        } catch (Exception e) {
            Logger.error(e);
        }
    }
    
    // TODO wenn ich das hier mache, darf sonst keiner aufs Repo dieses Branches zugreifen! sync...
    public ExchangeData getData(IBranch branch, String tag) {
        var data = new ExchangeData();
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
     * @param tag null: customer folder
     */
    public void setData(ExchangeData data, String tag) {
        var base = HestiaWebapp.config.getBaseFolder();
        var targetFolder = tag == null ? base : new File(base, tag);
        var ts = StringService.now().replace(":", "").replace(" ", "-");
        var backupFolder = new File(base, "backup/" + ts);
        for (Entry<String, String> e : data.getFiles().entrySet()) {
            var dn = e.getKey();
            var json = e.getValue();
            var file = new File(targetFolder, dn);
            if (tag == null && file.isFile()) {
                FileService.copyFile(file, backupFolder);
            }
            FileService.savePlainTextFile(file, json);
        }
    }

    public static File file(String customerKey, String tag) {
        return new File(HestiaWebapp.config.getBaseFolder(), customerKey + "/" + tag + ".json");
    }
}
