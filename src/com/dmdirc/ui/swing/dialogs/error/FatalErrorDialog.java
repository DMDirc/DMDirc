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

package com.dmdirc.ui.swing.dialogs.error;

import com.dmdirc.IconManager;
import com.dmdirc.Main;
import com.dmdirc.logger.ErrorReportStatus;
import com.dmdirc.logger.ProgramError;
import static com.dmdirc.ui.swing.UIUtilities.LARGE_BORDER;
import static com.dmdirc.ui.swing.UIUtilities.SMALL_BORDER;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.Timer;
import java.util.TimerTask;

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
 * @author chris
 */
public final class FatalErrorDialog extends JDialog implements ActionListener,
        WindowListener {
    
    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 2;
    
    /** error. */
    private final ProgramError error;
    
    /** button. */
    private JButton okButton;
    
    /** label. */
    private JLabel infoLabel;
    
    /** label. */
    private JTextArea messageLabel;
    
    /** label. */
    private JButton showMore;
    
    /** stack trace scroll pane. */
    private JScrollPane scrollPane;
    
    /**
     * Creates a new fatal error dialog.
     *
     * @param error Error
     */
    public FatalErrorDialog(final ProgramError error) {
        super();
        
        setModal(true);
        
        this.error = error;
        
        initComponents();
        layoutComponents();
        
        setLocationRelativeTo(getParent());
        setVisible(true);
    }
    
    /**
     * Initialises the components for this dialog.
     */
    private void initComponents() {
        final JTextArea stacktraceField = new JTextArea();
        
        messageLabel = new JTextArea();
        infoLabel = new JLabel();
        showMore = new JButton();
        scrollPane = new JScrollPane();
        okButton = new JButton();
        
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        
        setTitle("DMDirc: Error");
        
        infoLabel.setText("DMDirc has encountered a fatal error, it is "
                + "unable to recover from this error and will terminate.");
        
        infoLabel.setIcon(IconManager.getIconManager().getIcon("error"));
        
        messageLabel = new JTextArea("Description: \n" + error.getMessage());
        messageLabel.setEditable(false);
        messageLabel.setWrapStyleWord(true);
        messageLabel.setLineWrap(true);
        messageLabel.setHighlighter(null);
        messageLabel.setBackground(getContentPane().getBackground());
        
        showMore.setText("Show details");
        showMore.addActionListener(this);
        
        stacktraceField.setColumns(20);
        stacktraceField.setEditable(false);
        stacktraceField.setRows(5);
        
        final String[] trace = error.getTrace();
        if (trace.length > 0) {
            for (String line : trace) {
                stacktraceField.append(line + "\n");
            }
            stacktraceField.setCaretPosition(0);
        }
        
        scrollPane.setViewportView(stacktraceField);
        scrollPane.setMinimumSize(new Dimension(600, 200));
        scrollPane.setPreferredSize(new Dimension(600, 200));
        scrollPane.setVisible(false);
        
        okButton.setText("OK");
        okButton.setPreferredSize(new Dimension(100, 25));
        
        okButton.addActionListener(this);
        addWindowListener(this);
    }
    
    /**
     * lays the components out in the dialog.
     */
    private void layoutComponents() {
        final GridBagConstraints constraints = new GridBagConstraints();
        
        getContentPane().setLayout(new GridBagLayout());
        
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.gridwidth = 3;
        constraints.weightx = 1.0;
        constraints.weighty = 0.0;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.insets = new Insets(LARGE_BORDER, LARGE_BORDER,
                LARGE_BORDER, LARGE_BORDER);
        getContentPane().add(infoLabel, constraints);
        
        constraints.insets = new Insets(0, LARGE_BORDER, SMALL_BORDER,
                LARGE_BORDER);
        constraints.gridy = 1;
        getContentPane().add(messageLabel, constraints);
        
        constraints.insets = new Insets(0, LARGE_BORDER, SMALL_BORDER,
                LARGE_BORDER);
        constraints.gridx = 0;
        constraints.gridy = 2;
        constraints.gridwidth = 3;
        getContentPane().add(showMore, constraints);
        
        constraints.insets = new Insets(0, LARGE_BORDER, 0, LARGE_BORDER);
        constraints.gridx = 0;
        constraints.gridwidth = 3;
        constraints.weightx = 1.0;
        constraints.weighty = 1.0;
        constraints.gridy = 3;
        constraints.fill = GridBagConstraints.BOTH;
        getContentPane().add(scrollPane, constraints);
        
        constraints.insets = new Insets(0, LARGE_BORDER, SMALL_BORDER,
                LARGE_BORDER);
        constraints.weightx = 0.0;
        constraints.weighty = 0.0;
        constraints.gridx = 0;
        constraints.gridy = 4;
        constraints.gridwidth = 1;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        getContentPane().add(Box.createHorizontalGlue(), constraints);
        
        constraints.insets.set(LARGE_BORDER, LARGE_BORDER, LARGE_BORDER,
                LARGE_BORDER);
        constraints.gridx = 2;
        constraints.anchor = GridBagConstraints.EAST;
        constraints.fill = GridBagConstraints.NONE;
        getContentPane().add(okButton, constraints);
        pack();
    }
    
    /**
     * Exits the program. {@inheritDoc}
     */
    public void actionPerformed(final ActionEvent actionEvent) {
        if (actionEvent.getSource() == showMore) {
            if (showMore.getText().equals("Show details")) {
                scrollPane.setVisible(true);
                showMore.setText("Hide details");
                pack();
                setLocationRelativeTo(getParent());
            } else {
                scrollPane.setVisible(false);
                showMore.setText("Show details");
                pack();
                setLocationRelativeTo(getParent());
            }
        } else {
            closeDialog();
        }
    }
    
    /** Closes the dialog, and the client. */
    public void closeDialog() {
        setVisible(false);
        new Timer("Fatal Error Dialog Timer").schedule(new TimerTask() {
            public void run() {
                Main.getUI().getMainWindow().setVisible(false);
                while (error.getReportStatus() != ErrorReportStatus.FINISHED) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException ex) {
                        //Ignore
                    }
                }
                System.exit(-1);
            }
        }, 0);
    }
    
    /** {@inheritDoc} */
    public void windowOpened(final WindowEvent e) {
        //ignore
    }
    
    /** {@inheritDoc} */
    public void windowClosing(final WindowEvent e) {
        closeDialog();
    }
    
    /** {@inheritDoc} */
    public void windowClosed(final WindowEvent e) {
        //ignore
    }
    
    /** {@inheritDoc} */
    public void windowIconified(final WindowEvent e) {
        //ignore
    }
    
    /** {@inheritDoc} */
    public void windowDeiconified(final WindowEvent e) {
        //ignore
    }
    
    /** {@inheritDoc} */
    public void windowActivated(final WindowEvent e) {
        //ignore
    }
    
    /** {@inheritDoc} */
    public void windowDeactivated(final WindowEvent e) {
        //ignore
    }
}
