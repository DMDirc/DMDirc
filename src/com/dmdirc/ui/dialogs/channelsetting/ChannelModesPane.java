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

package com.dmdirc.ui.dialogs.channelsetting;

import com.dmdirc.Channel;
import com.dmdirc.parser.IRCParser;
import com.dmdirc.ui.components.ParamModePanel;
import static com.dmdirc.ui.UIUtilities.SMALL_BORDER;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.Hashtable;
import java.util.Map;
import javax.swing.BorderFactory;

import javax.swing.JCheckBox;
import javax.swing.JPanel;

/**
 * Non list mode panel.
 */
public class ChannelModesPane extends JPanel {
    
    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 1;
    
    /** Parent channel. */
    private Channel channel;
    
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
        this.channel = channel;
        
        initModesPanel();
        
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
        final GridBagConstraints constraints = new GridBagConstraints();
        
        final IRCParser parser = channel.getServer().getParser();
        
        final String booleanModes = parser.getBoolChanModes();
        final String ourBooleanModes = channel.getChannelInfo().getModeStr();
        final String paramModes = parser.getSetOnlyChanModes()
                + parser.getSetUnsetChanModes();
        
        setLayout(new GridBagLayout());
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), "Channel Modes"),
                BorderFactory.createEmptyBorder(SMALL_BORDER, SMALL_BORDER, SMALL_BORDER,
                SMALL_BORDER)));
        
        modeCheckBoxes = new Hashtable<String, JCheckBox>();
        
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.fill = GridBagConstraints.BOTH;
        constraints.anchor = GridBagConstraints.WEST;
        constraints.weightx = 1.0;
        constraints.weighty = 1.0;
        
        // Lay out all the boolean mode checkboxes
        for (int i = 0; i < booleanModes.length(); i++) {
            final String mode = booleanModes.substring(i, i + 1);
            final char modeChar = mode.toCharArray()[0];
            final boolean state = ourBooleanModes.split(" ")[0]
                    .contains(mode.subSequence(0, 1));
            String text = "Mode " + mode;
            
            if (channel.getConfigManager().getOptionBool("server", "friendlymodes")
                    && channel.getConfigManager().hasOption("server", "mode" + mode)) {
                text = channel.getConfigManager().getOption("server", "mode" + mode);
            }
            
            final JCheckBox checkBox = new JCheckBox(text, state);
            checkBox.setBorder(BorderFactory.createEmptyBorder(SMALL_BORDER,
                    0, 0, SMALL_BORDER));
            add(checkBox, constraints);
            
            constraints.gridx++;
            if (constraints.gridx == 2) {
                constraints.gridy++;
                constraints.gridx = 0;
            }
            
            modeCheckBoxes.put(mode, checkBox);
            if (channel.getConfigManager().getOptionBool("server", "enablemode" + modeChar)) {
                checkBox.setEnabled(true);
            } else if (!channel.getServer().getParser().isUserSettable(modeChar)) {
                checkBox.setEnabled(false);
            }
        }
        
        // Lay out all the parameter-requiring modes
        modeInputs = new Hashtable<String, ParamModePanel>();
        
        constraints.gridwidth = 2;
        
        if (constraints.gridx != 0) {
            constraints.gridy++;
            constraints.gridx = 0;
        }
        
        for (int i = 0; i < paramModes.length(); i++) {
            final String mode = paramModes.substring(i, i + 1);
            final String value = channel.getChannelInfo()
                    .getModeParam(mode.charAt(0));
            final boolean state = ourBooleanModes.split(" ")[0]
                    .contains(mode.subSequence(0, 1));
            
            final ParamModePanel panel = new ParamModePanel(mode, state, value,
                    channel.getConfigManager());
            panel.setBorder(BorderFactory.createEmptyBorder(SMALL_BORDER,
                    0, 0, 0));
            add(panel, constraints);
            
            modeInputs.put(mode, panel);
            
            constraints.gridy++;
        }
        
    }
    
        /**
     * Processes the channel settings dialog and constructs a mode string for
     * changed modes, then sends this to the server.
     */
    public void setChangedBooleanModes() {
        boolean changed = false;
        final IRCParser parser = channel.getServer().getParser();
        final String booleanModes = parser.getBoolChanModes();
        final String ourBooleanModes = channel.getChannelInfo().getModeStr();
        final String paramModes = parser.getSetOnlyChanModes() + parser.getSetUnsetChanModes();

        for (int i = 0; i < booleanModes.length(); i++) {
            final String mode = booleanModes.substring(i, i + 1);
            final boolean state = ourBooleanModes.split(" ")[0].contains(mode.subSequence(0, 1));

            if (modeCheckBoxes.get(mode) != null && state != modeCheckBoxes.get(mode).isSelected()) {
                changed = true;
                channel.getChannelInfo().alterMode(modeCheckBoxes.get(mode).isSelected(), mode.toCharArray()[0], "");
            }
        }

        for (int i = 0; i < paramModes.length(); i++) {
            final String mode = paramModes.substring(i, i + 1);
            final String value = channel.getChannelInfo().getModeParam(mode.charAt(0));
            final boolean state = ourBooleanModes.split(" ")[0].contains(mode.subSequence(0, 1));
            final ParamModePanel paramModePanel = modeInputs.get(mode);

            if (state != paramModePanel.getState() || !value.equals(paramModePanel.getValue())) {
                changed = true;
                if (paramModePanel.getValue().contains(" ")) {
                    channel.getChannelInfo().alterMode(paramModePanel.getState(), mode.toCharArray()[0], paramModePanel.getValue().substring(0, paramModePanel.getValue().indexOf(" ")));
                } else {
                    channel.getChannelInfo().alterMode(paramModePanel.getState(), mode.toCharArray()[0], paramModePanel.getValue());
                }
            }
        }
        if (changed) {
            channel.getChannelInfo().sendModes();
        }
    }
    
}
