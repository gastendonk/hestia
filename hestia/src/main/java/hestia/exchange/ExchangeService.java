package hestia.exchange;

import github.soltaufintel.amalia.rest.REST;

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
     * @param key -
     * @return JSON
     */
    public String serve(String key) {
        // TODO
        return "{}";
    }
    
    /**
     * Push data to cloud server
     */
    public void push() {
        var url = "...";
        var json = "";
        new REST(url).post(json).close();
        // TODO
    }
    
    /**
     * Cloud server receives data from Burg
     * @param key -
     * @param body JSON
     */
    public void receive(String key, String body) {
        // TODO
    }
}
