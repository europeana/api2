package eu.europeana.api2.v2.utils;

import eu.europeana.api2.v2.model.enums.Profile;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

@Ignore
public class ControllerUtilsTest {

    private static String LANG = "lang";

    private static String QUERY_1 = "profile=translate&wskey=test&rows=2&lang=pt,en,cs";
    private static String QUERY_2 = "&lang=pt,en,cs&wskey=test&rows=2&profile=translate,minimal,hits&profile=debug";
    private static String QUERY_3 = "profile=translate&wskey=test&rows=2&&lang=pt,en,cs&profile=minimal+hits&profile=debug";
    private static String QUERY_4 = "profile=test&wskey=happy&&lang=pt&page=1&view=list&query=hola&qf=contentTier%253A%25281%2520OR%25202%2520OR%25203%2520OR%25204%2529&profile=minimal&profile=minimal%252Ctranslate&rows=24&start=1&lang=es&q.source=es&q.target=en";

    private static  final Set<Profile> profiles = new HashSet<>();
    private static final String profileAdded = "profile=minimal,hits,debug";

    @Before
    public void setup() {
        profiles.add(Profile.DEBUG);
        profiles.add(Profile.HITS);
        profiles.add(Profile.MINIMAL);
        profiles.add(Profile.TRANSLATE);
    }

    // profile param tests

    @Test
    public void test_1() {
        check(ControllerUtils.getQueryStringWithoutTranslate(QUERY_1, profiles), "profile=translate");
    }

    @Test
    public void test_2() {
        check(ControllerUtils.getQueryStringWithoutTranslate(QUERY_2, profiles), "profile=translate,minimal,hits&profile=debug");
    }

    @Test
    public void test_3() {
        String result = ControllerUtils.getQueryStringWithoutTranslate(QUERY_3, profiles);
        check(result, "profile=translate");
        check(result, "profile=minimal+hits&profile=debug");
    }

    @Test
    public void test_4() {
        String result = ControllerUtils.getQueryStringWithoutTranslate(QUERY_4, profiles);
        check(result, "profile=test");
        check(result, "profile=minimal&profile=minimal%252Ctranslate");
    }

    // lang param test
    @Test
    public void test_5() {
        String result = ControllerUtils.removeLangFromRequest(QUERY_1);
        Assert.assertFalse(result.contains(LANG));
    }
    @Test
    public void test_6() {
        String result = ControllerUtils.removeLangFromRequest(QUERY_2);
        Assert.assertFalse(result.contains(LANG));
    }

    @Test
    public void test_7() {
        String result = ControllerUtils.removeLangFromRequest(QUERY_3);
        Assert.assertFalse(result.contains(LANG));
    }


    @Test
    public void test_8() {
        String result = ControllerUtils.removeLangFromRequest(QUERY_4);
        Assert.assertFalse(result.contains(LANG));
    }


    private static  void check(String result, String notExpected) {
        Assert.assertTrue(result.contains(profileAdded));
        Assert.assertFalse(result.contains(notExpected));
    }
}
