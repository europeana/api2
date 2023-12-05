package eu.europeana.api2.v2.service.translate;

import eu.europeana.api.commons.error.EuropeanaApiException;
import eu.europeana.api.translation.client.service.MetadataLangDetectionService;
import eu.europeana.api.translation.client.service.MetadataTranslationService;
import eu.europeana.api2.v2.model.translate.Language;
import eu.europeana.corelib.definitions.edm.beans.FullBean;
import eu.europeana.corelib.definitions.edm.entity.Proxy;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.util.ReflectionUtils;

import javax.annotation.Resource;
import java.lang.reflect.Field;
import java.util.*;

import static eu.europeana.api.translation.client.service.BaseService.*;

@Service
public class RecordTranslations {

    private static final Logger LOG = LogManager.getLogger(RecordTranslations.class);

    @Resource
    private MetadataTranslationService metadataTranslationService;

    @Resource
    MetadataLangDetectionService metadataLangDetectionService;


    public FullBean translate(FullBean bean, String targetLanguage) throws EuropeanaApiException {
        Proxy europeanaProxy = getEuropeanaProxy(bean.getProxies(), bean.getAbout());

        // check if the record was translated during ingestion
        List<Boolean> translatedDuringIngestion = new ArrayList<>();
        ReflectionUtils.doWithFields(europeanaProxy.getClass(), field-> {
            translatedDuringIngestion.add(pivotLanguageTaggedValueIsPresent(europeanaProxy, field));
        }, proxyFieldFilter);

        if (translatedDuringIngestion.contains(Boolean.TRUE)) {
            LOG.debug("Record was translated during the ingestion.. ");
            if (StringUtils.equals(targetLanguage, Language.ENGLISH)){
                return bean; // do nothing
            } else {
                // call translation workflow
                return metadataTranslationService.translationWorkflow(bean, targetLanguage);
            }
        }
        else {
            // if not translated during ingestion - apply detection + translation
           return metadataTranslationService.translationWorkflow(metadataLangDetectionService.detectLanguageForProxy(bean), targetLanguage);
        }
    }

    /**
     * Returns true if Pivot language tagged ("en") value is present for the field.
     * @param proxy
     * @param field
     * @return
     */
    private boolean pivotLanguageTaggedValueIsPresent(Proxy proxy, Field field) {
        HashMap<String, List<String>> origFieldData = (HashMap<String, List<String>>) getValueOfTheField(proxy, false).apply(field.getName());
        if (origFieldData != null && !origFieldData.isEmpty() && origFieldData.containsKey(Language.ENGLISH)) {
            return true;
        }
        return false;
    }
}
