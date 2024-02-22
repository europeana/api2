package eu.europeana.api2.v2.service.translate;

import eu.europeana.api.translation.definitions.language.Language;
import eu.europeana.api2.v2.model.translate.LanguageValueFieldMap;
import eu.europeana.api2.v2.utils.MockBeanConstants;
import eu.europeana.api2.v2.utils.MockFullBean;
import eu.europeana.api2.v2.utils.MockSearchBeanResults;
import eu.europeana.corelib.definitions.edm.beans.BriefBean;
import eu.europeana.corelib.definitions.edm.beans.FullBean;
import eu.europeana.corelib.definitions.edm.entity.Agent;
import eu.europeana.corelib.solr.bean.impl.FullBeanImpl;
import eu.europeana.corelib.solr.entity.EuropeanaAggregationImpl;
import org.apache.commons.lang.StringUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.*;

/**
 * Test the LanguageDetectionUtils
 *
 * @author Srishti singh
 * Created 7 Feb 2024
 */
@RunWith(MockitoJUnitRunner.class)
public class LanguageDetectionUtilTest {

    private static final Map<String, List<String>> map = new HashMap<>();
    private static final FullBean bean = MockFullBean.mock();
    private static final String DEF = "def";
    private static final String DE = "de";
    private static final String FR = "fr";
    private static final String EN = "en"; // case sensitive values

    private static final String DEF_PREF_LABEL = "Cultural Heritage & History";

    private static final List<String> DEF_VALUES = Arrays.asList("Calamatta, Luigi (1801 - 1869)", "Leonardo da Vinci (1452 - 1519)",
            "graveur", "voetbal", "http://data.europeana.eu/agent/base/6", "?");

    private static final List<String> DE_VALUES = Arrays.asList("TvB G 3674", "Paris Hilton", "voetbal", "http://data.europeana.eu/agent/base/6", "Paris Hilton");

    private static final List<String> FR_VALUES = Arrays.asList("graveur");
    private static final List<String> EN_VALUES = Arrays.asList("GrAveur", "paris hilton", "VoetBal", "calamatta, luigi (1801 - 1869)");


    @Before
    public void setup() {
        map.clear();
        map.put(DEF, DEF_VALUES);
        map.put(DE, DE_VALUES);
    }

    // test valid edm lang
    @Test
    public void Test_getEdmLanguage_Record_1() {
        List<Language> language = LanguageDetectionUtils.getEdmLanguage(bean, false);
        Assert.assertFalse(language.isEmpty());
        Assert.assertEquals(language.get(0),Language.NL);
    }

    // test valid edm lang
    @Test
    public void Test_getEdmLanguage_Search_1() {
        BriefBean bean =  MockSearchBeanResults.mockForLang("de");
        List<Language> language = LanguageDetectionUtils.getEdmLanguage(bean, true);
        Assert.assertFalse(language.isEmpty());
        Assert.assertEquals(language.get(0),Language.DE);
    }

    // test region codes
    @Test
    public void Test_getEdmLanguage_Record_2() {
        FullBeanImpl bean = new FullBeanImpl();
        bean.setAbout(MockBeanConstants.ABOUT);
        bean.setLanguage(new String[]{"en-GB"});
        EuropeanaAggregationImpl europeanaAggregation = new EuropeanaAggregationImpl();
        europeanaAggregation.setAbout(MockBeanConstants.EUROPEANA_AGG_ABOUT);
        europeanaAggregation.setEdmLanguage(new HashMap<>());
        europeanaAggregation.getEdmLanguage().put(MockBeanConstants.DEF, new ArrayList<>());
        europeanaAggregation.getEdmLanguage().get(MockBeanConstants.DEF).add("en-GB");
        bean.setEuropeanaAggregation(europeanaAggregation);

        List<Language> language = LanguageDetectionUtils.getEdmLanguage(bean, false);
        Assert.assertFalse(language.isEmpty());
        Assert.assertEquals(language.get(0),Language.EN);
    }

    // test Invalid edm lang
    @Test
    public void Test_getEdmLanguage_Search_2() {
        BriefBean bean =  MockSearchBeanResults.mockForLang("mul");
        List<Language> language = LanguageDetectionUtils.getEdmLanguage(bean, true);
        Assert.assertTrue(language.isEmpty());
    }

    // test invalid edm lang
    @Test
    public void Test_getEdmLanguage_Record_3() {
        FullBeanImpl bean = new FullBeanImpl();
        bean.setAbout(MockBeanConstants.ABOUT);
        bean.setLanguage(new String[]{"mul"});
        EuropeanaAggregationImpl europeanaAggregation = new EuropeanaAggregationImpl();
        europeanaAggregation.setAbout(MockBeanConstants.EUROPEANA_AGG_ABOUT);
        europeanaAggregation.setEdmLanguage(new HashMap<>());
        europeanaAggregation.getEdmLanguage().put(MockBeanConstants.DEF, new ArrayList<>());
        europeanaAggregation.getEdmLanguage().get(MockBeanConstants.DEF).add("mul");
        bean.setEuropeanaAggregation(europeanaAggregation);

        List<Language> language = LanguageDetectionUtils.getEdmLanguage(bean, false);
        Assert.assertTrue(language.isEmpty());
    }

