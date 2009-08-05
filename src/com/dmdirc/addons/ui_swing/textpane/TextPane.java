/*
 * Copyright (c) 2006-2009 Chris Smith, Shane Mc Cormack, Gregory Holmes
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

package com.dmdirc.addons.ui_swing.textpane;

import com.dmdirc.FrameContainer;

import java.awt.Point;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

import javax.swing.JComponent;
import javax.swing.JScrollBar;

import net.miginfocom.swing.MigLayout;

/**
 * Styled, scrollable text pane.
 */
public final class TextPane extends JComponent implements AdjustmentListener,
        MouseWheelListener, IRCDocumentListener {

    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 5;
    /** Scrollbar for the component. */
    private final JScrollBar scrollBar;
    /** Canvas object, used to draw text. */
    private final TextPaneCanvas canvas;
    /** IRCDocument. */
    private final IRCDocument document;
    /** Parent Frame. */
    private final FrameContainer frame;

    /** 
     * Creates a new instance of TextPane. 
     *
     * @param frame Parent Frame
     */
    public TextPane(final FrameContainer frame) {
        super();
        setUI(new TextPaneUI());

        this.frame = frame;
        document = new IRCDocument(frame.getConfigManager());
        frame.getConfigManager().addChangeListener("ui", "textPaneFontName", document);
        frame.getConfigManager().addChangeListener("ui", "textPaneFontSize", document);

        setLayout(new MigLayout("fill"));
        canvas = new TextPaneCanvas(this, document);
        add(canvas, "dock center");
        scrollBar = new JScrollBar(JScrollBar.VERTICAL);
        add(scrollBar, "dock east");
        scrollBar.setMaximum(document.getNumLines());
        scrollBar.setBlockIncrement(10);
        scrollBar.setUnitIncrement(1);
        scrollBar.addAdjustmentListener(this);

        addMouseWheelListener(this);
        document.addIRCDocumentListener(this);
        setAutoscrolls(true);

        MouseMotionListener doScrollRectToVisible = new MouseMotionAdapter() {

            /** {@inheritDoc} */
            @Override
            public void mouseDragged(MouseEvent e) {
                if (e.getXOnScreen() > getLocationOnScreen().getX() && e.
                        getXOnScreen() < (getLocationOnScreen().
                        getX() + getWidth()) && e.getModifiersEx() ==
                        MouseEvent.BUTTON1_DOWN_MASK) {
                    if (getLocationOnScreen().getY() > e.getYOnScreen()) {
                        setScrollBarPosition(scrollBar.getValue() - 1);
                    } else if (getLocationOnScreen().getY() + getHeight() <
                            e.getYOnScreen()) {
                        setScrollBarPosition(scrollBar.getValue() + 1);
                    }
                    canvas.highlightEvent(MouseEventType.DRAG, e);
                }
            }
        };
        addMouseMotionListener(doScrollRectToVisible);
    }

    /** {@inheritDoc} */
    @Override
    public void updateUI() {
        setUI(new TextPaneUI());
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
     * Returns the last visible line in the textpane.
     *
     * @return Last visible line index
     */
    public int getLastVisibleLine() {
        return scrollBar.getValue();
    }

    /**
     * Sets the scrollbar's maximum position. If the current position is
     * within <code>linesAllowed</code> of the end of the document, the
     * scrollbar's current position is set to the end of the document.
     * 
     * @param linesAllowed The number of lines allowed below the current position
     * @since 0.6
     */
    protected void setScrollBarMax(final int linesAllowed) {
        final int lines = document.getNumLines() - 1;
        final int currentLine = scrollBar.getValue();
        final int allowedDeviation = lines - linesAllowed;

        if (lines == 0) {
            canvas.repaint();
        }

        scrollBar.setMaximum(lines);

        boolean setToMax = currentLine == allowedDeviation;
        if (allowedDeviation == -1) {
            setToMax = true;
        }

        if (!scrollBar.getValueIsAdjusting() && setToMax) {
            setScrollBarPosition(lines);
        }
    }

    /** 
     * {@inheritDoc}
     * 
     * @param e Mouse wheel event
     */
    @Override
    public void adjustmentValueChanged(final AdjustmentEvent e) {
        setScrollBarPosition(e.getValue());
    }

    /** 
     * {@inheritDoc}
     * 
     * @param e Mouse wheel event
     */
    @Override
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
    public LineInfo getClickPosition(final Point point) {
        return canvas.getClickPosition(point);
    }

    /**
     * Returns the selected text.
     * 
     *    <li>0 = start line</li>
     *    <li>1 = start char</li>
     *    <li>2 = end line</li>
     *    <li>3 = end char</li>
     *
     * @return Selected text
     */
    public String getSelectedText() {
        final StringBuffer selectedText = new StringBuffer();
        final LinePosition selectedRange = canvas.getSelectedRange();

        if (selectedRange.getStartLine() == -1) {
            return null;
        }

        for (int i = selectedRange.getStartLine(); i <=
                selectedRange.getEndLine();
                i++) {
            if (i != selectedRange.getStartLine()) {
                selectedText.append('\n');
            }
            if (document.getNumLines() <= i) {
                return selectedText.toString();
            }
            final String line = document.getLine(i).getText();
            if (!line.isEmpty()) {
                if (selectedRange.getEndLine() == selectedRange.getStartLine()) {
                    //loop through range
                    if (selectedRange.getStartPos() != -1
                            && selectedRange.getEndPos() != -1) {
                        selectedText.append(line.substring(
                                selectedRange.getStartPos(),
                                selectedRange.getEndPos()));
                    }
                } else if (i == selectedRange.getStartLine()) {
                    //loop from start of range to the end
                    if (selectedRange.getStartPos() != -1) {
                        selectedText.append(line.substring(
                                selectedRange.getStartPos(), line.length()));
                    }
                } else if (i == selectedRange.getEndLine()) {
                    //loop from start to end of range
                    if (selectedRange.getEndPos() != -1) {
                        selectedText.append(line.substring(0, selectedRange.
                                getEndPos()));
                    }
                } else {
                    //loop the whole line
                    selectedText.append(line);
                }
            }
        }

        return selectedText.toString();
    }

    /**
     * Returns the selected range.
     *
     * @return selected range
     */
    public LinePosition getSelectedRange() {
        return canvas.getSelectedRange();
    }

    /**
     * Returns whether there is a selected range.
     * 
     * @return true iif there is a selected range
     */
    public boolean hasSelectedRange() {
        final LinePosition selectedRange = canvas.getSelectedRange();
        return !(selectedRange.getStartLine() == selectedRange.getEndLine() &&
                selectedRange.getStartPos() == selectedRange.getEndPos());
    }

    /**
     * Selects the specified region of text.
     *
     * @param position Line position
     */
    public void setSelectedTexT(final LinePosition position) {
        canvas.setSelectedRange(position);
    }

    /**
     * Returns the type of text this click represents.
     * 
     * @param lineInfo Line info of click.
     * 
     * @return Click type for specified position
     */
    public ClickType getClickType(final LineInfo lineInfo) {
        return canvas.getClickType(lineInfo);
    }

    /**
     * Returns the surrouding word at the specified position.
     * 
     * @param lineNumber Line number to get word from
     * @param index Position to get surrounding word
     * 
     * @return Surrounding word
     */
    public String getWordAtIndex(final int lineNumber, final int index) {
        if (lineNumber == -1) {
            return "";
        }
        final int[] indexes =
                canvas.getSurroundingWordIndexes(document.getLine(lineNumber).
                getText(),
                index);
        return document.getLine(lineNumber).getText().substring(indexes[0],
                indexes[1]);
    }

    /**
     * Returns the atrriute value for the specified location.
     * 
     * @param lineInfo Specified location
     * 
     * @return Specified value
     */
    public Object getAttributeValueAtPoint(LineInfo lineInfo) {
        return canvas.getAttributeValueAtPoint(lineInfo);
    }

    /** Adds the selected text to the clipboard. */
    public void copy() {
        if (getSelectedText() != null && !getSelectedText().isEmpty()) {
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(
                    new StringSelection(getSelectedText()), null);
        }
    }

    /** Clears the textpane. */
    public void clear() {
        document.clear();
        setScrollBarPosition(0);
        setScrollBarMax(1);
        canvas.repaint();
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
        if (document.getNumLines() < numLines) {
            return;
        }
        final int trimmedLines = document.getNumLines() - numLines;
        final LinePosition selectedRange = getSelectedRange();

        selectedRange.setStartLine(selectedRange.getStartLine() - trimmedLines);
        selectedRange.setEndLine(selectedRange.getEndLine() - trimmedLines);

        if (selectedRange.getStartLine() < 0) {
            selectedRange.setStartLine(0);
        }
        if (selectedRange.getEndLine() < 0) {
            selectedRange.setEndLine(0);
        }

        setSelectedTexT(selectedRange);
        document.trim(numLines);
    }

    /** Scrolls one page up in the textpane. */
    public void pageDown() {
        //setScrollBarPosition(scrollBar.getValue() + canvas.getLastVisibleLine() 
        // - canvas.getFirstVisibleLine() + 1);
        //use this method for now, its consistent with the block unit for the scrollbar
        setScrollBarPosition(scrollBar.getValue() + 10);
    }

    /** Scrolls one page down in the textpane. */
    public void pageUp() {
        //setScrollBarPosition(canvas.getFirstVisibleLine());
        //use this method for now, its consistent with the block unit for the scrollbar
        setScrollBarPosition(scrollBar.getValue() - 10);
    }

    /** {@inheritDoc}. */
    @Override
    public void lineAdded(final int line, final int size) {
        setScrollBarMax(1);
    }

    /** {@inheritDoc}. */
    @Override
    public void trimmed(final int numLines) {
        canvas.clearWrapCache();
        setScrollBarMax(1);
    }

    /** {@inheritDoc}. */
    @Override
    public void cleared() {
        canvas.clearWrapCache();
    }

    /** {@inheritDoc}. */
    @Override
    public void linesAdded(int line, int length, int size) {
        setScrollBarMax(length);
    }
    
    /** {@inheritDoc}. */
    @Override
    public void repaintNeeded() {
        canvas.repaint();
    }

    /** {@inheritDoc}. */
    @Override
    public void clearWrapCache() {
        canvas.clearWrapCache();
    }

    /**
     * Retrieves this textpane's IRCDocument.
     * 
     * @return This textpane's IRC document
     */
    public IRCDocument getDocument() {
        return document;
    }
    
    /**
     * Retrives the parent framecontainer for this textpane.
     * 
     * @return Parent frame container
     */
    public FrameContainer getFrameContainer() {
        return frame;
    }
}
