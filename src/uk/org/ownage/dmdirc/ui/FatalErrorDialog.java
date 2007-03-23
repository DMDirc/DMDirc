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

import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.Box;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.WindowConstants;

/**
 * The fatal error dialog is used to inform the user that a fatal error has
 * occured.
 * @author  chris
 */
public final class FatalErrorDialog extends JDialog implements ActionListener {
    
    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 2;
    
    /** Size of the large borders in the dialog. */
    private static final int LARGE_BORDER = 10;
    
    /** Size of the small borders in the dialog. */
    private static final int SMALL_BORDER = 5;
    
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
        final GridBagConstraints constraints = new GridBagConstraints();
        
        jLabel1 = new JLabel();
        jLabel2 = new JLabel();
        jLabel3 = new JLabel();
        jScrollPane1 = new JScrollPane();
        jTextArea1 = new JTextArea();
        jButton1 = new JButton();
        
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("DMDirc - an error occured");
        
        jLabel1.setFont(new Font("Dialog", 1, 18));
        jLabel1.setText("We're sorry...");
        
        jLabel2.setText("DMDirc has encountered a fatal error and cannot continue.");
        
        jLabel3.setText("Error details:");
        
        jTextArea1.setColumns(20);
        jTextArea1.setEditable(false);
        jTextArea1.setRows(5);
        jScrollPane1.setViewportView(jTextArea1);
        jScrollPane1.setPreferredSize(new Dimension(700, 300));
        
        jButton1.setText("OK");
        jButton1.setPreferredSize(new Dimension(100, 25));
        jButton1.addActionListener(this);
        
        getContentPane().setLayout(new GridBagLayout());
        
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.gridwidth = 3;
        constraints.weightx = 0.0;
        constraints.weighty = 1.0;
        constraints.fill = GridBagConstraints.BOTH;
        constraints.insets = new Insets(LARGE_BORDER, LARGE_BORDER,
                LARGE_BORDER, LARGE_BORDER);
        getContentPane().add(jLabel1, constraints);
        constraints.insets = new Insets(0, LARGE_BORDER, LARGE_BORDER, 
                LARGE_BORDER);
        constraints.gridy = 1;
        getContentPane().add(jLabel2, constraints);
        constraints.insets = new Insets(0, LARGE_BORDER, SMALL_BORDER, 
                LARGE_BORDER);
        constraints.gridy = 2;
        getContentPane().add(jLabel3, constraints);
        constraints.insets = new Insets(0, LARGE_BORDER, 0, LARGE_BORDER);
        constraints.weightx = 1.0;
        constraints.gridy = 3;
        getContentPane().add(jScrollPane1, constraints);
        
        constraints.weighty = 0.0;
        constraints.gridx = 0;
        constraints.gridy = 4;
        constraints.gridwidth = 1;
        getContentPane().add(Box.createHorizontalGlue(), constraints);
        
        constraints.weightx = 0.0;
        constraints.insets.set(LARGE_BORDER, LARGE_BORDER, LARGE_BORDER, 
                LARGE_BORDER);
        constraints.gridx = 1;
        constraints.gridy = 5;
        constraints.anchor = GridBagConstraints.EAST;
        constraints.fill = GridBagConstraints.NONE;
        getContentPane().add(jButton1, constraints);
        pack();
    }
    
    /**
     * Exits the program. {@inheritDoc}
     */
    public void actionPerformed(final ActionEvent actionEvent) {
        System.exit(-1);
    }
    
}
