/*
 * Copyright 2007-2012 The Europeana Foundation
 *
 *  Licenced under the EUPL, Version 1.1 (the "Licence") and subsequent versions as approved 
 *  by the European Commission;
 *  You may not use this work except in compliance with the Licence.
 *  
 *  You may obtain a copy of the Licence at:
 *  http://joinup.ec.europa.eu/software/page/eupl
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under 
 *  the Licence is distributed on an "AS IS" basis, without warranties or conditions of 
 *  any kind, either express or implied.
 *  See the Licence for the specific language governing permissions and limitations under 
 *  the Licence.
 */
package eu.europeana.api2.v2.web.controller.sugarcrm;

import java.util.concurrent.ScheduledFuture;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.TaskScheduler;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import org.slf4j.Logger;
import eu.europeana.corelib.logging.Log;
import eu.europeana.uim.sugarcrmclient.ws.SugarWsClient;
import eu.europeana.uim.sugarcrmclient.ws.exceptions.JIXBQueryResultException;

/**
 * @author Georgios Markakis (gwarkx@hotmail.com)
 *
 * @since Oct 30, 2013
 */
public class SugarCRMPollingScheduler {


	@Log
	private Logger log;

	@Resource
	private SugarCRMCache sugarCRMCache;
	
    @Resource(name="sugarcrm_taskScheduler")
    private  TaskScheduler scheduler;

    @Resource(name="sugarcrm_taskExecutor")
    private  TaskExecutor executor;

	public SugarCRMPollingScheduler(){
	}

	
	public SugarCRMPollingScheduler(SugarCRMCache sugarCRMCache){
		this.sugarCRMCache = sugarCRMCache;
	}
	
	
	private ScheduledFuture<?> frequentUpdateTask;
    private ScheduledFuture<?> nightlyUpdateTask;

	
	
    @PostConstruct
    public void scheduleFirstRun() {
    	try {
			sugarCRMCache.populateRepositoryFromScratch();
		} catch (JIXBQueryResultException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	frequentUpdateTask = scheduler.scheduleAtFixedRate(new FrequentUpdateTask(), 15000);
    	//nightlyUpdateTask = scheduler.scheduleAtFixedRate(new NigthlyUpdateTask(), 50000000);
    	
    }
	
	
	 /**
     *
     */
    private class FrequentUpdateTask implements Runnable {
            @Override
            public void run() {
            	try {
					sugarCRMCache.pollProviders();
					sugarCRMCache.pollCollections();
				} catch (JIXBQueryResultException e) {
					e.printStackTrace();
					log.error("Frequently scheduled update for provider/collections failed: " + e.getMessage());
				}
            }
    }
    
	 /**
    *
    */
   private class NigthlyUpdateTask implements Runnable {
           @Override
           public void run() {
           	try {
					sugarCRMCache.pollProviders();
					sugarCRMCache.pollCollections();
				} catch (JIXBQueryResultException e) {
					e.printStackTrace();
					log.error("Frequently scheduled update for provider/collections failed: " + e.getMessage());
				}
           }
   }
    
    
	
}
