/*
 * Copyright (c) 2006-2017 DMDirc Developers
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the
 * Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS
 * OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.dmdirc.ui.messages;

import com.dmdirc.util.colours.Colour;

/**
 * Delegates handling of styled messages to another maker, allowing the output to be mutated.
 */
public abstract class DelegatingStyledMessageMaker<S, T> implements StyledMessageMaker<T> {

    private final StyledMessageMaker<S> delegatedMaker;

    public DelegatingStyledMessageMaker(final StyledMessageMaker<S> delegatedMaker) {
        this.delegatedMaker = delegatedMaker;
    }

    protected abstract T convert(S styledMessage);

    @Override
    public T getStyledMessage() {
        return convert(delegatedMaker.getStyledMessage());
    }

    @Override
    public void resetAllStyles() {
        delegatedMaker.resetAllStyles();
    }

    @Override
    public void resetColours() {
        delegatedMaker.resetColours();
    }

    @Override
    public void appendString(final String text) {
        delegatedMaker.appendString(text);
    }

    @Override
    public void toggleBold() {
        delegatedMaker.toggleBold();
    }

    @Override
    public void toggleUnderline() {
        delegatedMaker.toggleUnderline();
    }

    @Override
    public void toggleItalic() {
        delegatedMaker.toggleItalic();
    }

    @Override
    public void startHyperlink(final String url) {
        delegatedMaker.startHyperlink(url);
    }

    @Override
    public void endHyperlink() {
        delegatedMaker.endHyperlink();
    }

    @Override
    public void toggleHyperlinkStyle(final Colour colour) {
        delegatedMaker.toggleHyperlinkStyle(colour);
    }

    @Override
    public void startChannelLink(final String channel) {
        delegatedMaker.startChannelLink(channel);
    }

    @Override
    public void endChannelLink() {
        delegatedMaker.endChannelLink();
    }

    @Override
    public void toggleChannelLinkStyle(final Colour colour) {
        delegatedMaker.toggleChannelLinkStyle(colour);
    }

    @Override
    public void startNicknameLink(final String nickname) {
        delegatedMaker.startNicknameLink(nickname);
    }

    @Override
    public void endNicknameLink() {
        delegatedMaker.endNicknameLink();
    }

    @Override
    public void toggleFixedWidth() {
        delegatedMaker.toggleFixedWidth();
    }

    @Override
    public void setForeground(final Colour colour) {
        delegatedMaker.setForeground(colour);
    }

    @Override
    public void setDefaultForeground(final Colour colour) {
        delegatedMaker.setDefaultForeground(colour);
    }

    @Override
    public void setBackground(final Colour colour) {
        delegatedMaker.setBackground(colour);
    }

    @Override
    public void setDefaultBackground(final Colour colour) {
        delegatedMaker.setDefaultBackground(colour);
    }

    @Override
    public void startSmilie(final String smilie) {
        delegatedMaker.startSmilie(smilie);
    }

    @Override
    public void endSmilie() {
        delegatedMaker.endSmilie();
    }

    @Override
    public void startToolTip(final String tooltip) {
        delegatedMaker.startToolTip(tooltip);
    }

    @Override
    public void endToolTip() {
        delegatedMaker.endToolTip();
    }

    @Override
    public void setDefaultFont(final String fontName, final int fontSize) {
        delegatedMaker.setDefaultFont(fontName, fontSize);
    }

    @Override
    public int getMaximumFontSize() {
        return delegatedMaker.getMaximumFontSize();
    }

    @Override
    public void clear() {
        delegatedMaker.clear();
    }

}
