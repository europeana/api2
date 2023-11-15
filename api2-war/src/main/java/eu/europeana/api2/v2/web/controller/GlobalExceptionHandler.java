package eu.europeana.api2.v2.web.controller;

import eu.europeana.api.commons.web.exception.EuropeanaGlobalExceptionHandler;
import eu.europeana.api2.ApiKeyException;
import eu.europeana.api2.model.json.ApiError;
import eu.europeana.api2.utils.JsonUtils;
import eu.europeana.api2.utils.XmlUtils;
import eu.europeana.api2.v2.exceptions.InvalidParamValueException;
import eu.europeana.api2.v2.exceptions.MissingParamException;
import eu.europeana.api2.v2.exceptions.TranslationServiceDisabledException;
import eu.europeana.api2.v2.exceptions.TranslationServiceLimitException;
import eu.europeana.api2.v2.model.xml.rss.Channel;
import eu.europeana.api2.v2.model.xml.rss.RssResponse;
import eu.europeana.api2.v2.utils.ControllerUtils;
import eu.europeana.corelib.edm.exceptions.SolrIOException;
import eu.europeana.corelib.edm.exceptions.SolrQueryException;
import eu.europeana.corelib.web.exception.EuropeanaException;
import org.apache.commons.lang3.StringUtils;
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
import java.util.regex.Pattern;


/**
 * This class functions as a global uncaught exception handler
 *
 * Created by luthien on 17/08/15.
 */

@ControllerAdvice
public class GlobalExceptionHandler extends EuropeanaGlobalExceptionHandler {

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
            return generateErrorResponse(request, response, ee.getMessage(), ee.getErrorDetails(), ee.getErrorCode());
        } catch (Exception ex) {
            LOG.error("Error while generating error response", ex);
            throw ex;
        }
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
        if (ee instanceof ApiKeyException) {
            if (((ApiKeyException) ee).getHttpStatus() > 0){
                result = ((ApiKeyException) ee).getHttpStatus();
            } else {
                result = HttpServletResponse.SC_UNAUTHORIZED;
            }
        } else if (ee instanceof SolrQueryException ||
                   ee instanceof InvalidParamValueException ||
                   ee instanceof MissingParamException ||
                   ee instanceof TranslationServiceDisabledException) {
            result = HttpServletResponse.SC_BAD_REQUEST;
        } else if (ee instanceof SolrIOException) {
            result = HttpServletResponse.SC_SERVICE_UNAVAILABLE;
        } else if (ee instanceof TranslationServiceLimitException) {
            result = HttpServletResponse.SC_BAD_GATEWAY;
        }
        return result;
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
    @ExceptionHandler(value = {HttpMessageNotReadableException.class,
            MissingServletRequestPartException.class, TypeMismatchException.class})
    public ModelAndView badRequestHandler(HttpServletRequest request, HttpServletResponse response, Exception e){
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        return generateErrorResponse(request, response, e.getMessage(), null, null);
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
