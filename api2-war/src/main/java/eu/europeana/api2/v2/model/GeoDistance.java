package eu.europeana.api2.v2.model;

/**
 * Created by luthien on 16/05/2022.
 */
public class GeoDistance {
    
    
    public static final  String FQ_GEOFILT_SFIELD = "{!geofilt sfield=%s}";
    public static final  String FL_STRING = "proxy_dcterms_spatial coverageLocation_wgs pl_wgs84_pos_lat pl_wgs84_pos_long";
    
    private String sField;
    private float latitude;
    private float longitude;
    private float distance;
    
    public GeoDistance(){}
    
    public GeoDistance(String sfield, float latitude, float longitude, float distance) {
        this.sField = sfield;
        this.latitude = latitude;
        this.longitude = longitude;
        this.distance = distance;
    }
    
    public String getsField() {
        return "sfield=" + sField;
    }
    
    public String getFQGeoSField() {
        return String.format(FQ_GEOFILT_SFIELD, sField);
    }
    
    public void setSField(String sField) {
        this.sField = sField;
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
    
    public String getPoint(){
        return latitude + "," + longitude;
    }
    
    public String getFlString(){
        return FL_STRING;
    }
    
    public String getParams(){
        return "&" + getDistance() + "&" + getPoint() + "&" + getFlString();
    }
    
    
}
