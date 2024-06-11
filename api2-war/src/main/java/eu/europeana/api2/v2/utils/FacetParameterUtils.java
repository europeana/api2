package eu.europeana.api2.v2.utils;

import eu.europeana.api.search.syntax.field.FieldDeclaration;
import eu.europeana.api.search.syntax.field.FieldMode;
import eu.europeana.api.search.syntax.field.FieldRegistry;
import eu.europeana.api.search.syntax.field.FieldType;
import eu.europeana.api2.v2.exceptions.DateMathParseException;
import eu.europeana.api2.v2.exceptions.InvalidParamValueException;
import eu.europeana.api2.v2.exceptions.InvalidRangeOrGapException;
import eu.europeana.api2.v2.exceptions.MissingParamException;
import eu.europeana.api2.v2.model.NumericFacetParameter;
import eu.europeana.corelib.definitions.solr.RangeFacetType;
import eu.europeana.corelib.definitions.solr.SolrFacetType;
import eu.europeana.corelib.definitions.solr.TechnicalFacetType;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.DataFormatException;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

/**
 * Utility class for extracting numeric parameters specific to a given facet.
 *
 * @author Peter.Kiraly@kb.nl / Lúthien
 */
public class FacetParameterUtils {

    private static final String DEFAULT_LIMIT_KEY       = "f.DEFAULT.facet.limit";
    private static final String DEFAULT_OFFSET_KEY      = "f.DEFAULT.facet.offset";
    private static final int LIMIT_FOR_DATA_PROVIDER    = 50;
    private static final int LIMIT_FOR_DEFAULT          = 50;
    private static final int LIMIT_FOR_CUSTOM           = 50;
    private static final int LIMIT_FOR_TECH_DEFAULT     = 50;
    private static final int LIMIT_FOR_TECH_CUSTOM      = 50;

    private static final String FACET_RANGE             = "facet.range";
    private static final String FACET_RANGE_START       = "facet.range.start";
    private static final String FACET_RANGE_END         = "facet.range.end";
    private static final String FACET_RANGE_GAP         = "facet.range.gap";
    private static final String FACET_MINCOUNT          = "facet.mincount";

    private static final String DEFAULT_START           = "0001-01-01T00:00:00Z"; // year 0 does not exist
    private static final String NOW                     = "now";
    private static final String DEFAULT_GAP             = "+1YEAR";

    private static final long MAX_RANGE_FACETS          = 30000L;
    public static final String NEGATIVE_SIGN = "-";
    public static final String DATE_FIELD_FACET_REGEX = "^facet\\.([a-z,A-Z]*)\\.(gap|start|end)$";
    private static List<String> defaultSolrFacetList;
    private static List<String> rangeFacetList;
    private static List<String> rangeSpecifiers = Arrays.asList("start", "end", "gap");
    private static DateFormat solrDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
    private static Map<String,String> dateTimeSpecifiersMap ;

    static {
        defaultSolrFacetList = new ArrayList<>();
        for (SolrFacetType facet : SolrFacetType.values()) defaultSolrFacetList.add(facet.toString());
        rangeFacetList = new ArrayList<>();
        for (RangeFacetType facet : RangeFacetType.values()) rangeFacetList.add(facet.toString());

        dateTimeSpecifiersMap = new HashMap<>();
        dateTimeSpecifiersMap.put("Y" ,"YEAR");
        dateTimeSpecifiersMap.put("M" ,"MONTHS");
        dateTimeSpecifiersMap.put("d" ,"DAYS");
        dateTimeSpecifiersMap.put("H" ,"HOURS");
        dateTimeSpecifiersMap.put("m" ,"MINUTES");
        dateTimeSpecifiersMap.put("s" ,"SECONDS");
    }

