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

package com.dmdirc.addons.ui_swing;

import com.dmdirc.config.prefs.PreferencesSetting;
import com.dmdirc.config.prefs.validator.NumericalValidator;
import com.dmdirc.addons.ui_swing.components.ColourChooser;
import com.dmdirc.addons.ui_swing.components.FontPicker;
import com.dmdirc.addons.ui_swing.components.OptionalColourChooser;
import com.dmdirc.addons.ui_swing.components.durationeditor.DurationDisplay;
import com.dmdirc.addons.ui_swing.components.durationeditor.DurationListener;
import com.dmdirc.addons.ui_swing.components.renderers.MapEntryRenderer;
import com.dmdirc.addons.ui_swing.components.validating.ValidatingJTextField;

import java.awt.Dimension;
import java.awt.Font;
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

import org.jdesktop.jxlayer.JXLayer;

/**
 * Provides methods for constructing a JComponent from a PreferencesSetting.
 */
public final class PrefsComponentFactory {

    /**
     * Creates a new instance of PrefsComponentFactory.
     */
    private PrefsComponentFactory() {
        // Shouldn't be initialised
    }

    /**
     * Retrieves the component for the specified setting. Components are
     * initialised with the current value(s) of the setting, and have listeners
     * added to update the setting whenever the components are changed.
     *
     * @param setting The setting whose component is being requested
     * @return An appropriate JComponent descendant
     */
    public static JComponent getComponent(final PreferencesSetting setting) {
        JComponent option;

        switch (setting.getType()) {
            case TEXT:
                option = getTextOption(setting);
                break;
            case BOOLEAN:
                option = getBooleanOption(setting);
                break;
            case MULTICHOICE:
                option = getComboOption(setting);
                break;
            case INTEGER:
                option = getIntegerOption(setting);
                break;
            case DURATION:
                option = getDurationOption(setting);
                break;
            case COLOUR:
                option = getColourOption(setting);
                break;
            case OPTIONALCOLOUR:
                option = getOptionalColourOption(setting);
                break;
            case FONT:
                option = getFontOption(setting);
                break;
            default:
                throw new IllegalArgumentException(setting.getType()
                        + " is not a valid option type");
        }

        option.setPreferredSize(new Dimension(Short.MAX_VALUE, option.getFont().
                getSize()));

        return new JXLayer<JComponent>(option);
    }

