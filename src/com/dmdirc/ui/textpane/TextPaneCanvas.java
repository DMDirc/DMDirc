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
import java.awt.font.FontRenderContext;
import java.awt.font.LineBreakMeasurer;
import java.awt.font.TextLayout;
import java.text.AttributedCharacterIterator;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JFrame;


/** Canvas object to draw text. */
class TextPaneCanvas extends Canvas implements MouseListener, MouseMotionListener {
    
    /**
     * A version number for this class. It should be changed whenever the
     * class structure is changed (or anything else that would prevent
     * serialized objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 2;
    
    /** Font render context to be used for the text in this pane. */
    private final FontRenderContext defaultFRC = new FontRenderContext(null, false, false);
    
    /** IRCDocument. */
    private IRCDocument document;
    
    /** line break measurer, used for line wrapping. */
    private LineBreakMeasurer lineMeasurer;
    
    /** start character of a paragraph. */
    private int paragraphStart;
    /** end character of a paragraph. */
    private int paragraphEnd;
    /** position of the scrollbar. */
    private int scrollBarPosition;
    /** parent textpane. */
    private TextPane textPane;
    
    /** Position -> TextLayout. */
    private Map<Rectangle, TextLayout> positions;
    /** TextLayout -> Line numbers. */
    private Map<TextLayout, LineInfo> textLayouts;
    
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
        
        textLayouts.clear();
        positions.clear();
        
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
            lineMeasurer = new LineBreakMeasurer(iterator, defaultFRC);
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
        //Ignore
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
        
        if (point != null) {
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
        } else {
            if ((int) getLocationOnScreen().getY() > e.getY()) {
                setScrollBarPosition(scrollBarPosition - 1);
            } else {
                setScrollBarPosition(scrollBarPosition + 1);
            }
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
