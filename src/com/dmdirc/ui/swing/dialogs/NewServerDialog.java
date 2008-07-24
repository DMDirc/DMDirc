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

package com.dmdirc.ui.swing.dialogs;

import com.dmdirc.Main;
import com.dmdirc.Server;
import com.dmdirc.ServerManager;
import com.dmdirc.config.Identity;
import com.dmdirc.config.IdentityManager;
import com.dmdirc.config.prefs.validator.PortValidator;
import com.dmdirc.ui.interfaces.Window;
import com.dmdirc.ui.swing.MainFrame;
import com.dmdirc.ui.swing.components.StandardDialog;
import com.dmdirc.config.prefs.validator.RegexStringValidator;
import com.dmdirc.ui.swing.components.validating.ValidatingJTextField;
import com.dmdirc.ui.swing.dialogs.profiles.ProfileManagerDialog;

import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.WindowConstants;

import net.miginfocom.swing.MigLayout;

/**
 * Dialog that allows the user to enter details of a new server to connect to.
 */
public final class NewServerDialog extends StandardDialog implements ActionListener {

    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 8;
    /** A previously created instance of NewServerDialog. */
    private static volatile NewServerDialog me;
    /** checkbox. */
    private JCheckBox newServerWindowCheck;
    /** checkbox. */
    private JCheckBox sslCheck;
    /** text field. */
    private ValidatingJTextField serverField;
    /** text field. */
    private ValidatingJTextField portField;
    /** text field. */
    private JTextField passwordField;
    /** combo box. */
    private JComboBox identityField;
    /** button. */
    private JButton editProfileButton;

    /**
     * Creates a new instance of the dialog.
     */
    private NewServerDialog() {
        super((MainFrame) Main.getUI().getMainWindow(), false);

        initComponents();
        layoutComponents();
        addListeners();
        
        update();
    }

    /**
     * Creates the new server dialog if one doesn't exist, and displays it.
     */
    public static void showNewServerDialog() {
        me = getNewServerDialog();

        me.setLocationRelativeTo((MainFrame) Main.getUI().getMainWindow());
        me.setVisible(true);
        me.requestFocus();
    }

    /**
     * Returns the current instance of the NewServerDialog.
     *
     * @return The current NewServerDialog instance
     */
    public static NewServerDialog getNewServerDialog() {
        synchronized (NewServerDialog.class) {
            if (me == null) {
                me = new NewServerDialog();
            }
        }

        return me;
    }

    /**
     * Is the new server dialog showing?
     * 
     * @return true iif the NSD is showing
     */
    public static synchronized boolean isNewServerDialogShowing() {
        return me != null;
    }

    /** Updates the values to defaults. */
    private void update() {
        serverField.setText(IdentityManager.getGlobalConfig().getOption("general",
                "server", ""));
        portField.setText(IdentityManager.getGlobalConfig().getOption("general",
                "port", "6667"));
        passwordField.setText(IdentityManager.getGlobalConfig().getOption("general",
                "password", ""));
        sslCheck.setSelected(false);
        newServerWindowCheck.setEnabled(false);

        serverField.requestFocus();

        if (ServerManager.getServerManager().numServers() == 0 || Main.getUI().
                getActiveWindow() == null) {
            newServerWindowCheck.setSelected(true);
            newServerWindowCheck.setEnabled(false);
        } else {
            newServerWindowCheck.setEnabled(true);
        }

        populateProfiles();
    }

    /**
     * Adds listeners for various objects in the dialog.
     */
    private void addListeners() {
        getCancelButton().addActionListener(this);
        getOkButton().addActionListener(this);
        editProfileButton.addActionListener(this);
    }

    /**
     * Initialises the components in this dialog.
     */
    private void initComponents() {
        serverField = new ValidatingJTextField(new RegexStringValidator("^[^\\s]+$+",
                "Cannot contain spaces."));
        portField = new ValidatingJTextField(new PortValidator());
        passwordField = new JPasswordField();
        newServerWindowCheck = new JCheckBox();
        newServerWindowCheck.setSelected(true);
        sslCheck = new JCheckBox();
        identityField = new JComboBox();
        editProfileButton = new JButton();

        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        orderButtons(new JButton(), new JButton());
        setTitle("Connect to a new server");

        populateProfiles();

        editProfileButton.setText("Edit");

        newServerWindowCheck.setText("Open in a new server window?");
        newServerWindowCheck.setBorder(
                BorderFactory.createEmptyBorder(0, 0, 0, 0));
        newServerWindowCheck.setMargin(new Insets(0, 0, 0, 0));

        sslCheck.setText("Use a secure (SSL) connection?");
        sslCheck.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        sslCheck.setMargin(new Insets(0, 0, 0, 0));
    }

    /** Populates the profiles list. */
    public void populateProfiles() {
        final List<Identity> profiles = IdentityManager.getProfiles();
        ((DefaultComboBoxModel) identityField.getModel()).removeAllElements();
        for (Identity profile : profiles) {
            ((DefaultComboBoxModel) identityField.getModel()).addElement(profile);
        }
    }

    /**
     * Lays out the components in the dialog.
     */
    private void layoutComponents() {
        getContentPane().setLayout(new MigLayout("fill"));

        getContentPane().add(new JLabel("Enter the details of the server that " +
                "you wish to connect to."), "span 3, wrap 1.5*unrel");
        getContentPane().add(new JLabel("Server: "), "");
        getContentPane().add(serverField, "growx, wrap");
        getContentPane().add(new JLabel("Port: "), "");
        getContentPane().add(portField, "growx, wrap");
        getContentPane().add(new JLabel("Password: "), "");
        getContentPane().add(passwordField, "growx, wrap");
        getContentPane().add(new JLabel("Profile: "), "");
        getContentPane().add(identityField, "split 2, growx");
        getContentPane().add(editProfileButton, "sg button, wrap");
        getContentPane().add(sslCheck, "skip, wrap");
        getContentPane().add(newServerWindowCheck, "skip, wrap 1.5*unrel");
        getContentPane().add(getLeftButton(), "split, skip, right, sg button");
        getContentPane().add(getRightButton(), "right, sg button");

        pack();
    }

    /**
     * Saves the dialog changes.
     */
    private void save() {
        if (!serverField.validateText()) {
            serverField.requestFocus();
            return;
        }
        if (!portField.validateText()) {
            portField.requestFocus();
            return;
        }

        final String host = serverField.getText();
        final String pass = passwordField.getText();
        final int port = Integer.parseInt(portField.getText());

        dispose();

        final Identity profile =
                (Identity) identityField.getSelectedItem();

        // Open in a new window?
        if (newServerWindowCheck.isSelected() || ServerManager.getServerManager().
                numServers() == 0 || Main.getUI().getActiveWindow() == null) {
            new Server(host, port, pass, sslCheck.isSelected(), profile);
        } else {
            final Window active =
                    Main.getUI().getActiveWindow();
            final Server server = ServerManager.getServerManager().
                    getServerFromFrame(active);
            if (server == null) {
                new Server(host, port, pass, sslCheck.isSelected(),
                        profile);
            } else {
                server.connect(host, port, pass, sslCheck.isSelected(),
                        profile);
            }
        }
    }

    /** 
     * {@inheritDoc}
     * 
     * @param e Action event
     */
    @Override
    public void actionPerformed(final ActionEvent e) {
        if (e.getSource() == getOkButton()) {
            save();
        } else if (e.getSource() == editProfileButton) {
            ProfileManagerDialog.showProfileManagerDialog();
        } else if (e.getSource() == getCancelButton()) {
            dispose();
        }
    }
}
