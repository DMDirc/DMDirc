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
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.font.TextAttribute;
import java.text.AttributedString;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JScrollBar;

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
    private static final long serialVersionUID = 2;
    
    /** Scrollbar for the component. */
    private JScrollBar scrollBar;
    /** Canvas object, used to draw text. */
    private TextPaneCanvas textPane;
    /** IRCDocument. */
    private IRCDocument document;
    
    /**
     * Creates a new instance of TextPane.
     */
    public TextPane() {
        
        document = new IRCDocument();
        
        this.setLayout(new BorderLayout());
        
        textPane = new TextPaneCanvas(this, document);
        this.add(textPane, BorderLayout.CENTER);
        
        
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
    private void setScrollBarPosition(final int position) {
        scrollBar.setValue(position);
        textPane.setScrollBarPosition(position);
    }
    
    /** {@inheritDoc}. */
    public void adjustmentValueChanged(final AdjustmentEvent e) {
        if (e.getValue() < document.getNumLines()) {
            scrollBar.setValue(e.getValue());
            textPane.setScrollBarPosition(e.getValue());
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
    
    /** temporary method to add some text to the text pane. */
    public void addTestText() {
        for (int i = 0; i <= 20; i++) {
            AttributedString attributedString =
                    new AttributedString("this is a line");
            document.addText(attributedString);
            attributedString = new AttributedString("this is a line");
            attributedString.addAttribute(TextAttribute.UNDERLINE,
                    TextAttribute.UNDERLINE_ON,
                    0, attributedString.getIterator().getEndIndex());
            document.addText(attributedString);
            attributedString = new AttributedString("this is a line");
            attributedString.addAttribute(TextAttribute.WEIGHT,
                    TextAttribute.WEIGHT_ULTRABOLD,
                    0, attributedString.getIterator().getEndIndex());
            document.addText(attributedString);
            attributedString = new AttributedString("this is a line");
            attributedString.addAttribute(TextAttribute.UNDERLINE,
                    TextAttribute.UNDERLINE_ON,
                    5, attributedString.getIterator().getEndIndex());
            attributedString.addAttribute(TextAttribute.FOREGROUND, Color.GREEN,
                    5, 10);
            document.addText(attributedString);
            attributedString = new AttributedString("this is a long, long, long, "
                    + "long, long, long, long, long, long, long, long, long, "
                    + "long, long, long, long, long, long, long, long, long, "
                    + "long, long, long line");
            document.addText(attributedString);
        }
        scrollBar.setMaximum(document.getNumLines());
        setScrollBarPosition(document.getNumLines());
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
