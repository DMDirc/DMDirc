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

package com.dmdirc.addons.dcc;

import com.dmdirc.Main;
import com.dmdirc.Server;
import com.dmdirc.actions.ActionManager;
import com.dmdirc.actions.CoreActionType;
import com.dmdirc.actions.interfaces.ActionType;
import com.dmdirc.addons.dcc.kde.KFileChooser;
import com.dmdirc.addons.dcc.actions.DCCActions;
import com.dmdirc.addons.ui_swing.components.frames.TextFrame;
import com.dmdirc.addons.ui_swing.components.text.TextLabel;
import com.dmdirc.commandparser.CommandManager;
import com.dmdirc.config.Identity;
import com.dmdirc.config.IdentityManager;
import com.dmdirc.config.prefs.PreferencesCategory;
import com.dmdirc.config.prefs.PreferencesManager;
import com.dmdirc.config.prefs.PreferencesSetting;
import com.dmdirc.config.prefs.PreferencesType;
import com.dmdirc.interfaces.ActionListener;
import com.dmdirc.logger.ErrorLevel;
import com.dmdirc.logger.Logger;
import com.dmdirc.parser.interfaces.ClientInfo;
import com.dmdirc.parser.interfaces.Parser;
import com.dmdirc.plugins.Plugin;
import com.dmdirc.ui.WindowManager;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.net.InetAddress;
import java.net.UnknownHostException;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

/**
 * This plugin adds DCC to dmdirc.
 *
 * @author Shane 'Dataforce' McCormack
 */
public final class DCCPlugin extends Plugin implements ActionListener {

    /** The DCCCommand we created. */
    private DCCCommand command;

    /** Our DCC Container window. */
    private DCCFrame container;

    /** Child Frames. */
    private final List<DCCFrame> childFrames = new ArrayList<DCCFrame>();

    /**
     * Creates a new instance of the DCC Plugin.
     */
    public DCCPlugin() {
        super();
    }

    /**
     * Ask a question, if the answer is the answer required, then recall handleProcessEvent.
     *
     * @param question Question to ask
     * @param title Title of question dialog
     * @param desiredAnswer Answer required
     * @param type Actiontype to pass back
     * @param format StringBuffer to pass back
     * @param arguments arguments to pass back
     */
    public void askQuestion(final String question, final String title, final int desiredAnswer, final ActionType type, final StringBuffer format, final Object... arguments) {
        // New thread to ask the question in to stop us locking the UI
        final Thread questionThread = new Thread(new Runnable() {

            /** {@inheritDoc} */
            @Override
            public void run() {
                int result = JOptionPane.showConfirmDialog(null, question, title, JOptionPane.YES_NO_OPTION);
                if (result == desiredAnswer) {
                    handleProcessEvent(type, format, true, arguments);
                }
            }

        }, "QuestionThread: " + title);
        // Start the thread
        questionThread.start();
    }

