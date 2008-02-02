/*
 * Copyright (c) 2006-2008 Chris Smith, Shane Mc Cormack, Gregory Holmes
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
import com.dmdirc.ui.swing.MainFrame;
import com.dmdirc.ui.swing.components.StandardInputDialog;
import com.dmdirc.config.prefs.validator.NotEmptyValidator;
import com.dmdirc.config.prefs.validator.RegexStringValidator;
import com.dmdirc.ui.swing.components.validating.ValidatingJTextField;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Enumeration;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import net.miginfocom.swing.MigLayout;

/** Profile detail panel. */
public class ProfileDetailPanel extends JPanel implements ActionListener,
        ListSelectionListener, ListDataListener {

    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 2;
    /** Nickname regex. */
    private static final String NICKNAME_REGEX =
            "[A-Za-z0-9\\[\\]{|}\\-\\^\\\\\\`\\_]+";
    /** Ident regex. */
    private static final String IDENT_REGEX =
            "[A-Za-z0-9\\[\\]{|}\\-\\^\\\\]*";
    /** Filename regex. */
    private static final String FILENAME_REGEX = "[A-Za-z0-9 ]+";
    /** Displayed profile. */
    private Profile profile;
    /** Name text field. */
    private ValidatingJTextField name;
    /** Nick name text field. */
    private ValidatingJTextField nickname;
    /** Realname text field. */
    private ValidatingJTextField realname;
    /** Ident text field. */
    private ValidatingJTextField ident;
    /** Alternate nicknames list. */
    private JList altNicknames;
    /** Add button. */
    private JButton addButton;
    /** Delete button. */
    private JButton delButton;
    /** Edit button. */
    private JButton editButton;

    /** Creates a new profile detail panel. */
    public ProfileDetailPanel() {
        initMainComponents();
        layoutComponents();

        clearProfile();
    }

    /** Initialises the components in the main panel. */
    private void initMainComponents() {
        name = new ValidatingJTextField(new RegexStringValidator(FILENAME_REGEX,
                "Name must only contain letters and numbers."));
        nickname =
                new ValidatingJTextField(new RegexStringValidator(NICKNAME_REGEX,
                "Nickname must only contain letters, numbers and []{}|-^\\.`_"));
        realname = new ValidatingJTextField(new NotEmptyValidator());
        ident = new ValidatingJTextField(new RegexStringValidator(IDENT_REGEX,
                "Ident must only contain letters, numbers and []{}|-^\\."));
        altNicknames = new JList(new DefaultListModel());

        addButton = new JButton("Add");
        delButton = new JButton("Delete");
        editButton = new JButton("Edit");

        name.setPreferredSize(new Dimension(0, name.getFont().getSize()));
        nickname.setPreferredSize(new Dimension(0, nickname.getFont().getSize()));
        realname.setPreferredSize(new Dimension(0, nickname.getFont().getSize()));
        ident.setPreferredSize(new Dimension(0, nickname.getFont().getSize()));
        altNicknames.setVisibleRowCount(3);

        altNicknames.addListSelectionListener(this);
        altNicknames.getModel().addListDataListener(this);

        addButton.addActionListener(this);
        delButton.addActionListener(this);
        editButton.addActionListener(this);
    }

    /** Lays out the components in the panel. */
    private void layoutComponents() {
        setLayout(new MigLayout("fill"));

        add(new JLabel("Name:"));
        add(name, "growx, pushx, wrap");

        add(new JLabel("Nickname:"));
        add(nickname, "growx, pushx, wrap");

        add(new JLabel("Realname:"));
        add(realname, "growx, pushx, wrap");

        add(new JLabel("Ident:"));
        add(ident, "growx, pushx, wrap");

        add(new JLabel("Alternate nicknames:"));
        add(new JScrollPane(altNicknames), "growx, pushx, wrap");

        add(addButton, "skip 1, split 3, sg button, growx");
        add(editButton, "sg button, growx");
        add(delButton, "sg button, growx");
    }

    /**
     * Sets the profile for the detail panel.
     *
     * @param profile new Profile for the detail panel
     */
    public void setProfile(final Profile profile) {
        this.profile = profile;

        updateProfile();
    }

    /** Updates this detail panel. */
    public void updateProfile() {
        if (profile == null) {
            clearProfile();
            return;
        }

        name.setText(profile.getName());
        nickname.setText(profile.getNickname());
        realname.setText(profile.getRealname());
        ident.setText(profile.getIdent());

        ((DefaultListModel) altNicknames.getModel()).clear();
        for (String altNick : profile.getAltNicknames()) {
            ((DefaultListModel) altNicknames.getModel()).addElement(altNick);
        }

        name.setEnabled(true);
        nickname.setEnabled(true);
        realname.setEnabled(true);
        ident.setEnabled(true);
        altNicknames.setEnabled(true);
        addButton.setEnabled(true);
    }

    /** Clears this detail panel. */
    public void clearProfile() {
        name.setText("");
        nickname.setText("");
        realname.setText("");
        ident.setText("");
        ((DefaultListModel) altNicknames.getModel()).clear();

        name.setEnabled(false);
        nickname.setEnabled(false);
        realname.setEnabled(false);
        ident.setEnabled(false);
        altNicknames.setEnabled(false);
        addButton.setEnabled(false);
        delButton.setEnabled(false);
        editButton.setEnabled(false);
    }

    /** Saves the detail panels details to the profile. */
    public void save() {
        if (profile == null) {
            return;
        }

        profile.setName(name.getText());
        profile.setNickname(nickname.getText());
        profile.setRealname(realname.getText());
        profile.setIdent(ident.getText());
        profile.setAltNicknames(new ArrayList<String>());
        final Enumeration<?> enumeration =
                ((DefaultListModel) altNicknames.getModel()).elements();
        while (enumeration.hasMoreElements()) {
            profile.addAltNickname((String) enumeration.nextElement());
        }
    }

    /**
     * Validates the current details.
     *
     * @return Validation Result
     */
    public boolean validateDetails() {
        if (!ident.validateText() || !realname.validateText() ||
                !nickname.validateText() || !name.validateText()) {
            return false;
        }
        return true;
    }

    /** 
     * {@inheritDoc}
     * 
     * @param e Action event
     */
    @Override
    public void actionPerformed(final ActionEvent e) {
        if (e.getSource() == addButton) {
            new StandardInputDialog((MainFrame) Main.getUI().getMainWindow(),
                    false, "New Alternate Nickname",
                    "Please enter the name for alternate nickname",
                    new RegexStringValidator(NICKNAME_REGEX,
                    "Nicknames must only contain letters, numbers and []{}|-^\\.`_")) {

                /**
                 * A version number for this class. It should be changed whenever the class
                 * structure is changed (or anything else that would prevent serialized
                 * objects being unserialized with the new class).
                 */
                private static final long serialVersionUID = 2;

                /** {@inheritDoc} */
                @Override
                public boolean save() {
                    ((DefaultListModel) altNicknames.getModel()).addElement(getText());
                    return true;
                }

                /** {@inheritDoc} */
                @Override
                public void cancelled() {
                //Ignore
                }
            }.display();
        } else if (e.getSource() == editButton) {
            final StandardInputDialog dialog = new StandardInputDialog(
                    (MainFrame) Main.getUI().getMainWindow(),
                    false, "New Alternate Nickname",
                    "Please enter the name for alternate nickname",
                    new RegexStringValidator(NICKNAME_REGEX,
                    "Ident must only contain letters, numbers and []{}|-^\\.")) {

                /**
                 * A version number for this class. It should be changed whenever the class
                 * structure is changed (or anything else that would prevent serialized
                 * objects being unserialized with the new class).
                 */
                private static final long serialVersionUID = 2;

                /** {@inheritDoc} */
                @Override
                public boolean save() {
                    ((DefaultListModel) altNicknames.getModel()).setElementAt(
                            getText(), altNicknames.getSelectedIndex());
                    return true;
                }

                /** {@inheritDoc} */
                @Override
                public void cancelled() {
                //Ignore
                }
            };
            dialog.setText((String) altNicknames.getSelectedValue());
            dialog.display();
        } else if (e.getSource() == delButton) {
            if (JOptionPane.showConfirmDialog(this,
                    "Are you sure you want to delete this nickname?",
                    "Delete Confirmaton", JOptionPane.YES_NO_OPTION) ==
                    JOptionPane.YES_OPTION) {
                ((DefaultListModel) altNicknames.getModel()).removeElementAt(altNicknames.getSelectedIndex());
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public void valueChanged(final ListSelectionEvent e) {
        if (!e.getValueIsAdjusting() && altNicknames.getSelectedIndex() == -1) {
            editButton.setEnabled(false);
            delButton.setEnabled(false);
        } else {
            editButton.setEnabled(true);
            delButton.setEnabled(true);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void intervalAdded(final ListDataEvent e) {
        validateDetails();
    }

    /** {@inheritDoc} */
    @Override
    public void intervalRemoved(final ListDataEvent e) {
        validateDetails();
    }

    /** {@inheritDoc} */
    @Override
    public void contentsChanged(final ListDataEvent e) {
        validateDetails();
    }
}
