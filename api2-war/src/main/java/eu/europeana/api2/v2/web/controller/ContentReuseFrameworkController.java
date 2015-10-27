package eu.europeana.api2.v2.web.controller;

import eu.europeana.api2.ApiLimitException;
import eu.europeana.api2.model.json.ApiError;
import eu.europeana.api2.utils.JsonUtils;
import eu.europeana.api2.v2.model.LimitResponse;
import eu.europeana.api2.v2.model.enums.DefaultImage;
import eu.europeana.api2.v2.model.json.CrfMetadataResult;
import eu.europeana.api2.v2.utils.ControllerUtils;
import eu.europeana.corelib.db.entity.enums.RecordType;
import eu.europeana.corelib.definitions.model.ThumbSize;
import eu.europeana.corelib.definitions.solr.DocType;
import eu.europeana.corelib.domain.MediaFile;
import eu.europeana.corelib.logging.Log;
import eu.europeana.corelib.logging.Logger;
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

    @Log
    private Logger log;

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
            @RequestParam(value = "size", required = false, defaultValue = "FULL_DOC") String size,
            @RequestParam(value = "type", required = false, defaultValue = "IMAGE") String type,
            HttpServletResponse response) {
        controllerUtils.addResponseHeaders(response);

        String suffix = "";

        if (size.equalsIgnoreCase("BRIEF_DOC") || size.equalsIgnoreCase("h180") ||
                size.equalsIgnoreCase("FULL_DOC") || size.equalsIgnoreCase("FULL_DOC_ALL") || size.equalsIgnoreCase("w200")) {
            suffix = "200";
        } else if (size.equalsIgnoreCase("w400")) {
            suffix = "400";
        }

        final HttpHeaders headers = new HttpHeaders();

        byte[] imageResponse = DefaultImage.getImage(ThumbSize.LARGE, DocType.IMAGE);
        switch (StringUtils.upperCase(type)) {
            case "IMAGE":
                final String ID = getMD5(url) + "-" + ("200".equals(suffix) ? "MEDIUM" : "LARGE");
                final MediaFile mediaFile = mediaStorageService.retrieve(ID, true);

                if (mediaFile != null) {

                    switch (mediaFile.getContentType()) {
                        case "image/jpeg":
                            headers.setContentType(MediaType.IMAGE_JPEG);
                            break;
                        case "image/png":
                            headers.setContentType(MediaType.IMAGE_PNG);
                            break;
                        default:
                            // TODO: Ask Remy if we need to add more cases.
                            break;
                    }
                    imageResponse = mediaFile.getContent();

                } else {
                    imageResponse = getImage("/images/item-image-large.gif");
                    headers.setContentType(MediaType.IMAGE_GIF);
                }
                break;
            case "SOUND":
                imageResponse = getImage("/images/item-sound-large.gif");
                headers.setContentType(MediaType.IMAGE_GIF);
                break;
            case "VIDEO":
                imageResponse = getImage("/images/item-video-large.gif");
                headers.setContentType(MediaType.IMAGE_GIF);
                break;
            case "TEXT":
                imageResponse = getImage("/images/item-text-large.gif");
                headers.setContentType(MediaType.IMAGE_GIF);
                break;
            case "3D":
                imageResponse = getImage("/images/item-3d-large.gif");
                headers.setContentType(MediaType.IMAGE_GIF);
                break;
        }

        return new ResponseEntity<>(imageResponse, headers, HttpStatus.CREATED);
    }

    private byte[] getImage(String path) {
        byte[] response = null;

        BufferedImage img;
        try {
            img = ImageIO.read(getClass().getResourceAsStream(path));
            response = getByteArray(img);
        } catch (IOException e) {
            e.printStackTrace();
        }
//        int imgType = img.getType() == 0 ? BufferedImage.TYPE_INT_ARGB : img.getType();

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
}
