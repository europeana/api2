package eu.europeana.api2.v2.utils;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.utils.DateUtils;
import org.apache.log4j.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Utility class for http caching functionality
 * @author Maike, integrated with similar class created for IIIF API by:
 * @author Patrick Ehlert (03-10-2018)
 * Created on 10-10-18.
 */
public class HttpCacheUtils {

    private static final Logger     LOG               = Logger.getLogger(HttpCacheUtils.class);
    private static final Properties properties        = new Properties();
    private static final String     LOCALBUILDVERSION = "localbuildversion";
    public  static final String     IFNONEMATCH       = "If-None-Match";
    public  static final String     IFMATCH           = "If-Match";
    private static final String     IFMODIFIEDSINCE   = "If-Modified-Since";
    private static final String     ANY               = "\"*\"";

    private static boolean useLocalBuildVersion = false;

    static{
        try {
            properties.load(HttpCacheUtils.class.getResourceAsStream("build.properties"));
        } catch (Exception e) {
            useLocalBuildVersion = true;
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
    private String getSHA256Hash(String tsUpdated){
        MessageDigest digest = null;
        String version = null;
        if (useLocalBuildVersion) {
            version = LOCALBUILDVERSION;
        } else {
            version = properties.getProperty("version");
        }
        try {
            digest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            LOG.error("Error creating SHA-265 hash", e);
        }
        byte[] encodedhash = digest.digest((version + tsUpdated).getBytes(StandardCharsets.UTF_8));
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

    // use this format: DateTimeFormatter.RFC_1123_DATE_TIME

    /**
     * Parses the given string into a ZonedDateTime object
     * @param dateString
     * @return ZonedDateTime
     */
    private ZonedDateTime stringToZonedUTC(String dateString) {
        if (StringUtils.isEmpty(dateString)) {
            return null;
        }
        // Note that Apache DateUtils can parse all 3 date format patterns allowed by RFC 2616
        Date date = DateUtils.parseDate(dateString);
        if (date == null) {
            LOG.error("Error parsing request header Date string: " + dateString);
            return null;
        }
        return dateToZonedUTC(date);
    }

    /**
     * Transforms a java.util.Date object to a ZonedDateTime object
     * @param date input Date object
     * @return ZonedDateTime representation of input date
     */
    private ZonedDateTime dateToZonedUTC(Date date){
        return date.toInstant().atOffset(ZoneOffset.UTC).toZonedDateTime().withNano(0);
    }

    /**
     * Formats the given java.util.Date according to the RFC 1123 pattern (e.g. Thu, 4 Oct 2018 10:34:20 GMT)
     * @param date Date object to be formatted
     * @return RFC 1123 patterned String representation
     */
    // Formats the given date according to the RFC 1123 pattern.
    public String dateToRFC1123String(Date date) {
        return DateUtils.formatDate(date);
    }

    /**
     * Formats the given date according to the RFC 1123 pattern (e.g. Thu, 4 Oct 2018 10:34:20 GMT)
     * @param lastModified ZonedDateTime object to be formatted
     * @return RFC 1123 patterned String representation
     */
    private static String headerDateToString(ZonedDateTime lastModified) {
        return lastModified.format(DateTimeFormatter.RFC_1123_DATE_TIME);
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
     * Supports multiple values in the "If-None-Match" header
     * @param  request      incoming HttpServletRequest
     * @param  eTag         String with the calculated eTag of the requested data
     * @return boolean true IF ( If-None-Match header is supplied AND
     *                           ( contains matching eTag OR == "*" ) )
     *         Otherwise false
     */
    public boolean doesAnyIfNoneMatch(HttpServletRequest request, String eTag){
        return ( StringUtils.isNotBlank(request.getHeader(IFNONEMATCH)) &&
                 ( doesAnyETagMatch(request.getHeader(IFNONEMATCH), eTag)));
    }

    /**
     * @param  request      incoming HttpServletRequest
     * @param  tsUpdated    Date representing the FullBean's timestamp_updated
     * @return boolean true IF If-Modified-Since header is supplied AND
     *                         is after or on the timestamp_updated
     *         Otherwise false
     */
    public boolean isNotModifiedSince(HttpServletRequest request, Date tsUpdated){
        return ( StringUtils.isNotBlank(request.getHeader(IFMODIFIEDSINCE)) &&
                 Objects.requireNonNull(stringToZonedUTC(request.getHeader(IFMODIFIEDSINCE)))
                         .compareTo(dateToZonedUTC(tsUpdated)) >= 0 );
    }

    /**
     * @param  request      incoming HttpServletRequest
     * @param  tsUpdated    Date representing the FullBean's timestamp_updated
     * @return boolean true IF If-Modified-Since header is supplied AND
     *                         is earlier the timestamp_updated
     *         Otherwise false
     *         NOTE this method was used for implementing the If-Modified-Since exception on the If-None-Match header
     *         processing specified in RFC 2616. It turns out that this RFC is obsolete and superseded by RFC 7232,
     *         which simply states to ignore the If-Modified-Since when If-None-Match is available (and can be handled)
     */
    @Deprecated
    public boolean isModifiedSince(HttpServletRequest request, Date tsUpdated){
        return ( StringUtils.isNotBlank(request.getHeader(IFMODIFIEDSINCE)) &&
                 Objects.requireNonNull(stringToZonedUTC(request.getHeader(IFMODIFIEDSINCE)))
                         .compareTo(dateToZonedUTC(tsUpdated)) < 0 );
    }

    /**
     * Supports multiple values in the "If-Match" header
     * @param request      incoming HttpServletRequest
     * @param eTag         String with the calculated eTag of the requested data
     * @return boolean true IF ("If-Match" header is supplied AND
     *                         NOT (contains matching eTag OR == "*") )
     *         otherwise false
     */
    public boolean doesPreconditionFail(HttpServletRequest request, String eTag){
        return (StringUtils.isNotBlank(request.getHeader(IFMATCH)) &&
                (!doesAnyETagMatch(request.getHeader(IFMATCH), eTag)));
    }

    private boolean doesAnyETagMatch(String eTags, String eTagToMatch){
        if (StringUtils.equals(ANY, eTags)){
            return true;
        }
        if (StringUtils.isNoneBlank(eTags, eTagToMatch)){
            for (String eTag : StringUtils.stripAll(StringUtils.split(eTags, ","))){
                if (StringUtils.equalsIgnoreCase(spicAndSpan(eTag),spicAndSpan(eTagToMatch))){
                    return true;
                }
            }
        }
        return false;
    }

    private static String spicAndSpan(String header){
        return StringUtils.remove(StringUtils.stripStart(header, "W/"), "\"");
    }

}
