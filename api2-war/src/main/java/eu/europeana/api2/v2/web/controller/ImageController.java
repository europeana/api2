/*
 * Copyright 2007-2012 The Europeana Foundation
 *
 *  Licenced under the EUPL, Version 1.1 (the "Licence") and subsequent versions as approved 
 *  by the European Commission;
 *  You may not use this work except in compliance with the Licence.
 *  
 *  You may obtain a copy of the Licence at:
 *  http://joinup.ec.europa.eu/software/page/eupl
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under 
 *  the Licence is distributed on an "AS IS" basis, without warranties or conditions of 
 *  any kind, either express or implied.
 *  See the Licence for the specific language governing permissions and limitations under 
 *  the Licence.
 */

package eu.europeana.api2.v2.web.controller;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import eu.europeana.api2.utils.DefaultImageCache;
import eu.europeana.api2.v2.model.enums.DefaultImage;
import eu.europeana.corelib.db.entity.nosql.ImageCache;
import eu.europeana.corelib.db.exception.DatabaseException;
import eu.europeana.corelib.db.service.ThumbnailService;
import eu.europeana.corelib.definitions.edm.beans.FullBean;
import eu.europeana.corelib.definitions.model.ThumbSize;
import eu.europeana.corelib.edm.exceptions.MongoDBException;
import eu.europeana.corelib.search.SearchService;

/**
 * @author Willem-Jan Boogerd <www.eledge.net/contact>
 */
@Controller
@RequestMapping("/image")
public class ImageController {

	@Resource
	private ThumbnailService thumbnailService;

	@Resource
	private SearchService searchService;

	@RequestMapping("/{collectionId}/{recordId}.jpg")
	public ResponseEntity<byte[]> image(
			@PathVariable String collectionId,
			@PathVariable String recordId,
			@RequestParam(value = "imageid", required = false, defaultValue = ThumbnailService.DEFAULT_IMAGEID) String imageId,
			@RequestParam(value = "size", required = false, defaultValue = "MEDIUM") ThumbSize size) {
		byte[] image = null;
		String objectId = "/" + collectionId + "/" + recordId;
		// TODO Use corelib for object id generation
		MediaType mediaType = MediaType.IMAGE_JPEG;
		if (thumbnailService.exists(objectId, imageId)) {
			image = thumbnailService.retrieveThumbnail(objectId, imageId, size);
		}
		if (image == null) {
			// retrieve record
			try {
				FullBean bean = searchService.findById(collectionId, recordId, false);
				if (bean != null) {
					image = DefaultImageCache.getImage(bean.getType());
					mediaType = MediaType.IMAGE_GIF;
				}
			} catch (MongoDBException e) {
				// ignore and image for unknown
			}
		}
		if (image == null) {
			// load image for unkown type/object
			image = DefaultImage.UNKNOWN.getCache();
			mediaType = MediaType.IMAGE_PNG;
		}

		final HttpHeaders headers = new HttpHeaders();
		headers.setContentType(mediaType);
		return new ResponseEntity<byte[]>(image, headers, HttpStatus.OK);
	}

	@RequestMapping(value = "/image")
	public void imageRedirect(HttpServletResponse response,
			@RequestParam(value = "uri", required = false) String uri,
			@RequestParam(value = "size", required = false) String sizeString) {
		ImageCache imageCache = null;
		if (StringUtils.isBlank(sizeString)) {
			sizeString = ThumbSize.LARGE.toString();
		} else if (StringUtils.equalsIgnoreCase(sizeString, "BRIEF_DOC")) {
			sizeString = ThumbSize.MEDIUM.toString();
		} else if (StringUtils.equalsIgnoreCase(sizeString, "FULL_DOC")) {
			sizeString = ThumbSize.LARGE.toString();
		}
		try {
			imageCache = thumbnailService.findByOriginalUrl(uri);
		} catch (DatabaseException e) {
			// ignore
		}
		String objectId = imageCache != null ? imageCache.getObjectId() : "";
		StringBuilder sb = new StringBuilder();
		sb.append("/image/");
		sb.append(objectId);
		sb.append("?size=");
		sb.append(sizeString);
		response.setStatus(301);
		response.setHeader("Location", sb.toString());
		response.setHeader("Connection", "close");
	}
}
