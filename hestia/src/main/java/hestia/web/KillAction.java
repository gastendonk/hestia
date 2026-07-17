package hestia.web;

import org.pmw.tinylog.Logger;

import hestia.HestiaWebapp;
import hestia.base.HAction;
import hestia.otc.OtcProcess;

public class KillAction extends HAction {

    @Override
    protected void execute() {
        var m = ctx.queryParam("m");

        if ("s".equals(m)) {
            Logger.info("KillAction -> restart");
            HestiaWebapp.otcProcess = new OtcProcess();
        } else if ("hard".equals(m)) {
            Logger.info("KillAction -> check & hard kill");
            ProcessHandle.allProcesses() //
                    .filter(ph -> ph.info().command().orElse("").contains("otelcol-contrib")) //
                    .forEach(ph -> {
                        Logger.info("Säubere verwaisten OTel-Prozess mit PID: " + ph.pid());
                        ph.destroyForcibly();
                    });
        } else {
            Logger.info("KillAction -> kill");
            HestiaWebapp.otcProcess.kill();
        }

        ctx.redirect("/");
    }
}
