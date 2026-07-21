package hestia.git;

import org.pmw.tinylog.Logger;

import hestia.HestiaWebapp;
import hestia.base.HPage;

/**
 * Branch auswählen
 */
public class GitBranchPage extends HPage {

    @Override
    protected void execute() {
        var repo = HestiaWebapp.config.getRepo();
        if (repo == null) {
            throw new RuntimeException();
        }
        if (isPOST()) {
            var branch = ctx.formParam("branch");
            
            repo.switchToBranch(branch);
            repo.pull();
            Logger.info("switched to branch " + branch);
            
            ctx.redirect("/" + branch);
        } else {
            header(n("SelectBranch"));
            combobox("branchs", repo.getBranchNames(), repo.getBranch(), false);
        }
    }
}
