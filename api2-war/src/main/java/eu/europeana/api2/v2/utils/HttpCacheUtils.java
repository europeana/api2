package eu.europeana.api2.v2.utils;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.utils.DateUtils;
import org.apache.log4j.Logger;
import org.springframework.http.HttpHeaders;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.HEAD;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Locale;
import java.util.Properties;
import java.util.TimeZone;

/**
 * Utility class for http caching functionality
 * @author Maike, integrated with similar class created for IIIF API by:
 * @author Patrick Ehlert (03-10-2018)
 * Created on 10-10-18.
 */
public class HttpCacheUtils {

    private static final Logger           LOG               = Logger.getLogger(HttpCacheUtils.class);
    private static final Properties       properties        = new Properties();
    private static final String           DATEUPDATEDFORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
    private static final SimpleDateFormat UPDATEFORMAT      = new SimpleDateFormat(DATEUPDATEDFORMAT, Locale.GERMAN);


    static{
        UPDATEFORMAT.setTimeZone(TimeZone.getTimeZone("GTM"));
        try {
            properties.load(HttpCacheUtils.class.getResourceAsStream("build.properties"));
        } catch (IOException e) {
            LOG.error("IOException trying to read build.properties");
        }
    }

    /**
     * Generates an eTag surrounded with double quotes
     * @param data
     * @param weakETag if true then the eTag will start with W/
     * @return
     */
    public String generateETag(String data, boolean weakETag) {
        String eTag = "\"" + getSHA256Hash(data) + "\"";
        if (weakETag) {
            return "W/"+eTag;
        }
        return eTag;
    }

    /**
     * Calculates SHA256 hash based on:
     * (1) the API version as read from project.version in build.properties, and
     * (2) the record's timestamp_updated represented as String
     * @param  tsUpdated  String
     * @return SHA256Hash String
     */
    public String getSHA256Hash(String tsUpdated){
        MessageDigest digest = null;
        try {
            digest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            LOG.error("Error creating SHA-265 hash", e);
        }
        byte[] encodedhash = digest.digest(
                (properties.getProperty("version") + tsUpdated)
                        .getBytes(StandardCharsets.UTF_8));
        return bytesToHex(encodedhash);
    }

