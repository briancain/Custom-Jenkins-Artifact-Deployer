package org.jenkinsci.plugins.customartifactbuilder;

import hudson.FilePath;
import hudson.model.AbstractBuild;
import hudson.model.Action;
import hudson.model.AbstractProject;

public class CustomProjectAction implements Action{
	
	private final FilePath filepath;
	
	// file path
	public CustomProjectAction(FilePath filepath) {
		this.filepath = filepath;
	}

	public String getIconFileName() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getDisplayName() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getUrlName() {
		// TODO Auto-generated method stub
		return null;
	}
}
