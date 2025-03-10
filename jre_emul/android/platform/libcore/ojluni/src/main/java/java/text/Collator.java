/*
 * Copyright (C) 2014 The Android Open Source Project
 * Copyright (c) 1997, 2013, Oracle and/or its affiliates. All rights reserved.
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
 * (C) Copyright Taligent, Inc. 1996-1998 -  All Rights Reserved
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

import java.util.Comparator;
import java.util.Locale;

import libcore.icu.ICU;

/**
 * The <code>Collator</code> class performs locale-sensitive
 * <code>String</code> comparison. You use this class to build
 * searching and sorting routines for natural language text.
 *
 * <p>
 * <code>Collator</code> is an abstract base class. Subclasses
 * implement specific collation strategies. One subclass,
 * <code>RuleBasedCollator</code>, is currently provided with
 * the Java Platform and is applicable to a wide set of languages. Other
 * subclasses may be created to handle more specialized needs.
 *
 * <p>
 * Like other locale-sensitive classes, you can use the static
 * factory method, <code>getInstance</code>, to obtain the appropriate
 * <code>Collator</code> object for a given locale. You will only need
 * to look at the subclasses of <code>Collator</code> if you need
 * to understand the details of a particular collation strategy or
 * if you need to modify that strategy.
 *
 * <p>
 * The following example shows how to compare two strings using
 * the <code>Collator</code> for the default locale.
 * <blockquote>
 * <pre>{@code
 * // Compare two strings in the default locale
 * Collator myCollator = Collator.getInstance();
 * if( myCollator.compare("abc", "ABC") < 0 )
 *     System.out.println("abc is less than ABC");
 * else
 *     System.out.println("abc is greater than or equal to ABC");
 * }</pre>
 * </blockquote>
 *
 * <p>
 * You can set a <code>Collator</code>'s <em>strength</em> property
 * to determine the level of difference considered significant in
 * comparisons. Four strengths are provided: <code>PRIMARY</code>,
 * <code>SECONDARY</code>, <code>TERTIARY</code>, and <code>IDENTICAL</code>.
 * The exact assignment of strengths to language features is
 * locale dependent.  For example, in Czech, "e" and "f" are considered
 * primary differences, while "e" and "&#283;" are secondary differences,
 * "e" and "E" are tertiary differences and "e" and "e" are identical.
 * The following shows how both case and accents could be ignored for
 * US English.
 * <blockquote>
 * <pre>
 * //Get the Collator for US English and set its strength to PRIMARY
 * Collator usCollator = Collator.getInstance(Locale.US);
 * usCollator.setStrength(Collator.PRIMARY);
 * if( usCollator.compare("abc", "ABC") == 0 ) {
 *     System.out.println("Strings are equivalent");
 * }
 * </pre>
 * </blockquote>
 * <p>
 * For comparing <code>String</code>s exactly once, the <code>compare</code>
 * method provides the best performance. When sorting a list of
 * <code>String</code>s however, it is generally necessary to compare each
 * <code>String</code> multiple times. In this case, <code>CollationKey</code>s
 * provide better performance. The <code>CollationKey</code> class converts
 * a <code>String</code> to a series of bits that can be compared bitwise
 * against other <code>CollationKey</code>s. A <code>CollationKey</code> is
 * created by a <code>Collator</code> object for a given <code>String</code>.
 * <br>
 * <strong>Note:</strong> <code>CollationKey</code>s from different
 * <code>Collator</code>s can not be compared. See the class description
 * for {@link CollationKey}
 * for an example using <code>CollationKey</code>s.
 *
 * @see         RuleBasedCollator
 * @see         CollationKey
 * @see         CollationElementIterator
 * @see         Locale
 * @author      Helena Shih, Laura Werner, Richard Gillam
 * @since 1.1
 */
public abstract class Collator implements Comparator<Object>, Cloneable {
    /**
     * Collator strength value.  When set, only PRIMARY differences are
     * considered significant during comparison. The assignment of strengths
     * to language features is locale dependent. A common example is for
     * different base letters ("a" vs "b") to be considered a PRIMARY difference.
     * @see java.text.Collator#setStrength
     * @see java.text.Collator#getStrength
     */
    public static final int PRIMARY = 0;
    /**
     * Collator strength value.  When set, only SECONDARY and above differences are
     * considered significant during comparison. The assignment of strengths
     * to language features is locale dependent. A common example is for
     * different accented forms of the same base letter ("a" vs "\u00E4") to be
     * considered a SECONDARY difference.
     * @see java.text.Collator#setStrength
     * @see java.text.Collator#getStrength
     */
    public static final int SECONDARY = 1;
    /**
     * Collator strength value.  When set, only TERTIARY and above differences are
     * considered significant during comparison. The assignment of strengths
     * to language features is locale dependent. A common example is for
     * case differences ("a" vs "A") to be considered a TERTIARY difference.
     * @see java.text.Collator#setStrength
     * @see java.text.Collator#getStrength
     */
    public static final int TERTIARY = 2;

