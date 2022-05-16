package eu.europeana.api2.v2.utils;

import eu.europeana.api2.v2.exceptions.InvalidParamValueException;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * @author lúthien (maike.dulk@europeana.eu)
 */
public class GeoUtils{

    public static final  String FQ_GEOFILT = "{!geofilt}";
    private static final String LOCATION_PATTERN
    = "^(currentLocation_wgs|coverageLocation_wgs|location_wgs)\\s*?,\\s*?(-?\\d+\\.?\\d*)\\s*?,\\s*?(-?\\d+\\.?\\d*)\\s*?,\\s*?(\\d+\\.?\\d*)$";

    private GeoUtils() {}

    /**
     * qf=distance(fieldname,lat,lon,distance)
     * fq={!geofilt}&sfield=fieldname&pt=lat,long&d=n
     *
     * @param qfValue a List of Strings containing the hexadecimal colours to encode
     * @return Set of Integers containing the encoded colours
     */
    public static String formatDistanceRefinement(String qfValue) throws InvalidParamValueException {
        StringBuilder geoParameters   = new StringBuilder();
        Pattern       compiledPattern = Pattern.compile(LOCATION_PATTERN, Pattern.CASE_INSENSITIVE);
        Matcher      matcher = compiledPattern.matcher(qfValue);
        List<String> geoArgumentList    = new ArrayList<>();

        if (matcher.groupCount() == 4 ) {
            while (matcher.find()) {
                for (int i = 1; i <= matcher.groupCount(); i++) {
                    geoArgumentList.add(matcher.group(i));
                }
            }
            geoParameters.append("&sfield=").append(geoArgumentList.get(0));
            geoParameters.append("&pt=").append(geoArgumentList.get(1)).append(",").append(geoArgumentList.get(2));
            geoParameters.append("&d=").append(geoArgumentList.get(3));
        } else {
            throw new InvalidParamValueException("Empty language value");
        }

        return geoParameters.toString();
    }

}