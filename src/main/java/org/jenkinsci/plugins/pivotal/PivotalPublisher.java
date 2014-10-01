package org.jenkinsci.plugins.pivotal;

import hudson.Util;
import hudson.Launcher;
import hudson.Extension;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.model.AbstractProject;
import hudson.model.Hudson;
import hudson.model.Result;
import hudson.model.Run;
import hudson.scm.ChangeLogSet.Entry;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Publisher;
import hudson.tasks.Recorder;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;

/**
 * Start of a plugin to parse the git logs for pivotal tickets and assuming a
 * successful build, update pivotal with the build# User: stevendeal Date:
 * 2/13/13 Time: 12:51 PM
 */
public class PivotalPublisher extends Recorder {

    protected static final Pattern STORIE_PATTERN = Pattern.compile("([0-9]{8})");
    protected static Pivotal pivotal;

    @DataBoundConstructor
    public PivotalPublisher() {
    }
    
    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.BUILD;
    }

    @Override
    public boolean perform(AbstractBuild build, Launcher launcher, BuildListener listener) {

        List<PivotalStory> stories = null;

        try {
            String rootUrl = Hudson.getInstance().getRootUrl();
            if (rootUrl == null) {
                LOGGER.log(Level.SEVERE, Messages.PivotalPublisher_NoJenkinsUrl());
                build.setResult(Result.FAILURE);
                return true;
            }

            Set<String> ids = findStoryIdsRecursive(build, STORIE_PATTERN, listener);

            if (ids.isEmpty()) {
                listener.getLogger().println(Messages.PivotalPublisher_NoStories());
                return true;
            } else {
                pivotal = new Pivotal(getDescriptor().getAuthToken());
            }

            boolean doUpdate = build.getResult().isBetterOrEqualTo(Result.UNSTABLE);

            stories = getPivotalStories(ids);

            if (doUpdate) {
                postToPivotal(build, rootUrl, stories);
            } else {
                // this build didn't work, so carry forward the issues to the next build
                build.addAction(new PivotalCarryOverAction(stories));
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error updating Pivotal stories. Saving stories for next build.\n{0}", e);
            if (stories != null && !stories.isEmpty()) {
                // updating stories failed, so carry forward stories to the next build
                build.addAction(new PivotalCarryOverAction(stories));
            }
        }
        return true;
    }

    /**
     * Submit the build comment to the given Pivotal story. Removes from
     * <code>stories</code> the ones which were not found.
     *
     * @param build
     * @param logger
     * @param jenkinsRootUrl
     * @param stories
     * @throws RemoteException
     */
    static void postToPivotal(
            AbstractBuild<?, ?> build, String jenkinsRootUrl,
            List<PivotalStory> stories) throws Exception {

        // copy to prevent ConcurrentModificationException
        List<PivotalStory> copy = new ArrayList<PivotalStory>(stories);
        for (PivotalStory story : copy) {
            try {
                LOGGER.log(Level.FINE, Messages.PivotalPublisher_Updating(story.id));
                String comment = "<note><text>Integrated in " + build + " (See [" + Util.encode(jenkinsRootUrl + build.getUrl()) + "]</text></note>";
                pivotal.postNote(story.id, comment);
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Looks like {0} is not valid or we don''t have permission to update.\n", story.id);
                LOGGER.log(Level.WARNING, "Story will not be updated.\n{0}", e);
                stories.remove(story);
            }
        }
    }

    /**
     * Finds the strings that match Pivotal story ID patterns.
     *
     * This method returns all likely candidates and doesn't check if such ID
     * actually exists or not.
     */
    private static Set<String> findStoryIdsRecursive(AbstractBuild<?, ?> build, Pattern pattern,
            BuildListener listener) {
        Set<String> ids = new HashSet<String>();

        // first, issues that were carried forward.
        Run<?, ?> prev = build.getPreviousBuild();
        if (prev != null) {
            PivotalCarryOverAction a = prev.getAction(PivotalCarryOverAction.class);
            if (a != null) {
                ids.addAll(a.getIDs());
            }
        }

        // then issues in this build
        findIssues(build, ids, listener);

        // check for issues fixed in dependencies
        for (AbstractBuild.DependencyChange depc : build.getDependencyChanges(build.getPreviousBuild()).values()) {
            for (AbstractBuild<?, ?> b : depc.getBuilds()) {
                findIssues(b, ids, listener);
            }
        }

        return ids;
    }

    private static List<PivotalStory> getPivotalStories(
            Set<String> ids) throws RemoteException {
        List<PivotalStory> issues = new ArrayList<PivotalStory>(ids.size());
        for (String id : ids) {
            issues.add(new PivotalStory(id));
        }
        return issues;
    }

    /**
     * @param pattern pattern to use to match story ids
     */
    static void findIssues(AbstractBuild<?, ?> build, Set<String> ids,
            BuildListener listener) {
        for (Entry change : build.getChangeSet()) {
            LOGGER.log(Level.FINE, "Looking for Pivotal stories in {0}", change.getMsg());
            Matcher m = STORIE_PATTERN.matcher(change.getMsg());

            while (m.find()) {
                if (m.groupCount() >= 1) {
                    String content = StringUtils.upperCase(m.group(1));
                    ids.add(content);
                }
            }
        }
    }
    
    // Overridden for better type safety.
    // If your plugin doesn't really define any property on Descriptor,
    // you don't have to do this.
    @Override
    public DescriptorImpl getDescriptor() {
        return (DescriptorImpl)super.getDescriptor();
    }

    @Extension // This indicates to Jenkins that this is an implementation of an extension point.
    public static final class DescriptorImpl extends BuildStepDescriptor<Publisher> {
        
        public DescriptorImpl() {
            load();
        }

        private String authToken;

        public boolean isApplicable(Class<? extends AbstractProject> aClass) {
            // Indicates that this builder can be used with all kinds of project types 
            return true;
        }

        /**
         * This human readable name is used in the job configuration screen.
         */
        public String getDisplayName() {
            return "Update Pivotal Stories";
        }

        @Override
        public boolean configure(StaplerRequest req, JSONObject formData) throws FormException {
            authToken = (String)formData.get("authToken");
            save();
            return super.configure(req,formData);
        }

        public String getAuthToken(){
            System.out.println("Auth Token " + authToken);
            return authToken;
        }
    }
    private static final Logger LOGGER = Logger.getLogger(PivotalPublisher.class.getName());
}

