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

import com.dmdirc.addons.ui_swing.UIUtilities;
import com.dmdirc.config.IdentityManager;
import com.dmdirc.interfaces.ConfigChangeListener;
import com.dmdirc.addons.ui_swing.actions.CloseFrameContainerAction;
import com.dmdirc.addons.ui_swing.components.TreeScroller;
import com.dmdirc.addons.ui_swing.components.frames.TextFrame;

import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import net.miginfocom.layout.PlatformDefaults;

/**
 * Specialised JTree for the frame manager.
 */
public class Tree extends JTree implements MouseMotionListener,
        ConfigChangeListener, MouseListener, ActionListener {

    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 1;
    /** Drag selection enabled? */
    private boolean dragSelect;
    /** Drag button 1? */
    private boolean dragButton;
    /** Tree frame manager. */
    private TreeFrameManager manager;

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
        getSelectionModel().setSelectionMode(
                TreeSelectionModel.SINGLE_TREE_SELECTION);
        setRootVisible(false);
        setRowHeight(0);
        setShowsRootHandles(false);
        setOpaque(true);
        setBorder(BorderFactory.createEmptyBorder(
                (int) PlatformDefaults.getUnitValueX("related").getValue(),
                (int) PlatformDefaults.getUnitValueX("related").getValue(),
                (int) PlatformDefaults.getUnitValueX("related").getValue(),
                (int) PlatformDefaults.getUnitValueX("related").getValue()));
        new TreeScroller(this) {

            /** {@inheritDoc} */
            @Override
            protected void setPath(final TreePath path) {
                super.setPath(path);
                ((TreeViewNode) path.getLastPathComponent()).getFrameContainer().
                        activateFrame();
            }
        };
        setFocusable(false);

        dragSelect = IdentityManager.getGlobalConfig().getOptionBool("treeview",
                "dragSelection");
        IdentityManager.getGlobalConfig().addChangeListener("treeview", this);

        addMouseListener(this);
        addMouseMotionListener(this);
    }

    /** {@inheritDoc} */
    @Override
    public void scrollRectToVisible(final Rectangle aRect) {
        final Rectangle rect = new Rectangle(0, aRect.y,
                aRect.width, aRect.height);
        super.scrollRectToVisible(rect);
    }

    /**
     * Set path.
     *
     * @param path Path
     */
    public void setTreePath(final TreePath path) {
        UIUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                setSelectionPath(path);
            }
        });
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
        if ("dragSelection".equals(key)) {
            dragSelect = IdentityManager.getGlobalConfig().getOptionBool(
                    "treeview",
                    "dragSelection");
        }
    }

    /** 
     * {@inheritDoc}
     * 
     * @param e Mouse event
     */
    @Override
    public void mouseDragged(final MouseEvent e) {
        if (dragSelect && dragButton) {
            final TreeViewNode node = getNodeForLocation(e.getX(), e.getY());
            if (node != null) {
                ((TreeViewNode) new TreePath(node.getPath()).
                        getLastPathComponent()).getFrameContainer().
                        activateFrame();
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
            dragButton = true;
            final TreePath selectedPath = getPathForLocation(e.getX(), e.getY());
            if (selectedPath != null) {
                ((TreeViewNode) selectedPath.getLastPathComponent()).
                        getFrameContainer().activateFrame();
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
        dragButton = false;
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
                final TextFrame frame = (TextFrame) ((TreeViewNode) localPath.
                        getLastPathComponent()).getFrameContainer().
                        getFrame();
                final JPopupMenu popupMenu = frame.getPopupMenu(null, "");
                frame.addCustomPopupItems(popupMenu);
                if (popupMenu.getComponentCount() > 0) {
                    popupMenu.addSeparator();
                }
                    final TreeViewNodeMenuItem moveUp =
                            new TreeViewNodeMenuItem("Move Up", "Up",
                            (TreeViewNode) localPath.getLastPathComponent());
                    final TreeViewNodeMenuItem moveDown =
                            new TreeViewNodeMenuItem("Move Down", "Down",
                            (TreeViewNode) localPath.getLastPathComponent());

                    moveUp.addActionListener(this);
                    moveDown.addActionListener(this);

                    popupMenu.add(moveUp);
                    popupMenu.add(moveDown);
                popupMenu.add(new JMenuItem(new CloseFrameContainerAction(frame.
                        getContainer())));
                popupMenu.show(this, e.getX(), e.getY());
            }
        }
    }

    /**
     * {@inheritDoc}
     *
     * @param e Action event
     */
    @Override
    public void actionPerformed(final ActionEvent e) {
        final TreeViewNode node = ((TreeViewNodeMenuItem) e.getSource()).
                getTreeNode();
        int index = getModel().getIndexOfChild(node.getParent(), node);
        if ("Up".equals(e.getActionCommand())) {
            if (index == 0) {
                index = node.getSiblingCount() - 1;
            } else {
                index--;
            }
        } else if ("Down".equals(e.getActionCommand())) {
            if (index == (node.getSiblingCount() - 1)) {
                index = 0;
            } else {
                index++;
            }
        }
        final TreeViewNode parentNode = (TreeViewNode) node.getParent();
        final TreePath nodePath = new TreePath(node.getPath());
        final boolean isExpanded = isExpanded(nodePath);
        ((TreeViewModel) getModel()).removeNodeFromParent(node);
        ((TreeViewModel) getModel()).insertNodeInto(node, parentNode, index);
        setExpandedState(nodePath, isExpanded);
    }
}