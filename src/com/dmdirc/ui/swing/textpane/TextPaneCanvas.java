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

import com.dmdirc.ui.messages.IRCTextAttribute;

import com.dmdirc.ui.swing.textpane.TextPane.ClickType;
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
import javax.swing.event.MouseInputListener;

/** Canvas object to draw text. */
class TextPaneCanvas extends JPanel implements MouseInputListener,
        ComponentListener {
    
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
    
    /** Line height. */
    private final int lineHeight;
    
    /** Selection event types. */
    private enum MouseEventType {
        /** Mouse clicked. */
        CLICK,
        /** Mouse dragged. */
        DRAG,
        /** Mouse released. */
        RELEASE,
    }
    
    /** position of the scrollbar. */
    private int scrollBarPosition;
    /** Start line of the selection. */
    private int selStartLine;
    /** Start character of the selection. */
    private int selStartChar;
    /** End line of the selection. */
    private int selEndLine;
    /** End character of the selection. */
    private int selEndChar;
    
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
        lineHeight = getFont().getSize() + 2;
        textPane = parent;
        setDoubleBuffered(true);
        setOpaque(true);
        textLayouts = new HashMap<TextLayout, LineInfo>();
        positions = new HashMap<Rectangle, TextLayout>();
        lineWrap = new HashMap<Integer, Integer>();
        addMouseListener(this);
        addMouseMotionListener(this);
        addComponentListener(this);
    }
    
    /**
     * Paints the text onto the canvas.
     *
     * @param graphics graphics object to draw onto
     */
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
        
        int useStartLine;
        int useStartChar;
        int useEndLine;
        int useEndChar;
        int paragraphStart;
        int paragraphEnd;
        LineBreakMeasurer lineMeasurer;
        
        g.setColor(textPane.getBackground());
        g.fill(g.getClipBounds());
        
        textLayouts.clear();
        positions.clear();
        
        //check theres something to draw
        if (document.getNumLines() == 0) {
            setCursor(Cursor.getDefaultCursor());
            return;
        }
        
        //check there is some space to draw in
        if (formatWidth < 1) {
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
        
        if (selStartLine > selEndLine) {
            // Swap both
            useStartLine = selEndLine;
            useStartChar = selEndChar;
            useEndLine = selStartLine;
            useEndChar = selStartChar;
        } else if (selStartLine == selEndLine && selStartChar > selEndChar) {
            // Just swap the chars
            useStartLine = selStartLine;
            useStartChar = selEndChar;
            useEndLine = selEndLine;
            useEndChar = selStartChar;
        } else {
            // Swap nothing
            useStartLine = selStartLine;
            useStartChar = selStartChar;
            useEndLine = selEndLine;
            useEndChar = selEndChar;
        }
        
        // Iterate through the lines
        for (int i = startLine; i >= 0; i--) {
            float drawPosX;
            final AttributedCharacterIterator iterator = document.getLine(i).getIterator();
            paragraphStart = iterator.getBeginIndex();
            paragraphEnd = iterator.getEndIndex();
            lineMeasurer = new LineBreakMeasurer(iterator, g.getFontRenderContext());
            lineMeasurer.setPosition(paragraphStart);
            
            final int wrappedLine;
            
            //do we have the line wrapping in the cache?
            if (lineWrap.containsKey(i)) {
                //use it
                wrappedLine = lineWrap.get(i);
            } else {
                //get it and populate the cache
                wrappedLine = getNumWrappedLines(lineMeasurer,
                        paragraphStart, paragraphEnd, formatWidth);
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
                    doHighlight(i, useStartLine, useEndLine, useStartChar,
                            useEndChar, chars, layout, g, drawPosY, drawPosX);
                    firstVisibleLine = i;
                    textLayouts.put(layout, new LineInfo(i, j));
                    positions.put(new Rectangle(0, (int) drawPosY,
                            (int) formatWidth, lineHeight), layout);
                }
                
                j++;
                chars += layout.getCharacterCount();
            }
            if (j > 1) {
                drawPosY -= lineHeight * (wrappedLine - 1);
            }
            if (drawPosY <= 0) {
                checkForLink();
                break;
            }
        }
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
            final int paragraphStart, final int paragraphEnd,
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
     * @param startLine Selection start line
     * @param endLine Selection end line
     * @param startChar Selection start char
     * @param endChar Selection end char
     * @param chars Number of characters so far in the line
     * @param layout Current line textlayout
     * @param g Graphics surface to draw highlight on
     * @param drawPosY current y location of the line
     * @param drawPosX current x location of the line
     */
    private void doHighlight(final int line, final int startLine,
            final int endLine, final int startChar, final int endChar,
            final int chars, final TextLayout layout, final Graphics2D g,
            final float drawPosY, final float drawPosX) {
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
            if (lastChar > chars && firstChar < chars + layout.getCharacterCount()) {
                final String text = textPane.getTextFromLine(line).substring(firstChar, lastChar);
                
                if (text.isEmpty()) {
                    return;
                }
                
                final int trans = (int) (lineHeight / 2f + drawPosY);
                final AttributedString as = new AttributedString(
                        textPane.getLine(line).getIterator(), firstChar, lastChar);
                as.addAttribute(TextAttribute.FOREGROUND, textPane.getBackground());
                as.addAttribute(TextAttribute.BACKGROUND, textPane.getForeground());
                final TextLayout newLayout = new TextLayout(as.getIterator(),
                        g.getFontRenderContext());
                final Shape shape = layout.getLogicalHighlightShape(firstChar
                        - chars, lastChar - chars);
                
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
    
    /** {@inheritDoc}. */
    public void mouseClicked(final MouseEvent e) {
        String clickedText = "";
        final int start;
        final int end;
        
        final int[] lineInfo = getClickPosition(getMousePosition());
        
        if (lineInfo[0] != -1) {
            clickedText = textPane.getTextFromLine(lineInfo[0]);
            
            if (lineInfo[2] == -1) {
                start = -1;
                end = -1;
            } else {
                final int[] extent = getSurroundingWordIndexes(clickedText, lineInfo[2]);
                start = extent[0];
                end = extent[1];
            }
            
            if (e.getClickCount() == 2) {
                selStartLine = lineInfo[0];
                selEndLine = lineInfo[0];
                selStartChar = start;
                selEndChar = end;
            } else if (e.getClickCount() == 3) {
                selStartLine = lineInfo[0];
                selEndLine = lineInfo[0];
                selStartChar = 0;
                selEndChar = clickedText.length();
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
    public ClickType getClickType(final int[] lineInfo) {
        if (lineInfo[0] != -1) {
            final AttributedCharacterIterator iterator = document.getLine(lineInfo[0]).getIterator();
            iterator.setIndex(lineInfo[2]);
            Object linkattr = iterator.getAttributes().get(IRCTextAttribute.HYPERLINK);
            if (linkattr instanceof String) {
                return ClickType.HYPERLINK;
            }
            linkattr = iterator.getAttributes().get(IRCTextAttribute.CHANNEL);
            if (linkattr instanceof String) {
                return ClickType.CHANNEL;
            }
            linkattr = iterator.getAttributes().get(IRCTextAttribute.NICKNAME);
            if (linkattr instanceof String) {
                return ClickType.NICKNAME;
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
    public Object getAttributeValueAtPoint(int[] lineInfo) {
        if (lineInfo[0] != -1) {
            final AttributedCharacterIterator iterator = document.getLine(lineInfo[0]).getIterator();
            iterator.setIndex(lineInfo[2]);
            Object linkattr = iterator.getAttributes().get(IRCTextAttribute.HYPERLINK);
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
    protected int[] getSurroundingWordIndexes(final String text, final int index) {
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
    
    /** {@inheritDoc}. */
    public void mousePressed(final MouseEvent e) {
        if (e.getButton() == e.BUTTON1) {
            highlightEvent(MouseEventType.CLICK, e);
        }
        e.setSource(textPane);
        textPane.dispatchEvent(e);
    }
    
    /** {@inheritDoc}. */
    public void mouseReleased(final MouseEvent e) {
        if (e.getButton() == e.BUTTON1) {
            highlightEvent(MouseEventType.RELEASE, e);
        }
        e.setSource(textPane);
        textPane.dispatchEvent(e);
    }
    
    /** {@inheritDoc}. */
    public void mouseDragged(final MouseEvent e) {
        if (e.getModifiersEx() == e.BUTTON1_DOWN_MASK) {
            highlightEvent(MouseEventType.DRAG, e);
        }
        
        e.setSource(textPane);
        textPane.dispatchEvent(e);
    }
    
    /** {@inheritDoc}. */
    public void mouseEntered(final MouseEvent e) {
        //Ignore
    }
    
    /** {@inheritDoc}. */
    public void mouseExited(final MouseEvent e) {
        //Ignore
    }
    
    /** {@inheritDoc}. */
    public void mouseMoved(final MouseEvent e) {
        checkForLink();
    }
    
    /** Checks for a link under the cursor and sets appropriately. */
    private void checkForLink() {
        final int[] info = getClickPosition(getMousePosition());
        
        if (info[0] != -1 && document.getLine(info[0]) != null) {
            final AttributedCharacterIterator iterator = document.getLine(info[0]).getIterator();
            if (info[2] < iterator.getBeginIndex() || info[2] > iterator.getEndIndex()) {
                return;
            }
            iterator.setIndex(info[2]);
            Object linkattr = iterator.getAttributes().get(IRCTextAttribute.HYPERLINK);
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
    private void highlightEvent(final MouseEventType type, final MouseEvent e) {
        final Point point = getMousePosition();
        if (isVisible()) {
            if (point == null) {
                if (e.getXOnScreen() > getLocationOnScreen().getX()
                && e.getXOnScreen() < (getLocationOnScreen().getX() + getWidth())) {
                    if (getLocationOnScreen().getY() > e.getYOnScreen()) {
                        textPane.setScrollBarPosition(scrollBarPosition - 1);
                    } else {
                        textPane.setScrollBarPosition(scrollBarPosition + 1);
                    }
                }
            } else {
                final int[] info = getClickPosition(point);
                if (info[0] != -1 && info[1] != -1) {
                    if (type == MouseEventType.CLICK) {
                        selStartLine = info[0];
                        selStartChar = info[2];
                    }
                    selEndLine = info[0];
                    selEndChar = info[2];
                    
                    if (isVisible()) {
                        repaint();
                    }
                }
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
            
            for (Map.Entry<Rectangle, TextLayout> entry : positions.entrySet()) {
                if (textLayouts.get(entry.getValue()).getLine() == lineNumber) {
                    if (textLayouts.get(entry.getValue()).getPart() < linePart) {
                        pos += entry.getValue().getCharacterCount();
                    } else if (textLayouts.get(entry.getValue()).getPart() == linePart) {
                        final TextHitInfo hit = entry.getValue().hitTestChar(
                                (int) point.getX() - 6, (int) point.getY());
                        pos += hit.getInsertionIndex();
                    }
                }
            }
        }
        
        return new int[]{lineNumber, linePart, pos};
    }
    
    /**
     * Returns the selected range info.
     *  <ul>
     *    <li>0 = start line</li>
     *    <li>1 = start char</li>
     *    <li>2 = end line</li>
     *    <li>3 = end char</li>
     *  </ul>
     *
     * @return Selected range info
     */
    protected int[] getSelectedRange() {
        if (selStartLine > selEndLine) {
            // Swap both
            return new int[]{selEndLine, selEndChar, selStartLine, selStartChar, };
        } else if (selStartLine == selEndLine && selStartChar > selEndChar) {
            // Just swap the chars
            return new int[]{selStartLine, selEndChar, selEndLine, selStartChar, };
        } else {
            // Swap nothing
            return new int[]{selStartLine, selStartChar, selEndLine, selEndChar, };
        }
    }
    
    /** Clears the selection. */
    protected void clearSelection() {
        selEndLine = selStartLine;
        selEndChar = selStartChar;
        if (isVisible()) {
            repaint();
        }
    }
    
    /**
     * Selects the specified region of text.
     *
     * @param startLine Start line
     * @param startChar Start char
     * @param endLine End line
     * @param endChar End char
     */
    public void setSelectedRange(final int startLine, final int startChar,
            final int endLine, final int endChar) {
        selStartLine = startLine;
        selStartChar = startChar;
        selEndLine = endLine;
        selEndChar = endChar;
        if (isVisible()) {
            repaint();
        }
    }
    
    /**
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
    
    /** {@inheritDoc}. */
    public void componentResized(final ComponentEvent e) {
        //line wrap cache now invalid, clear and repaint
        lineWrap.clear();
        if (isVisible()) {
            repaint();
        }
    }
    
    /** {@inheritDoc}. */
    public void componentMoved(final ComponentEvent e) {
        //Ignore
    }
    
    /** {@inheritDoc}. */
    public void componentShown(final ComponentEvent e) {
        //Ignore
    }
    
    /** {@inheritDoc}. */
    public void componentHidden(final ComponentEvent e) {
        //Ignore
    }
    
    /** Clears the line wrapping cache. */
    protected void clearWrapCache() {
        lineWrap.clear();
    }
}
