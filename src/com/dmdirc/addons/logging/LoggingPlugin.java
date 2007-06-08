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

package com.dmdirc.addons.logging;

import java.awt.Color;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Stack;
import java.util.Properties;
import java.util.Hashtable;

import com.dmdirc.Channel;
import com.dmdirc.Config;
import com.dmdirc.Query;
import com.dmdirc.actions.ActionType;
import com.dmdirc.actions.CoreActionType;
import com.dmdirc.commandparser.CommandWindow;
import com.dmdirc.logger.ErrorLevel;
import com.dmdirc.logger.Logger;
import com.dmdirc.parser.ChannelClientInfo;
import com.dmdirc.parser.ChannelInfo;
import com.dmdirc.parser.ClientInfo;
import com.dmdirc.parser.IRCParser;
import com.dmdirc.plugins.Plugin;
import com.dmdirc.plugins.EventPlugin;
import com.dmdirc.ui.components.Frame;
import com.dmdirc.ui.components.PreferencesInterface;
import com.dmdirc.ui.components.PreferencesPanel;
import com.dmdirc.ui.messages.Styliser;
import com.dmdirc.ui.messages.ColourManager;

/**
 * Adds logging facility to client.
 *
 * @author Shane 'Dataforce' McCormack
 * @version $Id: LoggingPlugin.java 969 2007-04-30 18:38:20Z ShaneMcC $
 */
public final class LoggingPlugin extends Plugin implements EventPlugin, PreferencesInterface {
	/** What domain do we store all settings in the global config under. */
	private static final String MY_DOMAIN = "plugin-Logging";
	
	/** Hashtable of open files */
	Hashtable<String,BufferedWriter> openFiles = new Hashtable<String,BufferedWriter>();

	/**
	 * Creates a new instance of the Logging Plugin.
	 */
	public LoggingPlugin() { super(); }

	/**
	 * Called when the plugin is loaded.
	 *
	 * @return false if the plugin can not be loaded
	 */
	public boolean onLoad() {
		final Properties config = Config.getConfig();
		
		// Set default options if they don't exist
		updateOption(config, "general.directory", Config.getConfigDir() + "logs" + System.getProperty("file.separator"));
		updateOption(config, "general.networkfolders", "true");
		updateOption(config, "advanced.filenamehash", "false");
		updateOption(config, "general.addtime", "true");
		updateOption(config, "general.timestamp", "[dd/MM/yyyy HH:mm:ss]");
		updateOption(config, "general.stripcodes", "true");
		updateOption(config, "general.channelmodeprefix", "true");
		updateOption(config, "backbuffer.autobackbuffer", "true");
		updateOption(config, "backbuffer.lines", "10");
		updateOption(config, "backbuffer.colour", "14");
		updateOption(config, "backbuffer.timestamp", "false");
		
		final File dir = new File(Config.getOption(MY_DOMAIN, "general.directory"));
		if (!dir.exists()) {
			try {
				dir.mkdirs();
				dir.createNewFile();
			} catch (IOException ex) {
				Logger.error(ErrorLevel.ERROR, "Unable to create logging dir", ex);
			}
		} else {
			if (!dir.isDirectory()) {
				Logger.error(ErrorLevel.ERROR, "Unable to create logging dir (file exists instead)");
			}
		}
		
		new LoggingCommand();
		
		return true;
	}
	
