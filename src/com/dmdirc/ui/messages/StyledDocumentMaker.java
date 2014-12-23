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

import com.dmdirc.util.colours.Colour;

import java.awt.Color;

import javax.swing.UIManager;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

/**
 * {@link StyledMessageMaker} that produces a swing {@link StyledDocument}.
 */
public class StyledDocumentMaker implements StyledMessageMaker<StyledDocument> {

    private final StyledDocument document;
    private final SimpleAttributeSet attribs;

    public StyledDocumentMaker(final StyledDocument document) {
        this(document, new SimpleAttributeSet());
    }

    public StyledDocumentMaker() {
        this(new DefaultStyledDocument(), new SimpleAttributeSet());
    }

    public StyledDocumentMaker(final StyledDocument document, final SimpleAttributeSet attribs) {
        this.document = document;
        this.attribs = attribs;
    }

    @Override
    public StyledDocument getStyledMessage() {
        return document;
    }

    @Override
    public void resetAllStyles() {
        if (attribs.containsAttribute(StyleConstants.Bold, Boolean.TRUE)) {
            attribs.removeAttribute(StyleConstants.Bold);
        }
        if (attribs.containsAttribute(StyleConstants.Underline, Boolean.TRUE)) {
            attribs.removeAttribute(StyleConstants.Underline);
        }
        if (attribs.containsAttribute(StyleConstants.Italic, Boolean.TRUE)) {
            attribs.removeAttribute(StyleConstants.Italic);
        }
        if (attribs.containsAttribute(StyleConstants.FontFamily, "monospaced")) {
            final Object defaultFont = attribs.getAttribute("DefaultFontFamily");
            attribs.removeAttribute(StyleConstants.FontFamily);
            attribs.addAttribute(StyleConstants.FontFamily, defaultFont);
        }

        attribs.addAttribute("DefaultFontFamily", UIManager.getFont("TextPane.font"));

        resetColours();
    }

