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

package com.dmdirc.ui.swing.dialogs;

import com.dmdirc.Main;
import com.dmdirc.config.Identity;
import com.dmdirc.config.IdentityManager;
import com.dmdirc.ui.swing.MainFrame;
import com.dmdirc.ui.swing.components.StandardDialog;
import static com.dmdirc.ui.swing.UIUtilities.LARGE_BORDER;
import static com.dmdirc.ui.swing.UIUtilities.SMALL_BORDER;
import static com.dmdirc.ui.swing.UIUtilities.layoutGrid;
import com.dmdirc.ui.swing.components.TextLabel;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SpringLayout;
import javax.swing.WindowConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

/**
 * Profile editing dialog.
 */
public final class ProfileEditorDialog extends StandardDialog implements
        ActionListener, ListSelectionListener {
    
    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 3;
    
    /** Previously created instance of ProfileEditorDialog. */
    private static ProfileEditorDialog me = new ProfileEditorDialog();
    
    /** Component panel. */
    private JPanel panel;
    
    /** profile list. */
    private JList profileList;
    
    /** Selected profile index. */
    private int selectedProfile;
    
    /** add button. */
    private JButton addButton;
    /** delete button. */
    private JButton deleteButton;
    /** rename button. */
    private JButton renameButton;
    /** revert button. */
    private JButton revertButton;
    
    /** info label. */
    private TextLabel infoLabel;
    
    /** nickname field. */
    private JTextField nickname;
    
    /** realname field. */
    private JTextField realname;
    
    /**  Ident field. */
    private JTextField ident;
    
    /** Alternate nicknames list. */
    private JList altNick;
    /** Alternate nicknames button panel. */
    private JPanel altNickButtonsPanel;
    
    /** profiles. */
    private List<Identity> profiles;
    
    /** Creates a new instance of ProfileEditorDialog. */
    private ProfileEditorDialog() {
        super((MainFrame) Main.getUI().getMainWindow(), false);
        
        profiles = IdentityManager.getProfiles();
        
        initComponents();
        
        layoutComponents();
        
        addCallbacks();
        
        setLocationRelativeTo((MainFrame) Main.getUI().getMainWindow());
        setVisible(true);
    }
    
    /** Creates the dialog if one doesn't exist, and displays it. */
    public static void showActionsManagerDialog() {
        synchronized (me) {
            me.setVisible(true);
            me.requestFocus();
        }
    }
    
    /** Initialises the components of the dialog. */
    private void initComponents() {
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        orderButtons(new JButton(), new JButton());
        setTitle("Profile Editor");
        
        setMinimumSize(new Dimension(600, 400));
        setPreferredSize(new Dimension(600, 400));
        
        panel = new JPanel(new SpringLayout());
        panel.setVisible(true);
        
        profileList = new JList(new DefaultListModel());
        profileList.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEtchedBorder(),
                BorderFactory.createEmptyBorder(SMALL_BORDER, SMALL_BORDER,
                SMALL_BORDER, SMALL_BORDER)));
        
        populateList();
        
        profileList.setSelectedIndex(0);
        selectedProfile = 0;
        
        profileList.setMinimumSize(new Dimension(200, Integer.MAX_VALUE));
        profileList.setPreferredSize(new Dimension(200, Integer.MAX_VALUE));
        
        addButton = new JButton("Add");
        
        deleteButton = new JButton("Delete");
        
        renameButton = new JButton("Rename");
        
        infoLabel = new TextLabel("Profiles describe information needed to "
                + "connect to a server.  You can use a different profile for "
                + "each connection. Profiles are automatically saved when you "
                + "select another or click OK", this);
        
        nickname = new JTextField();
        nickname.setPreferredSize(new Dimension(150, 10));
        nickname.setText(profiles.get(0).getOption("profile", "nickname"));
        
        realname = new JTextField();
        realname.setPreferredSize(new Dimension(150, 10));
        realname.setText(profiles.get(0).getOption("profile", "realname"));
        
        ident = new JTextField();
        ident.setPreferredSize(new Dimension(150, 10));
        ident.setText(profiles.get(0).getOption("profile", "ident"));
        
        altNick = new JList(new DefaultListModel());
        altNick.setVisibleRowCount(2);
        altNick.setFixedCellHeight(altNick.getFont().getSize());
        populateAltNicks(0);
        
        altNickButtonsPanel = new JPanel();
        altNickButtonsPanel.setLayout(new GridLayout(1, 3, SMALL_BORDER,
                SMALL_BORDER));
        
        JButton button = new JButton("Add");
        button.setActionCommand("addAltNick");
        button.addActionListener(this);
        altNickButtonsPanel.add(button);
        
        button = new JButton("Edit");
        button.setActionCommand("editAltNick");
        button.addActionListener(this);
        altNickButtonsPanel.add(button);
        
        button = new JButton("Delete");
        button.setActionCommand("deleteAltNick");
        button.addActionListener(this);
        altNickButtonsPanel.add(button);
        
        revertButton = new JButton("Revert");
    }
    
    /** Lays out the dialog. */
    private void layoutComponents() {
        final GridBagConstraints constraints = new GridBagConstraints();
        getContentPane().setLayout(new GridBagLayout());
        
        panel.add(new JLabel("Nickname: "));
        panel.add(nickname);
        panel.add(new JLabel("Realname: "));
        panel.add(realname);
        panel.add(new JLabel("Ident: "));
        panel.add(ident);
        panel.add(new JLabel("Alt nicknames: "));
        panel.add(new JScrollPane(altNick));
        panel.add(Box.createHorizontalBox());
        panel.add(altNickButtonsPanel);
        panel.add(Box.createHorizontalBox());
        panel.add(revertButton);
        
        layoutGrid(panel, 6, 2, LARGE_BORDER, LARGE_BORDER, SMALL_BORDER, SMALL_BORDER);
        
        constraints.weighty = 0.0;
        constraints.weightx = 0.0;
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.gridwidth = 1;
        constraints.gridheight = 3;
        constraints.fill = GridBagConstraints.BOTH;
        constraints.insets.set(LARGE_BORDER, LARGE_BORDER, LARGE_BORDER, 0);
        getContentPane().add(profileList, constraints);
        
        constraints.insets.set(0, LARGE_BORDER, 0, 0);
        constraints.gridheight = 1;
        constraints.gridy = 4;
        getContentPane().add(addButton, constraints);
        
        constraints.gridy = 5;
        getContentPane().add(deleteButton, constraints);
        
        constraints.gridy = 6;
        constraints.insets.set(0, LARGE_BORDER, LARGE_BORDER, 0);
        getContentPane().add(renameButton, constraints);
        
        constraints.weightx = 1.0;
        constraints.weighty = 0.0;
        constraints.gridx = 1;
        constraints.gridy = 0;
        constraints.gridheight = 1;
        constraints.gridwidth = 2;
        constraints.insets.set(LARGE_BORDER, LARGE_BORDER, LARGE_BORDER, 0);
        getContentPane().add(infoLabel, constraints);
        
        constraints.weighty = 1.0;
        constraints.gridy = 1;
        constraints.insets.set(0, SMALL_BORDER, 0, SMALL_BORDER);
        getContentPane().add(panel, constraints);
        
        constraints.gridwidth = 1;
        constraints.weighty = 0.0;
        constraints.gridy = 6;
        constraints.insets.set(0, 0, 0, 0);
        getContentPane().add(Box.createHorizontalGlue(), constraints);
        
        constraints.gridx = 1;
        constraints.weightx = 0.0;
        constraints.insets.set(0, LARGE_BORDER, LARGE_BORDER, 0);
        constraints.anchor = GridBagConstraints.EAST;
        constraints.fill = GridBagConstraints.NONE;
        getContentPane().add(getLeftButton(), constraints);
        
        constraints.gridx = 2;
        constraints.insets.set(0, LARGE_BORDER, LARGE_BORDER, LARGE_BORDER);
        getContentPane().add(getRightButton(), constraints);
        
        pack();
    }
    
    /** Adds listeners to various components in the dialog. */
    private void addCallbacks() {
        getCancelButton().addActionListener(this);
        getOkButton().addActionListener(this);
        revertButton.addActionListener(this);
        renameButton.addActionListener(this);
        deleteButton.addActionListener(this);
        addButton.addActionListener(this);
        profileList.addListSelectionListener(this);
    }
    
    /** {@inheritDoc}. */
    public void actionPerformed(final ActionEvent event) {
        if (event.getSource() == revertButton) {
            populateProfile(selectedProfile);
        } else if (event.getSource() == addButton) {
            addProfile();
        } else if (event.getSource() == deleteButton) {
            deleteProfile();
        } else if (event.getSource() == renameButton) {
            renameProfile();
        } else if ("addAltNick".equals(event.getActionCommand())) {
            addAltNick();
        } else if ("editAltNick".equals(event.getActionCommand())) {
            editAltNick();
        } else if ("deleteAltNick".equals(event.getActionCommand())) {
            deleteAltNick();
        } else if (event.getSource() == getOkButton()) {
            saveSettings();
        } else if (event.getSource() == getCancelButton()) {
            cancelDialog();
        }
    }
    
    /** Adds a profile. */
    private void addProfile() {
        final String newName = JOptionPane.showInputDialog(this,
                "Please enter the new profile's name", "New profile");
        if (newName != null && !newName.isEmpty()) {
            final Identity newIdentity = Identity.buildProfile(newName);
            profiles = IdentityManager.getProfiles();
            populateList();
            selectedProfile = profiles.indexOf(newIdentity);
            populateProfile(selectedProfile);
            profileList.setSelectedIndex(selectedProfile);
            profileList.repaint();
        }
    }
    
    /** Deletes a profile. */
    private void deleteProfile() {
        final int response = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete this profile?",
                "Delete confirmaton", JOptionPane.YES_NO_OPTION);
        if (response == JOptionPane.YES_OPTION) {
            final Identity oldIdentity =
                    profiles.get(profileList.getSelectedIndex());
            oldIdentity.delete();
            profiles = IdentityManager.getProfiles();
            populateList();
            selectedProfile = profiles.indexOf(oldIdentity);
            selectedProfile--;
            if (selectedProfile < 0) {
                selectedProfile = 0;
            }
            if (selectedProfile != -1 && profileList.getModel().getSize() > 0) {
                populateProfile(selectedProfile);
            } else {
                clearProfile();
            }
            profileList.repaint();
            renameButton.setEnabled(false);
            deleteButton.setEnabled(false);
        }
    }
    
    /** Renames a profile. */
    private void renameProfile() {
        final String newName = JOptionPane.showInputDialog(this,
                "Please enter the new name for the profile",
                profileList.getSelectedValue());
        if (newName != null && !newName.isEmpty()) {
            profiles.get(profileList.getSelectedIndex()).setOption("identity", "name", newName);
            profileList.repaint();
        }
    }
    
    /** Adds an alternate nick. */
    private void addAltNick() {
        final String newName = JOptionPane.showInputDialog(this,
                "Please enter the new nickname", "New alt nickname");
        if (newName != null && !newName.isEmpty()) {
            ((DefaultListModel) altNick.getModel()).addElement(newName);
        }
    }
    
    /** Edits an alternate nick. */
    private void editAltNick() {
        if (altNick.getSelectedIndex() != -1) {
            final String newName = JOptionPane.showInputDialog(this,
                    "Please enter the nickname for the alt nickname",
                    altNick.getSelectedValue());
            if (newName != null && !newName.isEmpty()) {
                ((DefaultListModel) altNick.getModel()).setElementAt(newName, altNick.getSelectedIndex());
            }
        }
    }
    
    /** Deletes an alternate nick. */
    private void deleteAltNick() {
        if (altNick.getSelectedIndex() != -1) {
            final int response = JOptionPane.showConfirmDialog(this,
                    "Are you sure you want to delete this nick?",
                    "Delete confirmaton", JOptionPane.YES_NO_OPTION);
            if (response == JOptionPane.YES_OPTION) {
                ((DefaultListModel) altNick.getModel()).removeElementAt(altNick.getSelectedIndex());
            }
        }
    }
    
    /** Closes and saves the dialog. */
    private void saveSettings() {
        if (selectedProfile != -1 && profileList.getModel().getSize() > 0) {
            saveProfile(selectedProfile);
        }
        NewServerDialog.getNewServerDialog().populateProfiles();
        this.dispose();
    }
    
    /** cancels the dialog */
    private void cancelDialog() {
        NewServerDialog.getNewServerDialog().populateProfiles();
        dispose();
    }
    
    /** {@inheritDoc}. */
    public void valueChanged(final ListSelectionEvent selectionEvent) {
        if (!selectionEvent.getValueIsAdjusting()) {
            final int selected = ((JList) selectionEvent.getSource()).getSelectedIndex();
            if (selected >= 0) {
                if (selectedProfile != -1 && profileList.getModel().getSize() > 0) {
                    saveProfile(selectedProfile);
                }
                populateProfile(selected);
                renameButton.setEnabled(true);
                deleteButton.setEnabled(true);
            } else {
                if (selectedProfile != -1) {
                    saveProfile(selectedProfile);
                    clearProfile();
                }
                renameButton.setEnabled(false);
                deleteButton.setEnabled(false);
            }
            selectedProfile = selected;
        }
    }
    
    /** Populates the identity list with identities from the identity manager. */
    private void populateList() {
        ((DefaultListModel) profileList.getModel()).clear();
        for (Identity profile : profiles) {
            ((DefaultListModel) profileList.getModel()).addElement(profile);
        }
    }
    
    /**
     *  Sets the profile options for the given index.
     *
     * @param index profile number to populate
     */
    private void populateProfile(final int index) {
        final Identity profile = profiles.get(index);
        
        nickname.setText(profile.getOption("profile", "nickname"));
        realname.setText(profile.getOption("profile", "realname"));
        ident.setText(profile.getOption("profile", "ident"));
        populateAltNicks(index);
    }
    
    /** Clears the profile. */
    private void clearProfile() {
        nickname.setText("");
        realname.setText("");
        ident.setText("");
        ((DefaultListModel) altNick.getModel()).clear();
    }
    
    /**
     *  Saves the profile options for the given index.
     *
     * @param index profile number to save
     */
    private void saveProfile(final int index) {
        StringBuffer altNicks;
        
        
        final Identity profile = profiles.get(index);
        
        profile.setOption("profile", "nickname", nickname.getText());
        profile.setOption("profile", "realname", realname.getText());
        profile.setOption("profile", "ident", ident.getText());
        
        altNicks = new StringBuffer();
        
        for (int i = 0; i < altNick.getModel().getSize(); i++) {
            altNicks.append(altNick.getModel().getElementAt(i)).append('\n');
        }
        
        profile.setOption("profile", "altnicks", altNicks.toString().trim());
    }
    
    /**
     * Populates the alternate nick list.
     *
     * @param index profile number to populate from
     */
    private void populateAltNicks(final int index) {
        String[] altNicks;
        
        ((DefaultListModel) altNick.getModel()).clear();
        
        if (profiles.get(index).getOption("profile", "altnicks") != null
                && !profiles.get(index).getOption("profile", "altnicks").isEmpty()) {
            altNicks = profiles.get(index).getOption("profile", "altnicks").split("\\n");
            
            for (String altNickname : altNicks) {
                ((DefaultListModel) altNick.getModel()).addElement(altNickname);
            }
        }
        altNick.repaint();
    }
}
