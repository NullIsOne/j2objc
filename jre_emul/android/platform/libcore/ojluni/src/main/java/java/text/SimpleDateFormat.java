/*
 * Copyright (C) 2014 The Android Open Source Project
 * Copyright (c) 1996, 2013, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

/*
 * (C) Copyright Taligent, Inc. 1996 - All Rights Reserved
 * (C) Copyright IBM Corp. 1996-1998 - All Rights Reserved
 *
 *   The original version of this source code and documentation is copyrighted
 * and owned by Taligent, Inc., a wholly-owned subsidiary of IBM. These
 * materials are provided under terms of a License Agreement between Taligent
 * and Sun. This technology is protected by multiple US and International
 * patents. This notice and attribution to Taligent may not be removed.
 *   Taligent is a registered trademark of Taligent, Inc.
 *
 */

package java.text;

import android.icu.text.TimeZoneNames;

import com.google.j2objc.util.ReflectionUtil;
import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.Map;
import java.util.NavigableMap;
import java.util.SimpleTimeZone;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import libcore.icu.LocaleData;
import sun.util.calendar.CalendarUtils;

import static java.text.DateFormatSymbols.*;

// Android-changed: Added supported API level, removed unnecessary <br>
// Android-changed: Clarified info about X symbol time zone parsing
// Android-changed: Changed MMMMM to MMMM in month format example (ICU behavior).
// http://b/147860740
/**
 * <code>SimpleDateFormat</code> is a concrete class for formatting and
 * parsing dates in a locale-sensitive manner. It allows for formatting
 * (date &rarr; text), parsing (text &rarr; date), and normalization.
 *
 * <p>
 * <code>SimpleDateFormat</code> allows you to start by choosing
 * any user-defined patterns for date-time formatting. However, you
 * are encouraged to create a date-time formatter with either
 * <code>getTimeInstance</code>, <code>getDateInstance</code>, or
 * <code>getDateTimeInstance</code> in <code>DateFormat</code>. Each
 * of these class methods can return a date/time formatter initialized
 * with a default format pattern. You may modify the format pattern
 * using the <code>applyPattern</code> methods as desired.
 * For more information on using these methods, see
 * {@link DateFormat}.
 *
 * <h3>Date and Time Patterns</h3>
 * <p>
 * Date and time formats are specified by <em>date and time pattern</em>
 * strings.
 * Within date and time pattern strings, unquoted letters from
 * <code>'A'</code> to <code>'Z'</code> and from <code>'a'</code> to
 * <code>'z'</code> are interpreted as pattern letters representing the
 * components of a date or time string.
 * Text can be quoted using single quotes (<code>'</code>) to avoid
 * interpretation.
 * <code>"''"</code> represents a single quote.
 * All other characters are not interpreted; they're simply copied into the
 * output string during formatting or matched against the input string
 * during parsing.
 * <p>
 * The following pattern letters are defined (all other characters from
 * <code>'A'</code> to <code>'Z'</code> and from <code>'a'</code> to
 * <code>'z'</code> are reserved):
 * <blockquote>
 * <table class="striped">
 * <caption style="display:none">Chart shows pattern letters, date/time component, presentation, and examples.</caption>
 * <thead>
 *     <tr>
 *         <th scope="col" style="text-align:left">Letter
 *         <th scope="col" style="text-align:left">Date or Time Component
 *         <th scope="col" style="text-align:left">Presentation
 *         <th scope="col" style="text-align:left">Examples
 *         <th scope="col" style="text-align:left">Supported (API Levels)
 * </thead>
 * <tbody>
 *     <tr>
 *         <th scope="row"><code>G</code>
 *         <td>Era designator
 *         <td><a href="#text">Text</a>
 *         <td><code>AD</code>
 *         <td>1+</td>
 *     <tr>
 *         <th scope="row"><code>y</code>
 *         <td>Year
 *         <td><a href="#year">Year</a>
 *         <td><code>1996</code>; <code>96</code>
 *         <td>1+</td>
 *     <tr>
 *         <th scope="row"><code>Y</code>
 *         <td>Week year
 *         <td><a href="#year">Year</a>
 *         <td><code>2009</code>; <code>09</code>
 *         <td>24+</td>
 *     <tr>
 *         <th scope="row"><code>M</code>
 *         <td>Month in year (context sensitive)
 *         <td><a href="#month">Month</a>
 *         <td><code>July</code>; <code>Jul</code>; <code>07</code>
 *         <td>1+</td>
 *     <tr>
 *         <th scope="row"><code>L</code>
 *         <td>Month in year (standalone form)
 *         <td><a href="#month">Month</a>
 *         <td><code>July</code>; <code>Jul</code>; <code>07</code>
 *         <td>TBD</td>
 *     <tr>
 *         <th scope="row"><code>w</code>
 *         <td>Week in year
 *         <td><a href="#number">Number</a>
 *         <td><code>27</code>
 *         <td>1+</td>
 *     <tr>
 *         <th scope="row"><code>W</code>
 *         <td>Week in month
 *         <td><a href="#number">Number</a>
 *         <td><code>2</code>
 *         <td>1+</td>
 *     <tr>
 *         <th scope="row"><code>D</code>
 *         <td>Day in year
 *         <td><a href="#number">Number</a>
 *         <td><code>189</code>
 *         <td>1+</td>
 *     <tr>
 *         <th scope="row"><code>d</code>
 *         <td>Day in month
 *         <td><a href="#number">Number</a>
 *         <td><code>10</code>
 *         <td>1+</td>
 *     <tr>
 *         <th scope="row"><code>F</code>
 *         <td>Day of week in month
 *         <td><a href="#number">Number</a>
 *         <td><code>2</code>
 *         <td>1+</td>
 *     <tr>
 *         <th scope="row"><code>E</code>
 *         <td>Day name in week
 *         <td><a href="#text">Text</a>
 *         <td><code>Tuesday</code>; <code>Tue</code>
 *         <td>1+</td>
 *     <tr>
 *         <th scope="row"><code>u</code>
 *         <td>Day number of week (1 = Monday, ..., 7 = Sunday)
 *         <td><a href="#number">Number</a>
 *         <td><code>1</code>
 *         <td>24+</td>
 *     <tr>
 *         <th scope="row"><code>a</code>
 *         <td>Am/pm marker
 *         <td><a href="#text">Text</a>
 *         <td><code>PM</code>
 *         <td>1+</td>
 *     <tr>
 *         <th scope="row"><code>H</code>
 *         <td>Hour in day (0-23)
 *         <td><a href="#number">Number</a>
 *         <td><code>0</code>
 *         <td>1+</td>
 *     <tr>
 *         <th scope="row"><code>k</code>
 *         <td>Hour in day (1-24)
 *         <td><a href="#number">Number</a>
 *         <td><code>24</code>
 *         <td>1+</td>
 *     <tr>
 *         <th scope="row"><code>K</code>
 *         <td>Hour in am/pm (0-11)
 *         <td><a href="#number">Number</a>
 *         <td><code>0</code>
 *         <td>1+</td>
 *     <tr>
 *         <th scope="row"><code>h</code>
 *         <td>Hour in am/pm (1-12)
 *         <td><a href="#number">Number</a>
 *         <td><code>12</code>
 *         <td>1+</td>
 *     <tr>
 *         <th scope="row"><code>m</code>
 *         <td>Minute in hour
 *         <td><a href="#number">Number</a>
 *         <td><code>30</code>
 *         <td>1+</td>
 *     <tr>
 *         <th scope="row"><code>s</code>
 *         <td>Second in minute
 *         <td><a href="#number">Number</a>
 *         <td><code>55</code>
 *         <td>1+</td>
 *     <tr>
 *         <th scope="row"><code>S</code>
 *         <td>Millisecond
 *         <td><a href="#number">Number</a>
 *         <td><code>978</code>
 *         <td>1+</td>
 *     <tr>
 *         <th scope="row"><code>z</code>
 *         <td>Time zone
 *         <td><a href="#timezone">General time zone</a>
 *         <td><code>Pacific Standard Time</code>; <code>PST</code>; <code>GMT-08:00</code>
 *         <td>1+</td>
 *     <tr>
 *         <th scope="row"><code>Z</code>
 *         <td>Time zone
 *         <td><a href="#rfc822timezone">RFC 822 time zone</a>
 *         <td><code>-0800</code>
 *         <td>1+</td>
 *     <tr>
 *         <th scope="row"><code>X</code>
 *         <td>Time zone
 *         <td><a href="#iso8601timezone">ISO 8601 time zone</a>
 *         <td><code>-08</code>; <code>-0800</code>;  <code>-08:00</code>
 *         <td>24+</td>
 * </tbody>
 * </table>
 * </blockquote>
 * Pattern letters are usually repeated, as their number determines the
 * exact presentation:
 * <ul>
 * <li><strong><a id="text">Text:</a></strong>
 *     For formatting, if the number of pattern letters is 4 or more,
 *     the full form is used; otherwise a short or abbreviated form
 *     is used if available.
 *     For parsing, both forms are accepted, independent of the number
 *     of pattern letters.</li>
 * <li><strong><a id="number">Number:</a></strong>
 *     For formatting, the number of pattern letters is the minimum
 *     number of digits, and shorter numbers are zero-padded to this amount.
 *     For parsing, the number of pattern letters is ignored unless
 *     it's needed to separate two adjacent fields.</li>
 * <li><strong><a id="year">Year:</a></strong>
 *     If the formatter's {@link #getCalendar() Calendar} is the Gregorian
 *     calendar, the following rules are applied.
 *     <ul>
 *     <li>For formatting, if the number of pattern letters is 2, the year
 *         is truncated to 2 digits; otherwise it is interpreted as a
 *         <a href="#number">number</a>.
 *     <li>For parsing, if the number of pattern letters is more than 2,
 *         the year is interpreted literally, regardless of the number of
 *         digits. So using the pattern "MM/dd/yyyy", "01/11/12" parses to
 *         Jan 11, 12 A.D.
 *     <li>For parsing with the abbreviated year pattern ("y" or "yy"),
 *         <code>SimpleDateFormat</code> must interpret the abbreviated year
 *         relative to some century.  It does this by adjusting dates to be
 *         within 80 years before and 20 years after the time the <code>SimpleDateFormat</code>
 *         instance is created. For example, using a pattern of "MM/dd/yy" and a
 *         <code>SimpleDateFormat</code> instance created on Jan 1, 1997,  the string
 *         "01/11/12" would be interpreted as Jan 11, 2012 while the string "05/04/64"
 *         would be interpreted as May 4, 1964.
 *         During parsing, only strings consisting of exactly two digits, as defined by
 *         {@link Character#isDigit(char)}, will be parsed into the default century.
 *         Any other numeric string, such as a one digit string, a three or more digit
 *         string, or a two digit string that isn't all digits (for example, "-1"), is
 *         interpreted literally.  So "01/02/3" or "01/02/003" are parsed, using the
 *         same pattern, as Jan 2, 3 AD.  Likewise, "01/02/-3" is parsed as Jan 2, 4 BC.
 *     </ul>
 *     Otherwise, calendar system specific forms are applied.
 *     For both formatting and parsing, if the number of pattern
 *     letters is 4 or more, a calendar specific {@linkplain
 *     Calendar#LONG long form} is used. Otherwise, a calendar
 *     specific {@linkplain Calendar#SHORT short or abbreviated form}
 *     is used.
 *     <br>
 *     If week year {@code 'Y'} is specified and the {@linkplain
 *     #getCalendar() calendar} doesn't support any <a
 *     href="../util/GregorianCalendar.html#week_year"> week
 *     years</a>, the calendar year ({@code 'y'}) is used instead. The
 *     support of week years can be tested with a call to {@link
 *     DateFormat#getCalendar() getCalendar()}.{@link
 *     java.util.Calendar#isWeekDateSupported()
 *     isWeekDateSupported()}.</li>
 * <li><strong><a id="month">Month:</a></strong>
 *     If the number of pattern letters is 3 or more, the month is
 *     interpreted as <a href="#text">text</a>; otherwise,
 *     it is interpreted as a <a href="#number">number</a>.
 *     <ul>
 *     <li>Letter <em>M</em> produces context-sensitive month names, such as the
 *         embedded form of names. Letter <em>M</em> is context-sensitive in the
 *         sense that when it is used in the standalone pattern, for example,
 *         "MMMM", it gives the standalone form of a month name and when it is
 *         used in the pattern containing other field(s), for example, "d MMMM",
 *         it gives the format form of a month name. For example, January in the
 *         Catalan language is "de gener" in the format form while it is "gener"
 *         in the standalone form. In this case, "MMMM" will produce "gener" and
 *         the month part of the "d MMMM" will produce "de gener". If a
 *         {@code DateFormatSymbols} has been set explicitly with constructor
 *         {@link #SimpleDateFormat(String,DateFormatSymbols)} or method {@link
 *         #setDateFormatSymbols(DateFormatSymbols)}, the month names given by
 *         the {@code DateFormatSymbols} are used.</li>
 *     <li>Letter <em>L</em> produces the standalone form of month names.</li>
 *     </ul>
 *     <br></li>
 * <li><strong><a id="timezone">General time zone:</a></strong>
 *     Time zones are interpreted as <a href="#text">text</a> if they have
 *     names. For time zones representing a GMT offset value, the
 *     following syntax is used:
 *     <pre>
 *     <a id="GMTOffsetTimeZone"><i>GMTOffsetTimeZone:</i></a>
 *             <code>GMT</code> <i>Sign</i> <i>Hours</i> <code>:</code> <i>Minutes</i>
 *     <i>Sign:</i> one of
 *             <code>+ -</code>
 *     <i>Hours:</i>
 *             <i>Digit</i>
 *             <i>Digit</i> <i>Digit</i>
 *     <i>Minutes:</i>
 *             <i>Digit</i> <i>Digit</i>
 *     <i>Digit:</i> one of
 *             <code>0 1 2 3 4 5 6 7 8 9</code></pre>
 *     <i>Hours</i> must be between 0 and 23, and <i>Minutes</i> must be between
 *     00 and 59. The format is locale independent and digits must be taken
 *     from the Basic Latin block of the Unicode standard.
 *     <p>For parsing, <a href="#rfc822timezone">RFC 822 time zones</a> are also
 *     accepted.</li>
 * <li><strong><a id="rfc822timezone">RFC 822 time zone:</a></strong>
 *     For formatting, the RFC 822 4-digit time zone format is used:
 *
 *     <pre>
 *     <i>RFC822TimeZone:</i>
 *             <i>Sign</i> <i>TwoDigitHours</i> <i>Minutes</i>
 *     <i>TwoDigitHours:</i>
 *             <i>Digit Digit</i></pre>
 *     <i>TwoDigitHours</i> must be between 00 and 23. Other definitions
 *     are as for <a href="#timezone">general time zones</a>.
 *
 *     <p>For parsing, <a href="#timezone">general time zones</a> are also
 *     accepted.
 * <li><strong><a id="iso8601timezone">ISO 8601 Time zone:</a></strong>
 *     The number of pattern letters designates the format for both formatting
 *     and parsing as follows:
 *     <pre>
 *     <i>ISO8601TimeZone:</i>
 *             <i>OneLetterISO8601TimeZone</i>
 *             <i>TwoLetterISO8601TimeZone</i>
 *             <i>ThreeLetterISO8601TimeZone</i>
 *     <i>OneLetterISO8601TimeZone:</i>
 *             <i>Sign</i> <i>TwoDigitHours</i>
 *             {@code Z}
 *     <i>TwoLetterISO8601TimeZone:</i>
 *             <i>Sign</i> <i>TwoDigitHours</i> <i>Minutes</i>
 *             {@code Z}
 *     <i>ThreeLetterISO8601TimeZone:</i>
 *             <i>Sign</i> <i>TwoDigitHours</i> {@code :} <i>Minutes</i>
 *             {@code Z}</pre>
 *     Other definitions are as for <a href="#timezone">general time zones</a> or
 *     <a href="#rfc822timezone">RFC 822 time zones</a>.
 *
 *     <p>For formatting, if the offset value from GMT is 0, {@code "Z"} is
 *     produced. If the number of pattern letters is 1, any fraction of an hour
 *     is ignored. For example, if the pattern is {@code "X"} and the time zone is
 *     {@code "GMT+05:30"}, {@code "+05"} is produced.
 *
 *     <p>For parsing, the letter {@code "Z"} is parsed as the UTC time zone designator (therefore
 *     {@code "09:30Z"} is parsed as {@code "09:30 UTC"}.
 *     <a href="#timezone">General time zones</a> are <em>not</em> accepted.
 *     <p>If the number of {@code "X"} pattern letters is 4 or more (e.g. {@code XXXX}), {@link
 *     IllegalArgumentException} is thrown when constructing a {@code
 *     SimpleDateFormat} or {@linkplain #applyPattern(String) applying a
 *     pattern}.
 * </ul>
 * <code>SimpleDateFormat</code> also supports <em>localized date and time
 * pattern</em> strings. In these strings, the pattern letters described above
 * may be replaced with other, locale dependent, pattern letters.
 * <code>SimpleDateFormat</code> does not deal with the localization of text
 * other than the pattern letters; that's up to the client of the class.
 *
 * <h4>Examples</h4>
 *
 * The following examples show how date and time patterns are interpreted in
 * the U.S. locale. The given date and time are 2001-07-04 12:08:56 local time
 * in the U.S. Pacific Time time zone.
 * <blockquote>
 * <table class="striped">
 * <caption style="display:none">Examples of date and time patterns interpreted in the U.S. locale</caption>
 * <thead>
 *     <tr>
 *         <th scope="col" style="text-align:left">Date and Time Pattern
 *         <th scope="col" style="text-align:left">Result
 * </thead>
 * <tbody>
 *     <tr>
 *         <th scope="row"><code>"yyyy.MM.dd G 'at' HH:mm:ss z"</code>
 *         <td><code>2001.07.04 AD at 12:08:56 PDT</code>
 *     <tr>
 *         <th scope="row"><code>"EEE, MMM d, ''yy"</code>
 *         <td><code>Wed, Jul 4, '01</code>
 *     <tr>
 *         <th scope="row"><code>"h:mm a"</code>
 *         <td><code>12:08 PM</code>
 *     <tr>
 *         <th scope="row"><code>"hh 'o''clock' a, zzzz"</code>
 *         <td><code>12 o'clock PM, Pacific Daylight Time</code>
 *     <tr>
 *         <th scope="row"><code>"K:mm a, z"</code>
 *         <td><code>0:08 PM, PDT</code>
 *     <tr>
 *         <th scope="row"><code>"yyyyy.MMMM.dd GGG hh:mm aaa"</code>
 *         <td><code>02001.July.04 AD 12:08 PM</code>
 *     <tr>
 *         <th scope="row"><code>"EEE, d MMM yyyy HH:mm:ss Z"</code>
 *         <td><code>Wed, 4 Jul 2001 12:08:56 -0700</code>
 *     <tr>
 *         <th scope="row"><code>"yyMMddHHmmssZ"</code>
 *         <td><code>010704120856-0700</code>
 *     <tr>
 *         <th scope="row"><code>"yyyy-MM-dd'T'HH:mm:ss.SSSZ"</code>
 *         <td><code>2001-07-04T12:08:56.235-0700</code>
 *     <tr>
 *         <th scope="row"><code>"yyyy-MM-dd'T'HH:mm:ss.SSSXXX"</code>
 *         <td><code>2001-07-04T12:08:56.235-07:00</code>
 *     <tr>
 *         <th scope="row"><code>"YYYY-'W'ww-u"</code>
 *         <td><code>2001-W27-3</code>
 * </tbody>
 * </table>
 * </blockquote>
 *
 * <h4><a id="synchronization">Synchronization</a></h4>
 *
 * <p>
 * Date formats are not synchronized.
 * It is recommended to create separate format instances for each thread.
 * If multiple threads access a format concurrently, it must be synchronized
 * externally.
 *
 * @see          <a href="http://docs.oracle.com/javase/tutorial/i18n/format/simpleDateFormat.html">Java Tutorial</a>
 * @see          java.util.Calendar
 * @see          java.util.TimeZone
 * @see          DateFormat
 * @see          DateFormatSymbols
 * @author       Mark Davis, Chen-Lieh Huang, Alan Liu
 * @since 1.1
 */
