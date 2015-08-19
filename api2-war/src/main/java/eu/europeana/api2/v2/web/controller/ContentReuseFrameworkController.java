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

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.annotation.Resource;
import javax.imageio.ImageIO;

import org.springframework.stereotype.Controller;

import eu.europeana.api2.v2.utils.ControllerUtils;
import eu.europeana.corelib.db.service.ApiKeyService;
import eu.europeana.corelib.db.service.ApiLogService;
import eu.europeana.corelib.logging.Log;
import eu.europeana.corelib.logging.Logger;
import eu.europeana.corelib.search.SearchService;
import eu.europeana.corelib.web.service.EuropeanaUrlService;

@Controller
public class ContentReuseFrameworkController {

    @Log
    private Logger log;

    @Resource
    private SearchService searchService;

    @Resource
    private ApiLogService apiLogService;

    @Resource
    private ApiKeyService apiService;

    @Resource
    private EuropeanaUrlService urlService;

    @Resource
    private ControllerUtils controllerUtils;

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