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

import com.dmdirc.events.DisplayPropertyMap;
import com.dmdirc.config.provider.AggregateConfigProvider;
import com.dmdirc.config.provider.ConfigChangeListener;
import com.dmdirc.util.collections.ListenerList;

import java.awt.Font;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import javax.swing.UIManager;

/**
 * Data contained in a TextPane.
 */
public class IRCDocument implements Serializable, ConfigChangeListener, Document {

    /** A version number for this class. */
    private static final long serialVersionUID = 4;
    /** List of lines of text. */
    private final List<Line> lines;
    /** Listener list. */
    private final ListenerList listeners;
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

    public IRCDocument(final AggregateConfigProvider configManager, final Styliser styliser) {
        this.configManager = configManager;
        this.styliser = styliser;

        lines = new ArrayList<>();
        listeners = new ListenerList();

        frameBufferSize = configManager.getOptionInt("ui", "frameBufferSize", false);

        configManager.addChangeListener("ui", "textPaneFontSize", this);
        configManager.addChangeListener("ui", "textPaneFontName", this);
        configManager.addChangeListener("ui", "frameBufferSize", this);

        setCachedSettings();
    }

    @Override
    public int getNumLines() {
        synchronized (lines) {
            return lines.size();
        }
    }

    @Override
    public Line getLine(final int lineNumber) {
        synchronized (lines) {
            return lines.get(lineNumber);
        }
    }

    @Override
    public void addText(final LocalDateTime timestamp, final DisplayPropertyMap displayPropertyMap,
        final String text) {
        final int start;
        synchronized (lines) {
            start = lines.size();
            lines.add(new IRCLine(styliser, formatTimestamp(timestamp), text, displayPropertyMap,
                    fontSize, fontName));
        }
        fireLinesAdded(start, 1);
    }

    private String formatTimestamp(final LocalDateTime timestamp) {
        return Formatter.formatMessage(configManager, "timestamp", timestamp);
    }

    @Override
    public void trim(final int numLines) {
        synchronized (lines) {
            if (frameBufferSize != null && frameBufferSize > 0) {
                final int i = lines.size() - numLines;
                if (i > 0) {
                    lines.subList(0, i).clear();
                    fireTrimmed(numLines, i);
                }
            }
        }
    }

    @Override
    public void clear() {
        synchronized (lines) {
            lines.clear();
        }
        fireCleared();
    }

    @Override
    public void addIRCDocumentListener(final DocumentListener listener) {
        if (listener == null) {
            return;
        }

        listeners.add(DocumentListener.class, listener);
    }

    @Override
    public void removeIRCDocumentListener(final DocumentListener listener) {
        listeners.remove(DocumentListener.class, listener);
    }

    /**
     * Fires the lines added method on all listeners.
     *
     * @param index Index of the added line
     * @param size  Number of lines added
     */
    protected void fireLinesAdded(final int index, final int size) {
        for (DocumentListener listener
                : listeners.get(DocumentListener.class)) {
            listener.linesAdded(index, size, lines.size());
        }
        trim(frameBufferSize);
    }

    /**
     * Fires the trimmed method on all listeners.
     *
     * @param newSize      New document size
     * @param trimmedLines Number of trimmed lines
     */
    protected void fireTrimmed(final int newSize, final int trimmedLines) {
        for (DocumentListener listener
                : listeners.get(DocumentListener.class)) {
            listener.trimmed(newSize, trimmedLines);
        }
    }

    /**
     * fires the cleared method on all listeners.
     */
    protected void fireCleared() {
        listeners.get(DocumentListener.class).forEach(DocumentListener::cleared);
    }

    /**
     * fires the need repaint method on all listeners.
     */
    protected void fireRepaintNeeded() {
        listeners.get(DocumentListener.class).forEach(DocumentListener::repaintNeeded);
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

    @Override
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
        frameBufferSize = configManager.getOptionInt("ui", "frameBufferSize", true);
        trim(frameBufferSize);
    }

    @Override
    public void configChanged(final String domain, final String key) {
        setCachedSettings();
        synchronized (lines) {
            for (Line line : lines) {
                line.setFontName(fontName);
                line.setFontSize(fontSize);
            }
        }
        fireRepaintNeeded();
    }
}
