package hestia.special.jenkins;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ComputerData {
    private List<Computer> computer;

    public List<Computer> getComputer() {
        return computer;
    }

    public void setComputer(List<Computer> computer) {
        this.computer = computer;
    }

    public static class Computer {
        private String _class;// hudson.slaves.SlaveComputer
        private List<Map<String, String>> assignedLabels;
        private String description;
        private String displayName;
        private int numExecutors;
        private boolean offline;
        private boolean temporarilyOffline;

        public String get_class() {
            return _class;
        }

        public void set_class(String _class) {
            this._class = _class;
        }

        public List<Map<String, String>> getAssignedLabels() {
            return assignedLabels;
        }
        
        public String getAssignedLabelsStr() {
            return assignedLabels.stream()
                    .map(map -> map.get("name"))
                    .collect(Collectors.joining(" "));
        }
        
        public boolean ok() {
            return !isOffline() && !isTemporarilyOffline();
        }

        public void setAssignedLabels(List<Map<String, String>> assignedLabels) {
            this.assignedLabels = assignedLabels;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getDisplayName() {
            return displayName;
        }

        public void setDisplayName(String displayName) {
            this.displayName = displayName;
        }

        public int getNumExecutors() {
            return numExecutors;
        }

        public void setNumExecutors(int numExecutors) {
            this.numExecutors = numExecutors;
        }

        public boolean isOffline() {
            return offline;
        }

        public void setOffline(boolean offline) {
            this.offline = offline;
        }

        public boolean isTemporarilyOffline() {
            return temporarilyOffline;
        }

        public void setTemporarilyOffline(boolean temporarilyOffline) {
            this.temporarilyOffline = temporarilyOffline;
        }
    }
}
