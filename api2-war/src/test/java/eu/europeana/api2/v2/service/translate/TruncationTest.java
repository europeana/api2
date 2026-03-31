package eu.europeana.api2.v2.service.translate;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.*;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static eu.europeana.api2.v2.service.translate.TranslationUtils.TRUNCATED_INDICATOR;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * These test run in ascending name order.
 * As the value for the limit increases for testing every scenario
 *
 * @author Sristhti Singh
 */
@Ignore
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TruncationTest {

    private static final Logger LOG = LogManager.getLogger(TruncationTest.class);

    private static Integer translationCharLimit = 100;
    private static Integer translationCharTolerance = 10;
    private static  final Integer increment = 20;
    private static  final Integer finalCharLimitValue = 330;

    private static List<String> valuesForTesting = Arrays.asList("Ciscar Mart Pau",
            "The psychosocial work environment in human service organizations is in many respects rewarding from the aspect of human interaction.",
            "Digital Comprehensive Summaries of Uppsala Dissertations from the Faculty of Medicine",
            "Landfills as anthropogenic landforms in \n urban environment from Neamt county testing translation");

    private static List<String> valuesForTesting_1 = Arrays.asList("Ciscar Mart Pau",
            "Struktura zbudowana przez człowieka mająca ściany i dach!! stojąca w określonym miejscu",
            "Se trata en realidad de un conjunto formado por varios edificios ?? el construido por López Sallaberry");

    private static final List<String> expectedResult = new ArrayList<>();

    @Before
    public void setUp() {
        // With each test the translation char limit increases until it reaches final finalCharLimitValue
        if (translationCharLimit <= finalCharLimitValue) {
            translationCharLimit += increment;
        }
        expectedResult.clear();

        // the first value is always present in the results according to the test limits
        expectedResult.add("Ciscar Mart Pau");
        if (translationCharLimit >= 150) {
            expectedResult.add("The psychosocial work environment in human service organizations is in many respects rewarding from the aspect of human interaction.");
        }
        if (translationCharLimit >= 235) {
            expectedResult.add("Digital Comprehensive Summaries of Uppsala Dissertations from the Faculty of Medicine");
        }
    }

    private void logTestInfo(List<String> valuesForTesting, int translationCharLimit, int translationCharTolerance) {
        LOG.info("Truncate {} values of total length {}, limit {}, tolerance {}", valuesForTesting.size(),
                valuesForTesting.stream().mapToInt(String::length).sum(), translationCharLimit, translationCharTolerance);
    }

    @Test
    public void test_A() {
        logTestInfo(valuesForTesting, translationCharLimit, translationCharTolerance);
        List<String> truncatedValues = TranslationUtils.truncate(valuesForTesting, translationCharLimit, translationCharTolerance);
        // A test results - the second value has a phrase hence the whole value should be added with "..." at the end
        // translationCharLimit = 120  ; length of second value - 132
        expectedResult.add("The psychosocial work environment in human service organizations is in many respects rewarding from the aspect" + TRUNCATED_INDICATOR);
        check(expectedResult, truncatedValues, translationCharLimit, translationCharTolerance);
    }

    @Test
    public void test_B() {
        logTestInfo(valuesForTesting, translationCharLimit, translationCharTolerance);
        List<String> truncatedValues = TranslationUtils.truncate(valuesForTesting, translationCharLimit, translationCharTolerance);
        // B test results - the second value has a phrase hence the whole value should be added with "..." at the end
        // translationCharLimit = 140  ; length of second value - 132
        expectedResult.add("The psychosocial work environment in human service organizations is in many respects rewarding from the aspect of human interaction" + TRUNCATED_INDICATOR);
        check(expectedResult, truncatedValues, translationCharLimit, translationCharTolerance);
    }

    @Test
    public void test_C() {
        logTestInfo(valuesForTesting, translationCharLimit, translationCharTolerance);
        List<String> truncatedValues = TranslationUtils.truncate(valuesForTesting, translationCharLimit, translationCharTolerance);
        // C test results - the third value has no phrase so we abbreviate till the tolerance
        // translationCharLimit = 160 ; length of third value - 85
        expectedResult.add("Digital Comprehensive" + TRUNCATED_INDICATOR);
        check(expectedResult, truncatedValues, translationCharLimit, translationCharTolerance);
    }

    @Test
    public void test_D() {
        logTestInfo(valuesForTesting, translationCharLimit, translationCharTolerance);
        List<String> truncatedValues = TranslationUtils.truncate(valuesForTesting, translationCharLimit, translationCharTolerance);
        // D test results - the third value has no phrase so we abbreviate till the tolerance
        // translationCharLimit = 180 ; length of third value - 85
        expectedResult.add("Digital Comprehensive Summaries of" + TRUNCATED_INDICATOR);
        check(expectedResult, truncatedValues, translationCharLimit, translationCharTolerance);
    }

    @Test
    public void test_E() {
        logTestInfo(valuesForTesting, translationCharLimit, translationCharTolerance);
        List<String> truncatedValues = TranslationUtils.truncate(valuesForTesting, translationCharLimit, translationCharTolerance);
        // E test results - the third value has no phrase so we abbreviate till the tolerance
        // translationCharLimit = 200 ; length of third value - 85
        expectedResult.add("Digital Comprehensive Summaries of Uppsala Dissertations" + TRUNCATED_INDICATOR);
        check(expectedResult, truncatedValues, translationCharLimit, translationCharTolerance);
    }

    @Test
    public void test_F() {
        logTestInfo(valuesForTesting, translationCharLimit, translationCharTolerance);
        List<String> truncatedValues = TranslationUtils.truncate(valuesForTesting, translationCharLimit, translationCharTolerance);
        // F test results - the third value has no phrase so we abbreviate till the tolerance
        // translationCharLimit = 220 ; length of third value - 85
        expectedResult.add("Digital Comprehensive Summaries of Uppsala Dissertations from the Faculty" + TRUNCATED_INDICATOR);
        check(expectedResult, truncatedValues, translationCharLimit, translationCharTolerance);
    }

    @Test
    public void test_G() {
        logTestInfo(valuesForTesting, translationCharLimit, translationCharTolerance);
        List<String> truncatedValues = TranslationUtils.truncate(valuesForTesting, translationCharLimit, translationCharTolerance);
        // G test results - the fourth value has new line, but we don't consider that
        // translationCharLimit = 240 ; length of third value - 97
        expectedResult.add("Landfills" + TRUNCATED_INDICATOR);
        check(expectedResult, truncatedValues, translationCharLimit, translationCharTolerance);
    }

    @Test
    public void test_H() {
        logTestInfo(valuesForTesting, translationCharLimit, translationCharTolerance);
        List<String> truncatedValues = TranslationUtils.truncate(valuesForTesting, translationCharLimit, translationCharTolerance);
        // H test results - the fourth value has new line
        // translationCharLimit = 260 ; length of third value - 97
        expectedResult.add("Landfills as anthropogenic landforms" + TRUNCATED_INDICATOR);
        check(expectedResult, truncatedValues, translationCharLimit, translationCharTolerance);
    }

    @Test
    public void test_I() {
        logTestInfo(valuesForTesting, translationCharLimit, translationCharTolerance);
        List<String> truncatedValues = TranslationUtils.truncate(valuesForTesting, translationCharLimit, translationCharTolerance);
        // I test results - this is an edge case as the cutoff point is a trailing space, so we don't include anything that comes after it
        // translationCharLimit = 280 ; length of third value - 97
        expectedResult.add("Landfills as anthropogenic landforms in \n urban" + TRUNCATED_INDICATOR);
        check(expectedResult, truncatedValues, translationCharLimit, translationCharTolerance);
    }

    @Test
    public void test_J() {
        logTestInfo(valuesForTesting, translationCharLimit, translationCharTolerance);
        List<String> truncatedValues = TranslationUtils.truncate(valuesForTesting, translationCharLimit, translationCharTolerance);
        // J test results - the fourth value has new line and the new line char is already under the limit
        // so now we will abbreviate the part after new line (after we have reached limit)
        // translationCharLimit = 300 ; length of third value - 97
        expectedResult.add("Landfills as anthropogenic landforms in \n urban environment from Neamt" + TRUNCATED_INDICATOR);
        check(expectedResult, truncatedValues, translationCharLimit, translationCharTolerance);
    }

    @Test
    public void test_K() {
        logTestInfo(valuesForTesting, translationCharLimit, translationCharTolerance);
        List<String> truncatedValues = TranslationUtils.truncate(valuesForTesting, translationCharLimit, translationCharTolerance);
        // K test results - the fourth value has new line and the new line char is already under the limit
        // so now we will abbreviate but also have reached almost the end of the string
        // hence complete value is included. NO truncation "..." at the end added
        // translationCharLimit = 300 ; length of third value - 97
        expectedResult.add("Landfills as anthropogenic landforms in \n urban environment from Neamt county testing translation");
        check(expectedResult, truncatedValues, translationCharLimit, translationCharTolerance);
    }


    @Test
    public void test_L() {
        logTestInfo(valuesForTesting, translationCharLimit, translationCharTolerance);
        List<String> truncatedValues = TranslationUtils.truncate(valuesForTesting, translationCharLimit, translationCharTolerance);
        // all the values are under the limit, No truncation done
        // translationCharLimit = 340 ; length of total value - 329
        expectedResult.add("Landfills as anthropogenic landforms in \n urban environment from Neamt county testing translation");
        check(expectedResult, truncatedValues, translationCharLimit, translationCharTolerance);
    }

    // Testing for other phrases like ? or !
    @Test
    public void test_M() {
        List<String> truncatedValues = TranslationUtils.truncate(valuesForTesting_1, 70 , 10);
        // clear what we have added in set up for previous values
        expectedResult.clear();
        expectedResult.add("Ciscar Mart Pau");
        expectedResult.add("Struktura zbudowana przez człowieka mająca ściany i dach" + TRUNCATED_INDICATOR);
        check(expectedResult, truncatedValues, translationCharLimit, translationCharTolerance); // has phrase "!"

        // increase limit to check further
        truncatedValues.clear();
        expectedResult.clear();
        expectedResult.add("Ciscar Mart Pau");
        expectedResult.add("Struktura zbudowana przez człowieka mająca ściany i dach!! stojąca w określonym miejscu");
        expectedResult.add(TRUNCATED_INDICATOR); // to indicate we cut off the last value
        truncatedValues = TranslationUtils.truncate(valuesForTesting_1, 100, 10);
        check(expectedResult, truncatedValues, translationCharLimit, translationCharTolerance); // we have reached the end of the second value


        // increase limit to check further
        truncatedValues.clear();
        expectedResult.clear();
        expectedResult.add("Ciscar Mart Pau");
        expectedResult.add("Struktura zbudowana przez człowieka mająca ściany i dach!! stojąca w określonym miejscu");
        expectedResult.add("Se trata en realidad de un conjunto formado por varios edificios " + TRUNCATED_INDICATOR);
        truncatedValues = TranslationUtils.truncate(valuesForTesting_1, 165, translationCharTolerance);
        check(expectedResult, truncatedValues, translationCharLimit, translationCharTolerance); // has phrase "?"

        // increase limit to check further
        truncatedValues.clear();
        expectedResult.clear();
        expectedResult.add("Ciscar Mart Pau");
        expectedResult.add("Struktura zbudowana przez człowieka mająca ściany i dach!! stojąca w określonym miejscu");
        expectedResult.add("Se trata en realidad de un conjunto formado por varios edificios ?? el construido" + TRUNCATED_INDICATOR);
        truncatedValues = TranslationUtils.truncate(valuesForTesting_1, 180, translationCharTolerance);
        check(expectedResult, truncatedValues, translationCharLimit, translationCharTolerance); // no phrase have abbreviated
    }

    /**
     * Test breaking off words for 1 string
     */
    @Test
    public void test__1String() {
        String test = "12345 7890 23456"; // size 16

        // keep entire string1
        List<String> result = TranslationUtils.truncate(List.of(test), 17, 5);
        assertEquals(test, result.get(0));

        // keep entire string2
        result = TranslationUtils.truncate(List.of(test), 16, 5);
        assertEquals(test, result.get(0));

        // keep entire string3
        result = TranslationUtils.truncate(List.of(test), 15, 5);
        assertEquals(test, result.get(0));

        // hard cut off at 13+2
        result = TranslationUtils.truncate(List.of(test), 13, 2);
        assertEquals("12345 7890 2345" + "...", result.get(0));

        // hard cut off at 13+0
        result = TranslationUtils.truncate(List.of(test), 13, 0);
        assertEquals("12345 7890 23" + "...", result.get(0));

        // soft cut off at 11 because end of word (and trim space)
        result = TranslationUtils.truncate(List.of(test), 11, 2);
        assertEquals("12345 7890" + "...", result.get(0));

        // soft cut off at 10 because end of word
        result = TranslationUtils.truncate(List.of(test), 10, 2);
        assertEquals("12345 7890" + "...", result.get(0));

        // soft cut off at 10 (9+1) because that's the first space
        result = TranslationUtils.truncate(List.of(test), 9, 2);
        assertEquals("12345 7890" + "...", result.get(0));
    }

    /**
     * Test breaking off sentences for 1 string
     */
    @Test
    public void test__1StringSentence() {
        // break off at end of sentence
        String test = "12345 7890 2345. 890"; // length 20
        List<String> result = TranslationUtils.truncate(List.of(test),9, 9);
        assertEquals("12345 7890 2345" + "...", result.get(0));

        // break off at end of word because end of sentence is outside tolerance
        result = TranslationUtils.truncate(List.of(test), 9, 3);
        assertEquals("12345 7890" + "...", result.get(0));
    }

    private void check(List<String> expectedValues, List<String> actualValues, int limit, int tolerance) {
        assertTrue(actualValues.stream().mapToInt(String::length).sum() <= limit + tolerance + TRUNCATED_INDICATOR.length());

        Assert.assertNotNull(actualValues);
        assertEquals(expectedValues.size(), actualValues.size());

        // first value should always be present
        assertEquals(expectedValues.get(0), actualValues.get(0));
        // check for second value
        assertEquals(expectedValues.get(1), actualValues.get(1));

        if (expectedValues.size() == 3) {
            assertEquals(expectedValues.get(2), actualValues.get(2));
        }
        if (expectedValues.size() == 4) {
            assertEquals(expectedValues.get(3), actualValues.get(3));
        }
    }
}
