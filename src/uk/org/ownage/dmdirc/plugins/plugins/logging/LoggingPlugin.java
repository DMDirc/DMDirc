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

package uk.org.ownage.dmdirc.plugins.plugins.logging;

import java.io.File;
import java.io.FileWriter;
import java.io.BufferedWriter;
import java.io.IOException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Properties;
import java.util.Date;

import uk.org.ownage.dmdirc.Channel;
import uk.org.ownage.dmdirc.Config;
import uk.org.ownage.dmdirc.actions.ActionType;
import uk.org.ownage.dmdirc.actions.CoreActionType;
import uk.org.ownage.dmdirc.logger.Logger;
import uk.org.ownage.dmdirc.logger.ErrorLevel;
import uk.org.ownage.dmdirc.parser.ChannelInfo;
import uk.org.ownage.dmdirc.parser.ChannelClientInfo;
import uk.org.ownage.dmdirc.parser.ClientInfo;
import uk.org.ownage.dmdirc.plugins.EventPlugin;
import uk.org.ownage.dmdirc.ui.components.PreferencesInterface;
import uk.org.ownage.dmdirc.ui.components.PreferencesPanel;
import uk.org.ownage.dmdirc.ui.messages.Styliser;
/**
 * Adds logging facility to client. (Currently only logs channels)
 *
 * @author Shane 'Dataforce' McCormack
 * @version $Id: LoggingPlugin.java 969 2007-04-30 18:38:20Z ShaneMcC $
 */
public class LoggingPlugin implements EventPlugin, PreferencesInterface {
	/** What domain do we store all settings in the global config under. */
	private static final String myDomain = "plugin-Logging";

	/**
	 * Creates a new instance of the Logging Plugin
	 */
	public LoggingPlugin() { }

