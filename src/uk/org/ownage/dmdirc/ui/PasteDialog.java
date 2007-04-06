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

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.WindowConstants;
import javax.swing.border.EtchedBorder;

import uk.org.ownage.dmdirc.ui.components.StandardDialog;
import uk.org.ownage.dmdirc.ui.components.Frame;

import static uk.org.ownage.dmdirc.ui.UIConstants.*;

/**
 * Allows the user to modify global client preferences.
 */
public final class PasteDialog extends StandardDialog implements ActionListener {
    
    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 1;
    
    /** Text area. */
    private JTextArea textField;
    
    /** parent frame. */
    private Frame parent;
    
    /**
     * Creates a new instance of PreferencesDialog.
     * @param parent The frame that owns this dialog
     * @param modal Whether to show modally or not
     */
    public PasteDialog(Frame newParent, String text) {
        super(MainFrame.getMainFrame(), false);
        
        this.parent = newParent;
        
        initComponents(text);
        initListeners();
        setLocationRelativeTo(MainFrame.getMainFrame());
    }
    
    /**
     * Initialises GUI components.
     */
    private void initComponents(String text) {
        final GridBagConstraints constraints = new GridBagConstraints();
        textField = new JTextArea(text);
        JScrollPane scrollPane = new JScrollPane();
        
        orderButtons(new JButton(), new JButton());
        
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        getContentPane().setLayout(new GridBagLayout());
        setTitle("Multi-line paste");
        setResizable(true);
        
        textField.setBorder(new EtchedBorder(EtchedBorder.LOWERED));
        textField.setColumns(50);
        textField.setRows(10);
        scrollPane.setViewportView(textField);
        
        constraints.insets.set(LARGE_BORDER, LARGE_BORDER, LARGE_BORDER, LARGE_BORDER);
        constraints.weightx = 1.0;
        constraints.weighty = 1.0;
        constraints.fill = GridBagConstraints.BOTH;
        constraints.gridwidth = 3;
        constraints.gridx = 0;
        constraints.gridy = 0;
        getContentPane().add(scrollPane, constraints);
        
        constraints.weighty = 0.0;
        constraints.weightx = 1.0;
        constraints.gridy = 1;
        constraints.gridwidth = 1;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        getContentPane().add(Box.createHorizontalGlue(), constraints);
        
        constraints.insets.set(LARGE_BORDER, 0, LARGE_BORDER, LARGE_BORDER);
        constraints.weightx = 0.0;
        constraints.gridx = 1;
        constraints.anchor = GridBagConstraints.EAST;
        constraints.fill = GridBagConstraints.NONE;
        getContentPane().add(getLeftButton(), constraints);
        
        constraints.gridx = 2;
        getContentPane().add(getRightButton(), constraints);
        
        pack();
    }
    
    /**
     * Initialises listeners for this dialog.
     */
    private void initListeners() {
        getOkButton().addActionListener(this);
        getCancelButton().addActionListener(this);
    }
    
    /**
     * Handles the actions for the dialog.
     *
     * @param actionEvent Action event
     */
    public void actionPerformed(final ActionEvent actionEvent) {
        if (getOkButton().equals(actionEvent.getSource())) {
            String[] lines = textField.getText().split(System.getProperty("line.separator"));
            for (String line : lines) {
                parent.sendLine(line);
            }
            parent.sendLine(textField.getText());
            this.dispose();
        } else if (getCancelButton().equals(actionEvent.getSource())) {
            this.dispose();
        }
    }
}
