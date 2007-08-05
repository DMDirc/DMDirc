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

package com.dmdirc.ui.swing.textpane;

import com.dmdirc.ui.messages.Styliser;
import com.dmdirc.ui.swing.components.Frame;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.text.AttributedCharacterIterator;
import java.text.AttributedString;

import javax.swing.JComponent;
import javax.swing.JScrollBar;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
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
        
        this.setBorder(UIManager.getBorder("TextField.border"));
        
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
     * Adds styled text to the textpane.
     * @param text styled text to add
     */
    public void addText(final AttributedString text) {
        document.addText(text);
        setScrollBarMax();
    }
    
    /**
     * Stylises the specified string and adds it to the passed TextPane.
     *
     * @param string The line to be stylised and added
     */
    public void addStyledString(final String string) {
        final AttributedString text = Styliser.styledDocumentToAttributedString(
                Styliser.getStyledString(new String[]{string, }));
        
        if (text.getIterator().getEndIndex() == 0) {
            addText(new AttributedString("\n"));
        } else {
            addText(text);
        }
    }
    
    /**
     * Stylises the specified string and adds it to the passed TextPane.
     *
     * @param strings The strings to be stylised and added to a line
     */
    public void addStyledString(final String[] strings) {
        final AttributedString text = Styliser.styledDocumentToAttributedString(
                Styliser.getStyledString(strings));
        
        if (text.getIterator().getEndIndex() == 0) {
            addText(new AttributedString("\n"));
        } else {
            addText(text);
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
    
    /**
     * Enables or disabled the scrollbar for the textpane.
     *
     * @param enabled State for the scrollbar
     */
    public void setScrollEnabled(final boolean enabled) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                scrollBar.setEnabled(enabled);
            }
        });
    }
    
    /**
     * Returns the last visible line in the textpane.
     *
     * @return Last visible line index
     */
    public int getLastVisibleLine() {
        return scrollBar.getValue();
    }
    
    /**
     * Returns the line count in the textpane.
     *
     * @return Line count
     */
    public int getNumLines() {
        return document.getNumLines();
    }
    
    /**
     * Returns the specified line in the textpane.
     *
     * @param line Line to return
     *
     * @return AttributedString at the specified line
     */
    public AttributedString getLine(final int line) {
        return document.getLine(line);
    }
    
    /** Sets the scrollbar to the maximum position. */
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
        setScrollBarPosition(e.getValue());
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
     *
     * Returns the line information from a mouse click inside the textpane.
     *
     * @param point mouse position
     *
     * @return line number, line part, position in whole line
     */
    public int[] getClickPosition(final Point point) {
        return canvas.getClickPosition(point);
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
            if (document.getLine(i) == null) {
                return "";
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
     * Returns the selected range.
     *
     * @return selected range
     *  <ul>
     *    <li>0 = start line</li>
     *    <li>1 = start char</li>
     *    <li>2 = end line</li>
     *    <li>3 = end char</li>
     *  </ul>
     */
    public int[] getSelectedRange() {
        return canvas.getSelectedRange();
    }
    
    /**
     * Selects the specified region of text.
     *
     * @param startLine Start line
     * @param startChar Start char
     * @param endLine End line
     * @param endChar End char
     */
    public void setSelectedTexT(final int startLine, final int startChar,
            final int endLine, final int endChar) {
        canvas.setSelectedRange(startLine, startChar, endLine, endChar);
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
     *
     * @return Text in the range from the line
     */
    public String getTextFromLine(final AttributedCharacterIterator iterator) {
        final int end = iterator.getEndIndex();
        final int start = iterator.getBeginIndex();
        final StringBuffer text = new StringBuffer();
        for (iterator.setIndex(start); iterator.getIndex() < end; iterator.next()) {
            text.append(iterator.current());
        }
        return text.toString();
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
        for (iterator.setIndex(start); iterator.getIndex() < end; iterator.next()) {
            text.append(iterator.current());
        }
        return text.toString();
    }
    
    /** Adds the selected text to the clipboard. */
    public void copy() {
        if (getSelectedText() != null && !"".equals(getSelectedText())) {
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(
                    new StringSelection(getSelectedText()), null);
        }
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
     * Trims the document to the specified number of lines.
     *
     * @param numLines Number of lines to trim the document to
     */
    public void trim(final int numLines) {
        document.trim(numLines);
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
        if (owner != null && channel.length() > 0) {
            return owner.getContainer().getServer().getParser().isValidChannelName(channel);
        }
        return false;
    }
    
    /** Scrolls one page up in the textpane. */
    public void pageDown() {
        //setScrollBarPosition(scrollBar.getValue() + canvas.getLastVisibleLine() - canvas.getFirstVisibleLine() + 1);
        //use this method for now, its consistent with the block unit for the scrollbar
        setScrollBarPosition(scrollBar.getValue() + 10);
    }
    
    /** Scrolls one page down in the textpane. */
    public void pageUp() {
        //setScrollBarPosition(canvas.getFirstVisibleLine());
        //use this method for now, its consistent with the block unit for the scrollbar
        setScrollBarPosition(scrollBar.getValue() - 10);
    }
}
