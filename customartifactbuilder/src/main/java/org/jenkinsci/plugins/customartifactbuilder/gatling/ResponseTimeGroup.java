package org.jenkinsci.plugins.customartifactbuilder.gatling;

public class ResponseTimeGroup {

	private String name;
	private int count;
	private int percentage;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public int getPercentage() {
		return percentage;
	}

	public void setPercentage(int percentage) {
		this.percentage = percentage;
	}
}