    // test region codes edm lang
    @Test
    public void Test_getEdmLanguage_Search_3() {
        BriefBean bean =  MockSearchBeanResults.mockForLang("de-NL");
        List<Language> language = LanguageDetectionUtils.getEdmLanguage(bean, true);
        Assert.assertFalse(language.isEmpty());
        Assert.assertEquals(language.get(0),Language.DE);

    }


    @Test
    public void Test_getValueFromLanguageMap_Record() {
        map.put(FR, FR_VALUES);
        LanguageValueFieldMap result = LanguageDetectionUtils.getValueFromLanguageMap(map, "dcDescription", bean, false);
        // will remove graveur","voetbal" as they are present in lang-tagged values
        // will resolve http://data.europeana.eu/agent/base/6 from bean but there are already lang-tagged values present in preflabel
        // so nothing is picked
        Assert.assertFalse(result.isEmpty());
        Assert.assertEquals(3, result.get(DEF).size());


        // now let's remove lang-tagged preflabel from agent and add only def preflabel
        Agent contextualEntity = bean.getAgents().stream().filter(agent -> StringUtils.equals(agent.getAbout(), "http://data.europeana.eu/agent/base/6"))
                .findFirst().get();
        contextualEntity.getPrefLabel().clear();
        contextualEntity.setPrefLabel(new HashMap<>());
        contextualEntity.getPrefLabel().put(MockBeanConstants.DEF, new ArrayList<>());
        contextualEntity.getPrefLabel().get(MockBeanConstants.DEF).add(DEF_PREF_LABEL);

        result.clear();
        result = LanguageDetectionUtils.getValueFromLanguageMap(map, "dcDescription", bean, false);
        // will remove graveur","voetbal" as they are present in lang-tagged values
        // will resolve http://data.europeana.eu/agent/base/6 from bean and will pick the def tagged value
        Assert.assertFalse(result.isEmpty());
        Assert.assertEquals(4, result.get(DEF).size());
        Assert.assertTrue(result.get(DEF).contains(DEF_PREF_LABEL));


        // now let's remove other lang-tagged values from map
        result.clear();
        map.remove(DE);
        map.remove(FR);
        result = LanguageDetectionUtils.getValueFromLanguageMap(map, "dcDescription", bean, false);
        // will resolve http://data.europeana.eu/agent/base/6 from bean and will pick the def tagged value
        Assert.assertFalse(result.isEmpty());
        Assert.assertEquals(DEF_VALUES.size() , result.get(DEF).size());
    }

    @Test
    public void Test_removeLangTaggedValuesFromDef() {
        List<String> values = LanguageDetectionUtils.removeLangTaggedValuesFromDef(map);
        Assert.assertFalse(values.isEmpty());
        Assert.assertEquals(DEF_VALUES.size() - 1, values.size()); // will remove "voetbal" as it present in DE tag. But will not remove URI

        values.clear();
        // add another lang-tag value
        map.put(FR, FR_VALUES);
        values = LanguageDetectionUtils.removeLangTaggedValuesFromDef(map);
        Assert.assertFalse(values.isEmpty());
        Assert.assertEquals(DEF_VALUES.size() - 2, values.size()); // will remove "voetbal" and "graveur" as it present in DE and FR tag
    }

    @Test
    public void Test_removeLangTaggedValuesFromDef_CaseSensistive() {
        map.put(EN, EN_VALUES);
        List<String> values = LanguageDetectionUtils.removeLangTaggedValuesFromDef(map);
        Assert.assertFalse(values.isEmpty());
    }

    @Test
    public void Test_onlyNulls() {
        List<String> onlyNullList = new ArrayList<>();
        onlyNullList.add(null);
        onlyNullList.add(null);
        Assert.assertTrue(LanguageDetectionUtils.onlyNulls(onlyNullList));

        onlyNullList.add("testing");
        Assert.assertFalse(LanguageDetectionUtils.onlyNulls(onlyNullList));
    }

    @Test
    public void Test_mapHasOtherLanguagesThanDef_1() {
        Assert.assertTrue(LanguageDetectionUtils.mapHasOtherLanguagesThanDef(map.keySet()));
    }

    @Test
    public void Test_mapHasOtherLanguagesThanDef_2() {
        // test if there is no other lang-tagged value in map
        map.remove(DE);
        Assert.assertFalse(LanguageDetectionUtils.mapHasOtherLanguagesThanDef(map.keySet()));
    }

    @Test
    public void Test_filterOutUris() {
        List<String> values = LanguageDetectionUtils.filterOutUris(DEF_VALUES);
        Assert.assertFalse(values.isEmpty());
        Assert.assertEquals(DEF_VALUES.size() - 1, values.size());

        values.clear();
        values = LanguageDetectionUtils.filterOutUris(DE_VALUES);
        Assert.assertFalse(values.isEmpty());
        Assert.assertEquals(DE_VALUES.size() - 1, values.size());

        // test empty
        values.clear();
        values = LanguageDetectionUtils.filterOutUris(new ArrayList<>());
        Assert.assertTrue(values.isEmpty());
    }
}
