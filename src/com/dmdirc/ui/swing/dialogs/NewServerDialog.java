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
import com.dmdirc.ui.interfaces.Window;
import com.dmdirc.ui.swing.MainFrame;
import com.dmdirc.ui.swing.components.StandardDialog;
import com.dmdirc.ui.swing.components.validating.RegexValidator;
import com.dmdirc.ui.swing.components.validating.ValidatingJTextField;
import com.dmdirc.ui.swing.components.validating.Validator;
import com.dmdirc.ui.swing.dialogs.profiles.ProfileManagerDialog;
import static com.dmdirc.ui.swing.UIUtilities.LARGE_BORDER;
import static com.dmdirc.ui.swing.UIUtilities.SMALL_BORDER;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;

/**
 * Dialog that allows the user to enter details of a new server to connect to.
 */
public final class NewServerDialog extends StandardDialog {
    
    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 8;
    
    /** The minimum port number. */
    private static final int MIN_PORT = 1;
    
    /** The maximum port number. */
    private static final int MAX_PORT = 65535;
    
    /** A previously created instance of NewServerDialog. */
    private static NewServerDialog me;
    
    /** checkbox. */
    private JCheckBox newServerWindowCheck;
    
    /** checkbox. */
    private JCheckBox sslCheck;
    
    /** label. */
    private JLabel serverLabel;
    
    /** label. */
    private JLabel portLabel;
    
    /** label. */
    private JLabel passwordLabel;
    
    /** text field. */
    private ValidatingJTextField serverField;
    
    /** text field. */
    private ValidatingJTextField portField;
    
    /** text field. */
    private JTextField passwordField;
    
    /** label. */
    private JLabel identityLabel;
    
    /** combo box. */
    private JComboBox identityField;
    
    /** label. */
    private JLabel serverListLabel;
    
    /** combo box. */
    private JComboBox serverListField;
    
    /** button. */
    private JButton editProfileButton;
    
    /**
     * Creates a new instance of the dialog.
     */
    private NewServerDialog() {
        super((MainFrame) Main.getUI().getMainWindow(), false);
        
        initComponents();
        
        layoutComponents();
               
        update();
        
        addCallbacks();
    }
    
