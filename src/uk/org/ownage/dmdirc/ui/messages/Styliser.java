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

package uk.org.ownage.dmdirc.ui.messages;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import uk.org.ownage.dmdirc.logger.ErrorLevel;
import uk.org.ownage.dmdirc.logger.Logger;

/**
 *
 * @author chris
 */
public class Styliser {
    
    /** Creates a new instance of Styliser */
    public Styliser() {
    }
    
    static public void addStyledString(StyledDocument doc, String add) {
        try {
            int offset = doc.getLength();
            int position = 0;
            boolean cont = true;
            SimpleAttributeSet attribs = new SimpleAttributeSet();
            
            while (position < add.length()) {
                String next = readUntilControl(add.substring(position));
                
                doc.insertString(offset, next, attribs);
                
                position += next.length();
                offset += next.length();
                
                if (position < add.length()) {
                    position += readControlChars(add.substring(position), attribs);
                }
            }
            
        } catch (BadLocationException ex) {
            Logger.error(ErrorLevel.WARNING, ex);
        }
    }
    
    static private String readUntilControl(String input) {
        int pos = input.length();
        
        // Bold
        pos = checkChar(pos, input.indexOf(2));
        // Underline
        pos = checkChar(pos, input.indexOf(31));
        // Stop all formatting
        pos = checkChar(pos, input.indexOf(15));
        
        return input.substring(0, pos);
    }
    
    private static int readControlChars(String string, SimpleAttributeSet attribs) {
        // Bold
        if (string.charAt(0) == 2) {
            toggleAttribute(attribs, StyleConstants.FontConstants.Bold);
            return 1;
        }
        
        // Underline
        if (string.charAt(0) == 31) {
            toggleAttribute(attribs, StyleConstants.FontConstants.Underline);
            return 1;
        }        
        
        // Stop formatting
        if (string.charAt(0) == 15) {
            resetAttributes(attribs);
            return 1;
        }        
        return 0;
    }

    private static int checkChar(int pos, int i) {
        if (i < pos && i != -1) { return i; }
        return pos;
    }

    private static void toggleAttribute(SimpleAttributeSet attribs, Object attrib) {
        if (attribs.containsAttribute(attrib, Boolean.TRUE)) {
            attribs.removeAttribute(attrib);
        } else {
            attribs.addAttribute(attrib, Boolean.TRUE);
        }
    }

    private static void resetAttributes(SimpleAttributeSet attribs) {
        if (attribs.containsAttribute(StyleConstants.FontConstants.Bold, Boolean.TRUE)) {
            attribs.removeAttribute(StyleConstants.FontConstants.Bold);
        }
        if (attribs.containsAttribute(StyleConstants.FontConstants.Underline, Boolean.TRUE)) {
            attribs.removeAttribute(StyleConstants.FontConstants.Underline);
        }        
    }
    
}
