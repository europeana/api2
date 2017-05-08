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
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@Controller
public class ContentReuseFrameworkController {

    @Resource
    private MediaStorageService mediaStorageService;

    @Resource
    private ControllerUtils controllerUtils;

    @RequestMapping(value = "/v2/thumbnail-by-url.json", method = RequestMethod.GET)
    public ResponseEntity<byte[]> thumbnailByUrl(
            @RequestParam(value = "uri", required = false) String url,
            @RequestParam(value = "size", required = false, defaultValue = "FULL_DOC") String size,
            @RequestParam(value = "type", required = false, defaultValue = "IMAGE") String type,
            HttpServletResponse response) throws IOException {
        controllerUtils.addResponseHeaders(response);
        url = (url == null ? "": url);
        final HttpHeaders headers = new HttpHeaders();
        final String mediaFileId = computeResourceUrl(url, size);
        final MediaFile mediaFile = mediaStorageService.retrieve(mediaFileId, true);

        byte[] mediaResponse = null;
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


    private byte[] getImage(String path) {
        byte[] response = null;

        BufferedImage img;
        try {
            img = ImageIO.read(getClass().getResourceAsStream(path));
            response = getByteArray(img, path.endsWith(".png") ? "png" : "gif");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return response;
    }

    private byte[] getByteArray(final BufferedImage bufferedImage, String formatName) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try {
            ImageIO.write(bufferedImage, formatName, baos);
            baos.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return baos.toByteArray();
    }

    private String getMD5(String input) {
        final MessageDigest messageDigest;
        String temp;
        try {
            messageDigest = MessageDigest.getInstance("MD5");
            messageDigest.reset();
            messageDigest.update(input.getBytes());
            final byte[] resultByte = messageDigest.digest();
            StringBuffer sb = new StringBuffer();
            for (byte aResultByte : resultByte) {
                sb.append(Integer.toString((aResultByte & 0xff) + 0x100, 16).substring(1));
            }
            temp = sb.toString();
        } catch (NoSuchAlgorithmException e) {
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

    private String computeResourceUrl(final String resourceUrl, final String resourceSize) {
        return getMD5(resourceUrl) + "-" + (StringUtils.equalsIgnoreCase(resourceSize, "w400") ? "LARGE" : "MEDIUM");

    }
}