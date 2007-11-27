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

package com.dmdirc.addons.dcc;

import com.dmdirc.commandparser.CommandManager;
import com.dmdirc.plugins.Plugin;
import com.dmdirc.ui.swing.JWrappingLabel;
import com.dmdirc.ui.WindowManager;
import com.dmdirc.ui.swing.components.TextFrame;

import java.util.List;
import java.util.ArrayList;

import javax.swing.SwingConstants;

/**
 * This plugin adds DCC to dmdirc
 *
 * @author Shane 'Dataforce' McCormack
 * @version $Id: DCCPlugin.java 969 2007-04-30 18:38:20Z ShaneMcC $
 */
public final class DCCPlugin extends Plugin {
	/** The DCCCommand we created */
	private DCCCommand command = null;
	
	/** Our DCC Container window. */
	private DCCFrame container;
	
	/** Child Frames */
	private List<DCCFrame> childFrames = new ArrayList<DCCFrame>();
	
	/**
	 * Creates a new instance of the DCC Plugin.
	 */
	public DCCPlugin() {
		super();
	}
	
	/**
	 * Create the container window
	 */
	protected void createContainer() {
		container = new DCCFrame(this, "DCCs"){};
		JWrappingLabel label = new JWrappingLabel("This is a placeholder window to group DCCs together", SwingConstants.CENTER);
		((TextFrame)container.getFrame()).getContentPane().add(label);
		WindowManager.addWindow(container.getFrame());
	}
	
	/**
	 * Add a window to the container window
	 *
	 * @param window Window to remove
	 */
	protected void addWindow(final DCCFrame window) {
		if (window == container) { return; }
		if (container == null) { createContainer(); }
		
		WindowManager.addWindow(container.getFrame(), window.getFrame());
		childFrames.add(window);
	}
	
	/**
	 * Remove a window from the container window
	 *
	 * @param window Window to remove
	 */
	protected void delWindow(final DCCFrame window) {
		if (container == null) { return; }
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
			if (childFrames.size() == 0) {
				container.close();
				container = null;
			}
		}
	}
	
	/**
	 * Called when the plugin is loaded.
	 */
	@Override
	public void onLoad() {
		command = new DCCCommand(this);
	}
	
	/**
	 * Called when this plugin is Unloaded
	 */
	@Override
	public void onUnload() {
		CommandManager.unregisterCommand(command);
		container.close();
	}
	
	/**
	 * Get SVN Version information.
	 *
	 * @return SVN Version String
	 */
	public static String getSvnInfo() { return "$Id: DCCPlugin.java 969 2007-04-30 18:38:20Z ShaneMcC $"; }	
}

