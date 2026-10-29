package hestia.special.jenkins;

import java.util.List;

import github.soltaufintel.amalia.rest.REST;
import hestia.special.jenkins.ComputerData.Computer;

public class JenkinsService {

    public List<Computer> loadWorkers(String jenkinsUrl) {
        var r = new REST(jenkinsUrl + "/computer/api/json").get().fromJson(ComputerData.class);
        r.getComputer().removeIf(i -> !i.get_class().equals("hudson.slaves.SlaveComputer"));
        return r.getComputer();
    }
}
