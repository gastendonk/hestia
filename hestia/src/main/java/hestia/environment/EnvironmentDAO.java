package hestia.environment;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import github.soltaufintel.amalia.base.FileService;
import github.soltaufintel.amalia.base.IdGenerator;
import github.soltaufintel.amalia.base.StringService;
import hestia.HestiaWebapp;

public class EnvironmentDAO {

    public static List<Environment> load() {
        Environments e = FileService.loadJsonFile(file(), Environments.class);
        return e == null || e.getList() == null ? new ArrayList<>() : e.getList();
    }

    public static void save(List<Environment> list) {
        list.sort((a, b) -> a.getName().compareToIgnoreCase(b.getName()));
        Environments e = new Environments();
        e.setList(list);
        FileService.saveJsonFile(file(), e);
    }

    public static class Environments {
        private List<Environment> list;

        public List<Environment> getList() {
            return list;
        }

        public void setList(List<Environment> list) {
            this.list = list;
        }
    }

    public static File file() {
        return new File(HestiaWebapp.config.getEnvironmentsFolder(), "environments.json");
    }

    public static Environment load(String id) {
        return load().stream().filter(i -> i.getId().equals(id)).findFirst().orElseThrow();
    }

    public static void save(Environment env, boolean setCustomerKey) {
        var list = load();
        list.removeIf(i -> i.getId().equals(env.getId()));
        if (setCustomerKey) {
            for (Environment i : list) {
                if (i.getCustomer().equals(env.getCustomer())) {
                    env.setCustomerKey(env.getCustomerKey());
                    break;
                }
            }
            if (StringService.isNullOrEmpty(env.getCustomerKey())) {
                env.setCustomerKey(IdGenerator.createId25());
            }
        }
        list.add(env);
        save(list);
    }
}
