/*
 * Copyright (c) 2006-2013 DMDirc Developers
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

import com.dmdirc.interfaces.config.AggregateConfigProvider;
import com.dmdirc.interfaces.config.ConfigChangeListener;
import com.dmdirc.util.collections.ListenerList;
import com.dmdirc.util.collections.RollingList;

import java.awt.Font;
import java.io.Serializable;
import java.text.AttributedCharacterIterator;
import java.text.AttributedString;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.UIManager;

/**
 * Data contained in a TextPane.
 */
public class IRCDocument implements Serializable, ConfigChangeListener {

    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 4;
    /** List of lines of text. */
    private final List<Line> lines;
    /** Listener list. */
    private final ListenerList listeners;
    /** Cached lines. */
    private final RollingList<Line> cachedLines;
    /** Cached attributed strings. */
    private final RollingList<AttributedString> cachedStrings;
    /** Config Manager for getting settings. */
    private final AggregateConfigProvider configManager;
    /** This document's styliser. */
    private final Styliser styliser;
    /** Font size. */
    private int fontSize;
    /** Font name. */
    private String fontName;
    /** Frame buffer size. */
    private Integer frameBufferSize;

    /**
     * Creates a new instance of IRCDocument.
     *
     * @param configManager Config Manager for required settings.
     * @param styliser Styliser to style text
     * @since 0.6.3
     */
    public IRCDocument(final AggregateConfigProvider configManager,
            final Styliser styliser) {
        this.configManager = configManager;
        this.styliser = styliser;

        lines = new ArrayList<>();
        listeners = new ListenerList();

        cachedLines = new RollingList<>(50);
        cachedStrings = new RollingList<>(50);
        frameBufferSize = configManager.getOptionInt("ui", "frameBufferSize", false);

        configManager.addChangeListener("ui", "textPaneFontSize", this);
        configManager.addChangeListener("ui", "textPaneFontName", this);
        configManager.addChangeListener("ui", "frameBufferSize", this);

        setCachedSettings();
    }

    /**
     * Returns the number of lines in this document.
     *
     * @return Number of lines
     */
    public int getNumLines() {
        synchronized (lines) {
            return lines.size();
        }
    }

    /**
     * Returns the Line at the specified number.
     *
     * @param lineNumber Line number to retrieve
     *
     * @return Line at the specified number or null
     */
    public Line getLine(final int lineNumber) {
        synchronized (lines) {
            return lines.get(lineNumber);
        }
    }

     /**
     * Adds the stylised strings to the canvas.  Each part of the array is
     * treated as a separate namespace for stylising but are all added on the
     * same line.
     *
     * @param text stylised string to add to the document
     */
    public void addText(final String[] text) {
        addText(Arrays.asList(new String[][]{text, }));
    }

    /**
     * Adds the stylised strings to the canvas.  Each part of the array is
     * treated as a separate namespace for stylising but are all added on the
     * same line.
     *
     * @param text stylised strings to add to the document
     */
    public void addText(final List<String[]> text) {
        final int start;
        synchronized (lines) {
            start = lines.size();
            for (String[] string : text) {
                lines.add(new Line(styliser, string, fontSize, fontName));
            }
        }
        fireLinesAdded(start, text.size());
    }

    /**
     * Adds the stylised string to the canvas.  Each part of the array is
     * treated as a separate namespace for stylising but are all added on the
     * same line.
     *
     * @param text stylised string to add to the document
     * @param lineHeights line heights for the new lines
     */
    public void addText(final List<String[]> text,
            final List<Integer> lineHeights) {
        final int start;
        synchronized (lines) {
            start = lines.size();
            for (int i = 0; i < text.size(); i++) {
                final String[] string = text.get(i);
                final int lineHeight = lineHeights.get(i);
                lines.add(new Line(styliser, string, lineHeight, fontName));
            }
        }
        fireLinesAdded(start, text.size());
    }

    /**
     * Trims the document to the specified number of lines.
     *
     * @param numLines Number of lines to trim the document to
     */
    public void trim(final int numLines) {
        synchronized (lines) {
            if (frameBufferSize != null && frameBufferSize > 0) {
                int i = 0;
                while (lines.size() > numLines) {
                    i++;
                    lines.remove(0);
                }
                if (i > 0) {
                    fireTrimmed(numLines, i);
                }
            }
        }
    }

