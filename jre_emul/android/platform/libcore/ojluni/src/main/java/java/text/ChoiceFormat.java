/*
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
 * (C) Copyright Taligent, Inc. 1996, 1997 - All Rights Reserved
 * (C) Copyright IBM Corp. 1996 - 1998 - All Rights Reserved
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

import java.io.InvalidObjectException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Arrays;

/**
 * A <code>ChoiceFormat</code> allows you to attach a format to a range of numbers.
 * It is generally used in a <code>MessageFormat</code> for handling plurals.
 * The choice is specified with an ascending list of doubles, where each item
 * specifies a half-open interval up to the next item:
 * <blockquote>
 * <pre>
 * X matches j if and only if limit[j] &le; X &lt; limit[j+1]
 * </pre>
 * </blockquote>
 * If there is no match, then either the first or last index is used, depending
 * on whether the number (X) is too low or too high.  If the limit array is not
 * in ascending order, the results of formatting will be incorrect.  ChoiceFormat
 * also accepts <code>&#92;u221E</code> as equivalent to infinity(INF).
 *
 * <p>
 * <strong>Note:</strong>
 * <code>ChoiceFormat</code> differs from the other <code>Format</code>
 * classes in that you create a <code>ChoiceFormat</code> object with a
 * constructor (not with a <code>getInstance</code> style factory
 * method). The factory methods aren't necessary because <code>ChoiceFormat</code>
 * doesn't require any complex setup for a given locale. In fact,
 * <code>ChoiceFormat</code> doesn't implement any locale specific behavior.
 *
 * <p>
 * When creating a <code>ChoiceFormat</code>, you must specify an array of formats
 * and an array of limits. The length of these arrays must be the same.
 * For example,
 * <ul>
 * <li>
 *     <em>limits</em> = {1,2,3,4,5,6,7}<br>
 *     <em>formats</em> = {"Sun","Mon","Tue","Wed","Thur","Fri","Sat"}
 * <li>
 *     <em>limits</em> = {0, 1, ChoiceFormat.nextDouble(1)}<br>
 *     <em>formats</em> = {"no files", "one file", "many files"}<br>
 *     (<code>nextDouble</code> can be used to get the next higher double, to
 *     make the half-open interval.)
 * </ul>
 *
 * <p>
 * Here is a simple example that shows formatting and parsing:
 * <blockquote>
 * <pre>{@code
 * double[] limits = {1,2,3,4,5,6,7};
 * String[] dayOfWeekNames = {"Sun","Mon","Tue","Wed","Thur","Fri","Sat"};
 * ChoiceFormat form = new ChoiceFormat(limits, dayOfWeekNames);
 * ParsePosition status = new ParsePosition(0);
 * for (double i = 0.0; i <= 8.0; ++i) {
 *     status.setIndex(0);
 *     System.out.println(i + " -> " + form.format(i) + " -> "
 *                              + form.parse(form.format(i),status));
 * }
 * }</pre>
 * </blockquote>
 * Here is a more complex example, with a pattern format:
 * <blockquote>
 * <pre>{@code
 * double[] filelimits = {0,1,2};
 * String[] filepart = {"are no files","is one file","are {2} files"};
 * ChoiceFormat fileform = new ChoiceFormat(filelimits, filepart);
 * Format[] testFormats = {fileform, null, NumberFormat.getInstance()};
 * MessageFormat pattform = new MessageFormat("There {0} on {1}");
 * pattform.setFormats(testFormats);
 * Object[] testArgs = {null, "ADisk", null};
 * for (int i = 0; i < 4; ++i) {
 *     testArgs[0] = new Integer(i);
 *     testArgs[2] = testArgs[0];
 *     System.out.println(pattform.format(testArgs));
 * }
 * }</pre>
 * </blockquote>
 * <p>
 * Specifying a pattern for ChoiceFormat objects is fairly straightforward.
 * For example:
 * <blockquote>
 * <pre>{@code
 * ChoiceFormat fmt = new ChoiceFormat(
 *      "-1#is negative| 0#is zero or fraction | 1#is one |1.0<is 1+ |2#is two |2<is more than 2.");
 * System.out.println("Formatter Pattern : " + fmt.toPattern());
 *
 * System.out.println("Format with -INF : " + fmt.format(Double.NEGATIVE_INFINITY));
 * System.out.println("Format with -1.0 : " + fmt.format(-1.0));
 * System.out.println("Format with 0 : " + fmt.format(0));
 * System.out.println("Format with 0.9 : " + fmt.format(0.9));
 * System.out.println("Format with 1.0 : " + fmt.format(1));
 * System.out.println("Format with 1.5 : " + fmt.format(1.5));
 * System.out.println("Format with 2 : " + fmt.format(2));
 * System.out.println("Format with 2.1 : " + fmt.format(2.1));
 * System.out.println("Format with NaN : " + fmt.format(Double.NaN));
 * System.out.println("Format with +INF : " + fmt.format(Double.POSITIVE_INFINITY));
 * }</pre>
 * </blockquote>
 * And the output result would be like the following:
 * <blockquote>
 * <pre>{@code
 * Format with -INF : is negative
 * Format with -1.0 : is negative
 * Format with 0 : is zero or fraction
 * Format with 0.9 : is zero or fraction
 * Format with 1.0 : is one
 * Format with 1.5 : is 1+
 * Format with 2 : is two
 * Format with 2.1 : is more than 2.
 * Format with NaN : is negative
 * Format with +INF : is more than 2.
 * }</pre>
 * </blockquote>
 *
 * <h3><a id="synchronization">Synchronization</a></h3>
 *
 * <p>
 * Choice formats are not synchronized.
 * It is recommended to create separate format instances for each thread.
 * If multiple threads access a format concurrently, it must be synchronized
 * externally.
 *
 *
 * @see          DecimalFormat
 * @see          MessageFormat
 * @author       Mark Davis
 * @since 1.1
 */
