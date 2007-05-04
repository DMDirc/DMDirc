/*
 * Copyright (c) 2006-2007 Chris Smith, Shane Mc Cormack, Gregory Holmes
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

package uk.org.ownage.dmdirc.actions;

import java.awt.Color;

import uk.org.ownage.dmdirc.ui.messages.ColourManager;

/**
 * An ActionComparison represents a possible comparison between two types of
 * data used in an action condition.
 * @author chris
 */
public enum ActionComparison {
    
    STRING_EQUALS {
        boolean test(final Object arg1, final Object arg2) { return ((String) arg1).equalsIgnoreCase((String) arg2); }
        Class appliesTo() { return String.class; }
    },
    
    STRING_NEQUALS {
        boolean test(final Object arg1, final Object arg2) { return !STRING_EQUALS.test(arg1, arg2); }
        Class appliesTo() { return String.class; }
    },
    
    STRING_STARTSWITH {
        boolean test(final Object arg1, final Object arg2) { return ((String) arg1).startsWith((String) arg2); }
        Class appliesTo() { return String.class; }
    },
    
    STRING_CONTAINS {
        boolean test(final Object arg1, final Object arg2) { return ((String) arg1).indexOf((String) arg2) != -1; }
        Class appliesTo() { return String.class; }
    },
    
    COLOUR_EQUALS {
        boolean test(final Object arg1, final Object arg2) {
            return ((Color) arg1).equals(ColourManager.parseColour((String) arg2));
        }        
        Class appliesTo() { return Color.class; }
    },
    
    COLOUR_NEQUALS {
        boolean test(final Object arg1, final Object arg2) { return !COLOUR_EQUALS.test(arg1, arg2); }
        Class appliesTo() { return Color.class; }
    },
    
    INT_EQUALS {
        boolean test(final Object arg1, final Object arg2) {
            try {
                return 0 == ((Integer) arg1).compareTo(Integer.parseInt((String) arg2));
            } catch (NumberFormatException ex) {
                return false;
            }
        }
        Class appliesTo() { return Integer.class; }
    },
    
    INT_GREATER {
        boolean test(final Object arg1, final Object arg2) {
            try {
                return 0 < ((Integer) arg1).compareTo(Integer.parseInt((String) arg2));
            } catch (NumberFormatException ex) {
                return false;
            }
        }
        Class appliesTo() { return Integer.class; }        
    },
    
    INT_LESS {
        boolean test(final Object arg1, final Object arg2) {
            try {
                return 0 > ((Integer) arg1).compareTo(Integer.parseInt((String) arg2));
            } catch (NumberFormatException ex) {
                return false;
            }
        }
        Class appliesTo() { return Integer.class; }            
    };
    
    /**
     * Tests the specified arguments against each other.
     * @param arg1 The first argument
     * @param arg2 The second argument
     * @return True iff the comparison between the args succeeds, false
     * otherwise
     */
    abstract boolean test(final Object arg1, final Object arg2);
    
    /**
     * Returns the class that this comparison deals with.
     * @return The Class that this comparison can compare
     */
    abstract Class appliesTo();
    
}