public class SimpleDateFormat extends DateFormat {

    // the official serial version ID which says cryptically
    // which version we're compatible with
    static final long serialVersionUID = 4774881970558875024L;

    // the internal serial version which says which version was written
    // - 0 (default) for version up to JDK 1.1.3
    // - 1 for version from JDK 1.1.4, which includes a new field
    static final int currentSerialVersion = 1;

    /**
     * The version of the serialized data on the stream.  Possible values:
     * <ul>
     * <li><b>0</b> or not present on stream: JDK 1.1.3.  This version
     * has no <code>defaultCenturyStart</code> on stream.
     * <li><b>1</b> JDK 1.1.4 or later.  This version adds
     * <code>defaultCenturyStart</code>.
     * </ul>
     * When streaming out this class, the most recent format
     * and the highest allowable <code>serialVersionOnStream</code>
     * is written.
     * @serial
     * @since 1.1.4
     */
    private int serialVersionOnStream = currentSerialVersion;

    /**
     * The pattern string of this formatter.  This is always a non-localized
     * pattern.  May not be null.  See class documentation for details.
     * @serial
     */
    private String pattern;

    /**
     * Saved numberFormat and pattern.
     * @see SimpleDateFormat#checkNegativeNumberExpression
     */
    private transient NumberFormat originalNumberFormat;
    private transient String originalNumberPattern;

    /**
     * The minus sign to be used with format and parse.
     */
    private transient char minusSign = '-';

    /**
     * True when a negative sign follows a number.
     * (True as default in Arabic.)
     */
    private transient boolean hasFollowingMinusSign = false;

    // BEGIN Android-removed: App compat for formatting pattern letter M.
    // OpenJDK forces the standalone form of month when patterns contain pattern M only.
    // This feature is not incorporated for app compatibility and because the feature is
    // not documented in OpenJDK or Android.
    /*
    /**
     * True if standalone form needs to be used.
     *
    private transient boolean forceStandaloneForm = false;
    */
    // END Android-removed: App compat for formatting pattern letter M.

    /**
     * The compiled pattern.
     */
    private transient char[] compiledPattern;

    /**
     * Tags for the compiled pattern.
     */
    private static final int TAG_QUOTE_ASCII_CHAR       = 100;
    private static final int TAG_QUOTE_CHARS            = 101;

    /**
     * Locale dependent digit zero.
     * @see #zeroPaddingNumber
     * @see java.text.DecimalFormatSymbols#getZeroDigit
     */
    private transient char zeroDigit;

    /**
     * The symbols used by this formatter for week names, month names,
     * etc.  May not be null.
     * @serial
     * @see java.text.DateFormatSymbols
     */
    private DateFormatSymbols formatData;

    /**
     * We map dates with two-digit years into the century starting at
     * <code>defaultCenturyStart</code>, which may be any date.  May
     * not be null.
     * @serial
     * @since 1.1.4
     */
    private Date defaultCenturyStart;

    private transient int defaultCenturyStartYear;

    private static final int MILLIS_PER_MINUTE = 60 * 1000;

    // For time zones that have no names, use strings GMT+minutes and
    // GMT-minutes. For instance, in France the time zone is GMT+60.
    private static final String GMT = "GMT";

    /**
     * Cache to hold the DateTimePatterns of a Locale.
     */
    private static final ConcurrentMap<Locale, String[]> cachedLocaleData
        = new ConcurrentHashMap<Locale, String[]>(3);

    /**
     * Cache NumberFormat instances with Locale key.
     */
    private static final ConcurrentMap<Locale, NumberFormat> cachedNumberFormatData
        = new ConcurrentHashMap<Locale, NumberFormat>(3);

    /**
     * The Locale used to instantiate this
     * <code>SimpleDateFormat</code>. The value may be null if this object
     * has been created by an older <code>SimpleDateFormat</code> and
     * deserialized.
     *
     * @serial
     * @since 1.6
     */
    private Locale locale;

    /**
     * Indicates whether this <code>SimpleDateFormat</code> should use
     * the DateFormatSymbols. If true, the format and parse methods
     * use the DateFormatSymbols values. If false, the format and
     * parse methods call Calendar.getDisplayName or
     * Calendar.getDisplayNames.
     */
    transient boolean useDateFormatSymbols;

    /**
     * Constructs a <code>SimpleDateFormat</code> using the default pattern and
     * date format symbols for the default
     * {@link java.util.Locale.Category#FORMAT FORMAT} locale.
     * <b>Note:</b> This constructor may not support all locales.
     * For full coverage, use the factory methods in the {@link DateFormat}
     * class.
     */
    public SimpleDateFormat() {
        // BEGIN Android-changed: Android has no LocaleProviderAdapter. Use ICU locale data.
        /*
        this("", Locale.getDefault(Locale.Category.FORMAT));
        applyPatternImpl(LocaleProviderAdapter.getResourceBundleBased().getLocaleResources(locale)
                         .getDateTimePattern(SHORT, SHORT, calendar));
        */
        this(SHORT, SHORT, Locale.getDefault(Locale.Category.FORMAT));
        // END Android-changed: Android has no LocaleProviderAdapter. Use ICU locale data.
    }

    /**
     * Constructs a <code>SimpleDateFormat</code> using the given pattern and
     * the default date format symbols for the default
     * {@link java.util.Locale.Category#FORMAT FORMAT} locale.
     * <b>Note:</b> This constructor may not support all locales.
     * For full coverage, use the factory methods in the {@link DateFormat}
     * class.
     * <p>This is equivalent to calling
     * {@link #SimpleDateFormat(String, Locale)
     *     SimpleDateFormat(pattern, Locale.getDefault(Locale.Category.FORMAT))}.
     *
     * @see java.util.Locale#getDefault(java.util.Locale.Category)
     * @see java.util.Locale.Category#FORMAT
     * @param pattern the pattern describing the date and time format
     * @exception NullPointerException if the given pattern is null
     * @exception IllegalArgumentException if the given pattern is invalid
     */
    public SimpleDateFormat(String pattern)
    {
        this(pattern, Locale.getDefault(Locale.Category.FORMAT));
    }

    /**
     * Constructs a <code>SimpleDateFormat</code> using the given pattern and
     * the default date format symbols for the given locale.
     * <b>Note:</b> This constructor may not support all locales.
     * For full coverage, use the factory methods in the {@link DateFormat}
     * class.
     *
     * @param pattern the pattern describing the date and time format
     * @param locale the locale whose date format symbols should be used
     * @exception NullPointerException if the given pattern or locale is null
     * @exception IllegalArgumentException if the given pattern is invalid
     */
    public SimpleDateFormat(String pattern, Locale locale)
    {
        if (pattern == null || locale == null) {
            throw new NullPointerException();
        }

        initializeCalendar(locale);
        this.pattern = pattern;
        this.formatData = DateFormatSymbols.getInstanceRef(locale);
        this.locale = locale;
        initialize(locale);
    }

    /**
     * Constructs a <code>SimpleDateFormat</code> using the given pattern and
     * date format symbols.
     *
     * @param pattern the pattern describing the date and time format
     * @param formatSymbols the date format symbols to be used for formatting
     * @exception NullPointerException if the given pattern or formatSymbols is null
     * @exception IllegalArgumentException if the given pattern is invalid
     */
    public SimpleDateFormat(String pattern, DateFormatSymbols formatSymbols)
    {
        if (pattern == null || formatSymbols == null) {
            throw new NullPointerException();
        }

        this.pattern = pattern;
        this.formatData = (DateFormatSymbols) formatSymbols.clone();
        this.locale = Locale.getDefault(Locale.Category.FORMAT);
        initializeCalendar(this.locale);
        initialize(this.locale);
        useDateFormatSymbols = true;
    }

