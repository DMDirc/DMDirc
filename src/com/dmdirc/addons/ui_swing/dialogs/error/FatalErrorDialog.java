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

package com.dmdirc.addons.ui_swing.dialogs.error;

import com.dmdirc.ui.IconManager;
import com.dmdirc.Main;
import com.dmdirc.logger.ErrorReportStatus;
import com.dmdirc.logger.ProgramError;
import com.dmdirc.addons.ui_swing.components.TextLabel;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.WindowConstants;

import net.miginfocom.swing.MigLayout;

/**
 * The fatal error dialog is used to inform the user that a fatal error has
 * occured.
 */
public final class FatalErrorDialog extends JDialog implements ActionListener,
        WindowListener {

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
    /** label. */
    private TextLabel infoLabel;
    /** Icon. */
    private ImageIcon icon;
    /** label. */
    private TextLabel messageLabel;
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
        setResizable(false);
        setVisible(true);
    }

    /**
     * Initialises the components for this dialog.
     */
    private void initComponents() {
        final JTextArea stacktraceField = new JTextArea();

        messageLabel = new TextLabel();
        infoLabel = new TextLabel();
        showMore = new JButton();
        scrollPane = new JScrollPane();
        okButton = new JButton();

        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        setTitle("DMDirc: Error");
        setIconImage(IconManager.getIconManager().getImage("icon"));

        infoLabel.setText("DMDirc has encountered a fatal error, and is " +
                "not able to recover. The application will now terminate.");

        icon = new ImageIcon(IconManager.getIconManager().getImage("error"));

        messageLabel.setText("Description: \n" + error.getMessage());

        showMore.setText("Show details");
        showMore.addActionListener(this);

        stacktraceField.setEditable(false);

        final String[] trace = error.getTrace();
        if (trace.length > 0) {
            for (String line : trace) {
                stacktraceField.append(line + "\n");
            }
            stacktraceField.setCaretPosition(0);
        }

        scrollPane.setViewportView(stacktraceField);
        scrollPane.setVisible(false);

        okButton.setText("OK");

        okButton.addActionListener(this);
        addWindowListener(this);
    }

    /**
     * lays the components out in the dialog.
     */
    private void layoutComponents() {
        getContentPane().setLayout(new MigLayout("wrap 1, hidemode 3, wmin 600, wmax 600, pack"));
        getContentPane().add(new JLabel(icon), "growx, split 2");
        getContentPane().add(infoLabel, "growx");
        getContentPane().add(messageLabel, "growx");
        getContentPane().add(showMore, "growx");
        getContentPane().add(scrollPane, "grow, hmin 200, hmax 200");
        getContentPane().add(okButton, "right, tag ok");
        pack();
    }

    /**
     * Exits the program. {@inheritDoc}
     * 
     * @param actionEvent Action event.
     */
    @Override
    public void actionPerformed(final ActionEvent actionEvent) {
        if (actionEvent.getSource() == showMore) {
            if (showMore.getText().equals("Show details")) {
                scrollPane.setVisible(true);
                showMore.setText("Hide details");
            } else {
                scrollPane.setVisible(false);
                showMore.setText("Show details");
            }
        } else {
            closeDialog();
        }
    }

    /** Closes the dialog, and the client. */
    public void closeDialog() {
        dispose();
        new Timer("Fatal Error Dialog Timer").schedule(new TimerTask() {

            /** {@inheritDoc} */
            @Override
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

    /** 
     * {@inheritDoc}
     * 
     * @param e Window event
     */
    @Override
    public void windowOpened(final WindowEvent e) {
    //ignore
    }

    /** 
     * {@inheritDoc}
     * 
     * @param e Window event
     */
    @Override
    public void windowClosing(final WindowEvent e) {
        closeDialog();
    }

    /** 
     * {@inheritDoc}
     * 
     * @param e Window event
     */
    @Override
    public void windowClosed(final WindowEvent e) {
    //ignore
    }

    /** 
     * {@inheritDoc}
     * 
     * @param e Window event
     */
    @Override
    public void windowIconified(final WindowEvent e) {
    //ignore
    }

    /** 
     * {@inheritDoc}
     * 
     * @param e Window event
     */
    @Override
    public void windowDeiconified(final WindowEvent e) {
    //ignore
    }

    /** 
     * {@inheritDoc}
     * 
     * @param e Window event
     */
    @Override
    public void windowActivated(final WindowEvent e) {
    //ignore
    }

    /** 
     * {@inheritDoc}
     * 
     * @param e Window event
     */
    @Override
    public void windowDeactivated(final WindowEvent e) {
    //ignore
    }
}
