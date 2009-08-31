/*
 * 
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

package com.dmdirc.ui.core.util;

import com.dmdirc.config.ConfigManager;
import com.dmdirc.logger.ErrorLevel;
import com.dmdirc.logger.Logger;
import com.dmdirc.ui.messages.IRCTextAttribute;
import com.dmdirc.ui.messages.Styliser;

import java.awt.Font;
import java.awt.font.TextAttribute;
import java.text.AttributedString;
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
 * Core UI Utilities.
 */
public class Utils {

    /** Prevent instantiation. */
    private Utils() {}

    /**
     * Converts a StyledDocument into an AttributedString.
     *
     * @param lineParts Parts of a line comprising the whole
     * @param config Config manager
     *
     * @return AttributedString representing the specified StyledDocument
     */
    public static ExtendedAttributedString getAttributedString(String[] lineParts,
            final ConfigManager config) {
        final int lineHeight;
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

        final Font defaultFont = UIManager.getFont("TextPane.font");
        String fontName = null;
        if (config.hasOptionString("ui", "textPaneFontName")) {
            fontName = config.getOption("ui", "textPaneFontName");
        } else {
            fontName = defaultFont.getName();
        }
        if (config.hasOptionString("ui", "textPaneFontSize")) {
            lineHeight = config.getOptionInt("ui", "textPaneFontSize");
        } else {
            lineHeight = defaultFont.getSize();
        }
        if (attString.getIterator().getEndIndex() != 0) {
            final Font font = new Font(fontName, Font.PLAIN, lineHeight);
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
            return new ExtendedAttributedString(new AttributedString("\n"), lineHeight);
        }

        return new ExtendedAttributedString(attString, lineHeight);
    }
}
