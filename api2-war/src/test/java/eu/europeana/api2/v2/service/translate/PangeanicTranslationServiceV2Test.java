package eu.europeana.api2.v2.service.translate;

import eu.europeana.api2.v2.exceptions.TranslationException;
import eu.europeana.api2.v2.model.translate.Language;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.anyString;

import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PangeanicTranslationServiceV2Test {

    private PangeanicTranslationServiceV2 pangeanicService;

    List<String> singleLangText;
    List<String> multipleLangText;
    List<String> duplicateText;
    List<String> multipleLangTextResponse;
    List<String> singleLangTextResponse;
    List<String> duplicateTextResponse;

    @Before
    public void setup() throws TranslationException {
        pangeanicService = Mockito.spy(PangeanicTranslationServiceV2.class);

        // multiple language texts
        multipleLangText = new ArrayList<>(Arrays.asList("Isto é uma frase para teste",
                "Traduit aussi cette phrase",
                "2014",
                "एहि वाक्यक सेहो अनुवाद करू"));

        // single lang "es" text
        singleLangText = new ArrayList<>(Arrays.asList("Patrimonio urbano",
                "An?bal GONZ?LEZ ?LVAREZ-OSSORIO",
                "Francisco del VILLAR CARMONA",
                "Jos? L?PEZ SALLABERRY"));

        duplicateText = new ArrayList<>(Arrays.asList("Chine",
                "Notice du catalogue",
                "Chine"));

        // responses
        multipleLangTextResponse = new ArrayList<>(Arrays.asList("This is a test sentence",
                "Also translates this sentence",
                "2014",
                "एहि वाक्यक सेहो अनुवाद करू"));

        singleLangTextResponse = new ArrayList<>(Arrays.asList("Urban Heritage",
                "An? bal GONZ? LEZ? LVAREZ-OSSORIO",
                "Francisco del VILLAR CARMON",
                "Jos? L? PEZ SALLABERRY"));

        // responses
        duplicateTextResponse = new ArrayList<>(Arrays.asList("China",
                "Catalogue notice",
                "China"));

        Mockito.when(pangeanicService.translate(eq(multipleLangText), anyString(), (Language) eq(null ))).thenReturn(multipleLangTextResponse);
        Mockito.when(pangeanicService.translate(eq(singleLangText), anyString(), eq(Language.ES))).thenReturn(singleLangTextResponse);
        Mockito.when(pangeanicService.translate(eq(singleLangText), anyString(), eq("es"))).thenReturn(singleLangTextResponse);
        Mockito.when(pangeanicService.translate(eq(duplicateText), anyString(), eq("fr"))).thenReturn(duplicateTextResponse);
        Mockito.when(pangeanicService.translate(eq(duplicateText), anyString(), (Language) eq(null ))).thenReturn(duplicateTextResponse);
    }

    @Test
    public void testTranslateMultipleLangDetect() throws TranslationException {
        List<String> translations = pangeanicService.translate(multipleLangText,  "en", (Language) null);
        Assert.assertNotNull(translations);
        Assert.assertEquals(multipleLangTextResponse, translations);
    }

    @Test
    public void testTranslateSingleLangDetect() throws TranslationException {
           List<String> translations = pangeanicService.translate(singleLangText,  "en", Language.ES);
        Assert.assertNotNull(translations);
        Assert.assertEquals(singleLangTextResponse, translations);
    }

    @Test
    public void testTranslateWithSource() throws TranslationException {
        List<String> translations = pangeanicService.translate(singleLangText,  "en", "es");
        Assert.assertNotNull(translations);
        Assert.assertEquals(singleLangTextResponse, translations);
    }

    @Test
    public void testTranslateWithSourceAndDuplicates() throws TranslationException {
        List<String> translations = pangeanicService.translate(duplicateText,  "en", "fr");
        Assert.assertNotNull(translations);
        Assert.assertEquals(duplicateTextResponse, translations);
    }

    @Test
    public void testTranslateWithoutSourceAndDuplicates() throws TranslationException {
        List<String> translations = pangeanicService.translate(duplicateText,  "en", (Language) null);
        Assert.assertNotNull(translations);
        Assert.assertEquals(duplicateTextResponse, translations);
    }

}
