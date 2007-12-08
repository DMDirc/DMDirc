/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.dmdirc.ui.swing.dialogs.actionsmanager;

import com.dmdirc.actions.ActionGroup;
import com.dmdirc.actions.ActionSetting;

import java.util.ArrayList;
import java.util.List;
import javax.swing.JLabel;
import javax.swing.JPanel;

import net.miginfocom.swing.MigLayout;

/**
 * Action group settings panel.
 */
public class ActionGroupSettingsPanel extends JPanel {

    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 1;
    /** Action group. */
    private ActionGroup group;
    /** Settings list. */
    private List<ActionSetting> settings;   

    /**
     * Initialises a new action group information panel.
     * 
     * @param group Action group
     */
    public ActionGroupSettingsPanel(final ActionGroup group) {
        initComponents();
        addListeners();
        
        setActionGroup(group);
    }

    /**
     * Initialises the components.
     */
    private void initComponents() {
        //
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
        setLayout(new MigLayout("fill, wrap 1"));

        for (ActionSetting setting : settings) {
            add(new JLabel(setting.getTitle()), "growx");
        }
    }

    /**
     * Sets the action group for the panel.
     * 
     * @param group New action group
     */
    public void setActionGroup(final ActionGroup group) {
        this.group = group;
        if (group == null || group.getSettings().isEmpty()) {
            this.settings = new ArrayList<ActionSetting>();
            setVisible(false);
        } else {
            this.settings = group.getSettings();
            setVisible(true);
        }
        
        layoutComponents();
    }
}