public class ChoiceFormat extends NumberFormat {

    // Proclaim serial compatibility with 1.1 FCS
    private static final long serialVersionUID = 1795184449645032964L;

    /**
     * Sets the pattern.
     * @param newPattern See the class description.
     * @exception NullPointerException if {@code newPattern}
     *            is {@code null}
     */
    public void applyPattern(String newPattern) {
        StringBuffer[] segments = new StringBuffer[2];
        for (int i = 0; i < segments.length; ++i) {
            segments[i] = new StringBuffer();
        }
        double[] newChoiceLimits = new double[30];
        String[] newChoiceFormats = new String[30];
        int count = 0;
        int part = 0;
        double startValue = 0;
        double oldStartValue = Double.NaN;
        boolean inQuote = false;
        for (int i = 0; i < newPattern.length(); ++i) {
            char ch = newPattern.charAt(i);
            if (ch=='\'') {
                // Check for "''" indicating a literal quote
                if ((i+1)<newPattern.length() && newPattern.charAt(i+1)==ch) {
                    segments[part].append(ch);
                    ++i;
                } else {
                    inQuote = !inQuote;
                }
            } else if (inQuote) {
                segments[part].append(ch);
            } else if (ch == '<' || ch == '#' || ch == '\u2264') {
                if (segments[0].length() == 0) {
                    throw new IllegalArgumentException("Each interval must"
                            + " contain a number before a format");
                }

                String tempBuffer = segments[0].toString();
                if (tempBuffer.equals("\u221E")) {
                    startValue = Double.POSITIVE_INFINITY;
                } else if (tempBuffer.equals("-\u221E")) {
                    startValue = Double.NEGATIVE_INFINITY;
                } else {
                    startValue = Double.parseDouble(tempBuffer);
                }

                if (ch == '<' && startValue != Double.POSITIVE_INFINITY &&
                        startValue != Double.NEGATIVE_INFINITY) {
                    startValue = nextDouble(startValue);
                }
                if (startValue <= oldStartValue) {
                    throw new IllegalArgumentException("Incorrect order of"
                            + " intervals, must be in ascending order");
                }
                segments[0].setLength(0);
                part = 1;
            } else if (ch == '|') {
                if (count == newChoiceLimits.length) {
                    newChoiceLimits = doubleArraySize(newChoiceLimits);
                    newChoiceFormats = doubleArraySize(newChoiceFormats);
                }
                newChoiceLimits[count] = startValue;
                newChoiceFormats[count] = segments[1].toString();
                ++count;
                oldStartValue = startValue;
                segments[1].setLength(0);
                part = 0;
            } else {
                segments[part].append(ch);
            }
        }
        // clean up last one
        if (part == 1) {
            if (count == newChoiceLimits.length) {
                newChoiceLimits = doubleArraySize(newChoiceLimits);
                newChoiceFormats = doubleArraySize(newChoiceFormats);
            }
            newChoiceLimits[count] = startValue;
            newChoiceFormats[count] = segments[1].toString();
            ++count;
        }
        choiceLimits = new double[count];
        System.arraycopy(newChoiceLimits, 0, choiceLimits, 0, count);
        choiceFormats = new String[count];
        System.arraycopy(newChoiceFormats, 0, choiceFormats, 0, count);
    }