    /**
     * Creates the new server dialog if one doesn't exist, and displays it.
     */
    public static synchronized void showNewServerDialog() {
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
    public static synchronized NewServerDialog getNewServerDialog() {
        if (me == null) {
            me = new NewServerDialog();
        } else {
            me.update();
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
        serverField.setText(IdentityManager.getGlobalConfig().getOption("general", "server", ""));
        portField.setText(IdentityManager.getGlobalConfig().getOption("general", "port", "6667"));
        passwordField.setText(IdentityManager.getGlobalConfig().getOption("general", "password", ""));
        sslCheck.setSelected(false);
        newServerWindowCheck.setEnabled(false);
        
        serverField.requestFocus();
        
        if (ServerManager.getServerManager().numServers() == 0
                || Main.getUI().getMainWindow().getActiveFrame() == null) {
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
    private void addCallbacks() {
        getCancelButton().addActionListener(new ActionListener() {
            public void actionPerformed(final ActionEvent actionEvent) {
                NewServerDialog.this.dispose();
            }
        });
        getOkButton().addActionListener(new ActionListener() {
            public void actionPerformed(final ActionEvent actionEvent) {
                if (!serverField.validateText()) {
                    serverField.requestFocus();
                    return;
                }
                
                final String host = serverField.getText();
                final String pass = passwordField.getText();
                int port = Integer.parseInt(portField.getText());
                
                NewServerDialog.this.dispose();
                
                final Identity profile =
                        (Identity) identityField.getSelectedItem();
                
                // Open in a new window?
                if (newServerWindowCheck.isSelected()
                || ServerManager.getServerManager().numServers() == 0
                        || Main.getUI().getMainWindow().getActiveFrame() == null) {
                    new Server(host, port, pass, sslCheck.isSelected(), profile);
                } else {
                    final Window active =
                            Main.getUI().getMainWindow().getActiveFrame();
                    final Server server = ServerManager.getServerManager().
                            getServerFromFrame(active);
                    if (server == null) {
                        new Server(host, port, pass, sslCheck.isSelected(), profile);
                    } else {
                        server.connect(host, port, pass, sslCheck.isSelected(),
                                profile);
                    }
                }
            }
        });
        editProfileButton.addActionListener(new ActionListener() {
            public void actionPerformed(final ActionEvent actionEvent) {
                ProfileManagerDialog.showProfileManagerDialog();
            }
        });
    }
    
    /**
     * Initialises the components in this dialog.
     */
    private void initComponents() {
        final JButton button1 = new JButton();
        final JButton button2 = new JButton();
        
        serverLabel = new JLabel();
        serverField = new ValidatingJTextField(new RegexValidator("^[^\\s]+$+", "Cannot contain spaces."));
        portLabel = new JLabel();
        portField = new ValidatingJTextField(new Validator<String>() {

            /** {@inheritDoc} */
            @Override
            public boolean validate(final String object) {
                int port;
                try {
                    port = Integer.parseInt(object);
                } catch (NumberFormatException e) {
                    return false;
                }
                if (port >= MIN_PORT && port <= MAX_PORT) {
                    return true;
                }
                return false;
            }

            /** {@inheritDoc} */
            @Override
            public String getFailureReason() {
                return "Must be a valid port (1-65535)";
            }
        });
        passwordLabel = new JLabel();
        passwordField = new JPasswordField();
        newServerWindowCheck = new JCheckBox();
        newServerWindowCheck.setSelected(true);
        sslCheck = new JCheckBox();
        identityLabel = new JLabel();
        identityField = new JComboBox();
        serverListLabel = new JLabel();
        serverListField = new JComboBox(new String[]{});
        editProfileButton = new JButton();
        
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        orderButtons(button2, button1);
        setTitle("Connect to a new server");
        
        serverLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        serverLabel.setText("Server:");
        
        portLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        portLabel.setText("Port:");
        
        passwordLabel.setText("Password:");
        
        identityLabel.setText("Profile: ");
        populateProfiles();
        
        editProfileButton.setText("Edit");
        
        serverListLabel.setText("Server: ");
        
        serverListField.setEnabled(false);
        
        newServerWindowCheck.setText("Open in a new server window");
        newServerWindowCheck.setBorder(
                BorderFactory.createEmptyBorder(0, 0, 0, 0));
        newServerWindowCheck.setMargin(new Insets(0, 0, 0, 0));
        
        sslCheck.setText("Use a secure (SSL) connection");
        sslCheck.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        sslCheck.setMargin(new Insets(0, 0, 0, 0));
    }
    
    /** Populates the profiles list. */
    public void populateProfiles() {
        final Identity[] profiles =
                IdentityManager.getProfiles().toArray(new Identity[0]);
        ((DefaultComboBoxModel) identityField.getModel()).removeAllElements();
        for (Identity profile : profiles) {
            ((DefaultComboBoxModel)
            identityField.getModel()).addElement(profile);
        }
    }
    
    /**
     * Lays out the components in the dialog.
     */
    private void layoutComponents() {
        final GridBagConstraints constraints = new GridBagConstraints();
        getContentPane().setLayout(new GridBagLayout());
        
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.gridwidth = 4;
        constraints.weightx = 0.0;
        constraints.weighty = 1.0;
        constraints.fill = GridBagConstraints.NONE;
        constraints.anchor = GridBagConstraints.WEST;
        constraints.insets = new Insets(LARGE_BORDER, LARGE_BORDER,
                LARGE_BORDER, LARGE_BORDER);
        getContentPane().add(new JLabel("To connect to a new IRC server, enter "
                + "the server name below"), constraints);
        
        constraints.gridy = 1;
        constraints.insets = new Insets(SMALL_BORDER, LARGE_BORDER,
                SMALL_BORDER, SMALL_BORDER);
        constraints.gridwidth = 1;
        getContentPane().add(serverLabel, constraints);
        constraints.insets = new Insets(SMALL_BORDER, 0,
                SMALL_BORDER, LARGE_BORDER);
        constraints.gridx = 1;
        constraints.gridwidth = 3;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.weightx = 1.0;
        getContentPane().add(serverField, constraints);
        
        constraints.insets = new Insets(SMALL_BORDER, LARGE_BORDER,
                SMALL_BORDER, SMALL_BORDER);
        constraints.gridy = 2;
        constraints.gridx = 0;
        constraints.fill = GridBagConstraints.NONE;
        constraints.gridwidth = 1;
        constraints.weightx = 0.0;
        getContentPane().add(portLabel, constraints);
        constraints.insets = new Insets(SMALL_BORDER, 0,
                SMALL_BORDER, LARGE_BORDER);
        constraints.gridx = 1;
        constraints.weightx = 0.2;
        constraints.gridwidth = 3;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        getContentPane().add(portField, constraints);
        
        constraints.gridwidth = 1;
        constraints.gridy = 3;
        constraints.insets = new Insets(SMALL_BORDER, LARGE_BORDER,
                SMALL_BORDER, SMALL_BORDER);
        constraints.gridx = 0;
        constraints.weightx = 0.0;
        constraints.fill = GridBagConstraints.NONE;
        getContentPane().add(passwordLabel, constraints);
        constraints.insets = new Insets(SMALL_BORDER, 0,
                SMALL_BORDER, LARGE_BORDER);
        constraints.gridwidth = 3;
        constraints.gridx = 1;
        constraints.weightx = 0.8;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        getContentPane().add(passwordField, constraints);
        
        /*constraints.insets = new Insets(SMALL_BORDER, LARGE_BORDER,
                SMALL_BORDER, SMALL_BORDER);
        constraints.gridy = 4;
        constraints.gridx = 0;
        constraints.gridwidth = 1;
        constraints.weightx = 0.0;
        getContentPane().add(serverListLabel, constraints);
        constraints.insets = new Insets(SMALL_BORDER, 0,
                SMALL_BORDER, LARGE_BORDER);
        constraints.gridx = 1;
        constraints.gridwidth = 3;
        constraints.weightx = 1.0;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        getContentPane().add(serverListField, constraints);*/
        
        constraints.insets = new Insets(SMALL_BORDER, LARGE_BORDER,
                SMALL_BORDER, SMALL_BORDER);
        constraints.gridy = 5;
        constraints.gridx = 0;
        constraints.gridwidth = 1;
        constraints.weightx = 0.0;
        getContentPane().add(identityLabel, constraints);
        constraints.insets = new Insets(SMALL_BORDER, 0,
                SMALL_BORDER, LARGE_BORDER);
        constraints.gridx = 1;
        constraints.gridwidth = 2;
        constraints.weightx = 1.0;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        getContentPane().add(identityField, constraints);
        constraints.gridwidth = 1;
        constraints.gridx = 3;
        getContentPane().add(editProfileButton, constraints);
        
        constraints.weightx = 0.0;
        constraints.gridwidth = 4;
        constraints.gridy = 6;
        constraints.gridx = 0;
        constraints.fill = GridBagConstraints.NONE;
        constraints.insets = new Insets(SMALL_BORDER, LARGE_BORDER,
                SMALL_BORDER, SMALL_BORDER);
        getContentPane().add(sslCheck, constraints);
        
        constraints.gridy = 7;
        getContentPane().add(newServerWindowCheck, constraints);
        
        constraints.weighty = 0.0;
        constraints.weightx = 1.0;
        constraints.gridx = 0;
        constraints.gridy = 8;
        constraints.gridwidth = 2;
        constraints.fill = GridBagConstraints.BOTH;
        getContentPane().add(Box.createHorizontalGlue(), constraints);
        
        constraints.gridwidth = 1;
        constraints.weightx = 0.0;
        constraints.insets.set(LARGE_BORDER, LARGE_BORDER, LARGE_BORDER, 0);
        constraints.gridx = 2;
        constraints.anchor = GridBagConstraints.EAST;
        constraints.fill = GridBagConstraints.NONE;
        getContentPane().add(getLeftButton(), constraints);
        
        constraints.insets.set(LARGE_BORDER, LARGE_BORDER, LARGE_BORDER,
                LARGE_BORDER);
        constraints.gridx = 3;
        getContentPane().add(getRightButton(), constraints);
        
        pack();
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
