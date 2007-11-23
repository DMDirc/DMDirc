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

import java.io.File;
import java.io.PrintWriter;
import java.io.IOException;

/**
 * Installs DMDirc on linux
 *
 * @author Shane Mc Cormack
 */
public class LinuxInstaller extends Installer {
	/**
	 * Are we running as root?
	 */
	private boolean isRoot() {
		return (CLIParser.getCLIParser().getParamNumber("-isroot") > 0);
	}

	/**
	 * Get the default install location
	 */
	public String defaultInstallLocation() {
		if (CLIParser.getCLIParser().getParamNumber("-directory") > 0) {
			return CLIParser.getCLIParser().getParam("-directory").getStringValue();
		} else {
			if (isRoot()) {
				return "/usr/local/DMDirc";
			} else {
				return System.getProperty("user.home") + "/DMDirc";
			}
		}
	}
	
	/**
	 * Check if this OS supports a given shortcut Type
	 *
	 * @param shortcutType Type of shortcut to check
	 * @return True if this OS supports a given shortcut Type
	 */
	public boolean supportsShortcut(final ShortcutType shortcutType) {
		switch (shortcutType) {
			case QUICKLAUNCH:
				return false;
			case DESKTOP:
				// No desktop for root
				return !isRoot();
			case UNINSTALLER:
			case MENU:
				// Both root and non-root have a menu and uninstaller
				return true;
			default:
				// Anything else that gets added should be false until the relevent
				// code is added
				return false;
		}
	}

	/**
	 * Setup shortcut
	 *
	 * @param location Location where app will be installed to.
	 * @param shortcutType Type of shortcut to add.
	 */
	public void setupShortcut(final String location, final ShortcutType shortcutType) {
		if (!supportsShortcut(shortcutType)) {
			step.addText(" - Error creating shortcut. Not applicable to this Operating System");
			return;
		}
		
		PrintWriter writer = null;
		try {
			String filename;

			switch (shortcutType) {
				case DESKTOP:
					filename = System.getProperty("user.home")+"/Desktop/DMDirc.desktop";
					break;
					
				case MENU:
					if (CLIParser.getCLIParser().getParamNumber("-isroot") > 0) {
						filename = "/usr/share/applications/DMDirc.desktop";
					} else {
						filename = System.getProperty("user.home")+"/.local/share/applications/DMDirc.desktop";
					}
					break;
					
				case UNINSTALLER:
					writer = new PrintWriter(location+"/uninstall.sh");
					writer.println("#!/bin/sh");
					writer.println("echo \"Uninstalling dmdirc\"");
					writer.println("echo \"Removing Shortcuts..\"");
					if (CLIParser.getCLIParser().getParamNumber("-isroot") > 0) {
						writer.println("rm -Rfv /usr/share/applications/DMDirc.desktop");
					} else {
						writer.println("rm -Rfv "+System.getProperty("user.home")+"/.local/share/applications/DMDirc.desktop");
						writer.println("rm -Rfv "+System.getProperty("user.home")+"/Desktop/DMDirc.desktop");
					}
					writer.println("echo \"Removing Installation Directory\"");
					writer.println("rm -Rfv "+location);
					writer.println("echo \"Done.\"");
					
					(new File(location+"/uninstall.sh")).setExecutable(true);
					return;
					
				default:
					step.addText(" - Error creating shortcut. Not applicable to this Operating System");
					return;
			}
			writer = new PrintWriter(filename);
			writeFile(writer, location);
		} catch (Exception e) {
			step.addText(" - Error creating shortcut: "+e.toString());
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
//		writer.println("Exec=java -jar "+location+"/DMDirc.jar");
		writer.println("Exec="+location+"/DMDirc.sh");
		writer.println("GenericName=IRC Client");
		writer.println("Icon="+location+"/icon.svg");
		if (isRoot()) {
			writer.println("Name=DMDirc (Global)");
		} else {
			writer.println("Name=DMDirc");
		}
		writer.println("StartupNotify=true");
		writer.println("Terminal=false");
		writer.println("TerminalOptions=");
		writer.println("Type=Application");
	}
}