    @Override
    public void resetColours() {
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

    @Override
    public void appendString(final String text) {
        try {
            document.insertString(document.getLength(), text, attribs);
        } catch (BadLocationException ex) {
            // Shouldn't happen?
        }
    }

    @Override
    public void toggleBold() {
        toggleAttribute(StyleConstants.Bold);
    }

    @Override
    public void toggleUnderline() {
        toggleAttribute(StyleConstants.Underline);
    }

    @Override
    public void toggleItalic() {
        toggleAttribute(StyleConstants.Italic);
    }

    @Override
    public void startHyperlink(final String url) {
        attribs.addAttribute(IRCTextAttribute.HYPERLINK, url);
    }

    @Override
    public void endHyperlink() {
        attribs.removeAttribute(IRCTextAttribute.HYPERLINK);
    }

    @Override
    public void toggleHyperlinkStyle(final Colour colour) {
        toggleLink(IRCTextAttribute.HYPERLINK, colour);
    }

    @Override
    public void startChannelLink(final String channel) {
        attribs.addAttribute(IRCTextAttribute.CHANNEL, channel);
    }

    @Override
    public void endChannelLink() {
        attribs.removeAttribute(IRCTextAttribute.CHANNEL);
    }

    @Override
    public void toggleChannelLinkStyle(final Colour colour) {
        toggleLink(IRCTextAttribute.CHANNEL, colour);
    }

    @Override
    public void startNicknameLink(final String nickname) {
        attribs.addAttribute(IRCTextAttribute.NICKNAME, nickname);
    }

    @Override
    public void endNicknameLink() {
        attribs.removeAttribute(IRCTextAttribute.NICKNAME);
    }

    @Override
    public void toggleFixedWidth() {
        if (attribs.containsAttribute(StyleConstants.FontFamily, "monospaced")) {
            attribs.removeAttribute(StyleConstants.FontFamily);
        } else {
            attribs.removeAttribute(StyleConstants.FontFamily);
            attribs.addAttribute(StyleConstants.FontFamily, "monospaced");
        }
    }

    @Override
    public void setForeground(final Colour colour) {
        if (attribs.isDefined(StyleConstants.Foreground)) {
            attribs.removeAttribute(StyleConstants.Foreground);
        }
        attribs.addAttribute(StyleConstants.Foreground, convertColour(colour));
    }

    @Override
    public void setDefaultForeground(final Colour colour) {
        attribs.addAttribute("DefaultForeground", convertColour(colour));
    }

    @Override
    public void setBackground(final Colour colour) {
        if (attribs.isDefined(StyleConstants.Background)) {
            attribs.removeAttribute(StyleConstants.Background);
        }
        attribs.addAttribute(StyleConstants.Background, convertColour(colour));
    }

    @Override
    public void setDefaultBackground(final Colour colour) {
        attribs.addAttribute("DefaultBackground", convertColour(colour));
    }

    @Override
    public void startSmilie(final String smilie) {
        attribs.addAttribute(IRCTextAttribute.SMILEY, smilie);
    }

    @Override
    public void endSmilie() {
        attribs.removeAttribute(IRCTextAttribute.SMILEY);
    }

    @Override
    public void startToolTip(final String tooltip) {
        attribs.addAttribute(IRCTextAttribute.TOOLTIP, tooltip);
    }

    @Override
    public void endToolTip() {
        attribs.removeAttribute(IRCTextAttribute.TOOLTIP);
    }

    /**
     * Toggles the specified attribute. If the attribute exists in the attribute set, it is removed.
     * Otherwise, it is added with a value of Boolean.True.
     *
     * @param attrib  The attribute to toggle
     */
    private void toggleAttribute(final Object attrib) {
        if (attribs.containsAttribute(attrib, Boolean.TRUE)) {
            attribs.removeAttribute(attrib);
        } else {
            attribs.addAttribute(attrib, Boolean.TRUE);
        }
    }

    /**
     * Toggles the attributes for a link.
     *
     * @param attribute The attribute indicating whether the link is open or closed
     * @param colour    The colour to colour the link
     */
    private void toggleLink(final IRCTextAttribute attribute, final Colour colour) {

        if (attribs.getAttribute(attribute) == null) {
            // Add the hyperlink style

            if (attribs.containsAttribute(StyleConstants.Underline, Boolean.TRUE)) {
                attribs.addAttribute("restoreUnderline", Boolean.TRUE);
            } else {
                attribs.addAttribute(StyleConstants.Underline, Boolean.TRUE);
            }

            if (colour != null) {
                final Object foreground = attribs.getAttribute(StyleConstants.Foreground);

                if (foreground != null) {
                    attribs.addAttribute("restoreColour", foreground);
                    attribs.removeAttribute(StyleConstants.Foreground);
                }

                attribs.addAttribute(StyleConstants.Foreground, convertColour(colour));
            }

        } else {
            // Remove the hyperlink style

            if (attribs.containsAttribute("restoreUnderline", Boolean.TRUE)) {
                attribs.removeAttribute("restoreUnderline");
            } else {
                attribs.removeAttribute(StyleConstants.Underline);
            }

            if (colour != null) {
                attribs.removeAttribute(StyleConstants.Foreground);
                final Object foreground = attribs.getAttribute("restoreColour");
                if (foreground != null) {
                    attribs.addAttribute(StyleConstants.Foreground, foreground);
                    attribs.removeAttribute("restoreColour");
                }
            }
        }
    }

    /**
     * Converts a DMDirc {@link Colour} into an AWT-specific {@link Color} by copying the values of
     * the red, green and blue channels.
     *
     * @param colour The colour to be converted
     *
     * @return A corresponding AWT colour
     */
    private static Color convertColour(final Colour colour) {
        return new Color(colour.getRed(), colour.getGreen(), colour.getBlue());
    }

}
