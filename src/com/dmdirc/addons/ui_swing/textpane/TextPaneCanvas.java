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

import com.dmdirc.addons.ui_swing.UIUtilities;
import com.dmdirc.interfaces.ConfigChangeListener;
import com.dmdirc.ui.messages.IRCTextAttribute;

import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Toolkit;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.awt.font.LineBreakMeasurer;
import java.awt.font.TextAttribute;
import java.awt.font.TextHitInfo;
import java.awt.font.TextLayout;
import java.text.AttributedCharacterIterator;
import java.text.AttributedString;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.event.MouseInputListener;

/** Canvas object to draw text. */
class TextPaneCanvas extends JPanel implements MouseInputListener,
        ComponentListener, ConfigChangeListener {

    /**
     * A version number for this class. It should be changed whenever the
     * class structure is changed (or anything else that would prevent
     * serialized objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 8;
    /** Hand cursor. */
    private static final Cursor HAND_CURSOR = new Cursor(Cursor.HAND_CURSOR);
    /** IRCDocument. */
    private final IRCDocument document;
    /** parent textpane. */
    private final TextPane textPane;
    /** Position -> TextLayout. */
    private final Map<Rectangle, TextLayout> positions;
    /** TextLayout -> Line numbers. */
    private final Map<TextLayout, LineInfo> textLayouts;
    /** position of the scrollbar. */
    private int scrollBarPosition;
    /** Selection. */
    private LinePosition selection;
    /** First visible line. */
    private int firstVisibleLine;
    /** Last visible line. */
    private int lastVisibleLine;
    /** Line wrapping cache. */
    private final Map<Integer, Integer> lineWrap;

    /**
     * Creates a new text pane canvas.
     *
     * @param parent parent text pane for the canvas
     * @param document IRCDocument to be displayed
     */
    public TextPaneCanvas(final TextPane parent, final IRCDocument document) {
        super();
        this.document = document;
        scrollBarPosition = 0;
        textPane = parent;
        setDoubleBuffered(true);
        setOpaque(true);
        textLayouts = new HashMap<TextLayout, LineInfo>();
        positions = new HashMap<Rectangle, TextLayout>();
        lineWrap = new HashMap<Integer, Integer>();
        selection = new LinePosition(-1, -1, -1, -1);
        addMouseListener(this);
        addMouseMotionListener(this);
        addComponentListener(this);
    //TODO issue 2251
    //parent.getFrameContainer().getConfigManager().addChangeListener("ui", "textPaneFontSize", this);
    }

    /**
     * Paints the text onto the canvas.
     *
     * @param graphics graphics object to draw onto
     */
    @Override
    public void paintComponent(final Graphics graphics) {
        final Graphics2D g = (Graphics2D) graphics;

        final Map desktopHints = (Map) Toolkit.getDefaultToolkit().
                getDesktopProperty("awt.font.desktophints");
        if (desktopHints != null) {
            g.addRenderingHints(desktopHints);
        }

        final float formatWidth = getWidth() - 6;
        final float formatHeight = getHeight();
        float drawPosY = formatHeight;
        int startLine = scrollBarPosition;

        int paragraphStart;
        int paragraphEnd;
        LineBreakMeasurer lineMeasurer;

        g.setColor(textPane.getBackground());
        g.fill(g.getClipBounds());

        textLayouts.clear();
        positions.clear();

        //check theres something to draw and theres some space to draw in
        if (document.getNumLines() == 0 || formatWidth < 1) {
            setCursor(Cursor.getDefaultCursor());
            return;
        }

        // Check the start line is in range
        if (startLine >= document.getNumLines()) {
            startLine = document.getNumLines() - 1;
        }

        if (startLine <= 0) {
            startLine = 0;
        }

        //sets the last visible line
        lastVisibleLine = startLine;
        firstVisibleLine = startLine;

        // Iterate through the lines
        for (int i = startLine; i >= 0; i--) {
            float drawPosX;
            final AttributedCharacterIterator iterator = document.getStyledLine(
                    i);
            int lineHeight = document.getLineHeight(i);
            lineHeight += lineHeight * 0.2;
            paragraphStart = iterator.getBeginIndex();
            paragraphEnd = iterator.getEndIndex();
            lineMeasurer = new LineBreakMeasurer(iterator,
                    g.getFontRenderContext());
            lineMeasurer.setPosition(paragraphStart);

            final int wrappedLine;

            //do we have the line wrapping in the cache?
            if (lineWrap.containsKey(i)) {
                //use it
                wrappedLine = lineWrap.get(i);
            } else {
                //get it and populate the cache
                wrappedLine = getNumWrappedLines(lineMeasurer,
                        paragraphStart, paragraphEnd,
                        formatWidth);
                lineWrap.put(i, wrappedLine);
            }

            if (wrappedLine > 1) {
                drawPosY -= lineHeight * wrappedLine;
            }

            int j = 0;
            int chars = 0;
            // Loop through each wrapped line
            while (lineMeasurer.getPosition() < paragraphEnd) {
                final TextLayout layout = lineMeasurer.nextLayout(formatWidth);

                // Calculate the Y offset
                if (wrappedLine == 1) {
                    drawPosY -= lineHeight;
                } else if (j != 0) {
                    drawPosY += lineHeight;
                }

                // Calculate the initial X position
                if (layout.isLeftToRight()) {
                    drawPosX = 3;
                } else {
                    drawPosX = formatWidth - layout.getAdvance();
                }

                // Check if the target is in range
                if (drawPosY >= 0 || drawPosY <= formatHeight) {

                    g.setColor(textPane.getForeground());

                    layout.draw(g, drawPosX, drawPosY + lineHeight / 2f);
                    doHighlight(i, chars, layout, g, drawPosY, drawPosX);
                    firstVisibleLine = i;
                    textLayouts.put(layout, new LineInfo(i, j));
                    positions.put(new Rectangle(0, (int) drawPosY,
                            (int) formatWidth + 6,
                            lineHeight), layout);
                }

                j++;
                chars += layout.getCharacterCount();
            }
            if (j > 1) {
                drawPosY -= lineHeight * (wrappedLine - 1);
            }
            if (drawPosY <= 0) {
                break;
            }
        }
        checkForLink();
    }

    /**
     * Returns the number of timesa line will wrap.
     *
     * @param lineMeasurer LineBreakMeasurer to work out wrapping for
     * @param paragraphStart Start index of the paragraph
     * @param paragraphEnd End index of the paragraph
     * @param formatWidth Width to wrap at
     *
     * @return Number of times the line wraps
     */
    private int getNumWrappedLines(final LineBreakMeasurer lineMeasurer,
            final int paragraphStart,
            final int paragraphEnd,
            final float formatWidth) {
        int wrappedLine = 0;

        while (lineMeasurer.getPosition() < paragraphEnd) {
            lineMeasurer.nextLayout(formatWidth);

            wrappedLine++;
        }

        lineMeasurer.setPosition(paragraphStart);

        return wrappedLine;
    }

    /**
     * Redraws the text that has been highlighted.
     *
     * @param line Line number
     * @param chars Number of characters so far in the line
     * @param layout Current line textlayout
     * @param g Graphics surface to draw highlight on
     * @param drawPosY current y location of the line
     * @param drawPosX current x location of the line
     */
    private void doHighlight(final int line, final int chars,
            final TextLayout layout, final Graphics2D g,
            final float drawPosY, final float drawPosX) {
        int startLine;
        int startChar;
        int endLine;
        int endChar;

        if (selection.getStartLine() > selection.getEndLine()) {
            // Swap both
            startLine = selection.getEndLine();
            startChar = selection.getEndPos();
            endLine = selection.getStartLine();
            endChar = selection.getStartPos();
        } else if (selection.getStartLine() == selection.getEndLine() &&
                selection.getStartPos() > selection.getEndPos()) {
            // Just swap the chars
            startLine = selection.getStartLine();
            startChar = selection.getEndPos();
            endLine = selection.getEndLine();
            endChar = selection.getStartPos();
        } else {
            // Swap nothing
            startLine = selection.getStartLine();
            startChar = selection.getStartPos();
            endLine = selection.getEndLine();
            endChar = selection.getEndPos();
        }

        //Does this line need highlighting?
        if (startLine <= line && endLine >= line) {
            int firstChar;
            int lastChar;

            // Determine the first char we care about
            if (startLine < line || startChar < chars) {
                firstChar = chars;
            } else {
                firstChar = startChar;
            }

            // ... And the last
            if (endLine > line || endChar > chars + layout.getCharacterCount()) {
                lastChar = chars + layout.getCharacterCount();
            } else {
                lastChar = endChar;
            }

            // If the selection includes the chars we're showing
            if (lastChar > chars && firstChar < chars +
                    layout.getCharacterCount()) {
                String text = document.getLine(line).getText();
                if (firstChar >= 0 && text.length() > lastChar) {
                    text = text.substring(firstChar, lastChar);
                }

                if (text.isEmpty()) {
                    return;
                }

                final AttributedCharacterIterator iterator = document.
                        getStyledLine(line);
                int lineHeight = document.getLineHeight(line);
                lineHeight += lineHeight * 0.2;
                final AttributedString as = new AttributedString(iterator,
                        firstChar,
                        lastChar);
                final int trans = (int) (lineHeight / 2f + drawPosY);

                as.addAttribute(TextAttribute.FOREGROUND,
                        textPane.getBackground());
                as.addAttribute(TextAttribute.BACKGROUND,
                        textPane.getForeground());
                final TextLayout newLayout = new TextLayout(as.getIterator(),
                        g.getFontRenderContext());
                final Shape shape = layout.getLogicalHighlightShape(firstChar -
                        chars,
                        lastChar -
                        chars);

                if (firstChar != 0) {
                    g.translate(shape.getBounds().getX(), 0);
                }

                newLayout.draw(g, drawPosX, trans);

                if (firstChar != 0) {
                    g.translate(-1 * shape.getBounds().getX(), 0);
                }
            }
        }
    }

    /**
     * sets the position of the scroll bar, and repaints if required.
     * @param position scroll bar position
     */
    protected void setScrollBarPosition(final int position) {
        if (scrollBarPosition != position) {
            scrollBarPosition = position;
            if (textPane.isVisible()) {
                repaint();
            }
        }
    }

    /**
     * Returns the current scroll bar position.
     * 
     * @return Scroll bar position
     */
    protected int getScrollBarPosition() {
        return scrollBarPosition;
    }

    /** 
     * {@inheritDoc}
     * 
     * @param e Mouse event
     */
    @Override
    public void mouseClicked(final MouseEvent e) {
        String clickedText = "";
        final int start;
        final int end;

        final LineInfo lineInfo = getClickPosition(getMousePosition());

        if (lineInfo.getLine() != -1) {
            clickedText = document.getLine(lineInfo.getLine()).getText();

            if (lineInfo.getIndex() == -1) {
                start = -1;
                end = -1;
            } else {
                final int[] extent =
                        getSurroundingWordIndexes(clickedText,
                        lineInfo.getIndex());
                start = extent[0];
                end = extent[1];
            }

            if (e.getClickCount() == 2) {
                selection.setStartLine(lineInfo.getLine());
                selection.setEndLine(lineInfo.getLine());
                selection.setStartPos(start);
                selection.setEndPos(end);
            } else if (e.getClickCount() == 3) {
                selection.setStartLine(lineInfo.getLine());
                selection.setEndLine(lineInfo.getLine());
                selection.setStartPos(0);
                selection.setEndPos(clickedText.length());
            }
        }

        e.setSource(textPane);
        textPane.dispatchEvent(e);
    }

    /**
     * Returns the type of text this click represents.
     * 
     * @param lineInfo Line info of click.
     * 
     * @return Click type for specified position
     */
    public ClickType getClickType(final LineInfo lineInfo) {
        if (lineInfo.getLine() != -1) {
            final AttributedCharacterIterator iterator = document.getStyledLine(
                    lineInfo.getLine());
            final int index = lineInfo.getIndex();
            if (index >= iterator.getRunStart() && index <=
                    iterator.getRunLimit()) {
                iterator.setIndex(lineInfo.getIndex());
                Object linkattr =
                        iterator.getAttributes().get(IRCTextAttribute.HYPERLINK);
                if (linkattr instanceof String) {
                    return ClickType.HYPERLINK;
                }
                linkattr =
                        iterator.getAttributes().get(IRCTextAttribute.CHANNEL);
                if (linkattr instanceof String) {
                    return ClickType.CHANNEL;
                }
                linkattr = iterator.getAttributes().get(
                        IRCTextAttribute.NICKNAME);
                if (linkattr instanceof String) {
                    return ClickType.NICKNAME;
                }
            } else {
                return ClickType.NORMAL;
            }
        }
        return ClickType.NORMAL;
    }

    /**
     * Returns the atrriute value for the specified location.
     * 
     * @param lineInfo Specified location
     * 
     * @return Specified value
     */
    public Object getAttributeValueAtPoint(LineInfo lineInfo) {
        if (lineInfo.getLine() != -1) {
            final AttributedCharacterIterator iterator = document.getStyledLine(
                    lineInfo.getLine());
            iterator.setIndex(lineInfo.getIndex());
            Object linkattr =
                    iterator.getAttributes().get(IRCTextAttribute.HYPERLINK);
            if (linkattr instanceof String) {
                return linkattr;
            }
            linkattr = iterator.getAttributes().get(IRCTextAttribute.CHANNEL);
            if (linkattr instanceof String) {
                return linkattr;
            }
            linkattr = iterator.getAttributes().get(IRCTextAttribute.NICKNAME);
            if (linkattr instanceof String) {
                return linkattr;
            }
        }
        return null;
    }

    /**
     * Returns the indexes for the word surrounding the index in the specified
     * string.
     *
     * @param text Text to get word from
     * @param index Index to get surrounding word
     *
     * @return Indexes of the word surrounding the index (start, end)
     */
    protected int[] getSurroundingWordIndexes(final String text,
            final int index) {
        final int start = getSurroundingWordStart(text, index);
        final int end = getSurroundingWordEnd(text, index);

        if (start < 0 || end > text.length() || start > end) {
            return new int[]{0, 0};
        }

        return new int[]{start, end};

    }

    /**
     * Returns the start index for the word surrounding the index in the
     * specified string.
     *
     * @param text Text to get word from
     * @param index Index to get surrounding word
     *
     * @return Start index of the word surrounding the index
     */
    private int getSurroundingWordStart(final String text, final int index) {
        int start = index;

        // Traverse backwards
        while (start > 0 && start < text.length() && text.charAt(start) != ' ') {
            start--;
        }
        if (start + 1 < text.length() && text.charAt(start) == ' ') {
            start++;
        }

        return start;
    }

    /**
     * Returns the end index for the word surrounding the index in the
     * specified string.
     *
     * @param text Text to get word from
     * @param index Index to get surrounding word
     *
     * @return End index of the word surrounding the index
     */
    private int getSurroundingWordEnd(final String text, final int index) {
        int end = index;

        // And forwards
        while (end < text.length() && end > 0 && text.charAt(end) != ' ') {
            end++;
        }

        return end;
    }

    /** 
     * {@inheritDoc}
     * 
     * @param e Mouse event
     */
    @Override
    public void mousePressed(final MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON1) {
            highlightEvent(MouseEventType.CLICK, e);
        }
        e.setSource(textPane);
        textPane.dispatchEvent(e);
    }

    /** 
     * {@inheritDoc}
     * 
     * @param e Mouse event
     */
    @Override
    public void mouseReleased(final MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON1) {
            highlightEvent(MouseEventType.RELEASE, e);
        }
        e.setSource(textPane);
        textPane.dispatchEvent(e);
    }

    /** 
     * {@inheritDoc}
     * 
     * @param e Mouse event
     */
    @Override
    public void mouseDragged(final MouseEvent e) {
        if (e.getModifiersEx() == MouseEvent.BUTTON1_DOWN_MASK) {
            highlightEvent(MouseEventType.DRAG, e);
        }

        e.setSource(textPane);
        textPane.dispatchEvent(e);
    }

    /** 
     * {@inheritDoc}
     * 
     * @param e Mouse event
     */
    @Override
    public void mouseEntered(final MouseEvent e) {
        //Ignore
    }

    /** 
     * {@inheritDoc}
     * 
     * @param e Mouse event
     */
    @Override
    public void mouseExited(final MouseEvent e) {
        //Ignore
    }

    /** 
     * {@inheritDoc}
     * 
     * @param e Mouse event
     */
    @Override
    public void mouseMoved(final MouseEvent e) {
        checkForLink();
    }

    /** Checks for a link under the cursor and sets appropriately. */
    private void checkForLink() {
        final LineInfo lineInfo = getClickPosition(getMousePosition());

        if (lineInfo.getLine() != -1 && document.getLine(lineInfo.getLine()) !=
                null) {
            final AttributedCharacterIterator iterator = document.getStyledLine(
                    lineInfo.getLine());
            if (lineInfo.getIndex() < iterator.getBeginIndex() ||
                    lineInfo.getIndex() > iterator.getEndIndex()) {
                return;
            }
            iterator.setIndex(lineInfo.getIndex());
            Object linkattr =
                    iterator.getAttributes().get(IRCTextAttribute.HYPERLINK);
            if (linkattr instanceof String) {
                setCursor(HAND_CURSOR);
                return;
            }
            linkattr = iterator.getAttributes().get(IRCTextAttribute.CHANNEL);
            if (linkattr instanceof String) {
                setCursor(HAND_CURSOR);
                return;
            }
            linkattr = iterator.getAttributes().get(IRCTextAttribute.NICKNAME);
            if (linkattr instanceof String) {
                setCursor(HAND_CURSOR);
                return;
            }
        }
        if (getCursor() == HAND_CURSOR) {
            setCursor(Cursor.getDefaultCursor());
        }
    }

    /**
     * Sets the selection for the given event.
     *
     * @param type mouse event type
     * @param e responsible mouse event
     */
    protected void highlightEvent(final MouseEventType type,
            final MouseEvent e) {
        if (isVisible()) {
            Point point = e.getLocationOnScreen();
            SwingUtilities.convertPointFromScreen(point, this);
            if (!contains(point)) {
                final Rectangle bounds = getBounds();
                final Point mousePos = e.getPoint();
                if (mousePos.getX() < bounds.getX()) {
                    point.setLocation(bounds.getX() + 3, point.getY());
                } else if (mousePos.getX() > (bounds.getX() + bounds.getWidth())) {
                    point.setLocation(bounds.getX() + bounds.getWidth() - 3,
                            point.getY());
                }
                if (mousePos.getY() < bounds.getY()) {
                    point.setLocation(point.getX(), bounds.getY() + 6);
                } else if (mousePos.getY() >
                        (bounds.getY() + bounds.getHeight())) {
                    //Nice text selection behaviour
                    //point.setLocation(point.getX(), bounds.getY() +
                    //        bounds.getHeight() - 6);
                    point.setLocation(bounds.getX() + bounds.getWidth() - 3,
                            bounds.getY() +
                            bounds.getHeight() - 6);
                }
            }
            final LineInfo info = getClickPosition(point);
            if (info.getLine() == -1 && info.getPart() == -1 && contains(point)) {
                info.setLine(0);
                info.setPart(0);
                //Nice text selection behaviour
                //info.setIndex(getHitPosition(info.getLine(), info.getPart(),
                //        point.x, 0));
                info.setIndex(0);
            }
            if (info.getLine() != -1 && info.getPart() != -1) {
                if (type == MouseEventType.CLICK) {
                    selection.setStartLine(info.getLine());
                    selection.setStartPos(info.getIndex());
                }
                selection.setEndLine(info.getLine());
                selection.setEndPos(info.getIndex());

                repaint();
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
        int lineNumber = -1;
        int linePart = -1;
        int pos = 0;

        if (point != null) {
            for (Map.Entry<Rectangle, TextLayout> entry : positions.entrySet()) {
                if (entry.getKey().contains(point)) {
                    lineNumber = textLayouts.get(entry.getValue()).getLine();
                    linePart = textLayouts.get(entry.getValue()).getPart();
                }
            }

            pos = getHitPosition(lineNumber, linePart, (int) point.getX(),
                    (int) point.getY());
        }

        return new LineInfo(lineNumber, linePart, pos);
    }

    /**
     * Returns the character index for a specified line and part for a specific hit position.
     * 
     * @param lineNumber Line number
     * @param linePart Line part
     * @param x X position
     * @param y Y position
     * 
     * @return Hit position
     */
    private int getHitPosition(final int lineNumber, final int linePart,
            final int x, final int y) {
        int pos = 0;

        for (Map.Entry<Rectangle, TextLayout> entry : positions.entrySet()) {
            if (textLayouts.get(entry.getValue()).getLine() == lineNumber) {
                if (textLayouts.get(entry.getValue()).getPart() < linePart) {
                    pos += entry.getValue().getCharacterCount();
                } else if (textLayouts.get(entry.getValue()).getPart() ==
                        linePart) {
                    final TextHitInfo hit = entry.getValue().hitTestChar(x - 6,
                            y);
                    pos += hit.getInsertionIndex();
                }
            }
        }

        return pos;
    }

    /**
     * Returns the selected range info.
     *
     * @return Selected range info
     */
    protected LinePosition getSelectedRange() {
        if (selection.getStartLine() > selection.getEndLine()) {
            // Swap both
            return new LinePosition(selection.getEndLine(),
                    selection.getEndPos(), selection.getStartLine(),
                    selection.getStartPos());
        } else if (selection.getStartLine() == selection.getEndLine() &&
                selection.getStartPos() > selection.getEndPos()) {
            // Just swap the chars
            return new LinePosition(selection.getStartLine(), selection.
                    getEndPos(), selection.getEndLine(),
                    selection.getStartPos());
        } else {
            // Swap nothing
            return new LinePosition(selection.getStartLine(), selection.
                    getStartPos(), selection.getEndLine(),
                    selection.getEndPos());
        }
    }

    /** Clears the selection. */
    protected void clearSelection() {
        selection.setEndLine(selection.getStartLine());
        selection.setEndPos(selection.getStartPos());
        if (isVisible()) {
            repaint();
        }
    }

    /**
     * Selects the specified region of text.
     *
     * @param position Line position
     */
    public void setSelectedRange(final LinePosition position) {
        selection = new LinePosition(position);
        if (isVisible()) {
            repaint();
        }
    }

    /**nee
     * Returns the first visible line.
     *
     * @return the line number of the first visible line
     */
    public int getFirstVisibleLine() {
        return firstVisibleLine;
    }

    /**
     * Returns the last visible line.
     *
     * @return the line number of the last visible line
     */
    public int getLastVisibleLine() {
        return lastVisibleLine;
    }

    /** 
     * {@inheritDoc}
     * 
     * @param e Component event
     */
    @Override
    public void componentResized(final ComponentEvent e) {
        //line wrap cache now invalid, clear and repaint
        lineWrap.clear();
        if (isVisible()) {
            repaint();
        }
    }

    /** 
     * {@inheritDoc}
     * 
     * @param e Component event
     */
    @Override
    public void componentMoved(final ComponentEvent e) {
        //Ignore
    }

    /** 
     * {@inheritDoc}
     * 
     * @param e Component event
     */
    @Override
    public void componentShown(final ComponentEvent e) {
        //Ignore
    }

    /** 
     * {@inheritDoc}
     * 
     * @param e Component event
     */
    @Override
    public void componentHidden(final ComponentEvent e) {
        //Ignore
    }

    /** Clears the line wrapping cache. */
    protected void clearWrapCache() {
        UIUtilities.invokeLater(new Runnable() {

            /** {@inheritDoc} */
            @Override
            public void run() {
                lineWrap.clear();
            }
        });
    }

    /** {@inheritDoc} */
    @Override
    public void configChanged(final String domain, final String key) {
        UIUtilities.invokeLater(new Runnable() {

            /** {@inheritDoc} */
            @Override
            public void run() {
                if (isVisible()) {
                    repaint();
                }
            }
        });
    }
}
