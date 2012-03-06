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

package eu.europeana.api2.web.controller;

import javax.annotation.Resource;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import eu.europeana.api2.utils.DefaultImageCache;
import eu.europeana.corelib.db.exception.DatabaseException;
import eu.europeana.corelib.db.service.ThumbnailService;
import eu.europeana.corelib.definitions.model.ThumbSize;
import eu.europeana.corelib.definitions.solr.beans.FullBean;
import eu.europeana.corelib.solr.exceptions.SolrTypeException;
import eu.europeana.corelib.solr.service.SearchService;

/**
 * @author Willem-Jan Boogerd <www.eledge.net/contact>
 */
@Controller
public class ImageController {

	@Resource
	private ThumbnailService thumbnailService;

	@Resource
	private SearchService searchService;

	@RequestMapping(value = "/image", produces = MediaType.IMAGE_JPEG_VALUE)
	public ResponseEntity<byte[]> image(@RequestParam(value = "apikey", required = true) String apiKey,
			@RequestParam(value = "objectId", required = true) String objectid,
			@RequestParam(value = "size", required = false, defaultValue = "MEDIUM") ThumbSize size) {
		// TODO: apikey checking
		byte[] image = null;
		MediaType mediaType = MediaType.IMAGE_JPEG;
		try {
			if (thumbnailService.exists(objectid)) {
				image = thumbnailService.retrieveThumbnail(objectid, size);
			}
		} catch (DatabaseException e) {
			// ignore and return default image
		}
		if (image == null) {
			// retrieve record
			FullBean bean;
			try {
				bean = searchService.findById(objectid);
				if (bean != null) {
					image = DefaultImageCache.getImage(bean.getType());
					mediaType = MediaType.IMAGE_GIF;
				}
			} catch (SolrTypeException e) {
				// ignore and image for unknown
			}
		}
		if (image == null) {
			// load image for unkown type/object
			image = DefaultImageCache.getImageUnknown();
			mediaType = MediaType.IMAGE_PNG;
		}

		final HttpHeaders headers = new HttpHeaders();
		headers.setContentType(mediaType);
		return new ResponseEntity<byte[]>(image, headers, HttpStatus.OK);
	}

}
