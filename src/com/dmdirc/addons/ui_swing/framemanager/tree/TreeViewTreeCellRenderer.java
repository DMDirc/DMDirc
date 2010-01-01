/*
 * Copyright (c) 2006-2010 Chris Smith, Shane Mc Cormack, Gregory Holmes
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

import com.dmdirc.interfaces.ConfigChangeListener;
import com.dmdirc.config.ConfigManager;
import com.dmdirc.config.IdentityManager;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;

import javax.swing.JLabel;
import javax.swing.JTree;
import javax.swing.tree.TreeCellRenderer;

/**
 * Displays a node in a tree according to its type.
 */
public class TreeViewTreeCellRenderer implements TreeCellRenderer,
        ConfigChangeListener {

    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 3;
    /** Parent frame manager. */
    private final TreeFrameManager manager;
    /** Config manager. */
    private final ConfigManager config;
    /** Rollover colours. */
    private Color rolloverColour;
    /** Active bold. */
    private boolean activeBold;
    /** Active background. */
    private Color activeBackground;
    /** Active foreground. */
    private Color activeForeground;

    /**
     * Creates a new instance of TreeViewTreeCellRenderer.
     *
     * @param manager Parent TreeFrameManager
     */
    public TreeViewTreeCellRenderer(final TreeFrameManager manager) {
        this.manager = manager;

        config = IdentityManager.getGlobalConfig();

        setColours();

        config.addChangeListener("ui", this);
        config.addChangeListener("treeview", this);
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

        if (value == null) {
            return new JLabel("Node == null");
        }
        final NodeLabel label = ((TreeViewNode) value).getLabel();
        if (label == null) {
            return new JLabel("Label == null");
        }

        label.setBackground(tree.getBackground());
        label.setForeground(tree.getForeground());

        if (label.isRollover()) {
            label.setBackground(rolloverColour);
        }

        final Color colour = label.getNotificationColour();
        if (colour != null) {
            label.setForeground(colour);
        }

        if (label.isSelected()) {
            if (activeBold) {
                label.setFont(label.getFont().deriveFont(Font.BOLD));
            } else {
                label.setFont(label.getFont().deriveFont(Font.PLAIN));
            }
            label.setBackground(activeBackground);
            label.setForeground(activeForeground);
        } else {
            label.setFont(label.getFont().deriveFont(Font.PLAIN));
        }

        return label;
    }

    /** Sets the colours for the renderer. */
    private void setColours() {
        rolloverColour = config.getOptionColour(
                "ui", "treeviewRolloverColour",
                "treeview", "backgroundcolour",
                "ui", "backgroundcolour");
        activeBackground = config.getOptionColour(
                "ui", "treeviewActiveBackground",
                "treeview", "backgroundcolour",
                "ui", "backgroundcolour");
        activeForeground = config.getOptionColour(
                "ui", "treeviewActiveForeground",
                "treeview", "foregroundcolour",
                "ui", "foregroundcolour");
        activeBold = config.getOptionBool("ui", "treeviewActiveBold");

        manager.getTree().repaint();
    }

    /** {@inheritDoc} */
    @Override
    public void configChanged(final String domain, final String key) {
        if (("ui".equals(domain) || "treeview".equals(domain)) &&
                ("treeviewRolloverColour".equals(key) ||
                "treeviewActiveBackground".equals(key) ||
                "treeviewActiveForeground".equals(key) ||
                "treeviewActiveBold".equals(key) ||
                "backgroundcolour".equals(key) ||
                "foregroundcolour".equals(key))) {
            setColours();
        }
    }
}