    /* Package-private, called by DateFormat factory methods */
    SimpleDateFormat(int timeStyle, int dateStyle, Locale loc) {
        if (loc == null) {
            throw new NullPointerException();
        }

        this.locale = loc;
        // initialize calendar and related fields
        initializeCalendar(loc);

        /* try the cache first */
        String[] dateTimePatterns = cachedLocaleData.get(loc);
        if (dateTimePatterns == null) { /* cache miss */
            LocaleData localeData = LocaleData.get(loc);
            dateTimePatterns = new String[9];
            dateTimePatterns[DateFormat.SHORT + 4] = localeData.getDateFormat(DateFormat.SHORT);
            dateTimePatterns[DateFormat.MEDIUM + 4] = localeData.getDateFormat(DateFormat.MEDIUM);
            dateTimePatterns[DateFormat.LONG + 4] = localeData.getDateFormat(DateFormat.LONG);
            dateTimePatterns[DateFormat.FULL + 4] = localeData.getDateFormat(DateFormat.FULL);
            dateTimePatterns[DateFormat.SHORT] = localeData.getTimeFormat(DateFormat.SHORT);
            dateTimePatterns[DateFormat.MEDIUM] = localeData.getTimeFormat(DateFormat.MEDIUM);
            dateTimePatterns[DateFormat.LONG] = localeData.getTimeFormat(DateFormat.LONG);
            dateTimePatterns[DateFormat.FULL] = localeData.getTimeFormat(DateFormat.FULL);
            dateTimePatterns[8] = "{0} {1}";
            /* update cache */
            cachedLocaleData.putIfAbsent(loc, dateTimePatterns);
        }
        formatData = DateFormatSymbols.getInstanceRef(loc);
        if ((timeStyle >= 0) && (dateStyle >= 0)) {
            Object[] dateTimeArgs = {dateTimePatterns[dateStyle + 4], dateTimePatterns[timeStyle]};
            pattern = MessageFormat.format(dateTimePatterns[8], dateTimeArgs);
        }
        else if (timeStyle >= 0) {
            pattern = dateTimePatterns[timeStyle];
        }
        else if (dateStyle >= 0) {
            pattern = dateTimePatterns[dateStyle + 4];
        }
        else {
            throw new IllegalArgumentException("No date or time style specified");
        }

        initialize(loc);
    }

    /* Initialize compiledPattern and numberFormat fields */
    private void initialize(Locale loc) {
        // Verify and compile the given pattern.
        compiledPattern = compile(pattern);

        /* try the cache first */
        numberFormat = cachedNumberFormatData.get(loc);
        if (numberFormat == null) { /* cache miss */
            numberFormat = NumberFormat.getIntegerInstance(loc);
            numberFormat.setGroupingUsed(false);

            /* update cache */
            cachedNumberFormatData.putIfAbsent(loc, numberFormat);
        }
        numberFormat = (NumberFormat) numberFormat.clone();

        initializeDefaultCentury();
    }

    private void initializeCalendar(Locale loc) {
        if (calendar == null) {
            assert loc != null;
            // The format object must be constructed using the symbols for this zone.
            // However, the calendar should use the current default TimeZone.
            // If this is not contained in the locale zone strings, then the zone
            // will be formatted using generic GMT+/-H:MM nomenclature.
            calendar = Calendar.getInstance(loc);
        }
    }

    /**
     * Returns the compiled form of the given pattern. The syntax of
     * the compiled pattern is:
     * <blockquote>
     * CompiledPattern:
     *     EntryList
     * EntryList:
     *     Entry
     *     EntryList Entry
     * Entry:
     *     TagField
     *     TagField data
     * TagField:
     *     Tag Length
     *     TaggedData
     * Tag:
     *     pattern_char_index
     *     TAG_QUOTE_CHARS
     * Length:
     *     short_length
     *     long_length
     * TaggedData:
     *     TAG_QUOTE_ASCII_CHAR ascii_char
     *
     * </blockquote>
     *
     * where `short_length' is an 8-bit unsigned integer between 0 and
     * 254.  `long_length' is a sequence of an 8-bit integer 255 and a
     * 32-bit signed integer value which is split into upper and lower
     * 16-bit fields in two char's. `pattern_char_index' is an 8-bit
     * integer between 0 and 18. `ascii_char' is an 7-bit ASCII
     * character value. `data' depends on its Tag value.
     * <p>
     * If Length is short_length, Tag and short_length are packed in a
     * single char, as illustrated below.
     * <blockquote>
     *     char[0] = (Tag << 8) | short_length;
     * </blockquote>
     *
     * If Length is long_length, Tag and 255 are packed in the first
     * char and a 32-bit integer, as illustrated below.
     * <blockquote>
     *     char[0] = (Tag << 8) | 255;
     *     char[1] = (char) (long_length >>> 16);
     *     char[2] = (char) (long_length & 0xffff);
     * </blockquote>
     * <p>
     * If Tag is a pattern_char_index, its Length is the number of
     * pattern characters. For example, if the given pattern is
     * "yyyy", Tag is 1 and Length is 4, followed by no data.
     * <p>
     * If Tag is TAG_QUOTE_CHARS, its Length is the number of char's
     * following the TagField. For example, if the given pattern is
     * "'o''clock'", Length is 7 followed by a char sequence of
     * <code>o&nbs;'&nbs;c&nbs;l&nbs;o&nbs;c&nbs;k</code>.
     * <p>
     * TAG_QUOTE_ASCII_CHAR is a special tag and has an ASCII
     * character in place of Length. For example, if the given pattern
     * is "'o'", the TaggedData entry is
     * <code>((TAG_QUOTE_ASCII_CHAR&nbs;<<&nbs;8)&nbs;|&nbs;'o')</code>.
     *
     * @exception NullPointerException if the given pattern is null
     * @exception IllegalArgumentException if the given pattern is invalid
     */
    private char[] compile(String pattern) {
        int length = pattern.length();
        boolean inQuote = false;
        StringBuilder compiledCode = new StringBuilder(length * 2);
        StringBuilder tmpBuffer = null;
        // BEGIN Android-removed: App compat for formatting pattern letter M.
        // See forceStandaloneForm field
        /*
        int count = 0, tagcount = 0;
        int lastTag = -1, prevTag = -1;
        */
        int count = 0;
        int lastTag = -1;
        // END Android-removed: App compat for formatting pattern letter M.

        for (int i = 0; i < length; i++) {
            char c = pattern.charAt(i);

            if (c == '\'') {
                // '' is treated as a single quote regardless of being
                // in a quoted section.
                if ((i + 1) < length) {
                    c = pattern.charAt(i + 1);
                    if (c == '\'') {
                        i++;
                        if (count != 0) {
                            encode(lastTag, count, compiledCode);
                            // BEGIN Android-removed: App compat for formatting pattern letter M.
                            // See forceStandaloneForm field
                            /*
                            tagcount++;
                            prevTag = lastTag;
                            */
                            // END Android-removed: App compat for formatting pattern letter M.
                            lastTag = -1;
                            count = 0;
                        }
                        if (inQuote) {
                            tmpBuffer.append(c);
                        } else {
                            compiledCode.append((char)(TAG_QUOTE_ASCII_CHAR << 8 | c));
                        }
                        continue;
                    }
                }
                if (!inQuote) {
                    if (count != 0) {
                        encode(lastTag, count, compiledCode);
                        // BEGIN Android-removed: App compat for formatting pattern letter M.
                        // See forceStandaloneForm field
                        /*
                        tagcount++;
                        prevTag = lastTag;
                        */
                        // END Android-removed: App compat for formatting pattern letter M.
                        lastTag = -1;
                        count = 0;
                    }
                    if (tmpBuffer == null) {
                        tmpBuffer = new StringBuilder(length);
                    } else {
                        tmpBuffer.setLength(0);
                    }
                    inQuote = true;
                } else {
                    int len = tmpBuffer.length();
                    if (len == 1) {
                        char ch = tmpBuffer.charAt(0);
                        if (ch < 128) {
                            compiledCode.append((char)(TAG_QUOTE_ASCII_CHAR << 8 | ch));
                        } else {
                            compiledCode.append((char)(TAG_QUOTE_CHARS << 8 | 1));
                            compiledCode.append(ch);
                        }
                    } else {
                        encode(TAG_QUOTE_CHARS, len, compiledCode);
                        compiledCode.append(tmpBuffer);
                    }
                    inQuote = false;
                }
                continue;
            }
            if (inQuote) {
                tmpBuffer.append(c);
                continue;
            }
            if (!(c >= 'a' && c <= 'z' || c >= 'A' && c <= 'Z')) {
                if (count != 0) {
                    encode(lastTag, count, compiledCode);
                    // BEGIN Android-removed: App compat for formatting pattern letter M.
                    // See forceStandaloneForm field
                    /*
                    tagcount++;
                    prevTag = lastTag;
                    */
                    // END Android-removed: App compat for formatting pattern letter M.
                    lastTag = -1;
                    count = 0;
                }
                if (c < 128) {
                    // In most cases, c would be a delimiter, such as ':'.
                    compiledCode.append((char)(TAG_QUOTE_ASCII_CHAR << 8 | c));
                } else {
                    // Take any contiguous non-ASCII alphabet characters and
                    // put them in a single TAG_QUOTE_CHARS.
                    int j;
                    for (j = i + 1; j < length; j++) {
                        char d = pattern.charAt(j);
                        if (d == '\'' || (d >= 'a' && d <= 'z' || d >= 'A' && d <= 'Z')) {
                            break;
                        }
                    }
                    compiledCode.append((char)(TAG_QUOTE_CHARS << 8 | (j - i)));
                    for (; i < j; i++) {
                        compiledCode.append(pattern.charAt(i));
                    }
                    i--;
                }
                continue;
            }

            int tag;
            if ((tag = DateFormatSymbols.patternChars.indexOf(c)) == -1) {
                throw new IllegalArgumentException("Illegal pattern character " +
                                                   "'" + c + "'");
            }
            if (lastTag == -1 || lastTag == tag) {
                lastTag = tag;
                count++;
                continue;
            }
            encode(lastTag, count, compiledCode);
            // BEGIN Android-removed: App compat for formatting pattern letter M.
            // See forceStandaloneForm field
            /*
            tagcount++;
            prevTag = lastTag;
            */
            // END Android-removed: App compat for formatting pattern letter M.
            lastTag = tag;
            count = 1;
        }

        if (inQuote) {
            throw new IllegalArgumentException("Unterminated quote");
        }

        if (count != 0) {
            encode(lastTag, count, compiledCode);
            // BEGIN Android-removed: App compat for formatting pattern letter M.
            // See forceStandaloneForm field
            /*
            tagcount++;
            prevTag = lastTag;
            */
            // END Android-removed: App compat for formatting pattern letter M.
        }

        // Android-removed: App compat for formatting pattern letter M.
        // See forceStandaloneForm field
        // forceStandaloneForm = (tagcount == 1 && prevTag == PATTERN_MONTH);

        // Copy the compiled pattern to a char array
        int len = compiledCode.length();
        char[] r = new char[len];
        compiledCode.getChars(0, len, r, 0);
        return r;
    }

    /**
     * Encodes the given tag and length and puts encoded char(s) into buffer.
     */
    private static void encode(int tag, int length, StringBuilder buffer) {
        if (tag == PATTERN_ISO_ZONE && length >= 4) {
            throw new IllegalArgumentException("invalid ISO 8601 format: length=" + length);
        }
        if (length < 255) {
            buffer.append((char)(tag << 8 | length));
        } else {
            buffer.append((char)((tag << 8) | 0xff));
            buffer.append((char)(length >>> 16));
            buffer.append((char)(length & 0xffff));
        }
    }

    /* Initialize the fields we use to disambiguate ambiguous years. Separate
     * so we can call it from readObject().
     */
    private void initializeDefaultCentury() {
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.add( Calendar.YEAR, -80 );
        parseAmbiguousDatesAsAfter(calendar.getTime());
    }

    /* Define one-century window into which to disambiguate dates using
     * two-digit years.
     */
    private void parseAmbiguousDatesAsAfter(Date startDate) {
        defaultCenturyStart = startDate;
        calendar.setTime(startDate);
        defaultCenturyStartYear = calendar.get(Calendar.YEAR);
    }

    /**
     * Sets the 100-year period 2-digit years will be interpreted as being in
     * to begin on the date the user specifies.
     *
     * @param startDate During parsing, two digit years will be placed in the range
     * <code>startDate</code> to <code>startDate + 100 years</code>.
     * @see #get2DigitYearStart
     * @throws NullPointerException if {@code startDate} is {@code null}.
     * @since 1.2
     */
    public void set2DigitYearStart(Date startDate) {
        parseAmbiguousDatesAsAfter(new Date(startDate.getTime()));
    }

    /**
     * Returns the beginning date of the 100-year period 2-digit years are interpreted
     * as being within.
     *
     * @return the start of the 100-year period into which two digit years are
     * parsed
     * @see #set2DigitYearStart
     * @since 1.2
     */
    public Date get2DigitYearStart() {
        return (Date) defaultCenturyStart.clone();
    }