    /**
     * Returns all relevant parameters of a given type (right now: limit and offset)
     *
     * @param type                   Type of parameter. Currently, we support "limit" and "offset"
     * @param solrFacets             List of Solr facets
     * @param parameters             The request parameters
     * @param defaultFacetsRequested Whether the default facets should be checked
     */
    public static Map<String, Integer> getSolrFacetParams(String type, String[] solrFacets,
                                                          Map<String, String[]> parameters,
                                                          boolean defaultFacetsRequested) {
        Map<String, Integer> solrFacetParams = new HashMap<>();
        if (defaultFacetsRequested) {
            for (SolrFacetType solrFacet : SolrFacetType.values()) {
                saveNumericFacetParam(type, solrFacet.name(), parameters, true, false, solrFacetParams);
            }
        }

        if (ArrayUtils.isNotEmpty(solrFacets)) {
            for (String solrFacetName : solrFacets) {
                if (!(defaultFacetsRequested && defaultSolrFacetList.contains(solrFacetName))) { // no duplicate DEFAULT facets
                    saveNumericFacetParam(type, solrFacetName, parameters, defaultSolrFacetList.contains(solrFacetName), false, solrFacetParams);
                }
            }
        }
        return solrFacetParams;
    }
    /**
     * Returns all relevant parameters of a given type (right now: limit and offset)
     *
     * @param type                   Type of parameter. Currently we support "limit" and "offset"
     * @param technicalFacets        List of technical metadata facets
     * @param parameters             The request parameters
     * @param defaultFacetsRequested Whether the all technical metadata facets should be checked
     */
    public static Map<String, Integer> getTechnicalFacetParams(String type, String[] technicalFacets, Map<String,
            String[]> parameters, boolean defaultFacetsRequested) {
        Map<String, Integer> technicalFacetParams = new HashMap<>();
        if (defaultFacetsRequested) {
            for (TechnicalFacetType technicalFacet : TechnicalFacetType.values()){
                saveNumericFacetParam(type, technicalFacet.name(), parameters, true, true, technicalFacetParams);
            }
        } else if (ArrayUtils.isNotEmpty(technicalFacets)) {
            for (String technicalFacetName : technicalFacets) {
                saveNumericFacetParam(type, technicalFacetName, parameters, false, true, technicalFacetParams);
            }
        }
        return technicalFacetParams;
    }

    /**
     * Process the new date faceting parameters which are in the below form
     * facet.<fieldname>.gap=<gap>
     * facet.<fieldname>.start=<start>
     * facet.<fieldname>.end=<end>
     *
     * @param parameters
     * @return Map containing the parameter and its value
     * @throws DateMathParseException
     * @throws DataFormatException
     * @throws MissingParamException
     * @throws InvalidParamValueException
     *
     */
    public static Map<String, String> getFacetRangeParamsForDateSearch(Map<String, String[]> parameters)
        throws DateMathParseException,  DataFormatException, MissingParamException, InvalidParamValueException {

        Map<String, String> dateRangeParams = new HashMap<>();
        HashSet<String> fieldsForFaceting = new HashSet<>();

        //extract valid  date faceting parameters from input parameters
        for(Entry<String, String[]> entry:parameters.entrySet()){
            String paramName = entry.getKey();
            String[] paramValue = entry.getValue();
            Matcher matcher = Pattern.compile(DATE_FIELD_FACET_REGEX).matcher(paramName);
            if(matcher.matches()){
                //paramName e.g.  facet.created.gap
                String[] splitParamName = paramName.split("\\.");
                String rangeSpecifier = splitParamName[2];
                FieldDeclaration field = FieldRegistry.INSTANCE.getField(splitParamName[1]);
                if (field != null && rangeSpecifiers.contains(rangeSpecifier)
                    && FieldType.DATE.equals(field.getType())) {
                    String facetingField = field.getField(FieldMode.FACET);
                    if (facetingField != null) {
                        fieldsForFaceting.add(facetingField);
                        String newKey = "f." + facetingField + "." + FACET_RANGE + "."+rangeSpecifier;
                        dateRangeParams.put(newKey, paramValue[0]);
                    }
                }
            }
        }
        validateAndUpdateDateFacetingParams(fieldsForFaceting, dateRangeParams);
       return dateRangeParams;
    }

