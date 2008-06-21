/*
 * Copyright (c) 2006-2008 Chris Smith, Shane Mc Cormack, Gregory Holmes
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
import com.dmdirc.config.IdentityManager;
import com.dmdirc.interfaces.ConfigChangeListener;
import com.dmdirc.interfaces.IconChangeListener;
import com.dmdirc.interfaces.NotificationListener;
import com.dmdirc.interfaces.SelectionListener;
import com.dmdirc.logger.ErrorLevel;
import com.dmdirc.logger.Logger;
import com.dmdirc.ui.interfaces.FrameManager;
import com.dmdirc.ui.interfaces.Window;
import com.dmdirc.ui.swing.actions.CloseFrameContainerAction;
import com.dmdirc.ui.swing.components.TextFrame;
import com.dmdirc.ui.swing.components.TreeScroller;

import java.awt.Color;
import java.awt.Rectangle;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.beans.PropertyVetoException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import net.miginfocom.layout.PlatformDefaults;
import net.miginfocom.swing.MigLayout;

/**
 * Manages open windows in the application in a tree style view.
 */
public final class TreeFrameManager implements FrameManager, MouseListener,
        MouseMotionListener, AdjustmentListener, Serializable,
        ConfigChangeListener, TreeSelectionListener, SelectionListener,
        NotificationListener, IconChangeListener {

    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 5;
    /** display tree. */
    private final JTree tree;
    /** root node. */
    private final DefaultMutableTreeNode root;
    /** data model. */
    private final TreeViewModel model;
    /** node storage, used for adding and deleting nodes correctly. */
    private final Map<FrameContainer, DefaultMutableTreeNode> nodes;
    /** Label storage. */
    private final Map<DefaultMutableTreeNode, NodeLabel> labels;
    /** Drag selection enabled? */
    private boolean dragSelect;

    /** creates a new instance of the TreeFrameManager. */
    public TreeFrameManager() {
        nodes = new HashMap<FrameContainer, DefaultMutableTreeNode>();
        labels = new HashMap<DefaultMutableTreeNode, NodeLabel>();
        root = new DefaultMutableTreeNode("DMDirc");
        model = new TreeViewModel(root);
        tree = new JTree(model);
        labels.put(root, new NodeLabel(null));

        final TreeViewTreeCellRenderer renderer =
                new TreeViewTreeCellRenderer(this);

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
        tree.setBorder(BorderFactory.createEmptyBorder(
                (int) PlatformDefaults.getUnitValueX("related").getValue(),
                (int) PlatformDefaults.getUnitValueX("related").getValue(),
                (int) PlatformDefaults.getUnitValueX("related").getValue(),
                (int) PlatformDefaults.getUnitValueX("related").getValue()));
        tree.setVisible(true);

        tree.addMouseListener(this);
        tree.addMouseMotionListener(this);
        tree.addTreeSelectionListener(this);
        new TreeScroller(tree);
        tree.setFocusable(false);

        dragSelect = IdentityManager.getGlobalConfig().getOptionBool("treeview",
                "dragSelection", true);

        IdentityManager.getGlobalConfig().addChangeListener("treeview", this);
        IdentityManager.getGlobalConfig().addChangeListener("ui",
                "backgroundcolour", this);
        IdentityManager.getGlobalConfig().addChangeListener("ui",
                "foregroundcolour", this);
        IdentityManager.getGlobalConfig().addChangeListener("treeview",
                "dragSelection", this);
    }

    /** {@inheritDoc} */
    @Override
    public boolean canPositionVertically() {
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public boolean canPositionHorizontally() {
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public void setParent(final JComponent parent) {
        SwingUtilities.invokeLater(new Runnable() {

            /** {@inheritDoc} */
            @Override
            public void run() {
                final JScrollPane scrollPane = new JScrollPane(tree);
                scrollPane.setAutoscrolls(true);
                scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
                scrollPane.getHorizontalScrollBar().addAdjustmentListener(TreeFrameManager.this);

                parent.setLayout(new MigLayout("ins 0, fill"));
                parent.add(scrollPane, "grow");
                parent.setFocusable(false);

                setColours();
            }
        });
    }

    /** {@inheritDoc} */
    @Override
    public void addWindow(final FrameContainer window) {
        addWindow(root, window);
    }

    /** {@inheritDoc} */
    @Override
    public void addWindow(final FrameContainer parent,
            final FrameContainer window) {
        final Runnable runnable = new Runnable() {

            /** {@inheritDoc} */
            @Override
            public void run() {
                addWindow(nodes.get(parent), window);
            }
        };
        if (SwingUtilities.isEventDispatchThread()) {
            runnable.run();
        }
        SwingUtilities.invokeLater(runnable);
    }

    /** {@inheritDoc} */
    @Override
    public void delWindow(final FrameContainer parent,
            final FrameContainer window) {
        delWindow(window);
    }

    /** {@inheritDoc} */
    @Override
    public void delWindow(final FrameContainer window) {
        if (nodes != null && nodes.get(window) != null) {
            SwingUtilities.invokeLater(new Runnable() {

                /** {@inheritDoc} */
                @Override
                public void run() {
                    synchronized (labels) {
                        final DefaultMutableTreeNode node = nodes.get(window);
                        if (node.getLevel() == 0) {
                            Logger.appError(ErrorLevel.MEDIUM,
                                    "delServer triggered for root node" +
                                    node.toString(),
                                    new IllegalArgumentException());
                        } else {
                            model.removeNodeFromParent(nodes.get(window));
                        }
                        nodes.remove(window);
                        labels.remove(node);
                        window.removeSelectionListener(TreeFrameManager.this);
                        window.removeIconChangeListener(TreeFrameManager.this);
                        window.removeNotificationListener(TreeFrameManager.this);
                    }
                }
            });
        }
    }

    /** 
     * Adds a window to the frame container.
     * 
     * @param parent Parent node
     * @param window Window to add
     */
    public void addWindow(final DefaultMutableTreeNode parent,
            final FrameContainer window) {
        SwingUtilities.invokeLater(new Runnable() {

            /** {@inheritDoc} */
            @Override
            public void run() {
                synchronized (labels) {
                    final DefaultMutableTreeNode node =
                            new DefaultMutableTreeNode();
                    nodes.put(window, node);
                    labels.put(node, new NodeLabel(window.getFrame()));
                    node.setUserObject(window);
                    if (parent == null) {
                        model.insertNodeInto(node, root);
                    } else {
                        model.insertNodeInto(node, parent);
                    }
                    tree.expandPath(new TreePath(node.getPath()).getParentPath());
                    final Rectangle view =
                            tree.getRowBounds(tree.getRowForPath(new TreePath(node.getPath())));
                    tree.scrollRectToVisible(new Rectangle(0, (int) view.getY(),
                            0, 0));
                    window.addSelectionListener(TreeFrameManager.this);
                    window.addIconChangeListener(TreeFrameManager.this);
                    window.addNotificationListener(TreeFrameManager.this);
                }
            }
        });
    }

    /**
     * Returns the tree for this frame manager.
     *
     * @return Tree for the manager
     */
    public JTree getTree() {
        return tree;
    }

    /** 
     * {@inheritDoc}
     * 
     * @param e Adjustment event
     */
    @Override
    public void adjustmentValueChanged(final AdjustmentEvent e) {
        //HACK Disregard all scrolling events
        ((JScrollBar) e.getSource()).setValue(0);
    }

    /**
     * Invoked when the mouse button has been clicked (pressed and released)
     * on a component.
     * @param event mouse event.
     */
    @Override
    public void mouseClicked(final MouseEvent event) {
        processMouseEvent(event);
    }

    /**
     * Invoked when a mouse button has been pressed on a component.
     * @param event mouse event.
     */
    @Override
    public void mousePressed(final MouseEvent event) {
        if (event.getButton() == MouseEvent.BUTTON1) {
            final TreePath selectedPath = tree.getPathForLocation(event.getX(),
                    event.getY());
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
    @Override
    public void mouseReleased(final MouseEvent event) {
        processMouseEvent(event);
    }

    /**
     * Invoked when the mouse enters a component.
     * @param event mouse event.
     */
    @Override
    public void mouseEntered(final MouseEvent event) {
    //Do nothing
    }

    /**
     * Invoked when the mouse exits a component.
     * @param event mouse event.
     */
    @Override
    public void mouseExited(final MouseEvent event) {
        checkRollover(null);
    }

    /**
     * Processes every mouse button event to check for a popup trigger.
     * @param event mouse event
     */
    public void processMouseEvent(final MouseEvent event) {
        final JTree source = (JTree) event.getSource();
        final TreePath path = tree.getPathForLocation(event.getX(),
                event.getY());
        if (path != null) {
            if (event.isPopupTrigger()) {
                final TextFrame frame =
                        (TextFrame) ((FrameContainer) ((DefaultMutableTreeNode) path.getLastPathComponent()).getUserObject()).getFrame();
                final JPopupMenu popupMenu =
                        frame.getPopupMenu(null, "");
                frame.addCustomPopupItems(popupMenu);
                if (popupMenu.getComponentCount() > 0) {
                    popupMenu.addSeparator();
                }
                popupMenu.add(new JMenuItem(new CloseFrameContainerAction(frame.getContainer())));
                popupMenu.show(source, event.getX(), event.getY());
            }
            if (((TextFrame) ((FrameContainer) ((DefaultMutableTreeNode) path.getLastPathComponent()).getUserObject()).getFrame()).isIcon()) {
                try {
                    ((TextFrame) ((FrameContainer) ((DefaultMutableTreeNode) path.getLastPathComponent()).getUserObject()).getFrame()).setIcon(false);
                    ((FrameContainer) ((DefaultMutableTreeNode) path.getLastPathComponent()).getUserObject()).activateFrame();
                } catch (PropertyVetoException ex) {
                //Ignore
                }
            }
        }
    }

    /**
     * Checks for and sets a rollover node.
     * 
     * @param event event to check 
     */
    protected void checkRollover(final MouseEvent event) {
        final NodeLabel node;

        if (event == null) {
            node = null;
        } else {
            synchronized (labels) {
                node = labels.get(getNodeForLocation(event.getX(), event.getY()));
            }
        }

        synchronized (labels) {
            for (NodeLabel label : labels.values()) {
                label.setRollover(node == null ? false : label == node);
            }
        }
        tree.repaint();
    }

    /**
     * Invoked when a mouse button is pressed on a component and then dragged.
     *
     * @param event mouse event.
     */
    @Override
    public void mouseDragged(final MouseEvent event) {
        if (dragSelect) {
            final DefaultMutableTreeNode node = getNodeForLocation(
                    event.getX(), event.getY());
            if (node != null) {
                ((FrameContainer) node.getUserObject()).activateFrame();
            }
        }
        checkRollover(event);
    }

    /**
     * Invoked when the mouse cursor has been moved onto a component but no
     * buttons have been pushed.
     *
     * @param event mouse event.
     */
    @Override
    public void mouseMoved(final MouseEvent event) {
        checkRollover(event);
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
    private DefaultMutableTreeNode getNodeForLocation(final int x,
            final int y) {
        DefaultMutableTreeNode node = null;
        final TreePath selectedPath = tree.getPathForLocation(x, y);
        if (selectedPath != null) {
            node = (DefaultMutableTreeNode) selectedPath.getLastPathComponent();
        }
        return node;
    }

    /** 
     * Returns the label for a specified node.
     *
     * @param node Node to get label for
     *
     * @return Label for node
     */
    public NodeLabel getLabelforNode(final DefaultMutableTreeNode node) {
        synchronized (labels) {
            return labels.get(node);
        }
    }

    /** Sets treeview colours. */
    private void setColours() {
        tree.setBackground(IdentityManager.getGlobalConfig().getOptionColour("treeview",
                "backgroundcolour",
                IdentityManager.getGlobalConfig().getOptionColour("ui",
                "backgroundcolour", Color.WHITE)));
        tree.setForeground(IdentityManager.getGlobalConfig().getOptionColour("treeview",
                "foregroundcolour",
                IdentityManager.getGlobalConfig().getOptionColour("ui",
                "foregroundcolour", Color.BLACK)));

        tree.repaint();
    }

    /** {@inheritDoc} */
    @Override
    public void configChanged(final String domain, final String key) {
        if ("treeview".equals(domain) && "dragSelection".equals(key)) {
            dragSelect =
                    IdentityManager.getGlobalConfig().getOptionBool("treeview",
                    "dragSelection", true);
            return;
        }
        setColours();

        tree.repaint();
    }

    /** {@inheritDoc} */
    @Override
    public void valueChanged(final TreeSelectionEvent e) {
        ((FrameContainer) ((DefaultMutableTreeNode) e.getPath().
                getLastPathComponent()).getUserObject()).activateFrame();
    }

    /** {@inheritDoc} */
    @Override
    public void selectionChanged(final Window window) {
        synchronized (labels) {
            for (NodeLabel label : labels.values()) {
                label.selectionChanged(window);
            }
        }
        if (window != null) {
            final TreeNode[] treePath =
                    ((DefaultTreeModel) tree.getModel()).getPathToRoot(nodes.get(window.getContainer()));
            if (treePath != null && treePath.length > 0) {
                tree.setSelectionPath(new TreePath(treePath));
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public void notificationSet(final Window window, final Color colour) {
        SwingUtilities.invokeLater(new Runnable() {

            /** {@inheritDoc} */
            @Override
            public void run() {
                synchronized (labels) {
                    labels.get(nodes.get(window.getContainer())).notificationSet(window,
                            colour);
                    tree.repaint();
                }
            }
        });
    }

    /** {@inheritDoc} */
    @Override
    public void notificationCleared(final Window window) {
        SwingUtilities.invokeLater(new Runnable() {

            /** {@inheritDoc} */
            @Override
            public void run() {
                synchronized (labels) {
                    labels.get(nodes.get(window.getContainer())).
                            notificationCleared(window);
                    tree.repaint();
                }
            }
        });
    }

    /** {@inheritDoc} */
    @Override
    public void iconChanged(final Window window, final Icon icon) {
        SwingUtilities.invokeLater(new Runnable() {

            /** {@inheritDoc} */
            @Override
            public void run() {
                synchronized (labels) {
                    final NodeLabel label =
                            labels.get(nodes.get(window.getContainer()));
                    if (label != null) {
                        label.iconChanged(window, icon);
                        tree.repaint();
                    }
                }
            }
        });
    }
}
