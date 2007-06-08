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

import com.dmdirc.Config;
import com.dmdirc.Server;
import com.dmdirc.commandparser.CommandManager;
import com.dmdirc.commandparser.CommandWindow;
import com.dmdirc.commandparser.ServerCommand;
import com.dmdirc.plugins.Plugin;
import com.dmdirc.plugins.PluginManager;

import java.util.Enumeration;
import java.util.Properties;

/**
 * The dcop command retrieves information from a dcop application.
 *
 * @author Shane "Dataforce" Mc Cormack
 * @version $Id: LoggingCommand.java 969 2007-04-30 18:38:20Z ShaneMcC $
 */
public final class LoggingCommand extends ServerCommand {
	/**
	 * Creates a new instance of DcopCommand.
	 */
	public LoggingCommand() {
		super();
		CommandManager.registerCommand(this);
	}
		
	/**
	 * Executes this command.
	 *
	 * @param origin The frame in which this command was issued
	 * @param server The server object that this command is associated with
         * @param isSilent Whether this command is silenced or not
	 * @param args The user supplied arguments
	 */
	public void execute(final CommandWindow origin, final Server server, final boolean isSilent, final String... args) {
		final Plugin gotPlugin = PluginManager.getPluginManager().getPlugin("com.dmdirc.addons.logging.LoggingPlugin");
		
		if (gotPlugin == null || !(gotPlugin instanceof LoggingPlugin)) {
			sendLine(origin, isSilent, "commandError", "Logging Plugin is not loaded.");
			return;
		}
		
		final LoggingPlugin plugin = (LoggingPlugin) gotPlugin;
		
		if (args.length > 0) {
			if (args[0].equalsIgnoreCase("config")) {
				plugin.showConfig();
			} else if (args[0].equalsIgnoreCase("reload")) {
				boolean wasActive = plugin.isActive();
				if (PluginManager.getPluginManager().reloadPlugin("com.dmdirc.addons.logging.LoggingPlugin")) {
					sendLine(origin, isSilent, "commandOutput", "Plugin reloaded.");
					PluginManager.getPluginManager().getPlugin("com.dmdirc.addons.logging.LoggingPlugin").setActive(wasActive);
				} else {
					sendLine(origin, isSilent, "commandOutput", "Plugin failed to reload.");
				}
			} else if (args[0].equalsIgnoreCase("help")) {
				sendLine(origin, isSilent, "commandOutput", getName() + " reload                     - Reload the logging plugin.");
				sendLine(origin, isSilent, "commandOutput", getName() + " help                       - Show this help.");
				sendLine(origin, isSilent, "commandOutput", getName() + " config                     - Show the logging plugin configuration.");
				sendLine(origin, isSilent, "commandOutput", getName() + " set <help|option> [value]  - Set a configuration option.");
			} else if (args[0].equalsIgnoreCase("set")) {
				if (args.length < 2 || args[1].equalsIgnoreCase("help")) {
					final Properties config = Config.getConfig();
					sendLine(origin, isSilent, "commandOutput", "Current Values:");
					final Enumeration values = config.propertyNames();
					while (values.hasMoreElements()) {
						final String property = (String) values.nextElement();
						
						if (property.toLowerCase().startsWith(plugin.getDomain().toLowerCase() + ".")) {
							sendLine(origin, isSilent, "commandOutput", "[" + property.substring(property.indexOf(".") + 1) + "] => " + config.getProperty(property));
						}
					}
					sendLine(origin, isSilent, "commandOutput", "");
					sendLine(origin, isSilent, "commandOutput", "Use " + getName() + " set <option> [value] to change the value. (if [value] is not given, the current value will be displayed)");
				} else if (args.length > 1) {
					if (Config.hasOption(plugin.getDomain(), args[1].toLowerCase())) {
						String newValue = "";
						if (args.length > 2) { newValue = implodeArgs(2, args); }
						if (newValue.equals("")) {
							sendLine(origin, isSilent, "commandOutput", "Current value of '" + args[1] + "' is '" + Config.getOption(plugin.getDomain(), args[1].toLowerCase()) + "'");
						} else {
							plugin.updateOption(null, args[1], newValue);
							sendLine(origin, isSilent, "commandOutput", "Setting '" + args[1] + "' to '" + newValue + "'");
						}
					} else {
						sendLine(origin, isSilent, "commandOutput", "'" + args[1] + "' is not a valid option");
					}
				}
			} else {
				sendLine(origin, isSilent, "commandOutput", "Unknown command '" + args[0] + "'. Use " + getName() + " help for a list of commands.");
			}
		} else {
			sendLine(origin, isSilent, "commandOutput", "Use " + getName() + " help for a list of commands.");
		}
	}

	/**
	 * Returns this command's name.
	 *
	 * @return The name of this command
	 */
	public String getName() { return "logging"; }
	
	/**
	 * Returns whether or not this command should be shown in help messages.
	 *
	 * @return True iff the command should be shown, false otherwise
	 */
	public boolean showInHelp() { return true; }
	
	/**
	 * Indicates whether this command is polyadic or not.
	 *
	 * @return True iff this command is polyadic, false otherwise
	 */
	public boolean isPolyadic() { return true; }
	
	/**
	 * Returns the arity of this command.
	 *
	 * @return This command's arity
	 */
	public int getArity() { return 0; }
	
	/**
	 * Returns a string representing the help message for this command.
	 *
	 * @return the help message for this command
	 */
	public String getHelp() { return this.getName() + " <config|set|help> [parameters]"; }
	
	/**
	 * Get SVN Version information.
	 *
	 * @return SVN Version String
	 */
	public static String getSvnInfo() { return "$Id: IRCParser.java 969 2007-04-30 18:38:20Z ShaneMcC $"; }	
}