    /**
     * Ask the location to save a file, then start the download.
     *
     * @param nickname Person this dcc is from.
     * @param send The DCCSend to save for.
     * @param parser The parser this send was received on
     * @param reverse Is this a reverse dcc?
     * @param sendFilename The name of the file which is being received
     * @param token Token used in reverse dcc.
     */
    public void saveFile(final String nickname, final DCCSend send, final Parser parser, final boolean reverse, final String sendFilename, final String token) {
        // New thread to ask the user where to save in to stop us locking the UI
        final Thread dccThread = new Thread(new Runnable() {

            /** {@inheritDoc} */
            @Override
            public void run() {
                final JFileChooser jc = KFileChooser.getFileChooser(DCCPlugin.this, IdentityManager.getGlobalConfig().getOption(getDomain(), "receive.savelocation"));
                jc.setDialogTitle("Save " + sendFilename + " As - DMDirc");
                jc.setFileSelectionMode(JFileChooser.FILES_ONLY);
                jc.setMultiSelectionEnabled(false);
                jc.setSelectedFile(new File(send.getFileName()));
                int result;
                if (IdentityManager.getGlobalConfig().getOptionBool(getDomain(), "receive.autoaccept")) {
                    result = JFileChooser.APPROVE_OPTION;
                } else {
                    result = jc.showSaveDialog((JFrame) Main.getUI().getMainWindow());
                }
                if (result == JFileChooser.APPROVE_OPTION) {
                    send.setFileName(jc.getSelectedFile().getPath());
                    boolean resume = false;
                    if (jc.getSelectedFile().exists()) {
                        if (send.getFileSize() > -1 && send.getFileSize() <= jc.getSelectedFile().length()) {
                            if (IdentityManager.getGlobalConfig().getOptionBool(getDomain(), "receive.autoaccept")) {
                                return;
                            } else {
                                JOptionPane.showMessageDialog((JFrame) Main.getUI().getMainWindow(), "This file has already been completed, or is longer than the file you are receiving.\nPlease choose a different file.", "Problem with selected file", JOptionPane.ERROR_MESSAGE);
                                saveFile(nickname, send, parser, reverse, sendFilename, token);
                                return;
                            }
                        } else {
                            if (IdentityManager.getGlobalConfig().getOptionBool(getDomain(), "receive.autoaccept")) {
                                resume = true;
                            } else {
                                result = JOptionPane.showConfirmDialog((JFrame) Main.getUI().getMainWindow(), "This file exists already, do you want to resume an exisiting download?", "Resume Download?", JOptionPane.YES_NO_OPTION);
                                resume = (result == JOptionPane.YES_OPTION);
                            }
                        }
                    }
                    if (reverse && !token.isEmpty()) {
                        new DCCSendWindow(DCCPlugin.this, send, "*Receive: " + nickname, nickname, null);
                        send.setToken(token);
                        if (resume) {
                            if (IdentityManager.getGlobalConfig().getOptionBool(getDomain(), "receive.reverse.sendtoken")) {
                                parser.sendCTCP(nickname, "DCC", "RESUME " + sendFilename + " 0 " + jc.getSelectedFile().length() + " " + token);
                            } else {
                                parser.sendCTCP(nickname, "DCC", "RESUME " + sendFilename + " 0 " + jc.getSelectedFile().length());
                            }
                        } else {
                            if (listen(send)) {
                                parser.sendCTCP(nickname, "DCC", "SEND " + sendFilename + " " + DCC.ipToLong(getListenIP(parser)) + " " + send.getPort() + " " + send.getFileSize() + " " + token);
                            } else {
                                // Listen failed.
                            }
                        }
                    } else {
                        new DCCSendWindow(DCCPlugin.this, send, "Receive: " + nickname, nickname, null);
                        if (resume) {
                            parser.sendCTCP(nickname, "DCC", "RESUME " + sendFilename + " " + send.getPort() + " " + jc.getSelectedFile().length());
                        } else {
                            send.connect();
                        }
                    }
                }
            }

        }, "saveFileThread: " + sendFilename);
        // Start the thread
        dccThread.start();
    }

    /**
     * Process an event of the specified type.
     *
     * @param type The type of the event to process
     * @param format Format of messages that are about to be sent. (May be null)
     * @param arguments The arguments for the event
     */
    @Override
    public void processEvent(final ActionType type, final StringBuffer format, final Object... arguments) {
        handleProcessEvent(type, format, false, arguments);
    }

    /**
     * Make the given DCC start listening.
     * This will either call dcc.listen() or dcc.listen(startPort, endPort)
     * depending on config.
     *
     * @param dcc DCC to start listening.
     * @return True if Socket was opened.
     */
    protected boolean listen(final DCC dcc) {

        final boolean usePortRange = IdentityManager.getGlobalConfig().getOptionBool(getDomain(), "firewall.ports.usePortRange");
        final int startPort = IdentityManager.getGlobalConfig().getOptionInt(getDomain(), "firewall.ports.startPort");
        final int endPort = IdentityManager.getGlobalConfig().getOptionInt(getDomain(), "firewall.ports.endPort");

        try {
            if (usePortRange) {
                dcc.listen(startPort, endPort);
            } else {
                dcc.listen();
            }
            return true;
        } catch (IOException ioe) {
            return false;
        }
    }

