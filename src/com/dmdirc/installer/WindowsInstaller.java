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
import java.util.ArrayList;

/**
 * Installs DMDirc on windows
 *
 * @author Shane Mc Cormack
 */
public class WindowsInstaller extends Installer {
	/**
	 * Get the default install location
	 */
	public String defaultInstallLocation() {
		if (CLIParser.getCLIParser().getParamNumber("-directory") > 0) {
			return CLIParser.getCLIParser().getParam("-directory").getStringValue();
		} else {
			String filename = System.getenv("PROGRAMFILES");
			if (filename == null) {
				if (isVista()) {
					filename = System.getProperty("user.home")+"\\Desktop\\DMDirc";
				} else {
					filename = "C:\\Program Files";
				}
			}
			return filename+"\\DMDirc";
		}
	}
	
	/**
	 * Are we running vista? -_-
	 *
	 * @return True if this is vista.
	 */
	public boolean isVista() {
		return System.getProperty("os.name").indexOf("Vista") >= 0;
	}
	
	/**
	 * Are we running NT?
	 *
	 * @return True if this is NT.
	 */
	public boolean isNT() {
		final String osName = System.getProperty("os.name");
		return (osName.indexOf("NT") >= 0  || osName.indexOf("2000") >= 0 || osName.indexOf("2003") >= 0 || osName.indexOf("XP") >= 0);
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
				// Only windows 95 doesn't have quick launch
				return !(System.getProperty("os.name").indexOf("95") >= 0);
			case DESKTOP:
			case MENU:
			case UNINSTALLER:
			case PROTOCOL:
				// All versions of windows have desktop, menu, uninstaller and protocol
				return true;
			default:
				// Anything else that gets added should be false until the relevent
				// code is added
				return false;
		}
	}
	
	/**
	 * Add a registry key.
	 *
	 * @param key Key to add.
	 */
	public void addRegistryKey(final String key) {
		step.addText(" - Adding Key: "+key);
		final String[] addKey = new String[] {"reg.exe", "add", key, "/f"};
		try {
			final Process registryProcess = Runtime.getRuntime().exec(addKey);
			new StreamReader(registryProcess.getInputStream()).start();
			new StreamReader(registryProcess.getErrorStream()).start();
			registryProcess.waitFor();
			if (registryProcess.exitValue() != 0) {
				step.addText(" - Error adding key: Unknown Reason");
			}
		} catch (Exception e) {
			step.addText(" - Error adding registry key: "+e.getMessage());
		}
	}
	
	/**
	 * Modify a registry value.
	 *
	 * @param key Key to use.
	 * @param value Value to modify.
	 * @param data Data for key.
	 */
	public void editRegistryValue(final String key, final String value, final String data) {
		editRegistryValue(key, value, "REG_SZ", data);
	}
	
	/**
	 * Modify a registry value.
	 *
	 * @param key Key to use.
	 * @param value Value to modify.
	 * @param type Type of data.
	 * @param data Data for key.
	 */
	public void editRegistryValue(final String key, final String value, final String type, final String data) {
		final ArrayList<String> params = new ArrayList<String>();
		step.addText(" - Editing value: "+key+"\\"+value);
		params.add("reg.exe");
		params.add("add");
		params.add(key);
		params.add("/f");
		if (value != "") {
			params.add("/v");
			params.add(value);
		} else {
			params.add("/ve");
		}
		params.add("/t");
		params.add(type);
		if (data != "") {
			params.add("/d");
			params.add(data);
		}
		
		try {
			final Process registryProcess = Runtime.getRuntime().exec(params.toArray(new String[0]));
			new StreamReader(registryProcess.getInputStream()).start();
			new StreamReader(registryProcess.getErrorStream()).start();
			registryProcess.waitFor();
			if (registryProcess.exitValue() != 0) {
				step.addText(" - Error editing value: Unknown Reason");
			}
		} catch (Exception e) {
			step.addText(" - Error editing registry key: "+e.getMessage());
		}
	}
	
	/**
	 * Setup shortcut
	 *
	 * @param location Location where app will be installed to.
	 * @param shortcutType Type of shortcut to add.
	 */
	public void setupShortcut(final String location, final ShortcutType shortcutType) {
		// Shortcut.exe is from http://www.optimumx.com/download/#Shortcut
		
		if (!supportsShortcut(shortcutType)) {
			step.addText(" - Error creating shortcut. Not applicable to this Operating System");
			return;
		}
		
		if (new File("Shortcut.exe").exists()) {
			String filename = "";
			File dir;
			
			switch (shortcutType) {
				case DESKTOP:
					if (isNT() || isVista()) {
						filename = System.getProperty("user.home")+"\\Desktop";
					} else {
						filename = System.getenv("WINDIR")+"\\Desktop";
					}
					break;
					
				case MENU:
					if (isVista()) {
						filename = System.getenv("APPDATA")+"\\Microsoft\\Windows";
					} else {
						filename = System.getProperty("user.home");
					}
					filename = filename+"\\Start Menu\\Programs\\DMDirc";
					break;
					
				case QUICKLAUNCH:
					if (isVista()) {
						filename = System.getProperty("user.home")+"\\AppData\\Roaming\\Microsoft\\Internet Explorer\\Quick Launch";
					} else {
						filename = System.getProperty("user.home")+"\\Application Data\\Microsoft\\Internet Explorer\\Quick Launch";
					}
					break;
					
				case UNINSTALLER:
					// Registry hax!
					final String key = "HKLM\\SOFTWARE\\Microsoft\\Windows\\CurrentVersion\\Uninstall\\DMDirc";
					addRegistryKey(key);
					editRegistryValue(key, "Comments", "DMDirc IRC Client");
					editRegistryValue(key, "DisplayName", "DMDirc IRC Client");
					editRegistryValue(key, "DisplayIcon", location+"\\icon.ico");
					editRegistryValue(key, "UninstallString", location+"\\Uninstaller.exe");
					editRegistryValue(key, "Publisher", "DMDirc.com");
					editRegistryValue(key, "URLInfoAbout", "http://www.DMDirc.com/");
					editRegistryValue(key, "URLUpdateInfo", "http://www.DMDirc.com/");
					editRegistryValue(key, "InstallDir", location);
					return;
					
				case PROTOCOL:
					// Add needed keys.
					addRegistryKey("HKCR\\irc");
					addRegistryKey("HKCR\\irc\\DefaultIcon");
					addRegistryKey("HKCR\\irc\\Shell");
					addRegistryKey("HKCR\\irc\\Shell\\open");
					addRegistryKey("HKCR\\irc\\Shell\\open\\command");
					// Now the values
					editRegistryValue("HKCR\\irc", "", "URL:IRC Protocol");
					editRegistryValue("HKCR\\irc", "URL Protocol", "");
					editRegistryValue("HKCR\\irc", "EditFlags", "REG_BINARY", "02000000");
					editRegistryValue("HKCR\\irc\\DefaultIcon", "", location+"\\icon.ico");
					editRegistryValue("HKCR\\irc\\Shell\\open\\command", "", "\\\""+location+"\\DMDirc.exe\\\" -e -c %1");
					return;
					
				default:
					step.addText(" - Error creating shortcut. Not applicable to this Operating System");
					return;
			}
			
			if (filename.length() == 0) {
				step.addText(" - Error creating shortcut. Not applicable to this System");
				return;
			}
			
			// Check the dir exists
			dir = new File(filename);
			if (!dir.exists()) { dir.mkdir(); }
			
			// Delete an older shortcut
			File oldFile = new File(filename+"\\DMDirc.lnk");
			if (oldFile.exists()) { oldFile.delete(); }
			
			try {
//				final String thisDirName = new File("").getAbsolutePath();
					final String[] command = new String[] {
//				                      thisDirName+"/Shortcut.exe",
				                      "Shortcut.exe",
				                      "/F:"+filename+"\\DMDirc.lnk",
				                      "/A:C",
//				                      "/T:"+location+"\\DMDirc.bat",
//				                      "/T:javaw.exe",
//				                      "/P:-jar DMDirc.jar",
				                      "/T:"+location+"\\DMDirc.exe",
				                      "/W:"+location,
				                      "/I:"+location+"\\icon.ico",
				                      "/D:DMDirc IRC Client"
				                      };
				final Process shortcutProcess = Runtime.getRuntime().exec(command);
				new StreamReader(shortcutProcess.getInputStream()).start();
				new StreamReader(shortcutProcess.getErrorStream()).start();
				shortcutProcess.waitFor();
				if (shortcutProcess.exitValue() != 0) {
					step.addText(" - Error creating shortcut: Unknown Reason");
					System.out.println(java.util.Arrays.toString(command));
					System.out.println("");
					for (String bit : command) {
						System.out.print(bit+' ');
					}
					System.out.println("");
				}
			} catch (Exception e) {
				step.addText(" - Error creating shortcut: "+e.getMessage());
			}
		} else {
			step.addText(" - Error creating shortcut: Unable to find Shortcut.exe");
		}
	}
}