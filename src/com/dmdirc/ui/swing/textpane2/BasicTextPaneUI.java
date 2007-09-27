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

package com.dmdirc.ui.swing.textpane2;

import java.awt.Graphics;
import javax.swing.JComponent;
import javax.swing.plaf.ComponentUI;

/**
 * Basic UI implementation for TextPane.
 */
public class BasicTextPaneUI extends TextPaneUI {
    
    /** Creates a new instance of BasicTextPaneUI. */
    public BasicTextPaneUI() {
        super();
    }
    
    /**
     * Creates a new BasicTextPaneUI for the given component.
     *
     * @param component Component to create a new UI for
     */
    public static ComponentUI createUI(final JComponent component) {
        return new BasicTextPaneUI();
    }
    
    /** {@inheritDoc} */
    @Override
    public void installUI(final JComponent component) {
        installDefaults();
        installListeners();
        installComponents();        
    }
    
    /** {@inheritDoc} */
    @Override
    public void uninstallUI(final JComponent component) {
        uninstallComponents();
        uninstallListeners();
        uninstallDefaults();        
    }
    
    /** Installs default properties for the UI. */
    protected void installDefaults() {
        //install colours
        //install borders
        //set some properties
    }
    
    /** Uninstalls default properties for the UI. */
    protected void uninstallDefaults() { 
        //uninstall colours
        //uninstall borders
        //unset some properties
    }
    
    /** Installs required listeners for the UI. */
    protected void installListeners() {   
    }
    /** Uninstalls required listeners for the UI. */
    protected void uninstallListeners() {
    }
    
    /** Installs required components for the UI. */
    protected void installComponents() {
    }
    
    /** Uninstalls required components for the UI. */
    protected void uninstallComponents() {
    }

    /** {@inheritDoc} */
    @Override
    public void paint(final Graphics g, final JComponent c) {
    }
}
