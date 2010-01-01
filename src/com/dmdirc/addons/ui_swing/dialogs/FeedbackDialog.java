/*
 * Copyright (c) 2006-2010 Chris Smith, Shane Mc Cormack, Gregory Holmes
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

package com.dmdirc.addons.ui_swing.dialogs;

import com.dmdirc.Main;
import com.dmdirc.Server;
import com.dmdirc.ServerManager;
import com.dmdirc.ui.core.util.Info;
import com.dmdirc.addons.ui_swing.SwingController;
import com.dmdirc.addons.ui_swing.UIUtilities;
import com.dmdirc.addons.ui_swing.components.LoggingSwingWorker;
import com.dmdirc.addons.ui_swing.components.text.TextLabel;
import com.dmdirc.config.IdentityManager;
import com.dmdirc.util.Downloader;

import java.awt.Insets;
import java.awt.Window;
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
    private static volatile FeedbackDialog me;
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
    /** DMDirc info checkbox. */
    private JCheckBox DMDircCheckbox;
    /** Sent. */
    private boolean sentReport = false;

    /** 
     * Instantiates the feedback dialog. 
     * 
     * @param parentWindow Parent window
     */
    private FeedbackDialog(final Window parentWindow) {
        super(parentWindow, ModalityType.MODELESS);

        initComponents();
        layoutComponents();
        addListeners();

        setTitle("DMDirc: Feedback");
        setResizable(false);
    }

    /**
     * Creates the new feedback dialog if one doesn't exist, and displays it.
     * 
     * @param parentWindow Parent window
     */
    public static void showFeedbackDialog(final Window parentWindow) {
        me = getFeedbackDialog(parentWindow);

        me.display();
        me.requestFocusInWindow();
    }

    /**
     * Returns the current instance of the FeedbackDialog.
     * 
     * @param parentWindow Parent window
     *
     * @return The current FeedbackDialog instance
     */
    public static FeedbackDialog getFeedbackDialog(final Window parentWindow) {
        synchronized (FeedbackDialog.class) {
            if (me == null) {
                me = new FeedbackDialog(parentWindow);
                me.serverCheckbox.setEnabled(ServerManager.getServerManager().
                        numServers() > 0);
            } else if (!me.isVisible()) {
                me = new FeedbackDialog(parentWindow);
                me.serverCheckbox.setEnabled(ServerManager.getServerManager().
                    numServers() > 0);
            }
        }

        return me;
    }

    /** Initialises the components. */
    private void initComponents() {
        orderButtons(new JButton(), new JButton());

        getOkButton().setText("Send");
        getOkButton().setActionCommand("Send");
        getOkButton().setEnabled(false);
        getCancelButton().setActionCommand("Close");

        info = new TextLabel("Thank you for using DMDirc. If you have any " +
                "feedback about the client, such as bug reports or feature " +
                "requests, please send it to us using the form below.  " +
                "The name and e-mail address fields are optional if you " +
                "don't want us to contact you about your feedback.\n\n" +
                "Please note that this is for feedback such as bug reports " +
                "and suggestions, not for technical support. For " +
                "technical support, please join #DMDirc using the button " +
                "in the help menu.");
        name = new JTextField();
        email = new JTextField();
        feedback = new JTextArea();
        serverCheckbox =
                new JCheckBox("Include information about connected servers.");
        DMDircCheckbox = new JCheckBox("Include information about DMDirc.");

        UIUtilities.addUndoManager(name);
        UIUtilities.addUndoManager(email);
        UIUtilities.addUndoManager(feedback);
    }

    /** Lays out the components. */
    private void layoutComponents() {
        serverCheckbox.setMargin(new Insets(0, 0, 0, 0));
        serverCheckbox.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        DMDircCheckbox.setMargin(new Insets(0, 0, 0, 0));
        DMDircCheckbox.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

        setLayout(new MigLayout("fill, wmin 600, wmax 600, hmin 400, hmax 400"));

        add(info, "span, growx, wrap, gapbottom unrel");

        add(new JLabel("Name: "), "aligny top, shrink");
        add(name, "growx, pushx, wrap");

        add(new JLabel("Email: "), "aligny top, shrink");
        add(email, "growx, pushx, wrap");

        add(new JLabel("Feedback: "), "aligny top, shrink");
        add(new JScrollPane(feedback), "grow, push, wrap");
        add(serverCheckbox, "skip 1, growx, wrap");
        add(DMDircCheckbox, "skip 1, growx, wrap");

        add(getCancelButton(), "skip, split 2, right, sg button");
        add(getOkButton(), "right, sg button");
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

        setLayout(new MigLayout("fill, wmin 600, wmax 600, hmin 400, hmax 400"));

        info.setText(error.toString());

        add(info, "span 3, grow, push, wrap");

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
        sentReport = true;
        getOkButton().setEnabled(false);
        getCancelButton().setEnabled(false);
        final StringBuilder serverInfo = new StringBuilder();
        final StringBuilder dmdircInfo = new StringBuilder();
        if (serverCheckbox.isSelected()) {
            for (Server server : ServerManager.getServerManager().getServers()) {
                serverInfo.append("Server name: ").append(server.getName()).
                        append("\n");
                serverInfo.append("Actual name: ").
                        append(server.getParser().getServerName()).append("\n");
                serverInfo.append("Network: ").append(server.getNetwork()).
                        append("\n");
                serverInfo.append("IRCd: ").append(server.getParser().getServerSoftware()).
                        append(" - ");
                serverInfo.append(server.getParser().getServerSoftwareType()).append("\n");
                serverInfo.append("Modes: ").
                        append(server.getParser().getBooleanChannelModes()).
                        append(" ");
                serverInfo.append(server.getParser().getListChannelModes()).append(" ");
                serverInfo.append(server.getParser().getParameterChannelModes()).
                        append(" ");
                serverInfo.append(server.getParser().getDoubleParameterChannelModes());
            }
        }
        if (DMDircCheckbox.isSelected()) {
            dmdircInfo.append("DMDirc version: " + Info.getDMDircVersion()).
                    append("\n");
            dmdircInfo.append("Profile directory: " + Main.getConfigDir()).
                    append("\n");
            dmdircInfo.append("Java version: " + Info.getJavaVersion()).append("\n");
            dmdircInfo.append("OS Version: " + Info.getOSVersion()).append("\n");
            dmdircInfo.append("Look & Feel: " + SwingController.getLookAndFeel());
        }
        new SendWorker(me, name.getText().trim(),
                email.getText().trim(), feedback.getText().trim(), serverInfo.
                toString().trim(), dmdircInfo.toString().trim()).execute();
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
            if (!sentReport) {
                send();
            }
        } else if (e.getActionCommand().equals("Close")) {
            dispose();
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

    /** {@inheritDoc} */
    @Override
    public void dispose() {
        if (me == null) {
            return;
        }
        synchronized (me) {
            super.dispose();
            me = null;
        }
    }
}

