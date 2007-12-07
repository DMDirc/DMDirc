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

package com.dmdirc.ui.swing.components.expandingsettings;

import com.dmdirc.config.Identity;
import com.dmdirc.ui.swing.components.TextLabel;
import static com.dmdirc.ui.swing.UIUtilities.SMALL_BORDER;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.UIManager;

/**
 * Settings panel.
 */
public final class SettingsPanel extends JPanel {
    
    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 2;
    
    /** Config manager. */
    private final transient Identity config;
    
    /** Valid option types. */
    public enum OptionType {
        /** Text field. */
        TEXTFIELD,
        /** Check box. */
        CHECKBOX,
        /** Colour chooser. */
        COLOUR,
        /** Number spinner. */
        SPINNER
    }
    /** config option -> name. */
    private Map<String, String> names;
    /** config option -> type. */
    private Map<String, OptionType> types;
    
    /** Info label. */
    private TextLabel infoLabel;
    
    /** Current options panel. */
    private CurrentOptionsPanel currentOptionsPanel;
    /** Add option panel. */
    private AddOptionPanel addOptionPanel;
    /** Current options scroll pane. */
    private JScrollPane scrollPane;
    
    /**
     * Creates a new instance of SettingsPanel.
     *
     * @param config Config to use
     * @param infoText Info blurb.
     */
    public SettingsPanel(final Identity config, final String infoText) {
        super();
        
        this.config = config;
        
        initComponents(infoText);
        layoutComponents();
    }
    
    /**
     * Initialises the components.
     *
     * @param infoText Info blurb.
     */
    private void initComponents(final String infoText) {
        names = new LinkedHashMap<String, String>();
        types = new LinkedHashMap<String, OptionType>();
        
        infoLabel = new TextLabel(infoText);
        
        addOptionPanel =
                new AddOptionPanel(this);
        currentOptionsPanel =
                new CurrentOptionsPanel(this);
        scrollPane = new JScrollPane(currentOptionsPanel);
        
        setBorder(BorderFactory.createEmptyBorder(SMALL_BORDER, SMALL_BORDER,
                SMALL_BORDER, SMALL_BORDER));
        
        scrollPane.setBorder(BorderFactory.createTitledBorder(
                UIManager.getBorder("TextField.border"), "Current settings"));
        
        addOptionPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder("Add new setting"),
                BorderFactory.createEmptyBorder(SMALL_BORDER, SMALL_BORDER,
                SMALL_BORDER, SMALL_BORDER)));
    }
    
    /** Lays out the components. */
    private void layoutComponents() {
        final GridBagConstraints constraints = new GridBagConstraints();
        
        setLayout(new GridBagLayout());
        
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.weightx = 1.0;
        constraints.weighty = 0.0;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        add(infoLabel, constraints);
        
        constraints.gridy = 1;
        constraints.weighty = 1.0;
        constraints.fill = GridBagConstraints.BOTH;
        add(scrollPane, constraints);
        
        constraints.gridy = 2;
        constraints.weighty = 0.0;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        add(addOptionPanel, constraints);
    }
    
    /**
     * Adds an option to the settings panel.
     *
     * @param optionName Option name
     * @param displayName Display name
     * @param type Option type
     */
    public void addOption(final String optionName,
            final String displayName,
            final OptionType type) {
        if (config == null) {
            return;
        }
        
        if (optionName.indexOf('.') == -1) {
            return;
        }
        final String[] splitOption = optionName.split("\\.");
        
        names.put(optionName, displayName);
        types.put(optionName, type);
        
        if (config.hasOption(splitOption[0], splitOption[1])) {
            addCurrentOption(optionName, type,
                    config.getOption(splitOption[0], splitOption[1]));
        } else {
            addAddableOption(optionName);
        }
    }
    
    /** Updates the options. */
    public void update() {
        addOptionPanel.clearOptions();
        currentOptionsPanel.clearOptions();
        
        for (Entry<String, OptionType> entry : types.entrySet()) {
            final String[] splitOption = entry.getKey().split("\\.");
            
            if (config.hasOption(splitOption[0], splitOption[1])) {
                addCurrentOption(entry.getKey(), entry.getValue(),
                        config.getOption(splitOption[0], splitOption[1]));
            } else {
                addAddableOption(entry.getKey());
            }
        }
    }
    
    /** Saves the options to the config. */
    public void save() {
        for (Entry<String, OptionType> entry : types.entrySet()) {
            final String value =
                    currentOptionsPanel.getOption(entry.getKey(),
                    entry.getValue());
            final String[] splitOption = entry.getKey().split("\\.");
            if (value == null) {
                config.unsetOption(splitOption[0], splitOption[1]);
            } else {
                config.setOption(splitOption[0], splitOption[1], value);
            }
        }
    }
    
    /**
     * Adds a current option.
     *
     * @param optionName option to add
     * @param type Option type
     * @param value Option value
     */
    protected void addCurrentOption(final String optionName,
            final OptionType type, final String value) {
        currentOptionsPanel.addOption(optionName, type, value);
    }
    
    /**
     * Deletes a current option.
     *
     * @param optionName Option to delete
     * @param type Option type
     */
    protected void removeCurrentOption(final String optionName,
            final OptionType type) {
        currentOptionsPanel.delOption(optionName, type);
    }
    
    /**
     * Adds an addable option.
     *
     * @param optionName Option name
     */
    protected void addAddableOption(final String optionName) {
        addOptionPanel.addOption(optionName);
    }
    
    /**
     * Returns the display name for a config option.
     *
     * @param optionName Option name to return the name for
     *
     * @return Display name for a specified option
     */
    public String getOptionName(final String optionName) {
        return names.get(optionName);
    }
    
    /**
     * Returns the option type for a config option.
     *
     * @param optionName Option name to return the type of
     *
     * @return Option type for a specified option
     */
    public OptionType getOptionType(final String optionName) {
        return types.get(optionName);
    }
}
