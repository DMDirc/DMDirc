/*
 * Copyright (c) 2006-2008 Chris Smith, Shane Mc Cormack, Gregory Holmes
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
package com.dmdirc.addons.ui_swing.textpane;

import com.dmdirc.config.ConfigManager;
import com.dmdirc.logger.ErrorLevel;
import com.dmdirc.logger.Logger;
import com.dmdirc.ui.messages.IRCTextAttribute;
import com.dmdirc.ui.messages.Styliser;

import java.awt.Font;
import java.awt.font.TextAttribute;
import java.text.AttributedString;
import java.util.Arrays;
import java.util.Enumeration;

import javax.swing.UIManager;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Element;
import javax.swing.text.StyleConstants.CharacterConstants;
import javax.swing.text.StyleConstants.ColorConstants;
import javax.swing.text.StyleConstants.FontConstants;
import javax.swing.text.StyledDocument;

/**
 * Represents a line of text in IRC.
 */
public class Line {

    private final String[] lineParts;
    private final ConfigManager config;

    /**
     * Creates a new line.
     * 
     * @param lineParts Parts of the line
     * @param config Configuration manager for this line
     */
    public Line(final String[] lineParts, final ConfigManager config) {
        this.lineParts = lineParts;
        this.config = config;
    }

    /**
     * Returns the line parts of this line.
     *
     * @return Lines parts
     */
    public String[] getLineParts() {
        return lineParts;
    }

    /**
     * Returns the length of the specified line
     * 
     * @return Length of the line
     */
    public int getLength() {
        int length = 0;
        for (String linePart : lineParts) {
            length += linePart.length();
        }
        return length;
    }

    /**
     * Returns the Line text at the specified number.
     *
     * @return Line at the specified number or null
     */
    public String getText() {
        StringBuilder lineText = new StringBuilder();
        for (String linePart : lineParts) {
            lineText.append(linePart);
        }
        return Styliser.stipControlCodes(lineText.toString());
    }

    /**
     * Returns the Line text at the specified number.
     *
     * @return Line at the specified number or null
     *
     * @since 0.6.3
     */
    public String getStyledText() {
        StringBuilder lineText = new StringBuilder();
        for (String linePart : lineParts) {
            lineText.append(linePart);
        }
        return lineText.toString();
    }

    /**
     * Converts a StyledDocument into an AttributedString.
     *
     * @return AttributedString representing the specified StyledDocument
     */
    public AttributedString getStyled() {
        final StyledDocument doc = Styliser.getStyledString(lineParts);

        AttributedString attString = null;
        final Element line = doc.getParagraphElement(0);
        try {
            attString = new AttributedString(line.getDocument().getText(0,
                    line.getDocument().getLength()));
        } catch (BadLocationException ex) {
            Logger.userError(ErrorLevel.MEDIUM,
                    "Unable to insert styled string: " +
                    ex.getMessage());
        }

        if (attString.getIterator().getEndIndex() != 0) {
            final Font defaultFont = UIManager.getFont("TextPane.font");
            final int fontSize;
            final String fontName;
            if (config.hasOptionString("ui", "textPaneFontName")) {
                fontName = config.getOption("ui", "textPaneFontName");
            } else {
                fontName = defaultFont.getName();
            }
            if (config.hasOptionString("ui", "textPaneFontSize")) {
                fontSize = config.getOptionInt("ui", "textPaneFontSize");
            } else {
                fontSize = defaultFont.getSize();
            }
            final Font font = new Font(fontName, Font.PLAIN, fontSize);
            attString.addAttribute(TextAttribute.SIZE, font.getSize());
            attString.addAttribute(TextAttribute.FAMILY, font.getFamily());
        }

        for (int i = 0; i < line.getElementCount(); i++) {
            final Element element = line.getElement(i);

            final AttributeSet as = element.getAttributes();
            final Enumeration<?> ae = as.getAttributeNames();

            while (ae.hasMoreElements()) {
                final Object attrib = ae.nextElement();

                if (attrib == IRCTextAttribute.HYPERLINK) {
                    //Hyperlink
                    attString.addAttribute(IRCTextAttribute.HYPERLINK,
                            as.getAttribute(attrib), element.getStartOffset(),
                            element.getEndOffset());
                } else if (attrib == IRCTextAttribute.NICKNAME) {
                    //Nicknames
                    attString.addAttribute(IRCTextAttribute.NICKNAME,
                            as.getAttribute(attrib), element.getStartOffset(),
                            element.getEndOffset());
                } else if (attrib == IRCTextAttribute.CHANNEL) {
                    //Channels
                    attString.addAttribute(IRCTextAttribute.CHANNEL,
                            as.getAttribute(attrib), element.getStartOffset(),
                            element.getEndOffset());
                } else if (attrib == ColorConstants.Foreground) {
                    //Foreground
                    attString.addAttribute(TextAttribute.FOREGROUND,
                            as.getAttribute(attrib), element.getStartOffset(),
                            element.getEndOffset());
                } else if (attrib == ColorConstants.Background) {
                    //Background
                    attString.addAttribute(TextAttribute.BACKGROUND,
                            as.getAttribute(attrib), element.getStartOffset(),
                            element.getEndOffset());
                } else if (attrib == FontConstants.Bold) {
                    //Bold
                    attString.addAttribute(TextAttribute.WEIGHT,
                            TextAttribute.WEIGHT_BOLD, element.getStartOffset(),
                            element.getEndOffset());
                } else if (attrib == FontConstants.Family) {
                    //Family
                    attString.addAttribute(TextAttribute.FAMILY,
                            as.getAttribute(attrib), element.getStartOffset(),
                            element.getEndOffset());
                } else if (attrib == FontConstants.Italic) {
                    //italics
                    attString.addAttribute(TextAttribute.POSTURE,
                            TextAttribute.POSTURE_OBLIQUE,
                            element.getStartOffset(),
                            element.getEndOffset());
                } else if (attrib == CharacterConstants.Underline) {
                    //Underline
                    attString.addAttribute(TextAttribute.UNDERLINE,
                            TextAttribute.UNDERLINE_ON, element.getStartOffset(),
                            element.getEndOffset());
                } else if (attrib == IRCTextAttribute.SMILEY) {
                    /* Lets avoid showing broken smileys shall we!
                    final Image image = IconManager.getIconManager().getImage((String) as.getAttribute(attrib)).
                    getScaledInstance(14, 14, Image.SCALE_DEFAULT);
                    ImageGraphicAttribute iga = new ImageGraphicAttribute(image, 
                    (int) BOTTOM_ALIGNMENT, 5, 5);
                    attString.addAttribute(TextAttribute.CHAR_REPLACEMENT, iga,
                    element.getStartOffset(), element.getEndOffset());
                     */
                }
            }
        }

        if (attString.getIterator().getEndIndex() == 0) {
            return new AttributedString("\n");
        }

        return attString;
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Line) {
            return Arrays.equals(((Line) obj).getLineParts(), getLineParts());
        }
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        return getLineParts().hashCode();
    }
}