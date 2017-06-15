/*
 * Copyright 2007-2015 The Europeana Foundation
 *
 * Licenced under the EUPL, Version 1.1 (the "Licence") and subsequent versions as approved
 * by the European Commission;
 * You may not use this work except in compliance with the Licence.
 *
 * You may obtain a copy of the Licence at:
 * http://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software distributed under
 * the Licence is distributed on an "AS IS" basis, without warranties or conditions of
 * any kind, either express or implied.
 * See the Licence for the specific language governing permissions and limitations under
 * the Licence.
 */

package eu.europeana.api2.v2.web.controller;

import eu.europeana.api2.v2.utils.ControllerUtils;
import eu.europeana.corelib.domain.MediaFile;
import eu.europeana.corelib.web.service.MediaStorageService;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.WebRequest;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Retrieves image thumbnails.
 * The thumbnail API doesn't require any form of authentication, providing an API key is optional.
 */
@RestController
public class ThumbnailController {

    private static final Logger LOG = Logger.getLogger(ThumbnailController.class);

    private MediaStorageService mediaStorageService;

    @Autowired
    private ThumbnailController(MediaStorageService mediaStorageService) {
        this.mediaStorageService = mediaStorageService;
    }

    /**
     * Retrieves image thumbnails.
     * @param url optional, the URL of the media resource of which a thumbnail should be returned. Note that the URL should be encoded.
     *            When no url is provided a default thumbnail will be returned
     * @param size optional, the size of the thumbnail, can either be w200 (width 200) or w400 (width 400).
     * @param type optional, type of the default thumbnail (media image) in case the thumbnail does not exists or no url is provided,
     *             can be: IMAGE, SOUND, VIDEO, TEXT or 3D.
     * @param webRequest
     * @param response
     * @return
     * @throws IOException
     */
    @RequestMapping(value = "/v2/thumbnail-by-url.json", method = RequestMethod.GET)
    public ResponseEntity<byte[]> thumbnailByUrl(
            @RequestParam(value = "uri", required = false) String url,
            @RequestParam(value = "size", required = false, defaultValue = "FULL_DOC") String size,
            @RequestParam(value = "type", required = false, defaultValue = "IMAGE") String type,
            WebRequest webRequest, HttpServletResponse response) throws IOException {

        // 2017-05-12 Timing debug statements added as part of ticket #613.
        // Can be removed when it's confirmed that timing is improved
        long startTime = 0;
        if (LOG.isDebugEnabled()) { startTime = System.nanoTime(); }

        ControllerUtils.addResponseHeaders(response);
        final HttpHeaders headers = new HttpHeaders();
        final String mediaFileId = computeResourceUrl(url, size);

        ResponseEntity result = null;

        // 2017-06-13 as part of ticket 638 we retrieve the entire mediafile and put the eTag and lastModified in our response
        // However we need to see if this will really have a positive effect on the load (see also ticket #659)
        
        MediaFile mediaFile = mediaStorageService.retrieve(mediaFileId, true);
        byte[] mediaContent;
        if (mediaFile == null) {
            headers.setContentType(MediaType.IMAGE_PNG);
            mediaContent = getDefaultThumbnailForNotFoundResourceByType(type);
            result = new ResponseEntity<>(mediaContent, headers, HttpStatus.OK);
        } else {
            // this check automatically sets an ETag and last-Modified in our response header and returns a 304
            // (but only when clients include the If_Modified_Since header in their request)
            if (webRequest.checkNotModified(mediaFile.getContentMd5(), mediaFile.getCreatedAt().getMillis())) {
                // no need to do anything, just return result = null
            } else {
                // All stored thumbnails are JPEG.
                headers.setContentType(MediaType.IMAGE_JPEG);
                mediaContent = mediaFile.getContent();
                result = new ResponseEntity<>(mediaContent, headers, HttpStatus.OK);
            }
        }

//        byte[] mediaContent = mediaStorageService.retrieveContent(mediaFileId);
//        if (mediaContent == null || mediaContent.length == 0) {
//            // All default not found thumbnails are PNG.
//            headers.setContentType(MediaType.IMAGE_PNG);
//            mediaContent = getDefaultThumbnailForNotFoundResourceByType(type);
//        } else {
//            // All stored thumbnails are JPEG.
//            headers.setContentType(MediaType.IMAGE_JPEG);
//        }

        if (LOG.isDebugEnabled()) {
            Long duration = (System.nanoTime() - startTime) / 1000;
            if (MediaType.IMAGE_PNG.equals(headers.getContentType())) {
                LOG.debug("Total thumbnail request time (missing media): " +duration);
            } else {
                if (result == null) {
                    LOG.debug("Total thumbnail request time (from s3 + return 304): " +duration);
                } else {
                    LOG.debug("Total thumbnail request time (from s3 + return 200): " + duration);
                }
            }
        }
        return result;
    }

    /**
     * Retrieve the default thumbnail image as a byte array
     * @param path
     * @return
     */
    private byte[] getImage(String path) {
        byte[] result = null;
        try (InputStream in = this.getClass().getResourceAsStream(path)){
            result = IOUtils.toByteArray(in);
        } catch (IOException e) {
            LOG.error("Error reading default thumbnail file", e);
        }
        return result;
    }


    private String getMD5(String input) {
        final MessageDigest messageDigest;
        String temp;
        try {
            messageDigest = MessageDigest.getInstance("MD5");
            messageDigest.reset();
            messageDigest.update(input.getBytes());
            final byte[] resultByte = messageDigest.digest();
            StringBuilder sb = new StringBuilder();
            for (byte aResultByte : resultByte) {
                sb.append(Integer.toString((aResultByte & 0xff) + 0x100, 16).substring(1));
            }
            temp = sb.toString();
        } catch (NoSuchAlgorithmException e) {
            LOG.error("Cannot find MD5 algorithm", e);
            temp = input;
        }

        return temp;
    }

    //@Cacheable
    private byte[] getDefaultThumbnailForNotFoundResourceByType(final String type) {
        switch (StringUtils.upperCase(type)) {
            case "IMAGE":
                return getImage("/images/EU_thumbnails_image.png");
            case "SOUND":
                return getImage("/images/EU_thumbnails_sound.png");
            case "VIDEO":
                return getImage("/images/EU_thumbnails_video.png");
            case "TEXT":
                return getImage("/images/EU_thumbnails_text.png");
            case "3D":
                return getImage("/images/EU_thumbnails_3d.png");
            default:
                return getImage("/images/EU_thumbnails_image.png");
        }

    }

    /**
     * Convert the provided url and size into a string representing the id of the media file.
     * @param resourceUrl
     * @param resourceSize
     * @return
     */
    private String computeResourceUrl(final String resourceUrl, final String resourceSize) {
        String urlText = (resourceUrl == null ? "" : resourceUrl);
        return getMD5(urlText) + "-" + (StringUtils.equalsIgnoreCase(resourceSize, "w200") ? "MEDIUM" : "LARGE");
    }
}
