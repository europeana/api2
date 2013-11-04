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

import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.Scheduled;

import javax.annotation.Resource;
import org.slf4j.Logger;
import eu.europeana.corelib.logging.Log;

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
	
	public SugarCRMPollingScheduler(){
		
	}
	
	
	@Scheduled(fixedRate=5000)
	public void pollSugarforChanges() {

	}
	
}
