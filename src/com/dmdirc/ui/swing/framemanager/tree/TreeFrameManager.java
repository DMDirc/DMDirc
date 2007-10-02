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

package com.dmdirc.ui.swing.framemanager.tree;

import com.dmdirc.FrameContainer;
import com.dmdirc.config.ConfigChangeListener;
import com.dmdirc.config.IdentityManager;
import com.dmdirc.logger.ErrorLevel;
import com.dmdirc.logger.Logger;
import com.dmdirc.ui.interfaces.FrameManager;
import com.dmdirc.ui.swing.components.TreeScroller;
import static com.dmdirc.ui.swing.UIUtilities.SMALL_BORDER;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.Serializable;
import java.util.Hashtable;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

/**
 * Manages open windows in the application in a tree style view.
 */
public final class TreeFrameManager implements FrameManager, MouseListener,
        ActionListener, MouseMotionListener, AdjustmentListener, Serializable,
        ConfigChangeListener, TreeSelectionListener {
    
    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 2;
    
    /** display tree. */
    private final JTree tree;
    
    /** root node. */
    private final DefaultMutableTreeNode root;
    
    /** data model. */
    private final TreeViewModel model;
    
    /** node storage, used for adding and deleting nodes correctly. */
    private final Map<FrameContainer, DefaultMutableTreeNode> nodes;
    
    /** Current rollover node. */
    private DefaultMutableTreeNode rolloverNode;
    
    /** popup menu for menu items on nodes. */
    private final JPopupMenu popup;
    
    /** close menu item used in popup menus. */
    private final JMenuItem closeMenuItem;
    
    /** node under right click operation. */
    private DefaultMutableTreeNode popupNode;
    
    
    /** creates a new instance of the TreeFrameManager. */
    public TreeFrameManager() {
        nodes = new Hashtable<FrameContainer, DefaultMutableTreeNode>();
        popup = new JPopupMenu();
        closeMenuItem = new JMenuItem("Close window");
        root = new DefaultMutableTreeNode("DMDirc");
        model = new TreeViewModel(root);
        tree = new JTree(model);
        
        final TreeViewTreeCellRenderer renderer = new TreeViewTreeCellRenderer(this);
        
        closeMenuItem.setActionCommand("Close");
        popup.add(closeMenuItem);
        
        popup.setOpaque(true);
        popup.setLightWeightPopupEnabled(true);
        
        tree.putClientProperty("JTree.lineStyle", "Angled");
        tree.setUI(new javax.swing.plaf.metal.MetalTreeUI());
        tree.getInputMap().setParent(null);
        tree.getInputMap(JComponent.WHEN_FOCUSED).clear();
        tree.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).clear();
        tree.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).clear();
        
        tree.getSelectionModel().
                setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        tree.setCellRenderer(renderer);
        tree.setRootVisible(false);
        tree.setRowHeight(0);
        tree.setShowsRootHandles(false);
        tree.setOpaque(true);
        tree.setBorder(BorderFactory.createEmptyBorder(SMALL_BORDER,
                SMALL_BORDER, SMALL_BORDER, SMALL_BORDER));
        tree.setVisible(true);
        
        closeMenuItem.addActionListener(this);
        
        tree.addMouseListener(this);
        tree.addMouseMotionListener(this);
        tree.addTreeSelectionListener(this);
        new TreeScroller(tree);
        
        IdentityManager.getGlobalConfig().addChangeListener("treeview", this);
        IdentityManager.getGlobalConfig().addChangeListener("ui", "backgroundcolour", this);
        IdentityManager.getGlobalConfig().addChangeListener("ui", "foregroundcolour", this);
    }
    
    /** {@inheritDoc} */
    public boolean canPositionVertically() {
        return true;
    }
    
    /** {@inheritDoc} */
    public boolean canPositionHorizontally() {
        return false;
    }
    
    /** {@inheritDoc} */
    public void setSelected(final FrameContainer source) {
        if (source != null) {
            final TreeNode[] treePath = ((DefaultTreeModel) tree.getModel()).
                    getPathToRoot(nodes.get(source));
            if (treePath == null || treePath.length == 0) {
                Logger.appError(ErrorLevel.MEDIUM, "Unknown node selected",
                        new IllegalArgumentException("Unknown node selected: "
                        + source));
                return;
            }
            tree.setSelectionPath(new TreePath(treePath));
        }
    }
    
    /** {@inheritDoc} */
    public void showNotification(final FrameContainer source, final Color colour) {
        tree.repaint();
    }
    
    /**
     * Sets the rollover node and repaints the tree.
     * @param node rollover node.
     */
    public void showRollover(final DefaultMutableTreeNode node) {
        rolloverNode = node;
        tree.repaint();
    }
    
    /**
     * retrives the rollover node.
     * @return rollover node.
     */
    public DefaultMutableTreeNode getRollover() {
        return rolloverNode;
    }
    
    /** {@inheritDoc} */
    public void clearNotification(final FrameContainer source) {
        tree.repaint();
    }
    
    /** {@inheritDoc} */
    public void setParent(final JComponent parent) {
        final JScrollPane scrollPane = new JScrollPane(tree);
        scrollPane.setAutoscrolls(true);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getHorizontalScrollBar().addAdjustmentListener(this);
        
        parent.setLayout(new BorderLayout());
        parent.add(scrollPane);
        
        tree.setBackground(IdentityManager.getGlobalConfig().getOptionColour("treeview", "backgroundcolour",
                IdentityManager.getGlobalConfig().getOptionColour("ui", "backgroundcolour", Color.WHITE)));
        tree.setForeground(IdentityManager.getGlobalConfig().getOptionColour("treeview", "foregroundcolour",
                IdentityManager.getGlobalConfig().getOptionColour("ui", "foregroundcolour", Color.BLACK)));
    }
    
    /** {@inheritDoc} */
    public void addWindow(final FrameContainer window) {
        final DefaultMutableTreeNode node = new DefaultMutableTreeNode();
        nodes.put(window, node);
        node.setUserObject(window);
        model.insertNodeInto(node, root);
        tree.expandPath(new TreePath(node.getPath()).getParentPath());
        final Rectangle view = tree.getRowBounds(tree.getRowForPath(new TreePath(node.getPath())));
        tree.scrollRectToVisible(new Rectangle(0, (int) view.getY(), 0, 0));
    }
    
    /** {@inheritDoc} */
    public void delWindow(final FrameContainer window) {
        if (nodes != null && nodes.get(window) != null) {
            final DefaultMutableTreeNode node = nodes.get(window);
            if (node.getLevel() == 0) {
                Logger.appError(ErrorLevel.MEDIUM,
                        "delServer triggered for root node" + node.toString(),
                        new IllegalArgumentException());
            } else {
                model.removeNodeFromParent(nodes.get(window));
            }
        }
    }
    
    /** {@inheritDoc} */
    public void addWindow(final FrameContainer parent, final FrameContainer window) {
        final DefaultMutableTreeNode node = new DefaultMutableTreeNode();
        nodes.put(window, node);
        node.setUserObject(window);
        model.insertNodeInto(node, nodes.get(parent));
        tree.expandPath(new TreePath(node.getPath()).getParentPath());
        final Rectangle view = tree.getRowBounds(tree.getRowForPath(new TreePath(node.getPath())));
        tree.scrollRectToVisible(new Rectangle(0, (int) view.getY(), 0, 0));
    }
    
    /** {@inheritDoc} */
    public void delWindow(final FrameContainer parent, final FrameContainer window) {
        if (nodes != null && nodes.get(window) != null) {
            if (nodes.get(window).getLevel() == 0) {
                Logger.appError(ErrorLevel.MEDIUM,
                        "delServer triggered for root node",
                        new IllegalArgumentException());
            } else {
                model.removeNodeFromParent(nodes.get(window));
            }
        }
    }
    
    /** {@inheritDoc} */
    public void iconUpdated(final FrameContainer window) {
        tree.repaint();
    }
    
    /**
     * Returns the tree for this frame manager.
     *
     * @return Tree for the manager
     */
    public JTree getTree() {
        return tree;
    }
    
    /** {@inheritDoc} */
    public void adjustmentValueChanged(final AdjustmentEvent e) {
        //HACK Disregard all scrolling events
        ((JScrollBar) e.getSource()).setValue(0);
    }
    
    /**
     * Invoked when the mouse button has been clicked (pressed and released)
     * on a component.
     * @param event mouse event.
     */
    public void mouseClicked(final MouseEvent event) {
        processMouseEvent(event);
    }
    
    /**
     * Invoked when a mouse button has been pressed on a component.
     * @param event mouse event.
     */
    public void mousePressed(final MouseEvent event) {
        if (event.getButton() == MouseEvent.BUTTON1) {
            final TreePath selectedPath = tree.getPathForLocation(event.getX(), event.getY());
            if (selectedPath != null) {
                tree.setSelectionPath(selectedPath);
            }
        }
        processMouseEvent(event);
    }
    
    /**
     * Invoked when a mouse button has been released on a component.
     * @param event mouse event.
     */
    public void mouseReleased(final MouseEvent event) {
        processMouseEvent(event);
    }
    
    /**
     * Invoked when the mouse enters a component.
     * @param event mouse event.
     */
    public void mouseEntered(final MouseEvent event) {
        //Do nothing
    }
    
    /**
     * Invoked when the mouse exits a component.
     * @param event mouse event.
     */
    public void mouseExited(final MouseEvent event) {
        tree.repaint();
    }
    
    /**
     * Processes every mouse button event to check for a popup trigger.
     * @param event mouse event
     */
    public void processMouseEvent(final MouseEvent event) {
        if (event.isPopupTrigger()) {
            final JTree source = (JTree) event.getSource();
            final TreePath path = tree.getPathForLocation(event.getX(),
                    event.getY());
            if (path != null) {
                popupNode = (DefaultMutableTreeNode) path.getLastPathComponent();
                popup.show(source, event.getX(), event.getY());
            }
        }
    }
    
    /**
     * Invoked when an action occurs.
     * @param event action event.
     */
    public void actionPerformed(final ActionEvent event) {
        if (event.getSource() == closeMenuItem && popupNode != null) {
            ((FrameContainer) popupNode.getUserObject()).close();
        }
    }
    
    /**
     * Invoked when a mouse button is pressed on a component and then dragged.
     *
     * @param event mouse event.
     */
    public void mouseDragged(final MouseEvent event) {
        final DefaultMutableTreeNode node = getNodeForLocation(
                event.getX(), event.getY());
        showRollover(node);
        if (node != null) {
            ((FrameContainer) node.getUserObject()).activateFrame();
        }
    }
    
    /**
     * Invoked when the mouse cursor has been moved onto a component but no
     * buttons have been pushed.
     *
     * @param event mouse event.
     */
    public void mouseMoved(final MouseEvent event) {
        showRollover(getNodeForLocation(event.getX(), event.getY()));
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
    private DefaultMutableTreeNode getNodeForLocation(final int x, final int y) {
        DefaultMutableTreeNode node = null;
        final TreePath selectedPath = tree.getPathForLocation(x, y);
        if (selectedPath != null) {
            node = (DefaultMutableTreeNode) selectedPath.getLastPathComponent();
        }
        return node;
    }
    
    /** {@inheritDoc} */
    public void configChanged(final String domain, final String key,
            final String oldValue, final String newValue) {
        tree.setBackground(IdentityManager.getGlobalConfig().getOptionColour("treeview", "backgroundcolour",
                IdentityManager.getGlobalConfig().getOptionColour("ui", "backgroundcolour", Color.WHITE)));
        tree.setForeground(IdentityManager.getGlobalConfig().getOptionColour("treeview", "foregroundcolour",
                IdentityManager.getGlobalConfig().getOptionColour("ui", "foregroundcolour", Color.BLACK)));
        
        tree.repaint();
    }
    
    /** {@inheritDoc} */
    public void valueChanged(final TreeSelectionEvent e) {
        ((FrameContainer )((DefaultMutableTreeNode) e.getPath().
                getLastPathComponent()).getUserObject()).activateFrame();
    }
    
}
