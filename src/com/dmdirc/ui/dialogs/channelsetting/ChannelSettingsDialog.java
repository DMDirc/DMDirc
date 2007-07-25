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
import com.dmdirc.Main;
import com.dmdirc.config.Identity;
import com.dmdirc.config.IdentityManager;
import com.dmdirc.ui.components.StandardDialog;
import com.dmdirc.ui.components.expandingsettings.SettingsPanel;
import com.dmdirc.ui.components.expandingsettings.SettingsPanel.OptionType;
import static com.dmdirc.ui.UIUtilities.SMALL_BORDER;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.WindowConstants;

/**
 * Allows the user to modify channel settings (modes, topics, etc).
 */
public final class ChannelSettingsDialog extends StandardDialog implements ActionListener {

    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 7;

    /** Channel settings dialogs, semi singleton use. */
    private static Map<Channel, ChannelSettingsDialog> dialogs;

    /** The channel object that this dialog belongs to. */
    private final Channel channel;

    /** Tabbed pane. */
    private JTabbedPane tabbedPane;

    /** Client settings panel. */
    private SettingsPanel channelSettingsPane;

    /** List modes panel. */
    private ChannelModesPane channelModesPane;

    /** List modes panel. */
    private TopicModesPane topicModesPane;

    /** List modes panel. */
    private ChannelListModesPane channelListModesPane;

    /** Channel identity file. */
    private final Identity identity;

    /**
     * Creates a new instance of ChannelSettingsDialog.
     *
     * @param newChannel The channel object that we're editing settings for
     */
    private ChannelSettingsDialog(final Channel newChannel) {
        super(Main.getUI().getMainWindow(), false);

        channel = newChannel;
        identity =
                IdentityManager.getChannelConfig(channel.getServer().
                getNetwork(), channel.getChannelInfo().getName());

        initComponents();
        initListeners();

        pack();
        setLocationRelativeTo(Main.getUI().getMainWindow());
    }

    /**
     * Returns an instance of the CSD for the specified channel.
     *
     * @param channel Channel to get a CSD for
     *
     * @return CSD instance for the specified channel
     */
    public static synchronized ChannelSettingsDialog getChannelSettingDialog(final Channel channel) {
        if (dialogs == null) {
            dialogs =
                    new HashMap<Channel, ChannelSettingsDialog>();
        }

        if (dialogs.containsKey(channel)) {
            dialogs.get(channel).update();
        } else {
            dialogs.put(channel, new ChannelSettingsDialog(channel));
        }

        return dialogs.get(channel);
    }

    /** Initialises the main UI components. */
    private void initComponents() {
        final GridBagConstraints constraints = new GridBagConstraints();
        tabbedPane = new JTabbedPane();

        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        getContentPane().setLayout(new GridBagLayout());
        setTitle("Channel settings for " + channel);
        setResizable(false);

        final JButton button1 = new JButton();
        final JButton button2 = new JButton();

        orderButtons(button1, button2);

        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.gridwidth = 3;
        constraints.weightx = 1.0;
        constraints.weighty = 1.0;
        constraints.fill = GridBagConstraints.BOTH;
        constraints.insets =
                new Insets(SMALL_BORDER, SMALL_BORDER, SMALL_BORDER,
                SMALL_BORDER);
        getContentPane().add(tabbedPane, constraints);

        constraints.weighty = 0.0;
        constraints.gridx = 0;
        constraints.gridy = 1;
        constraints.gridwidth = 1;
        getContentPane().add(Box.createHorizontalGlue(), constraints);

        constraints.weightx = 0.0;
        constraints.insets.set(0, SMALL_BORDER, SMALL_BORDER, SMALL_BORDER);
        constraints.gridx = 1;
        constraints.gridy = 1;
        constraints.anchor = GridBagConstraints.EAST;
        constraints.fill = GridBagConstraints.NONE;
        getContentPane().add(getLeftButton(), constraints);

        constraints.gridx = 2;
        constraints.insets.set(0, 0, SMALL_BORDER, SMALL_BORDER);
        getContentPane().add(getRightButton(), constraints);

        initIrcTab();

        initListModesTab();

        initSettingsTab();

        tabbedPane.setSelectedIndex(channel.getConfigManager().
                getOptionInt("dialogstate", "channelsettingsdialog", 0));
    }

    /** Updates the dialogs content. */
    private void update() {
        channelListModesPane.update();
        channelModesPane.update();
        channelSettingsPane.update();
        topicModesPane.update();

        tabbedPane.setSelectedIndex(channel.getConfigManager().
                getOptionInt("dialogstate", "channelsettingsdialog", 0));
    }

