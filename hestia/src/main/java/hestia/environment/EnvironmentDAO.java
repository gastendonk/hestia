package hestia.environment;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import github.soltaufintel.amalia.base.FileService;

public class EnvironmentDAO {

    public void save(List<Environment> environments, File folder) {
        var e = new Environments();
        e.setEnvironments(environments);
        FileService.saveJsonFile(new File(folder, "environments.json"), e);
    }
    
    public List<Environment> load(File folder) {
        var e = FileService.loadJsonFile(new File(folder, "environments.json"), Environments.class);
        return e == null || e.getEnvironments() == null ? new ArrayList<>() : e.getEnvironments();
    }

    public static class Environments {
        private List<Environment> environments = new ArrayList<>();

        public List<Environment> getEnvironments() {
            return environments;
        }

        public void setEnvironments(List<Environment> environments) {
            this.environments = environments;
        }
    }
}
