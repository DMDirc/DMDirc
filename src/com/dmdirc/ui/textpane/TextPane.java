/*
 * Copyright (c) 2006-2007 Chris Smith, Shane Mc Cormack, Gregory Holmes
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

package com.dmdirc.ui.textpane;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.text.AttributedCharacterIterator;
import java.text.AttributedString;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JScrollBar;
import javax.swing.event.EventListenerList;

import com.dmdirc.ui.components.Frame;

/**
 * Styled, scrollable text pane.
 */
public final class TextPane extends JComponent implements AdjustmentListener,
        MouseWheelListener {
    
    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 3;
    
    /** Parent frame container. */
    private final Frame owner;
    
    /** Scrollbar for the component. */
    private final JScrollBar scrollBar;
    /** Canvas object, used to draw text. */
    private final TextPaneCanvas canvas;
    /** IRCDocument. */
    private final IRCDocument document;
    
    /** Listener list. */
    private final EventListenerList textPaneListeners;
    
    /**
     * Creates a new instance of TextPane.
     *
     * @param owner Parent frame
     */
    public TextPane(final Frame owner) {
        super();
        
        this.owner = owner;
        
        textPaneListeners = new EventListenerList();
        
        document = new IRCDocument();
        
        this.setMinimumSize(new Dimension(0, 0));
        
        this.setLayout(new BorderLayout());
        
        canvas = new TextPaneCanvas(this, document);
        
        this.setBorder(BorderFactory.createEtchedBorder());
        
        this.add(canvas, BorderLayout.CENTER);
        
        
        scrollBar = new JScrollBar(JScrollBar.VERTICAL);
        this.add(scrollBar, BorderLayout.LINE_END);
        
        this.setAutoscrolls(true);
        
        scrollBar.setMaximum(document.getNumLines());
        scrollBar.setBlockIncrement(10);
        scrollBar.setUnitIncrement(1);
        scrollBar.addAdjustmentListener(this);
        
        this.addMouseWheelListener(this);
    }
    
    /**
     * Adds text to the textpane.
     * @param text text to add
     */
    public void addText(final String text) {
        addText(new AttributedString(text));
    }
    
    /**
     * Adds styled text to the textpane.
     * @param text styled text to add
     */
    public void addText(final AttributedString text) {
        document.addText(text);
        setScrollBarMax();
    }
    
    /**
     * Sets the new position for the scrollbar and the associated position
     * to render the text from.
     * @param position new position of the scrollbar
     */
    public void setScrollBarPosition(final int position) {
        scrollBar.setValue(position);
        canvas.setScrollBarPosition(position);
    }
    
    public void setScrollBarMax() {
        final int lines = document.getNumLines();
        if (lines <= 1) {
            scrollBar.setEnabled(false);
        } else {
            scrollBar.setEnabled(true);
        }
        scrollBar.setMaximum(lines);
        if (!scrollBar.getValueIsAdjusting()
        && (scrollBar.getValue() == scrollBar.getMaximum() - 1)) {
            setScrollBarPosition(lines);
        }
    }
    
    /** {@inheritDoc}. */
    public void adjustmentValueChanged(final AdjustmentEvent e) {
        if (e.getValue() < document.getNumLines()) {
            scrollBar.setValue(e.getValue());
            canvas.setScrollBarPosition(e.getValue());
        }
    }
    
    /** {@inheritDoc}. */
    public void mouseWheelMoved(final MouseWheelEvent e) {
        if (scrollBar.isEnabled()) {
            if (e.getWheelRotation() > 0) {
                setScrollBarPosition(scrollBar.getValue() + e.getScrollAmount());
            } else {
                setScrollBarPosition(scrollBar.getValue() - e.getScrollAmount());
            }
        }
    }
    
    /**
     * Returns the selected text.
     *
     * @return Selected text
     */
    public String getSelectedText() {
        final StringBuffer selectedText = new StringBuffer();
        final int[] selectedRange = canvas.getSelectedRange();
        
        for (int i = selectedRange[0]; i <= selectedRange[2]; i++) {
            if (i != selectedRange[0]) {
                selectedText.append('\n');
            }
            final AttributedCharacterIterator iterator = document.getLine(i).getIterator();
            if (selectedRange[2] == selectedRange[0]) {
                //loop through range
                selectedText.append(getTextFromLine(iterator, selectedRange[1], selectedRange[3]));
            } else if (i == selectedRange[0]) {
                //loop from start of range to the end
                selectedText.append(getTextFromLine(iterator, selectedRange[1], iterator.getEndIndex()));
            } else if (i == selectedRange[2]) {
                //loop from start to end of range
                selectedText.append(getTextFromLine(iterator, 0, selectedRange[3]));
            } else {
                //loop the whole line
                selectedText.append(getTextFromLine(iterator, 0, iterator.getEndIndex()));
            }
        }
        
        return selectedText.toString();
    }
    
    /**
     * Returns the entire text from the specified line.
     *
     * @param line line to retrieve text from
     *
     * @return Text from the line
     */
    public String getTextFromLine(final int line) {
        final AttributedCharacterIterator iterator = document.getLine(line).getIterator();
        return getTextFromLine(iterator, 0, iterator.getEndIndex());
    }
    
    /**
     * Returns the range of text from the specified iterator.
     *
     * @param line line to retrieve text from
     * @param start Start index in the iterator
     * @param end End index in the iterator
     *
     * @return Text in the range from the line
     */
    public String getTextFromLine(final int line, final int start,
            final int end) {
        return getTextFromLine(document.getLine(line).getIterator(), start, end);
    }
    
    /**
     * Returns the range of text from the specified iterator.
     *
     * @param iterator iterator to get text from
     * @param start Start index in the iterator
     * @param end End index in the iterator
     *
     * @return Text in the range from the line
     */
    public String getTextFromLine(final AttributedCharacterIterator iterator,
            final int start, final int end) {
        final StringBuffer text = new StringBuffer();
        for (iterator.setIndex(iterator.getBeginIndex());
        iterator.getIndex() < iterator.getEndIndex(); iterator.next()) {
            text.append(iterator.current());
        }
        return text.toString();
    }
    
    /** Adds the selected text to the clipboard. */
    public void copy() {
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(
                new StringSelection(getSelectedText()), null);
    }
    
    /** Clears the textpane. */
    public void clear() {
        document.clear();
        setScrollBarPosition(0);
        setScrollBarMax();
    }
    
    /** Clears the selection. */
    public void clearSelection() {
        canvas.clearSelection();
    }
    
    /**
     * Adds a TextPaneListener to the listener list.
     *
     * @param listener Listener to add
     */
    public void addTextPaneListener(final TextPaneListener listener) {
        synchronized (textPaneListeners) {
            if (listener == null) {
                return;
            }
            textPaneListeners.add(TextPaneListener.class, listener);
        }
    }
    
    /**
     * Removes a TextPaneListener from the listener list.
     *
     * @param listener Listener to remove
     */
    public void removeTextPaneListener(final TextPaneListener listener) {
        textPaneListeners.remove(TextPaneListener.class, listener);
    }
    
    /**
     * Informs listeners when a word has been clicked on.
     * @param text word clicked on
     */
    protected void fireHyperlinkClicked(final String text) {
        final Object[] listeners = textPaneListeners.getListenerList();
        for (int i = 0; i < listeners.length; i += 2) {
            if (listeners[i] == TextPaneListener.class) {
                ((TextPaneListener) listeners[i + 1]).hyperlinkClicked(text);
            }
        }
    }
    
    /**
     * Informs listeners when a word has been clicked on.
     * @param text word clicked on
     */
    protected void fireChannelClicked(final String text) {
        final Object[] listeners = textPaneListeners.getListenerList();
        for (int i = 0; i < listeners.length; i += 2) {
            if (listeners[i] == TextPaneListener.class) {
                ((TextPaneListener) listeners[i + 1]).channelClicked(text);
            }
        }
    }
    
    /**
     * Checks whether a given string is a valid channel.
     *
     * @param channel Channel name to verify
     *
     * @return true or false
     */
    public boolean isValidChannel(final String channel) {
        return owner.getServer().getParser().isValidChannelName(channel);
    }
}
