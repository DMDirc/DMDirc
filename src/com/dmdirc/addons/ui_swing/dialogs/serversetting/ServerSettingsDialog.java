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
import com.dmdirc.config.Identity;
import com.dmdirc.addons.ui_swing.UIUtilities;
import com.dmdirc.addons.ui_swing.dialogs.StandardDialog;
import com.dmdirc.addons.ui_swing.components.expandingsettings.SettingsPanel;
import com.dmdirc.addons.ui_swing.components.expandingsettings.SettingsPanel.OptionType;

import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;

import net.miginfocom.swing.MigLayout;

/**
 * Allows the user to modify server settings and the ignore list.
 */
public final class ServerSettingsDialog extends StandardDialog implements ActionListener {

    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 2;
    /** Server settings dialogs, semi singleton use. */
    private static volatile ServerSettingsDialog me;
    /** Parent server. */
    private final Server server;
    /** User modes panel. */
    private UserModesPane modesPanel;
    /** Ignore list panel. */
    private IgnoreListPanel ignoreList;
    /** Perform panel. */
    private PerformPanel performPanel;
    /** Settings panel. */
    private SettingsPanel settingsPanel;    
    /** The tabbed pane. */
    private JTabbedPane tabbedPane;
    /** Parent window. */
    private Window parentWindow;

    /**
     * Creates a new instance of ServerSettingsDialog.
     *
     * @param server The server object that we're editing settings for
     * @param parentWindow Parent window
     */
    private ServerSettingsDialog(final Server server, final Window parentWindow) {
        super(parentWindow, ModalityType.MODELESS);

        this.server = server;
        this.parentWindow = parentWindow;

        setTitle("Server settings");
        setResizable(false);

        initComponents();
        initListeners();
    }

    /**
     * Creates the dialog if one doesn't exist, and displays it.
     *
     * @param server The server object that we're editing settings for
     * @param parentWindow Parent window
     */
    public static void showServerSettingsDialog(final Server server, 
            final Window parentWindow) {
        me = getServerSettingsDialog(server, parentWindow);
        
        me.pack();
        me.setLocationRelativeTo(parentWindow);
        me.setVisible(true);
        me.requestFocusInWindow();
    }

    /**
     * Returns the current instance of the ServerSettingsDialog.
     *
     * @param server The server object that we're editing settings for
     * @param parentWindow Parent window
     * 
     * @return The current ServerSettingsDialog instance
     */
    public static ServerSettingsDialog getServerSettingsDialog(
            final Server server, final Window parentWindow) {
        synchronized (ServerSettingsDialog.class) {
            if (me == null) {
                me = new ServerSettingsDialog(server, parentWindow);
            }
        }

        return me;
    }

    /** Initialises the main UI components. */
    private void initComponents() {
        orderButtons(new JButton(), new JButton());

        tabbedPane = new JTabbedPane();

        modesPanel = new UserModesPane(server);

        ignoreList =
                new IgnoreListPanel(server, parentWindow);

        performPanel =
                new PerformPanel(server);

        settingsPanel =
                new SettingsPanel(server.getNetworkIdentity(),
                "These settings are specific to this " +
                "network, any settings specified here will overwrite global " +
                "settings");

        if (settingsPanel != null) {
            addSettings();
        }

        final JScrollPane userModesSP = new JScrollPane(modesPanel);
        userModesSP.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        userModesSP.setOpaque(UIUtilities.getTabbedPaneOpaque());
        userModesSP.getViewport().setOpaque(UIUtilities.getTabbedPaneOpaque());
        userModesSP.setBorder(null);

        tabbedPane.add("User modes", userModesSP);
        tabbedPane.add("Ignore list", ignoreList);
        tabbedPane.add("Perform", performPanel);
        if (settingsPanel != null) {
            tabbedPane.add("Settings", settingsPanel);
        }

        setLayout(new MigLayout("fill, wrap 1, hmax 80sp"));

        add(tabbedPane, "grow");
        add(getLeftButton(), "split 2, right");
        add(getRightButton(), "right");

        tabbedPane.setSelectedIndex(server.getConfigManager().
                getOptionInt("dialogstate", "serversettingsdialog"));
    }

