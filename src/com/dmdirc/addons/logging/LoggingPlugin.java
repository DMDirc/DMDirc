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

package com.dmdirc.addons.logging;

import com.dmdirc.Channel;
import com.dmdirc.Main;
import com.dmdirc.Query;
import com.dmdirc.Server;
import com.dmdirc.actions.ActionManager;
import com.dmdirc.actions.CoreActionType;
import com.dmdirc.actions.interfaces.ActionType;
import com.dmdirc.commandparser.CommandManager;
import com.dmdirc.config.IdentityManager;
import com.dmdirc.config.prefs.PreferencesCategory;
import com.dmdirc.config.prefs.PreferencesManager;
import com.dmdirc.config.prefs.PreferencesSetting;
import com.dmdirc.config.prefs.PreferencesType;
import com.dmdirc.interfaces.ActionListener;
import com.dmdirc.logger.ErrorLevel;
import com.dmdirc.logger.Logger;
import com.dmdirc.parser.interfaces.ChannelClientInfo;
import com.dmdirc.parser.interfaces.ChannelInfo;
import com.dmdirc.parser.interfaces.ClientInfo;
import com.dmdirc.parser.interfaces.Parser;
import com.dmdirc.plugins.Plugin;
import com.dmdirc.ui.interfaces.InputWindow;
import com.dmdirc.ui.interfaces.Window;
import com.dmdirc.ui.messages.Styliser;

import java.awt.Color;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Hashtable;
import java.util.Map;
import java.util.Stack;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Adds logging facility to client.
 *
 * @author Shane 'Dataforce' McCormack
 */
public class LoggingPlugin extends Plugin implements ActionListener {

    /** The command we registered. */
    private LoggingCommand command;

    /** Open File */
    protected class OpenFile {

        public long lastUsedTime = System.currentTimeMillis();

        public BufferedWriter writer = null;

        public OpenFile(final BufferedWriter writer) {
            this.writer = writer;
        }

    }

    /** Timer used to close idle files */
    protected Timer idleFileTimer;

    /** Hashtable of open files. */
    protected final Map<String, OpenFile> openFiles = new Hashtable<String, OpenFile>();

    /** Date format used for "File Opened At" log. */
    final DateFormat openedAtFormat = new SimpleDateFormat("EEEE MMMM dd, yyyy - HH:mm:ss");

    /**
     * Creates a new instance of the Logging Plugin.
     */
    public LoggingPlugin() {
        super();
    }

    /** {@inheritDoc} */
    @Override
    public void domainUpdated() {
        IdentityManager.getAddonIdentity().setOption(getDomain(),
                "general.directory", Main.getConfigDir() + "logs" + System.getProperty("file.separator"));
    }

    /**
     * Called when the plugin is loaded.
     */
    @Override
    public void onLoad() {
        final File dir = new File(IdentityManager.getGlobalConfig().getOption(getDomain(), "general.directory"));
        if (dir.exists()) {
            if (!dir.isDirectory()) {
                Logger.userError(ErrorLevel.LOW, "Unable to create logging dir (file exists instead)");
            }
        } else {
            if (!dir.mkdirs()) {
                Logger.userError(ErrorLevel.LOW, "Unable to create logging dir");
            }
        }

        command = new LoggingCommand();
        ActionManager.addListener(this,
                CoreActionType.CHANNEL_OPENED,
                CoreActionType.CHANNEL_CLOSED,
                CoreActionType.CHANNEL_MESSAGE,
                CoreActionType.CHANNEL_SELF_MESSAGE,
                CoreActionType.CHANNEL_ACTION,
                CoreActionType.CHANNEL_SELF_ACTION,
                CoreActionType.CHANNEL_GOTTOPIC,
                CoreActionType.CHANNEL_TOPICCHANGE,
                CoreActionType.CHANNEL_JOIN,
                CoreActionType.CHANNEL_PART,
                CoreActionType.CHANNEL_QUIT,
                CoreActionType.CHANNEL_KICK,
                CoreActionType.CHANNEL_NICKCHANGE,
                CoreActionType.CHANNEL_MODECHANGE,
                CoreActionType.QUERY_OPENED,
                CoreActionType.QUERY_CLOSED,
                CoreActionType.QUERY_MESSAGE,
                CoreActionType.QUERY_SELF_MESSAGE,
                CoreActionType.QUERY_ACTION,
                CoreActionType.QUERY_SELF_ACTION);

        // Close idle files every hour.
        idleFileTimer = new Timer("LoggingPlugin Timer");
        idleFileTimer.schedule(new TimerTask() {

            /** {@inheritDoc} */
            @Override
            public void run() {
                timerTask();
            }

        }, 3600000);
    }