    /** Clears all lines from the document. */
    public void clear() {
        synchronized (lines) {
            lines.clear();
        }
        fireCleared();
    }

    /**
     * Adds a IRCDocumentListener to the listener list.
     *
     * @param listener Listener to add
     */
    public void addIRCDocumentListener(final IRCDocumentListener listener) {
        if (listener == null) {
            return;
        }

        listeners.add(IRCDocumentListener.class, listener);
    }

    /**
     * Removes a IRCDocumentListener from the listener list.
     *
     * @param listener Listener to remove
     */
    public void removeIRCDocumentListener(final IRCDocumentListener listener) {
        listeners.remove(IRCDocumentListener.class, listener);
    }

    /**
     * Fires the lines added method on all listeners.
     *
     * @param index Index of the added line
     * @param size Number of lines added
     */
    protected void fireLinesAdded(final int index, final int size) {
        for (IRCDocumentListener listener
                : listeners.get(IRCDocumentListener.class)) {
            listener.linesAdded(index, size, lines.size());
        }
        trim(frameBufferSize);
    }

    /**
     * Fires the trimmed method on all listeners.
     *
     * @param newSize New document size
     * @param trimedLines Number of trimmed lines
     */
    protected void fireTrimmed(final int newSize, final int trimedLines) {
        for (IRCDocumentListener listener
                : listeners.get(IRCDocumentListener.class)) {
            listener.trimmed(newSize, trimedLines);
        }
    }

    /**
     * fires the cleared method on all listeners.
     */
    protected void fireCleared() {
        for (IRCDocumentListener listener
                : listeners.get(IRCDocumentListener.class)) {
            listener.cleared();
        }
    }

    /**
     * fires the need repaint method on all listeners.
     */
    protected void fireRepaintNeeded() {
        for (IRCDocumentListener listener
                : listeners.get(IRCDocumentListener.class)) {
            listener.repaintNeeded();
        }
    }

    /**
     * Returns an attributed character iterator for a particular line,
     * utilising the document cache where possible.
     *
     * @param line Line to be styled
     *
     * @return Styled line
     */
    protected AttributedCharacterIterator getStyledLine(final Line line) {
        synchronized (lines) {
            AttributedString styledLine = null;
            if (cachedLines.contains(line)) {
                final int index = cachedLines.getList().indexOf(line);
                styledLine = cachedStrings.get(index);
            }

            if (styledLine == null) {
                styledLine = line.getStyled();
                cachedLines.add(line);
                cachedStrings.add(styledLine);
            }

            return styledLine.getIterator();
        }
    }

    /**
     * Returns an attributed string for a particular line, utilising the
     * document cache where possible.
     *
     * @param line Line number to be styled
     *
     * @return Styled line
     */
    public AttributedCharacterIterator getStyledLine(final int line) {
        return getStyledLine(getLine(line));
    }

    /**
     * Returns the line height of the specified line.
     *
     * @param line Line
     *
     * @return Line height
     */
    protected int getLineHeight(final Line line) {
        return line.getFontSize();
    }

    /**
     * Returns the line height of the specified.
     *
     * @param line Line
     *
     * @return Line height
     */
    public int getLineHeight(final int line) {
        return getLineHeight(getLine(line));
    }

    /**
     * Sets all the cached settings in this document.
     */
    private void setCachedSettings() {
        final Font defaultFont = UIManager.getFont("TextPane.font");
        if (configManager.hasOptionString("ui", "textPaneFontName")) {
            fontName = configManager.getOption("ui", "textPaneFontName");
        } else {
            fontName = defaultFont.getName();
        }
        if (configManager.hasOptionString("ui", "textPaneFontSize")) {
            fontSize = configManager.getOptionInt("ui", "textPaneFontSize");
        } else {
            fontSize = defaultFont.getSize();
        }
        frameBufferSize = configManager.getOptionInt("ui", "frameBufferSize", false);
        trim(frameBufferSize);
    }

    /** {@inheritDoc} */
    @Override
    public void configChanged(final String domain, final String key) {
        setCachedSettings();
        cachedLines.clear();
        cachedStrings.clear();
        synchronized (lines) {
            for (Line line : lines) {
                line.setFontName(fontName);
                line.setFontSize(fontSize);
            }
        }
        fireRepaintNeeded();
    }
}