    /**
     * Initialises and returns a ValidatingJTextField for the specified setting.
     *
     * @param setting The setting to create the component for
     * @return A JComponent descendent for the specified setting
     */
    private static JComponent getTextOption(final PreferencesSetting setting) {
        final ValidatingJTextField option = new ValidatingJTextField(setting.getValidator());
        option.setText(setting.getValue());

        option.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(final KeyEvent e) {
                setting.setValue(((JTextField) e.getSource()).getText());
            }
        });

        return option;
    }

    /**
     * Initialises and returns a JCheckBox for the specified setting.
     *
     * @param setting The setting to create the component for
     * @return A JComponent descendent for the specified setting
     */
    private static JComponent getBooleanOption(final PreferencesSetting setting) {
        final JCheckBox option = new JCheckBox();
        option.setSelected(Boolean.parseBoolean(setting.getValue()));
        option.addChangeListener(new ChangeListener() {
            
            /** {@inheritDoc} */
            @Override
            public void stateChanged(final ChangeEvent e) {
                setting.setValue(String.valueOf(((JCheckBox) e.getSource()).isSelected()));
            }
        });

        return option;
    }

    /**
     * Initialises and returns a JComboBox for the specified setting.
     *
     * @param setting The setting to create the component for
     * @return A JComponent descendent for the specified setting
     */
    private static JComponent getComboOption(final PreferencesSetting setting) {
        final JComboBox option = new JComboBox(setting.getComboOptions().entrySet().toArray());
        option.setRenderer(new MapEntryRenderer());
        option.setEditable(false);

        for (Map.Entry<String, String> entry : setting.getComboOptions().entrySet()) {
            if (entry.getKey().equals(setting.getValue())) {
                option.setSelectedItem(entry);
                break;
            }
        }

        option.addActionListener(new ActionListener() {
            
            /** {@inheritDoc} */
            @Override
            public void actionPerformed(final ActionEvent e) {
                setting.setValue((String) ((Map.Entry)
                        ((JComboBox) e.getSource()).getSelectedItem()).getKey());
            }
        });

        return option;
    }

    /**
     * Initialises and returns a JSpinner for the specified setting.
     *
     * @param setting The setting to create the component for
     * @return A JComponent descendent for the specified setting
     */
    private static JComponent getIntegerOption(final PreferencesSetting setting) {
        JSpinner option;

        try {
            if (setting.getValidator() instanceof NumericalValidator) {
                option = new JSpinner(
                        new SpinnerNumberModel(Integer.parseInt(setting.getValue()),
                        ((NumericalValidator) setting.getValidator()).getMin(),
                        ((NumericalValidator) setting.getValidator()).getMax(),
                        1));
            } else {
                option = new JSpinner(new SpinnerNumberModel());
                option.setValue(Integer.parseInt(setting.getValue()));
            }
        } catch (NumberFormatException ex) {
            option = new JSpinner(new SpinnerNumberModel());
        }

        option.addChangeListener(new ChangeListener() {
            
            /** {@inheritDoc} */
            @Override
            public void stateChanged(final ChangeEvent e) {
                setting.setValue(((JSpinner) e.getSource()).getValue().
                        toString());
            }
        });

        return option;
    }

    /**
     * Initialises and returns a DurationDisplay for the specified setting.
     *
     * @param setting The setting to create the component for
     * @return A JComponent descendent for the specified setting
     */
    private static JComponent getDurationOption(final PreferencesSetting setting) {
        DurationDisplay option;

        try {
            option = new DurationDisplay(Integer.parseInt(setting.getValue()));
        } catch (NumberFormatException ex) {
            option = new DurationDisplay();
        }

        option.addDurationListener(new DurationListener() {
            
            /** {@inheritDoc} */
            @Override
            public void durationUpdated(final int newDuration) {
                setting.setValue(String.valueOf(newDuration));
            }
        });

        return option;
    }

    /**
     * Initialises and returns a ColourChooser for the specified setting.
     *
     * @param setting The setting to create the component for
     * @return A JComponent descendent for the specified setting
     */
    private static JComponent getColourOption(final PreferencesSetting setting) {
        final ColourChooser option = new ColourChooser(setting.getValue(), true, true);

        option.addActionListener(new ActionListener() {
            
            /** {@inheritDoc} */
            @Override
            public void actionPerformed(final ActionEvent e) {
                setting.setValue(((ColourChooser) e.getSource()).getColour());
            }
        });

        return option;
    }

    /**
     * Initialises and returns an OptionalColourChooser for the specified setting.
     *
     * @param setting The setting to create the component for
     * @return A JComponent descendent for the specified setting
     */
    private static JComponent getOptionalColourOption(final PreferencesSetting setting) {
        final boolean state = setting.getValue() != null
                && !setting.getValue().startsWith("false:");
        final String colour = setting.getValue() == null ? "0" : setting.getValue().
                substring(1 + setting.getValue().indexOf(':'));

        final OptionalColourChooser option = new OptionalColourChooser(colour, state, true, true);

        option.addActionListener(new ActionListener() {
            
            /** {@inheritDoc} */
            @Override
            public void actionPerformed(final ActionEvent e) {
                setting.setValue(
                        ((OptionalColourChooser) e.getSource()).isEnabled() + ":"
                        + ((OptionalColourChooser) e.getSource()).getColour());
            }
        });

        return option;
    }
    
    /**
     * Initialises and returns an Font Chooser for the specified setting.
     *
     * @param setting The setting to create the component for
     * @return A JComponent descendent for the specified setting
     */
    private static JComponent getFontOption(final PreferencesSetting setting) {
        final String value = setting.getValue();

        final FontPicker option = new FontPicker(value);
        
        option.addActionListener(new ActionListener() {

            /** {@inheritDoc} */
            @Override
            public void actionPerformed(final ActionEvent e) {
                Object value = ((FontPicker) e.getSource()).getSelectedItem();
                if (value instanceof Font) {
                    setting.setValue(((Font) value).getFamily());
                } else {
                    setting.setValue(null);
                }
            }
        });        
        
        return option;
    }

}
