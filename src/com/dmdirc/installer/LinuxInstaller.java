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

package com.dmdirc.installer;

import com.dmdirc.installer.cliparser.CLIParser;
import com.dmdirc.ui.swing.dialogs.wizard.TextStep;

import java.io.PrintWriter;
import java.io.IOException;

/**
 * Installs DMDirc on linux
 *
 * @author Shane Mc Cormack
 */
public class LinuxInstaller extends Installer {
	/**
	 * Get the default install location
	 */
	public String defaultInstallLocation() {
		if (CLIParser.getCLIParser().getParamNumber("-isroot") > 0) {
			return "/usr/local/DMDirc";
		} else {
			return System.getProperty("user.home") + "/DMDirc";
		}
	}
	 
	/**
	 * Setup shortcuts
	 *
	 * @param location Location where app will be installed to.
	 * @param step The step that called this
	 * @param shortcutType TYpe of shortcuts to add.
	 */
	public void setupShortcuts(final String location, final TextStep step, final int shortcutType) {
		PrintWriter writer = null;
		try {
			String filename;
			if ((shortcutType & SHORTCUT_DESKTOP) == SHORTCUT_DESKTOP) {
				filename = System.getProperty("user.home")+"/Desktop/DMDirc.desktop";
			} else if ((shortcutType & SHORTCUT_MENU) == SHORTCUT_MENU) {
				if (CLIParser.getCLIParser().getParamNumber("-isroot") > 0) {
					filename = "/usr/share/applications/DMDirc.desktop";
				} else {
					filename = System.getProperty("user.home")+"/.local/share/applications/DMDirc.desktop";
				}
			} else {
				return;
			}
			writer = new PrintWriter(filename);
			writeFile(writer, location);
		} catch (Exception e) {
			step.addText("Error creating shortcuts: "+e.toString());
		} finally {
			if (writer != null) {
				writer.close();
			}
		}
	}
	
	/**
	 * Write the .desktop file
	 *
	 * @param writer PrintWriter to write to
	 * @param location Location of installed files
	 * @throws IOException if an error occurs when writing
	 */
	private void writeFile(final PrintWriter writer, final String location) throws IOException {
		writer.println("[Desktop Entry]");
		writer.println("Categories=Network;IRCClient;");
		writer.println("Comment=DMDirc IRC Client");
		writer.println("Encoding=UTF-8");
		writer.println("Exec=java -jar "+location+"/DMDirc.jar");
		writer.println("GenericName=IRC Client");
		writer.println("Icon="+location+"/icon.svg");
		writer.println("Name=DMDirc");
		writer.println("StartupNotify=true");
		writer.println("Terminal=false");
		writer.println("TerminalOptions=");
		writer.println("Type=Application");
	}
}