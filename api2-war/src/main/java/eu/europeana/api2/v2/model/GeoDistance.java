package eu.europeana.api2.v2.model;

import eu.europeana.api2.v2.exceptions.InvalidParamValueException;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.Math.abs;

/**
 * @author lúthien (maike.dulk@europeana.eu)
 */
public class GeoDistance {
    
    
    private static final String FQ_GEOFILT_SFIELD = "{!geofilt}";
    private static final String LOCATION_SUFFIX   = "_wgs";
    private static final String LOCATION_PATTERN
                                                  = "^(currentLocation|coverageLocation|location)\\s*?,\\s*?(-?\\d+\\.?\\d*)\\s*?,\\s*?(-?\\d+\\.?\\d*)\\s*?,\\s*?(\\d+\\.?\\d*)$";
    private static final String ERROR_PATTERN     = "^(.+?)\\s*?,\\s*?(.+?)\\s*?,\\s*?(.+?)\\s*?,\\s*?(.+?)$";
    private static final String SFIELD_PATTERN    = "^currentLocation|coverageLocation|location$";
    private static final String LATLONG_PATTERN   = "^-?\\d+\\.?\\d*$";
    private static final String DISTANCE_PATTERN  = "^\\d+\\.?\\d*$";
    
    private String qfValue;
    
    private String sField;
    private Float  latitude;
    private Float  longitude;
    private Float  distance;
    
    public GeoDistance() {}
    
    public String getSField() {
        return sField;
    }
    
    public void setSField(String sField) {
        this.sField = sField + LOCATION_SUFFIX;
    }
    
    public String getFQGeo() {
        return FQ_GEOFILT_SFIELD;
    }
    
    public void setLatitude(float latitude) throws InvalidParamValueException {
        if (abs(latitude) > 90.0) {
            throw new InvalidParamValueException("The value for longitude must be between -90.0 and 90.0");
        }
        this.latitude = latitude;
    }
    
    public void setLongitude(float longitude) throws InvalidParamValueException {
        if (abs(longitude) > 90.0) {
            throw new InvalidParamValueException("The value for latitude must be between -180.0 and 180.0");
        }
        this.longitude = longitude;
    }
    
    public String getDistance() {
        return String.valueOf(distance);
    }
    
    public void setDistance(float distance) throws InvalidParamValueException {
        if (distance > 20004.0) {
            throw new InvalidParamValueException("The supplied value for distance - '"
                                                 + distance
                                                 + "' is larger than the longest possible distance between two points on Earth (20.004 km)");
        }
        this.distance = distance;
    }
    
    public String getPoint() {
        return latitude + "," + longitude;
    }
    
    public boolean isInitialised() {
        return (null != sField && null != latitude && null != longitude && null != distance);
    }
    
    /**
     * Initialises the GeoDistance object with the values contained in the qfValue parameter
     */
    public void initialise(String qfValue) throws InvalidParamValueException {
        this.qfValue = qfValue;
        
        if (StringUtils.isBlank(qfValue)) {
            throw new InvalidParamValueException("No value for qf=distance set");
        }
        Pattern      compiledLocPattern = Pattern.compile(LOCATION_PATTERN);
        Matcher      locMatcher         = compiledLocPattern.matcher(qfValue);
        List<String> geoArgumentList    = new ArrayList<>();
        
        
        if (locMatcher.find() && locMatcher.groupCount() == 4) {
            for (int i = 1; i <= locMatcher.groupCount(); i++) {
                geoArgumentList.add(locMatcher.group(i));
            }
            setSField(geoArgumentList.get(0));
            setLatitude(Float.parseFloat(geoArgumentList.get(1)));
            setLongitude(Float.parseFloat(geoArgumentList.get(2)));
            setDistance(Float.parseFloat(geoArgumentList.get(3)));
        } else {
            handleParameterErrors(qfValue);
        }
    }
    
    private void handleParameterErrors(String qfValue) throws InvalidParamValueException {
        Pattern      errorPattern      = Pattern.compile(ERROR_PATTERN);
        Matcher      errorMatcher      = errorPattern.matcher(qfValue);
        List<String> wrongArgumentList = new ArrayList<>();
        
        // four parameters have been supplied
        if (errorMatcher.find() && errorMatcher.groupCount() == 4) {
            for (int i = 1; i <= errorMatcher.groupCount(); i++) {
                wrongArgumentList.add(errorMatcher.group(i));
            }
            handleWrongArguments(wrongArgumentList);
            // something other than four parameters has been supplied
        } else {
            throw new InvalidParamValueException(
                "qf=distance requires four arguments separated by commas: [currentLocation|coverageLocation|location], latitude, longitude, distance");
        }
    }
    
    // handling the individual parameter errors
    private void handleWrongArguments(List<String> wrongArgumentList) throws InvalidParamValueException {
        Pattern sFieldPattern   = Pattern.compile(SFIELD_PATTERN);
        Matcher sFieldMatcher   = sFieldPattern.matcher(wrongArgumentList.get(0));
        Pattern latLongPattern  = Pattern.compile(LATLONG_PATTERN);
        Matcher latMatcher      = latLongPattern.matcher(wrongArgumentList.get(1));
        Matcher longMatcher     = latLongPattern.matcher(wrongArgumentList.get(2));
        Pattern distancePattern = Pattern.compile(DISTANCE_PATTERN);
        Matcher distanceMatcher = distancePattern.matcher(wrongArgumentList.get(3));
        
        if (!sFieldMatcher.find()) {
            throw new InvalidParamValueException(
                "The first argument to qf=distance must be one of [currentLocation|coverageLocation|location] (case sensitive)");
        } else if (!latMatcher.find()) {
            throw new InvalidParamValueException(
                "The second argument to qf=distance (latitude) must be a positive or negative decimal number");
        } else if (!longMatcher.find()) {
            throw new InvalidParamValueException(
                "The third argument to qf=distance (longitude) must be a positive or negative decimal number");
        } else if (!distanceMatcher.find()) {
            throw new InvalidParamValueException(
                "The last argument to qf=distance (distance) must be a positive decimal number");
        } else {
            throw new InvalidParamValueException(
                "Arguments to qf=distance should conform to ([currentLocation|coverageLocation|location], [-]0 - 90.0, [-]0 - 180.0, decimal number > 0)");
        }
        
    }
    
    
}