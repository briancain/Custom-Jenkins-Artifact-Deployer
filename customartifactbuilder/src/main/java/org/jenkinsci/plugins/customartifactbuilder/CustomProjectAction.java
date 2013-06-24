package org.jenkinsci.plugins.customartifactbuilder;

import hudson.model.AbstractBuild;
import hudson.model.Action;
import hudson.model.AbstractProject;

public class CustomProjectAction implements Action{
	
	private final AbstractProject<?, ?> project;
	
	public CustomProjectAction(AbstractProject<?, ?> project) {
		this.project = project;
	}
	
	public AbstractProject<?, ?> getProject(){
		return project;
	}

	public String getIconFileName() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getDisplayName() {
		// TODO Auto-generated method stub
		return "Custom Gatling Project";
	}

	public String getUrlName() {
		// TODO Auto-generated method stub
		return null;
	}

}
