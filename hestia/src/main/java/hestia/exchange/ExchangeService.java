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
import hestia.environment.Environment;
import hestia.environment.EnvironmentDAO;
import hestia.otc.model.MonitoredTargetDAO;
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
     * @return JSON
     */
    public String serve(String key) {
        Logger.info("[exchange] serve " + key);
        // TODO vom key zum tag
        var data = getData();
        var json = new Gson().toJson(data);
        return json;
    }
    
    /**
     * Push data to cloud server
     */
    public void push(String tag) {
        Logger.info("[exchange] push " + tag);
        var url = "...";
        var json = "";
        new REST(url).post(json).close();
        // TODO
    }
    
    /**
     * Cloud server receives data from Burg
     * @param tag -
     * @param body JSON
     */
    public void receive(String tag, String body) {
        Logger.info("[exchange] receive " + tag);
        ExchangeData data = new Gson().fromJson(body, ExchangeData.class);
        setData(data, tag);
    }
    
    // TODO tag
    public ExchangeData getData() {
        var data = new ExchangeData();
        data.setFiles(new HashMap<>());
        data.put(EnvironmentDAO.file());
        for (Environment env : EnvironmentDAO.load()) {
            data.put(MonitoredTargetDAO.file(env.getId()));
            data.put(AlertGroupDAO.file(env.getId()));
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
