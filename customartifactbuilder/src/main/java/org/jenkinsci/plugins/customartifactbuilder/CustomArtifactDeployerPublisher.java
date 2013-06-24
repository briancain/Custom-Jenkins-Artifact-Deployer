package org.jenkinsci.plugins.customartifactbuilder;

import com.excilys.ebi.gatling.jenkins.GatlingBuildAction;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.*;

import org.apache.commons.io.FileUtils;
import org.jenkinsci.plugins.customartifactbuilder.service.*;
import org.jenkinsci.plugins.customartifactbuilder.exception.ArtifactDeployerException;

import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.matrix.MatrixAggregatable;
import hudson.matrix.MatrixAggregator;
import hudson.matrix.MatrixBuild;
import hudson.matrix.MatrixConfiguration;
import hudson.matrix.MatrixRun;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Action;
import hudson.model.BuildListener;
import hudson.model.Result;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Publisher;
import hudson.tasks.Recorder;
import hudson.util.FormValidation;

/*
 * 
 * Code based off of original ArtifactDeployerPublisher
 */
public class CustomArtifactDeployerPublisher extends Recorder implements MatrixAggregatable, Serializable{

	//private final String file;
    //private final String filedir;

    private List<ArtifactDeployerEntry> entries = Collections.emptyList();
    private boolean deployEvenBuildFail;
    
    // Fields in config.jelly must match the parameter names in the "DataBoundConstructor"
    // old constructor
    /*@DataBoundConstructor
    public CustomArtifactDeployerPublisher(String file, String filedir) {
        this.file = file;
        this.filedir = filedir;
    }*/
    
    // New constructor
    @DataBoundConstructor
    public CustomArtifactDeployerPublisher(List<ArtifactDeployerEntry> deployedArtifact, boolean deployEvenBuildFail) {
        this.entries = deployedArtifact;
        this.deployEvenBuildFail = deployEvenBuildFail;
        if(this.entries == null)
        	this.entries = Collections.emptyList();
    }
    
    public Object readResolve() {
    	if(this.entries == null)
    		this.entries = Collections.emptyList();
    	return this;
    }

    /**
     * We'll use this from the <tt>config.jelly</tt>.
     */
    /*public String getFile() {
        return file;
    }
    
    public String getFiledir(){
    	return filedir;
    }*/
    
	public BuildStepMonitor getRequiredMonitorService() {
		return BuildStepMonitor.NONE;
	}
	
	@Override
    public Collection<? extends Action> getProjectActions(AbstractProject<?, ?> project) {
        return Arrays.asList(new ArtifactDeployerProjectAction(project));
    }
	
	public MatrixAggregator createAggregator(MatrixBuild build, Launcher launcher, BuildListener listener) {
		// TODO Auto-generated method stub
        return new MatrixAggregator(build, launcher, listener) {

            @Override
            public boolean endRun(MatrixRun run) throws InterruptedException, IOException {
                boolean result = _perform(run, launcher, listener);
                run.save();
                return result;
            }

        };
    }
	
	private boolean isPerformDeployment(AbstractBuild build) {
        Result result = build.getResult();
        if (result == null) {
            return true;
        }

        if (deployEvenBuildFail) {
            return true;
        }

        return build.getResult().isBetterOrEqualTo(Result.UNSTABLE);
    }
	
