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

package uk.org.ownage.dmdirc.ui;

import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.InputVerifier;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;

import uk.org.ownage.dmdirc.Config;
import uk.org.ownage.dmdirc.Server;
import uk.org.ownage.dmdirc.ServerManager;
import uk.org.ownage.dmdirc.logger.ErrorLevel;
import uk.org.ownage.dmdirc.logger.Logger;

import org.jdesktop.layout.GroupLayout;
import org.jdesktop.layout.LayoutStyle;

/**
 * Dialog that allows the user to enter details of a new server to connect to.
 */
public final class NewServerDialog extends StandardDialog {
    
    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 3;
    
    /**
     * A previously created instance of NewServerDialog.
     */
    private static NewServerDialog me;
    
    /** ok/cancel button. */
    private JButton jButton1;
    
    /** ok/cancel button. */
    private JButton jButton2;
    
    /** checkbox. */
    private JCheckBox jCheckBox1;
    
    /** checkbox. */
    private JCheckBox jCheckBox2;
    
    /** checkbox. */
    private JCheckBox jCheckBox3;
    
    /** checkbox. */
    private JCheckBox jCheckBox4;
    
    /** label. */
    private JLabel jLabel1;
    
    /** label. */
    private JLabel jLabel2;
    
    /** label. */
    private JLabel jLabel3;
    
    /** label. */
    private JLabel jLabel4;
    
    /** text field. */
    private JTextField jTextField1;
    
    /** text field. */
    private JTextField jTextField2;
    
    /** text field. */
    private JTextField jTextField3;
    
    /** label. */
    private JLabel jLabel5;
    
    /** combo box. */
    private JComboBox jComboBox1;
    
    /**
     * Creates a new instance of the dialog.
     */
    private NewServerDialog() {
        super(MainFrame.getMainFrame(), false);
        
        initComponents();
        
        orderButtons(jButton2, jButton1);
        
        jTextField1.setText(Config.getOption("general", "server"));
        jTextField2.setText(Config.getOption("general", "port"));
        jTextField3.setText(Config.getOption("general", "password"));
        jTextField2.setInputVerifier(new PortVerifier());
        
        addCallbacks();
        
    }
    
    /**
     * Creates the new server dialog if one doesn't exist, and displays it.
     */
    public static void showNewServerDialog() {
        if (me == null) {
            me = new NewServerDialog();
            me.setLocationRelativeTo(MainFrame.getMainFrame());
            me.setVisible(true);
        } else {
            me.setLocationRelativeTo(MainFrame.getMainFrame());
            me.setVisible(true);
            me.requestFocus();
        }
        
        me.jTextField1.requestFocus();
        
        if (ServerManager.getServerManager().numServers() == 0
                || MainFrame.getMainFrame().getActiveFrame() == null) {
            me.jCheckBox1.setSelected(true);
            me.jCheckBox1.setEnabled(false);
        } else {
            me.jCheckBox1.setEnabled(true);
        }
    }
    
    /**
     * Adds listeners for various objects in the dialog.
     */
    private void addCallbacks() {
        getCancelButton().addActionListener(new ActionListener() {
            public void actionPerformed(final ActionEvent actionEvent) {
                NewServerDialog.this.setVisible(false);
            }
        });
        getOkButton().addActionListener(new ActionListener() {
            public void actionPerformed(final ActionEvent actionEvent) {
                final String host = jTextField1.getText();
                final String pass = jTextField3.getText();
                final int port = Integer.parseInt(jTextField2.getText());
                
                NewServerDialog.this.setVisible(false);
                
                // Open in a new window?
                if (jCheckBox1.isSelected()
                || ServerManager.getServerManager().numServers() == 0
                        || MainFrame.getMainFrame().getActiveFrame() == null) {
                    new Server(host, port, pass, jCheckBox4.isSelected());
                } else {
                    final JInternalFrame active = MainFrame.getMainFrame().getActiveFrame();
                    final Server server = ServerManager.getServerManager().getServerFromFrame(active);
                    if (server != null) {
                        server.connect(host, port, pass, jCheckBox4.isSelected());
                    } else {
                        Logger.error(ErrorLevel.ERROR, "Cannot determine active server window");
                    }
                }
            }
        });
    }
    
