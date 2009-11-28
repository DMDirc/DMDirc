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

package com.dmdirc.addons.ui_swing.dialogs.actionsmanager;

import com.dmdirc.actions.ActionGroup;
import com.dmdirc.config.prefs.PreferencesSetting;
import com.dmdirc.addons.ui_swing.PrefsComponentFactory;
import com.dmdirc.addons.ui_swing.components.colours.ColourChooser;
import com.dmdirc.addons.ui_swing.components.colours.OptionalColourChooser;
import com.dmdirc.addons.ui_swing.components.durationeditor.DurationDisplay;

import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import net.miginfocom.swing.MigLayout;

/**
 * Action group settings panel.
 */
public final class ActionGroupSettingsPanel extends JPanel implements ActionListener {

    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 1;
    /** Settings list. */
    private Collection<PreferencesSetting> settings;
    /** Button -> Component map. */
    private Map<JButton, PreferencesSetting> settingMap;
    /** Parent dialog. */
    private Window window;
    
    /**
     * Initialises a new action group information panel.
     * 
     * @param group Action group
     */
    public ActionGroupSettingsPanel(final ActionGroup group) {
        this(group, null);
    }

    /**
     * Initialises a new action group information panel.
     * 
     * @param group Action group
     * @param window Parent window
     * 
     * @since 0.6
     */
    public ActionGroupSettingsPanel(final ActionGroup group, final Window window) {
        super();
        
        this.window = window;

        initComponents();
        addListeners();

        setActionGroup(group);
    }

    /**
     * Initialises the components.
     */
    private void initComponents() {
        settingMap = new HashMap<JButton, PreferencesSetting>();
    }

    /**
     * Adds listeners.
     */
    private void addListeners() {
    //Empty
    }

    /**
     * Lays out the components.
     */
    private void layoutComponents() {
        removeAll();
        setLayout(new MigLayout("fill, hidemode 3"));

        for (PreferencesSetting setting : settings) {
            final JLabel label = new JLabel(setting.getTitle());
            label.setToolTipText(setting.getTitle());
            final JComponent component =
                    PrefsComponentFactory.getComponent(setting);
            if (component instanceof DurationDisplay) {
            ((DurationDisplay) component).setWindow(window);
        } else if (component instanceof ColourChooser) {
            ((ColourChooser) component).setWindow(window);
        } else if (component instanceof OptionalColourChooser) {
            ((OptionalColourChooser) component).setWindow(window);
        }
            final JButton button = new SettingsRevertButton(setting);
            settingMap.put(button, setting);
            button.addActionListener(this);
            add(label, "newline");
            add(component, "growx, pushx");
            add(button, "");
        }
    }

    /**
     * Sets the action group for the panel.
     * 
     * @param group New action group
     */
    public void setActionGroup(final ActionGroup group) {
        if (group == null || group.getSettings().isEmpty()) {
            this.settings = new ArrayList<PreferencesSetting>();
        } else {
            this.settings = group.getSettings().values();
        }

        layoutComponents();
    }

    /**
     * Should the settings panel be shown?
     * 
     * @return true iif the panel should be shown
     */
    public boolean shouldDisplay() {
        return !settings.isEmpty();
    }

    /**
     * Saves the changes to the settings.
     */
    public void save() {
        for (PreferencesSetting setting : settings) {
            setting.save();
        }
    }

    /** 
     * {@inheritDoc}
     * 
     * @param e Action event
     */
    @Override
    public void actionPerformed(final ActionEvent e) {
        setVisible(false);
        settingMap.get(e.getSource()).dismiss();
        layoutComponents();
        setVisible(true);
    }
}
