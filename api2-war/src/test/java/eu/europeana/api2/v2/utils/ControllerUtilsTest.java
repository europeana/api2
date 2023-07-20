package eu.europeana.api2.v2.utils;

import eu.europeana.api2.v2.model.enums.Profile;
import org.junit.*;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Ignore
public class ControllerUtilsTest {

    private static String LANG = "lang";

    private static String QUERY_1 = "profile=translate&wskey=test&rows=2&lang=pt,en,cs";
    private static String QUERY_2 = "&lang=pt,en,cs&wskey=test&rows=2&profile=translate,minimal,hits&profile=debug";
    private static String QUERY_3 = "profile=translate&wskey=test&rows=2&&lang=pt,en,cs&profile=minimal+hits&profile=debug";
    private static String QUERY_4 = "profile=test&wskey=happy&&lang=pt&page=1&view=list&query=hola&qf=contentTier%253A%25281%2520OR%25202%2520OR%25203%2520OR%25204%2529&profile=minimal&profile=minimal%252Ctranslate&rows=24&start=1&lang=es&q.source=es&q.target=en";

    private static Set<Profile> profiles = new HashSet<>();
    private static  String profileAdded ;

    @Before
    public void setup() {
        profiles.add(Profile.DEBUG);
        profiles.add(Profile.HITS);
        profiles.add(Profile.MINIMAL);
        profileAdded = profiles.stream().map(Profile::getName).collect(Collectors.joining(","));

        // add translate afterwards
        profiles.add(Profile.TRANSLATE);

    }

    @After
    public void clear() {
        profiles = new HashSet<>();
    }

    // profile and lang param tests
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

    // url cleaner test
    @Test
    public void test_5() {
        checkUrl(ControllerUtils.removeRequestMapping("/api/v2/record/9200396/BibliographicResource_3000118436341.json"));
    }

    @Test
    public void test_6() {
        checkUrl(ControllerUtils.removeRequestMapping("/record/9200396/BibliographicResource_3000118436341.json"));
    }

    @Test
    public void test_7() {
        checkUrl(ControllerUtils.removeRequestMapping("/v2/record/9200396/BibliographicResource_3000118436341.json"));
    }

    @Test
    public void test_8() {
        checkUrl(ControllerUtils.removeRequestMapping("/record/v2/9200396/BibliographicResource_3000118436341.json"));
    }

    @Test
    public void test_9() {
        checkUrl(ControllerUtils.removeRequestMapping("/record/9200396/BibliographicResource_3000118436341.json"));
    }

    @Test
    public void test_10() {
        checkUrl(ControllerUtils.removeRequestMapping("/record/search.json"));
    }

    @Test
    public void test_11() {
        checkUrl(ControllerUtils.removeRequestMapping("/api/v2/record/search.json"));
    }

    @Test
    public void test_12() {
        checkUrl(ControllerUtils.removeRequestMapping("/record/v2/search.json"));
    }

    private static  void checkUrl(String result) {
        Assert.assertNotNull(result);
        Assert.assertFalse(result.contains("/api"));
        Assert.assertFalse(result.contains("/v2"));
        Assert.assertFalse(result.contains("/record"));
        Assert.assertFalse(result.startsWith("/"));

    }

    private static  void check(String result, String notExpected) {
        Assert.assertTrue(result.contains(profileAdded));
        Assert.assertFalse(result.contains(notExpected));
        Assert.assertFalse(result.contains(LANG));
    }
}
