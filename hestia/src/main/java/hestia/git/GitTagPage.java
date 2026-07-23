package hestia.git;

import java.util.ArrayList;
import java.util.List;

import hestia.base.HPage;

public class GitTagPage extends HPage {

    @Override
    protected void execute() {
        if (isPOST()) {
            String customer = ctx.formParam("customer");
            // TODO Brauche ich Umgebung????
            
            // BASF
            // Kunde  hat     production 4.06.7        jobserver 1, Datenbank 1
            //                test       4.12.1        jobserver 2, Datenbank 2    BASF-p4.06.7-t4.12.1
            
            // Bei uns gibts ein Update
            // Kunde  hat     production 4.06.7        jobserver 1, Datenbank 1
            //                test       4.12.3        jobserver 2, Datenbank 2    BASF-p4.06.7-t4.12.3

            // Ansatz: tag muss wohl eher eine fortlfd. Nr. sein.
            // data/4711abcd/k000074.json
            
            // Kunde will Daten pullen. Er hat dazu Kunden-Key "4711abcd".
            // Cloud-Instanz: "4711abcd" -> Kunde BASF -> neueste Daten liefern (höchste Nr.)
            
            
            // Wie läuft das wohl in der Praxis ab?
            
            String tag = ctx.formParam("tag");

            // TODO

            ctx.redirect("/" + ctx.pathParam("branch"));
        } else {
            // alle Git Repo tags laden und neuesten k... tag ermitteln und readonly vorschlagen
            
            header("taggen");
            List<String> customers = new ArrayList<>();
            combobox("customers", customers, (String) null, false);
            put("tag", "");
        }
    }
}