    /**
     * Collator strength value.  When set, all differences are
     * considered significant during comparison. The assignment of strengths
     * to language features is locale dependent. A common example is for control
     * characters ("&#092;u0001" vs "&#092;u0002") to be considered equal at the
     * PRIMARY, SECONDARY, and TERTIARY levels but different at the IDENTICAL
     * level.  Additionally, differences between pre-composed accents such as
     * "&#092;u00C0" (A-grave) and combining accents such as "A&#092;u0300"
     * (A, combining-grave) will be considered significant at the IDENTICAL
     * level if decomposition is set to NO_DECOMPOSITION.
     */
    public static final int IDENTICAL = 3;

    /**
     * Decomposition mode value. With NO_DECOMPOSITION
     * set, accented characters will not be decomposed for collation. This
     * is the default setting and provides the fastest collation but
     * will only produce correct results for languages that do not use accents.
     * @see java.text.Collator#getDecomposition
     * @see java.text.Collator#setDecomposition
     */
    public static final int NO_DECOMPOSITION = 0;

    /**
     * Decomposition mode value. With CANONICAL_DECOMPOSITION
     * set, characters that are canonical variants according to Unicode
     * standard will be decomposed for collation. This should be used to get
     * correct collation of accented characters.
     * <p>
     * CANONICAL_DECOMPOSITION corresponds to Normalization Form D as
     * described in
     * <a href="http://www.unicode.org/unicode/reports/tr15/tr15-23.html">Unicode
     * Technical Report #15</a>.
     * @see java.text.Collator#getDecomposition
     * @see java.text.Collator#setDecomposition
     */
    public static final int CANONICAL_DECOMPOSITION = 1;

    /**
     * Decomposition mode value. With FULL_DECOMPOSITION
     * set, both Unicode canonical variants and Unicode compatibility variants
     * will be decomposed for collation.  This causes not only accented
     * characters to be collated, but also characters that have special formats
     * to be collated with their norminal form. For example, the half-width and
     * full-width ASCII and Katakana characters are then collated together.
     * FULL_DECOMPOSITION is the most complete and therefore the slowest
     * decomposition mode.
     * <p>
     * FULL_DECOMPOSITION corresponds to Normalization Form KD as
     * described in
     * <a href="http://www.unicode.org/unicode/reports/tr15/tr15-23.html">Unicode
     * Technical Report #15</a>.
     * @see java.text.Collator#getDecomposition
     * @see java.text.Collator#setDecomposition
     */
    public static final int FULL_DECOMPOSITION = 2;

    /**
     * Gets the Collator for the desired locale.
     * @param desiredLocale the desired locale.
     * @return the Collator for the desired locale.
     * @see java.util.Locale
     * @see java.util.ResourceBundle
     */
    public static Collator getInstance(Locale desiredLocale) {
        if (desiredLocale == null) {
            throw new NullPointerException("locale == null");
        }
        return new IOSCollator(desiredLocale);
    }

    /**
     * Compares the source string to the target string according to the
     * collation rules for this Collator.  Returns an integer less than,
     * equal to or greater than zero depending on whether the source String is
     * less than, equal to or greater than the target string.  See the Collator
     * class description for an example of use.
     * <p>
     * For a one time comparison, this method has the best performance. If a
     * given String will be involved in multiple comparisons, CollationKey.compareTo
     * has the best performance. See the Collator class description for an example
     * using CollationKeys.
     * @param source the source string.
     * @param target the target string.
     * @return Returns an integer value. Value is less than zero if source is less than
     * target, value is zero if source and target are equal, value is greater than zero
     * if source is greater than target.
     * @see java.text.CollationKey
     * @see java.text.Collator#getCollationKey
     */
    public abstract int compare(String source, String target);

    /**
     * Compares its two arguments for order.  Returns a negative integer,
     * zero, or a positive integer as the first argument is less than, equal
     * to, or greater than the second.
     * <p>
     * This implementation merely returns
     *  <code> compare((String)o1, (String)o2) </code>.
     *
     * @return a negative integer, zero, or a positive integer as the
     *         first argument is less than, equal to, or greater than the
     *         second.
     * @exception ClassCastException the arguments cannot be cast to Strings.
     * @see java.util.Comparator
     * @since   1.2
     */
    @Override
    public int compare(Object o1, Object o2) {
    return compare((String)o1, (String)o2);
    }

    /**
     * Transforms the String into a series of bits that can be compared bitwise
     * to other CollationKeys. CollationKeys provide better performance than
     * Collator.compare when Strings are involved in multiple comparisons.
     * See the Collator class description for an example using CollationKeys.
     * @param source the string to be transformed into a collation key.
     * @return the CollationKey for the given String based on this Collator's collation
     * rules. If the source String is null, a null CollationKey is returned.
     * @see java.text.CollationKey
     * @see java.text.Collator#compare
     */
    public abstract CollationKey getCollationKey(String source);

