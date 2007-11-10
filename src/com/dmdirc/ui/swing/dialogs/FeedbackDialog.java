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

package com.dmdirc.ui.swing.dialogs;

import com.dmdirc.Main;
import com.dmdirc.Server;
import com.dmdirc.ui.swing.MainFrame;
import com.dmdirc.ui.swing.UIUtilities;
import com.dmdirc.ui.swing.components.StandardDialog;
import com.dmdirc.ui.swing.components.TextLabel;
import com.dmdirc.util.Downloader;

import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.BorderFactory;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingWorker;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import net.miginfocom.swing.MigLayout;

/** Feedback form. */
public class FeedbackDialog extends StandardDialog implements ActionListener,
        DocumentListener {

    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 1;
    /** A previously created instance of FeedbackDialog. */
    private static FeedbackDialog me;
    /** Information label. */
    private TextLabel info;
    /** Name field. */
    private JTextField name;
    /** Email field. */
    private JTextField email;
    /** Feedback area. */
    private JTextArea feedback;
    /** Server info checkbox. */
    private JCheckBox serverCheckbox;
    /** Server instance. */
    private Server server;

    /** Instantiates the feedback dialog. */
    private FeedbackDialog() {
        super((MainFrame) Main.getUI().getMainWindow(), false);

        initComponents();
        layoutComponents();
        addListeners();

        setPreferredSize(new Dimension(600, 400));

        setTitle("DMDirc: Feedback");

        pack();
    }

    /**
     * Creates the new feedback dialog if one doesn't exist, and displays it.
     */
    public static synchronized void showFeedbackDialog() {
        me = getFeedbackDialog();

        me.setLocationRelativeTo((MainFrame) Main.getUI().getMainWindow());
        me.setVisible(true);
        me.requestFocus();
    }

    /**
     * Returns the current instance of the FeedbackDialog.
     *
     * @return The current FeedbackDialog instance
     */
    public static synchronized FeedbackDialog getFeedbackDialog() {
        if (me == null) {
            me = new FeedbackDialog();
            me.serverCheckbox.setEnabled(me.server != null);
        }

        return me;
    }

    /** Initialises the components. */
    private void initComponents() {
        server = Main.getUI().getActiveServer();
        orderButtons(new JButton(), new JButton());

        getOkButton().setText("Send");
        getOkButton().setActionCommand("Send");
        getOkButton().setEnabled(false);
        getCancelButton().setActionCommand("Close");

        info =  new TextLabel("This is a feedback dialog, if you fancy " +
                "sending us some feedback fill in the form below, only the " +
                "feedback field is required, but if you want us to get back " +
                "to you the email address is pretty vital and the name " +
                "fairly useful.");
        name = new JTextField();
        email = new JTextField();
        feedback = new JTextArea();
        serverCheckbox = new JCheckBox("Check here to include some server information.");

        UIUtilities.addUndoManager(name);
        UIUtilities.addUndoManager(email);
        UIUtilities.addUndoManager(feedback);
    }

    /** Lays out the components. */
    private void layoutComponents() {
        serverCheckbox.setMargin(new Insets(0, 0, 0, 0));
        serverCheckbox.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        
        setLayout(new MigLayout("fill"));

        add(info, "span 3, growx, wrap");

        add(new JLabel("Name: "));
        add(name, "span 2, growx, wrap");

        add(new JLabel("Email: "));
        add(email, "span 2, growx, wrap");

        add(new JLabel("Feedback: "));
        add(new JScrollPane(feedback), "span 2, grow, pushx, wrap");
        add(serverCheckbox, "skip 1, span 2, growx, wrap");

        add(getCancelButton(), "skip, right, tag cancel, sg button");
        add(getOkButton(), "tag ok, sg button");
    }

    /**
     * Lays out the components.
     *
     * @param error Did the submission error?
     */
    protected void layoutComponents2(final StringBuilder error) {
        getContentPane().setVisible(false);
        getContentPane().removeAll();
        getOkButton().setEnabled(true);
        getOkButton().setText("Close");
        getOkButton().setActionCommand("Close");

        setLayout(new MigLayout("fill"));

        info.setText(error.toString());

        add(info, "span 3, grow, wrap");

        add(getOkButton(), "skip, right, tag ok, sg button");
        getContentPane().setVisible(true);
    }

    /** Adds listeners to the components. */
    private void addListeners() {
        getOkButton().addActionListener(this);
        getCancelButton().addActionListener(this);
        feedback.getDocument().addDocumentListener(this);
    }

    /** Checks and sends the feedback. */
    private void send() {
        getOkButton().setEnabled(false);
        getCancelButton().setEnabled(false);
        final SendWorker worker;
        if (serverCheckbox.isSelected()) {
            worker =new SendWorker(me, name.getText().trim(),
                    email.getText().trim(), feedback.getText().trim());
        } else {
            worker =new SendWorker(me, name.getText().trim(),
                    email.getText().trim(), feedback.getText().trim(), true,
                    server.getName(), server.getNetwork(), server.getIrcd());
        }
        worker.execute();
    }

    /** Validates the input. */
    private void validateInput() {
        if (feedback.getDocument().getLength() > 0) {
            getOkButton().setEnabled(true);
        } else {
            getOkButton().setEnabled(false);
        }
    }

    /**
     * {@inheritDoc}
     *
     * @param e action event
     */
    @Override
    public void actionPerformed(final ActionEvent e) {
        if (e.getActionCommand().equals("Send")) {
            send();
        } else if (e.getActionCommand().equals("Close")) {
            dispose();
        }
    }

    /** {@inheritDoc} */
    @Override
    public void dispose() {
        synchronized (me) {
            super.dispose();
            me = null;
        }
    }

    /** {@inheritDoc} */
    @Override
    public void insertUpdate(DocumentEvent e) {
        validateInput();
    }

    /** {@inheritDoc} */
    @Override
    public void removeUpdate(DocumentEvent e) {
        validateInput();
    }

    /** {@inheritDoc} */
    @Override
    public void changedUpdate(DocumentEvent e) {
        //Ignore
    }
}

