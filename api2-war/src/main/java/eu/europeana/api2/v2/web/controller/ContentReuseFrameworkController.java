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

import eu.europeana.api2.ApiLimitException;
import eu.europeana.api2.model.json.ApiError;
import eu.europeana.api2.utils.JsonUtils;
import eu.europeana.api2.v2.model.LimitResponse;
import eu.europeana.api2.v2.model.json.CrfMetadataResult;
import eu.europeana.api2.v2.utils.ControllerUtils;
import eu.europeana.corelib.db.entity.enums.RecordType;
import eu.europeana.corelib.domain.MediaFile;
import eu.europeana.corelib.web.service.ContentReuseFrameworkService;
import eu.europeana.corelib.web.service.MediaStorageService;
import eu.europeana.harvester.domain.SourceDocumentReferenceMetaInfo;
import org.apache.commons.lang.StringUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;
import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
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
    private ContentReuseFrameworkService crfService;

    @Resource
    private MediaStorageService mediaStorageService;

    @Resource
    private ControllerUtils controllerUtils;

    @RequestMapping(value = "/v2/metadata-by-url.json", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ModelAndView metadataByUrl(
            @RequestParam(value = "url", required = true) String url,
            @RequestParam(value = "wskey", required = true) String wskey,
            @RequestParam(value = "callback", required = false) String callback,
            HttpServletRequest request,
            HttpServletResponse response) {
        long t0 = System.currentTimeMillis();
        controllerUtils.addResponseHeaders(response);
        LimitResponse limitResponse;
        try {
            limitResponse = controllerUtils.checkLimit(wskey, request.getRequestURL().toString(),
                    RecordType.OBJECT, null);
        } catch (ApiLimitException e) {
            response.setStatus(e.getHttpStatus());
            return JsonUtils.toJson(new ApiError(e), callback);
        }

        CrfMetadataResult result = new CrfMetadataResult(wskey, limitResponse.getRequestNumber());
        SourceDocumentReferenceMetaInfo info = crfService.getMetadata(url);
        if (info != null) {
            result.imageMetaInfo = info.getImageMetaInfo();
        }
        result.statsDuration = (System.currentTimeMillis() - t0);
        return JsonUtils.toJson(result, callback);
    }

    @RequestMapping(value = "/v2/thumbnail-by-url.json", method = RequestMethod.GET)
    public ResponseEntity<byte[]> thumbnailByUrl(
            @RequestParam(value = "uri", required = true) String url,
            @RequestParam(value = "size", required = false, defaultValue = "FULL_DOC") String size,
            @RequestParam(value = "type", required = false, defaultValue = "IMAGE") String type,
            HttpServletResponse response) {
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
            headers.setContentType(MediaType.IMAGE_GIF);
            // All default not found thumbnails are GIF.
            mediaResponse = getDefaultThumbnailForNotFoundResourceByType(type);
        }

        return new ResponseEntity<>(mediaResponse, headers, HttpStatus.OK);
    }


    private byte[] getImage(String path) { //, String size) {
        byte[] response = null;

        BufferedImage img;
        try {
            img = ImageIO.read(getClass().getResourceAsStream(path));
            response = getByteArray(img);
        } catch (IOException e) {
            e.printStackTrace();
        }
//        int imgType = img.getType() == 0? BufferedImage.TYPE_INT_ARGB : img.getType();

//        if (size.equals("180")) {
//            try {
//                final BufferedImage newImage = resizeImage(img, imgType, 130, 180);
//                response = getByteArray(newImage);
//            } catch (Exception e) {
//                log.error(e.getMessage());
//            }
//        }

        return response;
    }

    private byte[] getByteArray(final BufferedImage bufferedImage) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try {
            ImageIO.write(bufferedImage, "gif", baos);
            baos.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return baos.toByteArray();
    }

    private BufferedImage resizeImage(BufferedImage originalImage, int type, int width, int height) {
        BufferedImage resizedImage = new BufferedImage(width, height, type);
        Graphics2D g = resizedImage.createGraphics();
        g.drawImage(originalImage, 0, 0, width, height, null);
        g.dispose();

        return resizedImage;
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
                return getImage("/images/item-image-large.gif");
            case "SOUND":
                return getImage("/images/item-sound-large.gif");
            case "VIDEO":
                return getImage("/images/item-video-large.gif");
            case "TEXT":
                return getImage("/images/item-text-large.gif");
            case "3D":
                return getImage("/images/item-3d-large.gif");
            default:
                return getImage("/images/item-image-large.gif");
        }
    }

    private String computeResourceUrl(final String resourceUrl, final String resourceSize) {
        return getMD5(resourceUrl) + "-" + (StringUtils.equalsIgnoreCase(resourceSize, "w400") ? "LARGE" : "MEDIUM");

    }
}