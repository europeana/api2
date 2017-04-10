package eu.europeana.api2.v2.web.controller;

import eu.europeana.api2.v2.model.VersionInfoResult;
import eu.europeana.corelib.search.SearchService;
import org.apache.log4j.Logger;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import java.io.*;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Controller for showing api and corelib build information (for debugging purposes only).
 * If there is a build.txt file we return the information in that, otherwise we try to extract version information
 * from (jar) manifest or filename
 * Created by Patrick Ehlert on 24-3-17.
 */
@Controller
public class VersionController {

    private static final Logger LOG = Logger.getLogger(VersionController.class);

    /**
     * Handles version requests by reading information from class files and/or the api2 build.txt file that's included
     * in the .war file
     *
     * @return ModelAndView that contains api and corelib version and build information
     */
    @RequestMapping(value = {"version", "/v2/version"}, method = {RequestMethod.GET}, produces = MediaType.TEXT_HTML_VALUE)
    public ModelAndView getVersion() {
        VersionInfoResult result = new VersionInfoResult();
        try {
            result.setApiBuildInfo(getVersion(VersionInfoResult.class) + " " + getCreationDate(VersionInfoResult.class));
            result.setCorelibBuildInfo(getVersion(SearchService.class) + " " + getCreationDate(SearchService.class));
        } catch (Exception e) {
            LOG.warn("Unable to retrieve api or corelib build information", e);
        }

        // get more accurate build information from api build.txt file (if we can)
        InputStream is = this.getClass().getResourceAsStream("/../../build.txt");
        if (is == null) {
            LOG.warn("No api2 build.txt file found!");
        } else {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                    sb.append("\n");
                }
                result.setApiBuildInfo(sb.toString());
            } catch (IOException e) {
                LOG.error("Error reading API2 build.txt file", e);
            }
        }
        return new ModelAndView("version", "versionInfo", result);
    }

    private String getVersion(Class clazz) {
        // Try reading info from manifes first, this only works with certain maven settings
        // (see also http://stackoverflow.com/questions/2712970/get-maven-artifact-version-at-runtime#2713013)
        String result = clazz.getPackage().getImplementationVersion();
        if (result == null) {
            result = clazz.getPackage().getSpecificationVersion();
        }
        // fallback: check if there is a version in the filename (which is usually the case if it's packaged in a jar)
        if (result == null) {
            result = stripVersionFileName(clazz.getProtectionDomain().getCodeSource().getLocation().getPath());
        }
        return result;
    }

    private String getCreationDate(Class clazz) throws IOException, URISyntaxException {
        Path file = Paths.get(clazz.getProtectionDomain().getCodeSource().getLocation().toURI());
        BasicFileAttributes attr = Files.readAttributes(file, BasicFileAttributes.class);
        ZoneId timezone = ZoneId.of("CET");
        LocalDateTime fileTime = LocalDateTime.ofInstant(attr.creationTime().toInstant(), timezone);
        StringBuilder timeString = new StringBuilder(fileTime.toLocalDate().toString());
        timeString.append(" ");
        timeString.append(fileTime.toLocalTime().toString());
        timeString.append(" ");
        timeString.append(timezone.getId());
        return timeString.toString();
    }

    private String stripVersionFileName(String fileName) {
        Pattern pattern = Pattern.compile("[\\d][.][.\\d*]*(-SNAPSHOT)?");
        Matcher matcher = pattern.matcher(fileName);
        if (matcher.find()) {
            return matcher.group();
        }
        return null;

    }


}
