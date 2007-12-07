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

package com.dmdirc.addons.identd;

import com.dmdirc.Main;
import com.dmdirc.Server;
import com.dmdirc.actions.ActionManager;
import com.dmdirc.actions.interfaces.ActionType;
import com.dmdirc.actions.CoreActionType;
import com.dmdirc.config.IdentityManager;
import com.dmdirc.config.Identity;
import com.dmdirc.interfaces.ActionListener;
import com.dmdirc.plugins.Plugin;
import com.dmdirc.ui.interfaces.PreferencesInterface;
import com.dmdirc.ui.interfaces.PreferencesPanel;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * The Identd plugin answers ident requests from IRC servers.
 *
 * @author Shane
 */
public class IdentdPlugin extends Plugin implements ActionListener, PreferencesInterface {
	/** What domain do we store all settings in the global config under. */
	private static final String MY_DOMAIN = "plugin-Identd";
	/** Array list to store all the servers in that need ident replies */
	private final List<Server> servers = new ArrayList<Server>();
	/** The IdentdServer that we use*/
	private IdentdServer myServer;

	/**
	 * Creates a new instance of IdentdPlugin.
	 */
	public IdentdPlugin() { }
	
	/**
	 * Called when the plugin is loaded.
	 */
	@Override
	public void onLoad() {
		// Add action hooks
		ActionManager.addListener(this, CoreActionType.SERVER_CONNECTED, CoreActionType.SERVER_CONNECTING, CoreActionType.SERVER_CONNECTERROR);

		// Set defaults
		Properties defaults = new Properties();
		defaults.setProperty(getDomain() + ".general.useUsername", "false");
		defaults.setProperty(getDomain() + ".general.useNickname", "false");
		defaults.setProperty(getDomain() + ".general.useCustomName", "false");
		defaults.setProperty(getDomain() + ".general.customName", "DMDirc-user");
		
		defaults.setProperty(getDomain() + ".advanced.alwaysOn", "false");
		defaults.setProperty(getDomain() + ".advanced.port", "113");
		defaults.setProperty(getDomain() + ".advanced.useCustomSystem", "false");
		defaults.setProperty(getDomain() + ".advanced.customSystem", "OTHER");
		defaults.setProperty(getDomain() + ".advanced.isHiddenUser", "false");
		defaults.setProperty(getDomain() + ".advanced.isNoUser", "false");

		defaults.setProperty("identity.name", "Identd Plugin Defaults");
		IdentityManager.addIdentity(new Identity(defaults));
		
		myServer = new IdentdServer(this);
		if (IdentityManager.getGlobalConfig().getOptionBool(getDomain(), "advanced.alwaysOn")) {
			myServer.startServer();
		}
	}
	
