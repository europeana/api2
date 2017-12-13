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

import eu.europeana.api2.v2.service.SugarCRMImporter;
import eu.europeana.uim.sugarcrmclient.ws.exceptions.JIXBQueryResultException;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.springframework.scheduling.annotation.Scheduled;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

/**
 * The Scheduler that maintains the timely population of the MongoDB-based cache
 * from SugarCRM.
 *
 * @author Georgios Markakis (gwarkx@hotmail.com)
 * @since Oct 30, 2013
 */
public class SugarCRMPollingScheduler {

    private static final Logger LOG = LogManager.getLogger(SugarCRMPollingScheduler.class);

    @Resource
    private SugarCRMImporter sugarCRMImporter;

    private boolean firstRunComplete = false;

    /**
     * Initializes the schedulers
     */
    @PostConstruct
    public void scheduleFirstRun() {
        try {
            sugarCRMImporter.populateRepositoryFromScratch();
        } catch (JIXBQueryResultException e) {
            LOG.error("Re-population of MongoDB Cache from SugarCRM failed: {}", e.getMessage(), e);
        }
        firstRunComplete = true;
    }

    @Scheduled(fixedRate = 300_000)
    public void frequentUpdateTask() {
        if (firstRunComplete) {
            try {
                sugarCRMImporter.pollProviders();
                sugarCRMImporter.pollCollections();
            } catch (JIXBQueryResultException e) {
                LOG.error("Scheduled update for provider/collections failed: {}", e.getMessage(), e);
            }
        }
    }

}
