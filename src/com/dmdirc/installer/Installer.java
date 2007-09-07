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

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.File;
import java.io.FilenameFilter;
import java.nio.channels.FileChannel;

/**
 * Installs DMDirc
 *
 * @author Shane Mc Cormack
 */
public abstract class Installer {
	/** Put a shortcut on the desktop */
	public static final int SHORTCUT_DESKTOP = 1;
	/** Put a shortcut on the menu (kmenu, start menu etc) */
	public static final int SHORTCUT_MENU = 2;
//	/** Put a shortcut on the quicklaunch bar (Windows Only) */
//	public static final int SHORTCUT_QUICKLAUNCH = 4;
	
	/**
	 * Get the default install location
	 */
	abstract String defaultInstallLocation();
	
	/**
	 * Main Setup stuff
	 *
	 * @param location Location where app will be installed to.
	 * @param step The step that called this
	 * @return TRue if installation passed, else false; 
	 */
	public boolean doSetup(final String location, final TextStep step) {
		// Create the directory
		final File directory = new File(location);
		if (!directory.exists()) { directory.mkdir(); }
	
		try {
			File dir = new File(".");
			FilenameFilter filter = new FilenameFilter() {
				public boolean accept(File dir, String name) {
					return !name.startsWith(".") &&
					       !name.equalsIgnoreCase("installer.jar") &&
					       !name.equalsIgnoreCase("setup.exe") &&
					       !name.equalsIgnoreCase("setup.sh") &&
					       !name.equalsIgnoreCase("shortcut.exe");
				}
			};
			String[] children = dir.list(filter);
			if (children != null) {
				for (String filename : children) {
					step.addText("Copying "+filename);
					copyFile(filename, location+File.separator+filename);
				}
			}
		} catch (IOException e) {
			step.addText("Error copying files: "+e.getMessage());
			return false;
		}
		step.addText("File Copying Complete.");
		return true;
	}
	
	/**
	 * Setup shortcuts
	 *
	 * @param location Location where app will be installed to.
	 * @param step The step that called this
	 * @param shortcutType TYpe of shortcuts to add.
	 */
	abstract void setupShortcuts(final String location, final TextStep step, final int shortcutType);
	
	/**
	 * Copy a file from one location to another.
	 * Based on http://www.exampledepot.com/egs/java.io/CopyFile.html
	 *
	 * @param srcFile Original file
	 * @param dstFile New file
	 */
	protected final void copyFile(final String srcFile, final String dstFile) throws IOException {
		if (new File(srcFile).exists()) {
			FileChannel srcChannel = new FileInputStream(srcFile).getChannel();
			FileChannel dstChannel = new FileOutputStream(dstFile).getChannel();
			
			dstChannel.transferFrom(srcChannel, 0, srcChannel.size());
			
			srcChannel.close();
			dstChannel.close();
		} else {
			throw new IOException(srcFile+" does not exist.");
		}
	}
}