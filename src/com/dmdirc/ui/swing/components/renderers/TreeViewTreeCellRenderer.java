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

import com.dmdirc.FrameContainer;
import com.dmdirc.ui.IconManager;
import com.dmdirc.interfaces.ConfigChangeListener;
import com.dmdirc.config.ConfigManager;
import com.dmdirc.config.IdentityManager;
import com.dmdirc.ui.messages.ColourManager;
import com.dmdirc.ui.swing.framemanager.tree.TreeFrameManager;
import static com.dmdirc.ui.swing.UIUtilities.SMALL_BORDER;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
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
    
    /** The default icon to use for unknown frames. */
    private final Icon defaultIcon;
    
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
        
        defaultIcon = IconManager.getIconManager().getIcon("icon");
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
        
        final DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
        final JLabel label = manager.getLabelforNode(node);
        
        if (node == null) {
            label.setText("null");
            return label;
        }
        
        label.setText(node.toString());
        
        label.setBackground(tree.getBackground());
        label.setForeground(tree.getForeground());
        label.setOpaque(true);
        label.setToolTipText(null);
        label.setBorder(BorderFactory.createEmptyBorder(1, 0, 2, 0));
        
        label.setPreferredSize(new Dimension(100000, label.getFont().getSize()
        + SMALL_BORDER));
        
        if (tree.getMousePosition() != null && manager.getRollover() == value) {
            label.setBackground(rolloverColour);
        }
        
        final Object nodeObject = node.getUserObject();
        
        if (nodeObject instanceof FrameContainer) {
            final Color colour = manager.getNotificationColour((FrameContainer) nodeObject);
            if (colour != null) {
              label.setForeground(colour);
            }
            label.setIcon(((FrameContainer) nodeObject).getIcon());
        } else {
            label.setIcon(defaultIcon);
        }
        
        if (sel) {
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
                config.getOptionColour("treeview", "backgroundcolour",
                config.getOptionColour("ui", "backgroundcolour", 
                ColourManager.parseColour("f0f0f0"))));
        activeBackground = config.getOptionColour(
                "ui", "treeviewActiveBackground", 
                config.getOptionColour("treeview", "backgroundcolour",
                config.getOptionColour("ui","backgroundcolour", 
                manager.getTree().getBackground())));
        activeForeground = config.getOptionColour(
                "ui", "treeviewActiveForeground", 
                config.getOptionColour("treeview", "foregroundcolour",
                config.getOptionColour("ui", "foregroundcolour", 
                manager.getTree().getForeground())));
        activeBold = config.getOptionBool("ui", "treeviewActiveBold", false);
        
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
