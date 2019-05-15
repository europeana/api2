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
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.TypeMismatchException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
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

    private static final Logger         LOG = LogManager.getLogger(ExceptionControllerAdvice.class);

    /**
     * Handles all required parameter missing problems (e.g. APIkey missing)
     * @param request
     * @param response
     * @param ex
     * @return
     */
    @ExceptionHandler(value = {MissingServletRequestParameterException.class})
    public ModelAndView missingParameterErrorHandler (HttpServletRequest request,
                                                      HttpServletResponse response,
                                                      MissingServletRequestParameterException ex) {
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
    public ModelAndView apiLimitErrorHandler(HttpServletRequest request,
                                             HttpServletResponse response,
                                             ApiLimitException e) {
        ControllerUtils.addResponseHeaders(response);
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
     * Handles HttpMediaTypeNotAcceptableExceptions
     * @param request
     * @param response
     * @param e
     * @return
     */
    @ExceptionHandler(value = {HttpMediaTypeNotAcceptableException.class})
    public ModelAndView mediaTypeNotAcceptableHandler(HttpServletRequest request,
                                             HttpServletResponse response,
                                             HttpMediaTypeNotAcceptableException e) {
        ControllerUtils.addResponseHeaders(response);
        response.setStatus(HttpServletResponse.SC_NOT_ACCEPTABLE);
        String requestedMediaType = ControllerUtils.getRequestedMediaType(request);
        String errorMsg;
        if (StringUtils.isNotBlank(requestedMediaType)){
            errorMsg = "The resource identified by this request cannot generate a response of type " + requestedMediaType;
        } else {
            errorMsg = "";
        }
        return JsonUtils.toJson(new ApiError("", errorMsg));
    }

    /**
     * Handles HttpMediaTypeNotSupportedExceptions
     * @param request
     * @param response
     * @param e
     * @return
     */
    @ExceptionHandler(value = {HttpMediaTypeNotSupportedException.class})
    public ModelAndView mediaTypeNotSupportedHandler(HttpServletRequest request,
                                                 HttpServletResponse response,
                                                 HttpMediaTypeNotSupportedException e) {
        ControllerUtils.addResponseHeaders(response);
        response.setStatus(HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE);
        String requestedContentType = ControllerUtils.getRequestedContentType(request);
        String errorMsg;
        if (StringUtils.isNotBlank(requestedContentType)){
            errorMsg = "Content type '" + requestedContentType + " not supported";
        } else {
            errorMsg = "";
        }
        return JsonUtils.toJson(new ApiError("", errorMsg));
    }

    /**
     * Handles "Bad Requests" causing HttpMessageNotReadableException, MethodArgumentNotValidException,
     * MissingServletRequestParameterException, MissingServletRequestPartException & TypeMismatchException
     * @param response
     * @param e
     * @return
     */
    @ExceptionHandler(value = {HttpMessageNotReadableException.class, MethodArgumentNotValidException.class,
            MissingServletRequestPartException.class, TypeMismatchException.class})
    public ModelAndView badRequestHandler(HttpServletResponse response,
                                          Exception e){
        ControllerUtils.addResponseHeaders(response);
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        return JsonUtils.toJson(new ApiError("", e.getMessage()));
    }

    /**
     * Handles HttpMediaTypeNotSupportedExceptions
     * @param request
     * @param response
     * @param e
     * @return
     */
    @ExceptionHandler(value = {HttpRequestMethodNotSupportedException.class})
    public ModelAndView unsupportedMethodHandler(HttpServletRequest request,
                                                 HttpServletResponse response,
                                                 HttpRequestMethodNotSupportedException e){
        ControllerUtils.addResponseHeaders(response);
        response.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
        String errorMsg = "Request method '" + request.getMethod() + "' is not allowed for the requested resource";
        return JsonUtils.toJson(new ApiError("", errorMsg));
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
    public ModelAndView defaultExceptionHandler(HttpServletRequest request,
                                                HttpServletResponse response,
                                                Exception e) throws Exception {
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
