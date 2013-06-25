package org.jenkinsci.plugins.customartifactbuilder.gatling;

import java.io.File;
import java.util.List;

import org.apache.commons.io.FileUtils;

import hudson.FilePath;
import hudson.model.Action;
import hudson.model.AbstractBuild;

public class CustomBuildAction implements Action {
	
	private final FilePath filePath;
	
	public CustomBuildAction(FilePath filePath){
		this.filePath = filePath;
	}

	public String getIconFileName() {
		// TODO Auto-generated method stub
		return "/plugin/customartifactbuilder/img/puppet.png";
	}

	public String getDisplayName() {
		// TODO Auto-generated method stub
		return "Customg Gatling";
	}

	public String getUrlName() {
		// TODO Auto-generated method stub
		return "cgatling";
	}
	
	//FileUtils.copyFileToDirectory(new File(localBasedir, includes), new File(remote));
}