    private static String bytesToHex(byte[] hash) {
        StringBuffer hexString = new StringBuffer();
        for (int i = 0; i < hash.length; i++) {
            String hex = Integer.toHexString(0xff & hash[i]);
            if(hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    }

    /**
     * Parses the date string received in a request header
     * @param dateString
     * @return Date
     */
    private ZonedDateTime stringToZonedUTC(String dateString) {
        if (StringUtils.isEmpty(dateString)) {
            return null;
        }
        // Note that Apache DateUtils can parse all 3 date format patterns allowed by RFC 2616
        Date headerDate = DateUtils.parseDate(dateString);
        if (headerDate == null) {
            LOG.error("Error parsing request header Date string: " + dateString);
            return null;
        }
        return headerDate.toInstant().atOffset(ZoneOffset.UTC).toZonedDateTime();
    }

    public String dateToZonedToISOString(Date date){
        return zonedToISOString(dateToZonedUTC(date));
    }

    private ZonedDateTime dateToZonedUTC(Date date){
        return date.toInstant().atOffset(ZoneOffset.UTC).toZonedDateTime();
    }

    // TODO review if necessary
    public static String dateToString(Date dateUpdate) {
        return UPDATEFORMAT.format(dateUpdate);
    }

    // TODO review if necessary Formats the given date according to the RFC 1123 pattern.
    public String dateToRFC1123String(Date dateHeader) {
        return DateUtils.formatDate(dateHeader);
    }

    /**
     * TODO review if necessary
     * Formats the given date according to the RFC 1123 pattern (e.g. Thu, 4 Oct 2018 10:34:20 GMT)
     * @param lastModified
     * @return
     */
    private String zonedToRFC1123String(ZonedDateTime lastModified) {
        return lastModified.format(DateTimeFormatter.RFC_1123_DATE_TIME);
    }

    /**
     * Formats the given date according to the RFC 1123 pattern (e.g. Thu, 4 Oct 2018 10:34:20 GMT)
     * @param lastModified
     * @return
     */
    private String zonedToISOString(ZonedDateTime lastModified) {
        return lastModified.format(DateTimeFormatter.ISO_DATE_TIME);
    }

    /**
     * Generate the default headers for sending a response with caching
     * @param response      required, HttpServletResponse to add the headers to
     * @param eTag          optional, if not null then an ETag header is added
     * @param tsUpdated     optional, if not null then a Last-Modified header is added
     * @param allow         optional, if not null then an Allow header is added
     * @param cacheControl  optional, if not null then a Cache-Control header is added
     * @return HttpServletResponse
     */
    public HttpServletResponse addDefaultHeaders(HttpServletResponse response, String eTag,
                                                 String tsUpdated, String allow, String cacheControl){
        if (StringUtils.isNotBlank(eTag)) {
            response.addHeader("ETag", eTag);
        }
        if (StringUtils.isNotBlank(tsUpdated)) {
            response.addHeader("Last-Modified", tsUpdated);
        }
        if (StringUtils.isNotBlank(allow)) {
            response.addHeader("Allow", allow);
        }
        if (StringUtils.isNotBlank(cacheControl)) {
            response.addHeader("Cache-Control", cacheControl);
        }
        return response;
    }

    /**
     * Generate the default headers for sending a response with caching
     * @param response      required, HttpServletResponse to add the headers to
     * @param allowMethods  optional, if not null then an Access-Control-Allow-Methods header is added
     * @param allowHeaders  optional, if not null then an Access-Control-Allow-Headers header is added
     * @param exposeHeaders optional, if not null then an Access-Control-Expose-Headers header is added
     * @param maxAge        optional, if not null then an Access-Control-Max-Age header is added
     * @return HttpServletResponse
     */
    public HttpServletResponse addCorsHeaders(HttpServletResponse response, String allowMethods,
                                              String allowHeaders, String exposeHeaders, String maxAge){
        if (StringUtils.isNotBlank(allowMethods)) {
            response.addHeader("Access-Control-Allow-Methods", allowMethods);
        }
        if (StringUtils.isNotBlank(allowHeaders)) {
            response.addHeader("Access-Control-Allow-Headers", allowHeaders);
        }
        if (StringUtils.isNotBlank(exposeHeaders)) {
            response.addHeader("Access-Control-Expose-Headers", exposeHeaders);
        }
        if (StringUtils.isNotBlank(maxAge)) {
            response.addHeader("Access-Control-Max-Age", maxAge);
        }
        return response;
    }

    /**
     * Please note that this method does not yet support multiple values supplied in the "If-None-Match" header
     * @param request      incoming HttpServletRequest
     * @param tsUpdated    Date representing the FullBean's timestamp_updated
     * @param eTag         String with the calculated eTag of the requested data
     * @return boolean true if ("If-Modified-Since" header is supplied AND object is modified after that date)
     * OR ("If-None-Match" header is supplied AND matches the object's eTag value) - otherwise false
     */
    public boolean checkNotModified(HttpServletRequest request, Date tsUpdated, String eTag){
        ZonedDateTime zonedTsUpdated      = dateToZonedUTC(tsUpdated);
        ZonedDateTime requestLastModified = stringToZonedUTC(request.getHeader("If-Modified-Since"));
        return (( requestLastModified != null && requestLastModified.compareTo(zonedTsUpdated) > 0 ) ||
                ( StringUtils.isNotEmpty(request.getHeader("If-None-Match")) &&
                  StringUtils.equalsIgnoreCase(request.getHeader("If-None-Match"), eTag)));
    }

    /**
     * @param request      incoming HttpServletRequest
     * @param eTag         String with the calculated eTag of the requested data
     * @return boolean true if ("If-Match" header is supplied AND (does NOT match the object's eTag value OR is "*") -
     * otherwise false
     */
    public boolean checkPreconditionFailed(HttpServletRequest request, String eTag){
        return (StringUtils.isNotEmpty(request.getHeader("If-Match")) &&
                 (!StringUtils.equalsIgnoreCase(request.getHeader("If-Match"), eTag) &&
                  !StringUtils.equalsIgnoreCase(request.getHeader("If-Match"), "*")));
    }


}
