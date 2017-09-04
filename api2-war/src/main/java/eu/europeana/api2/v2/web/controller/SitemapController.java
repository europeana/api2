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

package eu.europeana.api2.v2.web.controller;

import eu.europeana.api2.v2.web.SiteMapNotFoundException;
import eu.europeana.features.ObjectStorageClient;
import org.apache.log4j.Logger;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Handles requests for sitemap files
 * @author luthien, created on 07/12/2015.
 * @author Patrick Ehlert, major refactoring on 21/08/2017
 */
@RestController
public class SitemapController {

    private static final Logger LOG = Logger.getLogger(SitemapController.class);

    private static final String INDEX_FILE = "europeana-sitemap-index-hashed.xml";
    private static final String ACTIVE_SITEMAP_FILE = "europeana-sitemap-active-xml-file.txt";

    @Resource(name = "api_object_storage_client")
    private ObjectStorageClient objectStorageClient;

    /**
     * Return the sitemap index file
     *
     * @param response The {@link HttpServletResponse}
     * @throws IOException For any file-related exceptions
     * @return contents of sitemap index file
     */
    @RequestMapping(value = "/europeana-sitemap-index-hashed.xml",
                    method = RequestMethod.GET, produces = MediaType.TEXT_XML_VALUE)
    public String handleSitemapIndex(HttpServletResponse response) throws IOException {
        try {
            return getFileContents(INDEX_FILE);
        } catch (SiteMapNotFoundException e) {
            LOG.error("Sitemap index file not found", e);
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return null;
        }
    }

    /**
     * Return a sitemap file. Note that the to and from are fixed values, a list of all files with to/from values
     * can be found in the sitemap index
     *
     * @param from     start index
     * @param to       end index
     * @param response The {@link HttpServletResponse}
     * @throws IOException
     * @return contents of sitemap file
     */
    @RequestMapping(value = "/europeana-sitemap-hashed.xml",
                    method = RequestMethod.GET, produces = MediaType.TEXT_XML_VALUE)
    public String handleSitemapFile(@RequestParam(value = "from", required = true) String from,
                                    @RequestParam(value = "to", required = true) String to,
                                  HttpServletResponse response) throws IOException {
        try {
            String fileName = getActiveDeployment() + "?from=" + from + "&to=" + to;
            if (LOG.isDebugEnabled()) {
                LOG.debug("Retrieving sitemap file "+ fileName);
            }
            return getFileContents(fileName);
        } catch (SiteMapNotFoundException e) {
            LOG.error("Sitemap file not found", e);
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return null;
        }
    }

    private String getFileContents(String file) throws SiteMapNotFoundException, IOException {
        if (!objectStorageClient.getWithoutBody(file).isPresent()) {
            throw new SiteMapNotFoundException("File " + file + " not found!");
        } else {
            return new String(objectStorageClient.getContent(file), "UTF-8");
        }
    }

    /**
     * The active sitemap file stores either the value 'blue' or 'green' so we know which deployment of the files we
     * need to retrieve
     * @return
     * @throws SiteMapNotFoundException
     */
    private String getActiveDeployment() throws SiteMapNotFoundException, IOException {
        return getFileContents(ACTIVE_SITEMAP_FILE);
    }
}
