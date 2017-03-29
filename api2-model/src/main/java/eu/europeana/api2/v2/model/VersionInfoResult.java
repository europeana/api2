package eu.europeana.api2.v2.model;

/**
 * Version and build information object returned by versionController
 * Created by patrick on 23-3-17.
 */
public class VersionInfoResult {

    private String apiBuildInfo;

    private String corelibBuildInfo;

    /**
     * Set api2 version and build information
     * @param apiBuildInfo
     */
    public void setApiBuildInfo(String apiBuildInfo) {
        this.apiBuildInfo = apiBuildInfo;
    }

    /**
     * @return String with api version and build information
     */
    public String getApiBuildInfo() {
        return apiBuildInfo;
    }

    /**
     * Set corelib version and build information
     * @param corelibBuildInfo
     */
    public void setCorelibBuildInfo(String corelibBuildInfo) {
        this.corelibBuildInfo = corelibBuildInfo;
    }

    /**
     * @return String with corelib version and build information
     */
    public String getCorelibBuildInfo() {
        return corelibBuildInfo;
    }
}
