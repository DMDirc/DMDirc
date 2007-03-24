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
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.WindowConstants;

import uk.org.ownage.dmdirc.ui.interfaces.StatusErrorNotifier;

/**
 * The fatal error dialog is used to inform the user that a fatal error has
 * occured.
 * @author  chris
 */
public final class ErrorDialog extends JDialog implements ActionListener,
        StatusErrorNotifier {
    
    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 1;
    
    /** Size of the large borders in the dialog. */
    private static final int LARGE_BORDER = 10;
    
    /** Size of the small borders in the dialog. */
    private static final int SMALL_BORDER = 5;
    
    /** button. */
    private JButton okButton;
    
    /** label. */
    private JLabel infoLabel;
    
    /** label. */
    private JLabel detailLabel;
    
    /** Scroll pane. */
    private JScrollPane detailsScrollPane;
    
    /** text area. */
    private JTextArea detailsField;
    
    /**
     * Creates a new fatal error dialog.
     * @param parent The parent frame
     * @param modal Whether this dialog is modal or not
     * @param message The message (error info) to be displayed
     */
    public ErrorDialog(final Frame parent, final boolean modal,
            final String[] message) {
        super(parent, modal);
        initComponents();
        for (String line : message) {
            detailsField.append(line + "\r\n");
        }
        detailsField.setCaretPosition(0);
    }
    
    /**
     * Initialises the components for this dialog.
     */
    private void initComponents() {
        final GridBagConstraints constraints = new GridBagConstraints();
        
        infoLabel = new JLabel();
        detailLabel = new JLabel();
        detailsScrollPane = new JScrollPane();
        detailsField = new JTextArea();
        okButton = new JButton();
        
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("DMDirc - an error occured");
        
        infoLabel.setText("DMDirc has encountered a error.");
        
        detailLabel.setText("Error details:");
        
        detailsField.setColumns(20);
        detailsField.setEditable(false);
        detailsField.setRows(5);
        detailsScrollPane.setViewportView(detailsField);
        detailsScrollPane.setPreferredSize(new Dimension(700, 300));
        
        okButton.setText("OK");
        okButton.setPreferredSize(new Dimension(100, 25));
        okButton.addActionListener(this);
        
        getContentPane().setLayout(new GridBagLayout());
        
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.gridwidth = 3;
        constraints.weightx = 0.0;
        constraints.weighty = 1.0;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.insets = new Insets(LARGE_BORDER, LARGE_BORDER,
                LARGE_BORDER, LARGE_BORDER);
        getContentPane().add(infoLabel, constraints);
        
        constraints.insets = new Insets(0, LARGE_BORDER, SMALL_BORDER,
                LARGE_BORDER);
        constraints.gridy = 1;
        getContentPane().add(detailLabel, constraints);
        
        constraints.insets = new Insets(0, LARGE_BORDER, 0, LARGE_BORDER);
        constraints.weightx = 1.0;
        constraints.gridy = 2;
        getContentPane().add(detailsScrollPane, constraints);
        
        constraints.weighty = 0.0;
        constraints.gridx = 0;
        constraints.gridy = 3;
        constraints.gridwidth = 1;
        getContentPane().add(Box.createHorizontalGlue(), constraints);
        
        constraints.weightx = 0.0;
        constraints.insets.set(LARGE_BORDER, LARGE_BORDER, LARGE_BORDER,
                LARGE_BORDER);
        constraints.gridx = 1;
        constraints.gridy = 5;
        constraints.anchor = GridBagConstraints.EAST;
        constraints.fill = GridBagConstraints.NONE;
        getContentPane().add(okButton, constraints);
        pack();
    }
    
    /**
     * Exits the program. {@inheritDoc}
     */
    public void actionPerformed(final ActionEvent actionEvent) {
        this.setVisible(false);
    }

    public void clickReceived(MouseEvent mouseEvent) {
        this.setVisible(true);
    }
    
}
