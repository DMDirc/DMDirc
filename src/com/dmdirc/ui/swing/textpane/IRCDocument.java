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

package com.dmdirc.ui.swing.textpane;

import com.dmdirc.util.RollingList;

import java.io.Serializable;
import java.text.AttributedCharacterIterator;
import java.text.AttributedString;
import java.util.ArrayList;
import java.util.List;

import javax.swing.event.EventListenerList;

/**
 * Data contained in a TextPane.
 */
public final class IRCDocument implements Serializable {

    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 4;
    /** List of lines of text. */
    private final List<Line> lines;
    /** Listener list. */
    private final EventListenerList listeners;
    /** Cached lines. */
    private RollingList<Line> cachedLines;
    /** Cached attributed strings. */
    private RollingList<AttributedString> cachedStrings;

    /** Creates a new instance of IRCDocument. */
    public IRCDocument() {
        lines = new ArrayList<Line>();
        listeners = new EventListenerList();

        cachedLines = new RollingList<Line>(50);
        cachedStrings = new RollingList<AttributedString>(50);
    }

    /**
     * Returns the number of lines in this document.
     *
     * @return Number of lines
     */
    public int getNumLines() {
        return lines.size();
    }

    /**
     * Returns the Line at the specified number.
     *
     * @param lineNumber Line number to retrieve
     *
     * @return Line at the specified number or null
     */
    public Line getLine(final int lineNumber) {
        return lines.get(lineNumber);
    }

    /**
     * Adds the stylised string to the canvas.
     * 
     * @param text stylised string to add to the text
     */
    public void addText(final Line text) {
        synchronized (lines) {
            lines.add(text);
            fireLineAdded(lines.indexOf(text));
        }
    }

    /**
     * Adds the stylised string to the canvas.
     * 
     * @param text stylised string to add to the text
     */
    public void addText(final List<Line> text) {
        synchronized (lines) {
            final int start = lines.size();
            for (Line string : text) {
                lines.add(string);
            }
            fireLinesAdded(start, text.size());
        }
    }

    /**
     * Trims the document to the specified number of lines.
     *
     * @param numLines Number of lines to trim the document to
     */
    public void trim(final int numLines) {
        synchronized (lines) {
            while (lines.size() > numLines) {
                lines.remove(0);
            }
            fireTrimmed();
        }
    }

    /** Clears all lines from the document. */
    protected void clear() {
        synchronized (lines) {
            lines.clear();
            fireCleared();
        }
    }

    /**
     * Adds a IRCDocumentListener to the listener list.
     *
     * @param listener Listener to add
     */
    public void addIRCDocumentListener(final IRCDocumentListener listener) {
        synchronized (listeners) {
            if (listener == null) {
                return;
            }
            listeners.add(IRCDocumentListener.class, listener);
        }
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
     * Fires the line added method on all listeners.
     *
     * @param index Index of the added line
     */
    protected void fireLineAdded(final int index) {
        final Object[] listenerList = listeners.getListenerList();
        for (int i = 0; i < listenerList.length; i += 2) {
            if (listenerList[i] == IRCDocumentListener.class) {
                ((IRCDocumentListener) listenerList[i + 1]).lineAdded(index,
                                                                      lines.size());
            }
        }
    }

    /**
     * Fires the lines added method on all listeners.
     *
     * @param index Index of the added line
     * @param size Number of lines added
     */
    protected void fireLinesAdded(final int index, final int size) {
        final Object[] listenerList = listeners.getListenerList();
        for (int i = 0; i < listenerList.length; i += 2) {
            if (listenerList[i] == IRCDocumentListener.class) {
                ((IRCDocumentListener) listenerList[i + 1]).linesAdded(index,
                                                                       size,
                                                                       lines.
                        size());
            }
        }
    }

    /**
     * Fires the trimmed method on all listeners.
     */
    protected void fireTrimmed() {
        final Object[] listenerList = listeners.getListenerList();
        for (int i = 0; i < listenerList.length; i += 2) {
            if (listenerList[i] == IRCDocumentListener.class) {
                ((IRCDocumentListener) listenerList[i + 1]).trimmed(lines.size());
            }
        }
    }

    /**
     * fires the cleared method on all listeners.
     */
    protected void fireCleared() {
        final Object[] listenerList = listeners.getListenerList();
        for (int i = 0; i < listenerList.length; i += 2) {
            if (listenerList[i] == IRCDocumentListener.class) {
                ((IRCDocumentListener) listenerList[i + 1]).cleared();
            }
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
    public AttributedCharacterIterator getStyledLine(final Line line) {
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
}

