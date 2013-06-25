package org.jenkinsci.plugins.customartifactbuilder.gatling;

import java.io.File;
import java.util.List;

import org.apache.commons.io.FileUtils;

import hudson.FilePath;
import hudson.model.Action;
import hudson.model.AbstractBuild;

public class CustomBuildAction implements Action {
	
	private final FilePath filePath;
	private final AbstractBuild<?, ?> build;
	
	public CustomBuildAction(AbstractBuild<?, ?> build, FilePath filePath){
		this.build = build;
		this.filePath = filePath;
	}
	
	public AbstractBuild<?, ?> getbuild(){
		return build;
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