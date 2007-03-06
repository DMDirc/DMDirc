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
 * The styliser applies IRC styles to text. Styles are indicated by various
 * control codes which are a de-facto IRC standard.
 * @author chris
 */
public class Styliser {
    
    /** Creates a new instance of Styliser */
    public Styliser() {
    }
    
    /**
     * Stylises the specified string and adds it to the passed StyledDocument.
     * @param doc The document which the output should be added to
     * @param add The line to be stylised and added
     */
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
                    position += readControlChars(add.substring(position),
                            attribs, (position == 0));
                }
            }
            
        } catch (BadLocationException ex) {
            Logger.error(ErrorLevel.WARNING, ex);
        }
    }
    
    /**
     * Strips all recognised control codes from the input string
     * @param input the String to be stripped
     * @return a copy of the input with control codes removed
     */    
    static public String stipControlCodes(String input) {
            int position = 0;
            boolean cont = true;
            String output = "";
            SimpleAttributeSet attribs = new SimpleAttributeSet();
            
            while (position < input.length()) {
                String next = readUntilControl(input.substring(position));
                
                output = output.concat(next);
                
                position += next.length();
                
                if (position < input.length()) {
                    position += readControlChars(input.substring(position),
                            attribs, (position == 0));
                }
            }    
            
            return output;
    }
    
    /**
     * Returns a substring of the input string such that no control codes are present
     * in the output. If the returned value isn't the same as the input, then the
     * character immediately after is a control character.
     * @param input The string to read from
     * @return A substring of the input containing no control characters
     */
    static private String readUntilControl(String input) {
        int pos = input.length();
        
        // Bold
        pos = checkChar(pos, input.indexOf(2));
        // Underline
        pos = checkChar(pos, input.indexOf(31));
        // Stop all formatting
        pos = checkChar(pos, input.indexOf(15));
        // Colour
        pos = checkChar(pos, input.indexOf(3));
        
        return input.substring(0, pos);
    }
    
    /**
     * Helper function used in readUntilControl. Checks if i is a valid index of
     * the string (i.e., it's not -1), and then returns the minimum of pos and i.
     * @param pos The current position in the string
     * @param i The index of the first occurance of some character
     * @return The new position (see implementation)
     */
    private static int checkChar(int pos, int i) {
        if (i < pos && i != -1) { return i; }
        return pos;
    }    
    
    /**
     * Reads the first control character from the input string (and any arguments
     * it takes), and applies it to the specified attribute set.
     * @return The number of characters read as control characters
     * @param string The string to read from
     * @param attribs The attribute set that new attributes will be applied to
     * @param isStart Whether this is at the start of the string or not
     */
    private static int readControlChars(String string, SimpleAttributeSet attribs,
            boolean isStart) {
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
        
        // Colours
        if (string.charAt(0) == 3) {
            int count = 1;
            // This isn't too nice!
            if (isInt(string.charAt(count))) {
                int foreground = string.charAt(count) - 48;
                count++;
                if (isInt(string.charAt(count))) {
                    foreground = foreground*10 + (string.charAt(count) - 48);
                    count++;
                }
                foreground = foreground % 16;
                setForeground(attribs, foreground);
                if (isStart) {
                    setDefaultForeground(attribs, foreground);
                }
                
                // Now background
                if (string.charAt(count) == ',') {
                    if (isInt(string.charAt(count+1))) {
                        int background = string.charAt(count+1);
                        count += 2; // Comma and first digit
                        if (isInt(string.charAt(count))) {
                            background = background*10 + (string.charAt(count) - 48);
                            count++;
                        }
                        background = background % 16;
                        setBackground(attribs, background);
                    }
                }
            } else {
                resetColour(attribs);
            }
            return count;
        }
        
        return 0;
    }
    
    /**
     * Determines if the specified character represents a single integer (i.e. 0-9).
     * @param c The character to check
     * @return True iff the character is in the range [0-9], false otherwise
     */
    private static boolean isInt(char c) {
        return (c >= 48 && c <= 57);
    }    
       
    /**
     * Toggles the specified attribute. If the attribute exists in the attribute
     * set, it is removed. Otherwise, it is added with a value of Boolean.True.
     * @param attribs The attribute set to check
     * @param attrib The attribute to toggle
     */
    private static void toggleAttribute(SimpleAttributeSet attribs, Object attrib) {
        if (attribs.containsAttribute(attrib, Boolean.TRUE)) {
            attribs.removeAttribute(attrib);
        } else {
            attribs.addAttribute(attrib, Boolean.TRUE);
        }
    }
    
    /**
     * Resets all attributes in the specified attribute list
     * @param attribs The attribute list whose attributes should be reset
     */
    private static void resetAttributes(SimpleAttributeSet attribs) {
        if (attribs.containsAttribute(StyleConstants.FontConstants.Bold, Boolean.TRUE)) {
            attribs.removeAttribute(StyleConstants.FontConstants.Bold);
        }
        if (attribs.containsAttribute(StyleConstants.FontConstants.Underline, Boolean.TRUE)) {
            attribs.removeAttribute(StyleConstants.FontConstants.Underline);
        }
        resetColour(attribs);
    }
       
    /**
     * Resets the colour attributes in the specified attribute set
     * @param attribs The attribute set whose colour attributes should be reset
     */
    private static void resetColour(SimpleAttributeSet attribs) {
        if (attribs.isDefined(StyleConstants.Foreground)) {
            attribs.removeAttribute(StyleConstants.Foreground);
        }
        if (attribs.isDefined("DefaultForeground")) {
            attribs.addAttribute(StyleConstants.Foreground, 
                    attribs.getAttribute("DefaultForeground"));
        }
        if (attribs.isDefined(StyleConstants.Background)) {
            attribs.removeAttribute(StyleConstants.Background);
        }
        if (attribs.isDefined("DefaultBackground")) {
            attribs.addAttribute(StyleConstants.Background, 
                    attribs.getAttribute("DefaultBackground"));
        }        
    }
    
    /**
     * Sets the foreground colour in the specified attribute set to the colour
     * corresponding to the specified colour code.
     * @param attribs The attribute set to modify
     * @param foreground The colour code of the new foreground colour
     */
    private static void setForeground(SimpleAttributeSet attribs, int foreground) {
        if (attribs.isDefined(StyleConstants.Foreground)) {
            attribs.removeAttribute(StyleConstants.Foreground);
        }
        attribs.addAttribute(StyleConstants.Foreground, ColourManager.getColour(foreground));
    }
    
    /**
     * Sets the background colour in the specified attribute set to the colour
     * corresponding to the specified colour code.
     * @param attribs The attribute set to modify
     * @param background The colour code of the new background colour
     */
    private static void setBackground(SimpleAttributeSet attribs, int background) {
        if (attribs.isDefined(StyleConstants.Background)) {
            attribs.removeAttribute(StyleConstants.Background);
        }
        attribs.addAttribute(StyleConstants.Background, ColourManager.getColour(background));
    }

    /**
     * Sets the default foreground colour (used after an empty ctrl+k or a ctrl+o)
     * @param attribs The attribute set to apply this default on
     * @param foreground The default foreground colour
     */
    private static void setDefaultForeground(SimpleAttributeSet attribs, int foreground) {
        attribs.addAttribute("DefaultForeground", ColourManager.getColour(foreground));
    }
    
    /**
     * Sets the default background colour (used after an empty ctrl+k or a ctrl+o)
     * @param attribs The attribute set to apply this default on
     * @param foreground The default background colour
     */    
    private static void setDefaultBackground(SimpleAttributeSet attribs, int background) {
        attribs.addAttribute("DefaultBackground", ColourManager.getColour(background));
    }    
    
}
