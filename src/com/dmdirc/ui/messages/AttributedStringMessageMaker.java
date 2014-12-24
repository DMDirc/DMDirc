/*
 * Copyright (c) 2006-2014 DMDirc Developers
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

package com.dmdirc.ui.messages;

import com.dmdirc.ui.core.util.ExtendedAttributedString;

import java.awt.Font;
import java.awt.font.TextAttribute;
import java.text.AttributedString;
import java.util.Enumeration;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Element;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

/**
 * Creates an attributed string from a styled document.
 */
public class AttributedStringMessageMaker
        extends DelegatingStyledMessageMaker<StyledDocument, AttributedString> {

    private String fontName;
    private int fontSize;

    public AttributedStringMessageMaker() {
        super(new StyledDocumentMaker());
    }

    @Override
    public int getMaximumFontSize() {
        return fontSize;
    }

    @Override
    public void setDefaultFont(final String fontName, final int fontSize) {
        this.fontName = fontName;
        this.fontSize = fontSize;
    }

    @Override
    protected AttributedString convert(final StyledDocument styledMessage) {
        final Element line = styledMessage.getParagraphElement(0);
        final AttributedString attString;
        try {
            attString = new AttributedString(
                    line.getDocument().getText(0,
                    line.getDocument().getLength()));
        } catch (BadLocationException ex) {
            // Shouldn't happen
            return null;
        }

        if (attString.getIterator().getEndIndex() != 0) {
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
                } else if (attrib == IRCTextAttribute.TOOLTIP) {
                    //Tooltips
                    attString.addAttribute(IRCTextAttribute.TOOLTIP,
                            as.getAttribute(attrib), element.getStartOffset(),
                            element.getEndOffset());
                } else if (attrib == StyleConstants.Foreground) {
                    //Foreground
                    attString.addAttribute(TextAttribute.FOREGROUND,
                            as.getAttribute(attrib), element.getStartOffset(),
                            element.getEndOffset());
                } else if (attrib == StyleConstants.Background) {
                    //Background
                    attString.addAttribute(TextAttribute.BACKGROUND,
                            as.getAttribute(attrib), element.getStartOffset(),
                            element.getEndOffset());
                } else if (attrib == StyleConstants.Bold) {
                    //Bold
                    attString.addAttribute(TextAttribute.WEIGHT,
                            TextAttribute.WEIGHT_BOLD, element.getStartOffset(),
                            element.getEndOffset());
                } else if (attrib == StyleConstants.Family) {
                    //Family
                    attString.addAttribute(TextAttribute.FAMILY,
                            as.getAttribute(attrib), element.getStartOffset(),
                            element.getEndOffset());
                } else if (attrib == StyleConstants.Italic) {
                    //italics
                    attString.addAttribute(TextAttribute.POSTURE,
                            TextAttribute.POSTURE_OBLIQUE,
                            element.getStartOffset(),
                            element.getEndOffset());
                } else if (attrib == StyleConstants.Underline) {
                    //Underline
                    attString.addAttribute(TextAttribute.UNDERLINE,
                            TextAttribute.UNDERLINE_ON, element.getStartOffset(),
                            element.getEndOffset());
                }
            }
        }

        final ExtendedAttributedString attributedString =
                attString.getIterator().getEndIndex() == 0
                        ? new ExtendedAttributedString(new AttributedString("\n"), fontSize)
                        : new ExtendedAttributedString(attString, fontSize);
        fontSize = attributedString.getMaxLineHeight();
        return attributedString.getAttributedString();
    }

}
