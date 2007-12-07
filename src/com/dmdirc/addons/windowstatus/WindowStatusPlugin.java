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

package com.dmdirc.addons.windowstatus;

import com.dmdirc.Channel;
import com.dmdirc.FrameContainer;
import com.dmdirc.Main;
import com.dmdirc.Query;
import com.dmdirc.Server;
import com.dmdirc.actions.ActionManager;
import com.dmdirc.actions.interfaces.ActionType;
import com.dmdirc.actions.CoreActionType;
import com.dmdirc.config.IdentityManager;
import com.dmdirc.config.Identity;
import com.dmdirc.interfaces.ActionListener;
import com.dmdirc.parser.ChannelClientInfo;
import com.dmdirc.parser.ChannelInfo;
import com.dmdirc.parser.ClientInfo;
import com.dmdirc.plugins.Plugin;
import com.dmdirc.ui.interfaces.InputWindow;
import com.dmdirc.ui.interfaces.PreferencesInterface;
import com.dmdirc.ui.interfaces.PreferencesPanel;
import com.dmdirc.ui.interfaces.Window;

import net.miginfocom.swing.MigLayout;

import java.util.Hashtable;
import java.util.Map.Entry;
import java.util.Properties;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;


/**
 * Displays information related to the current window in the status bar.
 *
 * @author Shane 'Dataforce' McCormack
 * @version $Id: WindowStatusPlugin.java 969 2007-04-30 18:38:20Z ShaneMcC $
 */
public final class WindowStatusPlugin extends Plugin implements ActionListener, PreferencesInterface {
	/** What domain do we store all settings in the global config under. */
	private static final String MY_DOMAIN = "plugin-Logging";
	
	/** The panel we use in the status bar. */
	private final JPanel panel = new JPanel();
	
	/** The label we use to show window status. */
	private final JLabel label = new JLabel("???");
	
	/** Creates a new instance of WindowStatusPlugin. */
	public WindowStatusPlugin() {
		super();
		panel.setBorder(BorderFactory.createEtchedBorder());
		panel.setLayout(new MigLayout("ins 0 rel 0 rel, aligny center"));
		panel.add(label);
	}
	
	/**
	 * Called when the plugin is loaded.
	 */
	public void onLoad() {
		// Set defaults
		Properties defaults = new Properties();
		defaults.setProperty(MY_DOMAIN + ".channel.shownone", "true");
		defaults.setProperty(MY_DOMAIN + ".channel.noneprefix", "None:");
		defaults.setProperty(MY_DOMAIN + ".client.showname", "false");
		defaults.setProperty("identity.name", "WindowStatus Plugin Defaults");
		IdentityManager.addIdentity(new Identity(defaults));
		
		Main.getUI().getStatusBar().addComponent(panel);
		updateStatus();
		
		ActionManager.addListener(this, CoreActionType.CLIENT_FRAME_CHANGED);
	}
	
	/**
	 * Called when this plugin is unloaded.
	 */
	public void onUnload() {
		Main.getUI().getStatusBar().removeComponent(panel);
		ActionManager.removeListener(this);
	}
	
	/**
	 * Process an event of the specified type.
	 *
	 * @param type The type of the event to process
	 * @param format Format of messages that are about to be sent. (May be null)
	 * @param arguments The arguments for the event
	 */
	public void processEvent(final ActionType type, final StringBuffer format, final Object... arguments) {
		if (type.equals(CoreActionType.CLIENT_FRAME_CHANGED)) {
			updateStatus((FrameContainer)arguments[0]);
		}
	}
	
	/**
	 * Update the window status using the current active window.
	 */
	public void updateStatus() {
		Window active = Main.getUI().getMainWindow().getActiveFrame();
		
		if (active != null) {
			FrameContainer activeFrame = ((InputWindow) active).getContainer();
			updateStatus(activeFrame);
		}
	}
	
