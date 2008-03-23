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

package com.dmdirc.ui.swing;

import com.dmdirc.config.prefs.PreferencesSetting;
import com.dmdirc.config.prefs.validator.NumericalValidator;
import com.dmdirc.ui.swing.components.ColourChooser;
import com.dmdirc.ui.swing.components.OptionalColourChooser;
import com.dmdirc.ui.swing.components.durationeditor.DurationDisplay;
import com.dmdirc.ui.swing.components.durationeditor.DurationListener;
import com.dmdirc.ui.swing.components.renderers.MapEntryRenderer;
import com.dmdirc.ui.swing.components.validating.ValidatingJTextField;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Map;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * Provides methods for constructing a JComponent from a PreferencesSetting.
 *
 * @author chris
 */
public final class PrefsComponentFactory {

    /**
     * Creates a new instance of PrefsComponentFactory.
     */
    private PrefsComponentFactory() {
        // Shouldn't be initialised
    }

    /**
     * Retrieves the component for the specified setting.
     *
     * @param setting The setting whose component is being requested
     * @return An appropriate JComponent descendant
     */
    public static JComponent getComponent(final PreferencesSetting setting) {
        JComponent option;

        switch (setting.getType()) {
            case TEXT:
                option = new ValidatingJTextField(setting.getValidator());
                ((ValidatingJTextField) option).setText(setting.getValue());
                ((ValidatingJTextField) option).addKeyListener(new KeyAdapter() {

                    @Override
                    public void keyReleased(final KeyEvent e) {
                        setting.setValue(((JTextField) e.getSource()).getText());
                    }
                });
                break;
            case BOOLEAN:
                option = new JCheckBox();
                ((JCheckBox) option).setSelected(Boolean.parseBoolean(setting.getValue()));

                ((JCheckBox) option).addChangeListener(new ChangeListener() {

                    public void stateChanged(final ChangeEvent e) {
                        setting.setValue(String.valueOf(((JCheckBox) e.getSource()).isSelected()));
                    }
                });

                break;
            case MULTICHOICE:
                option = new JComboBox(setting.getComboOptions().entrySet().
                        toArray());
                ((JComboBox) option).setRenderer(new MapEntryRenderer());
                ((JComboBox) option).setEditable(false);

                for (Map.Entry<String, String> entry : setting.getComboOptions().
                        entrySet()) {
                    if (entry.getKey().equals(setting.getValue())) {
                        ((JComboBox) option).setSelectedItem(entry);
                        break;
                    }
                }

                ((JComboBox) option).addActionListener(new ActionListener() {

                    public void actionPerformed(ActionEvent e) {
                        setting.setValue((String) ((Map.Entry) ((JComboBox) e.getSource()).getSelectedItem()).getKey());
                    }
                });

                break;
            case INTEGER:
                try {
                    if (setting.getValidator() instanceof NumericalValidator) {
                        option = new JSpinner(
                                new SpinnerNumberModel(Integer.parseInt(setting.getValue()),
                                ((NumericalValidator) setting.getValidator()).getMin(),
                                ((NumericalValidator) setting.getValidator()).getMax(),
                                1));
                    } else {
                        option = new JSpinner(new SpinnerNumberModel());

                        ((JSpinner) option).setValue(Integer.parseInt(setting.getValue()));
                    }
                } catch (NumberFormatException ex) {
                    option = new JSpinner(new SpinnerNumberModel());
                }

                ((JSpinner) option).addChangeListener(new ChangeListener() {

                    public void stateChanged(ChangeEvent e) {
                        setting.setValue(((JSpinner) e.getSource()).getValue().
                                toString());
                    }
                });

                break;
            case DURATION:
                try {
                    option =
                            new DurationDisplay(Integer.parseInt(setting.getValue()));
                } catch (NumberFormatException ex) {
                    option = new DurationDisplay();
                }

                ((DurationDisplay) option).addDurationListener(new DurationListener() {

                    public void durationUpdated(int newDuration) {
                        setting.setValue("" + newDuration);
                    }
                });

                break;
            case COLOUR:
                option = new ColourChooser(setting.getValue(), true, true);

                ((ColourChooser) option).addActionListener(new ActionListener() {

                    public void actionPerformed(ActionEvent e) {
                        setting.setValue(((ColourChooser) e.getSource()).getColour());
                    }
                });

                break;
            case OPTIONALCOLOUR:
                final boolean state = setting.getValue() != null &&
                        !setting.getValue().startsWith("false:");
                final String colour = setting.getValue() == null ? "0" : setting.getValue().
                        substring(1 + setting.getValue().indexOf(':'));

                option = new OptionalColourChooser(colour, state, true, true);

                ((OptionalColourChooser) option).addActionListener(new ActionListener() {

                    public void actionPerformed(final ActionEvent e) {
                        setting.setValue(
                                ((OptionalColourChooser) e.getSource()).isEnabled() + ":"
                                + ((OptionalColourChooser) e.getSource()).getColour());
                    }
                });

                break;
            default:
                throw new IllegalArgumentException(setting.getType()
                        + " is not a valid option type");
        }

        option.setPreferredSize(new Dimension(Short.MAX_VALUE, option.getFont().
                getSize()));

        return option;
    }

}
