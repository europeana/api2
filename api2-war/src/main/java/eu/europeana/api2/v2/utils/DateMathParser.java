/*
 * Copyright 2007-2018 The Europeana Foundation
 *
 *  Licenced under the EUPL, Version 1.1 (the "Licence") and subsequent versions as approved
 *  by the European Commission;
 *  You may not use this work except in compliance with the Licence.
 *
 *  You may obtain a copy of the Licence at:
 *  http://joinup.ec.europa.eu/software/page/eupl
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under
 *  the Licence is distributed on an "AS IS" basis, without warranties or conditions of
 *  any kind, either express or implied.
 *  See the Licence for the specific language governing permissions and limitations under
 *  the Licence.
 */

package eu.europeana.api2.v2.utils;


import eu.europeana.api2.v2.exceptions.DateMathParseException;
import eu.europeana.api2.v2.exceptions.InvalidGapException;
import org.apache.commons.lang3.StringUtils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Created by luthien on 19/12/2018 from org.apache.solr.util.DateMathParser
 */
public class DateMathParser {

    private static final TimeZone UTC = TimeZone.getTimeZone("UTC");

    /** Default TimeZone for DateMath rounding (UTC) */
    private static final TimeZone DEFAULT_MATH_TZ = UTC;

    /**
     * Differs by {@link DateTimeFormatter#ISO_INSTANT} in that it's lenient.
     * @see #parseNoMath(String)
     */
    private static final DateTimeFormatter PARSER = new DateTimeFormatterBuilder()
            .parseCaseInsensitive().parseLenient().appendInstant().toFormatter(Locale.ROOT);

    /**
     * A mapping from (uppercased) String labels identifying time units,
     * to the corresponding enum (e.g. "YEARS") used to
     * set/add/roll that unit of measurement.
     */
    private static final Map<String,ChronoUnit> CALENDAR_UNITS = makeUnitsMap();


    /** @see #CALENDAR_UNITS */
    private static Map<String,ChronoUnit> makeUnitsMap() {

        Map<String,ChronoUnit> units = new HashMap<>(13);
        units.put("YEAR",        ChronoUnit.YEARS);
        units.put("YEARS",       ChronoUnit.YEARS);
        units.put("MONTH",       ChronoUnit.MONTHS);
        units.put("MONTHS",      ChronoUnit.MONTHS);
        units.put("DAY",         ChronoUnit.DAYS);
        units.put("DAYS",        ChronoUnit.DAYS);
        units.put("DATE",        ChronoUnit.DAYS);
        units.put("HOUR",        ChronoUnit.HOURS);
        units.put("HOURS",       ChronoUnit.HOURS);
        units.put("MINUTE",      ChronoUnit.MINUTES);
        units.put("MINUTES",     ChronoUnit.MINUTES);
        units.put("SECOND",      ChronoUnit.SECONDS);
        units.put("SECONDS",     ChronoUnit.SECONDS);
        units.put("MILLI",       ChronoUnit.MILLIS);
        units.put("MILLIS",      ChronoUnit.MILLIS);
        units.put("MILLISECOND", ChronoUnit.MILLIS);
        units.put("MILLISECONDS",ChronoUnit.MILLIS);

        return units;
    }

    /**
     * Returns the number of 'gaps' defined in the gapMatch string (follows the original DateMathParser syntax) that
     * fit in the date range defined by parameters start and end.
     * The returned value can be used to stop 'range facet' requests that could potentially lock Solr.
     *
     * @exception ParseException if gapMath cannot be parsed
     */
    public static long calculateGapCount(String start, String end, String gapMath) throws ParseException {
        Date startDate = parseNoMath(start);
        Date endDate = parseNoMath(end);
        final DateMathParser p = new DateMathParser();
        p.setNow(startDate);
        Date gapDate = p.parseMath(gapMath);
        long gapMillis = gapDate.getTime() - startDate.getTime();
        long timespanMillis = endDate.getTime() - startDate.getTime();
        return java.lang.Math.abs(timespanMillis / gapMillis);
    }