    /**
     * Formats the given <code>Date</code> into a date/time string and appends
     * the result to the given <code>StringBuffer</code>.
     *
     * @param date the date-time value to be formatted into a date-time string.
     * @param toAppendTo where the new date-time text is to be appended.
     * @param pos keeps track on the position of the field within
     * the returned string. For example, given a date-time text
     * {@code "1996.07.10 AD at 15:08:56 PDT"}, if the given {@code fieldPosition}
     * is {@link DateFormat#YEAR_FIELD}, the begin index and end index of
     * {@code fieldPosition} will be set to 0 and 4, respectively.
     * Notice that if the same date-time field appears more than once in a
     * pattern, the {@code fieldPosition} will be set for the first occurrence
     * of that date-time field. For instance, formatting a {@code Date} to the
     * date-time string {@code "1 PM PDT (Pacific Daylight Time)"} using the
     * pattern {@code "h a z (zzzz)"} and the alignment field
     * {@link DateFormat#TIMEZONE_FIELD}, the begin index and end index of
     * {@code fieldPosition} will be set to 5 and 8, respectively, for the
     * first occurrence of the timezone pattern character {@code 'z'}.
     * @return the formatted date-time string.
     * @exception NullPointerException if any of the parameters is {@code null}.
     */
    @Override
    public StringBuffer format(Date date, StringBuffer toAppendTo,
                               FieldPosition pos)
    {
        pos.beginIndex = pos.endIndex = 0;
        return format(date, toAppendTo, pos.getFieldDelegate());
    }

    // Called from Format after creating a FieldDelegate
    private StringBuffer format(Date date, StringBuffer toAppendTo,
                                FieldDelegate delegate) {
        // Convert input date to time field list
        calendar.setTime(date);

        boolean useDateFormatSymbols = useDateFormatSymbols();

        for (int i = 0; i < compiledPattern.length; ) {
            int tag = compiledPattern[i] >>> 8;
            int count = compiledPattern[i++] & 0xff;
            if (count == 255) {
                count = compiledPattern[i++] << 16;
                count |= compiledPattern[i++];
            }

            switch (tag) {
            case TAG_QUOTE_ASCII_CHAR:
                toAppendTo.append((char)count);
                break;

            case TAG_QUOTE_CHARS:
                toAppendTo.append(compiledPattern, i, count);
                i += count;
                break;

            default:
                subFormat(tag, count, delegate, toAppendTo, useDateFormatSymbols);
                break;
            }
        }
        return toAppendTo;
    }

    /**
     * Formats an Object producing an <code>AttributedCharacterIterator</code>.
     * You can use the returned <code>AttributedCharacterIterator</code>
     * to build the resulting String, as well as to determine information
     * about the resulting String.
     * <p>
     * Each attribute key of the AttributedCharacterIterator will be of type
     * <code>DateFormat.Field</code>, with the corresponding attribute value
     * being the same as the attribute key.
     *
     * @exception NullPointerException if obj is null.
     * @exception IllegalArgumentException if the Format cannot format the
     *            given object, or if the Format's pattern string is invalid.
     * @param obj The object to format
     * @return AttributedCharacterIterator describing the formatted value.
     * @since 1.4
     */
    @Override
    public AttributedCharacterIterator formatToCharacterIterator(Object obj) {
        StringBuffer sb = new StringBuffer();
        CharacterIteratorFieldDelegate delegate = new
                         CharacterIteratorFieldDelegate();

        if (obj instanceof Date) {
            format((Date)obj, sb, delegate);
        }
        else if (obj instanceof Number) {
            format(new Date(((Number)obj).longValue()), sb, delegate);
        }
        else if (obj == null) {
            throw new NullPointerException(
                   "formatToCharacterIterator must be passed non-null object");
        }
        else {
            throw new IllegalArgumentException(
                             "Cannot format given Object as a Date");
        }
        return delegate.getIterator(sb.toString());
    }

    // Map index into pattern character string to Calendar field number
    private static final int[] PATTERN_INDEX_TO_CALENDAR_FIELD = {
        Calendar.ERA,
        Calendar.YEAR,
        Calendar.MONTH,
        Calendar.DATE,
        Calendar.HOUR_OF_DAY,
        Calendar.HOUR_OF_DAY,
        Calendar.MINUTE,
        Calendar.SECOND,
        Calendar.MILLISECOND,
        Calendar.DAY_OF_WEEK,
        Calendar.DAY_OF_YEAR,
        Calendar.DAY_OF_WEEK_IN_MONTH,
        Calendar.WEEK_OF_YEAR,
        Calendar.WEEK_OF_MONTH,
        Calendar.AM_PM,
        Calendar.HOUR,
        Calendar.HOUR,
        Calendar.ZONE_OFFSET,
        Calendar.ZONE_OFFSET,
        CalendarBuilder.WEEK_YEAR,         // Pseudo Calendar field
        CalendarBuilder.ISO_DAY_OF_WEEK,   // Pseudo Calendar field
        Calendar.ZONE_OFFSET,
        // 'L' and 'c',
        Calendar.MONTH,
        // Android-added: 'c' for standalone day of week.
        Calendar.DAY_OF_WEEK,
        // Android-added: Support for 'b'/'B' (day period). Calendar.AM_PM is just used as a
        // placeholder in the absence of full support for day period.
        Calendar.AM_PM,
        Calendar.AM_PM
    };

    // Map index into pattern character string to DateFormat field number
    private static final int[] PATTERN_INDEX_TO_DATE_FORMAT_FIELD = {
        DateFormat.ERA_FIELD,
        DateFormat.YEAR_FIELD,
        DateFormat.MONTH_FIELD,
        DateFormat.DATE_FIELD,
        DateFormat.HOUR_OF_DAY1_FIELD,
        DateFormat.HOUR_OF_DAY0_FIELD,
        DateFormat.MINUTE_FIELD,
        DateFormat.SECOND_FIELD,
        DateFormat.MILLISECOND_FIELD,
        DateFormat.DAY_OF_WEEK_FIELD,
        DateFormat.DAY_OF_YEAR_FIELD,
        DateFormat.DAY_OF_WEEK_IN_MONTH_FIELD,
        DateFormat.WEEK_OF_YEAR_FIELD,
        DateFormat.WEEK_OF_MONTH_FIELD,
        DateFormat.AM_PM_FIELD,
        DateFormat.HOUR1_FIELD,
        DateFormat.HOUR0_FIELD,
        DateFormat.TIMEZONE_FIELD,
        DateFormat.TIMEZONE_FIELD,
        DateFormat.YEAR_FIELD,
        DateFormat.DAY_OF_WEEK_FIELD,
        DateFormat.TIMEZONE_FIELD,
        // 'L' and 'c',
        DateFormat.MONTH_FIELD,
        // Android-added: 'c' for standalone day of week.
        DateFormat.DAY_OF_WEEK_FIELD,
        // Android-added: Support for 'b'/'B' (day period). DateFormat.AM_PM_FIELD is just used as a
        // placeholder in the absence of full support for day period.
        DateFormat.AM_PM_FIELD,
        DateFormat.AM_PM_FIELD
    };

    // Maps from DecimalFormatSymbols index to Field constant
    private static final Field[] PATTERN_INDEX_TO_DATE_FORMAT_FIELD_ID = {
        Field.ERA,
        Field.YEAR,
        Field.MONTH,
        Field.DAY_OF_MONTH,
        Field.HOUR_OF_DAY1,
        Field.HOUR_OF_DAY0,
        Field.MINUTE,
        Field.SECOND,
        Field.MILLISECOND,
        Field.DAY_OF_WEEK,
        Field.DAY_OF_YEAR,
        Field.DAY_OF_WEEK_IN_MONTH,
        Field.WEEK_OF_YEAR,
        Field.WEEK_OF_MONTH,
        Field.AM_PM,
        Field.HOUR1,
        Field.HOUR0,
        Field.TIME_ZONE,
        Field.TIME_ZONE,
        Field.YEAR,
        Field.DAY_OF_WEEK,
        Field.TIME_ZONE,
        // 'L' and 'c',
        Field.MONTH,
        // Android-added: 'c' for standalone day of week.
        Field.DAY_OF_WEEK,
        // Android-added: Support for 'b'/'B' (day period). Field.AM_PM is just used as a
        // placeholder in the absence of full support for day period.
        Field.AM_PM,
        Field.AM_PM
    };

