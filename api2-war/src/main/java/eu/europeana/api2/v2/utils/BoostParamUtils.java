package eu.europeana.api2.v2.utils;

import eu.europeana.corelib.edm.exceptions.SolrQueryException;
import eu.europeana.corelib.web.exception.ProblemType;
import org.apache.commons.lang3.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BoostParamUtils {

    private static String curlyBracesPattern = "[{+|}+]";

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
     * Removes the start and end curly braces from boost param.
     * @param boostParam
     * @return
     */
    private static  String removeStartAndEndBraces(String boostParam) {
        String cleaned = StringUtils.removeStart(StringUtils.normalizeSpace(boostParam), "{");
        return StringUtils.removeEnd(cleaned, "}");
    }

}
