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

import java.io.Serializable;
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
    
    /** List of stylised lines of text. */
    private final List<AttributedString> iterators;
    
    /** Listener list. */
    private final EventListenerList listeners;
    
    /** Creates a new instance of IRCDocument. */
    protected IRCDocument() {
        iterators = new ArrayList<AttributedString>();
        listeners = new EventListenerList();
    }
    
    /**
     * Returns the number of lines in this document.
     *
     * @return Number of lines
     */
    protected int getNumLines() {
        return iterators.size();
    }
    
    /**
     * Returns the Line at the specified number.
     *
     * @param lineNumber Line number to retrieve
     *
     * @return Line at the specified number or null
     */
    protected AttributedString getLine(final int lineNumber) {
        return iterators.get(lineNumber);
    }
    
    /**
     * Adds the stylised string to the canvas.
     * @param text stylised string to add to the text
     */
    protected void addText(final AttributedString text) {
        synchronized (iterators) {
            iterators.add(text);
            fireLineAdded(iterators.indexOf(text));
        }
    }
    
    /**
     * Trims the document to the specified number of lines.
     *
     * @param numLines Number of lines to trim the document to
     */
    public void trim(final int numLines) {
        synchronized (iterators) {
            while (iterators.size() > numLines) {
                iterators.remove(0);
            }
            fireTrimmed();
        }
    }
    
    /** Clears all lines from the document. */
    protected void clear() {
        synchronized (iterators) {
            iterators.clear();
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
                ((IRCDocumentListener) listenerList[i + 1]).lineAdded(index, iterators.size());
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
                ((IRCDocumentListener) listenerList[i + 1]).trimmed(iterators.size());
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
}

