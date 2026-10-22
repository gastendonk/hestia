package hestia.otc.secret;

import java.util.ArrayList;
import java.util.List;

import github.soltaufintel.amalia.web.table.Col;
import github.soltaufintel.amalia.web.table.Cols;
import github.soltaufintel.amalia.web.table.TableComponent;
import hestia.environment.Environment;
import hestia.otc.model.Secret;
import hestia.web.base.HPage;

public class SecretsPage extends HPage {

    @Override
    protected void execute() {
        String id = ctx.pathParam("id");
        
        Environment env = environmentDAO().loadOne(id);
        cenv(env);
        List<Secret> secrets = new ArrayList<>(secretDAO().load(id));
        secrets.sort((a, b) -> a.getName().compareTo(b.getName()));

        header(n("Secrets"));
        put("id", esc(id));
        var list = list("list");
        var p = "/" + b().getBranch() + "/secrets/" + id + "/";
        for (Secret secret : secrets) {
            var map = list.add();
            map.put("name", esc(secret.getName()));
            map.put("link", esc(p + secret.getName()));
        }
        Cols cols = Cols.of(Col.i(n("Name"), "name"),
                new Col("", "<a onclick=\"return confirm('{{N.Delete}}?');\" href=\"/{{branch}}/secret/{{id}}/{{i.name}}/delete\""
                        + " class=\"btn btn-xs btn-danger\" title=\"{{N.Delete}}\"><i class=\"fa fa-trash-o\"></i></a>"));
        put("table", new TableComponent("wauto", cols, model, "list"));
    }
}
