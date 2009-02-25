/*
 * Copyright (c) 2006-2009 Chris Smith, Shane Mc Cormack, Gregory Holmes
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

package com.dmdirc.addons.dcc.kde;

import com.dmdirc.addons.dcc.DCCPlugin;

import com.dmdirc.config.IdentityManager;

import java.awt.Component;
import java.awt.HeadlessException;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileSystemView;

/**
 * JFileChooser that uses KDialog to show the actual chooser.
 * This is quite hacky, and not guarenteed to behave identically to JFileChooser,
 * altho it tries to be as close as possible.
 * Almost a drop in replacement for JFileChooser, replace:
 *    new JFileChooser();
 * with:
 *    KFileChooser.getFileChooser();
 *
 * There are obviously some differences:
 * - File filters must be set using setKDEFileFilter() not using FileFilter objects.
 * - FileSystemView's are ignored
 * - showOpenDialog and showSaveDialog shell kdialog, so only options available
 *   in kdialog work.
 * - getFileChooser() will return a JFileChooser object unless the config option
 *   "plugin-DCC.general.useKFileChooser" is set to "true" (defaults to false)
 *   and kdialog is in either /usr/bin or /bin
 * - Selection mode FILES_AND_DIRECTORIES can not be used
 */
public class KFileChooser extends JFileChooser {
	/**
	 * A version number for this class.
	 * It should be changed whenever the class structure is changed (or anything
	 * else that would prevent serialized objects being unserialized with the new 
	 * class).
	 */
	private static final long serialVersionUID = 200806141;
	
	/** File Filter */
	private String fileFilter = null;
	
	/**
	 * Should a KFileChooser be used rather than a JFileChooser?
	 *
	 * @return return true if getFileChooser() will return a KFileChooser not a
	 *         JFileChooser
	 */
	public static boolean useKFileChooser() {
		return KDialogProcess.hasKDialog() && IdentityManager.getGlobalConfig().getOptionBool("plugin-dcc", "general.useKFileChooser");
	}
	
	/**
	 * Constructs a FileChooser pointing to the user's default directory.
	 */
	public static JFileChooser getFileChooser() {
		return useKFileChooser() ? new KFileChooser() : new JFileChooser();
	}
	
	/**
	 * Constructs a FileChooser using the given File as the path.
	 *
	 * @param currentDirectory Directory to use as the base directory
	 */
	public static JFileChooser getFileChooser(final File currentDirectory) {
		return useKFileChooser() ? new KFileChooser(currentDirectory) : new JFileChooser(currentDirectory);
	}
	
	/**
	 * Constructs a FileChooser using the given current directory and FileSystemView.
	 *
	 * @param currentDirectory Directory to use as the base directory
	 * @param fsv The FileSystemView to use
	 */
	public static JFileChooser getFileChooser(final File currentDirectory, final FileSystemView fsv) {
		return useKFileChooser() ? new KFileChooser(currentDirectory, fsv) : new JFileChooser(currentDirectory, fsv);
	}
	
	/**
	 * Constructs a FileChooser using the given FileSystemView.
	 *
	 * @param fsv The FileSystemView to use
	 */
	public static JFileChooser getFileChooser(final FileSystemView fsv) {
		return useKFileChooser() ? new KFileChooser(fsv) : new JFileChooser(fsv);
	}
	
	/**
	 * Constructs a FileChooser using the given path.
	 *
	 * @param currentDirectoryPath Directory to use as the base directory
	 */
	public static JFileChooser getFileChooser(final String currentDirectoryPath) {
		return useKFileChooser() ? new KFileChooser(currentDirectoryPath) : new JFileChooser(currentDirectoryPath);
	}
	
	/**
	 * Constructs a FileChooser using the given current directory path and FileSystemView.
	 *
	 * @param currentDirectoryPath Directory to use as the base directory
	 * @param fsv The FileSystemView to use
	 */
	public static JFileChooser getFileChooser(String currentDirectoryPath, FileSystemView fsv) {
		return useKFileChooser() ? new KFileChooser(currentDirectoryPath, fsv) : new JFileChooser(currentDirectoryPath, fsv);
	}
	
	/**
	 * Constructs a FileChooser pointing to the user's default directory.
	 */
	private KFileChooser() {
		super();
	}
	
	/**
	 * Constructs a FileChooser using the given File as the path.
	 *
	 * @param currentDirectory Directory to use as the base directory
	 */
	private KFileChooser(final File currentDirectory) {
		super(currentDirectory);
	}
	
	/**
	 * Constructs a FileChooser using the given current directory and FileSystemView.
	 *
	 * @param currentDirectory Directory to use as the base directory
	 * @param fsv The FileSystemView to use
	 */
	private KFileChooser(final File currentDirectory, final FileSystemView fsv) {
		super(currentDirectory, fsv);
	}
	
