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

import com.dmdirc.IconManager;
import com.dmdirc.ui.swing.components.ImageButton;
import static com.dmdirc.ui.swing.UIUtilities.layoutGrid;
import static com.dmdirc.ui.swing.UIUtilities.SMALL_BORDER;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.swing.Box;
import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SpringLayout;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.Document;

/** Profile detail panel. */
public class ProfileDetailPanel extends JPanel implements ActionListener,
        ListSelectionListener, DocumentListener,
        ListDataListener {

    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 1;
    /** Invalid filename characters. */
    private static final String FILENAME_REGEX = ".*[\\a\\f\\n\\r\\t\\v#&/\\\\s].*";
    /** Invalid nickname characters. */
    private static final String NICKNAME_REGEX = ".*[\\a\\f\\n\\r\\t\\v\\s].*";
    /** Invalid ident characters. */
    private static final String IDENT_REGEX = ".*[\\a\\f\\n\\r\\t\\v\\s].*";
    /** Displayed profile. */
    private Profile profile;
    /** Name text field. */
    private JTextField name;
    /** Nick name text field. */
    private JTextField nickname;
    /** Realname text field. */
    private JTextField realname;
    /** Ident text field. */
    private JTextField ident;
    /** Alternate nicknames list. */
    private JList altNicknames;
    /** Buttons panel. */
    private JPanel buttonsPanel;
    /** Add button. */
    private JButton addButton;
    /** Delete button. */
    private JButton delButton;
    /** Edit button. */
    private JButton editButton;
    /** name error. */
    private ImageButton nameError;
    /** nick error. */
    private ImageButton nicknameError;
    /** real name error. */
    private ImageButton realnameError;
    /** ident error. */
    private ImageButton identError;
    /** Alt nicknames error. */
    private ImageButton altNicknamesError;
    /** Document -> Component map. */
    private Map<Document, Component> map;
    /** Error icon. */
    private Icon errorIcon;
    /** Warning icon. */
    private Icon warningIcon;
    /** Transparent icon. */
    private Icon normalIcon;

    /** Creates a new profile detail panel. */
    public ProfileDetailPanel() {
        initMainComponents();
        initButtonsPanel();
        layoutComponents();

        clearProfile();
    }

    /** Initialises the components in the main panel. */
    private void initMainComponents() {
        map =   new HashMap<Document, Component>();

        errorIcon =
                IconManager.getIconManager().getIcon("input-error");
        warningIcon = IconManager.getIconManager().getIcon("warning");
        normalIcon = IconManager.getIconManager().getIcon("nothing");

        name = new JTextField();
        nickname = new JTextField();
        realname = new JTextField();
        ident = new JTextField();
        altNicknames = new JList(new DefaultListModel());

        nameError =
                new ImageButton("nicknameError", normalIcon);
        nicknameError =
                new ImageButton("nicknameError", normalIcon);
        realnameError =
                new ImageButton("realnameError", normalIcon);
        identError = new ImageButton("identError", normalIcon);
        altNicknamesError =
                new ImageButton("altNicknamesError", normalIcon);

        nameError.setFocusable(false);
        nicknameError.setFocusable(false);
        realnameError.setFocusable(false);
        identError.setFocusable(false);
        altNicknamesError.setFocusable(false);

        map.put(name.getDocument(), nameError);
        map.put(nickname.getDocument(), nicknameError);
        map.put(realname.getDocument(), realnameError);
        map.put(ident.getDocument(), identError);

        name.setPreferredSize(new Dimension(0, name.getFont().getSize()));
        nickname.setPreferredSize(new Dimension(0, nickname.getFont().getSize()));
        realname.setPreferredSize(new Dimension(0, nickname.getFont().getSize()));
        ident.setPreferredSize(new Dimension(0, nickname.getFont().getSize()));
        altNicknames.setVisibleRowCount(3);

        name.getDocument().addDocumentListener(this);
        nickname.getDocument().addDocumentListener(this);
        realname.getDocument().addDocumentListener(this);
        ident.getDocument().addDocumentListener(this);
        altNicknames.addListSelectionListener(this);
        altNicknames.getModel().addListDataListener(this);
    }

    /** Initialiases the components in the buttons panel. */
    private void initButtonsPanel() {
        buttonsPanel =
                new JPanel(new GridLayout(1, 3, SMALL_BORDER, SMALL_BORDER));
        addButton = new JButton("Add");
        delButton = new JButton("Delete");
        editButton = new JButton("Edit");

        addButton.addActionListener(this);
        delButton.addActionListener(this);
        editButton.addActionListener(this);
    }

    /** Lays out the components in the panel. */
    private void layoutComponents() {
        buttonsPanel.add(addButton);
        buttonsPanel.add(editButton);
        buttonsPanel.add(delButton);

        setLayout(new SpringLayout());

        add(new JLabel("Name:"));
        add(nameError);
        add(name);

        add(new JLabel("Nickname:"));
        add(nicknameError);
        add(nickname);

        add(new JLabel("Realname:"));
        add(realnameError);
        add(realname);

        add(new JLabel("Ident:"));
        add(identError);
        add(ident);

        add(new JLabel("Alternate nicknames:"));
        add(altNicknamesError);
        add(new JScrollPane(altNicknames));

        add(Box.createHorizontalGlue());
        add(Box.createHorizontalGlue());
        add(buttonsPanel);

        layoutGrid(this, 6, 3, SMALL_BORDER, SMALL_BORDER, SMALL_BORDER,
                SMALL_BORDER);
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
        name.setEnabled(true);
        nickname.setEnabled(true);
        realname.setEnabled(true);
        ident.setEnabled(true);
        altNicknames.setEnabled(true);
        addButton.setEnabled(true);

        name.setText(profile.getName());
        nickname.setText(profile.getNickname());
        realname.setText(profile.getRealname());
        ident.setText(profile.getIdent());

        ((DefaultListModel) altNicknames.getModel()).clear();
        for (String altNick : profile.getAltNicknames()) {
            ((DefaultListModel) altNicknames.getModel()).addElement(altNick);
        }
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
    public ValidationResult validateDetails() {
        nameError.setToolTipText("");
        nicknameError.setToolTipText("");
        realnameError.setToolTipText("");
        identError.setToolTipText("");
        altNicknamesError.setToolTipText("");
        
        nameError.setIcons(normalIcon);
        nicknameError.setIcons(normalIcon);
        realnameError.setIcons(normalIcon);
        identError.setIcons(normalIcon);
        altNicknamesError.setIcons(normalIcon);

        final ValidationResult checkAltNicknames = checkAltNicknames();
        final ValidationResult checkIdent = checkIdent();
        final ValidationResult checkRealname = checkRealname();
        final ValidationResult checkNickname = checkNickname();
        final ValidationResult checkName = checkName();

        if (checkAltNicknames.equals(ValidationResult.FAIL) ||
                checkIdent.equals(ValidationResult.FAIL) ||
                checkRealname.equals(ValidationResult.FAIL) ||
                checkNickname.equals(ValidationResult.FAIL) || 
                checkName.equals(ValidationResult.FAIL)) {
            return ValidationResult.FAIL;
        } else if (checkAltNicknames.equals(ValidationResult.WARNING) ||
                checkIdent.equals(ValidationResult.WARNING) ||
                checkRealname.equals(ValidationResult.WARNING) ||
                checkNickname.equals(ValidationResult.WARNING) ||
                checkName.equals(ValidationResult.WARNING)) {
            return ValidationResult.WARNING;
        } else {
            return ValidationResult.PASS;
        }
    }

    /**
     * Validates the alternate nicknames.
     *
     * @return Validation result
     */
    private ValidationResult checkAltNicknames() {
        if (altNicknames.getModel().getSize() == 0) {
            altNicknamesError.setIcons(errorIcon);
            addButton.requestFocus();
            altNicknamesError.setToolTipText("You must specify at least one alternate nickname.");
            return ValidationResult.FAIL;
        }

        ValidationResult returnValue =
                ValidationResult.PASS;
        final Enumeration<?> enumeration =
                ((DefaultListModel) altNicknames.getModel()).elements();
        while (enumeration.hasMoreElements()) {
            final String altNickname =
                    (String) enumeration.nextElement();
            if (altNickname.isEmpty() || altNickname.matches(NICKNAME_REGEX)) {
                altNicknamesError.setIcons(errorIcon);
                altNicknamesError.setToolTipText("Your nickname cannot be blank.");
                return ValidationResult.FAIL;
            } else if (altNickname.length() <= 1 || altNickname.length() > 9) {
                altNicknamesError.setIcons(warningIcon);
                returnValue =
                        ValidationResult.WARNING;
                altNicknamesError.setToolTipText("Some servers may not allow nicknames this "
                    + (altNickname.length() <= 1 ? "short" : "long") + ".");
            }
        }

        return returnValue;
    }

    /**
     * Validates the ident.
     *
     * @return Validation result
     */
    private ValidationResult checkIdent() {
        ValidationResult returnValue =
                ValidationResult.PASS;
        final String identText = ident.getText();

        if (identText.isEmpty() || identText.matches(IDENT_REGEX)) {
            identError.setIcons(errorIcon);
            ident.requestFocus();
            returnValue =
                    ValidationResult.FAIL;
            identError.setToolTipText("Your ident cannot be blank.");
        }

        return returnValue;
    }

    /**
     * Validates the realname.
     *
     * @return Validation result
     */
    private ValidationResult checkRealname() {
        ValidationResult returnValue =
                ValidationResult.PASS;
        final String realnameText = realname.getText();

        if (realnameText.isEmpty()) {
            realnameError.setIcons(errorIcon);
            realname.requestFocus();
            returnValue =
                    ValidationResult.FAIL;
            realnameError.setToolTipText("Your realname cannot be blank.");
        }

        return returnValue;
    }

    /**
     * Validates the nickname.
     *
     * @return Validation result
     */
    private ValidationResult checkNickname() {
        ValidationResult returnValue =
                ValidationResult.PASS;
        final String nicknameText = nickname.getText();

        if (nicknameText.isEmpty() || nicknameText.matches(NICKNAME_REGEX)) {
            nicknameError.setIcons(errorIcon);
            nickname.requestFocus();
            returnValue =
                    ValidationResult.FAIL;
            nicknameError.setToolTipText("Your nickname cannot be blank.");
        } else if (nicknameText.length() <= 1 || nicknameText.length() > 9) {
            nicknameError.setIcons(warningIcon);
            returnValue =
                    ValidationResult.WARNING;
            nicknameError.setToolTipText("Some servers may not allow nicknames this "
                    + (nicknameText.length() <= 1 ? "short" : "long") + ".");
        }

        return returnValue;
    }

    /**
     * Validates the name.
     *
     * @return Validation result
     */
    private ValidationResult checkName() {
        ValidationResult returnValue =
                ValidationResult.PASS;
        final String nameText = name.getText();

        if (nameText.isEmpty() || nameText.matches(FILENAME_REGEX)) {
            nameError.setIcons(errorIcon);
            name.requestFocus();
            returnValue =
                    ValidationResult.FAIL;
            nameError.setToolTipText("The profile name cannot be blank.");
        }

        return returnValue;
    }

    /** {@inheritDoc} */
    @Override
    public void actionPerformed(final ActionEvent e) {
        if (e.getSource() == addButton) {
            final String newName =
                    JOptionPane.showInputDialog(this,
                    "Please enter the name for alternate nickname",
                    "New Alternate Nickname");
            if (newName != null && !newName.isEmpty()) {
                ((DefaultListModel) altNicknames.getModel()).addElement(newName);
            }
        } else if (e.getSource() == editButton) {
            final String newName =
                    JOptionPane.showInputDialog(this,
                    "Please enter the new nickname for the alternate nickname",
                    altNicknames.getSelectedValue());
            if (newName != null && !newName.isEmpty()) {
                ((DefaultListModel) altNicknames.getModel()).setElementAt(newName,
                        altNicknames.getSelectedIndex());
            }
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
    public void insertUpdate(final DocumentEvent e) {
        validateDetails();
    }

    /** {@inheritDoc} */
    @Override
    public void removeUpdate(final DocumentEvent e) {
        validateDetails();
    }

    /** {@inheritDoc} */
    @Override
    public void changedUpdate(final DocumentEvent e) {
        //Ignore
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