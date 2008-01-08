/*
 * Copyright (c) 2006-2008 Chris Smith, Shane Mc Cormack, Gregory Holmes
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
import com.dmdirc.actions.interfaces.ActionType;
import com.dmdirc.actions.CoreActionType;
import com.dmdirc.commandparser.CommandManager;
import com.dmdirc.config.Identity;
import com.dmdirc.config.IdentityManager;
import com.dmdirc.interfaces.ActionListener;
import com.dmdirc.logger.ErrorLevel;
import com.dmdirc.logger.Logger;
import com.dmdirc.parser.ChannelClientInfo;
import com.dmdirc.parser.ChannelInfo;
import com.dmdirc.parser.ClientInfo;
import com.dmdirc.parser.IRCParser;
import com.dmdirc.plugins.Plugin;
import com.dmdirc.ui.interfaces.InputWindow;
import com.dmdirc.ui.interfaces.PreferencesInterface;
import com.dmdirc.ui.interfaces.PreferencesPanel;
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
import java.util.Properties;
import java.util.Stack;


/**
 * Adds logging facility to client.
 *
 * @author Shane 'Dataforce' McCormack
 * @version $Id: LoggingPlugin.java 969 2007-04-30 18:38:20Z ShaneMcC $
 */
public final class LoggingPlugin extends Plugin implements ActionListener, PreferencesInterface {
	/** What domain do we store all settings in the global config under. */
	private static final String MY_DOMAIN = "plugin-Logging";
	
	/** The command we registered */
	private LoggingCommand command;
	
	/** Hashtable of open files */
	Hashtable<String,BufferedWriter> openFiles = new Hashtable<String,BufferedWriter>();
	
	/**
	 * Creates a new instance of the Logging Plugin.
	 */
	public LoggingPlugin() { super(); }
	
	/**
	 * Called when the plugin is loaded.
	 */
	@Override
	public void onLoad() {
		// Set defaults
		Properties defaults = new Properties();
		defaults.setProperty(MY_DOMAIN + ".general.directory", Main.getConfigDir() + "logs" + System.getProperty("file.separator"));
		defaults.setProperty(MY_DOMAIN + ".general.networkfolders", "true");
		defaults.setProperty(MY_DOMAIN + ".advanced.filenamehash", "false");
		defaults.setProperty(MY_DOMAIN + ".general.addtime", "true");
		defaults.setProperty(MY_DOMAIN + ".general.timestamp", "[dd/MM/yyyy HH:mm:ss]");
		defaults.setProperty(MY_DOMAIN + ".general.stripcodes", "true");
		defaults.setProperty(MY_DOMAIN + ".general.channelmodeprefix", "true");
		defaults.setProperty(MY_DOMAIN + ".backbuffer.autobackbuffer", "true");
		defaults.setProperty(MY_DOMAIN + ".backbuffer.lines", "10");
		defaults.setProperty(MY_DOMAIN + ".backbuffer.colour", "14");
		defaults.setProperty(MY_DOMAIN + ".backbuffer.timestamp", "false");
		defaults.setProperty(MY_DOMAIN + ".history.lines", "50000");
		defaults.setProperty("identity.name", "Logging Plugin Defaults");
		IdentityManager.addIdentity(new Identity(defaults));
		
		final File dir = new File(IdentityManager.getGlobalConfig().getOption(MY_DOMAIN, "general.directory"));
		if (!dir.exists()) {
			try {
				dir.mkdirs();
				dir.createNewFile();
			} catch (IOException ex) {
				Logger.userError(ErrorLevel.LOW, "Unable to create logging dir");
			}
		} else {
			if (!dir.isDirectory()) {
				Logger.userError(ErrorLevel.LOW, "Unable to create logging dir (file exists instead)");
			}
		}
		
		command = new LoggingCommand();
		ActionManager.addListener(this, CoreActionType.SERVER_CONNECTED, CoreActionType.QUERY_CLOSED, CoreActionType.CHANNEL_CLOSED, CoreActionType.QUERY_OPENED, CoreActionType.CHANNEL_OPENED, CoreActionType.QUERY_MESSAGE, CoreActionType.QUERY_SELF_MESSAGE, CoreActionType.QUERY_ACTION, CoreActionType.QUERY_SELF_ACTION, CoreActionType.CHANNEL_MESSAGE, CoreActionType.CHANNEL_SELF_MESSAGE, CoreActionType.CHANNEL_ACTION, CoreActionType.CHANNEL_SELF_ACTION, CoreActionType.CHANNEL_GOTTOPIC, CoreActionType.CHANNEL_TOPICCHANGE, CoreActionType.CHANNEL_JOIN, CoreActionType.CHANNEL_PART, CoreActionType.CHANNEL_QUIT, CoreActionType.CHANNEL_KICK, CoreActionType.CHANNEL_NICKCHANGE, CoreActionType.CHANNEL_MODECHANGE);
	}
	
