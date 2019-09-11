package eu.europeana.api2.v2.web.controller;

import eu.europeana.api2.ApiKeyException;
import eu.europeana.api2.model.json.ApiError;
import eu.europeana.api2.utils.JsonUtils;
import eu.europeana.api2.utils.XmlUtils;
import eu.europeana.api2.v2.model.xml.rss.Channel;
import eu.europeana.api2.v2.model.xml.rss.Item;
import eu.europeana.api2.v2.model.xml.rss.RssResponse;
import eu.europeana.api2.v2.utils.ControllerUtils;
import eu.europeana.corelib.edm.exceptions.SolrIOException;
import eu.europeana.corelib.edm.exceptions.SolrQueryException;
import eu.europeana.corelib.web.exception.EmailServiceException;
import eu.europeana.corelib.web.exception.EuropeanaException;
import eu.europeana.corelib.web.service.EmailService;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
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

import javax.annotation.Resource;
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
public class GlobalExceptionHandler {

    private static final Logger LOG = LogManager.getLogger(GlobalExceptionHandler.class);

    @Resource(name = "corelib_web_emailService")
    private EmailService emailService;

    @Resource(name = "api2_mvc_xmlUtils")
    private XmlUtils xmlUtils;

    @ExceptionHandler(value = {EuropeanaException.class})
    public ModelAndView europeanaExceptionHandler(HttpServletRequest request, HttpServletResponse response, EuropeanaException ee) {
        String wskey = request.getParameter("wskey");
        mailLogOrIgnoreError(wskey, ee);
        response.setStatus(getHttpStatus(response, ee));
        return generateErrorResponse(request, response, ee.getMessage(), ee.getErrorDetails(), ee.getErrorCode());
    }

    private void mailLogOrIgnoreError(String apiKey, EuropeanaException ee) {
        switch (ee.getAction()) {
            case IGNORE: break;
            case LOG_WARN: {
                // log apikey plus problem so we can track users who need help
                LOG.warn("[{}] {}", apiKey, ee.getErrorMsgAndDetails());
                break;
            }
            case MAIL: {
                LOG.error(ee.getErrorMsgAndDetails(), ee);
                try {
                    emailService.sendException("Search API exception: " + ee.getErrorMsgAndDetails(), ExceptionUtils.getStackTrace(ee));
                    LOG.info("Exception email was sent");
                } catch (EmailServiceException es) {
                    LOG.error("Error sending exception email", es);
                }
                break;
            }
            default: LOG.error(ee.getErrorMsgAndDetails(), ee);
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
        if (ee instanceof ApiKeyException) {
            result = HttpServletResponse.SC_UNAUTHORIZED;
        } else if (ee instanceof SolrQueryException) {
            result = HttpServletResponse.SC_BAD_REQUEST;
        } else if (ee instanceof SolrIOException) {
            result = HttpServletResponse.SC_SERVICE_UNAVAILABLE;
        }
        return result;
    }

    /**
     * Handles all required parameter missing problems (e.g. APIkey missing)
     * @param request
     * @param response
     * @param ex
     * @return
     */
    @ExceptionHandler(value = {MissingServletRequestParameterException.class})
    public ModelAndView missingParameterErrorHandler (HttpServletRequest request, HttpServletResponse response,
                                                      MissingServletRequestParameterException ex) {
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        String errorMsg;
        if ("wskey".equalsIgnoreCase(ex.getParameterName())) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            errorMsg = "No API key provided";
        } else {
            errorMsg = "Required parameter '" + ex.getParameterName() + "' missing";
        }
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
     * @param request
     * @param response
     * @return
     */
    @ExceptionHandler(value = {HttpMediaTypeNotSupportedException.class})
    public ModelAndView mediaTypeNotSupportedHandler(HttpServletRequest request, HttpServletResponse response) {
        response.setStatus(HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE);
        String requestedContentType = ControllerUtils.getRequestedContentType(request);
        String errorMsg = "Content type '" + requestedContentType + " not supported";
        return generateErrorResponse(request, response, errorMsg, null, null);
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
        LOG.error("Caught unexpected exception", e);
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        String errorMsg = "Internal server error";
        String errorDetails = e.getMessage();
        return generateErrorResponse(request, response, errorMsg, errorDetails, null);
    }

    public ModelAndView generateErrorResponse(HttpServletRequest request, HttpServletResponse response,
                                              String errorMsg, String errorDetails, String errorCode) {
        ControllerUtils.addResponseHeaders(response);
        String requestFormat = ControllerUtils.getRequestFormat(request);
        if ("RDF".equalsIgnoreCase(requestFormat)) {
            response.setContentType("application/rdf+xml");
            return generateRdfError(errorMsg, errorDetails);
        } else if ("RSS".equalsIgnoreCase(requestFormat)) {
            response.setContentType("application/xml");
            return generateRssError(errorMsg, errorDetails);
        }
        return generateJsonError(request, errorMsg, errorDetails, errorCode);
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
        channel.totalResults.value = 0;
        Item item = new Item();
        item.title = "Error";
        item.description = errorMessage + (StringUtils.isEmpty(errorDetails) ? "" : (" - " + errorDetails));
        channel.items.add(item);

        String xml = xmlUtils.toString(response);
        Map<String, Object> model = new HashMap<>();
        model.put("rss", xml);
        return new ModelAndView("rss", model);
    }

    private ModelAndView generateJsonError(HttpServletRequest request, String errorMsg, String errorDetails, String errorCode) {
        String wskey = request.getParameter("wskey");
        String callback = request.getParameter("callback");
        return JsonUtils.toJson(new ApiError(wskey, errorMsg, errorDetails, errorCode), callback);
    }

}
