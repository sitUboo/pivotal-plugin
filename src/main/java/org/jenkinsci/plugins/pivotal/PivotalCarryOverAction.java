/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jenkinsci.plugins.pivotal;

import hudson.Util;
import hudson.model.InvisibleAction;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 *
 * @author stevendeal
 */
class PivotalCarryOverAction extends InvisibleAction {
    /**
     * ','-separate IDs, for compact persistence.
     */
    private final String ids;

    public PivotalCarryOverAction(List<PivotalStory> issues) {
        StringBuilder buf = new StringBuilder();
        boolean first=true;
        for (PivotalStory issue : issues) {
            if(first)   first=false;
            else        buf.append(",");
            buf.append(issue.id);
        }
        this.ids = buf.toString();
    }

    public Collection<String> getIDs() {
        return Arrays.asList(Util.tokenize(ids,","));
    } 
}
