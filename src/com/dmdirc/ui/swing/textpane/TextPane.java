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

import com.dmdirc.FrameContainer;
import com.dmdirc.logger.ErrorLevel;
import com.dmdirc.logger.Logger;
import com.dmdirc.ui.IconManager;
import com.dmdirc.ui.messages.IRCTextAttribute;
import com.dmdirc.ui.messages.Styliser;

import java.awt.Dimension;
import java.awt.Image;
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
import java.awt.font.ImageGraphicAttribute;
import java.awt.font.TextAttribute;
import java.text.AttributedCharacterIterator;
import java.text.AttributedString;
import java.util.Enumeration;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JScrollBar;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Element;
import javax.swing.text.StyleConstants.CharacterConstants;
import javax.swing.text.StyleConstants.ColorConstants;
import javax.swing.text.StyleConstants.FontConstants;
import javax.swing.text.StyledDocument;

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

    /** Click types. */
    public enum ClickType {

        /** Hyperlink. */
        HYPERLINK,
        /** Channel. */
        CHANNEL,
        /** Nickname. */
        NICKNAME,
        /** Normal. */
        NORMAL,
    }

    /** 
     * Creates a new instance of TextPane. 
     *
     * @param frame Parent Frame
     */
    public TextPane(final FrameContainer frame) {
        super();

        this.frame = frame;

        document = new IRCDocument();

        setMinimumSize(new Dimension(0, 0));

        setLayout(new MigLayout("fill"));

        canvas = new TextPaneCanvas(this, document);

        setBorder(UIManager.getBorder("TextField.border"));

        add(canvas, "dock center");


        scrollBar = new JScrollBar(JScrollBar.VERTICAL);
        add(scrollBar, "dock east");

        setAutoscrolls(true);

        scrollBar.setMaximum(document.getNumLines());
        scrollBar.setBlockIncrement(10);
        scrollBar.setUnitIncrement(1);
        scrollBar.addAdjustmentListener(this);

        addMouseWheelListener(this);
        document.addIRCDocumentListener(this);

        MouseMotionListener doScrollRectToVisible = new MouseMotionAdapter() {

            /** {@inheritDoc} */
            @Override
            public void mouseDragged(MouseEvent e) {
                if (e.getXOnScreen() > getLocationOnScreen().getX() && e.getXOnScreen() < (getLocationOnScreen().
                        getX() + getWidth()) && e.getModifiersEx() ==
                        MouseEvent.BUTTON1_DOWN_MASK) {
                    if (getLocationOnScreen().getY() > e.getYOnScreen()) {
                        setScrollBarPosition(scrollBar.getValue() - 1);
                    } else if (getLocationOnScreen().getY() + getHeight() <
                            e.getYOnScreen()) {
                        setScrollBarPosition(scrollBar.getValue() + 1);
                    }
                    canvas.highlightEvent(TextPaneCanvas.MouseEventType.DRAG, e);
                }
            }
        };
        addMouseMotionListener(doScrollRectToVisible);
    }

    /**
     * Adds styled text to the textpane.
     * @param text styled text to add
     */
    public void addText(final AttributedString text) {
        document.addText(text);
    }

    /**
     * Adds styled text to the textpane.
     * @param text styled text to add
     */
    public void addText(final List<AttributedString> text) {
        document.addText(text);
    }

    /**
     * Stylises the specified string and adds it to the passed TextPane.
     *
     * @param string The line to be stylised and added
     */
    public void addStyledString(final String string) {
        addStyledString(new String[]{string,});
    }

    /**
     * Stylises the specified string and adds it to the passed TextPane.
     *
     * @param strings The strings to be stylised and added to a line
     */
    public void addStyledString(final String[] strings) {
        addText(styledDocumentToAttributedString(
                Styliser.getStyledString(strings)));
    }

    /**
     * Converts a StyledDocument into an AttributedString.
     *
     * @param doc StyledDocument to convert
     *
     * @return AttributedString representing the specified StyledDocument
     */
    public static AttributedString styledDocumentToAttributedString(
            final StyledDocument doc) {
        //Now lets get hacky, loop through the styled document and add all 
        //styles to an attributedString
        AttributedString attString = null;
        final Element line = doc.getParagraphElement(0);
        try {
            attString = new AttributedString(line.getDocument().getText(0,
                    line.getDocument().getLength()));
        } catch (BadLocationException ex) {
            Logger.userError(ErrorLevel.MEDIUM,
                    "Unable to insert styled string: " + ex.getMessage());
        }

        if (attString.getIterator().getEndIndex() != 0) {
            attString.addAttribute(TextAttribute.SIZE,
                    UIManager.getFont("TextPane.font").getSize());
            attString.addAttribute(TextAttribute.FAMILY,
                    UIManager.getFont("TextPane.font").getFamily());
        }

        for (int i = 0; i < line.getElementCount(); i++) {
            final Element element = line.getElement(i);

            final AttributeSet as = element.getAttributes();
            final Enumeration<?> ae = as.getAttributeNames();

            while (ae.hasMoreElements()) {
                final Object attrib = ae.nextElement();

                if (attrib == IRCTextAttribute.HYPERLINK) {
                    //Hyperlink
                    attString.addAttribute(IRCTextAttribute.HYPERLINK,
                            as.getAttribute(attrib), element.getStartOffset(),
                            element.getEndOffset());
                } else if (attrib == IRCTextAttribute.NICKNAME) {
                    //Nicknames
                    attString.addAttribute(IRCTextAttribute.NICKNAME,
                            as.getAttribute(attrib), element.getStartOffset(),
                            element.getEndOffset());
                } else if (attrib == IRCTextAttribute.CHANNEL) {
                    //Channels
                    attString.addAttribute(IRCTextAttribute.CHANNEL,
                            as.getAttribute(attrib), element.getStartOffset(),
                            element.getEndOffset());
                } else if (attrib == ColorConstants.Foreground) {
                    //Foreground
                    attString.addAttribute(TextAttribute.FOREGROUND,
                            as.getAttribute(attrib), element.getStartOffset(),
                            element.getEndOffset());
                } else if (attrib == ColorConstants.Background) {
                    //Background
                    attString.addAttribute(TextAttribute.BACKGROUND,
                            as.getAttribute(attrib), element.getStartOffset(),
                            element.getEndOffset());
                } else if (attrib == FontConstants.Bold) {
                    //Bold
                    attString.addAttribute(TextAttribute.WEIGHT,
                            TextAttribute.WEIGHT_BOLD, element.getStartOffset(),
                            element.getEndOffset());
                } else if (attrib == FontConstants.Family) {
                    //Family
                    attString.addAttribute(TextAttribute.FAMILY,
                            as.getAttribute(attrib), element.getStartOffset(),
                            element.getEndOffset());
                } else if (attrib == FontConstants.Italic) {
                    //italics
                    attString.addAttribute(TextAttribute.POSTURE,
                            TextAttribute.POSTURE_OBLIQUE,
                            element.getStartOffset(),
                            element.getEndOffset());
                } else if (attrib == CharacterConstants.Underline) {
                    //Underline
                    attString.addAttribute(TextAttribute.UNDERLINE,
                            TextAttribute.UNDERLINE_ON, element.getStartOffset(),
                            element.getEndOffset());
                } else if (attrib == IRCTextAttribute.SMILEY) {
                    final Image image = IconManager.getIconManager()
                            .getImage((String) as.getAttribute(attrib))
                            .getScaledInstance(14, 14, Image.SCALE_DEFAULT);
                    ImageGraphicAttribute iga = new ImageGraphicAttribute(image, 
                            (int) BOTTOM_ALIGNMENT, 5, 5);
                    attString.addAttribute(TextAttribute.CHAR_REPLACEMENT, iga,
                            element.getStartOffset(), element.getEndOffset());
                }
            }
        }

        if (attString.getIterator().getEndIndex() == 0) {
            return new AttributedString("\n");
        }

        return attString;
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

            @Override
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

        if (lines == 0) {
            canvas.repaint();
        }

        scrollBar.setMaximum(lines);

        if (!scrollBar.getValueIsAdjusting() && scrollBar.getValue() == lines -
                linesAllowed) {
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
     *     *    <li>0 = start line</li>
     *    <li>1 = start char</li>
     *    <li>2 = end line</li>
     *    <li>3 = end char</li>
     *
     * @return Selected text
     */
    public String getSelectedText() {
        final StringBuffer selectedText = new StringBuffer();
        final LinePosition selectedRange = canvas.getSelectedRange();

        for (int i = selectedRange.getStartLine(); i <=
                selectedRange.getEndLine(); i++) {
            if (i != selectedRange.getStartLine()) {
                selectedText.append('\n');
            }
            if (document.getNumLines() <= i) {
                return selectedText.toString();
            }
            final AttributedCharacterIterator iterator = document.getLine(i).
                    getIterator();
            if (selectedRange.getEndLine() == selectedRange.getStartLine()) {
                //loop through range
                selectedText.append(getTextFromLine(iterator,
                        selectedRange.getStartPos(), selectedRange.getEndPos()));
            } else if (i == selectedRange.getStartLine()) {
                //loop from start of range to the end
                selectedText.append(getTextFromLine(iterator,
                        selectedRange.getStartPos(), iterator.getEndIndex()));
            } else if (i == selectedRange.getEndLine()) {
                //loop from start to end of range
                selectedText.append(getTextFromLine(iterator, 0,
                        selectedRange.getEndPos()));
            } else {
                //loop the whole line
                selectedText.append(getTextFromLine(iterator, 0,
                        iterator.getEndIndex()));
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
     * Returns the entire text from the specified line.
     *
     * @param line line to retrieve text from
     *
     * @return Text from the line
     */
    public String getTextFromLine(final int line) {
        final AttributedCharacterIterator iterator = document.getLine(line).
                getIterator();
        return getTextFromLine(iterator, 0, iterator.getEndIndex(), document);
    }

    /**
     * Returns the entire text from the specified line.
     *
     * @param line line to retrieve text from
     * @param document Document to retrieve text from
     * 
     * @return Text from the line
     */
    public static String getTextFromLine(final int line,
            final IRCDocument document) {
        final AttributedCharacterIterator iterator = document.getLine(line).
                getIterator();
        return getTextFromLine(iterator, 0, iterator.getEndIndex(), document);
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
        return getTextFromLine(document.getLine(line).getIterator(), start, end,
                document);
    }

    /**
     * Returns the range of text from the specified iterator.
     *
     * @param line line to retrieve text from
     * @param start Start index in the iterator
     * @param end End index in the iterator
     * @param document Document to retrieve text from
     *
     * @return Text in the range from the line
     */
    public static String getTextFromLine(final int line, final int start,
            final int end, final IRCDocument document) {
        return getTextFromLine(document.getLine(line).getIterator(), start, end,
                document);
    }

    /**
     * Returns the range of text from the specified iterator.
     *
     * @param iterator iterator to get text from
     *
     * @return Text in the range from the line
     */
    public String getTextFromLine(final AttributedCharacterIterator iterator) {
        return getTextFromLine(iterator, iterator.getBeginIndex(),
                iterator.getEndIndex(), document);
    }

    /**
     * Returns the range of text from the specified iterator.
     *
     * @param iterator iterator to get text from
     * @param document Document to retrieve text from
     *
     * @return Text in the range from the line
     */
    public static String getTextFromLine(final AttributedCharacterIterator iterator,
            final IRCDocument document) {
        return getTextFromLine(iterator, iterator.getBeginIndex(),
                iterator.getEndIndex(), document);
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
        return getTextFromLine(iterator, start, end, document);
    }

    /**
     * Returns the range of text from the specified iterator.
     *
     * @param iterator iterator to get text from
     * @param start Start index in the iterator
     * @param end End index in the iterator
     * @param document Document to retrieve text from
     *
     * @return Text in the range from the line
     */
    public static String getTextFromLine(final AttributedCharacterIterator iterator,
            final int start, final int end, final IRCDocument document) {
        return document.getLineText(iterator, start, end);
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
                canvas.getSurroundingWordIndexes(getTextFromLine(lineNumber),
                index);
        return getTextFromLine(lineNumber, indexes[0], indexes[1]);
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

    /**
     * Retrieves this textpane's IRCDocument.
     * 
     * @return This textpane's IRC document
     */
    public IRCDocument getDocument() {
        return document;
    }
}
