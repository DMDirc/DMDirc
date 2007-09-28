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

package com.dmdirc.ui.swing;


import com.dmdirc.Server;
import com.dmdirc.commandparser.parsers.CommandParser;
import com.dmdirc.commandparser.parsers.ServerCommandParser;
import com.dmdirc.config.IdentityManager;
import com.dmdirc.ui.input.InputHandler;
import com.dmdirc.ui.interfaces.ServerWindow;
import com.dmdirc.ui.swing.components.InputFrame;
import com.dmdirc.ui.swing.dialogs.serversetting.ServerSettingsDialog;
import static com.dmdirc.ui.swing.UIUtilities.SMALL_BORDER;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JMenuItem;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

/**
 * The ServerFrame is the MDI window that shows server messages to the user.
 */
public final class ServerFrame extends InputFrame implements ServerWindow,
        ActionListener, PopupMenuListener {
    
    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 9;
    
    /** This channel's command parser. */
    private final ServerCommandParser commandParser;
    
    /** popup menu item. */
    private JMenuItem settingsMI;
    
    /**
     * Creates a new ServerFrame.
     * @param owner Parent Frame container
     */
    public ServerFrame(final Server owner) {
        super(owner);
        
        initComponents();
        
        commandParser = new ServerCommandParser((Server) getContainer());
        
        setInputHandler(new InputHandler(getInputField(), commandParser, this));
    }
    
    /**
     * Retrieves the command Parser for this command window.
     * @return This window's command Parser
     */
    public CommandParser getCommandParser() {
        return commandParser;
    }
    
    /**
     * Sets the away status for this and all associated frames.
     *
     * @param newAwayState away state
     */
    @Override
    public void setAwayIndicator(final boolean newAwayState) {
        if (IdentityManager.getGlobalConfig().getOptionBool("ui", "awayindicator")) {
            if (newAwayState) {
                inputPanel.add(awayLabel, BorderLayout.LINE_START);
                awayLabel.setVisible(true);
            } else {
                awayLabel.setVisible(false);
            }
            
            if (getContainer().getServer().getRaw() != null) {
                getContainer().getServer().getRaw().getFrame().setAwayIndicator(newAwayState);
            }
            
            for (String channel : getContainer().getServer().getChannels()) {
                getContainer().getServer().getChannel(channel).getFrame().setAwayIndicator(newAwayState);
            }
            
            for (String query : getContainer().getServer().getQueries()) {
                getContainer().getServer().getQuery(query).getFrame().setAwayIndicator(newAwayState);
            }
        }
    }
    
    /**
     * Initialises components in this frame.
     */
    private void initComponents() {
        settingsMI = new JMenuItem("Settings");
        settingsMI.addActionListener(this);
        getPopup().addSeparator();
        getPopup().add(settingsMI);
        getPopup().addPopupMenuListener(this);
        
        final GridBagConstraints constraints = new GridBagConstraints();
        
        setTitle("Server Frame");
        
        getContentPane().setLayout(new GridBagLayout());
        constraints.weightx = 1.0;
        constraints.weighty = 1.0;
        constraints.fill = GridBagConstraints.BOTH;
        constraints.insets = new Insets(0, 0, SMALL_BORDER, 0);
        getContentPane().add(getTextPane(), constraints);
        
        constraints.weighty = 0.0;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.gridy = 1;
        constraints.insets = new Insets(0, 0, 0, 0);
        getContentPane().add(getSearchBar(), constraints);
        
        constraints.gridy = 2;
        getContentPane().add(inputPanel, constraints);
        
        pack();
    }
    
    /** {@inheritDoc}. */
    public void actionPerformed(final ActionEvent actionEvent) {
        super.actionPerformed(actionEvent);
        if (actionEvent.getSource() == settingsMI) {
            new ServerSettingsDialog(getContainer().getServer()).setVisible(true);
        }
    }

    /** {@inheritDoc}. */
    public void popupMenuWillBecomeVisible(final PopupMenuEvent e) {
        if (getContainer().getServer().getState().equals(Server.STATE.CONNECTED)) {
            settingsMI.setEnabled(true);
        } else {
            settingsMI.setEnabled(false);
        }
    }

    /** {@inheritDoc}. */
    public void popupMenuWillBecomeInvisible(final PopupMenuEvent e) {
        //Ignore
    }

    /** {@inheritDoc}. */
    public void popupMenuCanceled(final PopupMenuEvent e) {
        //Ignore
    }
    
}
