package eu.europeana.api2.v2.utils;

import eu.europeana.api2.v2.exceptions.InvalidParamValueException;
import eu.europeana.api2.v2.model.GeoDistance;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author lúthien (maike.dulk@europeana.eu)
 */
public class GeoUtils{

    private static final String LOCATION_PATTERN
    = "^(currentLocation_wgs|coverageLocation_wgs|location_wgs)\\s*?,\\s*?(-?\\d+\\.?\\d*)\\s*?,\\s*?(-?\\d+\\.?\\d*)\\s*?,\\s*?(\\d+\\.?\\d*)$";
    
    public String getQfValue() {
        return qfValue;
    }
    
    public void setQfValue(String qfValue) {
        this.qfValue = qfValue;
    }
    
    private String qfValue;
    
    public GeoUtils() {}
    
    public GeoUtils(String qfValue) {
        this.qfValue = qfValue;
    }
    

    /**
     * probably deprecated in favour of below method
     * returns a String containing the relevant geodistance parameters
     *
     * @return validated response contained within GeoDistance object
     */
    public String formatDistanceRefinement() throws InvalidParamValueException {
        StringBuilder geoParameters   = new StringBuilder();
        Pattern       compiledPattern = Pattern.compile(LOCATION_PATTERN, Pattern.CASE_INSENSITIVE);
        Matcher      matcher = compiledPattern.matcher(qfValue);
        List<String> geoArgumentList    = new ArrayList<>();

        if (matcher.find() && matcher.groupCount() == 4 ) {
            for (int i = 1; i <= matcher.groupCount(); i++) {
                geoArgumentList.add(matcher.group(i));
            }
            geoParameters.append("&sfield=").append(geoArgumentList.get(0));
            geoParameters.append("&pt=").append(geoArgumentList.get(1)).append(",").append(geoArgumentList.get(2));
            geoParameters.append("&d=").append(geoArgumentList.get(3));
        } else {
            throw new InvalidParamValueException("Parameters for distance should conform to qf=distance('currentLocation_wgs'|'coverageLocation_wgs'|'location_wgs', [-]0-90.0, [-]0-180.0, positive decimal)");
        }
        return geoParameters.toString();
    
    }
    
    /**
     * returns a GeoDistance object containing the relevant geodistance parameters
     *
     * @return validated response contained within GeoDistance object
     */
    public GeoDistance getGeoDistance() throws InvalidParamValueException {
        if (StringUtils.isBlank(qfValue)){
            throw new InvalidParamValueException("No value for qf=distance set");
        }
        GeoDistance geoDistance = new GeoDistance();
        Pattern       compiledPattern = Pattern.compile(LOCATION_PATTERN, Pattern.CASE_INSENSITIVE);
        Matcher      matcher = compiledPattern.matcher(qfValue);
        List<String> geoArgumentList    = new ArrayList<>();
    

        if (matcher.find() && matcher.groupCount() == 4 ) {
            for (int i = 1; i <= matcher.groupCount(); i++) {
                geoArgumentList.add(matcher.group(i));
            }
            geoDistance.setSField(geoArgumentList.get(0));
            geoDistance.setLatitude(Float.parseFloat(geoArgumentList.get(1)));
            geoDistance.setLongitude(Float.parseFloat(geoArgumentList.get(2)));
            geoDistance.setDistance(Float.parseFloat(geoArgumentList.get(3)));
        } else {
            throw new InvalidParamValueException("Parameters for distance should conform to qf=distance('currentLocation_wgs'|'coverageLocation_wgs'|'location_wgs', [-]0-90.0, [-]0-180.0, positive decimal)");
        }
        
        return geoDistance;
    }

}