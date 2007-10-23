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

package com.dmdirc.ui.swing.dialogs.profiles;

import com.dmdirc.Main;
import com.dmdirc.config.Identity;
import com.dmdirc.config.IdentityManager;
import com.dmdirc.ui.swing.MainFrame;
import com.dmdirc.ui.swing.components.StandardDialog;
import com.dmdirc.ui.swing.components.TextLabel;
import static com.dmdirc.ui.swing.UIUtilities.SMALL_BORDER;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

/** Profile editing dialog. */
public final class ProfileManagerDialog extends StandardDialog implements ActionListener,
        ListSelectionListener {

    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 1;
    /** Previously created instance of ProfileEditorDialog. */
    private static ProfileManagerDialog me;
    /** Profile list. */
    private final List<Identity> profiles;
    /** Profile list. */
    private JList profileList;
    /** Profile list mode. */
    private ProfileListModel model;
    /** Profile detail panel. */
    private ProfileDetailPanel details;
    /** Buttons panel. */
    private JPanel buttonsPanel;
    /** Info label. */
    private TextLabel infoLabel;
    /** Add button. */
    private JButton addButton;
    /** Delete button. */
    private JButton deleteButton;
    /** Selected index. */
    private int selectedIndex;

    /** Creates a new instance of ProfileEditorDialog. */
    private ProfileManagerDialog() {
        super((MainFrame) Main.getUI().getMainWindow(), false);

        profiles = IdentityManager.getProfiles();

        initComponents();

        layoutComponents();

        addListeners();

        if (model.getSize() > 0) {
            profileList.setSelectedIndex(0);
        } else {
            selectedIndex = -1;
        }
    }

    /** Creates the dialog if one doesn't exist, and displays it. */
    public static synchronized void showProfileManagerDialog() {
        me = getProfileManagerDialog();

        me.setLocationRelativeTo((MainFrame) Main.getUI().getMainWindow());
        me.setVisible(true);
        me.requestFocus();
    }

    /**
     * Returns the current instance of the ProfileManagerDialog.
     *
     * @return The current ProfileManagerDialog instance
     */
    public static synchronized ProfileManagerDialog getProfileManagerDialog() {
        if (me == null) {
            me = new ProfileManagerDialog();
        }

        return me;
    }

    /** Initialises the components. */
    private void initComponents() {
        setTitle("Profile Editor");
        setMinimumSize(new Dimension(600, 400));
        setPreferredSize(new Dimension(600, 400));
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setResizable(false);

        model = new ProfileListModel();
        profileList = new JList(model);
        details = new ProfileDetailPanel();
        addButton = new JButton("Add");
        deleteButton = new JButton("Delete");
        infoLabel =
                new TextLabel("Profiles describe information needed to " +
                "connect to a server.  You can use a different profile for " +
                "each connection. Profiles are automatically saved when you " +
                "select another or click OK", this);

        profileList.setCellRenderer(new ProfileListCellRenderer());
        profileList.setFixedCellWidth(200 -
                UIManager.getInt("ScrollBar.width"));

        initButtonsPanel();
        populateList();
    }

    /** Initialises the buttons panel. */
    private void initButtonsPanel() {
        buttonsPanel = new JPanel();
        orderButtons(new JButton(), new JButton());
    }

    /** Lays out the dialog. */
    private void layoutComponents() {
        layoutButtonsPanel();

        final GridBagConstraints constraints = new GridBagConstraints();
        setLayout(new GridBagLayout());

        final JScrollPane sp = new JScrollPane(profileList);
        sp.setMinimumSize(new Dimension(200, 0));
        constraints.weighty = 0.0;
        constraints.weightx = 0.0;
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.gridwidth = 1;
        constraints.gridheight = 3;
        constraints.fill = GridBagConstraints.BOTH;
        constraints.insets.set(SMALL_BORDER, SMALL_BORDER, SMALL_BORDER, 0);
        getContentPane().add(sp, constraints);

        constraints.insets.set(0, SMALL_BORDER, 0, 0);
        constraints.gridheight = 1;
        constraints.gridy = 4;
        getContentPane().add(addButton, constraints);

        constraints.gridy = 5;
        constraints.insets.set(SMALL_BORDER, SMALL_BORDER, SMALL_BORDER, 0);
        getContentPane().add(deleteButton, constraints);

        constraints.weightx = 1.0;
        constraints.weighty = 0.0;
        constraints.gridx = 1;
        constraints.gridy = 0;
        constraints.gridheight = 1;
        constraints.gridwidth = 2;
        constraints.insets.set(SMALL_BORDER, SMALL_BORDER, SMALL_BORDER, 0);
        getContentPane().add(infoLabel, constraints);

        constraints.weighty = 1.0;
        constraints.gridy = 1;
        constraints.insets.set(0, SMALL_BORDER, 0, 0);
        getContentPane().add(details, constraints);

        constraints.gridwidth = 1;
        constraints.weighty = 0.0;
        constraints.gridy = 5;
        constraints.insets.set(0, SMALL_BORDER, SMALL_BORDER, SMALL_BORDER);
        getContentPane().add(buttonsPanel, constraints);

        pack();
    }

    /** Lays out the buttons panel. */
    private void layoutButtonsPanel() {
        buttonsPanel.setLayout(new BoxLayout(buttonsPanel,
                BoxLayout.LINE_AXIS));
        buttonsPanel.add(Box.createHorizontalGlue());
        buttonsPanel.add(getLeftButton());
        buttonsPanel.add(Box.createHorizontalStrut(SMALL_BORDER));
        buttonsPanel.add(getRightButton());
    }

    /** Adds listeners to the components. */
    private void addListeners() {
        getOkButton().addActionListener(this);
        getCancelButton().addActionListener(this);

        addButton.addActionListener(this);
        deleteButton.addActionListener(this);

        profileList.addListSelectionListener(this);
    }

    /** Populates the profile list. */
    public void populateList() {
        final String profileString = "profile";
        model.clear();

        for (Identity profile : profiles) {
            model.add(new Profile(profile.getName(),
                    profile.getOption(profileString, "nickname"),
                    profile.getOption(profileString, "realname"),
                    profile.getOption(profileString, "ident"),
                    profile.getOptionList(profileString, "altnicks"),
                    false));
        }
    }

    /** Saves the profile list. */
    private void save() {
        if (!details.validateDetails().equals(ValidationResult.FAIL)) {
            details.save();
            final Iterator<Profile> it = model.iterator();

            while (it.hasNext()) {
                it.next().save();
            }

            dispose();
        }
    }

    /** {@inheritDoc} */
    @Override
    public void actionPerformed(final ActionEvent e) {
        if (e.getSource().equals(getOkButton())) {
            save();
        } else if (e.getSource().equals(getCancelButton())) {
            dispose();
        } else if (e.getSource().equals(addButton)) {
            final Profile profile = new Profile("Unnamed");
            model.add(profile);
            profileList.setSelectedIndex(model.indexOf(profile));
        } else if (e.getSource().equals(deleteButton)) {
            if (JOptionPane.showConfirmDialog(this,
                    "Are you sure you want to delete this profile?",
                    "Delete Confirmaton", JOptionPane.YES_NO_OPTION) ==
                    JOptionPane.YES_OPTION) {
                model.remove((Profile) profileList.getSelectedValue());
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public void valueChanged(final ListSelectionEvent e) {
        if (e.getValueIsAdjusting()) {
            if (details.validateDetails().equals(ValidationResult.FAIL)) {
                profileList.setSelectedIndex(selectedIndex);
            }
        }
        if (!e.getValueIsAdjusting()) {
            details.save();
            details.setProfile((Profile) profileList.getSelectedValue());
            if (profileList.getSelectedIndex() == -1) {
                deleteButton.setEnabled(false);
            } else {
                deleteButton.setEnabled(true);
            }
        }
        selectedIndex = profileList.getSelectedIndex();
    }

    /** {@inheritDoc} */
    @Override
    public void dispose() {
        synchronized (me) {
            super.dispose();
            me = null;
        }
    }
}