	/**
	 * Update the window status using a given FrameContainer as the active frame.
	 *
	 * @param current Window to use when adding status.
	 */
	public void updateStatus(final FrameContainer current) {
		if (current == null) { return; }
		StringBuffer textString = new StringBuffer("");
		
		if (current instanceof Server) {
			Server frame = (Server)current;
			
			textString.append(frame.getName());
		} else if (current instanceof Channel) {
			final Channel frame = (Channel)current;
			final ChannelInfo chan = frame.getChannelInfo();
			final Hashtable<Long,String> names = new Hashtable<Long,String>();
			final Hashtable<Long,Integer> types = new Hashtable<Long,Integer>();
			
			textString.append(chan.getName());
			textString.append(" - Nicks: "+chan.getUserCount()+" (");
			
			for (ChannelClientInfo client : chan.getChannelClients()) {
				Long im = client.getImportantModeValue();
				
				if (!names.containsKey(im)) {
					String mode = client.getImportantModePrefix();
					
					if (mode.isEmpty()) {
						if (IdentityManager.getGlobalConfig().getOptionBool(MY_DOMAIN, "channel.shownone")) {
							if (IdentityManager.getGlobalConfig().hasOption(MY_DOMAIN, "channel.noneprefix")) {
								mode = IdentityManager.getGlobalConfig().getOption(MY_DOMAIN, "channel.noneprefix");
							} else {
								mode = "None:";
							}
						} else {
							continue;
						}
					}
					names.put(im, mode);
				}
				
				Integer count = types.get(im);
				
				if (count == null) {
					count = Integer.valueOf(1);
				} else {
					count = count+1;
				}
				types.put(im, count);
			}
			
			boolean isFirst = true;
			
			for (Entry<Long, Integer> entry : types.entrySet()) {
				if (isFirst) { isFirst = false; } else { textString.append(" "); }
				textString.append(names.get(entry.getKey())+entry.getValue());
			}
			
			textString.append(")");
		} else if (current instanceof Query) {
			final Query frame = (Query)current;
			
			textString.append(frame.getHost());
			if (IdentityManager.getGlobalConfig().getOptionBool(MY_DOMAIN, "client.showname")) {
				final ClientInfo client = frame.getServer().getParser().getClientInfo(frame.getHost());
				if (client != null) {
					final String realname = client.getRealName();
					if (!realname.isEmpty()) {
						textString.append(" - "+client.getRealName());
					}
				}
			}
		} else {
			textString.append("???");
		}
		label.setText(textString.toString());
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
		final PreferencesPanel preferencesPanel = Main.getUI().getPreferencesPanel(this, "Window Status Plugin - Config");
		preferencesPanel.addCategory("Channel", "Configuration for Window Status plugin when showing a channel window.");
		preferencesPanel.addCategory("Client", "Configuration for Window Status plugin when showing a client window.");
		
		preferencesPanel.addCheckboxOption("Channel", "channel.shownone", "Show 'none' count: ", "Should the count for uses with no state be shown?", IdentityManager.getGlobalConfig().getOptionBool(MY_DOMAIN, "channel.shownone"));
		preferencesPanel.addTextfieldOption("Channel", "channel.noneprefix", "Prefix used before 'none' count: ", "The Prefix to use when showing the 'none' count", IdentityManager.getGlobalConfig().getOption(MY_DOMAIN, "channel.noneprefix"));
		
		preferencesPanel.addCheckboxOption("Client", "client.showname", "Show Client realname if known: ", "Should the realname for clients be shown if known?", IdentityManager.getGlobalConfig().getOptionBool(MY_DOMAIN, "client.showname"));
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
	public void configClosed(final Properties properties) {
		// Update Config options
		updateOption(properties, "channel.shownone");
		updateOption(properties, "channel.noneprefix");
		updateOption(properties, "client.showname");
		// Update Status bar
		updateStatus();
	}
	
	/**
	 * Called when the preferences dialog is cancelled.
	 */
	public void configCancelled() { }
}