    /**
     * Calculates whether the number of requested gaps (specified by the number of times the interval defined by
     * 'gapMath' fits in the timespan between 'start' and 'end') exceeds the value of maxNrOfGaps.
     * Throws InvalidGapException if it does OR when interval and timespan have opposite signs (for instance
     * when requesting -1DAY for start > end, or +1DAY for start < end).
     * The idea is to prevent 'range facet' requests that could potentially lock Solr.
     * @param start         String: start date of timespan in format 0000-01-01T00:00:00Z
     * @param end           String: end date of timespan in format 0000-01-01T00:00:00Z
     * @param gapMath       String: time interval, in DateMathParser syntax
     * @param maxNrOfGaps   long:   maximum number of allowed gaps
     * @exception DateMathParseException if gapMath cannot be parsed
     * @exception InvalidGapException if above conditions are met
     */
    static void exceedsMaxNrOfGaps(String start, String end, String gapMath, long maxNrOfGaps) throws
                                                                                               DateMathParseException,
                                                                                               InvalidGapException {

        String               parsing        = start;
        String               whatsParsed    = "start";
        final DateMathParser p              = new DateMathParser();
        DateFormat           solrDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        Date                 startDate;

        solrDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        solrDateFormat.setLenient(false); // this is to avoid it accepting stuff like '1890-41-64T00:00:00Z'
        try {
            startDate = solrDateFormat.parse(start);
        } catch (ParseException e) {
            throw new DateMathParseException(e, parsing, whatsParsed);
        }
        Date endDate;
        parsing     = end;
        whatsParsed = "end";


        try {
            endDate = solrDateFormat.parse(end);
        } catch (ParseException e) {
            throw new DateMathParseException(e, parsing, whatsParsed);
        }


        p.setNow(startDate);
        parsing = gapMath;
        whatsParsed = "gap";
        Date gapDate;

        try {
            gapDate = p.parseMath(gapMath);
        } catch (ParseException e) {
            throw new DateMathParseException(e, parsing, whatsParsed);
        }

        long gapMillis      = gapDate.getTime() - startDate.getTime();
        long timespanMillis = endDate.getTime() - startDate.getTime();
        long actualNrOfGaps = timespanMillis / gapMillis;

        if (gapMillis < 0) {
            throw new InvalidGapException("Negative gaps ('" + gapMath + "') cannot be processed");
        } else if (timespanMillis < 0) {
            throw new InvalidGapException("The start date ('" + start + "') must be earlier than the end date ('" + end + "')");
        } else if (actualNrOfGaps > maxNrOfGaps){
            throw new InvalidGapException("The timespan between '" + start + "' and '" + end + "' contains " +
                                          actualNrOfGaps + " gaps of '" + gapMath + "', exceeding the maximum of " +
                                          maxNrOfGaps);
        }
    }

    /**
     * Returns a modified time by "adding" the specified value of units
     *
     * @exception IllegalArgumentException if unit isn't recognized.
     * @see #CALENDAR_UNITS
     */
    private static LocalDateTime add(LocalDateTime t, int val, String unit) {
        ChronoUnit uu = CALENDAR_UNITS.get(unit);
        if (null == uu) {
            throw new IllegalArgumentException("Adding Unit not recognized: "
                                               + unit);
        }
        return t.plus(val, uu);
    }

    /**
     * Returns a modified time by "rounding" down to the specified unit
     *
     * @exception IllegalArgumentException if unit isn't recognized.
     * @see #CALENDAR_UNITS
     */
    private static LocalDateTime round(LocalDateTime t, String unit) {
        ChronoUnit uu = CALENDAR_UNITS.get(unit);
        if (null == uu) {
            throw new IllegalArgumentException("Rounding Unit not recognized: "
                                               + unit);
        }
        // note: OffsetDateTime.truncatedTo does not support >= DAYS units so we handle those
        switch (uu) {
            case YEARS:
                return LocalDateTime.of(LocalDate.of(t.getYear(), 1, 1), LocalTime.MIDNIGHT); // midnight is 00:00:00
            case MONTHS:
                return LocalDateTime.of(LocalDate.of(t.getYear(), t.getMonth(), 1), LocalTime.MIDNIGHT);
            case DAYS:
                return LocalDateTime.of(t.toLocalDate(), LocalTime.MIDNIGHT);
            default:
                assert !uu.isDateBased();// >= DAY
                return t.truncatedTo(uu);
        }
    }