    /**
     * Private member function that does the real date/time formatting.
     */
    private void subFormat(int patternCharIndex, int count,
                           FieldDelegate delegate, StringBuffer buffer,
                           boolean useDateFormatSymbols)
    {
        int     maxIntCount = Integer.MAX_VALUE;
        String  current = null;
        int     beginOffset = buffer.length();

        int field = PATTERN_INDEX_TO_CALENDAR_FIELD[patternCharIndex];
        int value;
        if (field == CalendarBuilder.WEEK_YEAR) {
            if (calendar.isWeekDateSupported()) {
                value = calendar.getWeekYear();
            } else {
                // use calendar year 'y' instead
                patternCharIndex = PATTERN_YEAR;
                field = PATTERN_INDEX_TO_CALENDAR_FIELD[patternCharIndex];
                value = calendar.get(field);
            }
        } else if (field == CalendarBuilder.ISO_DAY_OF_WEEK) {
            value = CalendarBuilder.toISODayOfWeek(calendar.get(Calendar.DAY_OF_WEEK));
        } else {
            value = calendar.get(field);
        }

        int style = (count >= 4) ? Calendar.LONG : Calendar.SHORT;
        if (!useDateFormatSymbols && field != CalendarBuilder.ISO_DAY_OF_WEEK) {
            current = calendar.getDisplayName(field, style, locale);
        }

        // Note: zeroPaddingNumber() assumes that maxDigits is either
        // 2 or maxIntCount. If we make any changes to this,
        // zeroPaddingNumber() must be fixed.

        switch (patternCharIndex) {
        case PATTERN_ERA: // 'G'
            if (useDateFormatSymbols) {
                String[] eras = formatData.getEras();
                if (value < eras.length) {
                    current = eras[value];
                }
            }
            if (current == null) {
                current = "";
            }
            break;

        case PATTERN_WEEK_YEAR: // 'Y'
        case PATTERN_YEAR:      // 'y'
            if (calendar instanceof GregorianCalendar) {
                if (count != 2) {
                    zeroPaddingNumber(value, count, maxIntCount, buffer);
                } else {
                    zeroPaddingNumber(value, 2, 2, buffer);
                } // clip 1996 to 96
            } else {
                if (current == null) {
                    zeroPaddingNumber(value, style == Calendar.LONG ? 1 : count,
                                      maxIntCount, buffer);
                }
            }
            break;

        case PATTERN_MONTH:            // 'M' (context seinsive)
            // BEGIN Android-changed: formatMonth() method to format using ICU data.
            /*
            if (useDateFormatSymbols) {
                String[] months;
                if (count >= 4) {
                    months = formatData.getMonths();
                    current = months[value];
                } else if (count == 3) {
                    months = formatData.getShortMonths();
                    current = months[value];
                }
            } else {
                if (count < 3) {
                    current = null;
                } else if (forceStandaloneForm) {
                    current = calendar.getDisplayName(field, style | 0x8000, locale);
                    if (current == null) {
                        current = calendar.getDisplayName(field, style, locale);
                    }
                }
            }
            if (current == null) {
                zeroPaddingNumber(value+1, count, maxIntCount, buffer);
            }
            */
            current = formatMonth(count, value, maxIntCount, buffer, useDateFormatSymbols,
                false /* standalone */, field, style);
            // END Android-changed: formatMonth() method to format using ICU data.
            break;

        case PATTERN_MONTH_STANDALONE: // 'L'
            // BEGIN Android-changed: formatMonth() method to format using ICU data.
            /*
            assert current == null;
            if (locale == null) {
                String[] months;
                if (count >= 4) {
                    months = formatData.getMonths();
                    current = months[value];
                } else if (count == 3) {
                    months = formatData.getShortMonths();
                    current = months[value];
                }
            } else {
                if (count >= 3) {
                    current = calendar.getDisplayName(field, style | 0x8000, locale);
                }
            }
            if (current == null) {
                zeroPaddingNumber(value+1, count, maxIntCount, buffer);
            }
            */
            current = formatMonth(count, value, maxIntCount, buffer, useDateFormatSymbols,
                   true /* standalone */, field, style);
            // END Android-changed: formatMonth() method to format using ICU data.
            break;

        case PATTERN_HOUR_OF_DAY1: // 'k' 1-based.  eg, 23:59 + 1 hour =>> 24:59
            if (current == null) {
                if (value == 0) {
                    zeroPaddingNumber(calendar.getMaximum(Calendar.HOUR_OF_DAY) + 1,
                                      count, maxIntCount, buffer);
                } else {
                    zeroPaddingNumber(value, count, maxIntCount, buffer);
                }
            }
            break;

        case PATTERN_DAY_OF_WEEK: // 'E'
            // BEGIN Android-removed: App compat for formatting pattern letter M.
            // See forceStandaloneForm field
            /*
            if (useDateFormatSymbols) {
                String[] weekdays;
                if (count >= 4) {
                    weekdays = formatData.getWeekdays();
                    current = weekdays[value];
                } else { // count < 4, use abbreviated form if exists
                    weekdays = formatData.getShortWeekdays();
                    current = weekdays[value];
                }
            }
            */
            if (current == null) {
                current = formatWeekday(count, value, useDateFormatSymbols, false /* standalone */);
            }
            // END Android-removed: App compat for formatting pattern letter M.
            break;

        // BEGIN Android-added: support for 'c' (standalone day of week).
        case PATTERN_STANDALONE_DAY_OF_WEEK: // 'c'
            if (current == null) {
                current = formatWeekday(count, value, useDateFormatSymbols, true /* standalone */);
            }
            break;
        // END Android-added: support for 'c' (standalone day of week).

        case PATTERN_AM_PM:    // 'a'
            if (useDateFormatSymbols) {
                String[] ampm = formatData.getAmPmStrings();
                current = ampm[value];
            }
            break;

        // Android-added: Ignore 'b' and 'B' introduced in CLDR 32+ pattern data. http://b/68139386
        // Not currently supported here.
        case PATTERN_DAY_PERIOD:
        case PATTERN_FLEXIBLE_DAY_PERIOD:
            current = "";
            break;

        case PATTERN_HOUR1:    // 'h' 1-based.  eg, 11PM + 1 hour =>> 12 AM
            if (current == null) {
                if (value == 0) {
                    zeroPaddingNumber(calendar.getLeastMaximum(Calendar.HOUR) + 1,
                                      count, maxIntCount, buffer);
                } else {
                    zeroPaddingNumber(value, count, maxIntCount, buffer);
                }
            }
            break;

        case PATTERN_ZONE_NAME: // 'z'
            if (current == null) {
                // BEGIN Android-changed: Format time zone name using ICU.
                /*
                if (formatData.locale == null || formatData.isZoneStringsSet) {
                    int zoneIndex =
                        formatData.getZoneIndex(calendar.getTimeZone().getID());
                    if (zoneIndex == -1) {
                        value = calendar.get(Calendar.ZONE_OFFSET) +
                            calendar.get(Calendar.DST_OFFSET);
                        buffer.append(ZoneInfoFile.toCustomID(value));
                    } else {
                        int index = (calendar.get(Calendar.DST_OFFSET) == 0) ? 1: 3;
                        if (count < 4) {
                            // Use the short name
                            index++;
                        }
                        String[][] zoneStrings = formatData.getZoneStringsWrapper();
                        buffer.append(zoneStrings[zoneIndex][index]);
                    }
                } else {
                    TimeZone tz = calendar.getTimeZone();
                    boolean daylight = (calendar.get(Calendar.DST_OFFSET) != 0);
                    int tzstyle = (count < 4 ? TimeZone.SHORT : TimeZone.LONG);
                    buffer.append(tz.getDisplayName(daylight, tzstyle, formatData.locale));
                }
                */
                TimeZone tz = calendar.getTimeZone();
                boolean daylight = (calendar.get(Calendar.DST_OFFSET) != 0);
                int tzstyle = count < 4 ? TimeZone.SHORT : TimeZone.LONG;
                String zoneString = tz.getDisplayName(daylight, tzstyle, formatData.locale);
                if (zoneString != null) {
                    buffer.append(zoneString);
                } else {
                    int offsetMillis = calendar.get(Calendar.ZONE_OFFSET) +
                        calendar.get(Calendar.DST_OFFSET);
                    buffer.append(TimeZone.createGmtOffsetString(true, true, offsetMillis));
                }
                // END Android-changed: Format time zone name using ICU.
            }
            break;

        case PATTERN_ZONE_VALUE: // 'Z' ("-/+hhmm" form)
            // BEGIN Android-changed: Use shared code in TimeZone for zone offset string.
            /*
            value = (calendar.get(Calendar.ZONE_OFFSET) +
                     calendar.get(Calendar.DST_OFFSET)) / 60000;

            int width = 4;
            if (value >= 0) {
                buffer.append('+');
            } else {
                width++;
            }

            int num = (value / 60) * 100 + (value % 60);
            CalendarUtils.sprintf0d(buffer, num, width);
            */
            value = calendar.get(Calendar.ZONE_OFFSET) + calendar.get(Calendar.DST_OFFSET);
            final boolean includeSeparator = (count >= 4);
            final boolean includeGmt = (count == 4);
            buffer.append(TimeZone.createGmtOffsetString(includeGmt, includeSeparator, value));

            break;
            // END Android-changed: Use shared code in TimeZone for zone offset string.

        case PATTERN_ISO_ZONE:   // 'X'
            value = calendar.get(Calendar.ZONE_OFFSET)
                    + calendar.get(Calendar.DST_OFFSET);

            if (value == 0) {
                buffer.append('Z');
                break;
            }

            value /=  60000;
            if (value >= 0) {
                buffer.append('+');
            } else {
                buffer.append('-');
                value = -value;
            }

            CalendarUtils.sprintf0d(buffer, value / 60, 2);
            if (count == 1) {
                break;
            }

            if (count == 3) {
                buffer.append(':');
            }
            CalendarUtils.sprintf0d(buffer, value % 60, 2);
            break;
        // BEGIN Android-added: Better UTS#35 conformity for fractional seconds.
        case PATTERN_MILLISECOND: // 'S'
            // Fractional seconds must be treated specially. We must always convert the parsed
            // value into a fractional second [0, 1) and then widen it out to the appropriate
            // formatted size. For example, an initial value of 789 will be converted
            // 0.789 and then become ".7" (S) or ".78" (SS) or "0.789" (SSS) or "0.7890" (SSSS)
            // in the resulting formatted output.
            if (current == null) {
                value = (int) (((double) value / 1000) * Math.pow(10, count));
                zeroPaddingNumber(value, count, count, buffer);
            }
            break;
        // END Android-added: Better UTS#35 conformity for fractional seconds.

        default:
     // case PATTERN_DAY_OF_MONTH:         // 'd'
     // case PATTERN_HOUR_OF_DAY0:         // 'H' 0-based.  eg, 23:59 + 1 hour =>> 00:59
     // case PATTERN_MINUTE:               // 'm'
     // case PATTERN_SECOND:               // 's'
     // Android-removed: PATTERN_MILLISECONDS handled in an explicit case above.
     //// case PATTERN_MILLISECOND:          // 'S'
     // case PATTERN_DAY_OF_YEAR:          // 'D'
     // case PATTERN_DAY_OF_WEEK_IN_MONTH: // 'F'
     // case PATTERN_WEEK_OF_YEAR:         // 'w'
     // case PATTERN_WEEK_OF_MONTH:        // 'W'
     // case PATTERN_HOUR0:                // 'K' eg, 11PM + 1 hour =>> 0 AM
     // case PATTERN_ISO_DAY_OF_WEEK:      // 'u' pseudo field, Monday = 1, ..., Sunday = 7
            if (current == null) {
                zeroPaddingNumber(value, count, maxIntCount, buffer);
            }
            break;
        } // switch (patternCharIndex)

        if (current != null) {
            buffer.append(current);
        }

        int fieldID = PATTERN_INDEX_TO_DATE_FORMAT_FIELD[patternCharIndex];
        Field f = PATTERN_INDEX_TO_DATE_FORMAT_FIELD_ID[patternCharIndex];

        delegate.formatted(fieldID, f, f, beginOffset, buffer.length(), buffer);
    }

    // BEGIN Android-added: formatWeekday() and formatMonth() methods to format using ICU data.
    private String formatWeekday(int count, int value, boolean useDateFormatSymbols,
                                 boolean standalone) {
        if (useDateFormatSymbols) {
            final String[] weekdays;
            if (count == 4) {
                weekdays = standalone ? formatData.getStandAloneWeekdays() : formatData.getWeekdays();
            } else if (count == 5) {
                weekdays =
                        standalone ? formatData.getTinyStandAloneWeekdays() : formatData.getTinyWeekdays();

            } else { // count < 4, use abbreviated form if exists
                weekdays = standalone ? formatData.getShortStandAloneWeekdays() : formatData.getShortWeekdays();
            }

            return weekdays[value];
        }

        return null;
    }

    private String formatMonth(int count, int value, int maxIntCount, StringBuffer buffer,
                               boolean useDateFormatSymbols, boolean standalone,
                               int field, int style) {
        String current = null;
        if (useDateFormatSymbols) {
            final String[] months;
            if (count == 4) {
                months = standalone ? formatData.getStandAloneMonths() : formatData.getMonths();
            } else if (count == 5) {
                months = standalone ? formatData.getTinyStandAloneMonths() : formatData.getTinyMonths();
            } else if (count == 3) {
                months = standalone ? formatData.getShortStandAloneMonths() : formatData.getShortMonths();
            } else {
                months = null;
            }

            if (months != null) {
                current = months[value];
            }
        } else {
            if (count < 3) {
                current = null;
            }
        }

        if (current == null) {
            zeroPaddingNumber(value+1, count, maxIntCount, buffer);
        }

        return current;
    }
    // END Android-added: formatWeekday() and formatMonth() methods to format using ICU data.

    /**
     * Formats a number with the specified minimum and maximum number of digits.
     */
    private void zeroPaddingNumber(int value, int minDigits, int maxDigits, StringBuffer buffer)
    {
        // Optimization for 1, 2 and 4 digit numbers. This should
        // cover most cases of formatting date/time related items.
        // Note: This optimization code assumes that maxDigits is
        // either 2 or Integer.MAX_VALUE (maxIntCount in format()).
        try {
            if (zeroDigit == 0) {
                zeroDigit = ((DecimalFormat)numberFormat).getDecimalFormatSymbols().getZeroDigit();
            }
            if (value >= 0) {
                if (value < 100 && minDigits >= 1 && minDigits <= 2) {
                    if (value < 10) {
                        if (minDigits == 2) {
                            buffer.append(zeroDigit);
                        }
                        buffer.append((char)(zeroDigit + value));
                    } else {
                        buffer.append((char)(zeroDigit + value / 10));
                        buffer.append((char)(zeroDigit + value % 10));
                    }
                    return;
                } else if (value >= 1000 && value < 10000) {
                    if (minDigits == 4) {
                        buffer.append((char)(zeroDigit + value / 1000));
                        value %= 1000;
                        buffer.append((char)(zeroDigit + value / 100));
                        value %= 100;
                        buffer.append((char)(zeroDigit + value / 10));
                        buffer.append((char)(zeroDigit + value % 10));
                        return;
                    }
                    if (minDigits == 2 && maxDigits == 2) {
                        zeroPaddingNumber(value % 100, 2, 2, buffer);
                        return;
                    }
                }
            }
        } catch (Exception e) {
        }

        numberFormat.setMinimumIntegerDigits(minDigits);
        numberFormat.setMaximumIntegerDigits(maxDigits);
        numberFormat.format((long)value, buffer, DontCareFieldPosition.INSTANCE);
    }


    /**
     * Parses text from a string to produce a <code>Date</code>.
     * <p>
     * The method attempts to parse text starting at the index given by
     * <code>pos</code>.
     * If parsing succeeds, then the index of <code>pos</code> is updated
     * to the index after the last character used (parsing does not necessarily
     * use all characters up to the end of the string), and the parsed
     * date is returned. The updated <code>pos</code> can be used to
     * indicate the starting point for the next call to this method.
     * If an error occurs, then the index of <code>pos</code> is not
     * changed, the error index of <code>pos</code> is set to the index of
     * the character where the error occurred, and null is returned.
     *
     * <p>This parsing operation uses the {@link DateFormat#calendar
     * calendar} to produce a {@code Date}. All of the {@code
     * calendar}'s date-time fields are {@linkplain Calendar#clear()
     * cleared} before parsing, and the {@code calendar}'s default
     * values of the date-time fields are used for any missing
     * date-time information. For example, the year value of the
     * parsed {@code Date} is 1970 with {@link GregorianCalendar} if
     * no year value is given from the parsing operation.  The {@code
     * TimeZone} value may be overwritten, depending on the given
     * pattern and the time zone value in {@code text}. Any {@code
     * TimeZone} value that has previously been set by a call to
     * {@link #setTimeZone(java.util.TimeZone) setTimeZone} may need
     * to be restored for further operations.
     *
     * @param text  A <code>String</code>, part of which should be parsed.
     * @param pos   A <code>ParsePosition</code> object with index and error
     *              index information as described above.
     * @return A <code>Date</code> parsed from the string. In case of
     *         error, returns null.
     * @exception NullPointerException if <code>text</code> or <code>pos</code> is null.
     */
    @Override
    public Date parse(String text, ParsePosition pos) {
        // BEGIN Android-changed: extract parseInternal() and avoid modifying timezone during parse.
        // Make sure the timezone associated with this dateformat instance (set via
        // {@code setTimeZone} isn't change as a side-effect of parsing a date.
        final TimeZone tz = getTimeZone();
        try {
            return parseInternal(text, pos);
        } finally {
            setTimeZone(tz);
        }
    }

    private Date parseInternal(String text, ParsePosition pos)
    {
        // END Android-changed: extract parseInternal() and avoid modifying timezone during parse.
        checkNegativeNumberExpression();

        int start = pos.index;
        int oldStart = start;
        int textLength = text.length();

        boolean[] ambiguousYear = {false};

        CalendarBuilder calb = new CalendarBuilder();

        for (int i = 0; i < compiledPattern.length; ) {
            int tag = compiledPattern[i] >>> 8;
            int count = compiledPattern[i++] & 0xff;
            if (count == 255) {
                count = compiledPattern[i++] << 16;
                count |= compiledPattern[i++];
            }

            switch (tag) {
            case TAG_QUOTE_ASCII_CHAR:
                if (start >= textLength || text.charAt(start) != (char)count) {
                    pos.index = oldStart;
                    pos.errorIndex = start;
                    return null;
                }
                start++;
                break;

            case TAG_QUOTE_CHARS:
                while (count-- > 0) {
                    if (start >= textLength || text.charAt(start) != compiledPattern[i++]) {
                        pos.index = oldStart;
                        pos.errorIndex = start;
                        return null;
                    }
                    start++;
                }
                break;

            default:
                // Peek the next pattern to determine if we need to
                // obey the number of pattern letters for
                // parsing. It's required when parsing contiguous
                // digit text (e.g., "20010704") with a pattern which
                // has no delimiters between fields, like "yyyyMMdd".
                boolean obeyCount = false;

                // In Arabic, a minus sign for a negative number is put after
                // the number. Even in another locale, a minus sign can be
                // put after a number using DateFormat.setNumberFormat().
                // If both the minus sign and the field-delimiter are '-',
                // subParse() needs to determine whether a '-' after a number
                // in the given text is a delimiter or is a minus sign for the
                // preceding number. We give subParse() a clue based on the
                // information in compiledPattern.
                boolean useFollowingMinusSignAsDelimiter = false;

                if (i < compiledPattern.length) {
                    int nextTag = compiledPattern[i] >>> 8;
                    int nextCount = compiledPattern[i] & 0xff;
                    obeyCount = shouldObeyCount(nextTag, nextCount);

                    if (hasFollowingMinusSign &&
                        (nextTag == TAG_QUOTE_ASCII_CHAR ||
                         nextTag == TAG_QUOTE_CHARS)) {

                        if (nextTag != TAG_QUOTE_ASCII_CHAR) {
                            nextCount = compiledPattern[i+1];
                        }

                        if (nextCount == minusSign) {
                            useFollowingMinusSignAsDelimiter = true;
                        }
                    }
                }
                start = subParse(text, start, tag, count, obeyCount,
                                 ambiguousYear, pos,
                                 useFollowingMinusSignAsDelimiter, calb);
                if (start < 0) {
                    pos.index = oldStart;
                    return null;
                }
            }
        }

        // At this point the fields of Calendar have been set.  Calendar
        // will fill in default values for missing fields when the time
        // is computed.

        pos.index = start;

        Date parsedDate;
        try {
            parsedDate = calb.establish(calendar).getTime();
            // If the year value is ambiguous,
            // then the two-digit year == the default start year
            if (ambiguousYear[0]) {
                if (parsedDate.before(defaultCenturyStart)) {
                    parsedDate = calb.addYear(100).establish(calendar).getTime();
                }
            }
        }
        // An IllegalArgumentException will be thrown by Calendar.getTime()
        // if any fields are out of range, e.g., MONTH == 17.
        catch (IllegalArgumentException e) {
            pos.errorIndex = start;
            pos.index = oldStart;
            return null;
        }

        return parsedDate;
    }

