package org.jenkinsci.plugins.customartifactbuilder.gatling;

public class RequestReport {

	private Long meanAgentRunTime;
	private Long meanCatalogCompileTime;

	public Long getMeanAgentRunTime() {
		return meanAgentRunTime;
	}

	public void setMeanAgentRunTime(Long meanAgentRunTime) {
		this.meanAgentRunTime = meanAgentRunTime;
	}
	
	public Long getMeanCatalogCompileTime() {
		return meanCatalogCompileTime;
	}

	public void setMeanCatalogCompileTime(Long meanCatalogCompileTime) {
		this.meanCatalogCompileTime = meanCatalogCompileTime;
	}
	
}