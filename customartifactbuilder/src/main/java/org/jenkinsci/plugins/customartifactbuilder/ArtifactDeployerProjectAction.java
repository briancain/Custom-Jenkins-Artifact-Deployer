package org.jenkinsci.plugins.customartifactbuilder;

import hudson.model.AbstractProject;
import hudson.model.Action;
import hudson.model.Run;

import java.util.List;

/**
 * @author Gregory Boissinot
 */
public class ArtifactDeployerProjectAction implements Action {

    private final AbstractProject<?, ?> project;

    public ArtifactDeployerProjectAction(AbstractProject<?, ?> project) {
        this.project = project;
    }

    private Run getLastSuccessfulBuild() {
        return project.getLastSuccessfulBuild();
    }
    
    public AbstractProject<?, ?> getProject() {
    	return project;
    }

    @SuppressWarnings("unused")
    public DeployedArtifacts getLatestDeployedArtifacts() {
        Run latestSuccessfulBuild = getLastSuccessfulBuild();
        if (latestSuccessfulBuild == null) {
            return null;
        }
        List<DeployedArtifacts> actions = latestSuccessfulBuild.getActions(DeployedArtifacts.class);
        if (actions == null || actions.size() == 0) {
            return null;
        }
        return actions.get(actions.size() - 1);
    }

    @SuppressWarnings("unused")
    public int getLastSuccessfulNumber() {
        Run latestSuccessfulBuild = getLastSuccessfulBuild();
        if (latestSuccessfulBuild == null) {
            return 0;
        }
        return latestSuccessfulBuild.getNumber();
    }

    public String getIconFileName() {
		return "/plugin/customartifactbuilder/img/puppet.png";
	}

	public String getDisplayName() {
		return "Custom Gatling";
	}

	public String getUrlName() {
		return "cgatling";
	}
}