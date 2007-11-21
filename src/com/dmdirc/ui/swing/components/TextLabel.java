/*
JMultiLineLabel.java
A simple label supporting multiple lines.
Created: 28 January 1998
Version: $Revision: 1.6 $ %D%
Module By: Michael Mulvaney
Applied Research Laboratories, The University of Texas at Austin
 */

package com.dmdirc.ui.swing.components;

import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.util.StringTokenizer;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;

/*------------------------------------------------------------------------------
class
JMultiLineLabel
------------------------------------------------------------------------------*/

/*------------------------------------------------------------------------------
class
JMultiLineLabel
------------------------------------------------------------------------------*/
public class TextLabel extends JTextArea implements ComponentListener {
    // ---
    /**
     * Alignment stuff.
     */
    public final static int LEFT = SwingConstants.LEFT;
    public final static int RIGHT = SwingConstants.RIGHT;
    public final static int CENTER = SwingConstants.CENTER;
    int margin_height = 5,
            margin_width = 5,
            alignment,
            num_lines,
            line_ascent,
            line_height,
            max_width;
    String[] lines;
    boolean haveMeasured = false;
    int columns = 42;
    FontMetrics metric;

    /*
     * Constructors
     */
    public TextLabel() {
        this("", TextLabel.LEFT);
    }

    public TextLabel(String label) {
        this(label, TextLabel.LEFT);
    }

    public TextLabel(String label, int alignment) {
        metric = getFontMetrics(getFont());

        setEditable(false);
        setOpaque(false);

        // The JTextArea has an etched border around it, so get rid of it.
        //setBorder(BorderFactory.createEmptyBorder(0,0,0,0); 
        setBorder(null);

        // Find out the length of the string.  If the length is less than
        // the default number of columns, make the number of columns the
        // same as the string.

        if (label != null) {
            int length = label.length();
            if (length < columns) {
                columns = length;
            }
        }

        setText(wrap(label));

    }

    // Public functions
    public void setText(String s) {
        super.setText(wrap(s));
    }

    public int getAlignment() {
        return alignment;
    }

    public void setAlignment(int a) {
        alignment = a;
        repaint();
    }

    public Dimension getPreferredSize() {
        int width,
                height;

        String text = wrap(getText());

        /* -- */

        // First, find out how wide this puppy is
        width = getLongestLineWidth(text);

        // Now, the height
        height = super.getPreferredSize().height;

        return new Dimension(width, height);

    }

    /**
     * Insert new lines in the string
     *
     * @lineLength Number of characters to wrap the line at.
     */
    public String wrap(String text) {
        if (text == null) {
            return text;
        }

        return (WordWrap.wrap(text, columns, null));

    }

    ///////////////////////////////////////////////////////////////////////////////
    // Private functions
    ///////////////////////////////////////////////////////////////////////////////
    private int getLongestLineWidth(String wrappedText) {
        int length = 0;
        int maxLength = 0;
        StringTokenizer tk = new StringTokenizer(wrappedText, "\n");

        while (tk.hasMoreElements()) {
            length = metric.stringWidth((String) tk.nextElement());
            if (length > maxLength) {
                maxLength = length;
            }
        }

        return maxLength;
    }

    @Override
    public void componentResized(ComponentEvent e) {
        super.setText(wrap(getText()));
    }

    @Override
    public void componentMoved(ComponentEvent e) {
        //Ignore
    }

    @Override
    public void componentShown(ComponentEvent e) {
        //Ignore
    }

    @Override
    public void componentHidden(ComponentEvent e) {
        //Ignore
    }
}
class WordWrap {

    /* -- */
    /**
     *
     * This method takes a string and wraps it to a line length of no more than
     * wrap_length.  If prepend is not null, each resulting line will be prefixed
     * with the prepend string.  In that case, resultant line length will be no
     * more than wrap_length + prepend.length()
     *
     */
    public static String wrap(String inString, int wrap_length, String prepend) {
        char[] charAry;

        int p,
                p2,
                offset = 0,
                marker;

        StringBuffer result = new StringBuffer();

        /* -- */

        if (inString == null) {
            return null;
        }

        if (wrap_length < 0) {
            throw new IllegalArgumentException("bad params");
        }

        if (prepend != null) {
            result.append(prepend);
        }

        charAry = inString.trim().toCharArray();

        p = marker = 0;

        // each time through the loop, p starts out pointing to the same char as marker

        while (marker < charAry.length) {
            if (wrap_length > 0) {
                while (p < charAry.length && (charAry[p] != '\n') && ((p -
                        marker) <
                        wrap_length)) {
                    p++;
                }
            } else {
                p = charAry.length;
            }

            if (p == charAry.length) {
                result.append(inString.substring(marker, p));
                return result.toString();
            }

            if (charAry[p] == '\n') {
                /* We've got a newline.  This newline is bound to have
                terminated the while loop above.  Step p back one
                character so that the isspace(*p) check below will detect
                that it hit the \n, and will do the right thing. */


                result.append(inString.substring(marker, p + 1));

                if (prepend != null) {
                    result.append(prepend);
                }
                
                p = marker = p + 1;

                continue;
            }

            p2 = p - 1;

            /* We've either hit the end of the string, or we've
            gotten past the wrap_length.  Back p2 up to the last space
            before the wrap_length, if there is such a space.
            Note that if the next character in the string (the character
            immediately after the break point) is a space, we don't need
            to back up at all.  We'll just print up to our current
            location, do the newline, and skip to the next line. */

            if (p < charAry.length) {
                if (isspace(charAry[p])) {
                    offset = 1;	/* the next character is white space.  We'll
                want to skip that. */
                } else {
                    /* back p2 up to the last white space before the break point */

                    while ((p2 > marker) && !isspace(charAry[p2])) {
                        p2--;
                    }

                    offset = 0;
                }
            }

            /* If the line was completely filled (no place to break),
            we'll just copy the whole line out and force a break. */

            if (p2 == marker) {
                p2 = p - 1;
            }

            if (!isspace(charAry[p2])) {
                /* If weren't were able to back up to a space, copy
                out the whole line, including the break character 
                (in this case, we'll be making the string one
                character longer by inserting a newline). */

                result.append(inString.substring(marker, p2 + 1));
            } else {
                /* The break character is whitespace.  We'll
                copy out the characters up to but not
                including the break character, which
                we will effectively replace with a
                newline. */

                result.append(inString.substring(marker, p2));
            }

            /* If we have not reached the end of the string, newline */

            if (p < charAry.length) {
                result.append("\n");

                if (prepend != null) {
                    result.append(prepend);
                }
            }

            p = marker = p2 + 1 + offset;
        }

        return result.toString();
    }

    public static String wrap(String inString, int wrap_length) {
        return wrap(inString, wrap_length, null);
    }

    public static boolean isspace(char c) {
        return (c == '\n' || c == ' ' || c == '\t');
    }
}
