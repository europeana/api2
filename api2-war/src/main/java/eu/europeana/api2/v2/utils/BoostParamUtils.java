package eu.europeana.api2.v2.utils;

import eu.europeana.corelib.edm.exceptions.SolrQueryException;
import eu.europeana.corelib.web.exception.ProblemType;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BoostParamUtils {

    private static String curlyBracesPattern = "[{+|}+]";
    private static String  boostParamFieldPattern= "[a-z]{2,}=";

    private BoostParamUtils(){

    }

    /**
     * Validates : if the value starts with '{!edismax' and ends with '}' and
     * there is only one boost parameter value.
     *
     * @param boostParam
     * @throws SolrQueryException
     */
    public static boolean validateBoostParam(String boostParam) throws SolrQueryException {
        if (StringUtils.isNotEmpty(boostParam)) {
            // check if the param starts with '{!edismax' and ends with '}'
            String normalised = StringUtils.normalizeSpace(boostParam);
            if (!(StringUtils.startsWith(normalised, "{!edismax") && StringUtils.endsWith(normalised, "}"))) {
                throw new SolrQueryException(ProblemType.BOOST_PARAM_INVALID_FORMAT);
            }

            // check if there are any curly brackets in between
            Pattern pattern = Pattern.compile(curlyBracesPattern);
            Matcher matcher = pattern.matcher(removeStartAndEndBraces(boostParam));
            if (matcher.find()) {
                throw new SolrQueryException(ProblemType.BOOST_PARAM_INVALID_VALUE);
            }
        }
        return true;
    }

    /**
     * Gets the Field-value parameter map.
     * Creates a dynamic pattern every time. For example : (?<=ps=)(.*)(?=tie=)  OR for the last string (?<=qf=)(.*)
     * For ex, if query string is : {!edismax ps=2 tie=0.1 qf="title^weight proxy_dc_creator^weight"}
     * then returns : {tie=0.1, ps=2, qf=title^weight proxy_dc_creator^weight}
     *
     * @param paramValue
     * @return
     */
    public static Map<String, String> getDismaxQueryMap(String paramValue) throws SolrQueryException {
        String cleanParamValue = normaliseAndClean(paramValue);
        Map<String, String> dismaxQuerymap = new HashMap<>();
        // Get the fields first
        List<String> fieldsProvided = getFieldsFromString(cleanParamValue);
        if (!fieldsProvided.isEmpty()) {
            // get the value provided for the fields
            for (int i = 0; i < fieldsProvided.size(); i++) {
                String dynamicPattern;
                // if the last field, create a different pattern
                if (i == fieldsProvided.size() - 1) {
                    dynamicPattern = "(?<=" + fieldsProvided.get(i) + ")(.*)";
                } else {
                    dynamicPattern = "(?<=" + fieldsProvided.get(i) + ")(.*)(?=" + fieldsProvided.get(i + 1) + ")";
                }
                Pattern pattern = Pattern.compile(dynamicPattern);
                Matcher matcher = pattern.matcher(cleanParamValue);
                if (matcher.find()) {
                    dismaxQuerymap.put(cleanFieldValue(fieldsProvided.get(i)), cleanFieldValue(matcher.group()));
                }
            }
        }
        return dismaxQuerymap;
    }

    /**
     * Gets the fields provided in the dismax query string
     * For ex, if query string is : {!edismax ps=2 tie=0.1 qf="title^weight proxy_dc_creator^weight"}
     * then returns ps=, tie=, qf=
     *
     * @param value
     * @return
     * @throws SolrQueryException
     */
    public static List<String> getFieldsFromString(String value) throws SolrQueryException {
        List<String> fieldsProvided = new ArrayList<>();
        Pattern pattern = Pattern.compile(boostParamFieldPattern);
        Matcher matcher = pattern.matcher(value);
        // get the fields from the boost Param value
        while (matcher.find()) {
            fieldsProvided.add(matcher.group());
        }
        // must not contains duplicate fields
        Set<String> set = new HashSet<>(fieldsProvided);
        if(fieldsProvided.size() != set.size()) {
            throw new SolrQueryException(ProblemType.BOOST_PARAM_INVALID_VALUE);
        }
        return fieldsProvided;
    }

    /**
     * Normalises and cleans the string for processing
     */
    private static String normaliseAndClean(String boostParam) {
        String cleaned = StringUtils.remove(StringUtils.normalizeSpace(boostParam), "{!edismax");
        return StringUtils.remove(cleaned, "}");
    }

    /**
     * Removes the start and end curly braces from boost param.
     * @param boostParam
     * @return
     */
    private static  String removeStartAndEndBraces(String boostParam) {
        String cleaned = StringUtils.removeStart(StringUtils.normalizeSpace(boostParam), "{");
        return StringUtils.removeEnd(cleaned, "}");
    }

    /**
     * Removes the special characters '=' and '"' and trailing spaces
     * @param fieldValue
     * @return
     */
    private static  String cleanFieldValue(String fieldValue) {
        return StringUtils.replaceEach(fieldValue, new String [] {"=", "\""}, new String [] {"", ""}).trim();
    }

}