    /** Adds the settings to the panel. */
    private void addSettings() {
        settingsPanel.addOption("channel.splitusermodes", "Split user modes",
                OptionType.CHECKBOX);
        settingsPanel.addOption("channel.sendwho", "Send WHO",
                OptionType.CHECKBOX);
        settingsPanel.addOption("channel.showmodeprefix", "Show mode prefix",
                OptionType.CHECKBOX);

        settingsPanel.addOption("general.cyclemessage", "Cycle message",
                OptionType.TEXTFIELD);
        settingsPanel.addOption("general.kickmessage", "Kick message",
                OptionType.TEXTFIELD);
        settingsPanel.addOption("general.partmessage", "Part message",
                OptionType.TEXTFIELD);

        settingsPanel.addOption("ui.backgroundcolour", "Background colour",
                OptionType.COLOUR);
        settingsPanel.addOption("ui.foregroundcolour", "Foreground colour",
                OptionType.COLOUR);
        settingsPanel.addOption("ui.frameBufferSize", "Textpane buffer limit",
                OptionType.SPINNER);
        
        settingsPanel.addOption("ui.inputBufferSize", "Input buffer size",
                OptionType.SPINNER);
        settingsPanel.addOption("ui.textPaneFontName", "Textpane font name",
                OptionType.TEXTFIELD);
        //TODO issue 2251
        //settingsPanel.addOption("ui.textPaneFontSize", "Textpane font size",
        //        OptionType.SPINNER);
        
        settingsPanel.addOption("ui.inputbackgroundcolour",
                "Input field background colour",
                OptionType.COLOUR);
        settingsPanel.addOption("ui.inputforegroundcolour",
                "Input field foreground colour",
                OptionType.COLOUR);
        settingsPanel.addOption("ui.nicklistbackgroundcolour",
                "Nicklist background colour",
                OptionType.COLOUR);
        settingsPanel.addOption("ui.nicklistforegroundcolour",
                "Nicklist foreground colour",
                OptionType.COLOUR);
        settingsPanel.addOption("ui.shownickcoloursinnicklist",
                "Show coloured nicks in nicklist",
                OptionType.CHECKBOX);
        settingsPanel.addOption("ui.shownickcoloursintext",
                "Show coloured nicks in textpane",
                OptionType.CHECKBOX);

        settingsPanel.addOption("general.closechannelsonquit",
                "Close channels on quit",
                OptionType.CHECKBOX);
        settingsPanel.addOption("general.closechannelsondisconnect",
                "Close channels on disconnect",
                OptionType.CHECKBOX);
        settingsPanel.addOption("general.closequeriesonquit",
                "Close queries on quit",
                OptionType.CHECKBOX);
        settingsPanel.addOption("general.closequeriesondisconnect",
                "Close queries on disconnect",
                OptionType.CHECKBOX);
        settingsPanel.addOption("general.quitmessage", "Quit message",
                OptionType.TEXTFIELD);
        settingsPanel.addOption("general.reconnectmessage", "Reconnect message",
                OptionType.TEXTFIELD);
        settingsPanel.addOption("general.rejoinchannels",
                "Rejoin channels on reconnect",
                OptionType.CHECKBOX);

        settingsPanel.addOption("general.friendlymodes", "Show friendly modes",
                OptionType.CHECKBOX);
        settingsPanel.addOption("general.pingtimeout", "Ping timeout",
                OptionType.SPINNER);
    }

    /** Initialises listeners for this dialog. */
    private void initListeners() {
        getOkButton().addActionListener(this);
        getCancelButton().addActionListener(this);
    }

    /** Saves the settings from this dialog. */
    public void saveSettings() {
        modesPanel.save();
        settingsPanel.save();
        performPanel.savePerforms();
        ignoreList.saveList();

        final Identity identity = server.getNetworkIdentity();
        identity.setOption("dialogstate", "serversettingsdialog",
                String.valueOf(tabbedPane.getSelectedIndex()));
    }

    /**
     * {@inheritDoc}
     * 
     * @param e Action event
     */
    @Override
    public void actionPerformed(final ActionEvent e) {
        if (e.getSource() == getOkButton()) {
            saveSettings();
            dispose();
        } else if (e.getSource() == getCancelButton()) {
            dispose();
        }
    }
    
    /** {@inheritDoc} */
    @Override
    public void dispose() {
        if (me == null) {
            return;
        }
        synchronized (me) {
            super.dispose();
            me = null;
        }
    }
}