	/**
	 * Called when this plugin is unloaded.
	 */
	@Override
	public void onUnload() {
		myServer.stopServer();
		servers.clear();
		ActionManager.removeListener(this);
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
		if (type == CoreActionType.SERVER_CONNECTING) {
			synchronized (servers) {
				if (servers.size() == 0) {
					myServer.startServer();
				}
				servers.add((Server) arguments[0]);
			}
		} else if (type == CoreActionType.SERVER_CONNECTED || type == CoreActionType.SERVER_CONNECTERROR) {
			synchronized (servers) {
				servers.remove(arguments[0]);
			
				if (servers.size() == 0) {
					if (!IdentityManager.getGlobalConfig().getOptionBool(getDomain(), "advanced.alwaysOn")) {
						myServer.stopServer();
					}
				}
			}
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
		final PreferencesPanel preferencesPanel = Main.getUI().getPreferencesPanel(this, "Identd Plugin - Config");
		preferencesPanel.addCategory("General", "General Identd Plugin config ('Lower' options take priority over those above them)");
		preferencesPanel.addCategory("Advanced", "Advanced Identd Plugin config - Only edit these if you need to/know what you are doing. Editing these could prevent access to some servers. ('Lower' options take priority over those above them)");
		
		preferencesPanel.addCheckboxOption("General", "general.useUsername", "Use connection username rather than system username: ", "If this is enabled, the username for the connection will be used rather than '"+System.getProperty("user.name")+"'", IdentityManager.getGlobalConfig().getOptionBool(getDomain(), "general.useUsername"));
		preferencesPanel.addCheckboxOption("General", "general.useNickname", "Use connection nickname rather than system username: ", "If this is enabled, the nickname for the connection will be used rather than '"+System.getProperty("user.name")+"'", IdentityManager.getGlobalConfig().getOptionBool(getDomain(), "general.useNickname"));
		preferencesPanel.addCheckboxOption("General", "general.useCustomName", "Use custom name all the time: ", "If this is enabled, the name specified below will be used all the time", IdentityManager.getGlobalConfig().getOptionBool(getDomain(), "general.useCustomName"));
		preferencesPanel.addTextfieldOption("General", "general.customName", "Custom Name to use: ", "The custom name to use when 'Use Custom Name' is enabled", IdentityManager.getGlobalConfig().getOption(getDomain(), "general.customName"));
		
		preferencesPanel.addCheckboxOption("Advanced", "advanced.alwaysOn", "Always have ident port open: ", "By default the identd only runs when there is active connection attempts. This overrides that.", IdentityManager.getGlobalConfig().getOptionBool(getDomain(), "advanced.alwaysOn"));
		preferencesPanel.addSpinnerOption("Advanced", "advanced.port", "What port should the identd listen on: ", "Default port is 113, this is probably useless if changed unless you port forward ident to a different port", IdentityManager.getGlobalConfig().getOptionInt(getDomain(), "advanced.port", 113));
		preferencesPanel.addCheckboxOption("Advanced", "advanced.useCustomSystem", "Use custom OS: ", "By default the plugin uses 'UNIX' or 'WIN32' as the system type, this can be overriden by enabling this.", IdentityManager.getGlobalConfig().getOptionBool(getDomain(), "advanced.useCustomSystem"));
		preferencesPanel.addTextfieldOption("Advanced", "advanced.customSystem", "Custom OS to use: ", "The custom system to use when 'Use Custom System' is enabled", IdentityManager.getGlobalConfig().getOption(getDomain(), "advanced.customSystem"));
		preferencesPanel.addCheckboxOption("Advanced", "advanced.isHiddenUser", "Respond to ident requests with HIDDEN-USER error: ", "By default the plugin will give a USERID response, this can force an 'ERROR : HIDDEN-USER' response instead.", IdentityManager.getGlobalConfig().getOptionBool(getDomain(), "advanced.isHiddenUser"));
		preferencesPanel.addCheckboxOption("Advanced", "advanced.isNoUser", "Respond to ident requests with NO-USER error: ", "By default the plugin will give a USERID response, this can force an 'ERROR : NO-USER' response instead. (Overrides HIDDEN-USER)", IdentityManager.getGlobalConfig().getOptionBool(getDomain(), "advanced.isNoUser"));
		
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
			value = IdentityManager.getGlobalConfig().getOption(getDomain(), name);
		}
		
		// Check if the Value exists
		if (value != null) {
			// It does, so update the global config with the new value
			IdentityManager.getConfigIdentity().setOption(getDomain(), name, value);
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
		final int oldPort = IdentityManager.getGlobalConfig().getOptionInt(getDomain(), "advanced.port", 113);
		updateOption(properties, "general.useUsername");
		updateOption(properties, "general.useNickname");
		updateOption(properties, "general.useCustomName");
		updateOption(properties, "general.customName");
		
		updateOption(properties, "advanced.alwaysOn");
		updateOption(properties, "advanced.port");
		updateOption(properties, "advanced.useCustomSystem");
		updateOption(properties, "advanced.customSystem");
		updateOption(properties, "advanced.isHiddenUser");
		updateOption(properties, "advanced.isNoUser");
		final int newPort = IdentityManager.getGlobalConfig().getOptionInt(getDomain(), "advanced.port", 113);
		if (myServer.isRunning() && oldPort != newPort) {
			myServer.stopServer();
			myServer.startServer();
		}
	}
	
	/**
	 * Called when the preferences dialog is cancelled.
	 */
	@Override
	public void configCancelled() { }
	
}