	/**
	 * Called when this plugin is unloaded.
	 */
	@Override
	public void onUnload() {
		CommandManager.unregisterCommand(command);
		ActionManager.removeListener(this);
		
		BufferedWriter file;
		synchronized (openFiles) {
			for (String filename : openFiles.keySet()) {
				file = openFiles.get(filename);
				try {
					file.close();
				} catch (IOException e) {
					Logger.userError(ErrorLevel.LOW, "Unable to close file (File: "+filename+")");
				}
			}
			openFiles.clear();
		}
	}
	
	/**
	 * Called to see if the plugin has configuration options (via dialog).
	 *
	 * @return true if the plugin has configuration options via a dialog.
	 */
	@Override
	public boolean isConfigurable() { return true; }
	
	/**
	 * Called to show the Configuration dialog of the plugin if appropriate.
	 */
	@Override
	public void showConfig() {
		final PreferencesPanel preferencesPanel = Main.getUI().getPreferencesPanel(this, "Logging Plugin - Config");
		preferencesPanel.addCategory("General", "General configuration for Logging plugin.");
		preferencesPanel.addCategory("Back Buffer", "Options related to the automatic backbuffer");
		preferencesPanel.addCategory("Advanced", "Advanced configuration for Logging plugin. You shouldn't need to edit this unless you know what you are doing.");
		
		preferencesPanel.addTextfieldOption("General", "general.directory", "Directory: ", "Directory for log files", IdentityManager.getGlobalConfig().getOption(MY_DOMAIN, "general.directory"));
		preferencesPanel.addCheckboxOption("General", "general.networkfolders", "Separate logs by network: ", "Should the files be stored in a sub-dir with the networks name", IdentityManager.getGlobalConfig().getOptionBool(MY_DOMAIN, "general.networkfolders"));
		preferencesPanel.addCheckboxOption("General", "general.addtime", "Timestamp logs: ", "Should a timestamp be added to the log files", IdentityManager.getGlobalConfig().getOptionBool(MY_DOMAIN, "general.addtime"));
		preferencesPanel.addTextfieldOption("General", "general.timestamp", "Timestamp format: ", "The String to pass to 'SimpleDateFormat' to format the timestamp", IdentityManager.getGlobalConfig().getOption(MY_DOMAIN, "general.timestamp"));
		preferencesPanel.addCheckboxOption("General", "general.stripcodes", "Strip Control Codes: ", "Remove known irc control codes from lines before saving", IdentityManager.getGlobalConfig().getOptionBool(MY_DOMAIN, "general.stripcodes"));
		preferencesPanel.addCheckboxOption("General", "general.channelmodeprefix", "Show channel mode prefix: ", "Show the @,+ etc next to nicknames", IdentityManager.getGlobalConfig().getOptionBool(MY_DOMAIN, "general.channelmodeprefix"));
		
		preferencesPanel.addCheckboxOption("Back Buffer", "backbuffer.autobackbuffer", "Automatically display: ", "Automatically display the backbuffer when a channel is joined", IdentityManager.getGlobalConfig().getOptionBool(MY_DOMAIN, "backbuffer.autobackbuffer"));
		preferencesPanel.addColourOption("Back Buffer", "backbuffer.colour", "Colour to use for display: ", "Colour used when displaying the backbuffer", IdentityManager.getGlobalConfig().getOption(MY_DOMAIN, "backbuffer.colour"), true, true);
		preferencesPanel.addSpinnerOption("Back Buffer", "backbuffer.lines", "Number of lines to show: ", "Number of lines used when displaying backbuffer", IdentityManager.getGlobalConfig().getOptionInt(MY_DOMAIN, "backbuffer.lines", 0));
		preferencesPanel.addCheckboxOption("Back Buffer", "backbuffer.timestamp", "Show Formatter-Timestamp: ", "Should the line be added to the frame with the timestamp from the formatter aswell as the file contents", IdentityManager.getGlobalConfig().getOptionBool(MY_DOMAIN, "backbuffer.timestamp"));
		
		preferencesPanel.addCheckboxOption("Advanced", "advanced.filenamehash", "Add Filename hash: ", "Add the MD5 hash of the channel/client name to the filename. (This is used to allow channels with similar names (ie a _ not a  -) to be logged separately)", IdentityManager.getGlobalConfig().getOptionBool(MY_DOMAIN, "advanced.filenamehash"));
		
		preferencesPanel.display();
	}
	
