/*
 * Copyright (c) 2006-2013 DMDirc Developers
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.dmdirc.actions;

import com.dmdirc.interfaces.actions.ActionComparison;

import java.util.regex.PatternSyntaxException;

/**
 * A CoreActionComparison represents a possible comparison between two types of
 * data used in an action condition.
 */
public enum CoreActionComparison implements ActionComparison {

    /** Compares a string to another using a regular expression. */
    STRING_REGEX {
        /** {@inheritDoc} */
        @Override
        public boolean test(final Object arg1, final Object arg2) {
            try {
                return ((String) arg1).matches((String) arg2);
            } catch (PatternSyntaxException pse) {
                return false;
            }
        }
        /** {@inheritDoc} */
        @Override
        public Class<?> appliesTo() { return String.class; }
        /** {@inheritDoc} */
        @Override
        public String getName() { return "matches regex"; }
    },

    /** Compares if two strings content are the same, case insensitive. */
    STRING_EQUALS {
        /** {@inheritDoc} */
        @Override
        public boolean test(final Object arg1, final Object arg2) {
            return ((String) arg1).equalsIgnoreCase((String) arg2);
        }
        /** {@inheritDoc} */
        @Override
        public Class<?> appliesTo() { return String.class; }
        /** {@inheritDoc} */
        @Override
        public String getName() { return "equals"; }
    },

    /** Compares if two strings content aren't the same, case insensitive. */
    STRING_NEQUALS {
        /** {@inheritDoc} */
        @Override
        public boolean test(final Object arg1, final Object arg2) {
            return !STRING_EQUALS.test(arg1, arg2);
        }
        /** {@inheritDoc} */
        @Override
        public Class<?> appliesTo() { return String.class; }
        /** {@inheritDoc} */
        @Override
        public String getName() { return "does not equal"; }
    },

    /** Checks if the string starts with another strings. */
    STRING_STARTSWITH {
        /** {@inheritDoc} */
        @Override
        public boolean test(final Object arg1, final Object arg2) {
            return ((String) arg1).startsWith((String) arg2);
        }
        /** {@inheritDoc} */
        @Override
        public Class<?> appliesTo() { return String.class; }
        /** {@inheritDoc} */
        @Override
        public String getName() { return "starts with"; }
    },

    /** Checks if the string containts another string. */
    STRING_CONTAINS {
        /** {@inheritDoc} */
        @Override
        public boolean test(final Object arg1, final Object arg2) {
            return ((String) arg1).indexOf((String) arg2) != -1;
        }
        /** {@inheritDoc} */
        @Override
        public Class<?> appliesTo() { return String.class; }
        /** {@inheritDoc} */
        @Override
        public String getName() { return "contains"; }
    },

    /** Checks if the string doesn't containt another string. */
    STRING_NCONTAINS {
        /** {@inheritDoc} */
        @Override
        public boolean test(final Object arg1, final Object arg2) {
            return ((String) arg1).indexOf((String) arg2) == -1;
        }
        /** {@inheritDoc} */
        @Override
        public Class<?> appliesTo() { return String.class; }
        /** {@inheritDoc} */
        @Override
        public String getName() { return "doesn't contain"; }
    },

    /** Checks if two boolean values are equal. */
    BOOL_IS {
        /** {@inheritDoc} */
        @Override
        public boolean test(final Object arg1, final Object arg2) {
            return ((Boolean) arg1).equals(Boolean.valueOf((String) arg2));
        }
        /** {@inheritDoc} */
        @Override
        public Class<?> appliesTo() { return Boolean.class; }
        /** {@inheritDoc} */
        @Override
        public String getName() { return "is"; }
    },

    /** Checks if the int is equals to another int. */
    INT_EQUALS {
        /** {@inheritDoc} */
        @Override
        public boolean test(final Object arg1, final Object arg2) {
            try {
                return 0 == ((Integer) arg1).compareTo(Integer.parseInt((String) arg2));
            } catch (NumberFormatException ex) {
                return false;
            }
        }
        /** {@inheritDoc} */
        @Override
        public Class<?> appliesTo() { return Integer.class; }
        /** {@inheritDoc} */
        @Override
        public String getName() { return "equals"; }
    },

    /** Checks if the int is larger than another int. */
    INT_GREATER {
        /** {@inheritDoc} */
        @Override
        public boolean test(final Object arg1, final Object arg2) {
            try {
                return 0 < ((Integer) arg1).compareTo(Integer.parseInt((String) arg2));
            } catch (NumberFormatException ex) {
                return false;
            }
        }
        /** {@inheritDoc} */
        @Override
        public Class<?> appliesTo() { return Integer.class; }
        /** {@inheritDoc} */
        @Override
        public String getName() { return "is greater than"; }
    },

    /** Checks if the int is smaller than another int. */
    INT_LESS {
        /** {@inheritDoc} */
        @Override
        public boolean test(final Object arg1, final Object arg2) {
            try {
                return 0 > ((Integer) arg1).compareTo(Integer.parseInt((String) arg2));
            } catch (NumberFormatException ex) {
                return false;
            }
        }
        /** {@inheritDoc} */
        @Override
        public Class<?> appliesTo() { return Integer.class; }
        /** {@inheritDoc} */
        @Override
        public String getName() { return "is less than"; }
    };

}