    /**
     * Method validates and updates the search date faceting related parameters.
     * @param fieldsForFaceting
     * @param dateRangeParams
     * @throws MissingParamException
     * @throws DateMathParseException
     * @throws InvalidParamValueException
     */
    private static void validateAndUpdateDateFacetingParams(HashSet<String> fieldsForFaceting,
        Map<String, String> dateRangeParams)
        throws MissingParamException, DateMathParseException, InvalidParamValueException, DataFormatException {

        for(String field : fieldsForFaceting){
            String startDate = dateRangeParams.get("f." + field + "." + FACET_RANGE_START);
            String gap = dateRangeParams.get("f." + field + "." + FACET_RANGE_GAP);
            String endDate = dateRangeParams.get("f." + field + "." + FACET_RANGE_END);

            String gapValue;
            String gapValueUnit;
            //User need to provide both start and gap values
            if(StringUtils.isNotBlank(startDate) && StringUtils.isNotBlank(gap) ) {
                //in case both the start and gap provided but end not provided , default it to current dateTime
                if(StringUtils.isBlank(endDate)){
                    endDate= NOW;
                }
                startDate = convertDateInSolrFormat(startDate);
                endDate = convertDateInSolrFormat(endDate);
                //format param value  e.g. if input val is 2Y out put will be +2YEAR
                if(!gap.startsWith("+")) {
                    gap="+"+gap;
                }
                if(gap.startsWith("-"))
                    throw new InvalidParamValueException("Negative facet gaps are not allowed!");

                gapValue = StringUtils.chop(gap);
                gapValueUnit = dateTimeSpecifiersMap.get(gap.substring(gap.length() - 1));
                gap = gapValue+gapValueUnit;
                endDate = adjustEndDateForFaceting(endDate,gapValue,gapValueUnit);
            }
            else {
                throw new MissingParamException(String.format(
                    "Parameter facet.%s.start  and facet.%s.gap are mandatory for date search with facets.",
                    field, field));
            }

            compareDatesForFaceting(startDate,endDate,gapValue,gapValueUnit);

            dateRangeParams.put("f." + field + "." + FACET_RANGE + ".start",startDate);
            dateRangeParams.put("f." + field + "." + FACET_RANGE + ".end"  , endDate);
            dateRangeParams.put("f." + field + "." + FACET_RANGE + ".gap",gap);


             String facetRange = dateRangeParams.get(FACET_RANGE);
             if(StringUtils.isNotBlank(facetRange) && !facetRange.contains(field)) {
                 dateRangeParams.put(FACET_RANGE, facetRange+","+field);
             }
             else {
                 dateRangeParams.put(FACET_RANGE, field);
             }

        }
    }

    private static void compareDatesForFaceting( String startDate,String endDate,String gapValue,String gapUnit )
        throws DateMathParseException, InvalidParamValueException {
           //start date should be before end date
           Date startDateVal = parseDate(startDate);
           LocalDateTime startDateTime = LocalDateTime.ofInstant(startDateVal.toInstant(),
               ZoneOffset.UTC);
           Date endDateVal = parseDate(endDate);
           LocalDateTime endDateTime = LocalDateTime.ofInstant(endDateVal.toInstant(),
               ZoneOffset.UTC);
           if (startDateVal.compareTo(endDateVal) > 0) {
               throw new InvalidParamValueException(
                   "Facet start date can not be grater than end date !");
           }
        validateDateAgainstGapValues(gapValue, gapUnit, startDateTime, endDateTime);
    }

    private static void validateDateAgainstGapValues(String gapValue, String gapUnit, LocalDateTime startDateTime,
        LocalDateTime endDateTime) throws InvalidParamValueException {
        Date currentDate = new Date();
        Date gap = getDateBasedOnGapDuration(gapValue, gapUnit, currentDate);

        long timespanInMillis = Duration.between(startDateTime, endDateTime).toMillis();
        long gapInMillis = Duration.between(
            LocalDateTime.ofInstant(currentDate.toInstant(), ZoneOffset.UTC),
            LocalDateTime.ofInstant(gap.toInstant(), ZoneOffset.UTC)).toMillis();

        if( timespanInMillis < gapInMillis){
            throw new InvalidParamValueException("Incorrect gap value :  should not be more than the gap between start and end date!");
        }
        if((timespanInMillis+1)/gapInMillis >MAX_RANGE_FACETS){
            throw new InvalidParamValueException("Incorrect gap value : should not be more than maximum allowed number of gaps");
        }
    }

    private static Date parseDate(String dateVal) throws DateMathParseException {
        try {
            return solrDateFormat.parse(dateVal);
        } catch (ParseException e) {
            throw new DateMathParseException(e, dateVal, "startDate");
        }
    }

    /**
     * adjust the end date that is passed to solr for showing the user correct faceting value e.g. in
     * case facet.field.start -1980 facet.field.end -1990 and facet.field.gap = 2Y then value of facet
     * end parameter will be calculated as facet.field.end + facet.field.gap ignore the +/- sign in
     * front of gap and
     *
     * @return
     */
    private static String adjustEndDateForFaceting(String end,String gapVal,String gapValUnit)
        throws  DateMathParseException {
       try {
           if (end.startsWith(NEGATIVE_SIGN))
               end = end.substring(1);
           Date date = solrDateFormat.parse(end);
           return solrDateFormat.format(getDateBasedOnGapDuration(gapVal, gapValUnit, date));
       }catch(ParseException e){
           throw new DateMathParseException(e, end, end);
       }
    }