    /**
     * Process an event of the specified type.
     *
     * @param type The type of the event to process
     * @param format Format of messages that are about to be sent. (May be null)
     * @param dontAsk Don't ask any questions, assume yes.
     * @param arguments The arguments for the event
     */
    public void handleProcessEvent(final ActionType type, final StringBuffer format, final boolean dontAsk, final Object... arguments) {
        if (IdentityManager.getGlobalConfig().getOptionBool(getDomain(), "receive.autoaccept") && !dontAsk) {
            handleProcessEvent(type, format, true, arguments);
            return;
        }

        if (type == CoreActionType.SERVER_CTCP) {
            final String ctcpType = (String) arguments[2];
            final String[] ctcpData = ((String) arguments[3]).split(" ");
            if (ctcpType.equalsIgnoreCase("DCC")) {
                if (ctcpData[0].equalsIgnoreCase("chat") && ctcpData.length > 3) {
                    final String nickname = ((ClientInfo) arguments[1]).getNickname();
                    if (dontAsk) {
                        final DCCChat chat = new DCCChat();
                        try {
                            chat.setAddress(Long.parseLong(ctcpData[2]), Integer.parseInt(ctcpData[3]));
                        } catch (NumberFormatException nfe) {
                            return;
                        }
                        final String myNickname = ((Server) arguments[0]).getParser().getLocalClient().getNickname();
                        final DCCFrame f = new DCCChatWindow(this, chat, "Chat: " + nickname, myNickname, nickname);
                        f.getFrame().addLine("DCCChatStarting", nickname, chat.getHost(), chat.getPort());
                        chat.connect();
                    } else {
                        ActionManager.processEvent(DCCActions.DCC_CHAT_REQUEST, null, ((Server) arguments[0]), nickname);
                        askQuestion("User " + nickname + " on " + ((Server) arguments[0]).toString() + " would like to start a DCC Chat with you.\n\nDo you want to continue?", "DCC Chat Request", JOptionPane.YES_OPTION, type, format, arguments);
                        return;
                    }
                } else if (ctcpData[0].equalsIgnoreCase("send") && ctcpData.length > 3) {
                    final String nickname = ((ClientInfo) arguments[1]).getNickname();
                    final String filename;
                    String tmpFilename;
                    // Clients tend to put files with spaces in the name in "" so lets look for that.
                    final StringBuilder filenameBits = new StringBuilder();
                    int i;
                    final boolean quoted = ctcpData[1].startsWith("\"");
                    if (quoted) {
                        for (i = 1; i < ctcpData.length; i++) {
                            String bit = ctcpData[i];
                            if (i == 1) {
                                bit = bit.substring(1);
                            }
                            if (bit.endsWith("\"")) {
                                filenameBits.append(" " + bit.substring(0, bit.length() - 1));
                                break;
                            } else {
                                filenameBits.append(" " + bit);
                            }
                        }
                        tmpFilename = filenameBits.toString().trim();
                    } else {
                        tmpFilename = ctcpData[1];
                        i = 1;
                    }

                    // Try to remove path names if sent.
                    // Change file separatorChar from other OSs first
                    if (File.separatorChar == '/') {
                        tmpFilename = tmpFilename.replace('\\', File.separatorChar);
                    } else {
                        tmpFilename = tmpFilename.replace('/', File.separatorChar);
                    }
                    // Then get just the name of the file.
                    filename = (new File(tmpFilename)).getName();

                    final String ip = ctcpData[++i];
                    final String port = ctcpData[++i];
                    long size;
                    if (ctcpData.length + 1 > i) {
                        try {
                            size = Integer.parseInt(ctcpData[++i]);
                        } catch (NumberFormatException nfe) {
                            size = -1;
                        }
                    } else {
                        size = -1;
                    }
                    final String token = (ctcpData.length - 1 > i && !ctcpData[i + 1].equals("T")) ? ctcpData[++i] : "";

                    // Ignore incorrect ports, or non-numeric IP/Port
                    try {
                        int portInt = Integer.parseInt(port);
                        if (portInt > 65535 || portInt < 0) {
                            return;
                        }
                        Long.parseLong(ip);
                    } catch (NumberFormatException nfe) {
                        return;
                    }

                    DCCSend send = DCCSend.findByToken(token);

                    if (send == null && !dontAsk) {
                        if (!token.isEmpty() && !port.equals("0")) {
                            // This is a reverse DCC Send that we no longer care about.
                            return;
                        } else {
                            ActionManager.processEvent(DCCActions.DCC_SEND_REQUEST, null, ((Server) arguments[0]), nickname, filename);
                            askQuestion("User " + nickname + " on " + ((Server) arguments[0]).toString() + " would like to send you a file over DCC.\n\nFile: " + filename + "\n\nDo you want to continue?", "DCC Send Request", JOptionPane.YES_OPTION, type, format, arguments);
                            return;
                        }
                    } else {
                        final boolean newSend = send == null;
                        if (newSend) {
                            send = new DCCSend(IdentityManager.getGlobalConfig().getOptionInt(getDomain(), "send.blocksize"));
                            send.setTurbo(IdentityManager.getGlobalConfig().getOptionBool(getDomain(), "send.forceturbo"));
                        }
                        try {
                            send.setAddress(Long.parseLong(ip), Integer.parseInt(port));
                        } catch (NumberFormatException nfe) {
                            return;
                        }
                        if (newSend) {
                            send.setFileName(filename);
                            send.setFileSize(size);
                            saveFile(nickname, send, ((Server) arguments[0]).getParser(), "0".equals(port), (quoted) ? "\"" + filename + "\"" : filename, token);
                        } else {
                            send.connect();
                        }
                    }
                } else if ((ctcpData[0].equalsIgnoreCase("resume") || ctcpData[0].equalsIgnoreCase("accept")) && ctcpData.length > 2) {

                    final String filename;
                    // Clients tend to put files with spaces in the name in "" so lets look for that.
                    final StringBuilder filenameBits = new StringBuilder();
                    int i;
                    final boolean quoted = ctcpData[1].startsWith("\"");
                    if (quoted) {
                        for (i = 1; i < ctcpData.length; i++) {
                            String bit = ctcpData[i];
                            if (i == 1) {
                                bit = bit.substring(1);
                            }
                            if (bit.endsWith("\"")) {
                                filenameBits.append(" " + bit.substring(0, bit.length() - 1));
                                break;
                            } else {
                                filenameBits.append(" " + bit);
                            }
                        }
                        filename = filenameBits.toString().trim();
                    } else {
                        filename = ctcpData[1];
                        i = 1;
                    }

                    try {
                        final int port = Integer.parseInt(ctcpData[++i]);
                        final int position = Integer.parseInt(ctcpData[++i]);
                        final String token = (ctcpData.length - 1 > i) ? " " + ctcpData[++i] : "";

                        // Now look for a dcc that matches.
                        for (DCCSend send : DCCSend.getSends()) {
                            if (send.port == port && (new File(send.getFileName())).getName().equalsIgnoreCase(filename)) {
                                if ((!token.isEmpty() && !send.getToken().isEmpty()) && (!token.equals(send.getToken()))) {
                                    continue;
                                }
                                final Parser parser = ((Server) arguments[0]).getParser();
                                final String nickname = ((ClientInfo) arguments[1]).getNickname();
                                if (ctcpData[0].equalsIgnoreCase("resume")) {
                                    parser.sendCTCP(nickname, "DCC", "ACCEPT " + ((quoted) ? "\"" + filename + "\"" : filename) + " " + port + " " + send.setFileStart(position) + token);
                                } else {
                                    send.setFileStart(position);
                                    if (port == 0) {
                                        // Reverse dcc
                                        if (listen(send)) {
                                            if (send.getToken().isEmpty()) {
                                                parser.sendCTCP(nickname, "DCC", "SEND " + ((quoted) ? "\"" + filename + "\"" : filename) + " " + DCC.ipToLong(send.getHost()) + " " + send.getPort() + " " + send.getFileSize());
                                            } else {
                                                parser.sendCTCP(nickname, "DCC", "SEND " + ((quoted) ? "\"" + filename + "\"" : filename) + " " + DCC.ipToLong(send.getHost()) + " " + send.getPort() + " " + send.getFileSize() + " " + send.getToken());
                                            }
                                        } else {
                                            // Listen failed.
                                        }
                                    } else {
                                        send.connect();
                                    }
                                }
                            }
                        }
                    } catch (NumberFormatException nfe) {
                    }
                }
            }
        }
    }

