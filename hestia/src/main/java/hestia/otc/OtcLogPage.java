package hestia.otc;

import hestia.web.base.HPage;

public class OtcLogPage extends HPage {

    @Override
    protected void execute() {
        put("log", esc(OtcLog.load()));
        header(n("Log"));
    }
}
