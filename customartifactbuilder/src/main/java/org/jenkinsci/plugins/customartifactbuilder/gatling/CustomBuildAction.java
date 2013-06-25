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
		return "/plugin/customartifactbuilder/img/puppet.png";
	}

	public String getDisplayName() {
		return "Customg Gatling";
	}

	public String getUrlName() {
		return "cgatling";
	}
	
	public FilePath getFilePath(){
		return filePath;
	}
	
	//FileUtils.copyFileToDirectory(new File(localBasedir, includes), new File(remote));
}