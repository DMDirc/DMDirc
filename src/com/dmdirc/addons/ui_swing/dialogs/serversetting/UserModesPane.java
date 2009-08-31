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

package com.dmdirc.addons.ui_swing.dialogs.serversetting;

import com.dmdirc.Server;
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
    /** The checkboxes used for user modes. */
    private Map<String, JCheckBox> modeCheckBoxes;

    /**
     * Creates a new instance of UserModesPane.
     *
     * @param server Parent server
     */
    public UserModesPane(final Server server) {
        super();

        this.server = server;

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
        final Parser parser = server.getParser();

        final String userModes = parser.getUserModes();
        final String ourUserModes = parser.getLocalClient().getModes();

        modeCheckBoxes =
                new Hashtable<String, JCheckBox>();

        final boolean opaque = UIUtilities.getTabbedPaneOpaque();

        // Lay out all the boolean mode checkboxes
        for (int i = 0; i < userModes.length();
                i++) {
            final String mode = userModes.substring(i, i + 1);
            final boolean state =
                    ourUserModes.split(" ")[0].contains(mode.subSequence(0, 1));
            String text;
            String tooltip;

            if (server.getConfigManager().getOptionBool("server",
                    "friendlymodes") &&
                    server.getConfigManager().hasOptionString("server",
                    "umode" + mode)) {
                text = server.getConfigManager().
                        getOption("server", "umode" + mode);
            } else {
                text = "Mode " + mode;
            }

            if (server.getConfigManager().hasOptionString("server", "umode" +
                    mode)) {
                tooltip = "Mode " + mode + ": " + server.getConfigManager().
                        getOption("server", "umode" + mode);
            } else {
                tooltip = "Mode " + mode + ": Unknown";
            }

            final JCheckBox checkBox = new JCheckBox(text, state);
            checkBox.setMargin(new Insets(0, 0, 0, 0));
            checkBox.setToolTipText(tooltip);
            checkBox.setOpaque(opaque);

            modeCheckBoxes.put(mode, checkBox);
        }
    }

    /** Lays out the components. */
    private void layoutComponents() {
        final JPanel userModes =
                new JPanel(new MigLayout("wrap 2, fillx"));
        for (JCheckBox checkBox : modeCheckBoxes.values()) {
            userModes.add(checkBox);
        }

        userModes.setBorder(BorderFactory.createTitledBorder(UIManager.getBorder(
                "TitledBorder.border"), "User modes"));
        userModes.setOpaque(UIUtilities.getTabbedPaneOpaque());

        setLayout(new MigLayout("flowy, fillx", "fill", ""));
        add(userModes);
    }

    /**
     * Sends changed modes to the server.
     */
    public void save() {
        if (server == null || server.getParser() == null) {
            return;
        }
        boolean changed = false;
        final Parser parser = server.getParser();
        final String userModes = parser.getUserModes();
        final String ourUserModes = parser.getLocalClient().getModes();

        for (int i = 0; i < userModes.length();
                i++) {
            final String mode = userModes.substring(i, i + 1);
            final boolean state =
                    ourUserModes.split(" ")[0].contains(mode.subSequence(0, 1));

            if (modeCheckBoxes.get(mode) != null &&
                    state != modeCheckBoxes.get(mode).isSelected()) {
                changed = true;
                server.getParser().getLocalClient().
                        alterMode(modeCheckBoxes.get(mode).isSelected(),
                        mode.toCharArray()[0]);
            }
        }
        if (changed) {
            server.getParser().getLocalClient().flushModes();
        }
    }
}
