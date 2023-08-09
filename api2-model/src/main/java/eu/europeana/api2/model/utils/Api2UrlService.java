package eu.europeana.api2.model.utils;

import eu.europeana.corelib.definitions.ApplicationContextContainer;
import eu.europeana.corelib.definitions.EuropeanaStaticUrl;
import eu.europeana.corelib.record.BaseUrlWrapper;
import eu.europeana.corelib.web.service.impl.EuropeanaUrlBuilder;
import eu.europeana.corelib.web.utils.UrlBuilder;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;

import java.util.Map;
import java.util.Optional;

import static eu.europeana.api2.model.utils.RouteMatcher.getEntryForRoute;


/**
 * For generating often used links. Note that if an alternative portal.baseurl or api2.baseurl is defined in the
 * europeana.properties file then those base urls are used
 * @author Patrick Ehlert
 * Created on 10-10-2018
 */

public class Api2UrlService {

    public static final String API_BASEURL  = "https://api.europeana.eu";

    private final String apikeyValidateUrl;

    private final String defaultPortalBaseUrl;
    private final String defaultApi2BaseUrl;
    private final String defaultApiGatewayBaseUrl;

    private final Map<String, BaseUrlWrapper> routeBaseUrlMap;

    public Api2UrlService(Map<String, BaseUrlWrapper> routeBaseUrlMap, String portalBaseUrl, String api2BaseUrl, String apikeyValidateUrl, String apiGatewayBaseUrl) {
        this.routeBaseUrlMap = routeBaseUrlMap;
        this.defaultPortalBaseUrl = StringUtils.isNotBlank(portalBaseUrl) ? portalBaseUrl : EuropeanaStaticUrl.EUROPEANA_PORTAL_URL;
        this.defaultApi2BaseUrl = StringUtils.isNotBlank(api2BaseUrl) ? api2BaseUrl : API_BASEURL;
        this.apikeyValidateUrl = apikeyValidateUrl;
        if (apikeyValidateUrl.isBlank()) {
            LogManager.getLogger(Api2UrlService.class).warn("No API key services defined. API key validation is disabled!");
        }
        this.defaultApiGatewayBaseUrl = StringUtils.isNotBlank(apiGatewayBaseUrl) ? apiGatewayBaseUrl : EuropeanaStaticUrl.API_GATEWAY_URL;
    }


    /**
     * Provides quick access to this class
     * @return instance of the Api2UrlBuilder bean
     */
    public static Api2UrlService getBeanInstance() {
        return ApplicationContextContainer.getBean(Api2UrlService.class);
    }

    /**
     * @return either the default or alternative configured base url for the main Europeana website (a.k.a. Portal)
     * @param route
     */
    public String getPortalBaseUrl(String route) {
        Optional<BaseUrlWrapper> baseUrls = getEntryForRoute(route, routeBaseUrlMap);

        if (baseUrls.isEmpty() || StringUtils.isEmpty(baseUrls.get().getPortalBaseUrl())) {
            return defaultPortalBaseUrl;
        }
        return baseUrls.get().getPortalBaseUrl();
    }



    /**
     * @return either the default or alternative configured base url for the API
     */
    public String getApi2BaseUrl(String route) {
        Optional<BaseUrlWrapper> baseUrls = getEntryForRoute(route, routeBaseUrlMap);
        if (baseUrls.isEmpty() || StringUtils.isEmpty(baseUrls.get().getApi2BaseUrl())) {
            return defaultApi2BaseUrl;
        }
        return baseUrls.get().getApi2BaseUrl();
    }

    /**
     * @return the defined apikey service URL or null if not defined
     */
    public String getApikeyValidateUrl() {
        return apikeyValidateUrl;
    }

    /**
     * Generates an url to retrieve a record from the Europeana website
     * @param collectionId
     * @param itemId
     * @return url as String
     */
    public String getRecordPortalUrl(String route, String collectionId, String itemId) {
        return getRecordPortalUrl(route, "/" + collectionId + "/" +itemId);
    }

