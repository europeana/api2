package eu.europeana.api2.v2.service.translate;

import eu.europeana.api.translation.client.TranslationApiClient;
import eu.europeana.api2.v2.exceptions.TranslationException;
import eu.europeana.corelib.definitions.edm.beans.FullBean;
import eu.europeana.corelib.definitions.edm.entity.ContextualClass;
import eu.europeana.corelib.definitions.edm.entity.Proxy;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.util.*;
import java.util.function.Function;

public class BaseService {

    private static final Logger LOG = LogManager.getLogger(BaseService.class);

    private final TranslationApiClient translationApiClient;

    protected static final List<String> PRECENDANCE_LIST = List.of("sk", "hr", "pl", "ro", "it", "sv", "bg", "fr", "es", "cs", "de", "lv", "el", "fi", "nl", "hu", "da", "sl", "et", "pt", "lt", "ga", "en");

    private static final Set<String> INCLUDE_PROXY_MAP_FIELDS = Set.of("dcContributor", "dcCoverage", "dcCreator", "dcDate", "dcDescription", "dcFormat","dcPublisher",
            "dcRelation", "dcRights", "dcSource", "dcSubject", "dcTitle", "dcType", "dctermsAlternative", "dctermsCreated", "dctermsExtent", "dctermsHasPart", "dctermsHasVersion",
            "dctermsIsFormatOf", "dctermsIsPartOf", "dctermsIsReferencedBy", "dctermsIsReplacedBy", "dctermsIsRequiredBy", "dctermsIssued", "dctermsMedium", "dctermsProvenance",
            "dctermsReferences", "dctermsSpatial", "dctermsTemporal", "edmCurrentLocation", "edmHasMet");

    private static final Set<String> SEARCH_FIELDS_TO_TRANSLATE = Set.of("dcTitleLangAware", "dcDescriptionLangAware", "dcCreatorLangAware");

    // The non-language aware fields that should disappear. For title -> the values are picked from title, proxyDcTitle and dcDescription
    // hence we need remove "title" and "proxyDcTitle" to remove title field. @see eu.europeana.corelib.solr.bean.impl.BriefBeanImpl#getTitle()
    private static final Set<String> SEARCH_FIELDS_TO_FILTER = Set.of("title", "proxyDcTitle", "dcDescription", "dcCreator");

    private static final List<String> ENTITIES = List.of("agents", "concepts", "places", "timespans");

    public static final ReflectionUtils.FieldFilter proxyFieldFilter = field -> field.getType().isAssignableFrom(Map.class) &&
            INCLUDE_PROXY_MAP_FIELDS.contains(field.getName());

    public static final ReflectionUtils.FieldFilter searchFieldFilter = field -> field.getType().isAssignableFrom(Map.class) &&
            SEARCH_FIELDS_TO_TRANSLATE.contains(field.getName());

    public static final ReflectionUtils.FieldFilter searchFieldRemovalFilter = field -> field.getType().isAssignableFrom(String[].class) &&
            SEARCH_FIELDS_TO_FILTER.contains(field.getName());

    public BaseService(TranslationApiClient translationApiClient) {
        this.translationApiClient = translationApiClient;
    }

    public TranslationApiClient getTranslationApiClient() {
        return translationApiClient;
    }

    /**
     * Get the europeana proxy from the list of proxy
     * There are records present where the first proxy is not always the europeana proxy
     * @param proxies
     * @param recordId
     * @return
     * @throws TranslationException
     */
    public static Proxy getEuropeanaProxy(List<? extends Proxy> proxies, String recordId) throws TranslationException {
        Optional<? extends Proxy> europeanaProxy = proxies.stream().filter(Proxy :: isEuropeanaProxy).findFirst();
        if (europeanaProxy.isPresent()) {
            return europeanaProxy.get();
        } else {
            throw new TranslationException("Unexpected data - Europeana proxy not present! Record id - " +recordId);
        }
    }

    /**
     * Function to get the lang-value map of the field from the Object
     * @param object Could be any object. Usually for record - proxy and for search - BriefBean
     * @param update if true, and the value is null for the field - It sets the empty map in the proxy object
     *               for that field.
     * @return
     */
    public static Function<String, Map<String, List<String>>> getValueOfTheField(Object object, boolean update) {
        return e -> {
            Field field = ReflectionUtils.findField(object.getClass(), e);
            ReflectionUtils.makeAccessible(field);
            Object value = ReflectionUtils.getField(field, object);
            // If we are updating the proxy value, then for the field we must set an empty map
            // if it doesn't exist already. When we are just fetching the values, we need not alter anything in the proxy object
            if (value == null && update) {
                ReflectionUtils.setField(field, object, new LinkedHashMap<>());
                value = ReflectionUtils.getField(field, object);
            }
            if (value instanceof Map) {
                return (Map<String, List<String>>) value;
            } else if (value != null) { // should not happen as the whitelisted values are all lang-map
                LOG.warn("Unexpected data - field {} did not return a map", e);
            }
            return new LinkedHashMap<>(); // default return an empty map
        };
    }

    /**
     * Finds the Contextual entity from the bean matching the uri
     * @param bean record
     * @param uri url to check
     * @return
     */
    public static ContextualClass entityExistsWithUrl(FullBean bean, String uri) {
        List<ContextualClass> matchingEntity= new ArrayList<>();

        // check only entity objects
        ReflectionUtils.FieldFilter entityFilter = field -> ENTITIES.contains(field.getName());

        ReflectionUtils.doWithFields(bean.getClass(), field -> {
            // if we found the Contextual class already, no need to iterate more
            if (matchingEntity.size() == 1) {
                return;
            }
            ReflectionUtils.makeAccessible(field);
            Object o = ReflectionUtils.getField(field, bean);
            LOG.trace("Searching for entities with type {}...", field.getName());
            // check only if it's a list and is not empty
            if (o instanceof List && !((List<?>) o).isEmpty()) {
                List<ContextualClass> entities = (List<ContextualClass>) o;
                for (ContextualClass entity : entities) {
                    if (StringUtils.equalsIgnoreCase(uri, entity.getAbout())) {
                        LOG.debug(" Found matching entity for {}", entity.getAbout());
                        matchingEntity.add(entity);
                        break;
                    }
                }
            }
        }, entityFilter);

        // return Contextual Class if found or else null
        return matchingEntity.isEmpty() ? null : matchingEntity.get(0);
    }
}

