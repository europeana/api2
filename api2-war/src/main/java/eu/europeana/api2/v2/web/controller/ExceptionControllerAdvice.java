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

import eu.europeana.api2.model.json.ApiError;
import eu.europeana.api2.utils.JsonUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;


/**
 * This class replaces the default Spring handling of missing required parameters
 * adding an error message, the option to alter the HTTP status & returning JSON
 *
 * Created by luthien on 17/08/15.
 */

@ControllerAdvice
public class ExceptionControllerAdvice extends ResponseEntityExceptionHandler {

    @Override
    protected ResponseEntity<Object> handleMissingServletRequestParameter(
            MissingServletRequestParameterException ex, HttpHeaders headers,
            HttpStatus status, WebRequest request) {
        ModelAndView mav;
//        if (ex.getParameterName().equalsIgnoreCase("wskey")) {
            mav = JsonUtils.toJson(new ApiError("", "No API key provided"));
//        } else {
//            mav = JsonUtils.toJson(new ApiError("", "Required parameter '" + ex.getParameterName() + "' missing"));

//        }
        return new ResponseEntity<>(mav.getModel().get("json"), headers, HttpStatus.FORBIDDEN);
    }
}
