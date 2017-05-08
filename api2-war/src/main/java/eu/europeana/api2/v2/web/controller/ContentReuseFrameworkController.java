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
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.annotation.Resource;
import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Retrieves image thumbnails.
 * The thumbnail API doesn't require any form of authentication, providing an API key is optional.
 */
@Controller
public class ContentReuseFrameworkController {

    private static final Logger LOG = Logger.getLogger(ContentReuseFrameworkController.class);

    @Resource
    private MediaStorageService mediaStorageService;

    @Resource
    private ControllerUtils controllerUtils;

    /**
     * Retrieves image thumbnails.
     * @param url optional, the URL of the media resource of which a thumbnail should be returned. Note that the URL should be encoded.
     *            When no url is provided a default thumbnail will be returned
     * @param size optional, the size of the thumbnail, can either be w200 (width 200) or w400 (width 400).
     * @param type optional, type of the default thumbnail (media image) in case the thumbnail does not exists or no url is provided, can be: IMAGE, SOUND, VIDEO, TEXT or 3D.
     * @param response
     * @return
     * @throws IOException
     */
    @RequestMapping(value = "/v2/thumbnail-by-url.json", method = RequestMethod.GET)
    public ResponseEntity<byte[]> thumbnailByUrl(
            @RequestParam(value = "uri", required = false) String url,
            @RequestParam(value = "size", required = false, defaultValue = "FULL_DOC") String size,
            @RequestParam(value = "type", required = false, defaultValue = "IMAGE") String type,
            HttpServletResponse response) throws IOException {
        controllerUtils.addResponseHeaders(response);
        final HttpHeaders headers = new HttpHeaders();
        final String mediaFileId = computeResourceUrl(url, size);
        final MediaFile mediaFile = mediaStorageService.retrieve(mediaFileId, true);

        byte[] mediaResponse;
        if (mediaFile != null) {
            mediaResponse = mediaFile.getContent();
            // All stored thumbnails are JPEG.
            headers.setContentType(MediaType.IMAGE_JPEG);
        } else {
            // All default not found thumbnails are PNG.
            headers.setContentType(MediaType.IMAGE_PNG);
            mediaResponse = getDefaultThumbnailForNotFoundResourceByType(type);
        }

        return new ResponseEntity<>(mediaResponse, headers, HttpStatus.OK);
    }

    /**
     * Retrieve the default thumbnail image as a byte array
     * @param path
     * @return
     */
    private byte[] getImage(String path) {
        byte[] response = null;
        BufferedImage img;
        try {
            img = ImageIO.read(getClass().getResourceAsStream(path));
            response = getByteArray(img, path.endsWith(".png") ? "png" : "gif");
        } catch (IOException e) {
            LOG.error("Error reading default thumbnail", e);
        }
        return response;
    }

    /**
     * Convert a bufferedImage to a byte array
     * @param bufferedImage
     * @param formatName
     * @return
     */
    private byte[] getByteArray(final BufferedImage bufferedImage, String formatName) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()){
            ImageIO.write(bufferedImage, formatName, baos);
            baos.flush();
            return baos.toByteArray();
        } catch (IOException e) {
            LOG.error("Error writing buffered default thumbnail", e);
        }
        return new byte[0];
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