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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.JComponent;

/**
 * Instantiates and manages the active frame managers.
 */
public final class MainFrameManager implements FrameManager,
        Serializable {

    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 2;
    /** Frame manager list. */
    private final List<FrameManager> frameManagers =
            new ArrayList<FrameManager>();
    /** Main frame manager. */
    private FrameManager mainFrameManager;

    /** Creates a new instance of MainFrameManager. */
    public MainFrameManager() {
        final String manager =
                IdentityManager.getGlobalConfig().
                getOption("ui", "framemanager", "treeview");

        if (manager.equalsIgnoreCase("buttonbar")) {
            mainFrameManager =
                    new ButtonBar();
        } else {
            mainFrameManager =
                    new TreeFrameManager();
        }

        frameManagers.add(new WindowMenuFrameManager());
        frameManagers.add(mainFrameManager);
    }

    /**
     * Reads the object from the stream.
     *
     * @param stream Stream to read the object from
     *
     * @throws IOException on error reading object
     * @throws ClassNotFoundException on error loading object class
     */
    private void readObject(final ObjectInputStream stream) throws IOException,
            ClassNotFoundException {
        stream.defaultReadObject();
        final String manager =
                IdentityManager.getGlobalConfig().
                getOption("ui", "framemanager", "treeview");

        if (manager.equalsIgnoreCase("buttonbar")) {
            frameManagers.add(new ButtonBar());
        } else {
            frameManagers.add(new TreeFrameManager());
        }

        frameManagers.add(new WindowMenuFrameManager());
    }

    /** {@inheritDoc} */
    @Override
    public void setParent(final JComponent parent) {
        synchronized (frameManagers) {
            final Iterator<FrameManager> it = frameManagers.iterator();
            while (it.hasNext()) {
                it.next().setParent(parent);
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public boolean canPositionVertically() {
        return mainFrameManager.canPositionVertically();
    }

    /** {@inheritDoc} */
    @Override
    public boolean canPositionHorizontally() {
        return mainFrameManager.canPositionHorizontally();
    }

    /** {@inheritDoc} */
    @Override
    public void setSelected(final FrameContainer source) {
        synchronized (frameManagers) {
            final Iterator<FrameManager> it = frameManagers.iterator();
            while (it.hasNext()) {
                it.next().setSelected(source);
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public void showNotification(final FrameContainer source,
            final Color colour) {
        synchronized (frameManagers) {
            final Iterator<FrameManager> it = frameManagers.iterator();
            while (it.hasNext()) {
                it.next().showNotification(source, colour);
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public void clearNotification(final FrameContainer source) {
        synchronized (frameManagers) {
            final Iterator<FrameManager> it = frameManagers.iterator();
            while (it.hasNext()) {
                it.next().clearNotification(source);
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public void addWindow(final FrameContainer window) {
        synchronized (frameManagers) {
            final Iterator<FrameManager> it = frameManagers.iterator();
            while (it.hasNext()) {
                it.next().addWindow(window);
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public void delWindow(final FrameContainer window) {
        synchronized (frameManagers) {
            final Iterator<FrameManager> it = frameManagers.iterator();
            while (it.hasNext()) {
                it.next().delWindow(window);
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public void addWindow(final FrameContainer parent,
            final FrameContainer window) {
        synchronized (frameManagers) {
            final Iterator<FrameManager> it = frameManagers.iterator();
            while (it.hasNext()) {
                it.next().addWindow(parent, window);
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public void delWindow(final FrameContainer parent,
            final FrameContainer window) {
        synchronized (frameManagers) {
            final Iterator<FrameManager> it = frameManagers.iterator();
            while (it.hasNext()) {
                it.next().delWindow(parent, window);
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public void iconUpdated(final FrameContainer window) {
        synchronized (frameManagers) {
            final Iterator<FrameManager> it = frameManagers.iterator();
            while (it.hasNext()) {
                it.next().iconUpdated(window);
            }
        }
    }

    /**
     * Returns the active frame manager.
     *
     * @return Active frame manage
     */
    public FrameManager getActiveFrameManager() {
        return mainFrameManager;
    }

    /**
     * Sets the main frame manager.
     *
     * @param mainFrameManager New main frame manager
     */
    public void setMainFrameManager(final FrameManager mainFrameManager) {
        this.mainFrameManager = mainFrameManager;
    }

    /**
     * Adds a frame manager to the list.
     *
     * @param frameManager Frame manager to add
     */
    public void addFrameManager(final FrameManager frameManager) {
        synchronized (frameManagers) {
            if (!frameManagers.contains(frameManager)) {
                frameManagers.add(frameManager);
            }
        }
    }

    /**
     * Removes a frame manager to the list.
     *
     * @param frameManager Frame manager to remove
     */
    public void removeFrameManager(final FrameManager frameManager) {
        synchronized (frameManagers) {
            frameManagers.remove(frameManager);
        }
    }
}