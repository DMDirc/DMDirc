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

import java.awt.Font;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.WindowConstants;

import org.jdesktop.layout.GroupLayout;
import org.jdesktop.layout.LayoutStyle;

/**
 * The fatal error dialog is used to inform the user that a fatal error has
 * occured.
 * @author  chris
 */
public class FatalErrorDialog extends JDialog implements ActionListener,
        WindowListener {
    
    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 2;
    
    /** button. */
    private JButton jButton1;
    
    /** button. */
    private JLabel jLabel1;
    
    /** button. */
    private JLabel jLabel2;
    
    /** button. */
    private JLabel jLabel3;
    
    /** button. */
    private JScrollPane jScrollPane1;
    
    /** button. */
    private JTextArea jTextArea1;
    
    /**
     * Creates a new fatal error dialog.
     * @param parent The parent frame
     * @param modal Whether this dialog is modal or not
     * @param message The message (error info) to be displayed
     */
    public FatalErrorDialog(final Frame parent, final boolean modal,
            final String[] message) {
        super(parent, modal);
        initComponents();
        for (String line : message) {
            jTextArea1.append(line + "\r\n");
        }
        jTextArea1.setCaretPosition(0);
    }
    
    /**
     * Initialises the components for this dialog.
     */
    private void initComponents() {
        jLabel1 = new JLabel();
        jLabel2 = new JLabel();
        jLabel3 = new JLabel();
        jScrollPane1 = new JScrollPane();
        jTextArea1 = new JTextArea();
        jButton1 = new JButton();
        
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("DMDirc - an error occured");
        addWindowListener(this);
        
        jLabel1.setFont(new Font("Dialog", 1, 18));
        jLabel1.setText("We're sorry...");
        
        jLabel2.setText("DMDirc has encountered a fatal error and cannot continue.");
        
        jLabel3.setText("Error details:");
        
        jTextArea1.setColumns(20);
        jTextArea1.setEditable(false);
        jTextArea1.setRows(5);
        jScrollPane1.setViewportView(jTextArea1);
        
        jButton1.setText("OK");
        jButton1.addActionListener(this);
        
        final GroupLayout layout = new GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(GroupLayout.LEADING)
                .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(GroupLayout.LEADING)
                .add(jScrollPane1, GroupLayout.DEFAULT_SIZE, 443, Short.MAX_VALUE)
                .add(jLabel1)
                .add(GroupLayout.TRAILING, jButton1, GroupLayout.PREFERRED_SIZE, 100, GroupLayout.PREFERRED_SIZE)
                .add(jLabel3)
                .add(jLabel2))
                .addContainerGap())
                );
        layout.setVerticalGroup(
                layout.createParallelGroup(GroupLayout.LEADING)
                .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(jLabel1)
                .add(23, 23, 23)
                .add(jLabel2)
                .add(21, 21, 21)
                .add(jLabel3)
                .addPreferredGap(LayoutStyle.RELATED)
                .add(jScrollPane1, GroupLayout.PREFERRED_SIZE, 246, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(LayoutStyle.RELATED, 9, Short.MAX_VALUE)
                .add(jButton1)
                .addContainerGap())
                );
        pack();
    }
    
    public void actionPerformed(ActionEvent actionEvent) {
        System.exit(-1);
    }

    public void windowOpened(WindowEvent windowEvent) {
    }

    public void windowClosing(WindowEvent windowEvent) {
    }

    public void windowClosed(WindowEvent windowEvent) {
    }

    public void windowIconified(WindowEvent windowEvent) {
    }

    public void windowDeiconified(WindowEvent windowEvent) {
    }

    public void windowActivated(WindowEvent windowEvent) {
    }

    public void windowDeactivated(WindowEvent windowEvent) {
    }
    
}
