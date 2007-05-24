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

import com.dmdirc.ui.messages.Styliser;
import java.awt.BorderLayout;
import java.awt.Color;
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
import javax.swing.JFrame;
import javax.swing.JScrollBar;
import javax.swing.event.EventListenerList;

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
    
    /** Scrollbar for the component. */
    final private JScrollBar scrollBar;
    /** Canvas object, used to draw text. */
    final private TextPaneCanvas canvas;
    /** IRCDocument. */
    final private IRCDocument document;
    
    /** Listener list. */
    final private EventListenerList textPaneListeners;
    
    /**
     * Creates a new instance of TextPane.
     */
    public TextPane() {
        super();
        
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
        scrollBar.setMaximum(document.getNumLines());
        if (!scrollBar.getValueIsAdjusting()
        && (scrollBar.getValue() == scrollBar.getMaximum() - 1)) {
            setScrollBarPosition(document.getNumLines());
        }
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
        
        for(int i = selectedRange[0]; i <= selectedRange[2]; i++) {
            if (i != selectedRange[0]) {
                selectedText.append('\n');
            }
            final AttributedString as = document.getLine(i);
            final AttributedCharacterIterator iterator = as.getIterator();
            if (selectedRange[2] == selectedRange[0]) {
                //loop through range
                for (iterator.setIndex(selectedRange[1]);
                iterator.getIndex() < selectedRange[3]; iterator.next()) {
                    selectedText.append(iterator.current());
                }
            } else if (i == selectedRange[0]) {
                //loop from start of range to the end
                for (iterator.setIndex(selectedRange[1]);
                iterator.getIndex() < iterator.getEndIndex(); iterator.next()) {
                    selectedText.append(iterator.current());
                }
            } else if (i == selectedRange[2]) {
                //loop from start to end of range
                for (iterator.setIndex(iterator.getBeginIndex());
                iterator.getIndex() < selectedRange[3]; iterator.next()) {
                    selectedText.append(iterator.current());
                }
            } else {
                //loop the whole line
                for (iterator.setIndex(iterator.getBeginIndex());
                iterator.getIndex() < iterator.getEndIndex(); iterator.next()) {
                    selectedText.append(iterator.current());
                }
            }
        }
        
        return selectedText.toString();
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
        synchronized(textPaneListeners) {
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
    protected void fireTextClicked(final String text) {
        final Object[] listeners = textPaneListeners.getListenerList();
        for (int i = 0; i < listeners.length; i += 2) {
            if (listeners[i] == TextPaneListener.class) {
                ((TextPaneListener) listeners[i + 1]).hyperlinkClicked(text);
            }
        }
    }
}
