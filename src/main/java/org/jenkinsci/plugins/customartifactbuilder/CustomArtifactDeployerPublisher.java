package org.jenkinsci.plugins.customartifactbuilder;

import java.io.IOException;

import org.kohsuke.stapler.DataBoundConstructor;

import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Publisher;
import hudson.tasks.Recorder;

public class CustomArtifactDeployerPublisher extends Recorder {

	private final String file;
    private final String filedir;

    // Fields in config.jelly must match the parameter names in the "DataBoundConstructor"
    @DataBoundConstructor
    public CustomArtifactDeployerPublisher(String file, String filedir) {
        this.file = file;
        this.filedir = filedir;
    }

    /**
     * We'll use this from the <tt>config.jelly</tt>.
     */
    public String getFile() {
        return file;
    }
    
    public String getFiledir(){
    	return filedir;
    }
    
	public BuildStepMonitor getRequiredMonitorService() {
		// TODO Auto-generated method stub
		return BuildStepMonitor.NONE;
	}
	
	@Override
    public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) throws InterruptedException {
        // This is where you 'build' the project.
        // Since this is a dummy, we just say 'hello world' and call that a build.

        // This also shows how you can consult the global configuration of the builder
//	        if (getDescriptor().getUseFrench())
//	            listener.getLogger().println("Bonjour, "+name+"!");
//	        else
//	            listener.getLogger().println("Hello, "+name+"!");
        	
        listener.getLogger().println("[CustomArtifactDeployer] - Welcome to the custom artifact deployer post-build plugin.");
        listener.getLogger().println("[CustomArtifactDeployer] - The file you have picked is: " + file + ".");
        listener.getLogger().println("[CustomArtifactDeployer] - The file directory you have picked is: " + filedir + ".");
        
        final FilePath workspace = build.getWorkspace();
        boolean deploy = processDeployment(build, launcher, listener, workspace);
        
        if (!deploy){
        	listener.getLogger().println("[CustomArtifactDeployer] - Deployment failed.");
        	return false;
        }
        else{
        	listener.getLogger().println("[CustomArtifactDeployer] - Deployment worked!");
        }
       
        return true;
    }
	 
	private boolean processDeployment(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener, FilePath workspace) throws InterruptedException {
		//Creating the remote directory
		//listener.getLogger().println("[CustomArtifactDeployer] - The given path is:\n" + workspace);

		if (filedir == null){
			listener.getLogger().println("[CustomArtifactDeployer] - A directory must be set.");
			return false;
		}

		//Creating the remote directory
		listener.getLogger().println("[CustomArtifactDeployer] - Begin file directory creation.");
		final FilePath outputFilePath = new FilePath(workspace.getChannel(), filedir);
		try {
			outputFilePath.mkdirs();
		} catch (IOException ioe) {
			listener.getLogger().println("[CustomArtifactDeployer] - Making dir fails.");
			return false;
		}

		//Deleting files to remote directory if necessary
		/*boolean deletedPreviously = entry.isDeleteRemote();
	        if (deletedPreviously) {
	            try {
	                outputFilePath.deleteContents();
	            } catch (IOException ioe) {
	                throw new ArtifactDeployerException(String.format("Can't delete contents of '%s'", outputPath), ioe);
	            }
	        }*/
		
		// ArtifactManager files
		// Save them

		return true;
	}
	
	@Extension
    public static final class CustomArtifactDeployerDescriptor extends BuildStepDescriptor<Publisher> {
		public static final String DISPLAY_NAME = "Custom Artifact Deployer";
		
		@Override
		public boolean isApplicable(Class<? extends AbstractProject> jobType) {
			// TODO Auto-generated method stub
			return true;
		}

		@Override
		public String getDisplayName() {
			// TODO Auto-generated method stub
			return DISPLAY_NAME;
		}
		
	}
}