	/**
	 * Get the name of the domain we store all settings in the global config under.
	 *
	 * @return the plugins domain
	 */
	protected static String getDomain() { return MY_DOMAIN; }
	
	/**
	 * Copy the new vaule of an option to the global config.
	 *
	 * @param properties Source of option value, or null if setting default values
	 * @param name name of option
	 */
	protected void updateOption(final Properties properties, final String name) {
		String value = null;
		
		// Get the value from the properties file if one is given, else use the
		// value from the global config.
		if (properties != null) {
			value = properties.getProperty(name);
		} else {
			value = IdentityManager.getGlobalConfig().getOption(MY_DOMAIN, name);
		}
		
		// Check if the Value exists
		if (value != null) {
			// It does, so update the global config with the new value
			IdentityManager.getConfigIdentity().setOption(MY_DOMAIN, name, value);
		}
	}
	
	/**
	 * Called when the preferences dialog is closed.
	 *
	 * @param properties user preferences
	 */
	@Override
	public void configClosed(final Properties properties) {
		
		// Update Config options
		updateOption(properties, "general.networkfolders");
		updateOption(properties, "advanced.filenamehash");
		updateOption(properties, "general.addtime");
		updateOption(properties, "general.timestamp");
		updateOption(properties, "general.stripcodes");
		updateOption(properties, "general.channelmodeprefix");
		updateOption(properties, "backbuffer.autobackbuffer");
		updateOption(properties, "backbuffer.lines");
		updateOption(properties, "backbuffer.colour");
		updateOption(properties, "backbuffer.timestamp");
		
		// Check new dir exists before changing
		final File dir = new File(properties.getProperty("general.directory"));
		if (!dir.exists()) {
			try {
				dir.mkdirs();
				dir.createNewFile();
				updateOption(properties, "general.directory");
			} catch (IOException ex) {
				Logger.userError(ErrorLevel.LOW, "Unable to create new logging dir, not changing");
			}
		} else {
			if (!dir.isDirectory()) {
				Logger.userError(ErrorLevel.LOW, "Unable to create new logging dir (file exists instead), not changing");
			} else {
				updateOption(properties, "general.directory");
			}
		}
	}
	
	
	/**
	 * Called when the preferences dialog is cancelled.
	 */
	@Override
	public void configCancelled() { }
	