    /**
     * Convenience method for comparing the equality of two strings based on
     * this Collator's collation rules.
     * @param source the source string to be compared with.
     * @param target the target string to be compared with.
     * @return true if the strings are equal according to the collation
     * rules.  false, otherwise.
     * @see java.text.Collator#compare
     */
    public boolean equals(String source, String target)
    {
        // Android-changed: remove use of unnecessary EQUAL constant.
        return (compare(source, target) == 0);
    }

    /**
     * Returns this Collator's strength property.  The strength property determines
     * the minimum level of difference considered significant during comparison.
     * See the Collator class description for an example of use.
     * @return this Collator's current strength property.
     * @see java.text.Collator#setStrength
     * @see java.text.Collator#PRIMARY
     * @see java.text.Collator#SECONDARY
     * @see java.text.Collator#TERTIARY
     * @see java.text.Collator#IDENTICAL
     */
    public abstract int getStrength();

    /**
     * Sets this Collator's strength property.  The strength property determines
     * the minimum level of difference considered significant during comparison.
     * See the Collator class description for an example of use.
     * @param newStrength  the new strength value.
     * @see java.text.Collator#getStrength
     * @see java.text.Collator#PRIMARY
     * @see java.text.Collator#SECONDARY
     * @see java.text.Collator#TERTIARY
     * @see java.text.Collator#IDENTICAL
     * @exception  IllegalArgumentException If the new strength value is not one of
     * PRIMARY, SECONDARY, TERTIARY or IDENTICAL.
     */
    public abstract void setStrength(int value);

    /**
     * Returns the decomposition rule for this collator.
     *
     * @return the decomposition rule, either {@code NO_DECOMPOSITION} or
     *         {@code CANONICAL_DECOMPOSITION}. {@code FULL_DECOMPOSITION} is
     *         not supported.
     */
    public abstract int getDecomposition();

    /**
     * Returns a {@code Collator} instance which is appropriate for the user's default
     * {@code Locale}.
     * See "<a href="../util/Locale.html#default_locale">Be wary of the default locale</a>".
     */
    public static Collator getInstance() {
        return getInstance(Locale.getDefault());
    }
    /**
     * Sets the decomposition rule for this collator.
     *
     * @param value
     *            the decomposition rule, either {@code NO_DECOMPOSITION} or
     *            {@code CANONICAL_DECOMPOSITION}. {@code FULL_DECOMPOSITION}
     *            is not supported.
     * @throws IllegalArgumentException
     *            if the provided decomposition rule is not valid. This includes
     *            {@code FULL_DECOMPOSITION}.
     */
    public abstract void setDecomposition(int value);

    // Android-changed: Removed javadoc references to CollatorProvider.
    /**
     * Returns an array of all locales for which the
     * <code>getInstance</code> methods of this class can return
     * localized instances.
     *
     * @return An array of locales for which localized
     *         <code>Collator</code> instances are available.
     */
    public static synchronized Locale[] getAvailableLocales() {
      // Android-changed: Removed reference to CollatorProvider. Switched to ICU.
        return ICU.getAvailableLocales();
    }

    // Android-changed: improve clone() documentation.
    /**
     * Returns a new collator with the same decomposition mode and
     * strength value as this collator.
     *
     * @return a shallow copy of this collator.
     * @see java.lang.Cloneable
     */
    @Override
    public Object clone()
    {
      try {
        return (IOSCollator) super.clone();
      } catch (CloneNotSupportedException e) {
        throw new AssertionError(e);
      }
    }

    /**
     * Generates the hash code for this Collator.
     */
    @Override
    public abstract int hashCode();

    /**
     * Returns a {@code Collator} instance which is appropriate for the user's default
     * {@code Locale}.
     * See "<a href="../util/Locale.html#default_locale">Be wary of the default locale</a>".
     */

    // BEGIN Android-removed: Fields and constants.
    /*
    private int strength = 0;
    private int decmp = 0;
    private static final ConcurrentMap<Locale, SoftReference<Collator>> cache
            = new ConcurrentHashMap<>();

    //
    // FIXME: These three constants should be removed.
    //
    /**
     * LESS is returned if source string is compared to be less than target
     * string in the compare() method.
     * @see java.text.Collator#compare
     *
    static final int LESS = -1;
    /**
     * EQUAL is returned if source string is compared to be equal to target
     * string in the compare() method.
     * @see java.text.Collator#compare
     *
    static final int EQUAL = 0;
    /**
     * GREATER is returned if source string is compared to be greater than
     * target string in the compare() method.
     * @see java.text.Collator#compare
     *
    static final int GREATER = 1;
    */
    // END Android-removed: Fields and constants.
 }
