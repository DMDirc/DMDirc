/*
 * Copyright (c) 2006-2009 Chris Smith, Shane Mc Cormack, Gregory Holmes
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
package com.dmdirc.ui;

import com.dmdirc.logger.ErrorListener;
import com.dmdirc.logger.ErrorManager;
import com.dmdirc.logger.ErrorReportStatus;
import com.dmdirc.logger.ProgramError;

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import java.util.concurrent.Semaphore;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.WindowConstants;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

/**
 * The fatal error dialog is used to inform the user that a fatal error has
 * occured.
 */
public final class FatalErrorDialog extends JDialog implements ActionListener,
        ErrorListener {

    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 3;
    /** error. */
    private final ProgramError error;
    /** button. */
    private JButton okButton;
    /** button. */
    private JButton sendButton;
    /** info label. */
    private JTextPane infoLabel;
    /** message label. */
    private JTextPane messageLabel;
    /** Icon. */
    private ImageIcon icon;
    /** stack trace scroll pane. */
    private JScrollPane scrollPane;

    /**
     * Creates a new fatal error dialog.
     *
     * @param error Error
     */
    private FatalErrorDialog(final ProgramError error) {
        super(null, Dialog.ModalityType.TOOLKIT_MODAL);

        setModal(true);

        this.error = error;

        initComponents();
        layoutComponents();
        
        ErrorManager.getErrorManager().addErrorListener(this);
        
        setResizable(false);
        CoreUIUtils.centreWindow(this);
    }

    /**
     * Initialises the components for this dialog.
     */
    private void initComponents() {
        final JTextArea stacktraceField = new JTextArea();

        infoLabel = new JTextPane(new DefaultStyledDocument());
        infoLabel.setOpaque(false);
        infoLabel.setEditable(false);
        infoLabel.setHighlighter(null);
        messageLabel = new JTextPane(new DefaultStyledDocument());
        messageLabel.setOpaque(false);
        messageLabel.setEditable(false);
        messageLabel.setHighlighter(null);
        final SimpleAttributeSet sas = new SimpleAttributeSet();
        StyleConstants.setAlignment(sas, StyleConstants.ALIGN_JUSTIFIED);

        scrollPane = new JScrollPane();
        okButton = new JButton();
        sendButton = new JButton();

        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("DMDirc: Fatal Error");
        setIconImage(IconManager.getIconManager().getImage("icon"));

        infoLabel.setText("DMDirc has encountered a fatal error, and is " +
                "not able to recover. \nThe application will now terminate.");
        messageLabel.setText("Description: " + error.getMessage());
        ((StyledDocument) infoLabel.getDocument()).setParagraphAttributes(0,
                infoLabel.getText().length(), sas, false);
        ((StyledDocument) messageLabel.getDocument()).setParagraphAttributes(0,
                messageLabel.getText().length(), sas, false);

        icon = new ImageIcon(IconManager.getIconManager().getImage("error"));

        stacktraceField.setEditable(false);

        final String[] trace = error.getTrace();
        if (trace.length > 0) {
            for (String line : trace) {
                stacktraceField.append(line + "\n");
            }
            stacktraceField.setCaretPosition(0);
        }

        scrollPane.setViewportView(stacktraceField);

        okButton.setText("OK");
        sendButton.setText("Send");

        final ErrorReportStatus status = error.getReportStatus();
        okButton.setEnabled(status.isTerminal());
        updateSendButtonText(status);

        okButton.addActionListener(this);
        sendButton.addActionListener(this);
    }

    /**
     * lays the components out in the dialog.
     */
    private void layoutComponents() {
        final JPanel panel = new JPanel();
        final JPanel blurb = new JPanel();
        final JPanel info = new JPanel();
        final JPanel buttons = new JPanel();
        blurb.setLayout(new BorderLayout(5, 5));
        info.setLayout(new BorderLayout(5, 5));
        buttons.setLayout(new BoxLayout(buttons, BoxLayout.LINE_AXIS));

        blurb.add(new JLabel(icon), BorderLayout.LINE_START);
        blurb.add(infoLabel, BorderLayout.CENTER);

        info.add(messageLabel, BorderLayout.NORTH);
        info.add(scrollPane, BorderLayout.CENTER);

        buttons.add(Box.createHorizontalGlue());
        buttons.add(sendButton);
        buttons.add(Box.createHorizontalStrut(5));
        buttons.add(okButton);

        panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        panel.setLayout(new BorderLayout(5, 5));
        panel.add(blurb, BorderLayout.NORTH);
        panel.add(info, BorderLayout.CENTER);
        panel.add(buttons, BorderLayout.SOUTH);

        getContentPane().add(panel);

        setSize(new Dimension(550, 260));
    }

    /**
     * Exits the program. {@inheritDoc}
     * 
     * @param actionEvent Action event.
     */
    @Override
    public void actionPerformed(final ActionEvent actionEvent) {
        if (actionEvent.getSource() == sendButton) {
            sendButton.setText("Sending...");
            okButton.setEnabled(false);
            sendButton.setEnabled(false);
            new SwingWorker() {

                /** {@inheritDoc} */
                @Override
                protected Object doInBackground() throws Exception {
                    ErrorManager.getErrorManager().sendError(error);
                    return null;
                }

                /** {@inheritDoc} */
                @Override
                protected void done() {
                    super.done();
                }
            }.execute();
        } else {
            dispose();
        }
    }

    /**
     * Static method to instantiate and display the dialog.
     *
     * @param error Program error
     */
    public static void display(final ProgramError error) {
        SwingUtilities.invokeLater(new Runnable() {

            /** {@inheritDoc} */
            @Override
            public void run() {
                final FatalErrorDialog me = new FatalErrorDialog(error);
                me.setVisible(true);
            }
        });
    }

    /**
     * Static method to instantiate and display the dialog, blocking until it
     * is closed.
     * 
     * @param error Program error
     */
    public static void displayBlocking(final ProgramError error) {
        final Semaphore semaphore = new Semaphore(0);
        SwingUtilities.invokeLater(new Runnable() {

            /** {@inheritDoc} */
            @Override
            public void run() {
                final FatalErrorDialog me = new FatalErrorDialog(error);
                me.addWindowListener(new WindowAdapter() {

                    @Override
                    public void windowClosed(final WindowEvent e) {
                        semaphore.release();
                    }
                });
                me.setVisible(true);
            }
        });
        semaphore.acquireUninterruptibly();
    }

    private void updateSendButtonText(final ErrorReportStatus status) {
        switch (status) {
            case WAITING:
                sendButton.setText("Send");
                sendButton.setEnabled(true);
                break;
            case QUEUED:
                sendButton.setText("Queued");
                sendButton.setEnabled(false);
                break;
            case SENDING:
                sendButton.setText("Sending");
                sendButton.setEnabled(false);
                break;
            case ERROR:
                sendButton.setText("Error, resend");
                sendButton.setEnabled(true);
                break;
            case FINISHED:
                sendButton.setText("Sent");
                sendButton.setEnabled(false);
                break;
            case NOT_APPLICABLE:
                sendButton.setText("N/A");
                sendButton.setEnabled(false);
                break;
            default:
                sendButton.setText("Send");
                sendButton.setEnabled(true);
                break;
        }
    }

    /** {@inheritDoc} */
    @Override
    public void errorAdded(final ProgramError error) {
        //Ignore
    }

    /** {@inheritDoc} */
    @Override
    public void errorDeleted(final ProgramError error) {
        //Ignore
    }

    /** {@inheritDoc} */
    @Override
    public void errorStatusChanged(final ProgramError error) {
        if (this.error.equals(error)) {
            final ErrorReportStatus status = error.getReportStatus();
            okButton.setEnabled(status.isTerminal());   
            updateSendButtonText(status);
        }
    }

    /** {@inheritDoc} */
    @Override
    public boolean isReady() {
        //We're never ready
        return false;
    }
}
