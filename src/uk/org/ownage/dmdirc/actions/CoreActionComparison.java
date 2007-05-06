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
 * A CoreActionComparison represents a possible comparison between two types of
 * data used in an action condition.
 * @author chris
 */
public enum CoreActionComparison implements ActionComparison {
    
    STRING_EQUALS {
        public boolean test(final Object arg1, final Object arg2) {
            return ((String) arg1).equalsIgnoreCase((String) arg2);
        }
        public Class appliesTo() { return String.class; }
    },
    
    STRING_NEQUALS {
        public boolean test(final Object arg1, final Object arg2) {
            return !STRING_EQUALS.test(arg1, arg2);
        }
        public Class appliesTo() { return String.class; }
    },
    
    STRING_STARTSWITH {
        public boolean test(final Object arg1, final Object arg2) {
            return ((String) arg1).startsWith((String) arg2);
        }
        public Class appliesTo() { return String.class; }
    },
    
    STRING_CONTAINS {
        public boolean test(final Object arg1, final Object arg2) {
            return ((String) arg1).indexOf((String) arg2) != -1;
        }
        public Class appliesTo() { return String.class; }
    },
    
    COLOUR_EQUALS {
        public boolean test(final Object arg1, final Object arg2) {
            return ((Color) arg1).equals(ColourManager.parseColour((String) arg2));
        }        
        public Class appliesTo() { return Color.class; }
    },
    
    COLOUR_NEQUALS {
        public boolean test(final Object arg1, final Object arg2) {
            return !COLOUR_EQUALS.test(arg1, arg2);
        }
        public Class appliesTo() { return Color.class; }
    },
    
    INT_EQUALS {
        public boolean test(final Object arg1, final Object arg2) {
            try {
                return 0 == ((Integer) arg1).compareTo(Integer.parseInt((String) arg2));
            } catch (NumberFormatException ex) {
                return false;
            }
        }
        public Class appliesTo() { return Integer.class; }
    },
    
    INT_GREATER {
        public boolean test(final Object arg1, final Object arg2) {
            try {
                return 0 < ((Integer) arg1).compareTo(Integer.parseInt((String) arg2));
            } catch (NumberFormatException ex) {
                return false;
            }
        }
        public Class appliesTo() { return Integer.class; }        
    },
    
    INT_LESS {
        public boolean test(final Object arg1, final Object arg2) {
            try {
                return 0 > ((Integer) arg1).compareTo(Integer.parseInt((String) arg2));
            } catch (NumberFormatException ex) {
                return false;
            }
        }
        public Class appliesTo() { return Integer.class; }            
    };
    
    /** {@inheritDoc} */
    public abstract boolean test(final Object arg1, final Object arg2);
    
    /** {@inheritDoc} */
    public abstract Class appliesTo();
    
}