    /**
     * Create the container window.
     */
    protected void createContainer() {
        container = new DCCFrame(this, "DCCs", "dcc") {
        };
        final TextLabel label = new TextLabel("This is a placeholder window to group DCCs together.");
        label.setText(label.getText() + "\n\nClosing this window will close all the active DCCs");
        ((TextFrame) container.getFrame()).getContentPane().add(label);
        WindowManager.addWindow(container.getFrame());
        container.getFrame().open();
    }

    /**
     * Add a window to the container window.
     *
     * @param window Window to remove
     */
    protected synchronized void addWindow(final DCCFrame window) {
        if (window == container) {
            return;
        }
        if (container == null) {
            createContainer();
        }

        WindowManager.addWindow(container.getFrame(), window.getFrame());
        childFrames.add(window);
        window.getFrame().open();
    }

    /**
     * Remove a window from the container window.
     *
     * @param window Window to remove
     */
    protected synchronized void delWindow(final DCCFrame window) {
        if (container == null) {
            return;
        }
        if (window == container) {
            container = null;
            for (DCCFrame win : childFrames) {
                if (win != window) {
                    win.close();
                }
            }
            childFrames.clear();
        } else {
            childFrames.remove(window);
            if (childFrames.isEmpty()) {
                container.close();
                container = null;
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public void domainUpdated() {
        final Identity defaults = IdentityManager.getAddonIdentity();

        defaults.setOption(getDomain(), "receive.savelocation",
                Main.getConfigDir() + "downloads" + System.getProperty("file.separator"));
    }

    /**
     * Called when the plugin is loaded.
     */
    @Override
    public void onLoad() {
        final File dir = new File(IdentityManager.getGlobalConfig().getOption(getDomain(), "receive.savelocation"));
        if (dir.exists()) {
            if (!dir.isDirectory()) {
                Logger.userError(ErrorLevel.LOW, "Unable to create download dir (file exists instead)");
            }
        } else {
            try {
                dir.mkdirs();
                dir.createNewFile();
            } catch (IOException ex) {
                Logger.userError(ErrorLevel.LOW, "Unable to create download dir");
            }
        }

        command = new DCCCommand(this);
        ActionManager.registerActionTypes(DCCActions.values());
        ActionManager.addListener(this, CoreActionType.SERVER_CTCP);
    }

    /**
     * Called when this plugin is Unloaded.
     */
    @Override
    public synchronized void onUnload() {
        CommandManager.unregisterCommand(command);
        ActionManager.removeListener(this);
        if (container != null) {
            container.close();
        }
    }

    /**
     * Get the IP Address we should send as our listening IP.
     *
     * @return The IP Address we should send as our listening IP.
     */
    public String getListenIP() {
        return getListenIP(null);
    }

    /**
     * Get the IP Address we should send as our listening IP.
     *
     * @param parser Parser the IRC Parser where this dcc is initiated
     * @return The IP Address we should send as our listening IP.
     */
    public String getListenIP(final Parser parser) {
        final String configIP = IdentityManager.getGlobalConfig().getOption(getDomain(), "firewall.ip");
        if (!configIP.isEmpty()) {
            return configIP;
        } else if (parser != null) {
            final String myHost = parser.getLocalClient().getHostname();
            if (!myHost.isEmpty()) {
                try {
                    return InetAddress.getByName(myHost).getHostAddress();
                } catch (UnknownHostException e) { /* Will return default host below */ }
            }
        }
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            // This is almost certainly not what we want, but we can't work out
            // the right one.
            return "127.0.0.1";
        }
    }

    /** {@inheritDoc} */
    @Override
    public void showConfig(final PreferencesManager manager) {
        final PreferencesCategory general = new PreferencesCategory("DCC", "", "category-dcc");
        final PreferencesCategory firewall = new PreferencesCategory("Firewall", "");
        final PreferencesCategory sending = new PreferencesCategory("Sending", "");
        final PreferencesCategory receiving = new PreferencesCategory("Receiving", "");

        manager.getCategory("Plugins").addSubCategory(general.setInlineAfter());
        general.addSubCategory(firewall.setInline());
        general.addSubCategory(sending.setInline());
        general.addSubCategory(receiving.setInline());

        firewall.addSetting(new PreferencesSetting(PreferencesType.TEXT,
                getDomain(), "firewall.ip", "Forced IP",
                "What IP should be sent as our IP (Blank = work it out)"));
        firewall.addSetting(new PreferencesSetting(PreferencesType.BOOLEAN,
                getDomain(), "firewall.ports.usePortRange", "Use Port Range",
                "Useful if you have a firewall that only forwards specific ports"));
        firewall.addSetting(new PreferencesSetting(PreferencesType.INTEGER,
                getDomain(), "firewall.ports.startPort", "Start Port",
                "Port to try to listen on first"));
        firewall.addSetting(new PreferencesSetting(PreferencesType.INTEGER,
                getDomain(), "firewall.ports.endPort", "End Port",
                "Port to try to listen on last"));
        receiving.addSetting(new PreferencesSetting(PreferencesType.TEXT,
                getDomain(), "receive.savelocation", "Default save location",
                "Where the save as window defaults to?"));
        sending.addSetting(new PreferencesSetting(PreferencesType.BOOLEAN,
                getDomain(), "send.reverse", "Reverse DCC",
                "With reverse DCC, the sender connects rather than " +
                "listens like normal dcc"));
        sending.addSetting(new PreferencesSetting(PreferencesType.BOOLEAN,
                getDomain(), "send.forceturbo", "Use Turbo DCC",
                "Turbo DCC doesn't wait for ack packets. this is " +
                "faster but not always supported."));
        receiving.addSetting(new PreferencesSetting(PreferencesType.BOOLEAN,
                getDomain(), "receive.reverse.sendtoken",
                "Send token in reverse receive",
                "If you have problems with reverse dcc receive resume," +
                " try toggling this."));
        general.addSetting(new PreferencesSetting(PreferencesType.INTEGER,
                getDomain(), "send.blocksize", "Blocksize to use for DCC",
                "Change the block size for send/receive, this can " +
                "sometimes speed up transfers."));
    }

}

