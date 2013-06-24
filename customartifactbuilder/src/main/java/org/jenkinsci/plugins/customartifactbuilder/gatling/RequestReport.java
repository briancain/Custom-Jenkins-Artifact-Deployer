package org.jenkinsci.plugins.customartifactbuilder.gatling;

import com.fasterxml.jackson.annotation.JsonProperty;

public class RequestReport {

	private String name;
	private Statistics numberOfRequests;
	private Statistics minResponseTime;
	private Statistics maxResponseTime;
	private Statistics meanResponseTime;
	private Statistics standardDeviation;
	@JsonProperty("percentiles1")
	private Statistics percentiles95;
	@JsonProperty("percentiles2")
	private Statistics percentiles99;
	private Statistics meanNumberOfRequestsPerSecond;
	private ResponseTimeGroup group1;
	private ResponseTimeGroup group2;
	private ResponseTimeGroup group3;
	private ResponseTimeGroup group4;

	public Statistics getNumberOfRequests() {
		return numberOfRequests;
	}

	public void setNumberOfRequests(Statistics numberOfRequests) {
		this.numberOfRequests = numberOfRequests;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Statistics getMinResponseTime() {
		return minResponseTime;
	}

	public void setMinResponseTime(Statistics minResponseTime) {
		this.minResponseTime = minResponseTime;
	}

	public Statistics getMaxResponseTime() {
		return maxResponseTime;
	}

	public void setMaxResponseTime(Statistics maxResponseTime) {
		this.maxResponseTime = maxResponseTime;
	}

	public Statistics getMeanResponseTime() {
		return meanResponseTime;
	}

	public void setMeanResponseTime(Statistics meanResponseTime) {
		this.meanResponseTime = meanResponseTime;
	}

	public Statistics getStandardDeviation() {
		return standardDeviation;
	}

	public void setStandardDeviation(Statistics standardDeviation) {
		this.standardDeviation = standardDeviation;
	}

	public Statistics getPercentiles95() {
		return percentiles95;
	}

	public void setPercentiles95(Statistics percentiles95) {
		this.percentiles95 = percentiles95;
	}

	public Statistics getPercentiles99() {
		return percentiles99;
	}

	public void setPercentiles99(Statistics percentiles99) {
		this.percentiles99 = percentiles99;
	}

	public Statistics getMeanNumberOfRequestsPerSecond() {
		return meanNumberOfRequestsPerSecond;
	}

	public void setMeanNumberOfRequestsPerSecond(Statistics meanNumberOfRequestsPerSecond) {
		this.meanNumberOfRequestsPerSecond = meanNumberOfRequestsPerSecond;
	}

	public ResponseTimeGroup getGroup1() {
		return group1;
	}

	public void setGroup1(ResponseTimeGroup group1) {
		this.group1 = group1;
	}

	public ResponseTimeGroup getGroup2() {
		return group2;
	}

	public void setGroup2(ResponseTimeGroup group2) {
		this.group2 = group2;
	}

	public ResponseTimeGroup getGroup3() {
		return group3;
	}

	public void setGroup3(ResponseTimeGroup group3) {
		this.group3 = group3;
	}

	public ResponseTimeGroup getGroup4() {
		return group4;
	}

	public void setGroup4(ResponseTimeGroup group4) {
		this.group4 = group4;
	}
}