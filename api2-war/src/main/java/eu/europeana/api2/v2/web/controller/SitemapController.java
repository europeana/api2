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

import eu.europeana.corelib.web.swift.SwiftProvider;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.annotation.Resource;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;

/**
 * Created by luthien on 07/12/2015.
 */
@Controller
public class SitemapController {

    private final Logger log = Logger.getLogger(this.getClass());

    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Resource
    private SwiftProvider swiftProvider;

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
        if ((swiftProvider.getObjectApi().getWithoutBody(cacheFile) == null)) {
            boolean success = false;
            ServletOutputStream out = response.getOutputStream();
            log.error(String.format("Sitemap does not exist"));
        } else {
            // Read the sitemap from file
            readCachedSitemap(response.getOutputStream(), swiftProvider, cacheFile);
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
     * @param to       end index
     * @param response The {@link HttpServletResponse}
     * @throws IOException
     */
    @RequestMapping("/europeana-sitemap-hashed.xml")
    public void handleSitemap(@RequestParam(value = "from", required = true) String from, @RequestParam(value = "to", required = true) String to, HttpServletResponse response) throws IOException {
        String cacheFile = getActiveFile() + "?from=" + from + "&to=" + to;
        if (swiftProvider.getObjectApi().getWithoutBody(cacheFile) == null) {
            log.info(String.format("Error processing %s", cacheFile));
        } else {
            ServletOutputStream out = response.getOutputStream();
            readCachedSitemap(out, swiftProvider, cacheFile);
        }
    }

    /**
     * Read a cached sitemap, and copy its content to the output stream
     *
     * @param out
     * @param cacheFile
     */
    private void readCachedSitemap(ServletOutputStream out, SwiftProvider swiftProvider, String cacheFile) {
        try {
            StringWriter writer = new StringWriter();
            InputStream in = swiftProvider.getObjectApi().get(cacheFile).getPayload().openStream();
            IOUtils.copy(in, writer);
            out.println(writer.toString());
            in.close();
            out.flush();
        } catch (IOException e) {

        }
    }

    private String getActiveFile() {
        String result = "";
        String activeSiteMapFile = "europeana-sitemap-active-xml-file.txt";
        if (swiftProvider.getObjectApi().getWithoutBody(activeSiteMapFile) == null) {
            log.info(String.format("Error processing %s", activeSiteMapFile));
        } else {
            try {
                StringWriter writer = new StringWriter();
                InputStream in = swiftProvider.getObjectApi().get(activeSiteMapFile).getPayload().openStream();
                IOUtils.copy(in, writer);
                result = writer.toString();
                in.close();

            } catch (IOException e) {

            }
        }
        return result;
    }
}