	/**
	 * Constructs a FileChooser using the given FileSystemView.
	 *
	 * @param fsv The FileSystemView to use
	 */
	private KFileChooser(final FileSystemView fsv) {
		super(fsv);
	}
	
	/**
	 * Constructs a FileChooser using the given path.
	 *
	 * @param currentDirectoryPath Directory to use as the base directory
	 */
	private KFileChooser(final String currentDirectoryPath) {
		super(currentDirectoryPath);
	}
	
	/**
	 * Constructs a FileChooser using the given current directory path and FileSystemView.
	 *
	 * @param currentDirectoryPath Directory to use as the base directory
	 * @param fsv The FileSystemView to use
	 */
	private KFileChooser(String currentDirectoryPath, FileSystemView fsv) {
		super(currentDirectoryPath, fsv);
	}
	
	/**
	 * Set the file filter.
	 *
	 * @param fileFilter File filter (eg "*.php *.jpg" or null for no filter)
	 */
	public void setKDEFileFilter(final String fileFilter) {
		this.fileFilter = fileFilter;
	}
	
	/**
	 * Get the file filter.
	 *
	 * @return File filter (eg "*.php *.jpg" or null for no filter)
	 */
	public String getKDEFileFilter() {
		return fileFilter;
	}
	
	/** {@inheritDoc} */
    @Override
	public int showOpenDialog(final Component parent) throws HeadlessException {
		if (!useKFileChooser()) {
			return super.showOpenDialog(parent);
		}
		final ArrayList<String> params = new ArrayList<String>();
		if (isMultiSelectionEnabled()) {
			params.add("--multiple");
			params.add("--separate-output");
		}
		if (getDialogTitle() != null && !getDialogTitle().isEmpty()) {
			params.add("--caption");
			params.add(getDialogTitle());
		}
		if (getFileSelectionMode() == DIRECTORIES_ONLY) {
			params.add("--getexistingdirectory");
		} else {
			params.add("--getopenfilename");
		}
		if (getSelectedFile() != null && getFileSelectionMode() != DIRECTORIES_ONLY && !getSelectedFile().getPath().isEmpty()) {
			if (getSelectedFile().getPath().charAt(0) != '/') {
				params.add(getCurrentDirectory().getPath() + File.separator + getSelectedFile().getPath());
			} else {
				params.add(getSelectedFile().getPath());
			}
		} else if (getCurrentDirectory() != null) {
			params.add(getCurrentDirectory().getPath());
		}
		if (getFileSelectionMode() != DIRECTORIES_ONLY && fileFilter != null && !fileFilter.isEmpty()) {
			params.add(fileFilter);
		}
		KDialogProcess kdp;
		try {
			kdp = new KDialogProcess(params.toArray(new String[0]));
			kdp.waitFor();
		} catch (Exception e) {
			return JFileChooser.ERROR_OPTION;
		}
		
		if (kdp.getProcess().exitValue() == 0) {
			if (isMultiSelectionEnabled()) {
				final List<String> list = kdp.getStdOutStream().getList();
				final File[] fileList = new File[list.size()];
				for (int i = 0; i < list.size(); ++i) {
					fileList[i] = new File(list.get(i));
				}
				setSelectedFiles(fileList);
			} else {
				setSelectedFile(new File(kdp.getStdOutStream().getList().get(0)));
			}
			return JFileChooser.APPROVE_OPTION;
		} else {
			return JFileChooser.ERROR_OPTION;
		}
	}
	
	/** {@inheritDoc} */
    @Override
	public int showSaveDialog(final Component parent) throws HeadlessException {
		if (!useKFileChooser()) {
			return super.showSaveDialog(parent);
		}
		final ArrayList<String> params = new ArrayList<String>();
		if (getDialogTitle() != null && !getDialogTitle().isEmpty()) {
			params.add("--caption");
			params.add(getDialogTitle());
		}
		params.add("--getsavefilename");
		if (getSelectedFile() != null && !getSelectedFile().getPath().isEmpty()) {
			if (getSelectedFile().getPath().charAt(0) != '/') {
				params.add(getCurrentDirectory().getPath()  + File.separator +  getSelectedFile().getPath());
			} else {
				params.add(getSelectedFile().getPath());
			}
		} else if (getCurrentDirectory() != null) {
			params.add(getCurrentDirectory().getPath());
		}
		KDialogProcess kdp;
		try {
			kdp = new KDialogProcess(params.toArray(new String[0]));
			kdp.waitFor();
		} catch (Exception e) {
			return JFileChooser.ERROR_OPTION;
		}
		
		if (kdp.getProcess().exitValue() == 0) {
			setSelectedFile(new File(kdp.getStdOutStream().getList().get(0)));
			return JFileChooser.APPROVE_OPTION;
		} else {
			return JFileChooser.ERROR_OPTION;
		}
	}
}