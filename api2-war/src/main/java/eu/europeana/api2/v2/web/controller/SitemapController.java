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

import eu.europeana.domain.StorageObject;
import eu.europeana.features.ObjectStorageClient;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.jclouds.io.Payload;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.annotation.Resource;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.Optional;

/**
 * Created by luthien on 07/12/2015.
 */
@Controller
public class SitemapController {

    private final Logger log = Logger.getLogger(this.getClass());
    public static String ACTIVE_SITEMAP_FILE = "europeana-sitemap-active-xml-file.txt";
    ;

    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Resource(name = "api_object_storage_client")
    private ObjectStorageClient objectStorageClient;

    /**
     * Generate the sitemap index file. This file groups multiple sitemap files to adhere to the
     * 50,000 URLs / 10MB (10,485,760 bytes) sitemap limit. This index itself may not list more than
     * 50,000 Sitemaps and must be no larger than 10MB (10,485,760 bytes). The sitemaps are split by
     * id3hash (the first 3 letters of the record), and further split into files of maximum
     * MAX_URLS_PER_SITEMAP if these groups exceed 50,000 URLs.
     *
     * @param response The {@link HttpServletResponse}
     * @throws IOException For any file-related exceptions
     */
    @RequestMapping("/europeana-sitemap-index-hashed.xml")
    public void handleSitemapIndexHashed(HttpServletResponse response) throws IOException {
        String cacheFile = "europeana-sitemap-index-hashed.xml";
        // Generate the requested sitemap if it's outdated / doesn't exist (and is not currently being
        // created)
        // Read the sitemap from file
        try {
            readCachedSitemap(response.getOutputStream(), objectStorageClient, cacheFile);
        } catch (SiteMapNotFoundException e) {
            log.error(e);
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    /**
     * Generate the individual sitemaps, containing the actual record IDs. Each file needs to adhere
     * to the 50,000 URLs / 10MB (10,485,760 bytes) sitemap limit. Each sitemap is split by id3hash
     * (the first 3 letters of the record); an id3hash may be split over multiple files if there are
     * more than 50,000 records (the current implementation uses approx. 5.8 MB for 50,000 URLs, so
     * the size is not the limiting factor).
     *
     * @param from     start index
     * @param to       end indexw
     * @param response The {@link HttpServletResponse}
     * @throws IOException
     */
    @RequestMapping("/europeana-sitemap-hashed.xml")
    public void handleSitemap(@RequestParam(value = "from", required = true) String from, @RequestParam(value = "to", required = true) String to, HttpServletResponse response) throws Exception {
        ServletOutputStream out = response.getOutputStream();
        String cacheFile = null;
        try {
            cacheFile = getActiveFile() + "?from=" + from + "&to=" + to;
        } catch (SiteMapNotFoundException e) {
            log.error(ACTIVE_SITEMAP_FILE + "could not be found");
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            out.println(e.toString());
        }

        try {
            readCachedSitemap(out, objectStorageClient, cacheFile);
        } catch (IOException e) {
            log.error(e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.println(e.toString());
        }
    }

    /**
     * Read a cached sitemap, and copy its content to the output stream
     *
     * @param out
     * @param cacheFile
     */
    private void readCachedSitemap(ServletOutputStream out, ObjectStorageClient objectStorageClient, String cacheFile) throws IOException, SiteMapNotFoundException {
        InputStream in = null;
        Payload payload = null;
        try {
            StringWriter writer = new StringWriter();
            Optional<StorageObject> storageObject = objectStorageClient.get(cacheFile);
            if (!storageObject.isPresent()) {
                throw new SiteMapNotFoundException("Error while reading sitemap file " + cacheFile + " from the ObjectStorage provider");
            }
            StorageObject storageObjectValue = storageObject.get();
            payload = storageObjectValue.getPayload();
            in = payload.openStream();
            IOUtils.copy(in, writer);
            out.println(writer.toString());
            out.flush();
            in.close();
            payload.close();
        } catch (IOException e) {
            throw new IOException("Error while reading sitemap file " + cacheFile + " from the ObjectStorage provider", e);
        } finally {
            IOUtils.closeQuietly(in);
            IOUtils.closeQuietly(payload);
        }
    }

    private String getActiveFile() throws IOException, SiteMapNotFoundException {
        String result = "";
        InputStream in = null;
        Payload payload = null;
        try {
            StringWriter writer = new StringWriter();
            Optional<StorageObject> storageObject = objectStorageClient.get(ACTIVE_SITEMAP_FILE);
            if (!storageObject.isPresent()) {
                throw new SiteMapNotFoundException("Error while reading sitemap file " + ACTIVE_SITEMAP_FILE + " from the ObjectStorage provider");
            }
            StorageObject storageObjectValue = storageObject.get();
            payload = storageObjectValue.getPayload();
            in = payload.openStream();
            IOUtils.copy(in, writer);
            result = writer.toString();
            in.close();
            payload.close();
        } catch (IOException e) {
            throw new IOException("Error while reading active sitemap file " + ACTIVE_SITEMAP_FILE + " from the ObjectStorage provider", e);
        } finally {
            IOUtils.closeQuietly(in);
            IOUtils.closeQuietly(payload);
        }
        return result;
    }
}