    /** Initialises the IRC Settings tab. */
    private void initIrcTab() {
        final GridBagConstraints constraints = new GridBagConstraints();
        final JPanel settingsPanel = new JPanel(new GridBagLayout());

        tabbedPane.addTab("IRC Settings", settingsPanel);

        settingsPanel.setBorder(BorderFactory.createEmptyBorder(SMALL_BORDER,
                SMALL_BORDER, SMALL_BORDER, SMALL_BORDER));

        channelModesPane = new ChannelModesPane(channel);
        topicModesPane = new TopicModesPane(channel, this);

        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.fill = GridBagConstraints.BOTH;
        constraints.weightx = 1.0;
        constraints.weighty = 1.0;
        settingsPanel.add(channelModesPane, constraints);
        constraints.gridy = 1;
        settingsPanel.add(topicModesPane, constraints);
    }

    /** Initialises the IRC Settings tab. */
    private void initListModesTab() {
        channelListModesPane = new ChannelListModesPane(channel);
        tabbedPane.addTab("List Modes", channelListModesPane);
    }

    /** Initialises the channel Settings (identities) tab. */
    private void initSettingsTab() {

        initSettingsPanel();

        tabbedPane.addTab("Client Settings", channelSettingsPane);
    }

    /** Initialises the channel settings. */
    private void initSettingsPanel() {
        channelSettingsPane =
                new SettingsPanel(identity,
                "These settings are specific to this channel on this network, " 
                + "any settings specified here will overwrite global settings");

        channelSettingsPane.addOption("channel.splitusermodes",
                "Split user modes", OptionType.CHECKBOX);
        channelSettingsPane.addOption("channel.sendwho", "Send channel WHOs",
                OptionType.CHECKBOX);
        channelSettingsPane.addOption("channel.showmodeprefix", "Show mode prefixes",
                OptionType.CHECKBOX);
        channelSettingsPane.addOption("ui.shownickcoloursinnicklist", 
                "Show colours in nicklist", OptionType.CHECKBOX);
        channelSettingsPane.addOption("ui.shownickcoloursintext", 
                "Show colours in textpane", OptionType.CHECKBOX);
        channelSettingsPane.addOption("general.cyclemessage", "Cycle message",
                OptionType.TEXTFIELD);
        channelSettingsPane.addOption("general.kickmessage", "Kick message",
                OptionType.TEXTFIELD);
        channelSettingsPane.addOption("general.partmessage", "Part message",
                OptionType.TEXTFIELD);
        channelSettingsPane.addOption("ui.backgroundcolour", "Background colour",
                OptionType.COLOUR);
        channelSettingsPane.addOption("ui.foregroundcolour", "Foreground colour",
                OptionType.COLOUR);
        channelSettingsPane.addOption("ui.frameBufferSize", "Frame buffer size",
                OptionType.SPINNER);
        channelSettingsPane.addOption("ui.inputbuffersize", "Input buffer size",
                OptionType.SPINNER);
        channelSettingsPane.addOption("ui.inputbackgroundcolour", 
                "Inputfield background colour", OptionType.COLOUR);
        channelSettingsPane.addOption("ui.inputforegroundcolour", 
                "Inputfield foreground colour", OptionType.COLOUR);
        channelSettingsPane.addOption("ui.nicklistbackgroundcolour", 
                "Nicklist background colour", OptionType.COLOUR);
        channelSettingsPane.addOption("ui.nicklistforegroundcolour", 
                "Nicklist foreground colour", OptionType.COLOUR);
    }

    /** Initialises listeners for this dialog. */
    private void initListeners() {
        getOkButton().addActionListener(this);
        getCancelButton().addActionListener(this);
    }

    /**
     * Called whenever the user clicks on one of the two buttons.
     *
     * @param actionEvent Event generated by this action
     */
    public void actionPerformed(final ActionEvent actionEvent) {
        if (getOkButton().equals(actionEvent.getSource())) {
            save();
        } else if (getCancelButton().equals(actionEvent.getSource())) {
            setVisible(false);
        }
    }

    /** Saves the settings. */
    protected void save() {
        channelModesPane.setChangedBooleanModes();
        topicModesPane.setChangedTopic();
        channelSettingsPane.save();
        channelListModesPane.save();

        identity.setOption("dialogstate", "channelsettingsdialog",
                String.valueOf(tabbedPane.getSelectedIndex()));

        setVisible(false);
    }
}
