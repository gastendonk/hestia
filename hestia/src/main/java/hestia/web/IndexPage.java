package hestia.web;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import github.soltaufintel.amalia.base.FileService;
import github.soltaufintel.amalia.web.action.Page;
import hestia.HestiaWebapp;
import hestia.otc.process.OtcProcess;

public class IndexPage extends Page {

    @Override
    protected void execute() {
        OtcProcess otc = HestiaWebapp.otcProcess;

        put("info", esc(System.getenv("INFO")));
        put("pid", "" + otc.pid());
        put("alive", otc.alive());
        putInt("status", otc.check());
        put("datetime", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        put("info1", esc(otc.info1));
        put("info2", esc(otc.info2));
        put("config", esc(FileService.loadPlainTextFile(new File("/work/config.yaml"))));
    }
}
