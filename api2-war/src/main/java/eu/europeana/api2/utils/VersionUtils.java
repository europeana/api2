package eu.europeana.api2.utils;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class to retrieve version information at runtime
 * Created by patrick on 8-8-17.
 */
public class VersionUtils {

    private VersionUtils() {
        // empty constructor to prevent initialization
    }

    /**
     * Retrieve version information from the manifest or alternatively from the fileName (if it's a jar)
     * Note that version information in the manifest is only available if the class/jar file was build with certain
     * Maven settings (see also http://stackoverflow.com/questions/2712970/get-maven-artifact-version-at-runtime#2713013)
     * @param clazz
     * @return
     */
    public static String getVersion(Class clazz) {
        // Try reading info from manifest first
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
    /**
     * Retrieve the creation date from the file information
     * @param clazz
     * @return
     * @throws IOException
     * @throws URISyntaxException
     */
    public static FileTime getCreationDate(Class clazz ) throws IOException, URISyntaxException {
        Path file = Paths.get(clazz.getProtectionDomain().getCodeSource().getLocation().toURI());
        BasicFileAttributes attr = Files.readAttributes(file, BasicFileAttributes.class);
        return attr.creationTime();
    }

    /**
     * Return a string denoting the provided time in the provided timezone
     * @param fileTime
     * @param timezone
     * @return
     */
    public static String convertToLocalTimeString(FileTime fileTime, ZoneId timezone) {
        if (fileTime != null) {
            LocalDateTime localTime = LocalDateTime.ofInstant(fileTime.toInstant(), timezone);
            StringBuilder timeString = new StringBuilder(localTime.toLocalDate().toString());
            timeString.append(' ').append(localTime.toLocalTime())
                    .append(' ').append(timezone.getId());
            return timeString.toString();
        }
        return null;
    }

    private static String stripVersionFileName(String fileName) {
        Pattern pattern = Pattern.compile("[\\d][.][.\\d*]*(-SNAPSHOT)?");
        Matcher matcher = pattern.matcher(fileName);
        if (matcher.find()) {
            return matcher.group();
        }
        return null;
    }
}
