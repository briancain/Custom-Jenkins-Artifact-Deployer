package org.jenkinsci.plugins.customartifactbuilder;

import org.jenkinsci.plugins.customartifactbuilder.service.*;
import org.jenkinsci.plugins.customartifactbuilder.exception.ArtifactDeployerException;
import org.jenkinsci.plugins.customartifactbuilder.gatling.RequestReport;
import org.jenkinsci.plugins.customartifactbuilder.gatling.CustomBuildAction;

import com.excilys.ebi.gatling.jenkins.BuildSimulation;
import com.excilys.ebi.gatling.jenkins.GatlingBuildAction;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.io.Serializable;
import java.util.*;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;

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
 * Brian Cain
 * Code based off of original ArtifactDeployerPublisher
 */
public class CustomArtifactDeployerPublisher extends Recorder implements MatrixAggregatable, Serializable{

    private List<ArtifactDeployerEntry> entries = Collections.emptyList();
    private boolean deployEvenBuildFail;
    private PrintStream logger;
    
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
    
	public BuildStepMonitor getRequiredMonitorService() {
		return BuildStepMonitor.NONE;
	}
	
	@Override
    public Collection<? extends Action> getProjectActions(AbstractProject<?, ?> project) {
        return Arrays.asList(new ArtifactDeployerProjectAction(project));
    }
	
	public MatrixAggregator createAggregator(MatrixBuild build, Launcher launcher, BuildListener listener) {
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
		logger = listener.getLogger();
        logger.println("[CustomArtifactDeployer] - Welcome to the custom artifact deployer post-build plugin.");
        
        if (!(build.getProject() instanceof MatrixConfiguration)) {
            return _perform(build, launcher, listener);
        }
       
        return true;
    }
	 
	private boolean _perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) throws IOException, InterruptedException{
		if (isPerformDeployment(build)) {

			logger.println("[CustomArtifactDeployer] - Starting deployment from the post-action ...");
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
			logger.println("[CustomArtifactDeployer] - Stopping deployment from the post-action...");
		}
		
		boolean succ = getBuildAction(build, launcher, listener);
		
		if (!succ){
			logger.println("[CustomArtifactDeployer] - Get Build Action failed.");
			return succ;
		}
		
		return succ;
	}
	
	private boolean getBuildAction(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) throws IOException, InterruptedException{
		logger.println("[CustomArtifactDeployer] - Going to get Gatling Build Actions...");
		List<GatlingBuildAction> gba_lst = build.getActions(GatlingBuildAction.class);
		
		if (gba_lst.size() == 0){
			return false;
		}
		GatlingBuildAction action = gba_lst.get(0);
		int action_lst_size = action.getSimulations().size();
		
		
		List<RequestReport> rrList = new ArrayList<RequestReport>();
		int simulationCounter = 0;
		for (BuildSimulation sim : action.getSimulations()){
			FilePath simdir = action.getSimulations().get(simulationCounter).getSimulationDirectory();
			String stats_file_contents_path = simdir + "/stats.tsv";
			
			logger.println("[CustomArtifactDeployer] - It worked without errors..maybe... " + action_lst_size);
			logger.println("[CustomArtifactDeployer] - The simulation directory is: " + simdir);
			logger.println("[CustomArtifactDeployer] - The stats file contents path is: " + stats_file_contents_path);
			
			List<Integer> calcList = getCalculations(stats_file_contents_path);
			
			RequestReport requestReport = new RequestReport();
			requestReport.setMeanAgentRunTime(calcList.get(2).longValue());
			requestReport.setMeanCatalogCompileTime(calcList.get(1).longValue());
			requestReport.setName(sim.getSimulationName());
			simulationCounter++;
			rrList.add(requestReport);
		}
		
		CustomBuildAction customAction = new CustomBuildAction(build, rrList);
		build.addAction(customAction);
		return true;
	}
	
	private List<Integer> getCalculations(String statsFilePath) throws IOException{
		List<Integer> calcList = new ArrayList<Integer>();

		LineIterator it = FileUtils.lineIterator(new File(statsFilePath));
		List<String> ls_tokens = new ArrayList<String>();

		int globTotal = 0;
		int catMean = 0;
		int totalMean = 0;

		try{
			while(it.hasNext()){
				String line = it.nextLine();
				String[] tmp_toke = line.split("\t");
				if (tmp_toke[0].equals("Global Information")){
					logger.println("[CustomArtifactDeployer] - Global Information values Total: " + tmp_toke[1]);
					globTotal = Integer.parseInt(tmp_toke[1]);
				}
				else if (tmp_toke[0].equals("catalog")){
					logger.println("[CustomArtifactDeployer] - Catalog Info Mean: " + tmp_toke[12]);
					catMean = Integer.parseInt(tmp_toke[12]);
					totalMean += Integer.parseInt(tmp_toke[12]);
				}
				else if(tmp_toke.length > 1 && !tmp_toke[0].equals("name")){
					logger.println("[CustomArtifactDeployer] - Means: " + tmp_toke[0] + ": " + tmp_toke[12]);
					totalMean += Integer.parseInt(tmp_toke[12]);
				}
			}
		} finally{
			it.close();
		}

		logger.println("[CustomArtifactDeployer] - Here are the values parsed: \n" + globTotal + "\n" + catMean + "\n" + totalMean);
		calcList.add(globTotal);
		calcList.add(catMean);
		calcList.add(totalMean);
		return calcList;
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