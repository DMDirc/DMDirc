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

package uk.org.ownage.dmdirc.ui.dialogs;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
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
import javax.swing.JTextField;
import javax.swing.SpringLayout;
import javax.swing.WindowConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import uk.org.ownage.dmdirc.identities.ConfigSource;
import uk.org.ownage.dmdirc.identities.Identity;
import uk.org.ownage.dmdirc.identities.IdentityManager;
import uk.org.ownage.dmdirc.ui.MainFrame;
import uk.org.ownage.dmdirc.ui.components.StandardDialog;

import static uk.org.ownage.dmdirc.ui.UIUtilities.LARGE_BORDER;
import static uk.org.ownage.dmdirc.ui.UIUtilities.SMALL_BORDER;
import static uk.org.ownage.dmdirc.ui.UIUtilities.layoutGrid;

/**
 * Profiel editing dialog.
 */
public class ProfileEditorDialog extends StandardDialog implements
        ActionListener, ListSelectionListener {
    
    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 1;
    
    private JPanel panel;
    
    private JList profileList;
    
    private int selectedProfile;
    
    private JButton addButton;
    private JButton deleteButton;
    private JButton renameButton;
    private JButton revertButton;
    
    private JLabel infoLabel;
    
    private JLabel nicknameLabel;
    private JTextField nickname;
    
    private JLabel realnameLabel;
    private JTextField realname;
    
    private List<ConfigSource> profiles;
    
    /** Creates a new instance of ProfileEditorDialog. */
    public ProfileEditorDialog() {
        super(MainFrame.getMainFrame(), false);
        
        profiles = IdentityManager.getProfiles();
        
        initComponents();
        
        layoutComponents();
        
        addCallbacks();
        
        this.setLocationRelativeTo(MainFrame.getMainFrame());
        this.setVisible(true);
    }
    
    /** Initialises the components of the dialog. */
    private void initComponents() {
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        orderButtons(new JButton(), new JButton());
        setTitle("Profile Editor");
        
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
        
        profileList.setPreferredSize(new Dimension(200, Integer.MAX_VALUE));
        
        addButton = new JButton("Add");
        
        deleteButton = new JButton("Delete");
        
        renameButton = new JButton("Rename");
        
        infoLabel = new JLabel("Blah blah blah, this is a blurb.");
        
        nicknameLabel = new JLabel("Nickname: ");
        nicknameLabel.setPreferredSize(new Dimension(100, 10));
        
        nickname = new JTextField();
        nickname.setPreferredSize(new Dimension(150, 10));
        nickname.setText(profiles.get(0).getOption("profile", "nickname"));
        
        realnameLabel = new JLabel("Realname: ");
        realnameLabel.setPreferredSize(new Dimension(80, 10));
        
        realname = new JTextField();
        realname.setPreferredSize(new Dimension(150, 10));
        realname.setText(profiles.get(0).getOption("profile", "realname"));
        
        revertButton = new JButton("Revert");
    }
    
    /** Lays out the dialog. */
    private void layoutComponents() {
        final GridBagConstraints constraints = new GridBagConstraints();
        getContentPane().setLayout(new GridBagLayout());
        
        panel.add(nicknameLabel);
        panel.add(nickname);
        panel.add(realnameLabel);
        panel.add(realname);
        panel.add(Box.createHorizontalGlue());
        panel.add(revertButton);
        
        layoutGrid(panel, 3, 2, LARGE_BORDER, LARGE_BORDER, SMALL_BORDER, SMALL_BORDER);
        
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
    public void actionPerformed(ActionEvent event) {
        if (event.getSource() == revertButton) {
            nickname.setText(profiles.get(selectedProfile).getOption("profile", "nickname"));
            realname.setText(profiles.get(selectedProfile).getOption("profile", "realname"));
        } else if (event.getSource() == addButton) {
            String newName = JOptionPane.showInputDialog(this,
                    "Please enter the new profile's name", "New profile");
            if (newName != null && !"".equals(newName)) {
                Identity.buildProfile(newName);
                profiles = IdentityManager.getProfiles();
                populateList();
                profileList.repaint();
            }
        } else if (event.getSource() == deleteButton) {
            int response = JOptionPane.showConfirmDialog(this,
                    "Are you sure you want to delete this profile?",
                    "Delete confirmaton", JOptionPane.YES_NO_OPTION);
            if (response == JOptionPane.YES_OPTION) {
                ((Identity) profiles.get(profileList.getSelectedIndex())).delete();
                profiles = IdentityManager.getProfiles();
                populateList();
                profileList.repaint();
            }
        } else if (event.getSource() == renameButton) {
            String newName = JOptionPane.showInputDialog(this,
                    "Please enter the new name for the profile",
                    profileList.getSelectedValue());
            if (newName != null && !"".equals(newName)) {
                profiles.get(profileList.getSelectedIndex()).setOption("identity", "name", newName);
                profileList.repaint();
            }
        } else if (event.getSource() == getOkButton()) {
            nickname.setText(profiles.get(selectedProfile).getOption("profile", "nickname"));
            realname.setText(profiles.get(selectedProfile).getOption("profile", "realname"));
            this.dispose();
        } else if (event.getSource() == getCancelButton()) {
            this.dispose();
        }
    }
    
    /** {@inheritDoc}. */
    public void valueChanged(final ListSelectionEvent selectionEvent) {
        if (!selectionEvent.getValueIsAdjusting()) {
            int selected = ((JList) selectionEvent.getSource()).getSelectedIndex();
            if (selected >= 0) {
                profiles.get(selectedProfile).setOption("profile", "nickname", nickname.getText());
                profiles.get(selectedProfile).setOption("profile", "realname", realname.getText());
                nickname.setText(profiles.get(selected).getOption("profile", "nickname"));
                realname.setText(profiles.get(selected).getOption("profile", "realname"));
            }
            selectedProfile = selected;
        }
    }
    
    private void populateList() {
        ((DefaultListModel) profileList.getModel()).clear();
        for (ConfigSource profile : profiles) {
            ((DefaultListModel) profileList.getModel()).addElement(profile);
        }
    }
}
