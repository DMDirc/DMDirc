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

package uk.org.ownage.dmdirc.plugins.plugins.windowstatus;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.Hashtable;

import javax.swing.BorderFactory;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import uk.org.ownage.dmdirc.Channel;
import uk.org.ownage.dmdirc.FrameContainer;
import uk.org.ownage.dmdirc.Query;
import uk.org.ownage.dmdirc.Server;
import uk.org.ownage.dmdirc.commandparser.CommandWindow;
import uk.org.ownage.dmdirc.actions.ActionType;
import uk.org.ownage.dmdirc.actions.CoreActionType;
import uk.org.ownage.dmdirc.parser.ChannelClientInfo;
import uk.org.ownage.dmdirc.parser.ChannelInfo;
import uk.org.ownage.dmdirc.plugins.Plugin;
import uk.org.ownage.dmdirc.plugins.EventPlugin;
import uk.org.ownage.dmdirc.ui.ChannelFrame;
import uk.org.ownage.dmdirc.ui.MainFrame;
import uk.org.ownage.dmdirc.ui.QueryFrame;
import uk.org.ownage.dmdirc.ui.ServerFrame;
import uk.org.ownage.dmdirc.ui.components.Frame;

/**
 * Displays information related to the current window in the status bar.
 *
 * @author Shane 'Dataforce' McCormack
 * @version $Id: WindowStatusPlugin.java 969 2007-04-30 18:38:20Z ShaneMcC $
 */
public final class WindowStatusPlugin extends Plugin implements EventPlugin {
	/** The panel we use in the status bar. */
	private final JPanel panel = new JPanel();
	
	/** The label we use to show window status. */
	private final JLabel label = new JLabel(" ??? ");
	
	/** Creates a new instance of WindowStatusPlugin. */
	public WindowStatusPlugin() {
		super();
		panel.setLayout(new BorderLayout());
//		panel.setPreferredSize(new Dimension(70, 25));
		panel.setBorder(BorderFactory.createEtchedBorder());
		label.setHorizontalAlignment(SwingConstants.CENTER);
		panel.add(label);
	}
	
	/**
	 * Called when the plugin is loaded.
	 *
	 * @return false if the plugin can not be loaded
	 */
	public boolean onLoad() { return true; }
	
	/**
	 * Called when this plugin is Activated.
	 */
	public void onActivate() {
		MainFrame.getMainFrame().getStatusBar().addComponent(panel);
		JInternalFrame active = MainFrame.getMainFrame().getActiveFrame();
		if (active instanceof CommandWindow) {
			FrameContainer activeFrame = ((CommandWindow)active).getContainer();
			processEvent(CoreActionType.CLIENT_FRAME_CHANGED, new StringBuffer(), activeFrame);
		}
	}
	
	/**
	 * Called when this plugin is deactivated.
	 */
	public void onDeactivate() {
		MainFrame.getMainFrame().getStatusBar().removeComponent(panel);
	}
	
	/**
	 * Get the plugin version.
	 *
	 * @return Plugin Version
	 */
	public String getVersion() { return "0.2"; }
	
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
	public String getDescription() { return "Displays information related to the current window in the status bar."; }
	
	/**
	 * Get the name of the plugin (used in "Manage Plugins" dialog).
	 *
	 * @return Name of plugin
	 */
	public String toString() { return "WindowStatus Plugin"; }
	
	/**
	 * Process an event of the specified type.
	 *
	 * @param type The type of the event to process
	 * @param format Format of messages that are about to be sent. (May be null)
	 * @param arguments The arguments for the event
	 */
	public void processEvent(final ActionType type, final StringBuffer format, final Object... arguments) {
	
		StringBuffer textString = new StringBuffer("");
		if (type instanceof CoreActionType) {
			final CoreActionType thisType = (CoreActionType)type;
			switch (thisType) {
				case CLIENT_FRAME_CHANGED:
					FrameContainer current = (FrameContainer)arguments[0];
					if (current instanceof Server) {
						Server frame = (Server)current;
						textString.append(frame.getName());
					} else if (current instanceof Channel) {
						Channel frame = (Channel)current;
						ChannelInfo chan = frame.getChannelInfo();
						Hashtable<Integer,String> names = new Hashtable<Integer,String>();
						Hashtable<Integer,Integer> types = new Hashtable<Integer,Integer>();
						textString.append(chan.getName());
						textString.append(" - Nicks: "+chan.getUserCount()+" (");
						for (ChannelClientInfo client : chan.getChannelClients()) {
							Integer im = client.getImportantModeValue();
							if (!names.containsKey(im)) {
								String mode = client.getImportantModePrefix();
								if (mode.equals("")) { mode = "None:"; }
								names.put(im, mode);
							}
							
							Integer count = types.get(im);
							if (count == null) {
								count = new Integer(1);
							} else {
								count = count+1;
							}
							types.put(im, count);
						}
						boolean isFirst = true;
						for (Integer modeval : types.keySet()) {
							int count = types.get(modeval);
							if (isFirst) { isFirst = false; }
							else { textString.append(" "); }
							textString.append(names.get(modeval)+count);
						}
						textString.append(")");
					} else if (current instanceof Query) {
						Query frame = (Query)current;
						textString.append(frame.getHost());
					}
					label.setText(" "+textString.toString()+" ");
					break;
				default:
					break;
			}
		}
	}
}
