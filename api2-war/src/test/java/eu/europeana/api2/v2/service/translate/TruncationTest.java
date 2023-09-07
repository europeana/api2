package eu.europeana.api2.v2.service.translate;

import eu.europeana.api2.v2.service.translate.TranslationUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * These test run in ascending name order.
 * As the value for the limit increases for testing every scenario
 *
 * @author Sristhti Singh
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TruncationTest {

    private static Integer translationCharLimit = 100;
    private static Integer translationCharTolerance = 5;
    private static  final Integer increment = 20;
    private static  final Integer finalCharLimitValue = 330;

    private static List<String> valuesForTesting = Arrays.asList("Ciscar Mart Pau",
            "The psychosocial work environment in human service organizations is in many respects rewarding from the aspect of human interaction.",
            "Digital Comprehensive Summaries of Uppsala Dissertations from the Faculty of Medicine",
            "Landfills as anthropogenic landforms in \n urban environment from Neamt county testing translation");

    // the first value is always present in the results according to the test limits
    private static List<String> results = new ArrayList<>(Arrays.asList("Ciscar Mart Pau"));

    @Before
    public void setUp() {
        // translation char limit will keep on increasing for
        // testing until reaches final finalCharLimitValue
        if (translationCharLimit <= finalCharLimitValue) {
            translationCharLimit += increment;
        }
        if (translationCharLimit >= 150) {
            results.add("The psychosocial work environment in human service organizations is in many respects rewarding from the aspect of human interaction.");
        }
        if (translationCharLimit >= 235) {
            results.add("Digital Comprehensive Summaries of Uppsala Dissertations from the Faculty of Medicine");
        }
    }

    @Test
    public void test_A() {
        List<String> truncatedValues = TranslationUtils.truncate(valuesForTesting, translationCharLimit, translationCharTolerance);
        // A test results - the second value has a phrase hence the whole value should be added with "..." at the end
        // translationCharLimit = 120  ; length of second value - 132
        results.add("The psychosocial work environment in human service organizations is in many respects rewarding from the aspect of human interaction...");
        check(results, truncatedValues);
    }

    @Test
    public void test_B() {
        List<String> truncatedValues = TranslationUtils.truncate(valuesForTesting, translationCharLimit, translationCharTolerance);
        // B test results - the second value has a phrase hence the whole value should be added with "..." at the end
        // translationCharLimit = 140  ; length of second value - 132
        results.add("The psychosocial work environment in human service organizations is in many respects rewarding from the aspect of human interaction...");
        check(results, truncatedValues);
    }

    @Test
    public void test_C() {
        List<String> truncatedValues = TranslationUtils.truncate(valuesForTesting, translationCharLimit, translationCharTolerance);
        // C test results - the third value has no phrase so we abbreviate till the tolerance
        // translationCharLimit = 160 ; length of third value - 85
        results.add("Digital Comprehensive...");
        check(results, truncatedValues);
    }

    @Test
    public void test_D() {
        List<String> truncatedValues = TranslationUtils.truncate(valuesForTesting, translationCharLimit, translationCharTolerance);
        // D test results - the third value has no phrase so we abbreviate till the tolerance
        // translationCharLimit = 180 ; length of third value - 85
        results.add("Digital Comprehensive Summaries of...");
        check(results, truncatedValues);
    }

    @Test
    public void test_E() {
        List<String> truncatedValues = TranslationUtils.truncate(valuesForTesting, translationCharLimit, translationCharTolerance);
        // E test results - the third value has no phrase so we abbreviate till the tolerance
        // translationCharLimit = 200 ; length of third value - 85
        results.add("Digital Comprehensive Summaries of Uppsala Dissertations..");
        check(results, truncatedValues);
    }

    @Test
    public void test_F() {
        List<String> truncatedValues = TranslationUtils.truncate(valuesForTesting, translationCharLimit, translationCharTolerance);
        // F test results - the third value has no phrase so we abbreviate till the tolerance
        // translationCharLimit = 220 ; length of third value - 85
        results.add("Digital Comprehensive Summaries of Uppsala Dissertations from the Faculty...");
        check(results, truncatedValues);
    }

    @Test
    public void test_G() {
        List<String> truncatedValues = TranslationUtils.truncate(valuesForTesting, translationCharLimit, translationCharTolerance);
        // G test results - the fourth value has new line, so value until new line is added
        // translationCharLimit = 240 ; length of third value - 97
        results.add("Landfills as anthropogenic landforms in ...");
        check(results, truncatedValues);
    }

    @Test
    public void test_H() {
        List<String> truncatedValues = TranslationUtils.truncate(valuesForTesting, translationCharLimit, translationCharTolerance);
        // H test results - the fourth value has new line, so value until new line is added
        // translationCharLimit = 260 ; length of third value - 97
        results.add("Landfills as anthropogenic landforms in ...");
        check(results, truncatedValues);
    }

    @Test
    public void test_I() {
        List<String> truncatedValues = TranslationUtils.truncate(valuesForTesting, translationCharLimit, translationCharTolerance);
        // I test results - the fourth value has new line and the new line char is already under the limit
        // so now we will abbreviate the part after new line (after we have reached limit)
        // translationCharLimit = 280 ; length of third value - 97
        results.add("Landfills as anthropogenic landforms in \n urban environment...");
        check(results, truncatedValues);
    }

    @Test
    public void test_J() {
        List<String> truncatedValues = TranslationUtils.truncate(valuesForTesting, translationCharLimit, translationCharTolerance);
        // J test results - the fourth value has new line and the new line char is already under the limit
        // so now we will abbreviate the part after new line (after we have reached limit)
        // translationCharLimit = 300 ; length of third value - 97
        results.add("Landfills as anthropogenic landforms in \n urban environment from Neamt...");
        check(results, truncatedValues);
    }

    @Test
    public void test_K() {
        List<String> truncatedValues = TranslationUtils.truncate(valuesForTesting, translationCharLimit, translationCharTolerance);
        // K test results - the fourth value has new line and the new line char is already under the limit
        // so now we will abbreviate but also have reached almost the end of the string
        // hence complete value is included. NO truncation "..." at the end added
        // translationCharLimit = 300 ; length of third value - 97
        results.add("Landfills as anthropogenic landforms in \n urban environment from Neamt county testing translation");
        check(results, truncatedValues);
    }


    @Test
    public void test_L() {
        List<String> truncatedValues = TranslationUtils.truncate(valuesForTesting, translationCharLimit, translationCharTolerance);
        // all the values are under the limit, No truncation done
        // translationCharLimit = 340 ; length of total value - 329
        results.add("Landfills as anthropogenic landforms in \n urban environment from Neamt county testing translation");
        check(results, truncatedValues);
    }

    private void check(List<String> exceptedValues, List<String> actualValues) {
        Assert.assertNotNull(actualValues);
        Assert.assertEquals(exceptedValues.size(), results.size());

        // first value should always be present
        Assert.assertEquals(exceptedValues.get(0), actualValues.get(0));
        // check for second value
        Assert.assertEquals(exceptedValues.get(1), results.get(1));

        if (exceptedValues.size() == 3) {
            Assert.assertEquals(exceptedValues.get(2), results.get(2));
        }
        if (exceptedValues.size() == 4) {
            Assert.assertEquals(exceptedValues.get(3), results.get(3));
        }
    }
}
