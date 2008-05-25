
package com.dmdirc.ui.swing.dialogs.prefs;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JTree;
import javax.swing.tree.TreeCellRenderer;
import net.miginfocom.layout.PlatformDefaults;

public class PreferencesTreeCellRenderer extends JLabel implements TreeCellRenderer {

    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 1;
    /** Panel gap. */
    private final int padding = (int) PlatformDefaults.getUnitValueX("related").
            getValue();

    @Override
    public final Component getTreeCellRendererComponent(final JTree tree,
            final Object value, final boolean sel, final boolean expanded,
            final boolean leaf, final int row, final boolean focused) {
        setPreferredSize(new Dimension(100000,
                getFont().getSize() + padding));
        setBorder(BorderFactory.createEmptyBorder(0, 0, padding,
                padding));
        setText(value.toString());
        setBackground(tree.getBackground());
        setForeground(tree.getForeground());
        setOpaque(true);
        setToolTipText(null);

        if (sel) {
            setFont(getFont().deriveFont(Font.BOLD));
        } else {
            setFont(getFont().deriveFont(Font.PLAIN));
        }

        return this;
    }
}