/**
 * Sends feedback worker thread.
 */
class SendWorker extends LoggingSwingWorker {

    /** Parent feedback dialog. */
    private FeedbackDialog dialog;
    /** Name. */
    private String name;
    /** Email. */
    private String email;
    /** Feedback. */
    private String feedback;
    /** Server name. */
    private String serverInfo;
    /** DMDirc Info. */
    private String dmdircInfo;
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
    public SendWorker(FeedbackDialog dialog, String name, String email,
            String feedback) {
        this(dialog, name, email, feedback, "", "");
    }

    /**
     * Creates a new send worker to send feedback.
     *
     * @param dialog Parent feedback dialog
     * @param name Name
     * @param email Email
     * @param feedback Feedback
     * @param serverInfo serverInfo
     * @param dmdircInfo DMDirc info
     */
    public SendWorker(final FeedbackDialog dialog, final String name,
            final String email, final String feedback,
            final String serverInfo, final String dmdircInfo) {
        this.dialog = dialog;
        this.name = name;
        this.email = email;
        this.feedback = feedback;
        this.serverInfo = serverInfo;
        this.dmdircInfo = dmdircInfo;

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
        postData.put("version", IdentityManager.getGlobalConfig().getOption("version", "version"));
        if (!serverInfo.isEmpty()) {
            postData.put("serverInfo", serverInfo);
        }
        if (!dmdircInfo.isEmpty()) {
            postData.put("dmdircInfo", dmdircInfo);
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
        super.done();
        dialog.layoutComponents2(error);
    }
}