	/**
	 * Called when this plugin is deactivated.
	 */
	public void onDeactivate() {
		BufferedWriter file;
		synchronized (openFiles) {
			for (String filename : openFiles.keySet()) {
				file = openFiles.get(filename);
				try {
					file.close();
				} catch (IOException e) {
					Logger.error(ErrorLevel.ERROR, "Unable to close file (File: "+filename+")", e);
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
	public boolean isConfigurable() { return true; }
	
	/**
	 * Called to show the Configuration dialog of the plugin if appropriate.
	 */
	public void showConfig() {
		final PreferencesPanel preferencesPanel = new PreferencesPanel(this, "Logging Plugin - Config");
		preferencesPanel.addCategory("General", "General configuration for Logging plugin.");
		preferencesPanel.addCategory("Back Buffer", "Options related to the automatic backbuffer");
		preferencesPanel.addCategory("Advanced", "Advanced configuration for Logging plugin. You shouldn't need to edit this unless you know what you are doing.");

		preferencesPanel.addTextfieldOption("General", "general.directory", "Directory: ", "Directory for log files", Config.getOption(MY_DOMAIN, "general.directory"));
		preferencesPanel.addCheckboxOption("General", "general.networkfolders", "Separate logs by network: ", "Should the files be stored in a sub-dir with the networks name", Config.getOptionBool(MY_DOMAIN, "general.networkfolders"));
		preferencesPanel.addCheckboxOption("General", "general.addtime", "Timestamp logs: ", "Should a timestamp be added to the log files", Config.getOptionBool(MY_DOMAIN, "general.addtime"));
		preferencesPanel.addTextfieldOption("General", "general.timestamp", "Timestamp format: ", "The String to pass to 'SimpleDateFormat' to format the timestamp", Config.getOption(MY_DOMAIN, "general.timestamp"));
		preferencesPanel.addCheckboxOption("General", "general.stripcodes", "Strip Control Codes: ", "Remove known irc control codes from lines before saving", Config.getOptionBool(MY_DOMAIN, "general.stripcodes"));
		preferencesPanel.addCheckboxOption("General", "general.channelmodeprefix", "Show channel mode prefix: ", "Show the @,+ etc next to nicknames", Config.getOptionBool(MY_DOMAIN, "general.channelmodeprefix"));
		
		preferencesPanel.addCheckboxOption("Back Buffer", "backbuffer.autobackbuffer", "Automatically display: ", "Automatically display the backbuffer when a channel is joined", Config.getOptionBool(MY_DOMAIN, "backbuffer.autobackbuffer"));
		preferencesPanel.addColourOption("Back Buffer", "backbuffer.colour", "Colour to use for display: ", "Colour used when displaying the backbuffer", Config.getOption(MY_DOMAIN, "backbuffer.colour"), true, true);
		preferencesPanel.addSpinnerOption("Back Buffer", "backbuffer.lines", "Number of lines to show: ", "Number of lines used when displaying backbuffer", Config.getOptionInt(MY_DOMAIN, "backbuffer.lines", 0));
		preferencesPanel.addCheckboxOption("Back Buffer", "backbuffer.timestamp", "Show Formatter-Timestamp: ", "Should the line be added to the frame with the timestamp from the formatter aswell as the file contents", Config.getOptionBool(MY_DOMAIN, "backbuffer.timestamp"));
		
		preferencesPanel.addCheckboxOption("Advanced", "advanced.filenamehash", "Add Filename hash: ", "Add the MD5 hash of the channel/client name to the filename. (This is used to allow channels with similar names (ie a _ not a  -) to be logged separately)", Config.getOptionBool(MY_DOMAIN, "advanced.filenamehash"));
		
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
	 * @param properties Source of option value
	 * @param name name of option
	 * @param defaultValue value to set if source doesn't contain a value.
	 *                     if this is null, value will not be changed.
	 */
	protected void updateOption(final Properties properties, final String name, final String defaultValue) {
		String value = null;
		
		// Get the value from the properties file if one is given
		// if one isn't given we will just use the defaultValue and set that
		if (properties != null) {
			if (properties == Config.getConfig()) {
				// If this properties file is the global config, then
				// we need to prepend our domain name to it.
				value = properties.getProperty(MY_DOMAIN + "." + name);
			} else {
				// Otherwise we do not.
				value = properties.getProperty(name);
			}
		}
		
		// Check if the Properties contains the value we want
		if (value != null) {
			// It does, so update the global config
			Config.setOption(MY_DOMAIN, name, value);
		} else {
			// It does not, so check if we have a default value
			if (defaultValue != null) {
				// We do, use that instead.
				Config.setOption(MY_DOMAIN, name, defaultValue);
			}
		}
	}
	
	/**
	 * Called when the preferences dialog is closed.
	 *
	 * @param properties user preferences
	 */
	public void configClosed(final Properties properties) {
	
		// Update Config options
		updateOption(properties, "general.networkfolders", null);
		updateOption(properties, "advanced.filenamehash", null);
		updateOption(properties, "general.addtime", null);
		updateOption(properties, "general.timestamp", null);
		updateOption(properties, "general.stripcodes", null);
		updateOption(properties, "general.channelmodeprefix", null);
		updateOption(properties, "backbuffer.autobackbuffer", null);
		updateOption(properties, "backbuffer.lines", null);
		updateOption(properties, "backbuffer.colour", null);
		updateOption(properties, "backbuffer.timestamp", null);
		
		// Check new dir exists before changing
		final File dir = new File(properties.getProperty("general.directory"));
		if (!dir.exists()) {
			try {
				dir.mkdirs();
				dir.createNewFile();
				updateOption(properties, "general.directory", null);
			} catch (IOException ex) {
				Logger.error(ErrorLevel.ERROR, "Unable to create new logging dir, not changing", ex);
			}
		} else {
			if (!dir.isDirectory()) {
				Logger.error(ErrorLevel.ERROR, "Unable to create new logging dir (file exists instead), not changing");
			} else {		
				updateOption(properties, "general.directory", null);
			}
		}
	}
	
	/** {@inheritDoc}. */
	public void configCancelled() {
	}
	
	/**
	 * Get the plugin version.
	 *
	 * @return Plugin Version
	 */
	public String getVersion() { return "0.3"; }
	
	/**
	 * Get the plugin Author.
	 *
	 * @return Author of plugin
	 */
	public String getAuthor() { return "Shane <shane@dmdirc.com>"; }
	
	/**
	 * Get the plugin Description.
	 *
	 * @return Description of plugin
	 */
	public String getDescription() { return "Allows logging of conversations"; }
	
	/**
	 * Get the name of the plugin (used in "Manage Plugins" dialog).
	 *
	 * @return Name of plugin
	 */
	public String toString() { return "Logging Plugin"; }
	
	/**
	 * Process an event of the specified type.
	 *
	 * @param type The type of the event to process
	 * @param format Format of messages that are about to be sent. (May be null)
	 * @param arguments The arguments for the event
	 */
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
							Logger.error(ErrorLevel.ERROR, "Unable to close file (Filename: "+line+")", e);
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
							Logger.error(ErrorLevel.ERROR, "Unable to close file (Filename: "+line+")", e);
						}
						openFiles.remove(line);
					}
					break;
				case QUERY_OPENED:
					// ActionManager.processEvent(CoreActionType.QUERY_OPENED, this);
					query = (Query)arguments[0];
					parser = query.getServer().getParser();
					client = parser.getClientInfo(query.getHost());
					if (client == null) {
						client = new ClientInfo(parser, query.getHost()).setFake(true);
					}
					
					// Backbuffer Display goes here!
					if (Config.getOptionBool(MY_DOMAIN, "backbuffer.autobackbuffer")) {
						showBackBuffer((Frame)query.getFrame(), getLogFile(client));
					}
					
					appendLine(getLogFile(client), "*** Query opened at: " + openedAtFormat.format(new Date()));
					appendLine(getLogFile(client), "*** Query with User: " + query.getHost());
					appendLine(getLogFile(client), "");
					break;
				case CHANNEL_OPENED:
					// ActionManager.processEvent(CoreActionType.CHANNEL_OPENED, this);
					channel = ((Channel)arguments[0]).getChannelInfo();
					
					// Backbuffer Display goes here!
					if (Config.getOptionBool(MY_DOMAIN, "backbuffer.autobackbuffer")) {
						showBackBuffer((Frame)(((Channel)arguments[0]).getFrame()), getLogFile(channel));
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
					if (!reason.equals("")) {
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
					if (!reason.equals("")) {
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
					if (!reason.equals("")) {
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
					if (channelClient.getNickname().length() == 0) {
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
	private void showBackBuffer(final Frame frame, final String filename) {
		final int numLines = Config.getOptionInt(MY_DOMAIN, "backbuffer.lines", 0);
		final String colour = Config.getOption(MY_DOMAIN, "backbuffer.colour");
		final boolean showTimestamp = Config.getOptionBool(MY_DOMAIN, "backbuffer.timestamp");
		
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
				Logger.error(ErrorLevel.ERROR, "Unable to show backbuffer (Filename: "+filename+")", e);
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
		if (Config.getOptionBool(MY_DOMAIN, "general.addtime")) {
			final DateFormat dateFormat = new SimpleDateFormat(Config.getOption(MY_DOMAIN, "general.timestamp"));
			finalLine = dateFormat.format(new Date()) + " " + finalLine;
		}
		if (Config.getOptionBool(MY_DOMAIN, "general.stripcodes")) {
			finalLine = Styliser.stipControlCodes(finalLine);
		}
//		System.out.println("[Adding] "+filename+" => "+finalLine);
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
		String result = Config.getOption(MY_DOMAIN, "general.directory");
		
		if (obj == null) {
			result = result + "null.log";
		} else if (obj instanceof ChannelInfo) {
			final ChannelInfo channel = (ChannelInfo) obj;
			result = addNetworkDir(result, channel.getParser().getNetworkName());
			result = result + sanitise(channel.getName().toLowerCase());
			if (Config.getOptionBool(MY_DOMAIN, "advanced.filenamehash")) {
				result = result + '.' + md5(channel.getName());
			}
			result = result + ".log";
		} else if (obj instanceof ClientInfo) {
			final ClientInfo client = (ClientInfo) obj;
			result = addNetworkDir(result, client.getParser().getNetworkName());
			result = result + sanitise(client.getNickname().toLowerCase());
			if (Config.getOptionBool(MY_DOMAIN, "advanced.filenamehash")) {
				result = result + '.' + md5(client.getNickname());
			}
			result = result + ".log";
		} else if (obj instanceof IRCParser) {
			final IRCParser parser = (IRCParser) obj;
			result = addNetworkDir(result, parser.getNetworkName());
			result = result + "log";
			if (Config.getOptionBool(MY_DOMAIN, "advanced.filenamehash")) {
				result = result + '.' + md5("log");
			}
			result = result + ".log";
		} else {
			result = result + sanitise(obj.toString().toLowerCase());
			if (Config.getOptionBool(MY_DOMAIN, "advanced.filenamehash")) {
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
			if (overrideNick.equals("")) {
				result = "Unknown Client";
			} else {
				result = overrideNick;
			}
		} else {
			if (overrideNick.equals("")) {
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
			if (overrideNick.equals("")) {
				result = "Unknown Client";
			} else {
				result = overrideNick;
			}
		} else {
			if (overrideNick.equals("")) {
				if (Config.getOptionBool(MY_DOMAIN, "general.channelmodeprefix")) {
					result = channelClient.toString();
				} else {
					result = channelClient.getNickname();
				}
			} else {
				result = overrideNick;
				if (Config.getOptionBool(MY_DOMAIN, "general.channelmodeprefix")) {
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
		if (Config.getOptionBool(MY_DOMAIN, "general.networkfolders")) {
			result = result + sanitise(networkName.toLowerCase()) + System.getProperty("file.separator");
			// Check dir exists
			final File dir = new File(result);
			if (!dir.exists()) {
				try {
					dir.mkdirs();
					dir.createNewFile();
				} catch (IOException ex) {
					Logger.error(ErrorLevel.ERROR, "Unable to create networkfolders dir", ex);
					// Prepend network name instead.
					result = input + sanitise(networkName.toLowerCase()) + " -- ";
				}
			} else {
				if (!dir.isDirectory()) {
					Logger.error(ErrorLevel.ERROR, "Unable to create networkfolders dir (file exists instead)");
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
	 * Get SVN Version information.
	 *
	 * @return SVN Version String
	 */
	public static String getSvnInfo() { return "$Id: IRCParser.java 969 2007-04-30 18:38:20Z ShaneMcC $"; }	

}

