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
import com.dmdirc.IconManager;
import com.dmdirc.config.ConfigChangeListener;
import com.dmdirc.config.ConfigManager;
import com.dmdirc.config.IdentityManager;
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
public class TreeViewTreeCellRenderer extends JLabel implements
        TreeCellRenderer, ConfigChangeListener {
    
    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 2;
    
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
        super();
        
        this.manager = manager;
        
        defaultIcon = IconManager.getIconManager().getIcon("icon");
        config = IdentityManager.getGlobalConfig();
        
        rolloverColour = config.getOptionColour("ui",
                "treeviewRolloverColour", manager.getTree().getBackground());
        activeBackground = config.getOptionColour("ui",
                "treeviewActiveBackground", manager.getTree().getBackground());
        activeForeground = config.getOptionColour("ui",
                "treeviewActiveForeground", manager.getTree().getForeground());
        activeBold = config.getOptionBool("ui", "treeviewActiveBold", false);
        
        config.addChangeListener("ui", this);
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
    public final Component getTreeCellRendererComponent(final JTree tree,
            final Object value, final boolean sel, final boolean expanded,
            final boolean leaf, final int row, final boolean hasFocus) {
        
        final DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
        
        setText(node.toString());
        
        setBackground(tree.getBackground());
        setForeground(tree.getForeground());
        setOpaque(true);
        setToolTipText(null);
        setBorder(BorderFactory.createEmptyBorder(1, 0, 2, 0));
        
        setPreferredSize(new Dimension(100000, getFont().getSize()
        + SMALL_BORDER));
        
        if (tree.getMousePosition() != null && manager.getRollover() == value) {
            setBackground(rolloverColour);
        }
        
        final Object nodeObject = node.getUserObject();
        
        if (nodeObject instanceof FrameContainer) {
            setForeground(((FrameContainer) nodeObject).getNotification());
            setIcon(((FrameContainer) nodeObject).getIcon());
        } else {
            setIcon(defaultIcon);
        }
        
        if (sel) {
            if (activeBold) {
                setFont(getFont().deriveFont(Font.BOLD));
            }
            setBackground(activeBackground);
            setForeground(activeForeground);
        } else {
            setFont(getFont().deriveFont(Font.PLAIN));
        }
        
        return this;
    }
    
    /** {@inheritDoc} */
    public void configChanged(final String domain, final String key,
            final String oldValue, final String newValue) {
        if ("ui".equals(domain)) {
            if ("treeviewRolloverColour".equals(key)) {
                rolloverColour = config.getOptionColour("ui",
                        "treeviewRolloverColour", getBackground());
            } else if ("treeviewActiveBackground".equals(key)) {
                activeBackground = config.getOptionColour("ui",
                        "treeviewActiveBackground", getBackground());
            } else if ("treeviewActiveForeground".equals(key)) {
                activeForeground = config.getOptionColour("ui",
                        "treeviewActiveForeground", getForeground());
            } else if ("treeviewActiveBold".equals(key)) {
                activeBold = config.getOptionBool("ui", "treeviewActiveBold", false);
            }
        }
    }
}
