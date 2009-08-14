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

package com.dmdirc.addons.ui_swing.components.frames;

import com.dmdirc.addons.ui_swing.components.NickList;
import com.dmdirc.Channel;
import com.dmdirc.ServerState;
import com.dmdirc.actions.ActionManager;
import com.dmdirc.actions.CoreActionType;
import com.dmdirc.actions.interfaces.ActionType;
import com.dmdirc.addons.ui_swing.SwingController;
import com.dmdirc.addons.ui_swing.UIUtilities;
import com.dmdirc.addons.ui_swing.components.SplitPane;
import com.dmdirc.addons.ui_swing.components.SwingInputHandler;
import com.dmdirc.addons.ui_swing.dialogs.channelsetting.ChannelSettingsDialog;
import com.dmdirc.commandparser.PopupType;
import com.dmdirc.commandparser.parsers.ChannelCommandParser;
import com.dmdirc.commandparser.parsers.CommandParser;
import com.dmdirc.config.Identity;
import com.dmdirc.config.IdentityManager;
import com.dmdirc.parser.interfaces.ChannelClientInfo;
import com.dmdirc.ui.interfaces.ChannelWindow;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JSplitPane;

import net.miginfocom.swing.MigLayout;

/**
 * The channel frame is the GUI component that represents a channel to the user.
 */
public final class ChannelFrame extends InputTextFrame implements ActionListener,
        ChannelWindow, com.dmdirc.interfaces.ActionListener {

    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 10;
    /** This channel's command parser. */
    private final ChannelCommandParser commandParser;
    /** split pane. */
    private SplitPane splitPane;
    /** popup menu item. */
    private JMenuItem settingsMI;
    /** The channel object that owns this frame. */
    private final Channel parentChannel;
    /** Identity. */
    private Identity identity;
    /** Nicklist. */
    private NickList nicklist;

    /**
     * Creates a new instance of ChannelFrame. Sets up callbacks and handlers,
     * and default options for the form.
     * 
     * @param owner The Channel object that owns this frame
     * @param controller Swing controller
     */
    public ChannelFrame(final Channel owner, final SwingController controller) {
        super(owner, controller);

        parentChannel = owner;

        initComponents();

        IdentityManager.getGlobalConfig().addChangeListener("ui",
                "channelSplitPanePosition", this);
        ActionManager.addListener(this, CoreActionType.CLIENT_CLOSING);

        commandParser =
                new ChannelCommandParser(((Channel) getContainer()).getServer(),
                (Channel) getContainer());

        setInputHandler(new SwingInputHandler(getInputField(), commandParser,
                this));

        identity = IdentityManager.getChannelConfig(getChannel().getServer().
                getNetwork(), getChannel().getChannelInfo().getName());
    }

    /**
     * Retrieves the command Parser for this command window.
     * @return This window's command Parser
     */
    @Override
    public CommandParser getCommandParser() {
        return commandParser;
    }

    /** {@inheritDoc} */
    @Override
    public void updateNames(final Collection<ChannelClientInfo> clients) {
        nicklist.updateNames(clients);
    }

    /** {@inheritDoc} */
    @Override
    public void updateNames() {
        nicklist.updateNames();
    }

    /** {@inheritDoc} */
    @Override
    public void addName(final ChannelClientInfo client) {
        nicklist.addName(client);
    }

    /** {@inheritDoc} */
    @Override
    public void removeName(final ChannelClientInfo client) {
        nicklist.removeName(client);
    }

    /**
     * Retrieves this channel frame's nicklist component.
     * @return This channel's nicklist
     */
    public NickList getNickList() {
        return nicklist;
    }

    /** {@inheritDoc} */
    @Override
    public Channel getChannel() {
        return parentChannel;
    }

    /**
     * Initialises the compoents in this frame.
     */
    private void initComponents() {
        nicklist = new NickList(this, getConfigManager());
        settingsMI = new JMenuItem("Settings");
        settingsMI.addActionListener(this);

        splitPane = new SplitPane(
                SplitPane.Orientation.HORIZONTAL, false);

        getContentPane().setLayout(new MigLayout(
                "fill, ins 0, hidemode 3, wrap 1"));

        getContentPane().add(splitPane, "grow, push");
        getContentPane().add(getSearchBar(), "growx, pushx");
        getContentPane().add(inputPanel, "growx, pushx");

        splitPane.setLeftComponent(getTextPane());
        splitPane.setRightComponent(nicklist);
        splitPane.setResizeWeight(1);
        splitPane.setDividerLocation(-1);

        pack();
    }

    /**
     * {@inheritDoc}.
     * 
     * @param actionEvent Action event
     */
    @Override
    public void actionPerformed(final ActionEvent actionEvent) {
        if (actionEvent.getSource() == settingsMI) {
            ChannelSettingsDialog.showChannelSettingsDialog(
                    (Channel) getContainer(), getController().getMainFrame());
        }
    }

    /**
     * Returns the splitpane.
     * @return nicklist JSplitPane
     */
    public JSplitPane getSplitPane() {
        return splitPane;
    }

    /** {@inheritDoc} */
    @Override
    public void configChanged(final String domain, final String key) {
        super.configChanged(domain, key);

        if ("channelSplitPanePosition".equals(key)) {
            final int splitPanePosition = getConfigManager().getOptionInt("ui",
                    "channelSplitPanePosition");
            UIUtilities.invokeLater(new Runnable() {

                /** {@inheritDoc} */
                @Override
                public void run() {
                    nicklist.setPreferredSize(
                            new Dimension(splitPanePosition, 0));
                    splitPane.setDividerLocation(splitPane.getWidth() - 
                            splitPane.getDividerSize() - splitPanePosition);
                }
            });
        }
    }

    /** {@inheritDoc} */
    @Override
    public void processEvent(final ActionType type, final StringBuffer format,
            final Object... arguments) {
        saveSplitPanePosition();
    }

    /** {@inheritDoc} */
    @Override
    public void close() {
        saveSplitPanePosition();
        super.close();
    }

    private void saveSplitPanePosition() {
        identity.setOption("ui", "channelSplitPanePosition", nicklist.getWidth());
    }

    /** {@inheritDoc} */
    @Override
    public PopupType getNicknamePopupType() {
        return PopupType.CHAN_NICK;
    }

    /** {@inheritDoc} */
    @Override
    public PopupType getChannelPopupType() {
        return PopupType.CHAN_NORMAL;
    }

    /** {@inheritDoc} */
    @Override
    public PopupType getHyperlinkPopupType() {
        return PopupType.CHAN_HYPERLINK;
    }

    /** {@inheritDoc} */
    @Override
    public PopupType getNormalPopupType() {
        return PopupType.CHAN_NORMAL;
    }

    /** {@inheritDoc} */
    @Override
    public void addCustomPopupItems(final JPopupMenu popupMenu) {
        if (getContainer().getServer().getState().equals(ServerState.CONNECTED)) {
            settingsMI.setEnabled(true);
        } else {
            settingsMI.setEnabled(false);
        }

        if (popupMenu.getComponentCount() > 0) {
            popupMenu.addSeparator();
        }

        popupMenu.add(settingsMI);
    }

    /** {@inheritDoc} */
    @Override
    public void redrawNicklist() {
        getNickList().repaint();
    }
}
