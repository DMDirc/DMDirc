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

import com.dmdirc.FrameContainer;
import com.dmdirc.config.IdentityManager;
import com.dmdirc.interfaces.ConfigChangeListener;
import com.dmdirc.interfaces.FrameInfoListener;
import com.dmdirc.interfaces.NotificationListener;
import com.dmdirc.interfaces.SelectionListener;
import com.dmdirc.logger.ErrorLevel;
import com.dmdirc.logger.Logger;
import com.dmdirc.ui.interfaces.FrameManager;
import com.dmdirc.ui.interfaces.Window;
import com.dmdirc.addons.ui_swing.UIUtilities;
import com.dmdirc.ui.WindowManager;

import java.awt.Color;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import net.miginfocom.swing.MigLayout;

/**
 * Manages open windows in the application in a tree style view.
 */
public final class TreeFrameManager implements FrameManager,
        Serializable, ConfigChangeListener, SelectionListener,
        NotificationListener, FrameInfoListener {

    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 5;
    /** display tree. */
    private final Tree tree;
    /** data model. */
    private final TreeViewModel model;
    /** node storage, used for adding and deleting nodes correctly. */
    private final Map<FrameContainer, TreeViewNode> nodes;

    /** creates a new instance of the TreeFrameManager. */
    public TreeFrameManager() {
        nodes = new HashMap<FrameContainer, TreeViewNode>();
        model = new TreeViewModel(new TreeViewNode(null, null));
        tree = new Tree(this, model);

        tree.setCellRenderer(new TreeViewTreeCellRenderer(this));
        tree.setVisible(true);

        IdentityManager.getGlobalConfig().addChangeListener("treeview", this);
        IdentityManager.getGlobalConfig().addChangeListener("ui",
                "backgroundcolour", this);
        IdentityManager.getGlobalConfig().addChangeListener("ui",
                "foregroundcolour", this);
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
                scrollPane.setHorizontalScrollBarPolicy(
                        JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

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
        addWindow(model.getRootNode(), window);
    }

    /** {@inheritDoc} */
    @Override
    public void addWindow(final FrameContainer parent,
            final FrameContainer window) {
        addWindow(nodes.get(parent), window);
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
        UIUtilities.invokeLater(new Runnable() {

            /** {@inheritDoc} */
            @Override
            public void run() {
                if (nodes == null || nodes.get(window) == null) {
                    return;
                }
                final DefaultMutableTreeNode node =
                        nodes.get(window);
                if (node.getLevel() == 0) {
                    Logger.appError(ErrorLevel.MEDIUM,
                            "delServer triggered for root node" +
                            node.toString(),
                            new IllegalArgumentException());
                } else {
                    model.removeNodeFromParent(nodes.get(window));
                }
                synchronized (nodes) {
                    nodes.remove(window);
                }
                window.removeSelectionListener(TreeFrameManager.this);
                window.removeFrameInfoListener(TreeFrameManager.this);
                window.removeNotificationListener(TreeFrameManager.this);
            }
        });
    }

    /** 
     * Adds a window to the frame container.
     * 
     * @param parent Parent node
     * @param window Window to add
     */
    public void addWindow(final TreeViewNode parent,
            final FrameContainer window) {
        UIUtilities.invokeLater(new Runnable() {

            /** {@inheritDoc} */
            @Override
            public void run() {
                final TreeViewNode node =
                        new TreeViewNode(new NodeLabel(window.getFrame()),
                        window);
                synchronized (nodes) {
                    nodes.put(window, node);
                }
                if (parent == null) {
                    model.insertNodeInto(node, model.getRootNode());
                } else {
                    model.insertNodeInto(node, parent);
                }
                tree.expandPath(new TreePath(node.getPath()).getParentPath());
                final Rectangle view =
                        tree.getRowBounds(tree.getRowForPath(new TreePath(node.
                        getPath())));
                if (view != null) {
                    tree.scrollRectToVisible(new Rectangle(0, (int) view.getY(),
                            0, 0));
                }
                window.addSelectionListener(TreeFrameManager.this);
                window.addFrameInfoListener(TreeFrameManager.this);
                window.addNotificationListener(TreeFrameManager.this);
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
     * Checks for and sets a rollover node.
     * 
     * @param event event to check 
     */
    protected void checkRollover(final MouseEvent event) {
        NodeLabel node = null;

        if (event == null) {
            node = null;
        } else if (tree.getNodeForLocation(event.getX(), event.getY()) !=
                null) {
            node =
                    tree.getNodeForLocation(event.getX(), event.getY()).getLabel();
        }

        synchronized (nodes) {
            for (TreeViewNode treeNode : nodes.values()) {
                final NodeLabel label = treeNode.getLabel();
                label.setRollover(node == null ? false : label == node);
            }
        }
        tree.repaint();
    }

    /** Sets treeview colours. */
    private void setColours() {
        tree.setBackground(IdentityManager.getGlobalConfig().getOptionColour(
                "treeview", "backgroundcolour",
                "ui", "backgroundcolour"));
        tree.setForeground(IdentityManager.getGlobalConfig().getOptionColour(
                "treeview", "foregroundcolour",
                "ui", "foregroundcolour"));

        tree.repaint();
    }

    /** {@inheritDoc} */
    @Override
    public void configChanged(final String domain, final String key) {
        if ("sortservers".equals(key) || "sortwindows".equals(key)) {
            redoTreeView();
        } else {
            setColours();
        }
    }

    /**
     * Starts the tree from scratch taking into account new sort orders.
     */
    private void redoTreeView() {
        UIUtilities.invokeLater(new Runnable() {

            /** {@inheritDoc} */
            @Override
            public void run() {
                ((DefaultTreeModel) tree.getModel()).setRoot(null);
                ((DefaultTreeModel) tree.getModel()).setRoot(new TreeViewNode(
                        null, null));

                final Window[] rootWindows = WindowManager.getRootWindows();

                for (Window window : rootWindows) {
                    addWindow(window.getContainer());
                    final Window[] childWindows = WindowManager.getChildren(
                            window);
                    for (Window childWindow : childWindows) {
                        addWindow(window.getContainer(), childWindow.
                                getContainer());
                    }
                }
            }
        });
    }

    /** {@inheritDoc} */
    @Override
    public void selectionChanged(final Window window) {
        synchronized (nodes) {
            final Collection<TreeViewNode> collection =
                    new ArrayList<TreeViewNode>(nodes.values());
            for (TreeViewNode treeNode : collection) {
                final NodeLabel label = treeNode.getLabel();
                label.selectionChanged(window);
            }
        }

        if (window != null) {
            UIUtilities.invokeLater(new Runnable() {

                /** {@inheritDoc} */
                @Override
                public void run() {
                    final TreeNode[] treePath =
                            ((DefaultTreeModel) tree.getModel()).getPathToRoot(
                            nodes.get(window.getContainer()));
                    if (treePath != null && treePath.length > 0) {
                        final TreePath path = new TreePath(treePath);
                        if (path != null) {
                            tree.setTreePath(path);
                            tree.scrollPathToVisible(path);
                        }
                    }
                }
            });
        }
    }

    /** {@inheritDoc} */
    @Override
    public void notificationSet(final Window window, final Color colour) {
        SwingUtilities.invokeLater(new Runnable() {

            /** {@inheritDoc} */
            @Override
            public void run() {
                synchronized (nodes) {
                    final FrameContainer container = window.getContainer();
                    final TreeViewNode node = nodes.get(container);
                    if (container != null && node != null) {
                        final NodeLabel label = node.getLabel();
                        if (label != null) {
                            label.notificationSet(window, colour);
                            tree.repaint();
                        }
                    }
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
                synchronized (nodes) {
                    final FrameContainer container = window.getContainer();
                    final TreeViewNode node = nodes.get(container);
                    if (container != null && node != null) {
                        final NodeLabel label = node.getLabel();
                        if (label != null) {
                            label.notificationCleared(window);
                            tree.repaint();
                        }
                    }
                }
            }
        });
    }

    /** {@inheritDoc} */
    @Override
    public void iconChanged(final Window window, final String icon) {
        SwingUtilities.invokeLater(new Runnable() {

            /** {@inheritDoc} */
            @Override
            public void run() {
                synchronized (nodes) {
                    final TreeViewNode node = nodes.get(window.getContainer());
                    if (node != null) {
                        final NodeLabel label = node.getLabel();
                        if (label != null) {
                            label.iconChanged(window, icon);
                            tree.repaint();
                        }
                    }
                }
            }
        });
    }

    /** {@inheritDoc} */
    @Override
    public void nameChanged(final Window window, final String name) {
        SwingUtilities.invokeLater(new Runnable() {

            /** {@inheritDoc} */
            @Override
            public void run() {
                synchronized (nodes) {
                    final TreeViewNode node = nodes.get(window.getContainer());
                    if (node != null) {
                        final NodeLabel label = node.getLabel();
                        if (label != null) {
                            label.nameChanged(window, name);
                            tree.repaint();
                        }
                    }
                }
            }
        });
    }
}
