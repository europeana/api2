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

import com.google.common.base.Throwables;
import eu.europeana.api2.ApiLimitException;
import eu.europeana.api2.model.json.ApiError;
import eu.europeana.api2.utils.JsonUtils;
import eu.europeana.api2.v2.utils.ControllerUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;


/**
 * This class functions as a global uncaught exception handler
 *
 * Created by luthien on 17/08/15.
 */

@ControllerAdvice
public class ExceptionControllerAdvice {

    private static final Logger LOG = LogManager.getLogger(ExceptionControllerAdvice.class);

    /**
     * Handles all required parameter missing problems (e.g. APIkey missing)
     * @param request
     * @param response
     * @param ex
     * @return
     */
    @ExceptionHandler(value = {MissingServletRequestParameterException.class})
    public ModelAndView missingParameterErrorHandler (HttpServletRequest request, HttpServletResponse response, MissingServletRequestParameterException ex) {
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        String requestFormat = ControllerUtils.getRequestFormat(request);

        String errorMsg;
        if ("wskey".equalsIgnoreCase(ex.getParameterName())) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            errorMsg = "No API key provided";
        } else {
            errorMsg = "Required parameter '" + ex.getParameterName() + "' missing";
        }

        ModelAndView result;
        if ("RDF".equalsIgnoreCase(requestFormat)) {
            result = generateRdfError(errorMsg);
        } else if ("SRW".equalsIgnoreCase(requestFormat)) {
            // No specification available how to provide error details in SRW format, this is a temp solution
            return null;
        } else {
            result = JsonUtils.toJson(new ApiError("", errorMsg));
        }
        return result;
    }

    /**
     * Handles all ApiLimitExceptions
     * @param request
     * @param response
     * @param e
     * @return
     */
    @ExceptionHandler(value = {ApiLimitException.class})
    public ModelAndView apiLimitErrorHandler(HttpServletRequest request, HttpServletResponse response, ApiLimitException e)
            throws ApiLimitException{
        response.setStatus(e.getHttpStatus());
        String requestFormat = ControllerUtils.getRequestFormat(request);
        if ("RDF".equalsIgnoreCase(requestFormat)) {
            return generateRdfError("Unregistered API key");
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
        LOG.error("Caught exception: {}", e);
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        String requestFormat = ControllerUtils.getRequestFormat(request);

        if ("RDF".equalsIgnoreCase(requestFormat)) {
            return generateRdfError("Internal server error");
        } else if ("SRW".equalsIgnoreCase(requestFormat)) {
            // No specification available how to provide error details in SRW format, this is a temp solution
            throw e;
        }
        String wskey = request.getParameter("wskey");
        String callback = request.getParameter("callback");
        return JsonUtils.toJson(new ApiError(wskey, e.getClass().getSimpleName() + ": "+ e.getMessage()), callback);
    }

    private ModelAndView generateRdfError(String errorMessage) {
        Map<String, Object> model = new HashMap<>();
        model.put("error", errorMessage);
        return new ModelAndView("rdf", model);
    }

}
