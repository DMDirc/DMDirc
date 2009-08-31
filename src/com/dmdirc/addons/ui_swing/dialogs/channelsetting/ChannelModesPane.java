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

package com.dmdirc.addons.ui_swing.dialogs.channelsetting;

import com.dmdirc.Channel;
import com.dmdirc.addons.ui_swing.components.ParamModePanel;
import com.dmdirc.addons.ui_swing.UIUtilities;

import com.dmdirc.parser.interfaces.Parser;
import java.awt.Insets;
import java.util.Hashtable;
import java.util.Map;
import javax.swing.BorderFactory;

import javax.swing.JCheckBox;
import javax.swing.JPanel;

import javax.swing.UIManager;
import net.miginfocom.swing.MigLayout;

/** Non list mode panel. */
public final class ChannelModesPane extends JPanel {

    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 1;
    /** Parent channel. */
    private final Channel channel;
    /** The checkboxes used for boolean modes. */
    private Map<String, JCheckBox> modeCheckBoxes;
    /** The ParamModePanels used for parameter-requiring modes. */
    private Map<String, ParamModePanel> modeInputs;

    /**
     * Creates a new instance of ChannelModesPane.
     *
     * @param channel Parent channel
     */
    public ChannelModesPane(final Channel channel) {
        super();

        this.channel = channel;

        this.setOpaque(UIUtilities.getTabbedPaneOpaque());
        initModesPanel();
        layoutComponents();

        setVisible(true);
    }

    /** Updates the panel. */
    public void update() {
        setVisible(false);
        removeAll();
        initModesPanel();
        setVisible(true);
    }

    /** Initialises the modes panel. */
    private void initModesPanel() {
        final Parser parser = channel.getServer().getParser();

        final String booleanModes = parser.getBooleanChannelModes();
        final String ourBooleanModes = channel.getChannelInfo().getModes();
        final String paramModes =
                parser.getParameterChannelModes() + parser.
                getDoubleParameterChannelModes();

        modeCheckBoxes =
                new Hashtable<String, JCheckBox>();

        // Lay out all the boolean mode checkboxes
        for (int i = 0; i < booleanModes.length();
                i++) {
            final String mode = booleanModes.substring(i, i + 1);
            final char modeChar = mode.toCharArray()[0];
            final boolean state =
                    ourBooleanModes.split(" ")[0].contains(
                    mode.subSequence(0, 1));
            String text;
            String tooltip;

            final boolean opaque = UIUtilities.getTabbedPaneOpaque();

            if (channel.getConfigManager().getOptionBool("server",
                    "friendlymodes") &&
                    channel.getConfigManager().hasOptionString("server",
                    "mode" + mode)) {
                text = channel.getConfigManager().
                        getOption("server", "mode" + mode);
            } else {
                text = "Mode " + mode;
            }

            if (channel.getConfigManager().hasOptionString("server", "mode" +
                    mode)) {
                tooltip =
                        "Mode " + mode + ": " +
                        channel.getConfigManager().
                        getOption("server", "mode" + mode);
            } else {
                tooltip = "Mode " + mode + ": Unknown";
            }

            final JCheckBox checkBox = new JCheckBox(text, state);
            checkBox.setMargin(new Insets(0, 0, 0, 0));
            checkBox.setToolTipText(tooltip);
            checkBox.setOpaque(opaque);

            modeCheckBoxes.put(mode, checkBox);
            if (!channel.getConfigManager().hasOptionString("server",
                    "enablemode" + modeChar) || channel.getConfigManager().
                    getOptionBool("server",
                    "enablemode" + modeChar)) {
                checkBox.setEnabled(true);
            } else if (!channel.getServer().getParser().isUserSettable(
                    modeChar)) {
                checkBox.setEnabled(false);
            }
        }

        // Lay out all the parameter-requiring modes
        modeInputs =
                new Hashtable<String, ParamModePanel>();

        for (int i = 0; i < paramModes.length();
                i++) {
            final String mode = paramModes.substring(i, i + 1);
            final String value =
                    channel.getChannelInfo().getMode(mode.charAt(0));
            final boolean state =
                    ourBooleanModes.split(" ")[0].contains(
                    mode.subSequence(0, 1));

            final ParamModePanel panel =
                    new ParamModePanel(mode, state, value,
                    channel.getConfigManager());

            modeInputs.put(mode, panel);
        }
    }

    /** Lays out the components. */
    private void layoutComponents() {
        final JPanel booleanModes =
                new JPanel(new MigLayout("wrap 2, fillx"));
        for (JCheckBox checkBox : modeCheckBoxes.values()) {
            booleanModes.add(checkBox);
        }

        final JPanel paramModes =
                new JPanel(new MigLayout("wrap 2, fillx"));
        for (ParamModePanel modePanel : modeInputs.values()) {
            paramModes.add(modePanel.getCheckboxComponent());
            paramModes.add(modePanel.getValueComponent(), "growx, pushx");
        }

        booleanModes.setBorder(BorderFactory.createTitledBorder(UIManager.
                getBorder("TitledBorder.border"), "Boolean modes"));
        paramModes.setBorder(BorderFactory.createTitledBorder(UIManager.
                getBorder("TitledBorder.border"), "Parameter modes"));

        booleanModes.setOpaque(UIUtilities.getTabbedPaneOpaque());
        paramModes.setOpaque(UIUtilities.getTabbedPaneOpaque());

        setLayout(new MigLayout("flowy, fillx", "fill", ""));
        add(booleanModes);
        add(paramModes);
    }

    /**
     * Processes the channel settings dialog and constructs a mode string for
     * changed modes, then sends this to the server.
     */
    public void setChangedBooleanModes() {
        boolean changed = false;
        final Parser parser = channel.getServer().getParser();
        final String booleanModes = parser.getBooleanChannelModes();
        final String ourBooleanModes = channel.getChannelInfo().getModes();
        final String paramModes =
                parser.getParameterChannelModes() + parser.
                getDoubleParameterChannelModes();

        for (int i = 0; i < booleanModes.length();
                i++) {
            final String mode = booleanModes.substring(i, i + 1);
            final boolean state =
                    ourBooleanModes.split(" ")[0].contains(
                    mode.subSequence(0, 1));

            if (modeCheckBoxes.get(mode) != null &&
                    state != modeCheckBoxes.get(mode).isSelected()) {
                changed = true;
                channel.getChannelInfo().
                        alterMode(modeCheckBoxes.get(mode).isSelected(),
                        mode.toCharArray()[0], "");
            }
        }

        for (int i = 0; i < paramModes.length();
                i++) {
            final String mode = paramModes.substring(i, i + 1);
            final String value =
                    channel.getChannelInfo().getMode(mode.charAt(0));
            final boolean state =
                    ourBooleanModes.split(" ")[0].contains(
                    mode.subSequence(0, 1));
            final ParamModePanel paramModePanel = modeInputs.get(mode);

            if (state != paramModePanel.getState() ||
                    !value.equals(paramModePanel.getValue())) {
                changed = true;
                channel.getChannelInfo().
                        alterMode(paramModePanel.getState(),
                        mode.toCharArray()[0], paramModePanel.getValue());
            }
        }
        if (changed) {
            channel.getChannelInfo().flushModes();
        }
    }
}
