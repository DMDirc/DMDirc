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

import com.dmdirc.BrowserLauncher;
import com.dmdirc.ui.MainFrame;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.font.LineBreakMeasurer;
import java.awt.font.TextLayout;
import java.text.AttributedCharacterIterator;
import java.text.AttributedCharacterIterator.Attribute;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.swing.JFrame;


/** Canvas object to draw text. */
class TextPaneCanvas extends Canvas implements MouseListener, MouseMotionListener {
    
    /**
     * A version number for this class. It should be changed whenever the
     * class structure is changed (or anything else that would prevent
     * serialized objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 3;
    
    /** IRCDocument. */
    private final IRCDocument document;
    /** parent textpane. */
    private final TextPane textPane;
    /** Position -> TextLayout. */
    private final Map<Rectangle, TextLayout> positions;
    /** TextLayout -> Line numbers. */
    private final Map<TextLayout, LineInfo> textLayouts;
    /** Line number -> rectangle for lines containing hyperlinks. */
    private final Map<TextLayout, Rectangle> hyperlinks;
    
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
        textLayouts = new HashMap<TextLayout, LineInfo>();
        positions = new HashMap<Rectangle, TextLayout>();
        hyperlinks = new HashMap<TextLayout, Rectangle>();
        this.addMouseListener(this);
        this.addMouseMotionListener(this);
    }
    
    /**
     * Paints the text onto the canvas.
     * @param g graphics object to draw onto
     */
    public void paint(final Graphics g) {
        final Graphics2D graphics2D = (Graphics2D) g;
        
        final float formatWidth = getWidth();
        final float formatHeight = getHeight();
        
        int paragraphStart;
        int paragraphEnd;
        LineBreakMeasurer lineMeasurer;
        boolean isHyperlink = false;
        
        textLayouts.clear();
        positions.clear();
        hyperlinks.clear();
        
        float drawPosY = formatHeight;
        
        int startLine = scrollBarPosition;
        
        // Check the start line is in range
        if (startLine >= document.getNumLines()) {
            startLine = document.getNumLines() - 1;
        }
        
        if (startLine <= 0) {
            startLine = 0;
        }
        
        // We use these for drawing rather than the actual
        // sel{Start,End}{Line,Char} vars defined in the highlightEvent
        // This alllows for highlight in both directions.
        int useStartLine;
        int useStartChar;
        int useEndLine;
        int useEndChar;
        
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
            final AttributedCharacterIterator iterator = document.getLine(i).getIterator();
            paragraphStart = iterator.getBeginIndex();
            paragraphEnd = iterator.getEndIndex();
            lineMeasurer = new LineBreakMeasurer(iterator, ((Graphics2D)g ).getFontRenderContext());
            lineMeasurer.setPosition(paragraphStart);
            
            int wrappedLine = 0;
            int height = 0;
            int firstLineHeight = 0;
            
            // Work out the number of lines this will take
            while (lineMeasurer.getPosition() < paragraphEnd) {
                final TextLayout layout = lineMeasurer.nextLayout(formatWidth);
                if (wrappedLine == 0) {
                    firstLineHeight = (int) (layout.getDescent() + layout.getLeading() + layout.getAscent());
                }
                height += layout.getDescent() + layout.getLeading() + layout.getAscent();
                wrappedLine++;
            }
            
            // Get back to the start
            lineMeasurer.setPosition(paragraphStart);
            paragraphStart = iterator.getBeginIndex();
            paragraphEnd = iterator.getEndIndex();
            
            if (wrappedLine > 1) {
                drawPosY -= height;
            }
            
            //Check if this line contains a hyperlink
            for (Attribute attr : iterator.getAllAttributeKeys()) {
                if (attr instanceof IRCTextAttribute) {
                    isHyperlink = true;
                }
            }
            
            int j = 0;
            int chars = 0;
            // Loop through each wrapped line
            while (lineMeasurer.getPosition() < paragraphEnd) {
                
                final TextLayout layout = lineMeasurer.nextLayout(formatWidth);
                
                // Calculate the Y offset
                if (wrappedLine == 1) {
                    drawPosY -= layout.getDescent() + layout.getLeading() + layout.getAscent();
                } else if (j != 0) {
                    drawPosY += layout.getDescent() + layout.getLeading() + layout.getAscent();
                }
                
                float drawPosX;
                // Calculate the initial X position
                if (layout.isLeftToRight()) {
                    drawPosX = 0;
                } else {
                    drawPosX = formatWidth - layout.getAdvance();
                }
                
                // Check if the target is in range
                if (drawPosY + layout.getAscent() >= 0
                        || (drawPosY + layout.getDescent() + layout.getLeading()) <= formatHeight) {
                    
                    // If the selection includes this line
                    if (useStartLine <= i && useEndLine >= i) {
                        int firstChar;
                        int lastChar;
                        
                        // Determine the first char we care about
                        if (useStartLine < i || useStartChar < chars) {
                            firstChar = chars;
                        } else {
                            firstChar = useStartChar;
                        }
                        
                        // ... And the last
                        if (useEndLine > i || useEndChar > chars + layout.getCharacterCount()) {
                            lastChar = chars + layout.getCharacterCount();
                        } else {
                            lastChar = useEndChar;
                        }
                        
                        // If the selection includes the chars we're showing
                        if (lastChar > chars && firstChar < chars + layout.getCharacterCount()) {
                            final int trans = (int) (layout.getLeading() + layout.getAscent() + drawPosY);
                            final Shape shape = layout.getLogicalHighlightShape(firstChar - chars, lastChar - chars);
                            
                            graphics2D.setColor(Color.LIGHT_GRAY);
                            graphics2D.translate(0, trans);
                            graphics2D.fill(shape);
                            graphics2D.translate(0, -1 * trans);
                        }
                    }
                    
                    graphics2D.setColor(Color.BLACK);
                    
                    layout.draw(graphics2D, drawPosX, drawPosY + layout.getAscent());
                    textLayouts.put(layout, new LineInfo(i, j));
                    positions.put(new Rectangle(
                            (int) drawPosX, (int) drawPosY, (int) formatHeight,
                            (int) (layout.getDescent() + layout.getLeading() + layout.getAscent())
                            ), layout);
                    if (isHyperlink) {
                        hyperlinks.put(layout, new Rectangle((int) drawPosX, (int) drawPosY,
                                (int) formatWidth,
                                (int) (layout.getDescent() + layout.getLeading() + layout.getAscent())));
                    }
                }
                
                j++;
                chars += layout.getCharacterCount();
            }
            if (j > 1) {
                drawPosY -= height - firstLineHeight;
            }
            if (drawPosY <= 0) {
                break;
            }
        }
    }
    
    /**
     * Repaints the canvas offscreen.
     * @param g graphics object to draw onto
     */
    public void update(final Graphics g) {
        final Image offScreen = this.createImage(getWidth(), getHeight());
        final Graphics graphics = offScreen.getGraphics();
        
        graphics.clearRect(0, 0, this.getWidth(), this.getHeight());
        
        paint(graphics);
        
        g.drawImage(offScreen, 0, 0, this);
    }
    
    /**
     * sets the position of the scroll bar, and repaints if required.
     * @param position scroll bar position
     */
    public void setScrollBarPosition(final int position) {
        if (scrollBarPosition != position) {
            scrollBarPosition = position;
            this.repaint();
        }
    }
    
    /** {@inheritDoc}. */
    public void mouseClicked(final MouseEvent e) {
        final StringBuffer clickedText = new StringBuffer();
        int start = -1;
        int end = -1;
        for (Map.Entry<TextLayout, Rectangle> entry : hyperlinks.entrySet()) {
            if (entry.getValue().contains(this.getMousePosition())) {
                start = entry.getKey().hitTestChar((int) this.getMousePosition().getX(), (int) this.getMousePosition().getY()).getInsertionIndex();
                end = start;
                final AttributedCharacterIterator it = document.getLine(textLayouts.get(entry.getKey()).getLine()).getIterator();
                for (it.setIndex(it.getBeginIndex());
                it.getIndex() < it.getEndIndex(); it.next()) {
                    clickedText.append(it.current());
                }
            }
        }
        if (start == -1 || end == -1) {
            return;
        }
        // Traverse backwards
        while (start > 0 && start < clickedText.length() && clickedText.charAt(start) != ' ') {
            start--;
        }
        if (start + 1 < clickedText.length() && clickedText.charAt(start) == ' ') {
            start++;
        }
        
        // And forwards
        while (end < clickedText.length() && end > 0 && clickedText.charAt(end) != ' ') {
            end++;
        }
        
        if (start > end) {
            return;
        }
        if (start < 0 || end > clickedText.length()) {
            return;
        }
        fireTextClicked(clickedText.substring(start, end));
    }
    
    /** {@inheritDoc}. */
    public void mousePressed(final MouseEvent e) {
        highlightEvent(true, e);
    }
    
    /** {@inheritDoc}. */
    public void mouseReleased(final MouseEvent e) {
        highlightEvent(false, e);
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
    public void mouseDragged(final MouseEvent e) {
        highlightEvent(false, e);
    }
    
    /** {@inheritDoc}. */
    public void mouseMoved(final MouseEvent e) {
        //Ignore
    }
    
    /**
     * Sets the selection for the given event.
     *
     * @param start true = start
     * @param e responsible mouse event
     */
    private void highlightEvent(final boolean start, final MouseEvent e) {
        final Point point = this.getMousePosition();
        
        if (point == null) {
            if (getLocationOnScreen().getY() > e.getY()) {
                textPane.setScrollBarPosition(scrollBarPosition - 1);
            } else {
                textPane.setScrollBarPosition(scrollBarPosition + 1);
            }
        } else {
            int lineNumber = -1;
            int linePart = -1;
            int pos = 0;
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
                    } else if (textLayouts.get(entry.getValue()).getPart() == linePart){
                        pos += entry.getValue().hitTestChar((int) point.getX(), (int) point.getY()).getInsertionIndex();
                    }
                }
            }
            if (lineNumber != -1 && linePart != -1) {
                if (start) {
                    selStartLine = lineNumber;
                    selStartChar = pos;
                }
                selEndLine = lineNumber;
                selEndChar = pos;
                
                this.repaint();
            }
        }
    }
    
    /**
     * Informs listeners when a word has been clicked on.
     * @param text word clicked on
     */
    private void fireTextClicked(final String text) {
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
    public int[] getSelectedRange() {
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
    
    /**
     * temporary method to text the textpane.
     * @param args command line arguments
     */
    public static void main(final String[] args) {
        final TextPane tpc = new TextPane();
        final JFrame frame = new JFrame("Test textpane");
        
        tpc.setDoubleBuffered(true);
        tpc.setBackground(Color.WHITE);
        
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        frame.add(tpc);
        
        frame.setSize(new Dimension(400, 400));
        frame.setVisible(true);
        
        tpc.addTestText();
    }
}
