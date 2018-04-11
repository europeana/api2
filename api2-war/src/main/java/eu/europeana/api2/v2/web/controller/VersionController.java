package eu.europeana.api2.v2.web.controller;

import eu.europeana.api2.utils.VersionUtils;
import eu.europeana.api2.v2.model.VersionInfoResult;
import eu.europeana.corelib.search.SearchService;
import org.apache.log4j.Logger;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.util.Properties;

/**
 * Controller for showing api and corelib build information (for debugging purposes only).
 * If there is a build.txt file we return the information in that, otherwise we try to extract version information
 * from (jar) manifest or filename
 * Created by Patrick Ehlert on 24-3-17.
 * @Deprecated replaced by the /info endpoint
 */
@Deprecated
@RestController
public class VersionController {

    private static final Logger LOG = Logger.getLogger(VersionController.class);

    /**
     * Handles version requests by reading information from class files and/or the api2 build.txt file that's included
     * in the .war file
     *
     * @return ModelAndView that contains api and corelib version and build information
     */
    @RequestMapping(value = {"version", "/v2/version"}, method = {RequestMethod.GET})
    public VersionInfoResult getVersion() {
        VersionInfoResult result = new VersionInfoResult();
        try {
            result.setApiBuildInfo(VersionUtils.getVersion(VersionInfoResult.class) + " " + VersionUtils.getCreationDate(VersionInfoResult.class));
            result.setCorelibBuildInfo(VersionUtils.getVersion(SearchService.class) + " " + VersionUtils.getCreationDate(SearchService.class));
        } catch (IOException | URISyntaxException e) {
            LOG.warn("Error retrieving api or corelib build information", e);
        }

        // get more detailed build information from api build.properties file (if we can)
        Properties buildProperties = new Properties();
        try (InputStream stream = this.getClass().getClassLoader().getResourceAsStream("build.properties")) {
            if (stream != null) {
                buildProperties.load(stream);
                StringBuilder s = new StringBuilder();
                for (String propName : buildProperties.stringPropertyNames()) {
                    // skip application name
                    if (!"info.app.name".equalsIgnoreCase(propName)) {
                        s.append(buildProperties.getProperty(propName));
                        s.append(" ");
                    }
                }
                result.setApiBuildInfo(s.toString());
            } else {
                LOG.warn("build.properties file not found");
            }
        } catch (IOException e) {
            LOG.error("Error reading API2 build.properties file", e);
        }
        return result;
    }

}