    /**
     * Initialises the components in this dialog.
     */
    private void initComponents() {
        jLabel1 = new JLabel();
        jTextField1 = new JTextField();
        jLabel2 = new JLabel();
        jLabel3 = new JLabel();
        jTextField2 = new JTextField();
        jLabel4 = new JLabel();
        jTextField3 = new JTextField();
        jCheckBox1 = new JCheckBox();
        jCheckBox2 = new JCheckBox();
        jCheckBox3 = new JCheckBox();
        jButton1 = new JButton();
        jButton2 = new JButton();
        jCheckBox4 = new JCheckBox();
        jLabel5 = new JLabel();
        jComboBox1 = new JComboBox(new String[]{"Default", });
        
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Connect to a new server");
        jLabel1.setHorizontalAlignment(SwingConstants.RIGHT);
        jLabel1.setText("Server:");
        
        jTextField1.setText("blueyonder.uk.quakenet.org");
        
        jLabel2.setText("To connect to a new IRC server, enter the server name below");
        
        jLabel3.setHorizontalAlignment(SwingConstants.RIGHT);
        jLabel3.setText("Port:");
        
        jTextField2.setText("7000");
        
        jLabel4.setText("Password:");
        
        jLabel5.setText("Identity: ");
        
        jComboBox1.setEnabled(false);
        
        jCheckBox1.setText("Open in a new server window");
        jCheckBox1.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        jCheckBox1.setMargin(new Insets(0, 0, 0, 0));
        
        jCheckBox2.setText("Remember server password");
        jCheckBox2.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        jCheckBox2.setEnabled(false);
        jCheckBox2.setMargin(new Insets(0, 0, 0, 0));
        
        jCheckBox3.setText("Connect to this server automatically in the future");
        jCheckBox3.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        jCheckBox3.setEnabled(false);
        jCheckBox3.setMargin(new Insets(0, 0, 0, 0));
        
        jButton1.setText("OK");
        
        jButton2.setText("Cancel");
        
        jCheckBox4.setText("Use a secure (SSL) connection");
        jCheckBox4.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        jCheckBox4.setMargin(new Insets(0, 0, 0, 0));
        
        final GroupLayout layout = new GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(GroupLayout.LEADING)
                .add(layout.createSequentialGroup()
                .add(layout.createParallelGroup(GroupLayout.LEADING)
                .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(jLabel2, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .add(layout.createSequentialGroup()
                .add(16, 16, 16)
                .add(layout.createParallelGroup(GroupLayout.TRAILING, false)
                .add(GroupLayout.LEADING, jLabel3, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .add(GroupLayout.LEADING, jLabel1, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(LayoutStyle.RELATED)
                .add(layout.createParallelGroup(GroupLayout.LEADING)
                .add(layout.createSequentialGroup()
                .add(jTextField2, GroupLayout.PREFERRED_SIZE, 56, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(LayoutStyle.RELATED)
                .add(jLabel4)
                .addPreferredGap(LayoutStyle.RELATED)
                .add(jTextField3, GroupLayout.DEFAULT_SIZE, 178, Short.MAX_VALUE))
                .add(jTextField1, GroupLayout.DEFAULT_SIZE, 321, Short.MAX_VALUE)))
                .add(GroupLayout.TRAILING, layout.createSequentialGroup()
                .add(232, 232, 232)
                .add(jButton2, GroupLayout.PREFERRED_SIZE, 76, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(LayoutStyle.RELATED)
                .add(jButton1, GroupLayout.PREFERRED_SIZE, 76, GroupLayout.PREFERRED_SIZE))
                .add(GroupLayout.TRAILING, layout.createSequentialGroup()
                .add(28, 28, 28)
                .add(layout.createParallelGroup(GroupLayout.LEADING)
                .add(jCheckBox2, GroupLayout.DEFAULT_SIZE, 362, Short.MAX_VALUE)
                .add(jCheckBox1, GroupLayout.DEFAULT_SIZE, 362, Short.MAX_VALUE)
                .add(jCheckBox3, GroupLayout.DEFAULT_SIZE, 362, Short.MAX_VALUE)))
                .add(GroupLayout.TRAILING, layout.createSequentialGroup()
                .addContainerGap(28, Short.MAX_VALUE)
                .add(jCheckBox4, GroupLayout.PREFERRED_SIZE, 362, GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
                .add(GroupLayout.TRAILING, layout.createSequentialGroup()
                .add(16, 16, 16)
                .add(jLabel5)
                .addPreferredGap(LayoutStyle.RELATED)
                .add(jComboBox1)
                .addContainerGap())
                );
        layout.setVerticalGroup(
                layout.createParallelGroup(GroupLayout.LEADING)
                .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(jLabel2, GroupLayout.PREFERRED_SIZE, 23, GroupLayout.PREFERRED_SIZE)
                .add(16, 16, 16)
                .add(layout.createParallelGroup(GroupLayout.BASELINE)
                .add(jLabel1)
                .add(jTextField1, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(LayoutStyle.RELATED)
                .add(layout.createParallelGroup(GroupLayout.BASELINE)
                .add(jLabel3)
                .add(jTextField2, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .add(jLabel4)
                .add(jTextField3, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(LayoutStyle.RELATED, 15, Short.MAX_VALUE)
                .add(layout.createParallelGroup(GroupLayout.BASELINE)
                .add(jLabel5, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .add(jComboBox1))
                .addPreferredGap(LayoutStyle.RELATED, 15, Short.MAX_VALUE)
                .add(jCheckBox4)
                .addPreferredGap(LayoutStyle.RELATED)
                .add(jCheckBox1)
                .addPreferredGap(LayoutStyle.RELATED)
                .add(jCheckBox2)
                .addPreferredGap(LayoutStyle.RELATED)
                .add(jCheckBox3)
                .add(17, 17, 17)
                .add(layout.createParallelGroup(GroupLayout.BASELINE)
                .add(jButton1)
                .add(jButton2))
                .addContainerGap())
                );
        pack();
    }
    
}

/**
 * Verifies that the port number is a valid port.
 */
class PortVerifier extends InputVerifier {
    
    /** The minimum port number. */
    private static final int MIN_PORT = 0;
    
    /** The maximum port number. */
    private static final int MAX_PORT = 65535;
    
    /**
     * Creates a new instance of PortVerifier.
     */
    public PortVerifier() {
        
    }
    
    /**
     * Verifies that the number specified in the textfield is a valid port.
     * @param jComponent The component to be tested
     * @return true iff the number is a valid port, false otherwise
     */
    public boolean verify(final JComponent jComponent) {
        final JTextField textField = (JTextField) jComponent;
        try {
            final int port = Integer.parseInt(textField.getText());
            return port > MIN_PORT && port <= MAX_PORT;
        } catch (NumberFormatException e) {
            return false;
        }
    }
    
}