    /**
     * Gets the pattern.
     *
     * @return the pattern string
     */
    public String toPattern() {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < choiceLimits.length; ++i) {
            if (i != 0) {
                result.append('|');
            }
            // choose based upon which has less precision
            // approximate that by choosing the closest one to an integer.
            // could do better, but it's not worth it.
            double less = previousDouble(choiceLimits[i]);
            double tryLessOrEqual = Math.abs(Math.IEEEremainder(choiceLimits[i], 1.0d));
            double tryLess = Math.abs(Math.IEEEremainder(less, 1.0d));

            if (tryLessOrEqual < tryLess) {
                result.append(choiceLimits[i]);
                result.append('#');
            } else {
                if (choiceLimits[i] == Double.POSITIVE_INFINITY) {
                    result.append("\u221E");
                } else if (choiceLimits[i] == Double.NEGATIVE_INFINITY) {
                    result.append("-\u221E");
                } else {
                    result.append(less);
                }
                result.append('<');
            }
            // Append choiceFormats[i], using quotes if there are special characters.
            // Single quotes themselves must be escaped in either case.
            String text = choiceFormats[i];
            boolean needQuote = text.indexOf('<') >= 0
                || text.indexOf('#') >= 0
                || text.indexOf('\u2264') >= 0
                || text.indexOf('|') >= 0;
            if (needQuote) result.append('\'');
            if (text.indexOf('\'') < 0) result.append(text);
            else {
                for (int j=0; j<text.length(); ++j) {
                    char c = text.charAt(j);
                    result.append(c);
                    if (c == '\'') result.append(c);
                }
            }
            if (needQuote) result.append('\'');
        }
        return result.toString();
    }

    /**
     * Constructs with limits and corresponding formats based on the pattern.
     *
     * @param newPattern the new pattern string
     * @exception NullPointerException if {@code newPattern} is
     *            {@code null}
     * @see #applyPattern
     */
    public ChoiceFormat(String newPattern)  {
        applyPattern(newPattern);
    }

    /**
     * Constructs with the limits and the corresponding formats.
     *
     * @param limits limits in ascending order
     * @param formats corresponding format strings
     * @exception NullPointerException if {@code limits} or {@code formats}
     *            is {@code null}
     * @see #setChoices
     */
    public ChoiceFormat(double[] limits, String[] formats) {
        setChoices(limits, formats);
    }

    /**
     * Set the choices to be used in formatting.
     * @param limits contains the top value that you want
     * parsed with that format, and should be in ascending sorted order. When
     * formatting X, the choice will be the i, where
     * limit[i] &le; X {@literal <} limit[i+1].
     * If the limit array is not in ascending order, the results of formatting
     * will be incorrect.
     * @param formats are the formats you want to use for each limit.
     * They can be either Format objects or Strings.
     * When formatting with object Y,
     * if the object is a NumberFormat, then ((NumberFormat) Y).format(X)
     * is called. Otherwise Y.toString() is called.
     * @exception NullPointerException if {@code limits} or
     *            {@code formats} is {@code null}
     */
    public void setChoices(double[] limits, String formats[]) {
        if (limits.length != formats.length) {
            throw new IllegalArgumentException(
                "Array and limit arrays must be of the same length.");
        }
        choiceLimits = limits;
        choiceFormats = formats;
    }

    // Android-changed: Clarify that calling setChoices() changes what is returned here.
    /**
     * @return a copy of the {@code double[]} array supplied to the constructor or the most recent
     * call to {@link #setChoices(double[], String[])}.
     */
    public double[] getLimits() {
        return choiceLimits;
    }

    // Android-changed: Clarify that calling setChoices() changes what is returned here.
    /**
     * @return a copy of the {@code String[]} array supplied to the constructor or the most recent
     * call to {@link #setChoices(double[], String[])}.
     */
    public Object[] getFormats() {
        return choiceFormats;
    }

    // Overrides

    /**
     * Specialization of format. This method really calls
     * <code>format(double, StringBuffer, FieldPosition)</code>
     * thus the range of longs that are supported is only equal to
     * the range that can be stored by double. This will never be
     * a practical limitation.
     */
    public StringBuffer format(long number, StringBuffer toAppendTo,
                               FieldPosition status) {
        return format((double)number, toAppendTo, status);
    }

    /**
     * Returns pattern with formatted double.
     * @param number number to be formatted and substituted.
     * @param toAppendTo where text is appended.
     * @param status ignore no useful status is returned.
     * @exception NullPointerException if {@code toAppendTo}
     *            is {@code null}
     */
   public StringBuffer format(double number, StringBuffer toAppendTo,
                               FieldPosition status) {
        // find the number
        int i;
        for (i = 0; i < choiceLimits.length; ++i) {
            if (!(number >= choiceLimits[i])) {
                // same as number < choiceLimits, except catchs NaN
                break;
            }
        }
        --i;
        if (i < 0) i = 0;
        // return either a formatted number, or a string
        return toAppendTo.append(choiceFormats[i]);
    }

    /**
     * Parses a Number from the input text.
     * @param text the source text.
     * @param status an input-output parameter.  On input, the
     * status.index field indicates the first character of the
     * source text that should be parsed.  On exit, if no error
     * occurred, status.index is set to the first unparsed character
     * in the source text.  On exit, if an error did occur,
     * status.index is unchanged and status.errorIndex is set to the
     * first index of the character that caused the parse to fail.
     * @return A Number representing the value of the number parsed.
     * @exception NullPointerException if {@code status} is {@code null}
     *            or if {@code text} is {@code null} and the list of
     *            choice strings is not empty.
     */
    public Number parse(String text, ParsePosition status) {
        // find the best number (defined as the one with the longest parse)
        int start = status.index;
        int furthest = start;
        double bestNumber = Double.NaN;
        double tempNumber = 0.0;
        for (int i = 0; i < choiceFormats.length; ++i) {
            String tempString = choiceFormats[i];
            if (text.regionMatches(start, tempString, 0, tempString.length())) {
                status.index = start + tempString.length();
                tempNumber = choiceLimits[i];
                if (status.index > furthest) {
                    furthest = status.index;
                    bestNumber = tempNumber;
                    if (furthest == text.length()) break;
                }
            }
        }
        status.index = furthest;
        if (status.index == start) {
            status.errorIndex = furthest;
        }
        return Double.valueOf(bestNumber);
    }

    /**
     * Finds the least double greater than {@code d}.
     * If {@code NaN}, returns same value.
     * <p>Used to make half-open intervals.
     *
     * @param d the reference value
     * @return the least double value greather than {@code d}
     * @see #previousDouble
     */
    public static final double nextDouble (double d) {
        return nextDouble(d,true);
    }

    /**
     * Finds the greatest double less than {@code d}.
     * If {@code NaN}, returns same value.
     *
     * @param d the reference value
     * @return the greatest double value less than {@code d}
     * @see #nextDouble
     */
    public static final double previousDouble (double d) {
        return nextDouble(d,false);
    }

    /**
     * Overrides Cloneable
     */
    public Object clone()
    {
        ChoiceFormat other = (ChoiceFormat) super.clone();
        // for primitives or immutables, shallow clone is enough
        other.choiceLimits = choiceLimits.clone();
        other.choiceFormats = choiceFormats.clone();
        return other;
    }

    /**
     * Generates a hash code for the message format object.
     */
    public int hashCode() {
        int result = choiceLimits.length;
        if (choiceFormats.length > 0) {
            // enough for reasonable distribution
            result ^= choiceFormats[choiceFormats.length-1].hashCode();
        }
        return result;
    }

    /**
     * Equality comparison between two
     */
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (this == obj)                      // quick check
            return true;
        if (getClass() != obj.getClass())
            return false;
        ChoiceFormat other = (ChoiceFormat) obj;
        return (Arrays.equals(choiceLimits, other.choiceLimits)
             && Arrays.equals(choiceFormats, other.choiceFormats));
    }

    /**
     * After reading an object from the input stream, do a simple verification
     * to maintain class invariants.
     * @throws InvalidObjectException if the objects read from the stream is invalid.
     */
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        if (choiceLimits.length != choiceFormats.length) {
            throw new InvalidObjectException(
                    "limits and format arrays of different length.");
        }
    }

    // ===============privates===========================

    /**
     * A list of lower bounds for the choices.  The formatter will return
     * <code>choiceFormats[i]</code> if the number being formatted is greater than or equal to
     * <code>choiceLimits[i]</code> and less than <code>choiceLimits[i+1]</code>.
     * @serial
     */
    private double[] choiceLimits;

    /**
     * A list of choice strings.  The formatter will return
     * <code>choiceFormats[i]</code> if the number being formatted is greater than or equal to
     * <code>choiceLimits[i]</code> and less than <code>choiceLimits[i+1]</code>.
     * @serial
     */
    private String[] choiceFormats;

    /*
    static final long SIGN          = 0x8000000000000000L;
    static final long EXPONENT      = 0x7FF0000000000000L;
    static final long SIGNIFICAND   = 0x000FFFFFFFFFFFFFL;

    private static double nextDouble (double d, boolean positive) {
        if (Double.isNaN(d) || Double.isInfinite(d)) {
                return d;
            }
        long bits = Double.doubleToLongBits(d);
        long significand = bits & SIGNIFICAND;
        if (bits < 0) {
            significand |= (SIGN | EXPONENT);
        }
        long exponent = bits & EXPONENT;
        if (positive) {
            significand += 1;
            // FIXME fix overflow & underflow
        } else {
            significand -= 1;
            // FIXME fix overflow & underflow
        }
        bits = exponent | (significand & ~EXPONENT);
        return Double.longBitsToDouble(bits);
    }
    */

    static final long SIGN                = 0x8000000000000000L;
    static final long EXPONENT            = 0x7FF0000000000000L;
    static final long POSITIVEINFINITY    = 0x7FF0000000000000L;

    /**
     * Finds the least double greater than {@code d} (if {@code positive} is
     * {@code true}), or the greatest double less than {@code d} (if
     * {@code positive} is {@code false}).
     * If {@code NaN}, returns same value.
     *
     * Does not affect floating-point flags,
     * provided these member functions do not:
     *          Double.longBitsToDouble(long)
     *          Double.doubleToLongBits(double)
     *          Double.isNaN(double)
     *
     * @param d        the reference value
     * @param positive {@code true} if the least double is desired;
     *                 {@code false} otherwise
     * @return the least or greater double value
     */
    public static double nextDouble (double d, boolean positive) {

        /* filter out NaN's */
        if (Double.isNaN(d)) {
            return d;
        }

        /* zero's are also a special case */
        if (d == 0.0) {
            double smallestPositiveDouble = Double.longBitsToDouble(1L);
            if (positive) {
                return smallestPositiveDouble;
            } else {
                return -smallestPositiveDouble;
            }
        }

        /* if entering here, d is a nonzero value */

        /* hold all bits in a long for later use */
        long bits = Double.doubleToLongBits(d);

        /* strip off the sign bit */
        long magnitude = bits & ~SIGN;

        /* if next double away from zero, increase magnitude */
        if ((bits > 0) == positive) {
            if (magnitude != POSITIVEINFINITY) {
                magnitude += 1;
            }
        }
        /* else decrease magnitude */
        else {
            magnitude -= 1;
        }

        /* restore sign bit and return */
        long signbit = bits & SIGN;
        return Double.longBitsToDouble (magnitude | signbit);
    }

    private static double[] doubleArraySize(double[] array) {
        int oldSize = array.length;
        double[] newArray = new double[oldSize * 2];
        System.arraycopy(array, 0, newArray, 0, oldSize);
        return newArray;
    }

    private String[] doubleArraySize(String[] array) {
        int oldSize = array.length;
        String[] newArray = new String[oldSize * 2];
        System.arraycopy(array, 0, newArray, 0, oldSize);
        return newArray;
    }

}
