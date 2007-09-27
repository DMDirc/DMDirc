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

package com.dmdirc.ui.swing.components;

import java.awt.Component;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.JTextArea;

/**
 * Dyamnic text label.
 */
public class TextLabel extends JTextArea implements PropertyChangeListener {
    
    /**
     * A version number for this class. It should be changed whenever the
     * class structure is changed (or anything else that would prevent
     * serialized objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 1;
    
    /** Parent component. */
    private final Component comp;
    
    /**
     * Creates a new instance of TextLabel. 
     *
     * @param text Text to display
     */
    public TextLabel(final Component comp) {
        this(null, comp);
    }
    
    /**
     * Creates a new instance of TextLabel. 
     *
     * @param text Text to display
     * @param comp Parent component
     */
    public TextLabel(final String text, final Component comp) {
        super(text);
        
        this.comp = comp;
        
        init();
    }
    
    /** Initialiases the component. */
    private void init() {
        setEditable(false);
        setWrapStyleWord(true);
        setLineWrap(true);
        setHighlighter(null);
        setBackground(comp.getBackground());
        addPropertyChangeListener("UI", this);
    }

    /** {@inheritDoc} */
    public void propertyChange(final PropertyChangeEvent evt) {
        init();
    }
    
}
