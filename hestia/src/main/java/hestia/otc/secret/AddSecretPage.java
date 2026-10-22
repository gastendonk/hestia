package hestia.otc.secret;

import github.soltaufintel.amalia.base.StringService;
import hestia.otc.model.Secret;
import hestia.web.base.HPage;

public class AddSecretPage extends HPage {

    @Override
    protected void execute() {
        String id = ctx.pathParam("id");

        if (isPOST()) {
            var name = ctx.formParam("name");
            if (StringService.isNullOrEmpty(name)) {
                throw new RuntimeException("Bitte Name eingeben!");
            } else if (!name.matches("^[A-Z][A-Z0-9_]*$")) {
                throw new RuntimeException("Name muss aus Groﬂbuchstaben, Ziffern und Unterstrichen bestehen!");
            }
            
            Secret secret = new Secret();
            secret.setName(name);
            secret.setSecret(Secret.encode(ctx.formParam("secret")));
            secretDAO().insert(id, secret);
            
            ctx.redirect("/" + b().getBranch() + "/secret/" + id);
        } else {
            header(n("AddSecret"));
            put("branch", esc(b().getBranch()));
            put("id", esc(id));
        }
    }
}