    /**
     * What to do every hour when the timer fires.
     */
    protected void timerTask() {
        // Oldest time to allow
        final long oldestTime = System.currentTimeMillis() - 3480000;

        synchronized (openFiles) {
            for (String filename : (new Hashtable<String, OpenFile>(openFiles)).keySet()) {
                OpenFile file = openFiles.get(filename);
                if (file.lastUsedTime < oldestTime) {
                    try {
                        file.writer.close();
                        openFiles.remove(filename);
                    } catch (IOException e) {
                        Logger.userError(ErrorLevel.LOW, "Unable to close idle file (File: " + filename + ")");
                    }
                }
            }
        }
    }

    /**
     * Called when this plugin is unloaded.
     */
    @Override
    public void onUnload() {
        idleFileTimer.cancel();
        idleFileTimer.purge();

        CommandManager.unregisterCommand(command);
        ActionManager.removeListener(this);

        synchronized (openFiles) {
            for (String filename : openFiles.keySet()) {
                OpenFile file = openFiles.get(filename);
                try {
                    file.writer.close();
                } catch (IOException e) {
                    Logger.userError(ErrorLevel.LOW, "Unable to close file (File: " + filename + ")");
                }
            }
            openFiles.clear();
        }
    }

    /** {@inheritDoc} */
    @Override
    public void showConfig(final PreferencesManager manager) {
        final PreferencesCategory general = new PreferencesCategory("Logging", "General configuration for Logging plugin.");
        final PreferencesCategory backbuffer = new PreferencesCategory("Back Buffer", "Options related to the automatic backbuffer");
        final PreferencesCategory advanced = new PreferencesCategory("Advanced", "Advanced configuration for Logging plugin. You shouldn't need to edit this unless you know what you are doing.");

        general.addSetting(new PreferencesSetting(PreferencesType.TEXT, getDomain(), "general.directory", "Directory", "Directory for log files"));
        general.addSetting(new PreferencesSetting(PreferencesType.BOOLEAN, getDomain(), "general.networkfolders", "Separate logs by network", "Should the files be stored in a sub-dir with the networks name?"));
        general.addSetting(new PreferencesSetting(PreferencesType.BOOLEAN, getDomain(), "general.addtime", "Timestamp logs", "Should a timestamp be added to the log files?"));
        general.addSetting(new PreferencesSetting(PreferencesType.TEXT, getDomain(), "general.timestamp", "Timestamp format", "The String to pass to 'SimpleDateFormat' to format the timestamp"));
        general.addSetting(new PreferencesSetting(PreferencesType.BOOLEAN, getDomain(), "general.stripcodes", "Strip Control Codes", "Remove known irc control codes from lines before saving?"));
        general.addSetting(new PreferencesSetting(PreferencesType.BOOLEAN, getDomain(), "general.channelmodeprefix", "Show channel mode prefix", "Show the @,+ etc next to nicknames"));

        backbuffer.addSetting(new PreferencesSetting(PreferencesType.BOOLEAN, getDomain(), "backbuffer.autobackbuffer", "Automatically display", "Automatically display the backbuffer when a channel is joined"));
        backbuffer.addSetting(new PreferencesSetting(PreferencesType.COLOUR, getDomain(), "backbuffer.colour", "Colour to use for display", "Colour used when displaying the backbuffer"));
        backbuffer.addSetting(new PreferencesSetting(PreferencesType.INTEGER, getDomain(), "backbuffer.lines", "Number of lines to show", "Number of lines used when displaying backbuffer"));
        backbuffer.addSetting(new PreferencesSetting(PreferencesType.BOOLEAN, getDomain(), "backbuffer.timestamp", "Show Formatter-Timestamp", "Should the line be added to the frame with the timestamp from the formatter aswell as the file contents"));

        advanced.addSetting(new PreferencesSetting(PreferencesType.BOOLEAN, getDomain(), "advanced.filenamehash", "Add Filename hash", "Add the MD5 hash of the channel/client name to the filename. (This is used to allow channels with similar names (ie a _ not a  -) to be logged separately)"));

        advanced.addSetting(new PreferencesSetting(PreferencesType.BOOLEAN, getDomain(), "advanced.usedate", "Use Date directories", "Should the log files be in separate directories based on the date?"));
        advanced.addSetting(new PreferencesSetting(PreferencesType.TEXT, getDomain(), "advanced.usedateformat", "Archive format", "The String to pass to 'SimpleDateFormat' to format the directory name(s) for archiving"));

        general.addSubCategory(backbuffer.setInline());
        general.addSubCategory(advanced.setInline());
        manager.getCategory("Plugins").addSubCategory(general.setInlineAfter());
    }

