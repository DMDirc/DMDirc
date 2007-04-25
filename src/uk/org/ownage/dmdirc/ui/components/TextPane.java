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

package uk.org.ownage.dmdirc.ui.components;

import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.font.FontRenderContext;
import java.awt.font.LineBreakMeasurer;
import java.awt.font.TextAttribute;
import java.awt.font.TextLayout;
import java.text.AttributedCharacterIterator;
import java.text.AttributedString;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JScrollBar;

/**
 * Styled, scrollable text pane.
 */
public class TextPane extends JComponent implements AdjustmentListener,
        MouseWheelListener {
    
    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 1;
    
    /** Scrollbar for the component. */
    private JScrollBar scrollBar;
    /** Canvas object, used to draw text */
    private TextPaneCanvas textPane;
    
    /**
     * Creates a new instance of TextPane.
     */
    public TextPane() {
        this.setLayout(new BorderLayout());
        
        textPane = new TextPaneCanvas(this);
        this.add(textPane, BorderLayout.CENTER);
        
        
        scrollBar = new JScrollBar(JScrollBar.VERTICAL);
        this.add(scrollBar, BorderLayout.LINE_END);
        
        scrollBar.setMaximum(textPane.getNumLines());
        scrollBar.setBlockIncrement(10);
        scrollBar.setUnitIncrement(1);
        scrollBar.addAdjustmentListener(this);
        
        this.addMouseWheelListener(this);
    }
    
    /**
     * Adds text to the textpane.
     * @param text text to add
     */
    public void addText(String text) {
        AttributedString attributedString = new AttributedString(text);
        addText(attributedString);
    }
    
    /**
     * Adds styled text to the textpane.
     * @param text styled text to add
     */
    public void addText(AttributedString text) {
        textPane.addText(text);
        scrollBar.setMaximum(textPane.getNumLines());
        if (!scrollBar.getValueIsAdjusting() 
        && (scrollBar.getValue() == scrollBar.getMaximum()-1)) {
            setScrollBarPosition(textPane.getNumLines());
        }
    }
    
    /**
     * Sets the new position for the scrollbar and the associated position
     * to render the text from.
     * @param position new position of the scrollbar
     */
    private void setScrollBarPosition(int position) {
        scrollBar.setValue(position);
        textPane.setScrollBarPosition(position);
    }
    
    /** {@inheritDoc}. */
    public void adjustmentValueChanged(AdjustmentEvent e) {
        if (e.getValue() < textPane.getNumLines()) {
            scrollBar.setValue(e.getValue());
            textPane.setScrollBarPosition(e.getValue());
        }
    }
    
    /** {@inheritDoc}. */
    public void mouseWheelMoved(MouseWheelEvent e) {
        if (scrollBar.isEnabled()) {
            if (e.getWheelRotation() > 0) {
                setScrollBarPosition(scrollBar.getValue() + e.getScrollAmount());
            } else {
                setScrollBarPosition(scrollBar.getValue() - e.getScrollAmount());
            }
        }
    }
    
    /** temporary method to add some text to the text pane. */
    public void addTestText() {
        for (int i = 0; i <= 100; i++) {
            AttributedString attributedString = 
                    new AttributedString("this is line #" + i);
            textPane.addText(attributedString);
            i++;
            attributedString = new AttributedString("this is line #" + i);
            attributedString.addAttribute(TextAttribute.UNDERLINE, 
                    TextAttribute.UNDERLINE_ON,
                    0, attributedString.getIterator().getEndIndex());
            textPane.addText(attributedString);
            i++;
            attributedString = new AttributedString("this is line #" + i);
            attributedString.addAttribute(TextAttribute.WEIGHT, 
                    TextAttribute.WEIGHT_ULTRABOLD,
                    0, attributedString.getIterator().getEndIndex());
            textPane.addText(attributedString);
            i++;
            attributedString = new AttributedString("this is line #" + i);
            attributedString.addAttribute(TextAttribute.UNDERLINE, 
                    TextAttribute.UNDERLINE_ON,
                    5, attributedString.getIterator().getEndIndex());
            attributedString.addAttribute(TextAttribute.FOREGROUND, Color.GREEN,
                    5, 10);
            textPane.addText(attributedString);
            attributedString = new AttributedString("this is long, long, long, " 
                    + "long, long, long, long, long, long, long, long, long, " 
                    + "long, long, long, long, long, long, long, long, long, "
                    + "long, long, long line #" + i);
            textPane.addText(attributedString);
        }
        scrollBar.setMaximum(textPane.getNumLines());
        setScrollBarPosition(textPane.getNumLines());
    }
    
    /** temporary method to text the textpane */
    public static void main(String[] args) {
        TextPane tpc = new TextPane();
        tpc.setDoubleBuffered(true);
        tpc.setBackground(Color.WHITE);
        
        JFrame frame = new JFrame("Test textpane");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        frame.add(tpc);
        
        frame.setSize(new Dimension(400, 400));
        frame.setVisible(true);
        
        tpc.addTestText();
    }
    
    /** Canvas object to draw text. */
    private class TextPaneCanvas extends Canvas {
        
        /**
         * A version number for this class. It should be changed whenever the 
         * class structure is changed (or anything else that would prevent 
         * serialized objects being unserialized with the new class).
         */
        private static final long serialVersionUID = 1;
        
        /** Font render context to be used for the text in this pane. */
        private final FontRenderContext DEFAULT_FRC =
                new FontRenderContext(null, false, false);
        
        /** list of stylised lines of text. */
        private List<AttributedCharacterIterator> iterators;
        
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
        
        /**
         * Creates a new text pane canvas.
         * @param parent parent text pane for the canvas
         */
        public TextPaneCanvas(TextPane parent) {
            iterators = new ArrayList<AttributedCharacterIterator>();
            scrollBarPosition = 0;
            textPane = parent;
        }
        
        /**
         * Paints the text onto the canvas.
         * @param g graphics object to draw onto
         */
        public void paint(Graphics g) {
            Graphics2D graphics2D = (Graphics2D) g;
            
            float formatWidth = (float) getWidth();
            float formatHeight = (float) getHeight();
            
            float drawPosY = formatHeight;
            
            int startLine = scrollBarPosition;
            if (startLine >= iterators.size()) {
                startLine = iterators.size() - 1;
            }
            if (startLine <= 0) {
                startLine = 0;
            }
            
            for (int i = startLine; i >= 0; i--) {
                AttributedCharacterIterator iterator = iterators.get(i);
                paragraphStart = iterator.getBeginIndex();
                paragraphEnd = iterator.getEndIndex();;
                lineMeasurer = new LineBreakMeasurer(iterator, DEFAULT_FRC);
                lineMeasurer.setPosition(paragraphStart);
                
                while (lineMeasurer.getPosition() < paragraphEnd) {
                    
                    TextLayout layout = lineMeasurer.nextLayout(formatWidth);
                    drawPosY -= layout.getDescent() + layout.getLeading();
                    
                    float drawPosX;
                    if (layout.isLeftToRight()) {
                        drawPosX = 0;
                    } else {
                        drawPosX = formatWidth - layout.getAdvance();
                    }
                    
                    layout.draw(graphics2D, drawPosX, drawPosY);
                    
                    drawPosY -= layout.getAscent();
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
        public void update(Graphics g) {
            Image offScreen = this.createImage(getWidth(), getHeight());
            Graphics graphics = offScreen.getGraphics();
            
            graphics.clearRect(0, 0, this.getWidth(), this.getHeight());
            
            paint(graphics);
            
            g.drawImage(offScreen, 0, 0, this);
        }
        
        /**
         * Returns the number of lines in the component.
         * @return number of lines in the canvas
         */
        public int getNumLines() {
            return iterators.size() - 1;
        }
        
        /**
         * Adds the stylised string to the canvas.
         * @param text stylised string to add to the text
         */
        public void addText(AttributedString text) {
            synchronized (iterators) {
                iterators.add(text.getIterator());
            }
        }
        
        /**
         * sets the position of the scroll bar, and repaints if required.
         * @param position scroll bar position
         */
        public void setScrollBarPosition(int position) {
            if (scrollBarPosition != position) {
                scrollBarPosition = position;
                this.repaint();
            }
        }
    }
}