    private static Date getDateBasedOnGapDuration(String gapVal, String gapValUnit, Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int gapValueForEndDateAdjustment = Integer.parseInt(gapVal);
        if("YEAR".equals(gapValUnit))  calendar.add(Calendar.YEAR, gapValueForEndDateAdjustment);
        if("MONTHS".equals(gapValUnit))  calendar.add(Calendar.MONTH, gapValueForEndDateAdjustment);
        if("DAYS".equals(gapValUnit))  calendar.add(Calendar.DATE, gapValueForEndDateAdjustment);
        if("HOURS".equals(gapValUnit))  calendar.add(Calendar.HOUR_OF_DAY, gapValueForEndDateAdjustment);
        if("MINUTES".equals(gapValUnit))  calendar.add(Calendar.MINUTE, gapValueForEndDateAdjustment);
        if("SECONDS".equals(gapValUnit))  calendar.add(Calendar.SECOND, gapValueForEndDateAdjustment);
        return calendar.getTime();
    }

    public static String convertDateInSolrFormat(String val) throws DataFormatException {
        if (NOW.equalsIgnoreCase(val)) { return thisVeryMoment(); }

        Pattern pattern = Pattern.compile("^(-)?\\d{4}(-\\d{2}(-d{2})?)?$");
        Matcher matcher = pattern.matcher(val);
        if (!matcher.matches()) {
           throw new DataFormatException("Invalid date value " + val +"! should be in format YYYY-MM-dd or YYYY-MM  or YYYY");
        }
        String formatedDate = "";
        if (val.startsWith(NEGATIVE_SIGN)) {
            val = val.substring(1);
            formatedDate = NEGATIVE_SIGN;
        }
        return formatedDate + getDateInSolrFormat(val);
    }

    @NotNull
    private static String getDateInSolrFormat(String val) {
        Calendar cal = Calendar.getInstance();
        String[] tokenizedDate = val.split(NEGATIVE_SIGN);

        //values are defaulted in case not provided by user
        int year = 1;
        int month = 0;
        int day=1;
        if(tokenizedDate.length>=1)
           year = Integer.parseInt(tokenizedDate[0]);
        if(tokenizedDate.length>=2)
            month = Integer.parseInt(tokenizedDate[1]);
        if(tokenizedDate.length==3)
            day = Integer.parseInt(tokenizedDate[2]);
        cal.set(year,month,day,0,0,0);
        return  solrDateFormat.format(cal.getTime());
    }

    // NOTE that there can be more than one facet range parameter for every field, eg:
    // facet.range=timestamp & &facet.range.start=0000-01-01T00:00:00Z & &facet.range.end=NOW & facet.range.gap=+1DAY
    public static Map<String, String> getDateRangeParams(Map<String, String[]> parameters)
        throws DateMathParseException,
        InvalidRangeOrGapException, DataFormatException, MissingParamException, InvalidParamValueException {
        Map<String, String> dateRangeParams = new HashMap<>();

        // first, retrieve & validate field values from comma-separated facet.range parameter
        List<String> facetsToRange = retrieveRangeFacetList(parameters, FACET_RANGE);
        StringBuilder facetRangeValue = new StringBuilder();
        for (String facetToRange : facetsToRange) {
            facetRangeValue.append(facetToRange);
            facetRangeValue.append(",");
        }
        if (!facetsToRange.isEmpty()){
            dateRangeParams.put(FACET_RANGE, StringUtils.chop(facetRangeValue.toString()));
        }

        // and last, process the field-specific start, end & gap parameters
        for (String facetToRange : facetsToRange){
            for (String rangeSpecifier : rangeSpecifiers){
                String globalSpecifier = FACET_RANGE + "." + rangeSpecifier;
                String fieldSpecifier = "f." + facetToRange + "." + FACET_RANGE + "." + rangeSpecifier;
                if (parameters.containsKey(globalSpecifier)){
                    dateRangeParams.put(fieldSpecifier, plusificate(parameters.get(globalSpecifier)[0], rangeSpecifier));
                } else if (parameters.containsKey(fieldSpecifier)){
                    dateRangeParams.put(fieldSpecifier, plusificate(parameters.get(fieldSpecifier)[0], rangeSpecifier));
                } else {
                    dateRangeParams.put(fieldSpecifier, getDefaultValue(rangeSpecifier));
                }
            }
            DateMathParser.exceedsMaxNrOfGaps(
                        dateRangeParams.get("f." + facetToRange + "." + FACET_RANGE_START),
                        dateRangeParams.get("f." + facetToRange + "." + FACET_RANGE_END),
                        dateRangeParams.get("f." + facetToRange + "." + FACET_RANGE_GAP),
                        MAX_RANGE_FACETS);
        }
        getFacetRangeParamsForDateSearch(parameters).forEach(dateRangeParams::putIfAbsent);

      return dateRangeParams;
    }