    /* If the next tag/pattern is a <Numeric_Field> then the parser
     * should consider the count of digits while parsing the contigous digits
     * for the current tag/pattern
     */
    private boolean shouldObeyCount(int tag, int count) {
        switch (tag) {
            case PATTERN_MONTH:
            case PATTERN_MONTH_STANDALONE:
                return count <= 2;
            case PATTERN_YEAR:
            case PATTERN_DAY_OF_MONTH:
            case PATTERN_HOUR_OF_DAY1:
            case PATTERN_HOUR_OF_DAY0:
            case PATTERN_MINUTE:
            case PATTERN_SECOND:
            case PATTERN_MILLISECOND:
            case PATTERN_DAY_OF_YEAR:
            case PATTERN_DAY_OF_WEEK_IN_MONTH:
            case PATTERN_WEEK_OF_YEAR:
            case PATTERN_WEEK_OF_MONTH:
            case PATTERN_HOUR1:
            case PATTERN_HOUR0:
            case PATTERN_WEEK_YEAR:
            case PATTERN_ISO_DAY_OF_WEEK:
                return true;
            default:
                return false;
        }
    }

    /**
     * Private code-size reduction function used by subParse.
     * @param text the time text being parsed.
     * @param start where to start parsing.
     * @param field the date field being parsed.
     * @param data the string array to parsed.
     * @return the new start position if matching succeeded; a negative number
     * indicating matching failure, otherwise.
     */
    private int matchString(String text, int start, int field, String[] data, CalendarBuilder calb)
    {
        int i = 0;
        int count = data.length;

        if (field == Calendar.DAY_OF_WEEK) {
            i = 1;
        }

        // There may be multiple strings in the data[] array which begin with
        // the same prefix (e.g., Cerven and Cervenec (June and July) in Czech).
        // We keep track of the longest match, and return that.  Note that this
        // unfortunately requires us to test all array elements.
        int bestMatchLength = 0, bestMatch = -1;
        for (; i<count; ++i)
        {
            int length = data[i].length();
            // Always compare if we have no match yet; otherwise only compare
            // against potentially better matches (longer strings).
            if (length > bestMatchLength &&
                text.regionMatches(true, start, data[i], 0, length))
            {
                bestMatch = i;
                bestMatchLength = length;
            }

            // BEGIN Android-changed: Handle abbreviated fields that end with a '.'.
            // When the input option ends with a period (usually an abbreviated form), attempt
            // to match all chars up to that period.
            if ((data[i].charAt(length - 1) == '.') &&
                    ((length - 1) > bestMatchLength) &&
                    text.regionMatches(true, start, data[i], 0, length - 1)) {
                bestMatch = i;
                bestMatchLength = (length - 1);
            }
            // END Android-changed: Handle abbreviated fields that end with a '.'.
        }
        if (bestMatch >= 0)
        {
            calb.set(field, bestMatch);
            return start + bestMatchLength;
        }
        return -start;
    }

    /**
     * Performs the same thing as matchString(String, int, int,
     * String[]). This method takes a Map<String, Integer> instead of
     * String[].
     */
    private int matchString(String text, int start, int field,
                            Map<String,Integer> data, CalendarBuilder calb) {
        if (data != null) {
            // TODO: make this default when it's in the spec.
            // BEGIN Android-changed: SortedMap instance lookup optimization in matchString().
            // RI returns not the longest match as matchString(String[]) does. http://b/119913354
            /*
            if (data instanceof SortedMap) {
                for (String name : data.keySet()) {
            */
            if (data instanceof NavigableMap && ((NavigableMap) data).comparator() == null) {
                for (String name : ((NavigableMap<String, Integer>) data).descendingKeySet()) {
            // END Android-changed: SortedMap instance lookup optimization in matchString().
                    if (text.regionMatches(true, start, name, 0, name.length())) {
                        calb.set(field, data.get(name));
                        return start + name.length();
                    }
                }
                return -start;
            }

            String bestMatch = null;

            for (String name : data.keySet()) {
                int length = name.length();
                if (bestMatch == null || length > bestMatch.length()) {
                    if (text.regionMatches(true, start, name, 0, length)) {
                        bestMatch = name;
                    }
                }
            }

            if (bestMatch != null) {
                calb.set(field, data.get(bestMatch));
                return start + bestMatch.length();
            }
        }
        return -start;
    }

    private int matchZoneString(String text, int start, String[] zoneNames) {
        for (int i = 1; i <= 4; ++i) {
            // Checking long and short zones [1 & 2],
            // and long and short daylight [3 & 4].
            String zoneName = zoneNames[i];
            // Android-removed: App compat. Don't retrieve more data when data missing in DFS.
            //  It may have risk of app compat issue, and no significant benefit on Android because
            //  the DFS and TimeZoneNameUtility will both come from ICU / CLDR on Android.
            /*
            if (zoneName.isEmpty()) {
                // fill in by retrieving single name
                zoneName = TimeZoneNameUtility.retrieveDisplayName(
                                zoneNames[0], i >= 3, i % 2, locale);
                zoneNames[i] = zoneName;
            }
            */
            if (text.regionMatches(true, start,
                                   zoneName, 0, zoneName.length())) {
                return i;
            }
        }
        return -1;
    }

    // BEGIN Android-removed: Unused private method matchDSTString.
    /*
    private boolean matchDSTString(String text, int start, int zoneIndex, int standardIndex,
                                   String[][] zoneStrings) {
        int index = standardIndex + 2;
        String zoneName  = zoneStrings[zoneIndex][index];
        if (text.regionMatches(true, start,
                               zoneName, 0, zoneName.length())) {
            return true;
        }
        return false;
    }
    */
    // END Android-removed: Unused private method matchDSTString.

    // BEGIN Android-changed: Parse time zone strings using ICU TimeZoneNames.
    // Note that this change falls back to the upstream zone names parsing code if the zoneStrings
    // for the formatData field has been set by the user. The original code of subParseZoneString
    // can be found in subParseZoneStringFromSymbols().
    /**
     * Parses the string in {@code text} (starting at {@code start}), interpreting it as a time zone
     * name. If a time zone is found, the internal calendar is set to that timezone and the index of
     * the first character after the time zone name is returned. Otherwise, returns {@code 0}.
     * @return the index of the next character to parse or {@code 0} on error.
     */
    private int subParseZoneString(String text, int start, CalendarBuilder calb) {
        boolean useSameName = false; // true if standard and daylight time use the same abbreviation.
        TimeZone currentTimeZone = getTimeZone();

        // At this point, check for named time zones by looking through
        // the locale data from the TimeZoneNames strings.
        // Want to be able to parse both short and long forms.
        int zoneIndex = formatData.getZoneIndex(currentTimeZone.getID());
        TimeZone tz = null;
        String[][] zoneStrings = formatData.getZoneStringsWrapper();
        String[] zoneNames = null;
        int nameIndex = 0;
        if (zoneIndex != -1) {
            zoneNames = zoneStrings[zoneIndex];
            if ((nameIndex = matchZoneString(text, start, zoneNames)) > 0) {
                if (nameIndex <= 2) {
                    // Check if the standard name (abbr) and the daylight name are the same.
                    useSameName = zoneNames[nameIndex].equalsIgnoreCase(zoneNames[nameIndex + 2]);
                }
                tz = TimeZone.getTimeZone(zoneNames[0]);
            }
        }
        if (tz == null) {
            zoneIndex = formatData.getZoneIndex(TimeZone.getDefault().getID());
            if (zoneIndex != -1) {
                zoneNames = zoneStrings[zoneIndex];
                if ((nameIndex = matchZoneString(text, start, zoneNames)) > 0) {
                    if (nameIndex <= 2) {
                        useSameName = zoneNames[nameIndex].equalsIgnoreCase(zoneNames[nameIndex + 2]);
                    }
                    tz = TimeZone.getTimeZone(zoneNames[0]);
                }
            }
        }

        if (tz == null) {
            int len = zoneStrings.length;
            for (int i = 0; i < len; i++) {
                zoneNames = zoneStrings[i];
                if ((nameIndex = matchZoneString(text, start, zoneNames)) > 0) {
                    if (nameIndex <= 2) {
                        useSameName = zoneNames[nameIndex].equalsIgnoreCase(zoneNames[nameIndex + 2]);
                    }
                    tz = TimeZone.getTimeZone(zoneNames[0]);
                    break;
                }
            }
        }
        if (tz != null) { // Matched any ?
            if (!tz.equals(currentTimeZone)) {
                setTimeZone(tz);
            }
            // If the time zone matched uses the same name
            // (abbreviation) for both standard and daylight time,
            // let the time zone in the Calendar decide which one.
            //
            // Also if tz.getDSTSaving() returns 0 for DST, use tz to
            // determine the local time. (6645292)
            int dstAmount = (nameIndex >= 3) ? tz.getDSTSavings() : 0;
            if (!(useSameName || (nameIndex >= 3 && dstAmount == 0))) {
                calb.clear(Calendar.ZONE_OFFSET).set(Calendar.DST_OFFSET, dstAmount);
            }
            return (start + zoneNames[nameIndex].length());
        }
        return 0;
    }

    /**
     * Parses numeric forms of time zone offset, such as "hh:mm", and
     * sets calb to the parsed value.
     *
     * @param text  the text to be parsed
     * @param start the character position to start parsing
     * @param sign  1: positive; -1: negative
     * @param count 0: 'Z' or "GMT+hh:mm" parsing; 1 - 3: the number of 'X's
     * @param colon true - colon required between hh and mm; false - no colon required
     * @param calb  a CalendarBuilder in which the parsed value is stored
     * @return updated parsed position, or its negative value to indicate a parsing error
     */
    private int subParseNumericZone(String text, int start, int sign, int count,
                                    boolean colon, CalendarBuilder calb) {
        int index = start;

      parse:
        try {
            char c = text.charAt(index++);
            // Parse hh
            int hours;
            if (!isDigit(c)) {
                break parse;
            }
            hours = c - '0';
            c = text.charAt(index++);
            if (isDigit(c)) {
                hours = hours * 10 + (c - '0');
            } else {
                // BEGIN Android-removed: Be more tolerant of colon. b/26426526
                /*
                // If no colon in RFC 822 or 'X' (ISO), two digits are
                // required.
                if (count > 0 || !colon) {
                    break parse;
                }
                */
                // END Android-removed: Be more tolerant of colon. b/26426526
                --index;
            }
            if (hours > 23) {
                break parse;
            }
            int minutes = 0;
            if (count != 1) {
                // Proceed with parsing mm
                c = text.charAt(index++);
                // BEGIN Android-changed: Be more tolerant of colon. b/26426526
                // OpenJDK will return an error code if a : is found and colonRequired is false,
                // this will return an error code if a : is not found and colonRequired is true.
                //
                //   colon       | c == ':' | OpenJDK | this
                //   false       |  false   |   ok    |  ok
                //   false       |  true    |  error  |  ok
                //   true        |  false   |   ok    | error
                //   true        |  true    |   ok    |  ok
                /*
                if (colon) {
                    if (c != ':') {
                        break parse;
                    }
                */
                if (c == ':') {
                    c = text.charAt(index++);
                } else if (colon) {
                    break parse;
                }
                // END Android-changed: Be more tolerant of colon. b/26426526
                if (!isDigit(c)) {
                    break parse;
                }
                minutes = c - '0';
                c = text.charAt(index++);
                if (!isDigit(c)) {
                    break parse;
                }
                minutes = minutes * 10 + (c - '0');
                if (minutes > 59) {
                    break parse;
                }
            }
            minutes += hours * 60;
            calb.set(Calendar.ZONE_OFFSET, minutes * MILLIS_PER_MINUTE * sign)
                .set(Calendar.DST_OFFSET, 0);
            return index;
        } catch (IndexOutOfBoundsException e) {
        }
        return  1 - index; // -(index - 1)
    }

