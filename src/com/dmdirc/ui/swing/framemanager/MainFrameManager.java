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

package com.dmdirc.ui.swing.framemanager;

import com.dmdirc.FrameContainer;
import com.dmdirc.config.IdentityManager;
import com.dmdirc.ui.interfaces.FrameManager;
import com.dmdirc.ui.swing.framemanager.buttonbar.ButtonBar;
import com.dmdirc.ui.swing.framemanager.tree.TreeFrameManager;
import com.dmdirc.ui.swing.framemanager.windowmenu.WindowMenuFrameManager;

import java.awt.Color;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;

import javax.swing.JComponent;

/**
 * Instantiates and manages the active frame managers.
 */
public final class MainFrameManager implements FrameManager, Serializable {
    
    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 1;
    
    /** Active frame manager. */
    private transient FrameManager frameManager;
    
    /** Window menu frame manager. */
    private final WindowMenuFrameManager windowMenuFrameManager;
    
    /** Creates a new instance of MainFrameManager. */
    public MainFrameManager() {
        final String manager = IdentityManager.getGlobalConfig().getOption("ui", "framemanager", "treeview");
        
        if (manager.equalsIgnoreCase("buttonbar")) {
            frameManager = new ButtonBar();
        } else {
            frameManager = new TreeFrameManager();
        }
        
        windowMenuFrameManager = new WindowMenuFrameManager();
    }
    
    /** 
     * Reads the object from the stream. 
     *
     * @param stream Stream to read the object from
     *
     * @throws IOException on error reading object
     * @throws ClassNotFoundException on error loading object class
     */
    private void readObject(final ObjectInputStream stream)
    throws IOException, ClassNotFoundException {
        stream.defaultReadObject();
        final String manager = IdentityManager.getGlobalConfig().getOption("ui", "framemanager", "treeview");
        
        if (manager.equalsIgnoreCase("buttonbar")) {
            frameManager = new ButtonBar();
        } else {
            frameManager = new TreeFrameManager();
        }
    }
    
    /** {@inheritDoc} */
    public void setParent(final JComponent parent) {
        frameManager.setParent(parent);
    }
    
    /** {@inheritDoc} */
    public boolean canPositionVertically() {
        return frameManager.canPositionVertically();
    }
    
    /** {@inheritDoc} */
    public boolean canPositionHorizontally() {
        return frameManager.canPositionHorizontally();
    }
    
    /** {@inheritDoc} */
    public void setSelected(final FrameContainer source) {
        frameManager.setSelected(source);
        windowMenuFrameManager.setSelected(source);
    }
    
    /** {@inheritDoc} */
    public void showNotification(final FrameContainer source, final Color colour) {
        frameManager.showNotification(source, colour);
        windowMenuFrameManager.showNotification(source, colour);
    }
    
    /** {@inheritDoc} */
    public void clearNotification(final FrameContainer source) {
        frameManager.clearNotification(source);
        windowMenuFrameManager.clearNotification(source);
    }
    
    /** {@inheritDoc} */
    public void addWindow(final FrameContainer window) {
        frameManager.addWindow(window);
        windowMenuFrameManager.addWindow(window);
    }
    
    /** {@inheritDoc} */
    public void delWindow(final FrameContainer window) {
        frameManager.delWindow(window);
        windowMenuFrameManager.delWindow(window);
    }
        
    /** {@inheritDoc} */
    public void addWindow(final FrameContainer parent, final FrameContainer window) {
        frameManager.addWindow(parent, window);
        windowMenuFrameManager.addWindow(parent, window);
    }
    
    /** {@inheritDoc} */
    public void delWindow(final FrameContainer parent, final FrameContainer window) {
        frameManager.delWindow(parent, window);
        windowMenuFrameManager.delWindow(parent, window);
    }
    
    /** {@inheritDoc} */
    public void iconUpdated(final FrameContainer window) {
        frameManager.iconUpdated(window);
        windowMenuFrameManager.iconUpdated(window);
    }    
    
    /**
     * Returns the active frame manager.
     *
     * @return Active frame manage
     */
    public FrameManager getActiveFrameManager() {
        return frameManager;
    }
    
}