    /**
     * Log a query-related event
     *
     * @param type The type of the event to process
     * @param format Format of messages that are about to be sent. (May be null)
     * @param arguments The arguments for the event
     */
    protected void handleQueryEvent(final CoreActionType type, final StringBuffer format, final Object... arguments) {
        final Query query = (Query) arguments[0];
        if (query.getServer() == null) {
            Logger.appError(ErrorLevel.MEDIUM, "Query object has no server (" + type.toString() + ")", new Exception("Query object has no server (" + type.toString() + ")"));
            return;
        }

        final Parser parser = query.getServer().getParser();
        ClientInfo client;

        if (parser == null) {
            // Without a parser object, we might not be able to find the file to log this to.
            if (IdentityManager.getGlobalConfig().getOptionBool(getDomain(), "general.networkfolders")) {
                // We *wont* be able to, so rather than logging to an incorrect file we just won't log.
                return;
            }
            client = null;
        } else {
            client = parser.getClient(query.getHost());
        }

        final String filename = getLogFile(client);

        switch (type) {
            case QUERY_OPENED:
                if (IdentityManager.getGlobalConfig().getOptionBool(getDomain(), "backbuffer.autobackbuffer")) {
                    showBackBuffer(query.getFrame(), filename);
                }

                appendLine(filename, "*** Query opened at: %s", openedAtFormat.format(new Date()));
                appendLine(filename, "*** Query with User: %s", query.getHost());
                appendLine(filename, "");
                break;
            case QUERY_CLOSED:
                appendLine(filename, "*** Query closed at: %s", openedAtFormat.format(new Date()));
                if (openFiles.containsKey(filename)) {
                    final BufferedWriter file = openFiles.get(filename).writer;
                    try {
                        file.close();
                    } catch (IOException e) {
                        Logger.userError(ErrorLevel.LOW, "Unable to close file (Filename: " + filename + ")");
                    }
                    openFiles.remove(filename);
                }
                break;
            case QUERY_MESSAGE:
            case QUERY_SELF_MESSAGE:
            case QUERY_ACTION:
            case QUERY_SELF_ACTION:
                final boolean isME = (type == CoreActionType.QUERY_SELF_MESSAGE || type == CoreActionType.QUERY_SELF_ACTION);
                final String overrideNick = (isME) ? getDisplayName(parser.getLocalClient()) : "";

                if (type == CoreActionType.QUERY_MESSAGE || type == CoreActionType.QUERY_SELF_MESSAGE) {
                    appendLine(filename, "<%s> %s", getDisplayName(client, overrideNick), (String) arguments[1]);
                } else {
                    appendLine(filename, "* %s %s", getDisplayName(client, overrideNick), (String) arguments[1]);
                }
                break;
        }
    }