    private boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    /**
     * Private member function that converts the parsed date strings into
     * timeFields. Returns -start (for ParsePosition) if failed.
     * @param text the time text to be parsed.
     * @param start where to start parsing.
     * @param patternCharIndex the index of the pattern character.
     * @param count the count of a pattern character.
     * @param obeyCount if true, then the next field directly abuts this one,
     * and we should use the count to know when to stop parsing.
     * @param ambiguousYear return parameter; upon return, if ambiguousYear[0]
     * is true, then a two-digit year was parsed and may need to be readjusted.
     * @param origPos origPos.errorIndex is used to return an error index
     * at which a parse error occurred, if matching failure occurs.
     * @return the new start position if matching succeeded; -1 indicating
     * matching failure, otherwise. In case matching failure occurred,
     * an error index is set to origPos.errorIndex.
     */
    private int subParse(String text, int start, int patternCharIndex, int count,
                         boolean obeyCount, boolean[] ambiguousYear,
                         ParsePosition origPos,
                         boolean useFollowingMinusSignAsDelimiter, CalendarBuilder calb) {
        Number number = null;
        int value = 0;
        ParsePosition pos = new ParsePosition(0);
        pos.index = start;
        if (patternCharIndex == PATTERN_WEEK_YEAR && !calendar.isWeekDateSupported()) {
            // use calendar year 'y' instead
            patternCharIndex = PATTERN_YEAR;
        }
        int field = PATTERN_INDEX_TO_CALENDAR_FIELD[patternCharIndex];

        // If there are any spaces here, skip over them.  If we hit the end
        // of the string, then fail.
        for (;;) {
            if (pos.index >= text.length()) {
                origPos.errorIndex = start;
                return -1;
            }
            char c = text.charAt(pos.index);
            if (c != ' ' && c != '\t') {
                break;
            }
            ++pos.index;
        }
        // Remember the actual start index
        int actualStart = pos.index;

      parsing:
        {
            // We handle a few special cases here where we need to parse
            // a number value.  We handle further, more generic cases below.  We need
            // to handle some of them here because some fields require extra processing on
            // the parsed value.
            if (patternCharIndex == PATTERN_HOUR_OF_DAY1 ||
                patternCharIndex == PATTERN_HOUR1 ||
                (patternCharIndex == PATTERN_MONTH && count <= 2) ||
                (patternCharIndex == PATTERN_MONTH_STANDALONE && count <= 2) ||
                patternCharIndex == PATTERN_YEAR ||
                patternCharIndex == PATTERN_WEEK_YEAR) {
                // It would be good to unify this with the obeyCount logic below,
                // but that's going to be difficult.
                if (obeyCount) {
                    if ((start+count) > text.length()) {
                        break parsing;
                    }
                    number = numberFormat.parse(text.substring(0, start+count), pos);
                } else {
                    number = numberFormat.parse(text, pos);
                }
                if (number == null) {
                    if (patternCharIndex != PATTERN_YEAR || calendar instanceof GregorianCalendar) {
                        break parsing;
                    }
                } else {
                    value = number.intValue();

                    if (useFollowingMinusSignAsDelimiter && (value < 0) &&
                        (((pos.index < text.length()) &&
                         (text.charAt(pos.index) != minusSign)) ||
                         ((pos.index == text.length()) &&
                          (text.charAt(pos.index-1) == minusSign)))) {
                        value = -value;
                        pos.index--;
                    }
                }
            }

            boolean useDateFormatSymbols = useDateFormatSymbols();

            int index;
            switch (patternCharIndex) {
            case PATTERN_ERA: // 'G'
                if (useDateFormatSymbols) {
                    if ((index = matchString(text, start, Calendar.ERA, formatData.getEras(), calb)) > 0) {
                        return index;
                    }
                } else {
                    Map<String, Integer> map = getDisplayNamesMap(field, locale);
                    if ((index = matchString(text, start, field, map, calb)) > 0) {
                        return index;
                    }
                }
                break parsing;

            case PATTERN_WEEK_YEAR: // 'Y'
            case PATTERN_YEAR:      // 'y'
                if (!(calendar instanceof GregorianCalendar)) {
                    // calendar might have text representations for year values,
                    // such as "\u5143" in JapaneseImperialCalendar.
                    int style = (count >= 4) ? Calendar.LONG : Calendar.SHORT;
                    Map<String, Integer> map = calendar.getDisplayNames(field, style, locale);
                    if (map != null) {
                        if ((index = matchString(text, start, field, map, calb)) > 0) {
                            return index;
                        }
                    }
                    calb.set(field, value);
                    return pos.index;
                }

                // If there are 3 or more YEAR pattern characters, this indicates
                // that the year value is to be treated literally, without any
                // two-digit year adjustments (e.g., from "01" to 2001).  Otherwise
                // we made adjustments to place the 2-digit year in the proper
                // century, for parsed strings from "00" to "99".  Any other string
                // is treated literally:  "2250", "-1", "1", "002".
                if (count <= 2 && (pos.index - actualStart) == 2
                    && Character.isDigit(text.charAt(actualStart))
                    && Character.isDigit(text.charAt(actualStart + 1))) {
                    // Assume for example that the defaultCenturyStart is 6/18/1903.
                    // This means that two-digit years will be forced into the range
                    // 6/18/1903 to 6/17/2003.  As a result, years 00, 01, and 02
                    // correspond to 2000, 2001, and 2002.  Years 04, 05, etc. correspond
                    // to 1904, 1905, etc.  If the year is 03, then it is 2003 if the
                    // other fields specify a date before 6/18, or 1903 if they specify a
                    // date afterwards.  As a result, 03 is an ambiguous year.  All other
                    // two-digit years are unambiguous.
                    int ambiguousTwoDigitYear = defaultCenturyStartYear % 100;
                    ambiguousYear[0] = value == ambiguousTwoDigitYear;
                    value += (defaultCenturyStartYear/100)*100 +
                        (value < ambiguousTwoDigitYear ? 100 : 0);
                }
                calb.set(field, value);
                return pos.index;

            case PATTERN_MONTH: // 'M'
            // BEGIN Android-changed: extract parseMonth method.
            /*
                if (count <= 2) // i.e., M or MM.
                {
                    // Don't want to parse the month if it is a string
                    // while pattern uses numeric style: M or MM.
                    // [We computed 'value' above.]
                    calb.set(Calendar.MONTH, value - 1);
                    return pos.index;
                }

                if (useDateFormatSymbols) {
                    // count >= 3 // i.e., MMM or MMMM
                    // Want to be able to parse both short and long forms.
                    // Try count == 4 first:
                    int newStart;
                    if ((newStart = matchString(text, start, Calendar.MONTH,
                                                formatData.getMonths(), calb)) > 0) {
                        return newStart;
                    }
                    // count == 4 failed, now try count == 3
                    if ((index = matchString(text, start, Calendar.MONTH,
                                             formatData.getShortMonths(), calb)) > 0) {
                        return index;
                    }
                } else {
                    Map<String, Integer> map = getDisplayNamesMap(field, locale);
                    if ((index = matchString(text, start, field, map, calb)) > 0) {
                        return index;
                    }
                }
                break parsing;

            case PATTERN_MONTH_STANDALONE: // 'L'
                if (count <= 2) {
                    // Don't want to parse the month if it is a string
                    // while pattern uses numeric style: L or LL
                    //[we computed 'value' above.]
                    calb.set(Calendar.MONTH, value - 1);
                    return pos.index;
                }
                Map<String, Integer> maps = getDisplayNamesMap(field, locale);
                if ((index = matchString(text, start, field, maps, calb)) > 0) {
                    return index;
                }
                break parsing;
            */
            {
                final int idx = parseMonth(text, count, value, start, field, pos,
                        useDateFormatSymbols, false /* isStandalone */, calb);
                if (idx > 0) {
                    return idx;
                }

                break parsing;
            }

            case PATTERN_MONTH_STANDALONE: // 'L'.
            {
                final int idx = parseMonth(text, count, value, start, field, pos,
                        useDateFormatSymbols, true /* isStandalone */, calb);
                if (idx > 0) {
                    return idx;
                }
                break parsing;
            }
            // END Android-changed: extract parseMonth method.

            case PATTERN_HOUR_OF_DAY1: // 'k' 1-based.  eg, 23:59 + 1 hour =>> 24:59
                if (!isLenient()) {
                    // Validate the hour value in non-lenient
                    if (value < 1 || value > 24) {
                        break parsing;
                    }
                }
                // [We computed 'value' above.]
                if (value == calendar.getMaximum(Calendar.HOUR_OF_DAY) + 1) {
                    value = 0;
                }
                calb.set(Calendar.HOUR_OF_DAY, value);
                return pos.index;

            case PATTERN_DAY_OF_WEEK:  // 'E'
            // BEGIN Android-changed: extract parseWeekday method.
            /*
                {
                    if (useDateFormatSymbols) {
                        // Want to be able to parse both short and long forms.
                        // Try count == 4 (DDDD) first:
                        int newStart;
                        if ((newStart=matchString(text, start, Calendar.DAY_OF_WEEK,
                                                  formatData.getWeekdays(), calb)) > 0) {
                            return newStart;
                        }
                        // DDDD failed, now try DDD
                        if ((index = matchString(text, start, Calendar.DAY_OF_WEEK,
                                                 formatData.getShortWeekdays(), calb)) > 0) {
                            return index;
                        }
                    } else {
                        int[] styles = { Calendar.LONG, Calendar.SHORT };
                        for (int style : styles) {
                            Map<String,Integer> map = calendar.getDisplayNames(field, style, locale);
                            if ((index = matchString(text, start, field, map, calb)) > 0) {
                                return index;
                            }
                        }
                    }
                }
            */
            {
                final int idx = parseWeekday(text, start, field, useDateFormatSymbols,
                        false /* standalone */, calb);
                if (idx > 0) {
                    return idx;
                }
                break parsing;
            }
            // END Android-changed: extract parseWeekday method.

            // BEGIN Android-added: support for 'c' (standalone day of week).
            case PATTERN_STANDALONE_DAY_OF_WEEK: // 'c'
            {
                final int idx = parseWeekday(text, start, field, useDateFormatSymbols,
                        true /* standalone */, calb);
                if (idx > 0) {
                    return idx;
                }

                break parsing;
            }
            // END Android-added: support for 'c' (standalone day of week).

            case PATTERN_AM_PM:    // 'a'
                if (useDateFormatSymbols) {
                    if ((index = matchString(text, start, Calendar.AM_PM,
                                             formatData.getAmPmStrings(), calb)) > 0) {
                        return index;
                    }
                } else {
                    Map<String,Integer> map = getDisplayNamesMap(field, locale);
                    if ((index = matchString(text, start, field, map, calb)) > 0) {
                        return index;
                    }
                }
                break parsing;

            case PATTERN_HOUR1: // 'h' 1-based.  eg, 11PM + 1 hour =>> 12 AM
                if (!isLenient()) {
                    // Validate the hour value in non-lenient
                    if (value < 1 || value > 12) {
                        break parsing;
                    }
                }
                // [We computed 'value' above.]
                if (value == calendar.getLeastMaximum(Calendar.HOUR) + 1) {
                    value = 0;
                }
                calb.set(Calendar.HOUR, value);
                return pos.index;

            case PATTERN_ZONE_NAME:  // 'z'
            case PATTERN_ZONE_VALUE: // 'Z'
                {
                    int sign = 0;
                    try {
                        char c = text.charAt(pos.index);
                        if (c == '+') {
                            sign = 1;
                        } else if (c == '-') {
                            sign = -1;
                        }
                        if (sign == 0) {
                            // Try parsing a custom time zone "GMT+hh:mm" or "GMT".
                            if ((c == 'G' || c == 'g')
                                && (text.length() - start) >= GMT.length()
                                && text.regionMatches(true, start, GMT, 0, GMT.length())) {
                                pos.index = start + GMT.length();

                                if ((text.length() - pos.index) > 0) {
                                    c = text.charAt(pos.index);
                                    if (c == '+') {
                                        sign = 1;
                                    } else if (c == '-') {
                                        sign = -1;
                                    }
                                }

                                if (sign == 0) {    /* "GMT" without offset */
                                    calb.set(Calendar.ZONE_OFFSET, 0)
                                        .set(Calendar.DST_OFFSET, 0);
                                    return pos.index;
                                }

                                // BEGIN Android-changed: Be more tolerant of colon. b/26426526
                                /*
                                // Parse the rest as "hh:mm"
                                int i = subParseNumericZone(text, ++pos.index,
                                                            sign, 0, true, calb);
                                */
                                // Parse the rest as "hh[:]?mm"
                                int i = subParseNumericZone(text, ++pos.index, sign, 0,
                                        false, calb);
                                // END Android-changed: Be more tolerant of colon. b/26426526
                                if (i > 0) {
                                    return i;
                                }
                                pos.index = -i;
                            } else {
                                // Try parsing the text as a time zone
                                // name or abbreviation.
                                int i = subParseZoneString(text, pos.index, calb);
                                if (i > 0) {
                                    return i;
                                }
                                pos.index = -i;
                            }
                        } else {
                            // BEGIN Android-changed: Be more tolerant of colon. b/26426526
                            // Parse the rest as "hh[:]?mm" (RFC 822)
                            /*
                            // Parse the rest as "hhmm" (RFC 822)
                            int i = subParseNumericZone(text, ++pos.index,
                                                        sign, 0, false, calb);
                            */
                            int i = subParseNumericZone(text, ++pos.index, sign, 0,
                                    false, calb);
                            // END Android-changed: Be more tolerant of colon. b/26426526
                            if (i > 0) {
                                return i;
                            }
                            pos.index = -i;
                        }
                    } catch (IndexOutOfBoundsException e) {
                    }
                }
                break parsing;

            case PATTERN_ISO_ZONE:   // 'X'
                {
                    if ((text.length() - pos.index) <= 0) {
                        break parsing;
                    }

                    int sign;
                    char c = text.charAt(pos.index);
                    if (c == 'Z') {
                        calb.set(Calendar.ZONE_OFFSET, 0).set(Calendar.DST_OFFSET, 0);
                        return ++pos.index;
                    }

                    // parse text as "+/-hh[[:]mm]" based on count
                    if (c == '+') {
                        sign = 1;
                    } else if (c == '-') {
                        sign = -1;
                    } else {
                        ++pos.index;
                        break parsing;
                    }
                    int i = subParseNumericZone(text, ++pos.index, sign, count,
                                                count == 3, calb);
                    if (i > 0) {
                        return i;
                    }
                    pos.index = -i;
                }
                break parsing;

            default:
         // case PATTERN_DAY_OF_MONTH:         // 'd'
         // case PATTERN_HOUR_OF_DAY0:         // 'H' 0-based.  eg, 23:59 + 1 hour =>> 00:59
         // case PATTERN_MINUTE:               // 'm'
         // case PATTERN_SECOND:               // 's'
         // case PATTERN_MILLISECOND:          // 'S'
         // case PATTERN_DAY_OF_YEAR:          // 'D'
         // case PATTERN_DAY_OF_WEEK_IN_MONTH: // 'F'
         // case PATTERN_WEEK_OF_YEAR:         // 'w'
         // case PATTERN_WEEK_OF_MONTH:        // 'W'
         // case PATTERN_HOUR0:                // 'K' 0-based.  eg, 11PM + 1 hour =>> 0 AM
         // case PATTERN_ISO_DAY_OF_WEEK:      // 'u' (pseudo field);

                // Handle "generic" fields
                // BEGIN Android-changed: Better UTS#35 conformity for fractional seconds.
                // http://b/25863120
                int parseStart = pos.getIndex();
                // END Android-changed: Better UTS#35 conformity for fractional seconds.
                if (obeyCount) {
                    if ((start+count) > text.length()) {
                        break parsing;
                    }
                    number = numberFormat.parse(text.substring(0, start+count), pos);
                } else {
                    number = numberFormat.parse(text, pos);
                }
                if (number != null) {
                    // BEGIN Android-changed: Better UTS#35 conformity for fractional seconds.
                    /*
                    value = number.intValue();
                    */
                    if (patternCharIndex == PATTERN_MILLISECOND) {
                        // Fractional seconds must be treated specially. We must always
                        // normalize them to their fractional second value [0, 1) before we attempt
                        // to parse them.
                        //
                        // Case 1: 11.78 seconds is 11 seconds and 780 (not 78) milliseconds.
                        // Case 2: 11.7890567 seconds is 11 seconds and 789 (not 7890567) milliseconds.
                        double doubleValue = number.doubleValue();
                        int width = pos.getIndex() - parseStart;
                        final double divisor = Math.pow(10, width);
                        value = (int) ((doubleValue / divisor) * 1000);
                    } else {
                        value = number.intValue();
                    }
                    // END Android-changed: Better UTS#35 conformity for fractional seconds.

                    if (useFollowingMinusSignAsDelimiter && (value < 0) &&
                        (((pos.index < text.length()) &&
                         (text.charAt(pos.index) != minusSign)) ||
                         ((pos.index == text.length()) &&
                          (text.charAt(pos.index-1) == minusSign)))) {
                        value = -value;
                        pos.index--;
                    }

                    calb.set(field, value);
                    return pos.index;
                }
                break parsing;
            }
        }

        // Parsing failed.
        origPos.errorIndex = pos.index;
        return -1;
    }

