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
import eu.europeana.api2.v2.utils.ControllerUtils;
import org.apache.log4j.Logger;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;


/**
 * This class replaces the default Spring handling of missing required parameters and functions as a
 * global uncaught exception handler
 *
 * Created by luthien on 17/08/15.
 */

@ControllerAdvice
public class ExceptionControllerAdvice extends ResponseEntityExceptionHandler {

    private static final Logger LOG = Logger.getLogger(ExceptionControllerAdvice.class);

    /**
     * Handles apikey parameter missing problems
     * @param ex
     * @param headers
     * @param status
     * @param request
     * @return
     */
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

    /**
     * Handles all ApiLimitExceptions
     * @param request
     * @param response
     * @param e
     * @return
     */
    @ExceptionHandler(value = {ApiLimitException.class})
    public ModelAndView defaultErrorHandler(HttpServletRequest request, HttpServletResponse response, ApiLimitException e)
            throws ApiLimitException{
        response.setStatus(e.getHttpStatus());
        String requestFormat = ControllerUtils.getRequestFormat(request);
        if ("RDF".equalsIgnoreCase(requestFormat)) {
            Map<String, Object> model = new HashMap<>();
            model.put("error", "Unregistered API key");
            return new ModelAndView("rdf", model);
        } else if ("SRW".equalsIgnoreCase(requestFormat)) {
            // No specification available how to provide error details in SRW format, this is a temp solution
            return null;
        }
        return JsonUtils.toJson(new ApiError(e), request.getParameter("callback"));
    }

    /**
     * General error handler. This handler is used when there are no more specific handlers for the error in question.
     * The drawback of using this is that we cannot supply the requestNumber in the error message
     * @param request
     * @param response
     *
     * @param e
     * @return ModelAndView with error message
     */
    @ExceptionHandler(value = {Exception.class})
    public ModelAndView defaultExceptionHandler(HttpServletRequest request, HttpServletResponse response, Exception e)
            throws Exception {
        LOG.error("Caught exception:", e);
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        String requestFormat = ControllerUtils.getRequestFormat(request);

        if ("RDF".equalsIgnoreCase(requestFormat)) {
            Map<String, Object> model = new HashMap<>();
            model.put("error", "Internal server error");
            return new ModelAndView("rdf", model);
        } else if ("SRW".equalsIgnoreCase(requestFormat)) {
            // No specification available how to provide error details in SRW format, this is a temp solution
            throw e;
        }
        String wskey = request.getParameter("wskey");
        String callback = request.getParameter("callback");
        return JsonUtils.toJson(new ApiError(wskey, e.getClass().getSimpleName() + ": "+ e.getMessage()), callback);
    }

}
