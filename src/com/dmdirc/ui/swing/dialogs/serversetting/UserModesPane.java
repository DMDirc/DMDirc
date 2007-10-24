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

package com.dmdirc.ui.swing.dialogs.serversetting;

import com.dmdirc.Server;
import com.dmdirc.parser.IRCParser;
import static com.dmdirc.ui.swing.UIUtilities.SMALL_BORDER;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.Hashtable;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JCheckBox;
import javax.swing.JPanel;

/** User mode panel. */
public final class UserModesPane extends JPanel {

    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 1;
    /** Parent server. */
    private final Server server;
    /** The checkboxes used for boolean modes. */
    private Map<String, JCheckBox> modeCheckBoxes;

    /**
     * Creates a new instance of UserModesPane.
     *
     * @param server Parent server
     */
    public UserModesPane(final Server server) {
        super();

        this.server = server;
        
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

        final IRCParser parser = server.getParser();

        final String booleanModes = ""; //parser.getUserModes();
        final String ourBooleanModes = parser.getMyself().getUserModeStr();

        setLayout(new GridBagLayout());
        setBorder(BorderFactory.createEmptyBorder(SMALL_BORDER, SMALL_BORDER,
                SMALL_BORDER, SMALL_BORDER));

        modeCheckBoxes =
                new Hashtable<String, JCheckBox>();

        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.anchor = GridBagConstraints.NORTHWEST;
        constraints.weightx = 1.0;
        constraints.weighty = 0.0;

        // Lay out all the boolean mode checkboxes
        for (int i = 0; i < booleanModes.length();
                i++) {
            final String mode = booleanModes.substring(i, i + 1);
            final char modeChar = mode.toCharArray()[0];
            final boolean state =
                    ourBooleanModes.split(" ")[0].contains(mode.subSequence(0, 1));
            String text;
            String tooltip;

            if (server.getConfigManager().
                    getOptionBool("server", "friendlymodes", false) &&
                    server.getConfigManager().hasOption("server", "mode" + mode)) {
                text =  server.getConfigManager().
                        getOption("server", "mode" + mode);
            } else {
                text = "Mode " + mode;
            }

            if (server.getConfigManager().hasOption("server", "mode" + mode)) {
                tooltip =
                        "Mode " + mode + ": " +
                        server.getConfigManager().
                        getOption("server", "mode" + mode);
            } else {
                tooltip = "Mode " + mode + ": Unknown";
            }

            final JCheckBox checkBox = new JCheckBox(text, state);
            checkBox.setToolTipText(tooltip);
            checkBox.setBorder(BorderFactory.createEmptyBorder(SMALL_BORDER, 0,
                    0, SMALL_BORDER));
            add(checkBox, constraints);

            constraints.gridx++;
            if (constraints.gridx == 2) {
                constraints.gridy++;
                constraints.gridx = 0;
            }

            modeCheckBoxes.put(mode, checkBox);
        }
        
        constraints.weighty = 1.0;
        constraints.fill = GridBagConstraints.VERTICAL;
        add(Box.createVerticalGlue(), constraints);
    }

    /**
     * Processes the channel settings dialog and constructs a mode string for
     * changed modes, then sends this to the server.
     */
    public void setChangedBooleanModes() {
        /*boolean changed = false;
        final IRCParser parser = channel.getServer().getParser();
        final String booleanModes = parser.getBoolChanModes();
        final String ourBooleanModes = channel.getChannelInfo().getModeStr();

        for (int i = 0; i < booleanModes.length();
                i++) {
            final String mode = booleanModes.substring(i, i + 1);
            final boolean state =
                    ourBooleanModes.split(" ")[0].contains(mode.subSequence(0, 1));

            if (modeCheckBoxes.get(mode) != null &&
                    state != modeCheckBoxes.get(mode).isSelected()) {
                changed = true;
                //change the mode
            }
        }
        if (changed) {
            //send modes
        }*/
    }
}