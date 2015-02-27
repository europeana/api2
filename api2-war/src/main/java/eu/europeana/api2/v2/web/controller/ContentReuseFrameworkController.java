package eu.europeana.api2.v2.web.controller;

import javax.annotation.Resource;
import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import eu.europeana.api2.v2.model.enums.DefaultImage;
import eu.europeana.corelib.definitions.model.ThumbSize;
import eu.europeana.corelib.definitions.solr.DocType;
import eu.europeana.corelib.domain.MediaFile;
import eu.europeana.corelib.service.impl.MediaStorageClientImpl;
import eu.europeana.corelib.utils.ImageUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import eu.europeana.api2.model.enums.ApiLimitException;
import eu.europeana.api2.model.json.ApiError;
import eu.europeana.api2.utils.JsonUtils;
import eu.europeana.api2.v2.model.LimitResponse;
import eu.europeana.api2.v2.model.json.CrfMetadataResult;
import eu.europeana.api2.v2.utils.ControllerUtils;
import eu.europeana.corelib.db.entity.enums.RecordType;
import eu.europeana.corelib.db.service.ApiKeyService;
import eu.europeana.corelib.db.service.ApiLogService;
import eu.europeana.corelib.logging.Log;
import eu.europeana.corelib.logging.Logger;
import eu.europeana.corelib.solr.service.SearchService;
import eu.europeana.corelib.utils.service.OptOutService;
import eu.europeana.corelib.web.service.ContentReuseFrameworkService;
import eu.europeana.corelib.web.service.EuropeanaUrlService;
import eu.europeana.harvester.domain.SourceDocumentReferenceMetaInfo;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

@Controller
public class ContentReuseFrameworkController {

    @Log
    private Logger log;

    @Resource
    private SearchService searchService;

    @Resource(name = "corelib_web_contentReuseFrameworkService")
    private ContentReuseFrameworkService crfService;

    @Resource(name = "corelib_mediaStorageClient")
    private MediaStorageClientImpl mediaStorageClient;

    @Resource
    private ApiLogService apiLogService;

    @Resource
    private ApiKeyService apiService;

    @Resource
    private OptOutService optOutService;

    @Resource
    private EuropeanaUrlService urlService;

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
        LimitResponse limitResponse = null;
        try {
            limitResponse = controllerUtils.checkLimit(wskey, request.getRequestURL().toString(),
                    "record.json", RecordType.OBJECT, null);
        } catch (ApiLimitException e) {
            response.setStatus(e.getHttpStatus());
            return JsonUtils.toJson(new ApiError(e), callback);
        }

        CrfMetadataResult result = new CrfMetadataResult(wskey, "metadata-by-url.json", limitResponse.getRequestNumber());
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
            @RequestParam(value = "size", required = true) String size,
            @RequestParam(value = "type", required = true) String type,
            @RequestParam(value = "callback", required = false) String callback,
            HttpServletRequest request,
            HttpServletResponse response) {
        controllerUtils.addResponseHeaders(response);

        log.info("Thumbnail");

        String sufix = "";
        log.info("Size: " + size);
        log.info("Type: " + type);

        if(size.equalsIgnoreCase("BRIEF-DOC") || size.equalsIgnoreCase("h180")) {
            sufix = "180";
        }
        if(size.equalsIgnoreCase("FULL-DOC") || size.equalsIgnoreCase("w200")) {
            sufix = "200";
        }

        final HttpHeaders headers = new HttpHeaders();

        byte[] imageResponse = DefaultImage.getImage(ThumbSize.LARGE, DocType.IMAGE);
        if(type.equalsIgnoreCase("IMAGE")) {
            final String ID = getMD5(url + sufix);
            log.info("Image: " + ID + " sufix: " + sufix);
            final MediaFile mediaFile = mediaStorageClient.retrieve(ID, true);

            if (mediaFile != null) {
                if (mediaFile.getContentType().equals("image/jpeg")) {
                    headers.setContentType(MediaType.IMAGE_JPEG);
                }
                if (mediaFile.getContentType().equals("image/png")) {
                    headers.setContentType(MediaType.IMAGE_PNG);
                }

                imageResponse = mediaFile.getContent();
            } else {
                imageResponse = getImage("/images/item-image-large.gif", sufix);
                headers.setContentType(MediaType.IMAGE_GIF);
            }
        }
        if(type.equalsIgnoreCase("SOUND")) {
            imageResponse = getImage("/images/item-sound-large.gif", sufix);
            headers.setContentType(MediaType.IMAGE_GIF);
        }
        if(type.equalsIgnoreCase("VIDEO")) {
            imageResponse = getImage("/images/item-video-large.gif", sufix);
            headers.setContentType(MediaType.IMAGE_GIF);
        }
        if(type.equalsIgnoreCase("TEXT")) {
            imageResponse = getImage("/images/item-text-large.gif", sufix);
            headers.setContentType(MediaType.IMAGE_GIF);
        }
        if(type.equalsIgnoreCase("3D")) {
            imageResponse = getImage("/images/item-3d-large.gif", sufix);
            headers.setContentType(MediaType.IMAGE_GIF);
        }

        return new ResponseEntity<>(imageResponse, headers, HttpStatus.CREATED);
    }

    private byte[] getImage(String path, String size) {
        byte[] response = null;

        BufferedImage img = null;
        try {
            img = ImageIO.read(getClass().getResourceAsStream(path));
            response = getByteArray(img);
        } catch (IOException e) {
            e.printStackTrace();
        }
        int imgType = img.getType() == 0? BufferedImage.TYPE_INT_ARGB : img.getType();

        if (size.equals("180")) {
            try {
                final BufferedImage newImage = resizeImage(img, imgType, 130, 180);
                response = getByteArray(newImage);
            } catch (Exception e) {
                log.error(e.getMessage());
            }
        }

        return response;
    }

    private byte[] getByteArray(final BufferedImage bufferedImage) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try {
            ImageIO.write(bufferedImage, "gif", baos );
            baos.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return baos.toByteArray();
    }

    private BufferedImage resizeImage(BufferedImage originalImage, int type, int width, int height){
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
}