	@Override
    public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) throws InterruptedException, IOException {
        // This is where you 'build' the project.
        // Since this is a dummy, we just say 'hello world' and call that a build.

        // This also shows how you can consult the global configuration of the builder
//	        if (getDescriptor().getUseFrench())
//	            listener.getLogger().println("Bonjour, "+name+"!");
//	        else
//	            listener.getLogger().println("Hello, "+name+"!");
        	
        listener.getLogger().println("[CustomArtifactDeployer] - Welcome to the custom artifact deployer post-build plugin.");
        /*listener.getLogger().println("[CustomArtifactDeployer] - The file you have picked is: " + file + ".");
        listener.getLogger().println("[CustomArtifactDeployer] - The file directory you have picked is: " + filedir + ".");
        
        //final FilePath workspace = build.getWorkspace();
        boolean deploy = processDeployment(build, launcher, listener, workspace);
        
        if (!deploy){
        	listener.getLogger().println("[CustomArtifactDeployer] - Deployment failed.");
        	return false;
        }
        else{
        	listener.getLogger().println("[CustomArtifactDeployer] - Deployment worked!");
        }*/
        if (!(build.getProject() instanceof MatrixConfiguration)) {
            return _perform(build, launcher, listener);
        }
       
        return true;
    }
	 
	private boolean _perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) throws IOException{
		if (isPerformDeployment(build)) {

			listener.getLogger().println("[CustomArtifactDeployer] - Starting deployment from the post-action ...");
			DeployedArtifactsActionManager deployedArtifactsService = DeployedArtifactsActionManager.getInstance();
			DeployedArtifacts deployedArtifactsAction = deployedArtifactsService.getOrCreateAction(build);
			Map<Integer, List<ArtifactDeployerVO>> deployedArtifacts;
			try {
				int currentTotalDeployedCounter = deployedArtifactsAction.getDeployedArtifactsInfo().size();
				deployedArtifacts = processDeployment(build, listener, currentTotalDeployedCounter);
			} catch (ArtifactDeployerException ae) {
				listener.getLogger().println("[CustomArtifactDeployer] - [ERROR] - Failed to deploy. " + ae.getMessage());
				if (ae.getCause() != null) {
					listener.getLogger().println("[CustomArtifactDeployer] - [ERROR] - " + ae.getCause().getMessage());
				}
				build.setResult(Result.FAILURE);
				return false;
			}

			deployedArtifactsAction.addDeployedArtifacts(deployedArtifacts);
			listener.getLogger().println("[CustomArtifactDeployer] - Stopping deployment from the post-action...");
		}
		
		boolean succ = getAction(build, launcher, listener);
		
		return true;
	}
	
	private boolean getAction(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) throws IOException{
		// get action - gatling build action
		listener.getLogger().println("[CustomArtifactDeployer] - Going to get Gatling Build Actions...");
		List<GatlingBuildAction> gba_lst = build.getActions(GatlingBuildAction.class);
		GatlingBuildAction action = gba_lst.get(0);
		int action_lst_size = action.getSimulations().size();
		FilePath simdir = action.getSimulations().get(0).getSimulationDirectory();
		String file_contents_path = simdir + "/stats.tsv";
		
		listener.getLogger().println("[CustomArtifactDeployer] - It worked without errors..maybe... " + action_lst_size);
		listener.getLogger().println("[CustomArtifactDeployer] - The simulation directory is: " + simdir);
		listener.getLogger().println("[CustomArtifactDeployer] - The file contents path is: " + file_contents_path);
		// Open file object from simulation directory, get stats.tsv, parse it to obtain list of first things in line, then save as artifact
		
		// Open file, save to string, then split contents on tab
		String file_contents = FileUtils.readFileToString(new File(file_contents_path));
		String[] tokens = file_contents.split("\t");
		
		// Original File contents, then tokenized contents
		listener.getLogger().println("[CustomArtifactDeployer] - The file contents is: \n" + file_contents);
		for(String token: tokens){
			listener.getLogger().println("[CustomArtifactDeployer] - Here's a token: " + token + "\n");
		}
		
		// Save to artifact Build Action?
		// savefullreports function
		
		// CustomProjectAction action = new CustomProjectAction(build, sims);
		// build.addaction(myaction);
		return true;
	}
	
	private Map<Integer, List<ArtifactDeployerVO>> processDeployment(AbstractBuild<?, ?> build, final BuildListener listener, int currentNbDeployedArtifacts) throws ArtifactDeployerException {
		Map<Integer, List<ArtifactDeployerVO>> deployedArtifacts = new HashMap<Integer, List<ArtifactDeployerVO>>();
        FilePath workspace = build.getWorkspace();
        
        int numberOfCurrentDeployedArtifacts = currentNbDeployedArtifacts;
        for (final ArtifactDeployerEntry entry : entries){
        	if (entry.getRemote() == null){
        		throw new ArtifactDeployerException("All remote directories must be set.");
        	}
        	
        	final String includes;
            final String excludes;
            final String basedir;
            final String outputPath;
            try {
                includes = build.getEnvironment(listener).expand(entry.getIncludes());
                excludes = build.getEnvironment(listener).expand(entry.getExcludes());
                basedir = build.getEnvironment(listener).expand(entry.getBasedir());
                outputPath = build.getEnvironment(listener).expand(entry.getRemote());
            } catch (IOException ioe) {
                throw new ArtifactDeployerException(ioe);
            } catch (InterruptedException ie) {
                throw new ArtifactDeployerException(ie);
            }
            
            final boolean flatten = entry.isFlatten();
            
          //Creating the remote directory
            final FilePath outputFilePath = new FilePath(workspace.getChannel(), outputPath);
            try {
                outputFilePath.mkdirs();
            } catch (IOException ioe) {
                throw new ArtifactDeployerException(String.format("Can't create the directory '%s'", outputPath), ioe);
            } catch (InterruptedException ie) {
                throw new ArtifactDeployerException(String.format("Can't create the directory '%s'", outputPath), ie);
            }

            //Deleting files to remote directory if necessary
            boolean deletedPreviously = entry.isDeleteRemote();
            if (deletedPreviously) {
                try {
                    outputFilePath.deleteContents();
                } catch (IOException ioe) {
                    throw new ArtifactDeployerException(String.format("Can't delete contents of '%s'", outputPath), ioe);
                } catch (InterruptedException ie) {
                    throw new ArtifactDeployerException(String.format("Can't delete contents of '%s'", outputPath), ie);
                }
            }


            ArtifactDeployerCopy deployerCopy =
                    new ArtifactDeployerCopy(listener, includes, excludes, flatten, outputFilePath, numberOfCurrentDeployedArtifacts);
            ArtifactDeployerManager deployerManager = new ArtifactDeployerManager();
            FilePath basedirFilPath = deployerManager.getBasedirFilePath(workspace, basedir);
            List<ArtifactDeployerVO> results;
            try {
                results = basedirFilPath.act(deployerCopy);
            } catch (IOException ioe) {
                throw new ArtifactDeployerException(ioe);
            } catch (InterruptedException ie) {
                throw new ArtifactDeployerException(ie);
            }

            if (isFailNoFilesDeploy(results, entry)) {
                throw new ArtifactDeployerException("Can't find any artifacts to deploy with the following configuration :"
                        + printConfiguration(includes, excludes, basedirFilPath.getRemote(), outputPath));
            }

            numberOfCurrentDeployedArtifacts += results.size();
            deployedArtifacts.put(entry.getUniqueId(), results);
        }
        return deployedArtifacts;
	}
	
	private boolean isFailNoFilesDeploy(List<ArtifactDeployerVO> results, ArtifactDeployerEntry entry) {
		return ((results == null || results.size() == 0) && entry.isFailNoFilesDeploy());
	}
	
	 private String printConfiguration(String includes, String excludes, String basedir, String outputPath) {
		 StringBuffer sb = new StringBuffer();

		 if (includes != null) {
			 sb.append(",includes:").append(includes);
		 }
		 if (excludes != null) {
			 sb.append(",excludes:").append(excludes);
		 }

		 sb.append(",basedir:").append(basedir);
		 sb.append(",outPath:").append(outputPath);
		 sb.append("]");

		 sb = sb.replace(0, 1, "[");

		 return sb.toString();
	}
	
	 @SuppressWarnings("unused")
	 public List<ArtifactDeployerEntry> getEntries() {
		 return entries;
	 }

	 public void setEntries(List<ArtifactDeployerEntry> entries) {
		 this.entries = entries;
	 }

	 public boolean isDeployEvenBuildFail() {
		 return deployEvenBuildFail;
	 }

	 public void setDeployEvenBuildFail(boolean deployEvenBuildFail) {
		 this.deployEvenBuildFail = deployEvenBuildFail;
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
		
		public FormValidation doCheckIncludes(@AncestorInPath AbstractProject project, @QueryParameter String value) throws IOException {
            return FilePath.validateFileMask(project.getSomeWorkspace(), value);
        }
		
		public FormValidation doCheckRemote(@QueryParameter String value) throws IOException {
            if (value == null || value.trim().length() == 0) {
                return FormValidation.error("Remote directory is mandatory.");
            }
            return FormValidation.ok();
        }
	}
}
