package eu.europeana.api2.v2.utils;

import org.apache.commons.lang3.StringUtils;;

import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * @author lúthien (maike.dulk@europeana.eu)
 */
public class GeoUtils {
    
    public static final String FQ_GEOFILT = "fq={!geofilt}";
    //private static final Pattern LOCATION_PATTERN = Pattern.compile("^(currentLocation_wgs|coverageLocation_wgs|location_wgs)\s*?,\s*?(\-?\d+\.?\d*)\s*?,\s*?(\-?\d+\.?\d*)\s*?,\s*?(\d+\.?\d*)$");
    private static final String LOCATION_PATTERN
//        = "^(currentLocation_wgs|coverageLocation_wgs|location_wgs)\\s*?,\\s*?(\\d+\\.?\\d*)\\s*?,\\s*?(\\d+\\.?\\d*)\\s*?,\\s*?(\\d+\\.?\\d*)$";
        = "^(currentlocation_wgs|coveragelocation_wgs|location_wgs)\\s*?,\\s*?([\\d.-]+)\\s*?,\\s*?([\\d.-]+)\\s*?,\\s*?([\\d.]+)$";
    
    private GeoUtils() {}
    
    /**
     * qf=distance(fieldname,lat,lon,distance)
     * fq={!geofilt}&sfield=fieldname&pt=lat,long&d=n
     *
     * @param qfValue a List of Strings containing the hexadecimal colours to encode
     * @return Set of Integers containing the encoded colours
     */
    public static String formatDistanceRefinement(String qfValue) {
        StringBuilder geoParameters = new StringBuilder();
        
        Pattern        compiledPattern   = Pattern.compile(LOCATION_PATTERN);
        Stream<String> textSplitAsStream = compiledPattern.splitAsStream(qfValue);
        String[]       arguments         = textSplitAsStream.toArray(String[]::new);
        
        if (arguments.length == 4 && StringUtils.isNoneBlank(arguments) ){
            geoParameters.append("&sfield=").append(arguments[0]);
            geoParameters.append("&pt=").append(arguments[1]).append(",").append(arguments[2]);
            geoParameters.append("&d=").append(arguments[3]);
        } else {
            System.out.println("throw exception");
        }
        
        return geoParameters.toString();
    }
    
}