	/**
	 * Called when the plugin is loaded.
	 *
	 * @return false if the plugin can not be loaded
	 */
	public boolean onLoad() {
		Properties config = Config.getConfig();
		
		// Set default options if they don't exist
		updateOption(config, "general.directory", Config.getConfigDir()+"logs"+System.getProperty("file.separator"));
		updateOption(config, "general.networkfolders", "true");
		updateOption(config, "advanced.filenamehash", "false");
		updateOption(config, "general.addtime", "true");
		updateOption(config, "general.timestamp", "[dd/MM/yyyy HH:mm:ss]");
		updateOption(config, "general.stripcodes", "true");
		updateOption(config, "general.channelmodeprefix", "true");
		
		final File dir = new File(Config.getOption(myDomain, "general.directory"));
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
	 * Called when the plugin is about to be unloaded.
	 */
	public void onUnload() { }

	/**
	 * Called when this plugin becomes active.
	 */
	public void onActivate() { }
	
	/**
	 * Called when this plugin is deactivated.
	 */
	public void onDeactivate() { }
	
	/**
	 * Called to see if the plugin has configuration options (via dialog).
	 *
	 * @return true if the plugin has configuration options via a dialog.
	 */
	public boolean isConfigurable() { return false; }
	
	/**
	 * Called to show the Configuration dialog of the plugin if appropriate.
	 */
	public void showConfig() {
		PreferencesPanel preferencesPanel = new PreferencesPanel(this);
		preferencesPanel.addCategory("General", "General configuration for Logging plugin.");
		preferencesPanel.addCategory("Advanced", "Advanced configuration for Logging plugin. You shouldn't need to edit this unless you know what you are doing.");
		preferencesPanel.addOption("General", "general.directory", "Directory: ", PreferencesPanel.OptionType.TEXTFIELD, Config.getOption(myDomain, "general.directory"));
		preferencesPanel.addOption("General", "general.networkfolders", "Separate logs by network: ", PreferencesPanel.OptionType.CHECKBOX, Boolean.parseBoolean(Config.getOption(myDomain, "general.networkfolders")));
		preferencesPanel.addOption("General", "general.addtime", "Timestamp logs: ", PreferencesPanel.OptionType.CHECKBOX, Boolean.parseBoolean(Config.getOption(myDomain, "general.addtime")));
		preferencesPanel.addOption("General", "general.timestamp", "Timestamp format: ", PreferencesPanel.OptionType.TEXTFIELD, Config.getOption(myDomain, "general.timestamp"));
		preferencesPanel.addOption("General", "general.stripcodes", "Strip Control Codes: ", PreferencesPanel.OptionType.CHECKBOX, Boolean.parseBoolean(Config.getOption(myDomain, "general.stripcodes")));
		preferencesPanel.addOption("General", "general.channelmodeprefix", "Show channel mode prefix: ", PreferencesPanel.OptionType.CHECKBOX, Boolean.parseBoolean(Config.getOption(myDomain, "general.channelmodeprefix")));
		preferencesPanel.addOption("Advanced", "advanced.filenamehash", "Add Filename hash: ", PreferencesPanel.OptionType.CHECKBOX, Boolean.parseBoolean(Config.getOption(myDomain, "advanced.filenamehash")));
		
		preferencesPanel.display();
	}
	
	/**
	 * Get the name of the domain we store all settings in the global config under.
	 */
	 protected static String getDomain() { return myDomain; }
	
	/**
	 * Copy the new vaule of an option to the global config
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
				value = properties.getProperty(myDomain+"."+name);
			} else {
				// Otherwise we do not.
				value = properties.getProperty(name);
			}
		}
		
		// Check if the Properties contains the value we want
		if (value != null) {
			// It does, so update the global config
			Config.setOption(myDomain, name, value);
		} else {
			// It does not, so check if we have a default value
			if (defaultValue != null) {
				// We do, use that instead.
				Config.setOption(myDomain, name, defaultValue);
			}
		}
	}
	
	/**
	 * Called when the preferences dialog is closed.
	 *
	 * @param properties user preferences
	 */
	public void configClosed(Properties properties) {
	
		// Update Config options
		updateOption(properties, "general.networkfolders", null);
		updateOption(properties, "advanced.filenamehash", null);
		updateOption(properties, "general.addtime", null);
		updateOption(properties, "general.timestamp", null);
		updateOption(properties, "general.stripcodes", null);
		updateOption(properties, "general.channelmodeprefix", null);
		
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
	
	/**
	 * Get the plugin version
	 *
	 * @return Plugin Version
	 */
	public int getVersion() { return 1; }
	
	/**
	 * Get the plugin Author.
	 *
	 * @return Author of plugin
	 */
	public String getAuthor() { return "Shane 'Dataforce' McCormack - shane@dmdirc.com"; }
	
	/**
	 * Get the plugin Description.
	 *
	 * @return Description of plugin
	 */
	public String getDescription() { return "Allows logging of conversations"; }
	
	/**
	 * Process an event of the specified type.
	 *
	 * @param type The type of the event to process
	 * @param arguments The arguments for the event
	 */
	public void processEvent(final ActionType type, final Object ... arguments) {
		if (type instanceof CoreActionType) {
			CoreActionType thisType = (CoreActionType)type;
			ChannelInfo channel;
			ChannelClientInfo channelClient;
			ClientInfo client;
			String line = "";
			String reason = "";

			switch (thisType) {
				case CHANNEL_MESSAGE:
				case CHANNEL_SELF_MESSAGE:
					// ActionManager.processEvent(CoreActionType.CHANNEL_MESSAGE, this, cChannelClient, sMessage);
					channel = ((Channel)arguments[0]).getChannelInfo();
					channelClient = (ChannelClientInfo)arguments[1];
					appendLine(getLogFile(channel), "<"+getDisplayName(channelClient)+"> "+arguments[2]);
					break;
				case CHANNEL_ACTION:
				case CHANNEL_SELF_ACTION:
					// ActionManager.processEvent(CoreActionType.CHANNEL_ACTION, this, cChannelClient, sMessage);
					channel = ((Channel)arguments[0]).getChannelInfo();
					channelClient = (ChannelClientInfo)arguments[1];
					appendLine(getLogFile(channel), "* "+getDisplayName(channelClient)+" "+(String)arguments[2]);
					break;
				case CHANNEL_GOTTOPIC:
					// ActionManager.processEvent(CoreActionType.CHANNEL_GOTTOPIC, this);
					channel = ((Channel)arguments[0]).getChannelInfo();
					final DateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
					final DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
					final String topicTime = timeFormat.format(1000 * channel.getTopicTime())+" on "+dateFormat.format(1000 * channel.getTopicTime());
					
					appendLine(getLogFile(channel), "*** Topic is: "+channel.getTopic());
					appendLine(getLogFile(channel), "*** Set at: "+topicTime+" by "+channel.getTopicUser());
					break;
				case CHANNEL_TOPICCHANGE:
					// ActionManager.processEvent(CoreActionType.CHANNEL_TOPICCHANGE, this, user, topic);
					channel = ((Channel)arguments[0]).getChannelInfo();
					channelClient = (ChannelClientInfo)arguments[1];
					appendLine(getLogFile(channel), "*** "+getDisplayName(channelClient)+" Changed the topic to: "+(String)arguments[2]);
					break;
				case CHANNEL_JOIN:
					// ActionManager.processEvent(CoreActionType.CHANNEL_JOIN, this, cChannelClient);
					channel = ((Channel)arguments[0]).getChannelInfo();
					channelClient = (ChannelClientInfo)arguments[1];
					client = channelClient.getClient();
					appendLine(getLogFile(channel), "*** "+getDisplayName(channelClient)+" ("+client.toString()+") joined the channel");
					break;
				case CHANNEL_PART:
					// ActionManager.processEvent(CoreActionType.CHANNEL_PART, this, cChannelClient, sReason);
					channel = ((Channel)arguments[0]).getChannelInfo();
					channelClient = (ChannelClientInfo)arguments[1];
					client = channelClient.getClient();
					line = "*** "+getDisplayName(channelClient)+" ("+client.toString()+") left the channel";
					
					reason = (String)arguments[2];
					if (!reason.equals("")) {
						line = line+" ("+reason+")";
					}
					appendLine(getLogFile(channel), line);
					break;
				case CHANNEL_QUIT:
					// ActionManager.processEvent(CoreActionType.CHANNEL_QUIT, this, cChannelClient, sReason);
					channel = ((Channel)arguments[0]).getChannelInfo();
					channelClient = (ChannelClientInfo)arguments[1];
					client = channelClient.getClient();
					line = "*** "+getDisplayName(channelClient)+" ("+client.toString()+") Quit IRC";
					
					reason = (String)arguments[2];
					if (!reason.equals("")) {
						line = line+" ("+reason+")";
					}
					appendLine(getLogFile(channel), line);
					break;
				case CHANNEL_KICK:
					// ActionManager.processEvent(CoreActionType.CHANNEL_KICK, this, cKickedByClient, cKickedClient, sReason);
					channel = ((Channel)arguments[0]).getChannelInfo();
					channelClient = (ChannelClientInfo)arguments[2];
					ChannelClientInfo kickerClient = (ChannelClientInfo)arguments[1];
					line = "*** "+getDisplayName(channelClient)+" was kicked by "+getDisplayName(kickerClient);
					
					reason = (String)arguments[3];
					if (!reason.equals("")) {
						line = line+" ("+reason+")";
					}
					appendLine(getLogFile(channel), line);
					break;
				case CHANNEL_NICKCHANGE:
					// ActionManager.processEvent(CoreActionType.CHANNEL_NICKCHANGE, this, cChannelClient, sOldNick);
					channel = ((Channel)arguments[0]).getChannelInfo();
					channelClient = (ChannelClientInfo)arguments[1];
					String oldnick = (String)arguments[2];
					appendLine(getLogFile(channel), "*** "+getDisplayName(channelClient, oldnick)+" is now "+getDisplayName(channelClient));
					break;
				case CHANNEL_MODECHANGE:
					// ActionManager.processEvent(CoreActionType.CHANNEL_MODECHANGE, this, cChannelClient, sModes);
					channel = ((Channel)arguments[0]).getChannelInfo();
					channelClient = (ChannelClientInfo)arguments[1];
					if (channelClient.getNickname().length() == 0) {
						appendLine(getLogFile(channel), "*** Channel modes are: "+(String)arguments[2]);
					} else {
						appendLine(getLogFile(channel), "*** "+getDisplayName(channelClient)+" set modes: "+(String)arguments[2]);
					}
					break;
			}
		}
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
		if (Boolean.parseBoolean(Config.getOption(myDomain, "general.addtime"))) {
			final DateFormat dateFormat = new SimpleDateFormat(Config.getOption(myDomain, "general.timestamp"));
			finalLine = dateFormat.format(new Date())+" "+finalLine;
		}
		if (Boolean.parseBoolean(Config.getOption(myDomain, "general.stripcodes"))) {
			finalLine = Styliser.stipControlCodes(finalLine);
		}

		BufferedWriter out = null;
		try {
			out = new BufferedWriter(new FileWriter(filename, true));
			out.write(finalLine);
			out.newLine();
			out.close();
			return true;
		} catch (IOException e) {
			/* Do Nothing */
		} finally {
			// Try to close the file.
			try {
				if (out != null) { out.close(); }
			} catch (IOException e) { /* Do Nothing */ }
		}
		return false;
	}
	
	/**
	 * Get the name of the log file for a specific object.
	 *
	 * @param obj Object to get name for
	 * @return the name of the log file to use for this object.
	 */
	private String getLogFile(Object obj) {
		String result = Config.getOption(myDomain, "general.directory");
		
		if (obj == null) {
			result = result+"null.log";
		} else if (obj instanceof ChannelInfo) {
			ChannelInfo channel = (ChannelInfo)obj;
			result = addNetworkDir(result, channel.getParser().getNetworkName());
			result = result+sanitise(channel.getName().toLowerCase());
			if (Boolean.parseBoolean(Config.getOption(myDomain, "advanced.filenamehash"))) {
				result = result+'.'+md5(channel.getName());
			}
			result = result+".log";
		} else if (obj instanceof ClientInfo) {
			ClientInfo client = (ClientInfo)obj;
			result = addNetworkDir(result, client.getParser().getNetworkName());
			result = result+sanitise(client.getNickname().toLowerCase());
			if (Boolean.parseBoolean(Config.getOption(myDomain, "advanced.filenamehash"))) {
				result = result+'.'+md5(client.getNickname());
			}
			result = result+".log";
		} else {
			result = result+sanitise(obj.toString().toLowerCase());
			if (Boolean.parseBoolean(Config.getOption(myDomain, "advanced.filenamehash"))) {
				result = result+'.'+md5(obj.toString());
			}
			result = result+".log";
		}
		return result;
	}
	
	/**
	 * Get name to display for channelClient (Taking into account the channelmodeprefix setting)
	 *
	 * @param channelClient The client to get the display name for
	 * @return name to display
	 */
	private String getDisplayName(final ChannelClientInfo channelClient) {
		return getDisplayName(channelClient, "");
	}
	
	/**
	 * Get name to display for channelClient (Taking into account the channelmodeprefix setting)
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
				if (Boolean.parseBoolean(Config.getOption(myDomain, "general.channelmodeprefix"))) {
					result = channelClient.toString();
				} else {
					result = channelClient.getNickname();
				}
			} else {
				result = overrideNick;
				if (Boolean.parseBoolean(Config.getOption(myDomain, "general.channelmodeprefix"))) {
					result = channelClient.getImportantModePrefix()+overrideNick;
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
		if (Boolean.parseBoolean(Config.getOption(myDomain, "general.networkfolders"))) {
			result = result+sanitise(networkName.toLowerCase())+System.getProperty("file.separator");
			// Check dir exists
			final File dir = new File(result);
			if (!dir.exists()) {
				try {
					dir.mkdirs();
					dir.createNewFile();
				} catch (IOException ex) {
					Logger.error(ErrorLevel.ERROR, "Unable to create networkfolders dir", ex);
					// Prepend network name instead.
					result = input+sanitise(networkName.toLowerCase())+" -- ";
				}
			} else {
				if (!dir.isDirectory()) {
					Logger.error(ErrorLevel.ERROR, "Unable to create networkfolders dir (file exists instead)");
					// Prepend network name instead.
					result = input+sanitise(networkName.toLowerCase())+" -- ";
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
	private String sanitise(String name) {
		// Replace illegal chars with
		return name.replaceAll("[^\\w\\.\\s\\-\\#\\&\\_]", "_");
	}
	
	/**
	 * Get the md5 hash of a string
	 *
	 * @param string String to hash
	 * @return md5 hash of given string
	 */
	private String md5(String string) {
		try {
			MessageDigest m = MessageDigest.getInstance("MD5");
			m.update(string.getBytes(),0,string.length());
			return new BigInteger(1,m.digest()).toString(16);
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

