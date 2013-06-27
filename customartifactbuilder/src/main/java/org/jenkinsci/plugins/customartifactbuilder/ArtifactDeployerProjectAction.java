package org.jenkinsci.plugins.customartifactbuilder;

import static com.excilys.ebi.gatling.jenkins.PluginConstants.MAX_BUILDS_TO_DISPLAY_DASHBOARD;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Action;
import hudson.model.Run;

import java.util.List;

import org.jenkinsci.plugins.customartifactbuilder.chart.Graph;

import com.excilys.ebi.gatling.jenkins.RequestReport;
import com.excilys.ebi.gatling.jenkins.GatlingBuildAction;

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
    
    public boolean isVisible() {
		for (AbstractBuild<?, ?> build : getProject().getBuilds()) {
			GatlingBuildAction gatlingBuildAction = build.getAction(GatlingBuildAction.class);
			if (gatlingBuildAction != null) {
				return true;
			}
		}
		return false;
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
    
    public Graph<Long> getdashboardGraph() {
    	return new Graph<Long>(project, MAX_BUILDS_TO_DISPLAY_DASHBOARD) {
			@Override
			public Long getValue(RequestReport requestReport) {
				return requestReport.getMeanResponseTime().getTotal();
			}

			@Override
			protected Long getValue(int i) {
				// TODO Auto-generated method stub
				Long ret = new Long(i);
				return ret;
			}
		};
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
