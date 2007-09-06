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

import com.dmdirc.ui.swing.dialogs.wizard.TextStep;

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
		return "C:\\Program Files\\DMDirc";
	}
	
	/**
	 * Setup shortcuts
	 *
	 * @param location Location where app will be installed to.
	 * @param step The step that called this
	 * @param shortcutType TYpe of shortcuts to add.
	 */
	public void setupShortcuts(final String location, final TextStep step, final int shortcutType) {
		// Shortcut.exe is from http://www.optimumx.com/download/#Shortcut
		if (new File("Shortcut.exe").exists()) {
			String filename;
			File dir;
			if ((shortcutType & SHORTCUT_DESKTOP) == SHORTCUT_DESKTOP) {
				filename = System.getProperty("user.home")+"\\Desktop\\DMDirc.lnk";
				dir = new File(System.getProperty("user.home")+"\\Desktop");
				if (!dir.exists()) { dir.mkdir(); }
			} else if ((shortcutType & SHORTCUT_MENU) == SHORTCUT_MENU) {
				filename = System.getProperty("user.home")+"\\Start Menu\\Programs\\DMDirc\\DMDirc.lnk";
				dir = new File(System.getProperty("user.home")+"\\Start Menu\\Programs\\DMDirc");
				if (!dir.exists()) { dir.mkdir(); }
			} else if ((shortcutType & SHORTCUT_QUICKLAUNCH) == SHORTCUT_QUICKLAUNCH) {
				filename = System.getProperty("user.home")+"\\Application Data\\Microsoft\\Internet Explorer\\Quick Launch\\DMDirc.lnk";
				dir = new File(System.getProperty("user.home")+"\\Application Data\\Microsoft\\Internet Explorer\\Quick Launch");
				if (!dir.exists()) { dir.mkdir(); }
			} else {
				return;
			}
			File oldFile = new File(filename);
			if (oldFile.exists()) { oldFile.delete(); }
			try {
				final Process shortcutProcess = Runtime.getRuntime().exec(new String[] {
				                      "Shotcut.exe",
				                      "/F:"+filename,
				                      "/A:C",
				                      "/T:"+location+"\\DMDirc.bat",
				                      "/W:"+location,
				                      "/I:"+location+"\\icon.ico",
				                      "/D:DMDirc IRC Client"
				                      });
				new StreamReader(shortcutProcess.getInputStream()).start();
				new StreamReader(shortcutProcess.getErrorStream()).start();
				shortcutProcess.waitFor();
				if (shortcutProcess.exitValue() != 0) {
					step.addText("Error creating shortcuts: Unknown Reason");
				}
			} catch (Exception e) {
				step.addText("Error creating shortcuts: "+e.getMessage());
			}
		} else {
			step.addText("Error creating shortcuts: Unable to find Shortcut.exe");
		}
	}
}