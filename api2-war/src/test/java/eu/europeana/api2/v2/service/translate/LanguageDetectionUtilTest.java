package eu.europeana.api2.v2.service.translate;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.junit.Assert;

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

    private static final String DEF = "def";
    private static final String DE = "de";
    private static final String FR = "fr";

    private static final List<String> DEF_VALUES = Arrays.asList("Calamatta, Luigi (1801 - 1869)", "Leonardo da Vinci (1452 - 1519)",
            "graveur","voetbal", "http://data.europeana.eu/agent/base/6", "?");

    private static final List<String> DE_VALUES = Arrays.asList("TvB G 3674", "Paris Hilton", "voetbal", "http://data.europeana.eu/agent/base/6", "Paris Hilton");

    private static final List<String> FR_VALUES = Arrays.asList("graveur");


    @Before
    public void setup() {
        map.put(DEF, DEF_VALUES);
        map.put(DE, DE_VALUES);
    }

    @Test
    public void Test_getValueFromLanguageMap() {

    }

    @Test
    public void Test_removeLangTaggedValuesFromDef() {
        List<String> values = LanguageDetectionUtils.removeLangTaggedValuesFromDef(map);
        Assert.assertFalse(values.isEmpty());
        Assert.assertEquals( DEF_VALUES.size() - 1 ,values.size()); // will remove "voetbal" as it present in DE tag. But will not remove URI

        values.clear();
        // add another lang-tag value
        map.put(FR, FR_VALUES);
        values = LanguageDetectionUtils.removeLangTaggedValuesFromDef(map);
        Assert.assertFalse(values.isEmpty());
        Assert.assertEquals( DEF_VALUES.size() - 2 ,values.size()); // will remove "voetbal" and "graveur" as it present in DE and FR tag
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
    public void Test_mapHasOtherLanguagesThanDef() {
        Assert.assertTrue(LanguageDetectionUtils.mapHasOtherLanguagesThanDef(map.keySet()));
    }

    @Test
    public void Test_filterOutUris() {
        List<String> values = LanguageDetectionUtils.filterOutUris(DEF_VALUES);
        Assert.assertFalse(values.isEmpty());
        Assert.assertEquals(DEF_VALUES.size() - 1 , values.size());

        values.clear();
        values = LanguageDetectionUtils.filterOutUris(DE_VALUES);
        Assert.assertFalse(values.isEmpty());
        Assert.assertEquals(DE_VALUES.size() - 1 , values.size());
    }


}
