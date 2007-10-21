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

import java.io.File;

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
				// All versions of windows have desktop and menu
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
				                      "/T:javaw.exe",
				                      "/P:-jar DMDirc.jar",
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