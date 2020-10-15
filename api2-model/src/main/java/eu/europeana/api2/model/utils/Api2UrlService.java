package eu.europeana.api2.model.utils;

import eu.europeana.corelib.definitions.ApplicationContextContainer;
import eu.europeana.corelib.definitions.EuropeanaStaticUrl;
import eu.europeana.corelib.record.BaseUrlWrapper;
import eu.europeana.corelib.web.service.impl.EuropeanaUrlBuilder;
import eu.europeana.corelib.web.utils.UrlBuilder;
import org.apache.commons.lang3.StringUtils;

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

    private String portalBaseUrl;
    private final String apikeyValidateUrl;
    private String api2BaseUrl;
    private String apiGatewayBaseUrl;

    private final Map<String, BaseUrlWrapper> routeBaseUrlMap;

    public Api2UrlService(Map<String, BaseUrlWrapper> routeBaseUrlMap, String portalBaseUrl, String api2BaseUrl, String apikeyValidateUrl, String apiGatewayBaseUrl) {
        this.routeBaseUrlMap = routeBaseUrlMap;
        this.portalBaseUrl = portalBaseUrl;
        this.apikeyValidateUrl = apikeyValidateUrl;
        this.api2BaseUrl = api2BaseUrl;
        this.apiGatewayBaseUrl = apiGatewayBaseUrl;
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
        Optional<BaseUrlWrapper> baseUrls = getEntryForRoute(route, routeBaseUrlMap, "Portal BaseUrl");

        if (baseUrls.isEmpty() || StringUtils.isEmpty(baseUrls.get().getPortalBaseUrl())) {
            return StringUtils.isNotBlank(portalBaseUrl) ? portalBaseUrl : EuropeanaStaticUrl.EUROPEANA_PORTAL_URL;
        }
        return baseUrls.get().getPortalBaseUrl();
    }



    /**
     * @return either the default or alternative configured base url for the API
     */
    public String getApi2BaseUrl(String route) {
        Optional<BaseUrlWrapper> baseUrls = getEntryForRoute(route, routeBaseUrlMap, "Api2 BaseUrl");
        if (baseUrls.isEmpty() || StringUtils.isEmpty(baseUrls.get().getApi2BaseUrl())) {
            return StringUtils.isNotBlank(api2BaseUrl) ? api2BaseUrl : API_BASEURL;
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
        Optional<BaseUrlWrapper> baseUrls = getEntryForRoute(route, routeBaseUrlMap, "Api Gateway BaseUrl");
        if (baseUrls.isEmpty() || StringUtils.isEmpty(baseUrls.get().getApiGatewayBaseUrl())) {
            return StringUtils.isNotBlank(apiGatewayBaseUrl) ? apiGatewayBaseUrl : EuropeanaStaticUrl.API_GATEWAY_URL;
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
                .addPath(getApiRecordPath(route))
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

    /**
     * Generates URL path to search result record.
     * /api/v2/ path prefix not required if running in production (EA-2151)
     *
     * TODO: Remove hardcoded url check
     * @return string array with path to record.
     */
    private String[] getApiRecordPath(String route) {
        if (!API_BASEURL.equals(getApi2BaseUrl(route))) {
            return new String[]{"api", "v2"};
        } else {
            return new String[]{};
        }
    }
}
