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

package com.dmdirc.ui.swing.components.renderers;

import com.dmdirc.ui.swing.framemanager.tree.NodeLabel;
import com.dmdirc.ui.swing.framemanager.tree.TreeFrameManager;
import com.dmdirc.ui.swing.framemanager.tree.TreeViewModel;

import java.awt.Component;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

/**
 * Displays a node in a tree according to its type.
 */
public class TreeViewTreeCellRenderer implements TreeCellRenderer,
        MouseMotionListener, MouseListener {

    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 3;
    /** Parent frame manager. */
    private final TreeFrameManager manager;
    /** Selection path. */
    private TreePath oldSelectedPath = null;

    /**
     * Creates a new instance of TreeViewTreeCellRenderer.
     *
     * @param manager Parent TreeFrameManager
     */
    public TreeViewTreeCellRenderer(final TreeFrameManager manager) {
        this.manager = manager;
        manager.getTree().addMouseMotionListener(this);
        manager.getTree().addMouseListener(this);
    }

    /**
     * Configures the renderer based on the passed parameters.
     *
     * @param tree JTree for this renderer.
     * @param value node to be renderered.
     * @param sel whether the node is selected.
     * @param expanded whether the node is expanded.
     * @param leaf whether the node is a leaf.
     * @param row the node's row.
     * @param hasFocus whether the node has focus.
     *
     * @return RendererComponent for this node.
     */
    @Override
    public final Component getTreeCellRendererComponent(final JTree tree,
            final Object value, final boolean sel, final boolean expanded,
            final boolean leaf, final int row, final boolean hasFocus) {
        final boolean rollover = (oldSelectedPath != null) && (value ==
                oldSelectedPath.getLastPathComponent());
        final NodeLabel label = manager.getLabelforNode((DefaultMutableTreeNode) value);
        label.setRollover(rollover);
        return label;
    }
    
    private void checkMousePosition(final MouseEvent e) {
        final JTree tree = manager.getTree();
        final TreeViewModel model = (TreeViewModel) tree.getModel();
        final int selectedRow = tree.getRowForLocation(e.getX(), e.getY());
        if (selectedRow > 0) {
            final TreePath selectedPath = tree.getPathForLocation(e.getX(),
                    e.getY());
            if (oldSelectedPath == null || !selectedPath.equals(oldSelectedPath)) {
                oldSelectedPath = selectedPath;
                model.nodeChanged((TreeNode) oldSelectedPath.getLastPathComponent());
            }
        } else {
            final TreePath currentSelected = oldSelectedPath;
            oldSelectedPath = null;
            if (currentSelected != null) {
                model.nodeChanged((TreeNode) currentSelected.getLastPathComponent());
            }
        }
    }

    /** 
     * {@inheritDoc}
     * 
     * @param e Mouse event
     */
    @Override
    public void mouseMoved(final MouseEvent e) {
        checkMousePosition(e);
    }

    /** 
     * {@inheritDoc}
     * 
     * @param e Mouse event
     */
    @Override
    public void mouseDragged(final MouseEvent e) {
        //Ignore
    }

    /** 
     * {@inheritDoc}
     * 
     * @param e Mouse event
     */
    @Override
    public void mouseClicked(final MouseEvent e) {
        //Ignore
    }

    /** 
     * {@inheritDoc}
     * 
     * @param e Mouse event
     */
    @Override
    public void mousePressed(final MouseEvent e) {
        //Ignore
    }

    /** 
     * {@inheritDoc}
     * 
     * @param e Mouse event
     */
    @Override
    public void mouseReleased(final MouseEvent e) {
        //Ignore
    }

    /** 
     * {@inheritDoc}
     * 
     * @param e Mouse event
     */
    @Override
    public void mouseEntered(final MouseEvent e) {
        checkMousePosition(e);
        manager.getTree().repaint();
    }

    /** 
     * {@inheritDoc}
     * 
     * @param e Mouse event
     */
    @Override
    public void mouseExited(final MouseEvent e) {
        oldSelectedPath = null;
        manager.getTree().repaint();
    }
}
