package hestia.otc;

import java.io.File;

// XXX Spielwiese

public class Tester {
    private static final String exe = "development\\bin\\otelcol-contrib.exe";
    private static final String
//        configFile = "development\\bin\\config.yaml";
        configFile = "development/setup1/config.yaml";

    public static void main(String[] args) throws InterruptedException {
        new OtcService().start(new File(exe), true, new File(configFile));
        System.out.println("start ist durch");
        while(true) {
            Thread.sleep(1000);
        }
    }
    
    public static void main2(String[] args) {
        String out = new OtcService().validate(new File(exe), true, new File(configFile));
        if (out.isEmpty()) {
            System.out.println("ERFOLG");
        } else {
            System.out.println("FEHLER in " + configFile);
            System.out.println(out);
        }
    }
}
