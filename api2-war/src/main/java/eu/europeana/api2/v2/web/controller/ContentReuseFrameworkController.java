package eu.europeana.api2.v2.web.controller;

import javax.annotation.Resource;
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

import java.awt.image.BufferedImage;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

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
            @RequestParam(value = "url", required = true) String url,
            @RequestParam(value = "size", required = true) String size,
            @RequestParam(value = "type", required = true) String type,
            @RequestParam(value = "callback", required = false) String callback,
            HttpServletRequest request,
            HttpServletResponse response) {
        controllerUtils.addResponseHeaders(response);

        String sufix = "";
        if(size.equalsIgnoreCase("BRIEF_DOC") || size.equalsIgnoreCase("h180")) {
            sufix = "180";
        }
        if(size.equalsIgnoreCase("FULL-DOC") || size.equalsIgnoreCase("w200")) {
            sufix = "200";
        }

        final HttpHeaders headers = new HttpHeaders();
        byte[] imageResponse = DefaultImage.getImage(ThumbSize.LARGE, DocType.IMAGE);
        if(type.equalsIgnoreCase("IMAGE")) {
            final String ID = getMD5(url + "" + sufix);
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
                imageResponse = DefaultImage.getImage(ThumbSize.LARGE, DocType.IMAGE);

                if (sufix.equals("180")) {
                    try {
                        final BufferedImage oldImage = ImageUtils.toBufferedImage(imageResponse);
                        final BufferedImage newImage = ImageUtils.scale(oldImage, 130, 180);
                        imageResponse = ImageUtils.toByteArray(newImage);
                    } catch (Exception e) {
                        log.error(e.getMessage());
                    }
                }
            }
        }
        if(type.equalsIgnoreCase("SOUND")) {
            imageResponse = DefaultImage.getImage(ThumbSize.LARGE, DocType.SOUND);

            if (sufix.equals("180")) {
                try {
                    final BufferedImage oldImage = ImageUtils.toBufferedImage(imageResponse);
                    final BufferedImage newImage = ImageUtils.scale(oldImage, 130, 180);
                    imageResponse = ImageUtils.toByteArray(newImage);
                } catch (Exception e) {
                    log.error(e.getMessage());
                }
            }
        }
        if(type.equalsIgnoreCase("VIDEO")) {
            imageResponse = DefaultImage.getImage(ThumbSize.LARGE, DocType.VIDEO);

            if (sufix.equals("180")) {
                try {
                    final BufferedImage oldImage = ImageUtils.toBufferedImage(imageResponse);
                    final BufferedImage newImage = ImageUtils.scale(oldImage, 130, 180);
                    imageResponse = ImageUtils.toByteArray(newImage);
                } catch (Exception e) {
                    log.error(e.getMessage());
                }
            }
        }
        if(type.equalsIgnoreCase("TEXT")) {
            imageResponse = DefaultImage.getImage(ThumbSize.LARGE, DocType.TEXT);

            if (sufix.equals("180")) {
                try {
                    final BufferedImage oldImage = ImageUtils.toBufferedImage(imageResponse);
                    final BufferedImage newImage = ImageUtils.scale(oldImage, 130, 180);
                    imageResponse = ImageUtils.toByteArray(newImage);
                } catch (Exception e) {
                    log.error(e.getMessage());
                }
            }
        }
        if(type.equalsIgnoreCase("3D")) {
            imageResponse = DefaultImage.getImage(ThumbSize.LARGE, DocType._3D);

            if (sufix.equals("180")) {
                try {
                    final BufferedImage oldImage = ImageUtils.toBufferedImage(imageResponse);
                    final BufferedImage newImage = ImageUtils.scale(oldImage, 130, 180);
                    imageResponse = ImageUtils.toByteArray(newImage);
                } catch (Exception e) {
                    log.error(e.getMessage());
                }
            }
        }

        return new ResponseEntity<>(imageResponse, headers, HttpStatus.CREATED);
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
