/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.dmdirc.ui.swing.components.renderers;

import com.dmdirc.actions.ActionGroup;
import java.awt.Component;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;

/**
 * Action group list cell renderer.
 */
public class ActionGroupListCellRenderer extends DefaultListCellRenderer {

    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 1;

    /** Creates a new instance of ActionGroupListCellRenderer. */
    public ActionGroupListCellRenderer() {
        super();
    }

    /** {@inheritDoc} */
    @Override
    public Component getListCellRendererComponent(final JList list,
            final Object value, final int index, final boolean isSelected,
            final boolean cellHasFocus) {

        super.getListCellRendererComponent(list, value, index, isSelected,
                cellHasFocus);

        if (value instanceof ActionGroup) {
            setText(((ActionGroup) value).getName());
        } else {
            setText(value.toString());
        }

        return this;
    }
}