    /**
     * Parsing Solr dates <b>without DateMath</b>.
     * This is the standard/pervasive ISO-8601 UTC format but is configured with some leniency.
     *
     * Callers should almost always call
     *
     * @throws DateTimeParseException if it can't parse
     */
    private static Date parseNoMath(String val) {
        return new Date(PARSER.parse(val, Instant::from).toEpochMilli());
    }

    private TimeZone zone;
    private Date now;

    /**
     * Default constructor that assumes UTC should be used for rounding unless
     * otherwise specified in the SolrRequestInfo
     *
     */
    private DateMathParser() {
        this(null);
    }

    /**
     * @param tz The TimeZone used for rounding (to determine when hours/days begin).  If null, then this method defaults
     *           to the value dictated by the SolrRequestInfo if it exists -- otherwise it uses UTC.
     */
    private DateMathParser(TimeZone tz) {
        zone = (null != tz) ? tz : DEFAULT_MATH_TZ;
    }

    /**
     * Defines this instance's concept of "now".
     * @see #getNow
     */
    private void setNow(Date n) {
        now = n;
    }

    private Date getNow() {
        if (now == null) {
            now = new Date();
        }
        return (Date) now.clone();
    }

    /**
     * Parses a string of commands relative "now" are returns the resulting Date.
     *
     * @exception ParseException positions in ParseExceptions are token positions, not character positions.
     */
    private Date parseMath(String math) throws ParseException {
        /* check for No-Op */
        if (0==math.length()) {
            return getNow();
        }
        // changed to always use UTC
        ZoneId zoneId = UTC.toZoneId();
        // localDateTime is a date and time local to the timezone specified
        LocalDateTime localDateTime = ZonedDateTime.ofInstant(getNow().toInstant(), zoneId).toLocalDateTime();

        String[] ops = splitter.split(math);
        int pos = 0;
        while ( pos < ops.length ) {

            if (1 != ops[pos].length()) {
                throw new ParseException
                        ("Multi character command found: \"" + ops[pos] + "\"", pos);
            }
            char command = ops[pos++].charAt(0);

            switch (command) {
                case '/':
                    if (ops.length < pos + 1) {
                        throw new ParseException
                                ("Need a unit after command: \"" + command + "\"", pos);
                    }
                    try {
                        localDateTime = round(localDateTime, ops[pos++]);
                    } catch (IllegalArgumentException e) {
                        throw new ParseException
                                ("Unit not recognized: \"" + ops[pos-1] + "\"", pos-1);
                    }
                    break;
                case '+': /* fall through */
                case '-':
                    if (ops.length < pos + 2) {
                        throw new ParseException
                                ("Need a value and unit for command: \"" + command + "\"", pos);
                    }
                    int val = 0;
                    try {
                        val = Integer.parseInt(ops[pos++]);
                    } catch (NumberFormatException e) {
                        throw new ParseException
                                ("Not a Number: \"" + ops[pos-1] + "\"", pos-1);
                    }
                    if ('-' == command) {
                        val = 0 - val;
                    }
                    try {
                        String unit = ops[pos++];
                        localDateTime = add(localDateTime, val, unit);
                    } catch (IllegalArgumentException e) {
                        throw new ParseException
                                ("Unit not recognized: \"" + ops[pos-1] + "\"", pos-1);
                    }
                    break;
                default:
                    throw new ParseException
                            ("Unrecognized command: \"" + command + "\"", pos-1);
            }
        }

        return Date.from(ZonedDateTime.of(localDateTime, zoneId).toInstant());
    }

    private static Pattern splitter = Pattern.compile("\\b|(?<=\\d)(?=\\D)");

}