	/**
	 * Process an event of the specified type.
	 *
	 * @param type The type of the event to process
	 * @param format Format of messages that are about to be sent. (May be null)
	 * @param arguments The arguments for the event
	 */
	@Override
	public void processEvent(final ActionType type, final StringBuffer format, final Object ... arguments) {
		if (type instanceof CoreActionType) {
			final CoreActionType thisType = (CoreActionType) type;
			ChannelInfo channel;
			ChannelClientInfo channelClient;
			ClientInfo client;
			IRCParser parser;
			String line = "";
			String reason = "";
			Query query;
			final DateFormat openedAtFormat = new SimpleDateFormat("EEEE MMMM dd, yyyy - HH:mm:ss");
			
			switch (thisType) {
				case SERVER_CONNECTED:
					// Do Nothing
					break;
				case QUERY_CLOSED:
					// ActionManager.processEvent(CoreActionType.QUERY_CLOSED, this);
					query = (Query)arguments[0];
					if (query.getServer() == null) {
						Logger.appError(ErrorLevel.MEDIUM, "Query object has no server ("+thisType.toString()+")", new Exception("Query object has no server ("+thisType.toString()+")"));
						break;
					}
					parser = query.getServer().getParser();
					client = parser.getClientInfo(query.getHost());
					if (client == null) {
						client = new ClientInfo(parser, query.getHost()).setFake(true);
					}
					line = getLogFile(client);
					if (openFiles.containsKey(line)) {
						appendLine(line, "*** Query closed at: " + openedAtFormat.format(new Date()));
						BufferedWriter file = openFiles.get(line);
						try {
							file.close();
						} catch (IOException e) {
							Logger.userError(ErrorLevel.LOW, "Unable to close file (Filename: "+line+")");
						}
						openFiles.remove(line);
					}
					break;
				case CHANNEL_CLOSED:
					// ActionManager.processEvent(CoreActionType.CHANNEL_CLOSED, this);
					channel = ((Channel)arguments[0]).getChannelInfo();
					
					line = getLogFile(channel);
					if (openFiles.containsKey(line)) {
						appendLine(line, "*** Channel closed at: " + openedAtFormat.format(new Date()));
						BufferedWriter file = openFiles.get(line);
						try {
							file.close();
						} catch (IOException e) {
							Logger.userError(ErrorLevel.LOW, "Unable to close file (Filename: "+line+")");
						}
						openFiles.remove(line);
					}
					break;
				case QUERY_OPENED:
					// ActionManager.processEvent(CoreActionType.QUERY_OPENED, this);
					query = (Query)arguments[0];
					if (query.getServer() == null) {
						Logger.appError(ErrorLevel.MEDIUM, "Query object has no server ("+thisType.toString()+")", new Exception("Query object has no server ("+thisType.toString()+")"));
						break;
					}
					parser = query.getServer().getParser();
					client = parser.getClientInfo(query.getHost());
					if (client == null) {
						client = new ClientInfo(parser, query.getHost()).setFake(true);
					}
					
					// Backbuffer Display goes here!
					if (IdentityManager.getGlobalConfig().getOptionBool(MY_DOMAIN, "backbuffer.autobackbuffer")) {
						showBackBuffer(query.getFrame(), getLogFile(client));
					}
					
					appendLine(getLogFile(client), "*** Query opened at: " + openedAtFormat.format(new Date()));
					appendLine(getLogFile(client), "*** Query with User: " + query.getHost());
					appendLine(getLogFile(client), "");
					break;
				case CHANNEL_OPENED:
					// ActionManager.processEvent(CoreActionType.CHANNEL_OPENED, this);
					channel = ((Channel)arguments[0]).getChannelInfo();
					
					// Backbuffer Display goes here!
					if (IdentityManager.getGlobalConfig().getOptionBool(MY_DOMAIN, "backbuffer.autobackbuffer")) {
						showBackBuffer(((Channel)arguments[0]).getFrame(), getLogFile(channel));
					}
					
					appendLine(getLogFile(channel), "*** Channel opened at: " + openedAtFormat.format(new Date()));
					appendLine(getLogFile(channel), "");
					break;
				case QUERY_MESSAGE:
				case QUERY_SELF_MESSAGE:
				case QUERY_ACTION:
				case QUERY_SELF_ACTION:
					// ActionManager.processEvent(CoreActionType.QUERY_MESSAGE, this, message);
					query = (Query)arguments[0];
					if (query.getServer() == null) {
						Logger.appError(ErrorLevel.MEDIUM, "Query object has no server ("+thisType.toString()+")", new Exception("Query object has no server ("+thisType.toString()+")"));
						break;
					}
					parser = query.getServer().getParser();
					String overrideNick = "";
					if (thisType == CoreActionType.QUERY_SELF_MESSAGE || thisType == CoreActionType.QUERY_SELF_ACTION) {
						overrideNick = getDisplayName(parser.getMyself());
					}
					client = parser.getClientInfo(query.getHost());
					if (client == null) {
						client = new ClientInfo(parser, query.getHost()).setFake(true);
					}
					if (thisType == CoreActionType.QUERY_MESSAGE || thisType == CoreActionType.QUERY_SELF_MESSAGE) {
						appendLine(getLogFile(client), "<" + getDisplayName(client, overrideNick) + "> " + arguments[1]);
					} else {
						appendLine(getLogFile(client), "* " + getDisplayName(client, overrideNick) + " " + arguments[1]);
					}
					break;
				case CHANNEL_MESSAGE:
				case CHANNEL_SELF_MESSAGE:
				case CHANNEL_ACTION:
				case CHANNEL_SELF_ACTION:
					// ActionManager.processEvent(CoreActionType.CHANNEL_MESSAGE, this, cChannelClient, sMessage);
					channel = ((Channel)arguments[0]).getChannelInfo();
					channelClient = (ChannelClientInfo) arguments[1];
					if (thisType == CoreActionType.CHANNEL_MESSAGE || thisType == CoreActionType.CHANNEL_SELF_MESSAGE) {
						appendLine(getLogFile(channel), "<" + getDisplayName(channelClient) + "> " + (String) arguments[2]);
					} else {
						appendLine(getLogFile(channel), "* " + getDisplayName(channelClient) + " " + (String) arguments[2]);
					}
					break;
				case CHANNEL_GOTTOPIC:
					// ActionManager.processEvent(CoreActionType.CHANNEL_GOTTOPIC, this);
					channel = ((Channel)arguments[0]).getChannelInfo();
					final DateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
					final DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
					final String topicTime = timeFormat.format(1000 * channel.getTopicTime()) + " on " + dateFormat.format(1000 * channel.getTopicTime());
					
					appendLine(getLogFile(channel), "*** Topic is: " + channel.getTopic());
					appendLine(getLogFile(channel), "*** Set at: " + topicTime + " by " + channel.getTopicUser());
					break;
				case CHANNEL_TOPICCHANGE:
					// ActionManager.processEvent(CoreActionType.CHANNEL_TOPICCHANGE, this, user, topic);
					channel = ((Channel)arguments[0]).getChannelInfo();
					channelClient = (ChannelClientInfo) arguments[1];
					appendLine(getLogFile(channel), "*** " + getDisplayName(channelClient) + " Changed the topic to: " + (String) arguments[2]);
					break;
				case CHANNEL_JOIN:
					// ActionManager.processEvent(CoreActionType.CHANNEL_JOIN, this, cChannelClient);
					channel = ((Channel)arguments[0]).getChannelInfo();
					channelClient = (ChannelClientInfo) arguments[1];
					client = channelClient.getClient();
					appendLine(getLogFile(channel), "*** " + getDisplayName(channelClient) + " (" + client.toString() + ") joined the channel");
					break;
				case CHANNEL_PART:
					// ActionManager.processEvent(CoreActionType.CHANNEL_PART, this, cChannelClient, sReason);
					channel = ((Channel)arguments[0]).getChannelInfo();
					channelClient = (ChannelClientInfo) arguments[1];
					client = channelClient.getClient();
					line = "*** " + getDisplayName(channelClient) + " (" + client.toString() + ") left the channel";
					
					reason = (String) arguments[2];
					if (!reason.isEmpty()) {
						line = line + " (" + reason + ")";
					}
					appendLine(getLogFile(channel), line);
					break;
				case CHANNEL_QUIT:
					// ActionManager.processEvent(CoreActionType.CHANNEL_QUIT, this, cChannelClient, sReason);
					channel = ((Channel)arguments[0]).getChannelInfo();
					channelClient = (ChannelClientInfo) arguments[1];
					client = channelClient.getClient();
					line = "*** " + getDisplayName(channelClient) + " (" + client.toString() + ") Quit IRC";
					
					reason = (String) arguments[2];
					if (!reason.isEmpty()) {
						line = line + " (" + reason + ")";
					}
					appendLine(getLogFile(channel), line);
					break;
				case CHANNEL_KICK:
					// ActionManager.processEvent(CoreActionType.CHANNEL_KICK, this, cKickedByClient, cKickedClient, sReason);
					channel = ((Channel)arguments[0]).getChannelInfo();
					channelClient = (ChannelClientInfo) arguments[2];
					final ChannelClientInfo kickerClient = (ChannelClientInfo) arguments[1];
					line = "*** " + getDisplayName(channelClient) + " was kicked by " + getDisplayName(kickerClient);
					
					reason = (String) arguments[3];
					if (!reason.isEmpty()) {
						line = line + " (" + reason + ")";
					}
					appendLine(getLogFile(channel), line);
					break;
				case CHANNEL_NICKCHANGE:
					// ActionManager.processEvent(CoreActionType.CHANNEL_NICKCHANGE, this, cChannelClient, sOldNick);
					channel = ((Channel)arguments[0]).getChannelInfo();
					channelClient = (ChannelClientInfo) arguments[1];
					final String oldnick = (String) arguments[2];
					appendLine(getLogFile(channel), "*** " + getDisplayName(channelClient, oldnick) + " is now " + getDisplayName(channelClient));
					break;
				case CHANNEL_MODECHANGE:
					// ActionManager.processEvent(CoreActionType.CHANNEL_MODECHANGE, this, cChannelClient, sModes);
					channel = ((Channel)arguments[0]).getChannelInfo();
					channelClient = (ChannelClientInfo) arguments[1];
					if (channelClient.getNickname().isEmpty()) {
						appendLine(getLogFile(channel), "*** Channel modes are: " + (String) arguments[2]);
					} else {
						appendLine(getLogFile(channel), "*** " + getDisplayName(channelClient) + " set modes: " + (String) arguments[2]);
					}
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
	private void showBackBuffer(final Window frame, final String filename) {
		final int numLines = IdentityManager.getGlobalConfig().getOptionInt(MY_DOMAIN, "backbuffer.lines", 0);
		final String colour = IdentityManager.getGlobalConfig().getOption(MY_DOMAIN, "backbuffer.colour");
		final boolean showTimestamp = IdentityManager.getGlobalConfig().getOptionBool(MY_DOMAIN, "backbuffer.timestamp");
		if (frame == null) {
			Logger.userError(ErrorLevel.LOW, "Given a null frame");
			return;
		}
		
		File testFile = new File(filename);
		if (testFile.exists()) {
			try {
				ReverseFileReader file = new ReverseFileReader(testFile);
				// Because the file includes a newline char at the end, an empty line
				// is returned by getLines. To counter this, we call getLines(1) and do
				// nothing with the output.
				file.getLines(1);
				Stack<String> lines = file.getLines(numLines);
				while (!lines.empty()) {
					frame.addLine(getColouredString(colour,lines.pop()), showTimestamp);
				}
				file.close();
				frame.addLine(getColouredString(colour,"--- End of backbuffer\n"), showTimestamp);
			} catch (Exception e) {
				Logger.userError(ErrorLevel.LOW, "Unable to show backbuffer (Filename: "+filename+"): " + e.getMessage());
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
	private String getColouredString(String colour, String line) {
		String res = null;
		if (colour.length() < 3) {
			int num;
			
			try {
				num = Integer.parseInt(colour);
			} catch (NumberFormatException ex) {
				num = -1;
			}
			
			if (num >= 0 && num <= 15) {
				if (num > 10) {
					res = Styliser.CODE_COLOUR+""+num+""+line+Styliser.CODE_COLOUR;
				} else {
					res = Styliser.CODE_COLOUR+"0"+num+""+line+Styliser.CODE_COLOUR;
				}
			}
		} else if (colour.length() == 6) {
			try {
				Color.decode("#" + colour);
				res = Styliser.CODE_HEXCOLOUR+""+colour+""+line+Styliser.CODE_HEXCOLOUR;
			} catch (NumberFormatException ex) { /* Do Nothing */ }
		}
		
		if (res == null) {
			res = Styliser.CODE_COLOUR+"14"+line+""+Styliser.CODE_COLOUR;
		}
		return res;
	}
	
	
	/**
	 * Add a line to a file.
	 *
	 * @param filename Name of file to write to
	 * @param line Line to add. (NewLine will be added Automatically)
	 * @return true on success, else false.
	 */
	private boolean appendLine(final String filename, final String line) {
		String finalLine = line;
		if (IdentityManager.getGlobalConfig().getOptionBool(MY_DOMAIN, "general.addtime")) {
			final DateFormat dateFormat = new SimpleDateFormat(IdentityManager.getGlobalConfig().getOption(MY_DOMAIN, "general.timestamp"));
			finalLine = dateFormat.format(new Date()) + " " + finalLine;
		}
		if (IdentityManager.getGlobalConfig().getOptionBool(MY_DOMAIN, "general.stripcodes")) {
			finalLine = Styliser.stipControlCodes(finalLine);
		}
		//System.out.println("[Adding] "+filename+" => "+finalLine);
		BufferedWriter out = null;
		try {
			if (openFiles.containsKey(filename)) {
				out = openFiles.get(filename);
			} else {
				out = new BufferedWriter(new FileWriter(filename, true));
				openFiles.put(filename, out);
			}
			out.write(finalLine);
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
	private String getLogFile(final Object obj) {
		String result = IdentityManager.getGlobalConfig().getOption(MY_DOMAIN, "general.directory");
		
		if (obj == null) {
			result = result + "null.log";
		} else if (obj instanceof ChannelInfo) {
			final ChannelInfo channel = (ChannelInfo) obj;
			result = addNetworkDir(result, channel.getParser().getNetworkName());
			result = result + sanitise(channel.getName().toLowerCase());
			if (IdentityManager.getGlobalConfig().getOptionBool(MY_DOMAIN, "advanced.filenamehash")) {
				result = result + '.' + md5(channel.getName());
			}
			result = result + ".log";
		} else if (obj instanceof ClientInfo) {
			final ClientInfo client = (ClientInfo) obj;
			result = addNetworkDir(result, client.getParser().getNetworkName());
			result = result + sanitise(client.getNickname().toLowerCase());
			if (IdentityManager.getGlobalConfig().getOptionBool(MY_DOMAIN, "advanced.filenamehash")) {
				result = result + '.' + md5(client.getNickname());
			}
			result = result + ".log";
		} else if (obj instanceof IRCParser) {
			final IRCParser parser = (IRCParser) obj;
			result = addNetworkDir(result, parser.getNetworkName());
			result = result + "log";
			if (IdentityManager.getGlobalConfig().getOptionBool(MY_DOMAIN, "advanced.filenamehash")) {
				result = result + '.' + md5("log");
			}
			result = result + ".log";
		} else {
			result = result + sanitise(obj.toString().toLowerCase());
			if (IdentityManager.getGlobalConfig().getOptionBool(MY_DOMAIN, "advanced.filenamehash")) {
				result = result + '.' + md5(obj.toString());
			}
			result = result + ".log";
		}
		return result;
	}
	
	/**
	 * Get name to display for client.
	 *
	 * @param client The client to get the display name for
	 * @return name to display
	 */
	private String getDisplayName(final ClientInfo client) {
		return getDisplayName(client, "");
	}
	
	/**
	 * Get name to display for client.
	 *
	 * @param client The client to get the display name for
	 * @param overrideNick Nickname to display instead of real nickname
	 * @return name to display
	 */
	private String getDisplayName(final ClientInfo client, final String overrideNick) {
		String result = "";
		if (client == null) {
			if (overrideNick.isEmpty()) {
				result = "Unknown Client";
			} else {
				result = overrideNick;
			}
		} else {
			if (overrideNick.isEmpty()) {
				result = client.getNickname();
			} else {
				result = overrideNick;
			}
		}
		return result;
	}
	
	/**
	 * Get name to display for channelClient (Taking into account the channelmodeprefix setting).
	 *
	 * @param channelClient The client to get the display name for
	 * @return name to display
	 */
	private String getDisplayName(final ChannelClientInfo channelClient) {
		return getDisplayName(channelClient, "");
	}
	
	/**
	 * Get name to display for channelClient (Taking into account the channelmodeprefix setting).
	 *
	 * @param channelClient The client to get the display name for
	 * @param overrideNick Nickname to display instead of real nickname
	 * @return name to display
	 */
	private String getDisplayName(final ChannelClientInfo channelClient, final String overrideNick) {
		String result = "";
		if (channelClient == null) {
			if (overrideNick.isEmpty()) {
				result = "Unknown Client";
			} else {
				result = overrideNick;
			}
		} else {
			if (overrideNick.isEmpty()) {
				if (IdentityManager.getGlobalConfig().getOptionBool(MY_DOMAIN, "general.channelmodeprefix")) {
					result = channelClient.toString();
				} else {
					result = channelClient.getNickname();
				}
			} else {
				result = overrideNick;
				if (IdentityManager.getGlobalConfig().getOptionBool(MY_DOMAIN, "general.channelmodeprefix")) {
					result = channelClient.getImportantModePrefix() + result;
				}
			}
		}
		return result;
	}
	
	/**
	 * This function adds the networkName to the log file.
	 * It first tries to create a directory for each network, if that fails
	 * it will prepend the networkName to the filename instead.
	 *
	 * @param input Current filename (Logging directory)
	 * @param networkName Name of network
	 * @return Updated filename to include network name
	 */
	private String addNetworkDir(final String input, final String networkName) {
		String result = input;
		if (IdentityManager.getGlobalConfig().getOptionBool(MY_DOMAIN, "general.networkfolders")) {
			result = result + sanitise(networkName.toLowerCase()) + System.getProperty("file.separator");
			// Check dir exists
			final File dir = new File(result);
			if (!dir.exists()) {
				try {
					dir.mkdirs();
					dir.createNewFile();
				} catch (IOException ex) {
					Logger.userError(ErrorLevel.LOW, "Unable to create networkfolders dir");
					// Prepend network name instead.
					result = input + sanitise(networkName.toLowerCase()) + " -- ";
				}
			} else {
				if (!dir.isDirectory()) {
					Logger.userError(ErrorLevel.LOW, "Unable to create networkfolders dir (file exists instead)");
					// Prepend network name instead.
					result = input + sanitise(networkName.toLowerCase()) + " -- ";
				}
			}
		}
		return result;
	}
	
	/**
	 * Sanitise a string to be used as a filename.
	 *
	 * @param name String to sanitise
	 * @return Sanitised version of name that can be used as a filename.
	 */
	private String sanitise(final String name) {
		// Replace illegal chars with
		return name.replaceAll("[^\\w\\.\\s\\-\\#\\&\\_]", "_");
	}
	
	/**
	 * Get the md5 hash of a string.
	 *
	 * @param string String to hash
	 * @return md5 hash of given string
	 */
	private String md5(final String string) {
		try {
			final MessageDigest m = MessageDigest.getInstance("MD5");
			m.update(string.getBytes(), 0, string.length());
			return new BigInteger(1, m.digest()).toString(16);
		} catch (NoSuchAlgorithmException e) {
			return "";
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
			final IRCParser parser = ((Query) target.getContainer()).getServer().getParser();
			component = parser.getClientInfo(((Query) target.getContainer()).getHost());
			if (component == null) {
				component = new ClientInfo(parser, ((Query) target.getContainer()).getHost()).setFake(true);
			}
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
		
		new HistoryWindow("History", reader, target);
		
		return true;
	}
	
	/**
	 * Get SVN Version information.
	 *
	 * @return SVN Version String
	 */
	public static String getSvnInfo() { return "$Id: IRCParser.java 969 2007-04-30 18:38:20Z ShaneMcC $"; }
	
}