    // BEGIN Android-added: parseMonth and parseWeekday methods to parse using ICU data.
    private int parseMonth(String text, int count, int value, int start,
                           int field, ParsePosition pos, boolean useDateFormatSymbols,
                           boolean standalone,
                           CalendarBuilder out) {
        if (count <= 2) // i.e., M or MM.
        {
            // Don't want to parse the month if it is a string
            // while pattern uses numeric style: M or MM.
            // [We computed 'value' above.]
            out.set(Calendar.MONTH, value - 1);
            return pos.index;
        }

        int index = -1;
        if (useDateFormatSymbols) {
            // count >= 3 // i.e., MMM or MMMM
            // Want to be able to parse both short and long forms.
            // Try count == 4 first:
            if ((index = matchString(
                    text, start, Calendar.MONTH,
                    standalone ? formatData.getStandAloneMonths() : formatData.getMonths(),
                    out)) > 0) {
                return index;
            }
            // count == 4 failed, now try count == 3
            if ((index = matchString(
                    text, start, Calendar.MONTH,
                    standalone ? formatData.getShortStandAloneMonths() : formatData.getShortMonths(),
                    out)) > 0) {
                return index;
            }
        } else {
            Map<String, Integer> map = getDisplayNamesMap(field, locale);
            if ((index = matchString(text, start, field, map, out)) > 0) {
                return index;
            }
        }

        return index;
    }

    private int parseWeekday(String text, int start, int field, boolean useDateFormatSymbols,
                             boolean standalone, CalendarBuilder out) {
        int index = -1;
        if (useDateFormatSymbols) {
            // Want to be able to parse both short and long forms.
            // Try count == 4 (DDDD) first:
            if ((index=matchString(
                    text, start, Calendar.DAY_OF_WEEK,
                    standalone ? formatData.getStandAloneWeekdays() : formatData.getWeekdays(),
                    out)) > 0) {
                return index;
            }

            // DDDD failed, now try DDD
            if ((index = matchString(
                    text, start, Calendar.DAY_OF_WEEK,
                    standalone ? formatData.getShortStandAloneWeekdays() : formatData.getShortWeekdays(),
                    out)) > 0) {
                return index;
            }
        } else {
            int[] styles = { Calendar.LONG, Calendar.SHORT };
            for (int style : styles) {
                Map<String,Integer> map = calendar.getDisplayNames(field, style, locale);
                if ((index = matchString(text, start, field, map, out)) > 0) {
                    return index;
                }
            }
        }

        return index;
    }
    // END Android-added: parseMonth and parseWeekday methods to parse using ICU data.

    // Android-changed: Always useDateFormatSymbols() for GregorianCalendar.
    /**
     * Returns true if the DateFormatSymbols has been set explicitly or locale
     * is null or calendar is Gregorian.
     */
    private boolean useDateFormatSymbols() {
        if (useDateFormatSymbols) {
            return true;
        }
        return isGregorianCalendar() || locale == null;
    }

    private boolean isGregorianCalendar() {
        // J2ObjC reflection-stripping change.
        return ReflectionUtil.matchClassNamePrefix(calendar.getClass().getName(),
                                                   "java.util.GregorianCalendar");
    }

    /**
     * Translates a pattern, mapping each character in the from string to the
     * corresponding character in the to string.
     *
     * @exception IllegalArgumentException if the given pattern is invalid
     */
    private String translatePattern(String pattern, String from, String to) {
        StringBuilder result = new StringBuilder();
        boolean inQuote = false;
        for (int i = 0; i < pattern.length(); ++i) {
            char c = pattern.charAt(i);
            if (inQuote) {
                if (c == '\'') {
                    inQuote = false;
                }
            }
            else {
                if (c == '\'') {
                    inQuote = true;
                } else if ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z')) {
                    int ci = from.indexOf(c);
                    if (ci >= 0) {
                        // patternChars is longer than localPatternChars due
                        // to serialization compatibility. The pattern letters
                        // unsupported by localPatternChars pass through.
                        if (ci < to.length()) {
                            c = to.charAt(ci);
                        }
                    } else {
                        throw new IllegalArgumentException("Illegal pattern " +
                                                           " character '" +
                                                           c + "'");
                    }
                }
            }
            result.append(c);
        }
        if (inQuote) {
            throw new IllegalArgumentException("Unfinished quote in pattern");
        }
        return result.toString();
    }

    /**
     * Returns a pattern string describing this date format.
     *
     * @return a pattern string describing this date format.
     */
    public String toPattern() {
        return pattern;
    }

    /**
     * Returns a localized pattern string describing this date format.
     *
     * @return a localized pattern string describing this date format.
     */
    public String toLocalizedPattern() {
        return translatePattern(pattern,
                                DateFormatSymbols.patternChars,
                                formatData.getLocalPatternChars());
    }

    /**
     * Applies the given pattern string to this date format.
     *
     * @param pattern the new date and time pattern for this date format
     * @exception NullPointerException if the given pattern is null
     * @exception IllegalArgumentException if the given pattern is invalid
     */
    public void applyPattern(String pattern)
    {
        applyPatternImpl(pattern);
    }

    private void applyPatternImpl(String pattern) {
        compiledPattern = compile(pattern);
        this.pattern = pattern;
    }

    /**
     * Applies the given localized pattern string to this date format.
     *
     * @param pattern a String to be mapped to the new date and time format
     *        pattern for this format
     * @exception NullPointerException if the given pattern is null
     * @exception IllegalArgumentException if the given pattern is invalid
     */
    public void applyLocalizedPattern(String pattern) {
         String p = translatePattern(pattern,
                                     formatData.getLocalPatternChars(),
                                     DateFormatSymbols.patternChars);
         compiledPattern = compile(p);
         this.pattern = p;
    }

    /**
     * Gets a copy of the date and time format symbols of this date format.
     *
     * @return the date and time format symbols of this date format
     * @see #setDateFormatSymbols
     */
    public DateFormatSymbols getDateFormatSymbols()
    {
        return (DateFormatSymbols)formatData.clone();
    }

    /**
     * Sets the date and time format symbols of this date format.
     *
     * @param newFormatSymbols the new date and time format symbols
     * @exception NullPointerException if the given newFormatSymbols is null
     * @see #getDateFormatSymbols
     */
    public void setDateFormatSymbols(DateFormatSymbols newFormatSymbols)
    {
        this.formatData = (DateFormatSymbols)newFormatSymbols.clone();
        useDateFormatSymbols = true;
    }

    /**
     * Creates a copy of this <code>SimpleDateFormat</code>. This also
     * clones the format's date format symbols.
     *
     * @return a clone of this <code>SimpleDateFormat</code>
     */
    @Override
    public Object clone() {
        SimpleDateFormat other = (SimpleDateFormat) super.clone();
        other.formatData = (DateFormatSymbols) formatData.clone();
        return other;
    }

    /**
     * Returns the hash code value for this <code>SimpleDateFormat</code> object.
     *
     * @return the hash code value for this <code>SimpleDateFormat</code> object.
     */
    @Override
    public int hashCode()
    {
        return pattern.hashCode();
        // just enough fields for a reasonable distribution
    }

    /**
     * Compares the given object with this <code>SimpleDateFormat</code> for
     * equality.
     *
     * @return true if the given object is equal to this
     * <code>SimpleDateFormat</code>
     */
    @Override
    public boolean equals(Object obj)
    {
        if (!super.equals(obj)) {
            return false; // super does class check
        }
        SimpleDateFormat that = (SimpleDateFormat) obj;
        return (pattern.equals(that.pattern)
                && formatData.equals(that.formatData));
    }

    private static final int[] REST_OF_STYLES = {
        Calendar.SHORT_STANDALONE, Calendar.LONG_FORMAT, Calendar.LONG_STANDALONE,
    };
    private Map<String, Integer> getDisplayNamesMap(int field, Locale locale) {
        Map<String, Integer> map = calendar.getDisplayNames(field, Calendar.SHORT_FORMAT, locale);
        // Get all SHORT and LONG styles (avoid NARROW styles).
        for (int style : REST_OF_STYLES) {
            Map<String, Integer> m = calendar.getDisplayNames(field, style, locale);
            if (m != null) {
                map.putAll(m);
            }
        }
        return map;
    }

    /**
     * After reading an object from the input stream, the format
     * pattern in the object is verified.
     *
     * @exception InvalidObjectException if the pattern is invalid
     */
    private void readObject(ObjectInputStream stream)
                         throws IOException, ClassNotFoundException {
        stream.defaultReadObject();

        try {
            compiledPattern = compile(pattern);
        } catch (Exception e) {
            throw new InvalidObjectException("invalid pattern");
        }

        if (serialVersionOnStream < 1) {
            // didn't have defaultCenturyStart field
            initializeDefaultCentury();
        }
        else {
            // fill in dependent transient field
            parseAmbiguousDatesAsAfter(defaultCenturyStart);
        }
        serialVersionOnStream = currentSerialVersion;

        // If the deserialized object has a SimpleTimeZone, try
        // to replace it with a ZoneInfo equivalent in order to
        // be compatible with the SimpleTimeZone-based
        // implementation as much as possible.
        TimeZone tz = getTimeZone();
        if (tz instanceof SimpleTimeZone) {
            String id = tz.getID();
            TimeZone zi = TimeZone.getTimeZone(id);
            if (zi != null && zi.hasSameRules(tz) && zi.getID().equals(id)) {
                setTimeZone(zi);
            }
        }
    }

    /**
     * Analyze the negative subpattern of DecimalFormat and set/update values
     * as necessary.
     */
    private void checkNegativeNumberExpression() {
        if ((numberFormat instanceof DecimalFormat) &&
            !numberFormat.equals(originalNumberFormat)) {
            String numberPattern = ((DecimalFormat)numberFormat).toPattern();
            if (!numberPattern.equals(originalNumberPattern)) {
                hasFollowingMinusSign = false;

                int separatorIndex = numberPattern.indexOf(';');
                // If the negative subpattern is not absent, we have to analayze
                // it in order to check if it has a following minus sign.
                if (separatorIndex > -1) {
                    int minusIndex = numberPattern.indexOf('-', separatorIndex);
                    if ((minusIndex > numberPattern.lastIndexOf('0')) &&
                        (minusIndex > numberPattern.lastIndexOf('#'))) {
                        hasFollowingMinusSign = true;
                        minusSign = ((DecimalFormat)numberFormat).getDecimalFormatSymbols().getMinusSign();
                    }
                }
                originalNumberPattern = numberPattern;
            }
            originalNumberFormat = numberFormat;
        }
    }

}