    /**
     * @return either the default or alternative configured api gateway url
     */
    public String getApiGatewayBaseUrl(String route) {
        Optional<BaseUrlWrapper> baseUrls = getEntryForRoute(route, routeBaseUrlMap);
        if (baseUrls.isEmpty() || StringUtils.isEmpty(baseUrls.get().getApiGatewayBaseUrl())) {
            return defaultApiGatewayBaseUrl;
        }
        return baseUrls.get().getApiGatewayBaseUrl();
    }



    /**
     * Generates an url to retrieve a record from the Europeana website.
     *
     * @param route server name from HTTP request from which this is triggered
     * @param europeanaId
     * @return url as String
     */
    public String getRecordPortalUrl(String route, String europeanaId) {
        UrlBuilder url = new UrlBuilder(this.getPortalBaseUrl(route))
                .addPath("item")
                .addPage(europeanaId);
        return url.toString();
    }

    /**
     * Generates an old-style url to retrieve a record from the Europeana website
     * Note that the EuropeanaId database still contains a lot of these urls as 'oldId'
     * @deprecated
     *
     * @param europeanaId
     * @return url as String
     */
    @Deprecated
    public String getRecordResolveUrl(String route, String europeanaId) {
        UrlBuilder url = new UrlBuilder(this.getPortalBaseUrl(route))
                .addPath("resolve", "record")
                .addPage(europeanaId);
        return url.toString();
    }

    /**
     * Generates an url to retrieve record JSON data from the Record API
     * @param collectionId
     * @param itemId
     * @param wskey
     * @return url as String
     */
    public String getRecordApi2Url(String route, String collectionId, String itemId, String wskey) {
        return getRecordApi2Url(route, "/" + collectionId + "/" +itemId, wskey);
    }


    /**
     * Generates an url to retrieve record JSON data from the Record API
     * @param europeanaId
     * @param wskey
     * @return url as String
     */
    public String getRecordApi2Url(String route, String europeanaId, String wskey) {
        UrlBuilder url = new UrlBuilder(getApi2BaseUrl(route))
                .addPath("record")
                .addPage(europeanaId + ".json")
                .addParam("wskey", wskey);
        return url.toString();
    }

    /**
     * Generates an url to retrieve a thumbnail with default size from the Europeana Thumbnail Storage
     * The url is processed eventually by the ThumbnailController in the API2 project.
     * @param uri uri of original thumbnail. A null value can be provided but will result in a not-working thumbnail-url
     *            so for proper working an uri is required.
     * @param type defaults to IMAGE (optional)
     * @return url as String
     */
    public String getThumbnailUrl(String route, String uri, String type) {
        return getThumbnailUrl(route, uri, null, type);
    }

    /**
     * Generates an url to retrieve a thumbnail from the Europeana Thumbnail Storage
     * The url is processed by the Thumbnail API.
     * @param uri uri of original thumbnail. A null value can be provided but will result in a not-working thumbnail-url
     *            so for proper working an uri is required.
     * @param size either w200 or w400, other values are ignored (optional)
     * @param type defaults to IMAGE (optional)
     * @return url as String
     */
    public String getThumbnailUrl(String route, String uri, String size, String type) {
        UrlBuilder url = EuropeanaUrlBuilder.getThumbnailUrl(uri, size, type);
        String newBaseUrl = this.getApiGatewayBaseUrl(route);
        if (newBaseUrl.contains("://")) {
            url.setProtocol(newBaseUrl);
        }
        url.setDomain(newBaseUrl);
        return url.toString();
    }

    @Deprecated
    public String getRedirectUrl(String route, String wskey, String isShownAtUri, String provider, String europeanaId, String profile) {
        UrlBuilder url = new UrlBuilder(this.getApi2BaseUrl(route))
                .addPath("api", String.valueOf(wskey), "redirect").disableTrailingSlash()
                .addParam("shownAt", isShownAtUri)
                // Note that provider and id are not required paramaters for the RedirectController, but sent along for
                // logging purposes.
                .addParam("provider", provider)
                .addParam("id", this.getRecordResolveUrl(route, europeanaId))
                // Not sure the profile parameter still serves any purpose, can probably be removed
                .addParam("profile", profile);
        return url.toString();
    }
}
