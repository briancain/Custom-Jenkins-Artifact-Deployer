/**
 * Copyright 2011-2012 eBusiness Information, Groupe Excilys (www.excilys.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 		http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jenkinsci.plugins.customartifactbuilder.chart;

import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;

import java.io.IOException;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.excilys.ebi.gatling.jenkins.GatlingBuildAction;
import com.excilys.ebi.gatling.jenkins.chart.Serie;
import com.excilys.ebi.gatling.jenkins.chart.SerieName;
import com.excilys.ebi.gatling.jenkins.BuildSimulation;
import com.excilys.ebi.gatling.jenkins.RequestReport;
import com.fasterxml.jackson.databind.ObjectMapper;

public abstract class Graph<Y extends Number> {
	private static final Logger LOGGER = Logger.getLogger(Graph.class.getName());

	private final SortedMap<SerieName, Serie<Integer, Y>> series = new TreeMap<SerieName, Serie<Integer, Y>>();

	private final ObjectMapper mapper = new ObjectMapper();

	public Graph(AbstractProject<?, ?> project, int maxBuildsToDisplay) {
		int numberOfBuild = 0;
		series.put(new SerieName("Puppet Test"), new Serie<Integer, Y>());
		for (int x = 0; x <= 10; x++){
			series.get(new SerieName("Puppet Test")).addPoint((Integer)x, getValue(4));
		}
	}

	protected abstract Y getValue(int i);

	public String getSeriesNamesJSON() {
		String json = null;

		try {
			json = mapper.writeValueAsString(series.keySet());
		} catch (IOException e) {
			LOGGER.log(Level.INFO, e.getMessage(), e);
		}
		return json;
	}

	public String getSeriesJSON() {
		String json = null;

		try {
			json = mapper.writeValueAsString(series.values());
		} catch (IOException e) {
			LOGGER.log(Level.INFO, e.getMessage(), e);
		}
		return json;
	}

	protected abstract Y getValue(RequestReport requestReport);
}
