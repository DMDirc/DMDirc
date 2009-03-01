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

package com.dmdirc.addons.ui_swing.framemanager.tree;

import com.dmdirc.config.IdentityManager;
import com.dmdirc.interfaces.ConfigChangeListener;
import com.dmdirc.addons.ui_swing.actions.CloseFrameContainerAction;
import com.dmdirc.addons.ui_swing.components.TextFrame;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.beans.PropertyVetoException;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import net.miginfocom.layout.PlatformDefaults;

/**
 * Specialised JTree for the frame manager.
 */
public class Tree extends JTree implements TreeSelectionListener,
        MouseMotionListener, ConfigChangeListener, MouseListener {

    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 1;
    /** Drag selection enabled? */
    private boolean dragSelect;
    /** Tree frame manager. */
    private TreeFrameManager manager;
    /** Current selection path. */
    private TreePath path;

    /**
     * Specialised JTree for frame manager.
     *
     * @param manager Frame manager
     * @param model tree model.
     */
    public Tree(final TreeFrameManager manager, final TreeModel model) {
        super(model);
        
        this.manager = manager;
        
        putClientProperty("JTree.lineStyle", "Angled");
        getInputMap().setParent(null);
        getInputMap(JComponent.WHEN_FOCUSED).clear();
        getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).clear();
        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).clear();
        getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        setRootVisible(false);
        setRowHeight(0);
        setShowsRootHandles(false);
        setOpaque(true);
        setBorder(BorderFactory.createEmptyBorder(
                (int) PlatformDefaults.getUnitValueX("related").getValue(),
                (int) PlatformDefaults.getUnitValueX("related").getValue(),
                (int) PlatformDefaults.getUnitValueX("related").getValue(),
                (int) PlatformDefaults.getUnitValueX("related").getValue()));
        new TreeTreeScroller(this);
        setFocusable(false);

        dragSelect = IdentityManager.getGlobalConfig().getOptionBool("treeview",
                "dragSelection");
        IdentityManager.getGlobalConfig().addChangeListener("treeview",
                "dragSelection", this);

        addMouseListener(this);
        addMouseMotionListener(this);
        addTreeSelectionListener(this);
    }

    /**
     * Set path.
     *
     * @param path Path
     */
    public void setTreePath(final TreePath path) {
        this.path = path;
    }

    /** {@inheritDoc} */
    @Override
    public void valueChanged(final TreeSelectionEvent e) {
        if (path != null && !path.equals(e.getPath())) {
            setSelection(e.getPath());
        }
    }

    /**
     * Sets the tree selection path.
     *
     * @param path Tree path
     */
    public void setSelection(final TreePath path) {
        if (this.path != null && !this.path.equals(path)) {
            setTreePath(path);
            ((TreeViewNode) path.getLastPathComponent()).getFrameContainer().activateFrame();
        }
    }

    /**
     * Returns the node for the specified location, returning null if rollover
     * is disabled or there is no node at the specified location.
     *
     * @param x x coordiantes
     * @param y y coordiantes
     *
     * @return node or null
     */
    public TreeViewNode getNodeForLocation(final int x,
            final int y) {
        TreeViewNode node = null;
        final TreePath selectedPath = getPathForLocation(x, y);
        if (selectedPath != null) {
            node = (TreeViewNode) selectedPath.getLastPathComponent();
        }
        return node;
    }

    /** {@inheritDoc} */
    @Override
    public void configChanged(final String domain, final String key) {
        dragSelect = IdentityManager.getGlobalConfig().getOptionBool("treeview",
                "dragSelection");
    }
    
    /** 
     * {@inheritDoc}
     * 
     * @param e Mouse event
     */
    @Override
    public void mouseDragged(final MouseEvent e) {
        if (dragSelect) {
            final TreeViewNode node = getNodeForLocation(e.getX(), e.getY());
            if (node != null) {
                setSelection(new TreePath(node.getPath()));
            }
        }
        manager.checkRollover(e);
    }

    /** 
     * {@inheritDoc}
     * 
     * @param e Mouse event
     */
    @Override
    public void mouseMoved(final MouseEvent e) {
        manager.checkRollover(e);
    }

    /** 
     * {@inheritDoc}
     * 
     * @param e Mouse event
     */
    @Override
    public void mouseClicked(MouseEvent e) {
        processMouseEvents(e);
    }

    /** 
     * {@inheritDoc}
     * 
     * @param e Mouse event
     */
    @Override
    public void mousePressed(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON1) {
            final TreePath selectedPath = getPathForLocation(e.getX(), e.getY());
            if (selectedPath != null) {
                setSelection(selectedPath);
            }
        }
        processMouseEvents(e);
    }

    /** 
     * {@inheritDoc}
     * 
     * @param e Mouse event
     */
    @Override
    public void mouseReleased(MouseEvent e) {
        processMouseEvents(e);
    }

    /** 
     * {@inheritDoc}
     * 
     * @param e Mouse event
     */
    @Override
    public void mouseEntered(MouseEvent e) {
    //Ignore
    }

    /** 
     * {@inheritDoc}
     * 
     * @param e Mouse event
     */
    @Override
    public void mouseExited(MouseEvent e) {
        manager.checkRollover(null);
    }

    /**
     * Processes every mouse button event to check for a popup trigger.
     * @param e mouse event
     */
    public void processMouseEvents(final MouseEvent e) {
        final TreePath localPath = getPathForLocation(e.getX(), e.getY());
        if (localPath != null) {
            if (e.isPopupTrigger()) {
                final TextFrame frame = (TextFrame) ((TreeViewNode) localPath.getLastPathComponent()).getFrameContainer().
                        getFrame();
                final JPopupMenu popupMenu = frame.getPopupMenu(null, "");
                frame.addCustomPopupItems(popupMenu);
                if (popupMenu.getComponentCount() > 0) {
                    popupMenu.addSeparator();
                }
                popupMenu.add(new JMenuItem(new CloseFrameContainerAction(frame.getContainer())));
                popupMenu.show(this, e.getX(), e.getY());
            }
            if (((TextFrame) ((TreeViewNode) localPath.getLastPathComponent()).getFrameContainer().
                    getFrame()).isIcon()) {
                try {
                    ((TextFrame) ((TreeViewNode) localPath.getLastPathComponent()).getFrameContainer().
                            getFrame()).setIcon(false);
                    ((TreeViewNode) localPath.getLastPathComponent()).getFrameContainer().
                            activateFrame();
                } catch (PropertyVetoException ex) {
                //Ignore
                }
            }
        }
    }
}
