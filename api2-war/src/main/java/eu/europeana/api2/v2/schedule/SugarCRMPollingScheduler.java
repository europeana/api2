/*
 * Copyright 2007-2015 The Europeana Foundation
 *
 * Licenced under the EUPL, Version 1.1 (the "Licence") and subsequent versions as approved
 * by the European Commission;
 * You may not use this work except in compliance with the Licence.
 *
 * You may obtain a copy of the Licence at:
 * http://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software distributed under
 * the Licence is distributed on an "AS IS" basis, without warranties or conditions of
 * any kind, either express or implied.
 * See the Licence for the specific language governing permissions and limitations under
 * the Licence.
 */
package eu.europeana.api2.v2.schedule;

import java.util.concurrent.ScheduledFuture;

import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.TaskScheduler;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import eu.europeana.api2.v2.service.SugarCRMImporter;
import eu.europeana.corelib.logging.Logger;
import eu.europeana.corelib.logging.Log;
import eu.europeana.uim.sugarcrmclient.ws.exceptions.JIXBQueryResultException;

/**
 * The Scheduler that maintains the timely population of the MongoDB-based cache
 * from SugarCRM.
 * 
 * @author Georgios Markakis (gwarkx@hotmail.com)
 *
 * @since Oct 30, 2013
 */
public class SugarCRMPollingScheduler {

	@Log
	private Logger log;

	@Resource
	private SugarCRMImporter sugarCRMImporter;

	@Resource(name="sugarcrm_taskScheduler")
	private TaskScheduler scheduler;

	@Resource(name="sugarcrm_taskExecutor")
	private TaskExecutor executor;

	/**
	 * Default Constructor
	 */
	public SugarCRMPollingScheduler() {}

	/**
	 * The frequently invoked task (updates everything that has been recently updated 
	 * in CRM)
	 */
	private ScheduledFuture<?> frequentUpdateTask;

	/**
	 * The nightly invoked task (updates everything that has been updated in CRM for
	 * the last 24 hours)
	 */
	private ScheduledFuture<?> nightlyUpdateTask;

	/**
	 * Initializes the schedulers
	 */
	@PostConstruct
	public void scheduleFirstRun() {
		try {
			sugarCRMImporter.populateRepositoryFromScratch();
		} catch (JIXBQueryResultException e) {
			e.printStackTrace();
			log.error("Re-population of MongoDB Cache from SugarCRM failed: " + e.getMessage());
		}
		frequentUpdateTask = scheduler.scheduleAtFixedRate(new FrequentUpdateTask(), 100000);
		// nightlyUpdateTask = scheduler.scheduleAtFixedRate(new NigthlyUpdateTask(), 50000000);
	}

	/**
	 * The frequent task implementation
	 */
	private class FrequentUpdateTask implements Runnable {
		@Override
		public void run() {
			try {
				sugarCRMImporter.pollProviders();
				sugarCRMImporter.pollCollections();
			} catch (JIXBQueryResultException e) {
				e.printStackTrace();
				log.error("Frequently scheduled update for provider/collections failed: " + e.getMessage());
			}
		}
	}

	/**
	 * The nightly task implementation
	 */
	private class NigthlyUpdateTask implements Runnable {
		@Override
		public void run() {
			try {
				sugarCRMImporter.pollProviders();
				sugarCRMImporter.pollCollections();
			} catch (JIXBQueryResultException e) {
				e.printStackTrace();
				log.error("Frequently scheduled update for provider/collections failed: " + e.getMessage());
			}
		}
	}
}
