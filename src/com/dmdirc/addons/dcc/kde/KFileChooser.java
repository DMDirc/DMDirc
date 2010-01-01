/*
 * Copyright (c) 2006-2010 Chris Smith, Shane Mc Cormack, Gregory Holmes
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
 * - getFileChooser() will return a JFileChooser object unless the DCC plugin's
 *   config option "general.useKFileChooser" is set to "true" (defaults to false)
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

    /** The plugin that this file chooser is for. */
    private final DCCPlugin plugin;

    /**
     * Should a KFileChooser be used rather than a JFileChooser?
     *
     * @param plugin The DCC Plugin that is requesting a chooser
     * @return return true if getFileChooser() will return a KFileChooser not a
     *         JFileChooser
     */
    public static boolean useKFileChooser(final DCCPlugin plugin) {
        return KDialogProcess.hasKDialog() && IdentityManager.getGlobalConfig().getOptionBool(plugin.getDomain(), "general.useKFileChooser");
    }

    /**
     * Constructs a FileChooser pointing to the user's default directory.
     *
     * @param plugin The DCC Plugin that is requesting a chooser
     * @return The relevant FileChooser
     */
    public static JFileChooser getFileChooser(final DCCPlugin plugin) {
        return useKFileChooser(plugin) ? new KFileChooser(plugin) : new JFileChooser();
    }

    /**
     * Constructs a FileChooser using the given File as the path.
     *
     * @param plugin The DCC Plugin that is requesting a chooser
     * @param currentDirectory Directory to use as the base directory
     * @return The relevant FileChooser
     */
    public static JFileChooser getFileChooser(final DCCPlugin plugin, final File currentDirectory) {
        return useKFileChooser(plugin) ? new KFileChooser(plugin, currentDirectory) : new JFileChooser(currentDirectory);
    }

    /**
     * Constructs a FileChooser using the given current directory and FileSystemView.
     *
     * @param plugin The DCC Plugin that is requesting a chooser
     * @param currentDirectory Directory to use as the base directory
     * @param fsv The FileSystemView to use
     * @return The relevant FileChooser
     */
    public static JFileChooser getFileChooser(final DCCPlugin plugin, final File currentDirectory, final FileSystemView fsv) {
        return useKFileChooser(plugin) ? new KFileChooser(plugin, currentDirectory, fsv) : new JFileChooser(currentDirectory, fsv);
    }

    /**
     * Constructs a FileChooser using the given FileSystemView.
     *
     * @param plugin The DCC Plugin that is requesting a chooser
     * @param fsv The FileSystemView to use
     * @return The relevant FileChooser
     */
    public static JFileChooser getFileChooser(final DCCPlugin plugin, final FileSystemView fsv) {
        return useKFileChooser(plugin) ? new KFileChooser(plugin, fsv) : new JFileChooser(fsv);
    }

    /**
     * Constructs a FileChooser using the given path.
     *
     * @param plugin The DCC Plugin that is requesting a chooser
     * @param currentDirectoryPath Directory to use as the base directory
     * @return The relevant FileChooser
     */
    public static JFileChooser getFileChooser(final DCCPlugin plugin, final String currentDirectoryPath) {
        return useKFileChooser(plugin) ? new KFileChooser(plugin, currentDirectoryPath) : new JFileChooser(currentDirectoryPath);
    }

    /**
     * Constructs a FileChooser using the given current directory path and FileSystemView.
     *
     * @param plugin The DCC Plugin that is requesting a chooser
     * @param currentDirectoryPath Directory to use as the base directory
     * @param fsv The FileSystemView to use
     * @return The relevant FileChooser
     */
    public static JFileChooser getFileChooser(final DCCPlugin plugin, final String currentDirectoryPath, final FileSystemView fsv) {
        return useKFileChooser(plugin) ? new KFileChooser(plugin, currentDirectoryPath, fsv) : new JFileChooser(currentDirectoryPath, fsv);
    }

    /**
     * Constructs a FileChooser pointing to the user's default directory.
     *
     * @param plugin The plugin that owns this KFileChooser
     */
    private KFileChooser(final DCCPlugin plugin) {
        super();

        this.plugin = plugin;
    }

    /**
     * Constructs a FileChooser using the given File as the path.
     *
     * @param plugin The plugin that owns this KFileChooser
     * @param currentDirectory Directory to use as the base directory
     */
    private KFileChooser(final DCCPlugin plugin, final File currentDirectory) {
        super(currentDirectory);

        this.plugin = plugin;
    }

    /**
     * Constructs a FileChooser using the given current directory and FileSystemView.
     *
     * @param plugin The plugin that owns this KFileChooser
     * @param currentDirectory Directory to use as the base directory
     * @param fsv The FileSystemView to use
     */
    private KFileChooser(final DCCPlugin plugin, final File currentDirectory, final FileSystemView fsv) {
        super(currentDirectory, fsv);

        this.plugin = plugin;
    }

    /**
     * Constructs a FileChooser using the given FileSystemView.
     *
     * @param plugin The plugin that owns this KFileChooser
     * @param fsv The FileSystemView to use
     */
    private KFileChooser(final DCCPlugin plugin, final FileSystemView fsv) {
        super(fsv);

        this.plugin = plugin;
    }

    /**
     * Constructs a FileChooser using the given path.
     *
     * @param plugin The plugin that owns this KFileChooser
     * @param currentDirectoryPath Directory to use as the base directory
     */
    private KFileChooser(final DCCPlugin plugin, final String currentDirectoryPath) {
        super(currentDirectoryPath);

        this.plugin = plugin;
    }

    /**
     * Constructs a FileChooser using the given current directory path and FileSystemView.
     *
     * @param plugin The plugin that owns this KFileChooser
     * @param currentDirectoryPath Directory to use as the base directory
     * @param fsv The FileSystemView to use
     */
    private KFileChooser(final DCCPlugin plugin, final String currentDirectoryPath, final FileSystemView fsv) {
        super(currentDirectoryPath, fsv);

        this.plugin = plugin;
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
        if (!useKFileChooser(plugin)) {
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
        if (!useKFileChooser(plugin)) {
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
                params.add(getCurrentDirectory().getPath() + File.separator + getSelectedFile().getPath());
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
