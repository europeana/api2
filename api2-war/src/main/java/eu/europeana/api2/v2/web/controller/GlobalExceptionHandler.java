package eu.europeana.api2.v2.web.controller;

import eu.europeana.api.commons.web.exception.HttpException;
import eu.europeana.api2.model.json.ApiError;
import eu.europeana.api2.utils.JsonUtils;
import eu.europeana.api2.utils.XmlUtils;
import eu.europeana.api2.v2.exceptions.*;
import eu.europeana.api2.v2.model.xml.rss.Channel;
import eu.europeana.api2.v2.model.xml.rss.RssResponse;
import eu.europeana.api2.v2.utils.ControllerUtils;
import eu.europeana.corelib.edm.exceptions.SolrIOException;
import eu.europeana.corelib.edm.exceptions.SolrQueryException;
import eu.europeana.corelib.web.exception.EuropeanaException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.TypeMismatchException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.servlet.ModelAndView;


/**
 * This class functions as a global uncaught exception handler
 * Created by luthien on 17/08/15.
 */

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger LOG = LogManager.getLogger(GlobalExceptionHandler.class);

    // urls may or may not start with http(s), so that's why we also need to check for eanadev.org or europeana.eu domain
    private static final Pattern EUROPEANA_URL = Pattern.compile("(at )?" +
            "((https?:\\/\\/)?[a-zA-Z0-9-_/.]*(\\.eanadev\\.org|\\.europeana\\.eu))" + // hostname
            "(:[a-zA-Z0-9-_/.]*)?"); // port and database name
    private static final String API_KEY_PARAM = "wskey";

    @Resource(name = "api2_mvc_xmlUtils")
    private XmlUtils xmlUtils;

    @ExceptionHandler(value = {EuropeanaException.class})
    public ModelAndView europeanaExceptionHandler(HttpServletRequest request, HttpServletResponse response, EuropeanaException ee) {
        try {
            String wskey = request.getParameter(API_KEY_PARAM);
            logOrIgnoreError(request.getServerName(), wskey, ee);
            response.setStatus(getHttpStatus(response, ee));
            // for TranslationServiceNotAvailableException and "quota limit errors", throw with specific error details
            if (ee instanceof TranslationServiceNotAvailableException && StringUtils.containsIgnoreCase(ee.getErrorDetails(), "quota limit reached")) {
                return generateErrorResponse(request, response, ee.getMessage(), "No more translations available today. Resource is exhausted", ee.getErrorCode());
            }
            return generateErrorResponse(request, response, ee.getMessage(), ee.getErrorDetails(), ee.getErrorCode());
        } catch (Exception ex) {
            LOG.error("Error while generating error response", ex);
            throw ex;
        }
    }

    @ExceptionHandler(value = {HttpException.class})
    public ModelAndView httpExceptionHandler(HttpServletRequest request, HttpServletResponse response, HttpException ee) {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        return generateErrorResponse(request, response, "Unauthorized",
                I18nErrorMessageKeys.getMessageForKey(ee.getI18nKey()), StringUtils.substringAfter(ee.getI18nKey(), "."));
    }


    private void logOrIgnoreError(String route, String apiKey, EuropeanaException ee) {
        switch (ee.getAction()) {
            case IGNORE: break;
            case LOG_WARN:
                // log apikey plus problem so we can track users who need help
                LOG.warn("[{} - {}] {}", route, apiKey, ee.getErrorMsgAndDetails());
                break;
            default: LOG.error("[{} - {}] {}", route, apiKey, ee.getErrorMsgAndDetails());
        }
    }

    private int getHttpStatus(HttpServletResponse response, EuropeanaException ee) {
        // default status is 200, but it may be changed by a controller before throwing the error so in that case we
        // keep the altered status
        if (response.getStatus() != HttpServletResponse.SC_OK) {
            return response.getStatus();
        }
        // set status depending on type of exception
        int result = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
            if (ee instanceof SolrQueryException ||
                   ee instanceof InvalidParamValueException ||
                   ee instanceof MissingParamException ||
                   ee instanceof TranslationServiceDisabledException) {
            result = HttpServletResponse.SC_BAD_REQUEST;
        } else if (ee instanceof SolrIOException) {
            result = HttpServletResponse.SC_SERVICE_UNAVAILABLE;
        } else if (ee instanceof TranslationServiceNotAvailableException) {
            result = HttpServletResponse.SC_BAD_GATEWAY;
        } else if ( ee instanceof InvalidAuthorizationException) {
                result = HttpServletResponse.SC_FORBIDDEN;
            }
        return result;
    }

    /**
     * Handles all required parameter missing problems
     * Don't need apikey missing handling here anymore, will be handled in HttpException
     *
     * @param request
     * @param response
     * @param ex
     * @return
     */
    @ExceptionHandler(value = {MissingServletRequestParameterException.class})
    public ModelAndView missingParameterErrorHandler (HttpServletRequest request, HttpServletResponse response,
                                                      MissingServletRequestParameterException ex) {
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        String errorMsg = "Required parameter '" + ex.getParameterName() + "' missing";
        return generateErrorResponse(request, response, errorMsg, null, null);
    }

    /**
     * Handles HttpMediaTypeNotAcceptableExceptions
     * @param request
     * @param response
     * @return
     */
    @ExceptionHandler(value = {HttpMediaTypeNotAcceptableException.class})
    public ModelAndView mediaTypeNotAcceptableHandler(HttpServletRequest request, HttpServletResponse response) {
        response.setStatus(HttpServletResponse.SC_NOT_ACCEPTABLE);
        String requestedMediaType = ControllerUtils.getRequestedMediaType(request);
        String errorMsg = "The resource identified by this request cannot generate a response of type " + requestedMediaType;
        return generateErrorResponse(request, response, errorMsg, null, null);
    }

    /**
     * Handles HttpMediaTypeNotSupportedExceptions
     * @param request  HttpServletRequest
     * @param response HttpServletResponse
     * @return ModelAndView
     */
    @ExceptionHandler(value = {HttpMediaTypeNotSupportedException.class})
    public ModelAndView mediaTypeNotSupportedHandler(HttpServletRequest request, HttpServletResponse response) {
        response.setStatus(HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE);
        String requestedContentType = ControllerUtils.getRequestedContentType(request);
        String errorMsg = "Content type '" + requestedContentType + " not supported";
        return generateErrorResponse(request, response, errorMsg, null, null);
    }

    /**
     * Handles "Bad Requests" causing HttpMessageNotReadableException,
     * MissingServletRequestParameterException, TypeMismatchException
     * @param  request HttpServletRequest
     * @param  response HttpServletResponse
     * @return  ModelAndView
     */
    @ExceptionHandler(value = {HttpMessageNotReadableException.class,
            MissingServletRequestPartException.class, TypeMismatchException.class})
    public ModelAndView badRequestHandler(HttpServletRequest request, HttpServletResponse response, Exception e){
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        return generateErrorResponse(request, response, e.getMessage(), null, null);
    }


    /**
     * Handles HttpMediaTypeNotSupportedExceptions
     * @param request
     * @param response
     * @return
     */
    @ExceptionHandler(value = {HttpRequestMethodNotSupportedException.class})
    public ModelAndView unsupportedMethodHandler(HttpServletRequest request, HttpServletResponse response){
        response.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
        String errorMsg = "Request method '" + request.getMethod() + "' is not allowed for the requested resource";
        return generateErrorResponse(request, response, errorMsg, null, null);
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
    public ModelAndView defaultExceptionHandler(HttpServletRequest request, HttpServletResponse response, Exception e)  {
        try {
            LOG.error("Caught unexpected exception", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            String errorMsg = "Internal server error";
            String errorDetails = e.getMessage();
            return generateErrorResponse(request, response, errorMsg, errorDetails, null);
        } catch (Exception ex) {
            LOG.error("Error while generating error response", ex);
            throw ex;
        }
    }


    private ModelAndView generateErrorResponse(HttpServletRequest request, HttpServletResponse response,
                                              String errorMsg, String errorDetails, String errorCode) {
        response.setCharacterEncoding("UTF-8");
        String requestFormat = ControllerUtils.getRequestFormat(request);
        String details = filterSensitiveInfo(errorDetails);
        if ("RDF".equalsIgnoreCase(requestFormat)) {
            response.setContentType("application/rdf+xml");
            return generateRdfError(errorMsg, details);
        } else if ("RSS".equalsIgnoreCase(requestFormat)) {
            response.setContentType("application/xml");
            return generateRssError(errorMsg, details);
        }
        return generateJsonError(request, errorMsg, details, errorCode);
    }

    private ModelAndView generateRdfError(String errorMessage, String errorDetails) {
        Map<String, Object> model = new HashMap<>();
        model.put("error", errorMessage);
        model.put("errorDetails", errorDetails);
        return new ModelAndView("rdf", model);
    }

    private ModelAndView generateRssError(String errorMessage, String errorDetails) {
        RssResponse response = new RssResponse();
        Channel channel = response.channel;
        channel.setTitle("Error");
        channel.setDescription(errorMessage + (StringUtils.isEmpty(errorDetails) ? "" : (" - " + errorDetails)));
        channel.setAtomLink(null);
        channel.setLink(null);
        channel.totalResults = null;
        channel.startIndex = null;
        channel.itemsPerPage = null;
        channel.query = null;
        channel.image = null;

        String xml = xmlUtils.toString(response);
        Map<String, Object> model = new HashMap<>();
        model.put("rss", xml);
        return new ModelAndView("rss", model);
    }

    private ModelAndView generateJsonError(HttpServletRequest request, String errorMsg, String errorDetails, String errorCode) {
        String wskey = request.getParameter(API_KEY_PARAM);
        String callback = request.getParameter("callback");
        return JsonUtils.toJson(new ApiError(wskey, errorMsg, errorDetails, errorCode), callback);
    }

    /**
     * Some error messages include server urls and we don't want to display that to end-users, so that's why we
     * filter this out.
     */
    private String filterSensitiveInfo(String errorMsg) {
        // just to be sure a simple regex to filter out urls
        if (StringUtils.isNotEmpty(errorMsg)) {
            return EUROPEANA_URL.matcher(errorMsg).replaceAll("-");
        }
        return errorMsg;
    }


}