    /**
     * Log a channel-related event
     *
     * @param type The type of the event to process
     * @param format Format of messages that are about to be sent. (May be null)
     * @param arguments The arguments for the event
     */
    protected void handleChannelEvent(final CoreActionType type, final StringBuffer format, final Object... arguments) {
        final Channel chan = ((Channel) arguments[0]);
        final ChannelInfo channel = chan.getChannelInfo();
        final String filename = getLogFile(channel);

        final ChannelClientInfo channelClient = (arguments.length > 1 && arguments[1] instanceof ChannelClientInfo) ? (ChannelClientInfo) arguments[1] : null;
        final ClientInfo client = (channelClient != null) ? channelClient.getClient() : null;

        final String message = (arguments.length > 2 && arguments[2] instanceof String) ? (String) arguments[2] : null;

        switch (type) {
            case CHANNEL_OPENED:
                if (IdentityManager.getGlobalConfig().getOptionBool(getDomain(), "backbuffer.autobackbuffer")) {
                    showBackBuffer(chan.getFrame(), filename);
                }

                appendLine(filename, "*** Channel opened at: %s", openedAtFormat.format(new Date()));
                appendLine(filename, "");
                break;
            case CHANNEL_CLOSED:
                appendLine(filename, "*** Channel closed at: %s", openedAtFormat.format(new Date()));
                if (openFiles.containsKey(filename)) {
                    final BufferedWriter file = openFiles.get(filename).writer;
                    try {
                        file.close();
                    } catch (IOException e) {
                        Logger.userError(ErrorLevel.LOW, "Unable to close file (Filename: " + filename + ")");
                    }
                    openFiles.remove(filename);
                }
                break;
            case CHANNEL_MESSAGE:
            case CHANNEL_SELF_MESSAGE:
            case CHANNEL_ACTION:
            case CHANNEL_SELF_ACTION:
                if (type == CoreActionType.CHANNEL_MESSAGE || type == CoreActionType.CHANNEL_SELF_MESSAGE) {
                    appendLine(filename, "<%s> %s", getDisplayName(client), message);
                } else {
                    appendLine(filename, "* %s %s", getDisplayName(client), message);
                }
                break;
            case CHANNEL_GOTTOPIC:
                // ActionManager.processEvent(CoreActionType.CHANNEL_GOTTOPIC, this);
                final DateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
                final DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

                appendLine(filename, "*** Topic is: %s", channel.getTopic());
                appendLine(filename, "*** Set at: %s on %s by %s", timeFormat.format(1000 * channel.getTopicTime()), dateFormat.format(1000 * channel.getTopicTime()), channel.getTopicSetter());
                break;
            case CHANNEL_TOPICCHANGE:
                appendLine(filename, "*** %s Changed the topic to: %s", getDisplayName(channelClient), message);
                break;
            case CHANNEL_JOIN:
                appendLine(filename, "*** %s (%s) joined the channel", getDisplayName(channelClient), client.toString());
                break;
            case CHANNEL_PART:
                if (message.isEmpty()) {
                    appendLine(filename, "*** %s (%s) left the channel", getDisplayName(channelClient), client.toString());
                } else {
                    appendLine(filename, "*** %s (%s) left the channel (%s)", getDisplayName(channelClient), client.toString(), message);
                }
                break;
            case CHANNEL_QUIT:
                if (message.isEmpty()) {
                    appendLine(filename, "*** %s (%s) Quit IRC", getDisplayName(channelClient), client.toString());
                } else {
                    appendLine(filename, "*** %s (%s) Quit IRC (%s)", getDisplayName(channelClient), client.toString(), message);
                }
                break;
            case CHANNEL_KICK:
                final String kickReason = (String) arguments[3];
                final ChannelClientInfo kickedClient = (ChannelClientInfo) arguments[2];

                if (kickReason.isEmpty()) {
                    appendLine(filename, "*** %s was kicked by %s", getDisplayName(kickedClient), getDisplayName(channelClient));
                } else {
                    appendLine(filename, "*** %s was kicked by %s (%s)", getDisplayName(kickedClient), getDisplayName(channelClient), kickReason);
                }
                break;
            case CHANNEL_NICKCHANGE:
                appendLine(filename, "*** %s is now %s", getDisplayName(channelClient, message), getDisplayName(channelClient));
                break;
            case CHANNEL_MODECHANGE:
                if (channelClient.getClient().getNickname().isEmpty()) {
                    appendLine(filename, "*** Channel modes are: %s", message);
                } else {
                    appendLine(filename, "*** %s set modes: %s", getDisplayName(channelClient), message);
                }
                break;
        }
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
        if (type instanceof CoreActionType) {
            final CoreActionType thisType = (CoreActionType) type;

            switch (thisType) {
                case CHANNEL_OPENED:
                case CHANNEL_CLOSED:
                case CHANNEL_MESSAGE:
                case CHANNEL_SELF_MESSAGE:
                case CHANNEL_ACTION:
                case CHANNEL_SELF_ACTION:
                case CHANNEL_GOTTOPIC:
                case CHANNEL_TOPICCHANGE:
                case CHANNEL_JOIN:
                case CHANNEL_PART:
                case CHANNEL_QUIT:
                case CHANNEL_KICK:
                case CHANNEL_NICKCHANGE:
                case CHANNEL_MODECHANGE:
                    handleChannelEvent(thisType, format, arguments);
                    break;
                case QUERY_OPENED:
                case QUERY_CLOSED:
                case QUERY_MESSAGE:
                case QUERY_SELF_MESSAGE:
                case QUERY_ACTION:
                case QUERY_SELF_ACTION:
                    handleQueryEvent(thisType, format, arguments);
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * Add a backbuffer to a frame.
     *
     * @param frame The frame to add the backbuffer lines to
     * @param filename File to get backbuffer from
     */
    protected void showBackBuffer(final Window frame, final String filename) {
        final int numLines = IdentityManager.getGlobalConfig().getOptionInt(getDomain(), "backbuffer.lines");
        final String colour = IdentityManager.getGlobalConfig().getOption(getDomain(), "backbuffer.colour");
        final boolean showTimestamp = IdentityManager.getGlobalConfig().getOptionBool(getDomain(), "backbuffer.timestamp");
        if (frame == null) {
            Logger.userError(ErrorLevel.LOW, "Given a null frame");
            return;
        }

        final File testFile = new File(filename);
        if (testFile.exists()) {
            try {
                final ReverseFileReader file = new ReverseFileReader(testFile);
                // Because the file includes a newline char at the end, an empty line
                // is returned by getLines. To counter this, we call getLines(1) and do
                // nothing with the output.
                file.getLines(1);
                final Stack<String> lines = file.getLines(numLines);
                while (!lines.empty()) {
                    frame.addLine(getColouredString(colour, lines.pop()), showTimestamp);
                }
                file.close();
                frame.addLine(getColouredString(colour, "--- End of backbuffer\n"), showTimestamp);
            } catch (FileNotFoundException e) {
                Logger.userError(ErrorLevel.LOW, "Unable to show backbuffer (Filename: " + filename + "): " + e.getMessage());
            } catch (IOException e) {
                Logger.userError(ErrorLevel.LOW, "Unable to show backbuffer (Filename: " + filename + "): " + e.getMessage());
            } catch (SecurityException e) {
                Logger.userError(ErrorLevel.LOW, "Unable to show backbuffer (Filename: " + filename + "): " + e.getMessage());
            }
        }
    }

    /**
     * Get a coloured String.
     * If colour is invalid, IRC Colour 14 will be used.
     *
     * @param colour The colour the string should be (IRC Colour or 6-digit hex colour)
     * @param line the line to colour
     * @return The given line with the appropriate irc codes appended/prepended to colour it.
     */
    protected static String getColouredString(final String colour, final String line) {
        String res = null;
        if (colour.length() < 3) {
            int num;

            try {
                num = Integer.parseInt(colour);
            } catch (NumberFormatException ex) {
                num = -1;
            }

            if (num >= 0 && num <= 15) {
                res = String.format("%c%02d%s%1$c", Styliser.CODE_COLOUR, num, line);
            }
        } else if (colour.length() == 6) {
            try {
                Color.decode("#" + colour);
                res = String.format("%c%s%s%1$c", Styliser.CODE_HEXCOLOUR, colour, line);
            } catch (NumberFormatException ex) { /* Do Nothing */ }
        }

        if (res == null) {
            res = String.format("%c%02d%s%1$c", Styliser.CODE_COLOUR, 14, line);
        }
        return res;
    }

    /**
     * Add a line to a file.
     *
     * @param filename Name of file to write to
     * @param format Format of line to add. (NewLine will be added Automatically)
     * @param args Arguments for format
     * @return true on success, else false.
     */
    protected boolean appendLine(final String filename, final String format, final Object... args) {
        return appendLine(filename, String.format(format, args));
    }

    /**
     * Add a line to a file.
     *
     * @param filename Name of file to write to
     * @param line Line to add. (NewLine will be added Automatically)
     * @return true on success, else false.
     */
    protected boolean appendLine(final String filename, final String line) {
        final StringBuffer finalLine = new StringBuffer();

        if (IdentityManager.getGlobalConfig().getOptionBool(getDomain(), "general.addtime")) {
            String dateString;
            final String dateFormatString = IdentityManager.getGlobalConfig().getOption(getDomain(), "general.timestamp");
            try {
                final DateFormat dateFormat = new SimpleDateFormat(dateFormatString);
                dateString = dateFormat.format(new Date()).trim();
            } catch (IllegalArgumentException iae) {
                // Default to known good format
                final DateFormat dateFormat = new SimpleDateFormat("[dd/MM/yyyy HH:mm:ss]");
                dateString = dateFormat.format(new Date()).trim();

                Logger.userError(ErrorLevel.LOW, "Dateformat String '" + dateFormatString + "' is invalid. For more information: http://java.sun.com/javase/6/docs/api/java/text/SimpleDateFormat.html");
            }
            finalLine.append(dateString);
            finalLine.append(" ");
        }

        if (IdentityManager.getGlobalConfig().getOptionBool(getDomain(), "general.stripcodes")) {
            finalLine.append(Styliser.stipControlCodes(line));
        } else {
            finalLine.append(line);
        }
        //System.out.println("[Adding] "+filename+" => "+finalLine);
        BufferedWriter out = null;
        try {
            if (openFiles.containsKey(filename)) {
                OpenFile of = openFiles.get(filename);
                of.lastUsedTime = System.currentTimeMillis();
                out = of.writer;
            } else {
                out = new BufferedWriter(new FileWriter(filename, true));
                openFiles.put(filename, new OpenFile(out));
            }
            out.write(finalLine.toString());
            out.newLine();
            out.flush();
            return true;
        } catch (IOException e) {
            /*
             * Do Nothing
             *
             * Makes no sense to keep adding errors to the logger when we can't
             * write to the file, as chances are it will happen on every incomming
             * line.
             */
        }
        return false;
    }

    /**
     * Get the name of the log file for a specific object.
     *
     * @param obj Object to get name for
     * @return the name of the log file to use for this object.
     */
    protected String getLogFile(final Object obj) {
        final StringBuffer directory = new StringBuffer();
        final StringBuffer file = new StringBuffer();
        String md5String = "";

        directory.append(IdentityManager.getGlobalConfig().getOption(getDomain(), "general.directory"));
        if (directory.charAt(directory.length() - 1) != File.separatorChar) {
            directory.append(File.separatorChar);
        }

        if (obj == null) {
            file.append("null.log");
        } else if (obj instanceof ChannelInfo) {
            final ChannelInfo channel = (ChannelInfo) obj;
            if (channel.getParser() != null) {
                addNetworkDir(directory, file, channel.getParser().getNetworkName());
            }
            file.append(sanitise(channel.getName().toLowerCase()));
            md5String = channel.getName();
        } else if (obj instanceof ClientInfo) {
            final ClientInfo client = (ClientInfo) obj;
            if (client.getParser() != null) {
                addNetworkDir(directory, file, client.getParser().getNetworkName());
            }
            file.append(sanitise(client.getNickname().toLowerCase()));
            md5String = client.getNickname();
        } else {
            file.append(sanitise(obj.toString().toLowerCase()));
            md5String = obj.toString();
        }

        if (IdentityManager.getGlobalConfig().getOptionBool(getDomain(), "advanced.usedate")) {
            final String dateFormat = IdentityManager.getGlobalConfig().getOption(getDomain(), "advanced.usedateformat");
            final String dateDir = (new SimpleDateFormat(dateFormat)).format(new Date());
            directory.append(dateDir);
            if (directory.charAt(directory.length() - 1) != File.separatorChar) {
                directory.append(File.separatorChar);
            }

            if (!new File(directory.toString()).exists() && !(new File(directory.toString())).mkdirs()) {
                Logger.userError(ErrorLevel.LOW, "Unable to create date dirs");
            }
        }

        if (IdentityManager.getGlobalConfig().getOptionBool(getDomain(), "advanced.filenamehash")) {
            file.append('.');
            file.append(md5(md5String));
        }
        file.append(".log");

        return directory.toString() + file.toString();
    }

    /**
     * This function adds the networkName to the log file.
     * It first tries to create a directory for each network, if that fails
     * it will prepend the networkName to the filename instead.
     *
     * @param directory Current directory name
     * @param file Current file name
     * @param networkName Name of network
     */
    protected void addNetworkDir(final StringBuffer directory, final StringBuffer file, final String networkName) {
        if (!IdentityManager.getGlobalConfig().getOptionBool(getDomain(), "general.networkfolders")) {
            return;
        }

        final String network = sanitise(networkName.toLowerCase());

        boolean prependNetwork = false;

        // Check dir exists
        final File dir = new File(directory.toString() + network + System.getProperty("file.separator"));
        if (dir.exists() && !dir.isDirectory()) {
            Logger.userError(ErrorLevel.LOW, "Unable to create networkfolders dir (file exists instead)");
            // Prepend network name to file instead.
            prependNetwork = true;
        } else if (!dir.exists() && !dir.mkdirs()) {
            Logger.userError(ErrorLevel.LOW, "Unable to create networkfolders dir");
            prependNetwork = true;
        }

        if (prependNetwork) {
            file.insert(0, " -- ");
            file.insert(0, network);
        } else {
            directory.append(network);
            directory.append(System.getProperty("file.separator"));
        }
    }

    /**
     * Sanitise a string to be used as a filename.
     *
     * @param name String to sanitise
     * @return Sanitised version of name that can be used as a filename.
     */
    protected static String sanitise(final String name) {
        // Replace illegal chars with
        return name.replaceAll("[^\\w\\.\\s\\-\\#\\&\\_]", "_");
    }

    /**
     * Get the md5 hash of a string.
     *
     * @param string String to hash
     * @return md5 hash of given string
     */
    protected static String md5(final String string) {
        try {
            final MessageDigest m = MessageDigest.getInstance("MD5");
            m.update(string.getBytes(), 0, string.length());
            return new BigInteger(1, m.digest()).toString(16);
        } catch (NoSuchAlgorithmException e) {
            return "";
        }
    }

    /**
     * Get name to display for client.
     *
     * @param client The client to get the display name for
     * @return name to display
     */
    protected String getDisplayName(final ClientInfo client) {
        return getDisplayName(client, "");
    }

    /**
     * Get name to display for client.
     *
     * @param client The client to get the display name for
     * @param overrideNick Nickname to display instead of real nickname
     * @return name to display
     */
    protected String getDisplayName(final ClientInfo client, final String overrideNick) {
        if (overrideNick.isEmpty()) {
            return (client == null) ? "Unknown Client" : client.getNickname();
        } else {
            return overrideNick;
        }
    }

    /**
     * Get name to display for channelClient (Taking into account the channelmodeprefix setting).
     *
     * @param channelClient The client to get the display name for
     * @return name to display
     */
    protected String getDisplayName(final ChannelClientInfo channelClient) {
        return getDisplayName(channelClient, "");
    }

    /**
     * Get name to display for channelClient (Taking into account the channelmodeprefix setting).
     *
     * @param channelClient The client to get the display name for
     * @param overrideNick Nickname to display instead of real nickname
     * @return name to display
     */
    protected String getDisplayName(final ChannelClientInfo channelClient, final String overrideNick) {
        final boolean addModePrefix = (IdentityManager.getGlobalConfig().getOptionBool(getDomain(), "general.channelmodeprefix"));

        if (channelClient == null) {
            return (overrideNick.isEmpty()) ? "Unknown Client" : overrideNick;
        } else if (overrideNick.isEmpty()) {
            return (addModePrefix) ? channelClient.toString() : channelClient.getClient().getNickname();
        } else {
            return (addModePrefix) ? channelClient.getImportantModePrefix() + overrideNick : overrideNick;
        }
    }

    /**
     * Shows the history window for the specified target, if available.
     *
     * @param target The window whose history we're trying to open
     * @return True if the history is available, false otherwise
     */
    protected boolean showHistory(final InputWindow target) {
        Object component;

        if (target.getContainer() instanceof Channel) {
            component = ((Channel) target.getContainer()).getChannelInfo();
        } else if (target.getContainer() instanceof Query) {
            final Parser parser = ((Query) target.getContainer()).getServer().getParser();
            component = parser.getClient(((Query) target.getContainer()).getHost());
        } else if (target.getContainer() instanceof Server) {
            component = target.getContainer().getServer().getParser();
        } else {
            // Unknown component
            return false;
        }

        final String log = getLogFile(component);

        if (!new File(log).exists()) {
            // File doesn't exist
            return false;
        }

        ReverseFileReader reader;

        try {
            reader = new ReverseFileReader(log);
        } catch (FileNotFoundException ex) {
            return false;
        } catch (IOException ex) {
            return false;
        } catch (SecurityException ex) {
            return false;
        }

        new HistoryWindow("History", reader, target,
                IdentityManager.getGlobalConfig().getOptionInt(getDomain(), "history.lines"));

        return true;
    }

}
