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

package com.dmdirc.ui.framemanager.tree;

import com.dmdirc.Config;
import com.dmdirc.FrameContainer;
import com.dmdirc.ui.messages.ColourManager;
import static com.dmdirc.ui.UIUtilities.SMALL_BORDER;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;

/**
 * Displays a node in a tree according to its type.
 */
public class TreeViewTreeCellRenderer extends DefaultTreeCellRenderer {
    
    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 1;
    
    /**
     * The default icon to use for unknown frames.
     */
    private final ImageIcon defaultIcon;
    
    /** Parent frame manager. */
    private final TreeFrameManager manager;
    
    /**
     * Creates a new instance of TreeViewTreeCellRenderer.
     *
     * @param manager Parent TreeFrameManager
     */
    public TreeViewTreeCellRenderer(final TreeFrameManager manager) {
        super();
        this.manager = manager;
        defaultIcon = new ImageIcon(this.getClass().getClassLoader()
        .getResource("com/dmdirc/res/icon.png"));
    }
    
    /**
     * Configures the renderer based on the passed parameters.
     * @param tree JTree for this renderer.
     * @param value node to be renderered.
     * @param sel whether the node is selected.
     * @param expanded whether the node is expanded.
     * @param leaf whether the node is a leaf.
     * @param row the node's row.
     * @param hasFocus whether the node has focus.
     * @return RendererComponent for this node.
     */
    public final Component getTreeCellRendererComponent(final JTree tree,
            final Object value, final boolean sel, final boolean expanded,
            final boolean leaf, final int row, final boolean hasFocus) {
        
        super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf,
                row, hasFocus);
        
        final DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
        
        setBackground(tree.getBackground());
        setOpaque(true);
        setToolTipText(null);
        setBorder(BorderFactory.createEmptyBorder(1, 0, 2, 0));
        setForeground(tree.getForeground());
        
        /*final int indent = (UIManager.getInt("Tree.leftChildIndent")
        + UIManager.getInt("Tree.rightChildIndent")) * (node.getLevel() - 1);
        setPreferredSize(new Dimension(manager.getNodeWidth() - indent,
                getFont().getSize() + SMALL_BORDER));*/
        setPreferredSize(new Dimension(100000, getFont().getSize() + SMALL_BORDER));
        if (manager.getRollover() == value) {
            final Color fallback = ColourManager.getColour("b8d6e6");
            setBackground(Config.getOptionColor("ui", "treeviewRolloverColour",
                    fallback));
        }
        
        final Object nodeObject = node.getUserObject();
        
        if (nodeObject.equals(manager.getSelected())) {
            if (Config.hasOption("ui", "treeviewActiveBold")
            && Config.getOptionBool("ui", "treeviewActiveBold")) {
                setFont(getFont().deriveFont(Font.BOLD));
            }
            setBackground(Config.getOptionColor("ui", "treeviewActiveBackground", tree.getBackground()));
            setForeground(Config.getOptionColor("ui", "treeviewActiveForeground", tree.getForeground()));
        } else {
            setFont(getFont().deriveFont(Font.PLAIN));
        }
        
        if (nodeObject instanceof FrameContainer) {
            final Color colour =
                    manager.getNodeColour((FrameContainer) nodeObject);
            if (colour != null) {
                setForeground(colour);
            }
            setIcon(((FrameContainer) nodeObject).getIcon());
        } else {
            setIcon(defaultIcon);
        }
        return this;
    }
}