/**
 * Sends feedback worker thread.
 */
class SendWorker extends SwingWorker {

    /** Parent feedback dialog. */
    private FeedbackDialog dialog;
    /** Name. */
    private String name;
    /** Email. */
    private String email;
    /** Feedback. */
    private String feedback;
    /** Send server info. */
    private boolean sendServerInfo;
    /** Server name. */
    private String serverName;
    /** Server network. */
    private String serverNetwork;
    /** Server IRCD. */
    private String serverIRCD;
    /** Error/Success message. */
    private StringBuilder error;

    /**
     * Creates a new send worker to send feedback.
     * 
     * @param dialog Parent feedback dialog
     * @param name Name
     * @param email Email
     * @param feedback Feedback
     */
    public SendWorker(FeedbackDialog dialog, String name,
            String email, String feedback) {
        this(dialog, name, email, feedback, false, "", "", "");
    }

    /**
     * Creates a new send worker to send feedback.
     * 
     * @param dialog Parent feedback dialog
     * @param name Name
     * @param email Email
     * @param feedback Feedback
     * @param sendServerInfo Send server info
     * @param serverName Server name
     * @param serverNetwork Server network
     * @param serverIRCD Server IRCD
     */
    public SendWorker(FeedbackDialog dialog, String name,
            String email, String feedback, boolean sendServerInfo,
            String serverName, String serverNetwork, String serverIRCD) {
        this.dialog = dialog;
        this.name = name;
        this.email = email;
        this.feedback = feedback;
        this.sendServerInfo = sendServerInfo;
        this.serverName = serverName;
        this.serverNetwork = serverNetwork;
        this.serverIRCD = serverIRCD;

        error = new StringBuilder();
    }

    /** 
     * {@inheritDoc}
     * 
     * @throws java.lang.Exception If unable to return a result
     */
    @Override
    protected Object doInBackground() throws Exception {
        final Map<String, String> postData =
                new HashMap<String, String>();

        if (!name.isEmpty()) {
            postData.put("name", name);
        }
        if (!email.isEmpty()) {
            postData.put("email", email);
        }
        if (!feedback.isEmpty()) {
            postData.put("feedback", feedback);
        }
        postData.put("version",
                Main.VERSION + "(" + Main.RELEASE_DATE + ")");
        if (sendServerInfo) {
            final StringBuilder sb = new StringBuilder();
            sb.append("Name: ").append(serverName).append("\n");
            sb.append("Network: ").append(serverNetwork).append("\n");
            sb.append("IRCD: ").append(serverIRCD);
            postData.put("serverInfo", sb.toString());
        }

        try {
            final List<String> response =
                    Downloader.getPage("http://www.dmdirc.com/feedback.php",
                    postData);
            if (response.size() >= 1) {
                for (String responseLine : response) {
                    error.append(responseLine).append("\n");
                }
            } else {
                error.append("Failure: Unknown response from the server.");
            }
        } catch (MalformedURLException ex) {
            error.append("Malformed feedback URL.");
        } catch (IOException ex) {
            error.append("Failure: " + ex.getMessage());
        }

        return error;
    }

    /** {@inheritDoc} */
    @Override
    protected void done() {
        dialog.layoutComponents2(error);
    }
}