package hestia.otc;

import github.soltaufintel.amalia.base.FileService;
import hestia.HestiaWebapp;
import hestia.web.DeployAction;
import hestia.web.base.HPage;

public class OtcStatusPage extends HPage {
    // TODO Passwörter maskieren

    @Override
    protected void execute() {
        waitForGreen(); // at begin
        
        OtcProcess otc = HestiaWebapp.otcProcess;
        int checkHealth = otc == null ? -3 : otc.checkHealth();
        var otcFile = HestiaWebapp.config.getOtelcolContrib();
        var configYamlFile = HestiaWebapp.config.getConfigYaml();
        var configYaml = FileService.loadPlainTextFile(configYamlFile);

        header(n("otcStatus"));
        put("pid", otc == null || otc.pid() <= 0 ? "" : "" + otc.pid());
        put("alive", otc != null && otc.alive());
        if (otc == null || checkHealth < -1) {
            put("status", "");
        } else {
            putInt("status", checkHealth);
        }
        put("otcFileInfo", otcFile.getName() + " " + n(otcFile.isFile() ? "exists" : "notexist") + ".");
        put("config", configYaml == null ? n("fileXNotFound").replace("$x", configYamlFile.getName()) : esc(configYaml));
        put("cp0", otcFile.isFile());
        put("cp1", otc != null && otc.isCheckpoint1());
        put("cp2", otc != null && otc.isCheckpoint2());
        put("cp3", otc != null && checkHealth == 200);
        put("downloadUrl", esc(HestiaWebapp.config.getOtelcolContribDownloadUrl()));
        put("lastDeployed", esc(DeployAction.lastDeployed));
    }

    private void waitForGreen() {
        for (int i = 1; i <= 4; i++) {
            if (HestiaWebapp.otcProcess != null && HestiaWebapp.otcProcess.isCheckpoint1()) {
                break;
            }
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
            }
        }
    }
}