    private static String getDefaultValue(String rangeSpecifier){
        String defaultValue;
        switch(rangeSpecifier){
            case "start":
                defaultValue = DEFAULT_START;
                break;
            case "end":
                defaultValue = thisVeryMoment();
                break;
            case "gap":
                defaultValue = DEFAULT_GAP;
                break;
            default:
                defaultValue = "";
        }
        return defaultValue;
    }

    private static String plusificate(String plusMinusNow, String rangeSpecifier){
        if (StringUtils.equalsIgnoreCase(rangeSpecifier, "gap") &&
            !StringUtils.startsWithAny(plusMinusNow, "+", "-")){
            return "+" + plusMinusNow;
        } else if (StringUtils.equalsIgnoreCase(NOW, plusMinusNow)){
            return thisVeryMoment();
        } else {
            return plusMinusNow;
        }
    }

    private static String thisVeryMoment(){
        return solrDateFormat.format(new Date());
    }

    // retrieve the facets that need to be ranged, but check them against the values in Enum RangeFacetType
    private static List<String> retrieveRangeFacetList(Map<String, String[]> parameters, String pattern){
        List<String> facetsToRangeTemp = new ArrayList<>();
        List<String> facetsToRange = new ArrayList<>();
        if (parameters.containsKey(pattern)){
            Collections.addAll(facetsToRangeTemp, StringUtils.stripAll(
                    StringUtils.split(parameters.get(pattern)[0], ',')));
            for (String facetToRangeTemp : facetsToRangeTemp) {
                if (rangeFacetList.contains(facetToRangeTemp)){
                    facetsToRange.add(facetToRangeTemp);
                }
            }
        }
        return facetsToRange;
    }


    /**
     * Extracts and saves parameter of a given type (right now: limit and offset) belongs to a facet
     *
     * @param type        The type of parameter (limit or offset)
     * @param name        The name of the facet
     * @param parameters  Request parameters
     * @param isDefault   The facet is a default facet
     * @param facetParams The container to save into
     */
    private static void saveNumericFacetParam(String type, String name, Map<String, String[]> parameters,
                                              boolean isDefault, boolean isTech, Map<String, Integer> facetParams) {
        NumericFacetParameter parameter = null;
        if (type.equals("limit")) parameter = getFacetLimit(name, parameters, isDefault, isTech);
        else if (type.equals("offset")) parameter = getFacetOffset(name, parameters, isDefault);
        if (parameter != null) facetParams.put(parameter.getName(), parameter.getValue());

    }

    private static NumericFacetParameter getFacetLimit(String facet, Map<String, String[]> parameters,
                                                       boolean isDefault, boolean isTech) {
        String key = "f." + facet + ".facet.limit";
        Integer defaultLimit;
        if (isTech) defaultLimit = isDefault ? LIMIT_FOR_TECH_DEFAULT : LIMIT_FOR_TECH_CUSTOM;
        else {
            int dataProvider = StringUtils.equals(facet, "DATA_PROVIDER") ? LIMIT_FOR_DATA_PROVIDER
                : LIMIT_FOR_DEFAULT;
            defaultLimit = isDefault ? dataProvider : LIMIT_FOR_CUSTOM;
        }
        return extractNumericParameter(key, DEFAULT_LIMIT_KEY, parameters, isDefault, defaultLimit);
    }

    private static NumericFacetParameter getFacetOffset(String facet, Map<String, String[]> parameters,
                                                        boolean isDefault) {
        String key = "f." + facet + ".facet.offset";
        return extractNumericParameter(key, DEFAULT_OFFSET_KEY, parameters, isDefault, null);
    }

    private static NumericFacetParameter extractNumericParameter(String key, String defaultKey, Map<String, String[]> parameters,
                                                                 boolean isDefault, Integer defaultValue) {
        if (parameters.containsKey(key)) {
            String[] value = parameters.get(key);
            return new NumericFacetParameter(key, value[0]);
        }
        if (isDefault && parameters.containsKey(defaultKey)) {
            String[] value = parameters.get(defaultKey);
            return new NumericFacetParameter(key, value[0]);
        }
        if (defaultValue != null) return new NumericFacetParameter(key, defaultValue);
        return null;
    }

    public static String getLimitForDataProvider(){
        return String.valueOf(LIMIT_FOR_DATA_PROVIDER);
    }
}
