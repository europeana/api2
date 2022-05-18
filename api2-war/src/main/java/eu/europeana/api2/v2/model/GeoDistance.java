package eu.europeana.api2.v2.model;

import eu.europeana.api2.v2.exceptions.InvalidParamValueException;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author lúthien (maike.dulk@europeana.eu)
 */
public class GeoDistance {
    
    
    public static final  String FQ_GEOFILT_SFIELD = "{!geofilt}";
    public static final  String FL_STRING         = "proxy_dcterms_spatial coverageLocation_wgs pl_wgs84_pos_lat pl_wgs84_pos_long";
    private static final String LOCATION_PATTERN  = "^(currentLocation_wgs|coverageLocation_wgs|location_wgs)\\s*?,\\s*?(-?\\d+\\.?\\d*)\\s*?,\\s*?(-?\\d+\\.?\\d*)\\s*?,\\s*?(\\d+\\.?\\d*)$";
    
    private String qfValue;
    
    private String sField;
    private Float  latitude;
    private Float  longitude;
    private Float  distance;
    
    public GeoDistance() {}
    
    public GeoDistance(String qfValue) throws InvalidParamValueException {
        initialise(qfValue);
    }
    
    public String getQfValue() {
        return qfValue;
    }
    
    public String getSField() {
        return sField;
    }
    
    public void setSField(String sField) {
        this.sField = sField;
    }
    
    public String getFQGeo() {
        return FQ_GEOFILT_SFIELD;
    }
    
    public float getLatitude() {
        return latitude;
    }
    
    public void setLatitude(float latitude) {
        this.latitude = latitude;
    }
    
    public float getLongitude() {
        return longitude;
    }
    
    public void setLongitude(float longitude) {
        this.longitude = longitude;
    }
    
    public String getDistance() {
        return String.valueOf(distance);
    }
    
    public void setDistance(float distance) {
        this.distance = distance;
    }
    
    public String getPoint() {
        return latitude + "," + longitude;
    }
    
    public String getFlString() {
        return FL_STRING;
    }
    
    public String getParams() {
        return "&" + getDistance() + "&" + getPoint() + "&" + getFlString();
    }
    
    public boolean isInitialised(){
        return (null != sField && null != latitude && null != longitude && null != distance);
    }
    
    /**
     * returns a ZeoDistance object containing the relevant geodistance parameters
     *
     * @return validated response contained within ZeoDistance object
     */
    public void initialise(String qfValue) throws InvalidParamValueException {
        this.qfValue = qfValue;
        
        if (StringUtils.isBlank(qfValue)) {
            throw new InvalidParamValueException("No value for qf=distance set");
        }
        Pattern      compiledPattern = Pattern.compile(LOCATION_PATTERN, Pattern.CASE_INSENSITIVE);
        Matcher      matcher         = compiledPattern.matcher(qfValue);
        List<String> geoArgumentList = new ArrayList<>();
        
        
        if (matcher.find() && matcher.groupCount() == 4) {
            for (int i = 1; i <= matcher.groupCount(); i++) {
                geoArgumentList.add(matcher.group(i));
            }
            setSField(geoArgumentList.get(0));
            setLatitude(Float.parseFloat(geoArgumentList.get(1)));
            setLongitude(Float.parseFloat(geoArgumentList.get(2)));
            setDistance(Float.parseFloat(geoArgumentList.get(3)));
        } else {
            throw new InvalidParamValueException(
                "Parameters for distance should conform to qf=distance('currentLocation_wgs'|'coverageLocation_wgs'|'location_wgs', [-]0-90.0, [-]0-180.0, positive decimal)");
        }
    }
    
}