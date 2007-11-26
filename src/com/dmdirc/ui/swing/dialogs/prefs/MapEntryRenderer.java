
package com.dmdirc.ui.swing.dialogs.prefs;

import java.awt.Component;
import java.util.Map.Entry;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;

/**
 * Map entry renderer.
 */
public final class MapEntryRenderer extends DefaultListCellRenderer {

    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 1;

    @Override
    public Component getListCellRendererComponent(final JList list,
            final Object value, final int index, final boolean isSelected,
            final boolean cellHasFocus) {

        super.getListCellRendererComponent(list, value, index, isSelected,
                cellHasFocus);
        if (value == null) {
            setText("Any");
        } else if (value instanceof Entry) {
            setText((String) ((Entry) value).getKey());
        } else {
            setText(value.toString());
        }

        return this;
    }
}
