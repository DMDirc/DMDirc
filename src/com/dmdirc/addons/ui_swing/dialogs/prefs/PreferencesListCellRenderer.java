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

package com.dmdirc.addons.ui_swing.dialogs.prefs;

import com.dmdirc.addons.ui_swing.components.LoggingSwingWorker;
import com.dmdirc.config.prefs.PreferencesCategory;
import com.dmdirc.logger.ErrorLevel;
import com.dmdirc.logger.Logger;
import com.dmdirc.ui.IconManager;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

import net.miginfocom.layout.PlatformDefaults;

/**
 * Prefs dialog list cell renderer.
 *
 * @since 0.6.3
 */
public class PreferencesListCellRenderer extends JLabel implements ListCellRenderer {

    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 1;
    /** Panel gap. */
    private final int padding
            = (int) (1.5 * PlatformDefaults.getUnitValueX("related").getValue());
    /** Label map. */
    private Map<PreferencesCategory, JLabel> labelMap;
    
    public PreferencesListCellRenderer() {
        labelMap = new HashMap<PreferencesCategory, JLabel>();
    }

    /** {@inheritDoc} */
    @Override
    public final Component getListCellRendererComponent(final JList list,
            final Object value, final int index, final boolean isSelected,
            final boolean cellHasFocus) {
        final PreferencesCategory cat = (PreferencesCategory) value;
        if (!labelMap.containsKey(cat)) {
            labelMap.put(cat, new JLabel());
        }
        final JLabel label = labelMap.get(cat);
        label.setText(cat.getTitle());
        
        new LoggingSwingWorker<Icon, Void>() {

            /** {@inheritDoc} */
            @Override
            protected Icon doInBackground() throws Exception {
                return IconManager.getIconManager().getIcon(cat.getIcon());
            }

            /** {@inheritDoc} */
            @Override
            protected void done() {
                try {
                    label.setIcon(get());
                    list.repaint();
                } catch (InterruptedException ex) {
                    //Ignore
                } catch (ExecutionException ex) {
                    Logger.appError(ErrorLevel.LOW, ex.getMessage(), ex);
                }
                
            }
        }.execute();

        int level = 0;
        PreferencesCategory temp = cat;
        while (temp.getParent() != null) {
            temp = temp.getParent();
            level++;
        }

        label.setPreferredSize(new Dimension(100000, Math.max(16,
                getFont().getSize()) + padding));
        label.setBorder(BorderFactory.createEmptyBorder(padding / 2, padding + level * 18, padding / 2,
                padding));
        label.setBackground(list.getBackground());
        label.setForeground(list.getForeground());
        label.setOpaque(true);
        label.setToolTipText(null);

        if (cat.getPath().equals(cat.getTitle())) {
            boolean hasChildren = false;
            for (PreferencesCategory child : cat.getSubcats()) {
                if (!child.isInline()) {
                    hasChildren = true;
                    break;
                }
            }

            hasChildren = hasChildren || index + 1 == list.getModel().getSize();

            label.setBackground(Color.LIGHT_GRAY);
            label.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(1, 0, hasChildren ? 1 : 0, 0, Color.GRAY),
                    getBorder()));
        }

        if (isSelected) {
            label.setFont(getFont().deriveFont(Font.BOLD));
        } else {
            label.setFont(getFont().deriveFont(Font.PLAIN));
        }

        return label;
    }

}
