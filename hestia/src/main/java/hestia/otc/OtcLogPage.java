package hestia.otc;

import hestia.HestiaWebapp;
import hestia.web.base.HPage;

public class OtcLogPage extends HPage {

    @Override
    protected void execute() {
        OtcProcess otc = HestiaWebapp.otcProcess;
        put("log", otc == null ? "" : esc(otc.getLog()));
        header(n("Log"));
    }
}
