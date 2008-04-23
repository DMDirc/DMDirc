/*
 * Copyright (c) 2006-2008 Chris Smith, Shane Mc Cormack, Gregory Holmes
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
 *
 * SVN: $Id$
 */
package com.dmdirc.plugins;

import com.dmdirc.Main;
import com.dmdirc.actions.ActionManager;
import com.dmdirc.actions.CoreActionType;
import com.dmdirc.util.resourcemanager.ResourceManager;
import com.dmdirc.logger.Logger;
import com.dmdirc.logger.ErrorLevel;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Properties;
import java.util.List;
import java.util.ArrayList;

public class PluginInfo implements Comparable<PluginInfo> {
	/** Plugin Meta Data */
	private Properties metaData = null;
	/** File that this plugin was loaded from */
	private final String filename;
	/** The actual Plugin from this jar */
	private Plugin plugin = null;
	/** The classloader used for this Plugin */
	private PluginClassLoader classloader = null;
	/** The resource manager used by this pluginInfo */
	private ResourceManager myResourceManager = null;
	/** Is this plugin only loaded temporarily? */
	private boolean tempLoaded = false;
	/** List of classes this plugin has */
	private List<String> myClasses = new ArrayList<String>();
	/** Requirements error message. */
	private String requirementsError = "";

	/**
	 * Create a new PluginInfo.
	 *
	 * @param filename File that this plugin is stored in.
	 * @throws PluginException if there is an error loading the Plugin
	 */
	public PluginInfo(final String filename) throws PluginException {
		this(filename, true);
	}

	/**
	 * Create a new PluginInfo.
	 *
	 * @param filename File that this plugin is stored in.
	 * @param load Should this plugin be loaded, or is this just a placeholder? (true for load, false for placeholder)
	 * @throws PluginException if there is an error loading the Plugin
	 */
	public PluginInfo(final String filename, final boolean load) throws PluginException {
		this.filename = filename;

		if (!load) { return; }
		
		// Check for updates.
		if (new File(getFullFilename()+".update").exists() && new File(getFullFilename()).delete()) {
			new File(getFullFilename()+".update").renameTo(new File(getFullFilename()));
		}
		
		ResourceManager res;
		try {
			res = getResourceManager();
		} catch (IOException ioe) {
			throw new PluginException("Plugin "+filename+" failed to load, error with resourcemanager: "+ioe.getMessage(), ioe);
		}

		try {
			if (res.resourceExists("META-INF/plugin.info")) {
				metaData = new Properties();
				metaData.load(res.getResourceInputStream("META-INF/plugin.info"));
			} else {
				throw new PluginException("Plugin "+filename+" failed to load, plugin.info doesn't exist in jar");
			}
		} catch (PluginException pe) {
			// Stop the next catch Catching the one we threw ourself
			throw pe;
		} catch (Exception e) {
			throw new PluginException("Plugin "+filename+" failed to load, plugin.info failed to open - "+e.getMessage(), e);
		}

		if (getVersion() < 0) {
			throw new PluginException("Plugin "+filename+" failed to load, incomplete plugin.info (Missing or invalid 'version')");
		} else if (getAuthor().isEmpty()) {
			throw new PluginException("Plugin "+filename+" failed to load, incomplete plugin.info (Missing 'author')");
		} else if (getName().isEmpty()) {
			throw new PluginException("Plugin "+filename+" failed to load, incomplete plugin.info (Missing 'name')");
		} else if (getMinVersion().isEmpty()) {
			throw new PluginException("Plugin "+filename+" failed to load, incomplete plugin.info (Missing 'minversion')");
		} else if (getMainClass().isEmpty()) {
			throw new PluginException("Plugin "+filename+" failed to load, incomplete plugin.info (Missing 'mainclass')");
		}

		if (checkRequirements()) {
			final String mainClass = getMainClass().replace('.', '/')+".class";
			if (!res.resourceExists(mainClass)) {
				throw new PluginException("Plugin "+filename+" failed to load, main class file ("+mainClass+") not found in jar.");
			}

			for (final String classfilename : res.getResourcesStartingWith("")) {
				String classname = classfilename.replace('/', '.');
				if (classname.matches("^.*\\.class$")) {
					classname = classname.replaceAll("\\.class$", "");
					myClasses.add(classname);
				}
			}

			if (isPersistant()) { loadEntirePlugin(); }
		} else {
			// throw new PluginException("Plugin "+filename+" was not loaded, one or more requirements not met ("+requirements+")");
		}
		myResourceManager = null;
	}

	/**
	 * Get the contents of requirementsError
	 *
	 * @return requirementsError
	 */
	public String getRequirementsError() {
		return requirementsError;
	}

	/**
	 * Get the resource manager for this plugin
	 *
	 * @throws IOException if there is any problem getting a ResourceManager for this plugin
	 */
	public synchronized ResourceManager getResourceManager() throws IOException {
		if (myResourceManager == null) {
			final String directory = PluginManager.getPluginManager().getDirectory();
			myResourceManager = ResourceManager.getResourceManager("jar://"+directory+filename);
		}
		return myResourceManager;
	}

	/**
	 * Checks to see if the minimum version requirement of the plugin is
	 * satisfied.
	 * If either version is non-positive, the test passes.
	 * On failure, the requirementsError field will contain a user-friendly
	 * error message.
	 *
	 * @param desired The desired minimum version of DMDirc.
	 * @param actual The actual current version of DMDirc.
	 * @return True if the test passed, false otherwise
	 */
	protected boolean checkMinimumVersion(final String desired, final int actual) {
		int idesired;
		
		try {
			idesired = Integer.parseInt(desired);
		} catch (NumberFormatException ex) {
			requirementsError = "'minversion' is a non-integer";
			return false;
		}
		
		if (actual > 0 && idesired > 0 && actual < idesired) {
			requirementsError = "Plugin is for a newer version of DMDirc";
			return false;
		} else {
			return true;
		}
	}

	/**
	 * Checks to see if the maximum version requirement of the plugin is
	 * satisfied.
	 * If either version is non-positive, the test passes.
	 * If the desired version is empty, the test passes.
	 * On failure, the requirementsError field will contain a user-friendly
	 * error message.
	 *
	 * @param desired The desired maximum version of DMDirc.
	 * @param actual The actual current version of DMDirc.
	 * @return True if the test passed, false otherwise
	 */
	protected boolean checkMaximumVersion(final String desired, final int actual) {
		int idesired;
		
		if (desired.isEmpty()) {
			return true;
		}
		
		try {
			idesired = Integer.parseInt(desired);
		} catch (NumberFormatException ex) {
			requirementsError = "'maxversion' is a non-integer";
			return false;
		}
		
		if (actual > 0 && idesired > 0 && actual > idesired) {
			requirementsError = "Plugin is for an older version of DMDirc";
			return false;
		} else {
			return true;
		}
	}
	
	/**
	 * Checks to see if the OS requirements of the plugin are satisfied.
	 * If the desired string is empty, the test passes.
	 * Otherwise it is used as one to three colon-delimited regular expressions,
	 * to test the name, version and architecture of the OS, respectively.
	 * On failure, the requirementsError field will contain a user-friendly
	 * error message.
	 * 
	 * @param desired The desired OS requirements
	 * @param actualName The actual name of the OS
	 * @param actualVersion The actual version of the OS
	 * @param actualArch The actual architecture of the OS
	 * @return True if the test passes, false otherwise
	 */
	protected boolean checkOS(final String desired, final String actualName, final String actualVersion, final String actualArch) {
		if (desired.isEmpty()) {
			return true;
		}
		
		final String[] desiredParts = desired.split(":");
		
		if (!actualName.toLowerCase().matches(desiredParts[0])) {
			requirementsError = "Invalid OS. (Wanted: '" + desiredParts[0] + "', actual: '" + actualName + "')";
			return false;
		} else if (desiredParts.length > 1 && !actualVersion.toLowerCase().matches(desiredParts[1])) {
			requirementsError = "Invalid OS version. (Wanted: '" + desiredParts[1] + "', actual: '" + actualVersion + "')";
			return false;
		} else if (desiredParts.length > 2 && !actualArch.toLowerCase().matches(desiredParts[2])) {
			requirementsError = "Invalid OS architecture. (Wanted: '" + desiredParts[2] + "', actual: '" + actualArch + "')";
			return false;
		}
		
		return true;
	}
	
	/**
	 * Checks to see if the UI requirements of the plugin are satisfied.
	 * If the desired string is empty, the test passes.
	 * Otherwise it is used as a regular expressions against the package of the
	 * UIController to test what UI is currently in use.
	 * On failure, the requirementsError field will contain a user-friendly
	 * error message.
	 * 
	 * @param desired The desired UI requirements
	 * @param actual The package of the current UI in use.
	 * @return True if the test passes, false otherwise
	 */
	protected boolean checkUI(final String desired, final String actual) {
		if (desired.isEmpty()) {
			return true;
		}
		
		if (!actual.toLowerCase().matches(desired)) {
			requirementsError = "Invalid UI. (Wanted: '" + desired + "', actual: '" + actual + "')";
			return false;
		}
		return true;
	}
	
	/**
	 * Checks to see if the file requirements of the plugin are satisfied.
	 * If the desired string is empty, the test passes.
	 * Otherwise it is passed to File.exists() to see if the file is valid.
	 * Multiple files can be specified by using a "," to separate. And either/or
	 * files can be specified using a "|" (eg /usr/bin/bash|/bin/bash)
	 * If the test fails, the requirementsError field will contain a
	 * user-friendly error message.
	 *
	 * @param desired The desired file requirements
	 * @return True if the test passes, false otherwise
	 */
	protected boolean checkFiles(final String desired) {
		if (desired.isEmpty()) {
			return true;
		}
	
		for (String files : desired.split(",")) {
			final String[] filelist = files.split("\\|");
			boolean foundFile = false;
			for (String file : filelist) {
				if ((new File(file)).exists()) {
					foundFile = true;
					break;
				}
			}
			if (!foundFile) {
				requirementsError = "Required file '"+files+"' not found";
				return false;
			}
		}
		return true;
	}
	
	/**
	 * Checks to see if the plugin requirements of the plugin are satisfied.
	 * If the desired string is empty, the test passes.
	 * Plugins should be specified as:
	 * plugin1[:minversion[:maxversion]],plugin2[:minversion[:maxversion]]
	 * Plugins will be attempted to be loaded if not loaded, else the test will
	 * fail if the versions don't match, or the plugin isn't known.
	 * If the test fails, the requirementsError field will contain a
	 * user-friendly error message.
	 *
	 * @param desired The desired file requirements
	 * @return True if the test passes, false otherwise
	 */
	protected boolean checkPlugins(final String desired) {
		if (desired.isEmpty()) {
			return true;
		}
	
		for (String plugin : desired.split(",")) {
			final String[] data = plugin.split(":");
			final PluginInfo pi = PluginManager.getPluginManager().getPluginInfoByName(data[0]);
			if (pi == null) {
				requirementsError = "Required plugin '"+data[0]+"' was not found";
				return false;
			} else {
				if (data.length > 1) {
					// Check plugin minimum version matches.
					try {
						final int minversion = Integer.parseInt(data[1]);
						if (pi.getVersion() < minversion) {
							requirementsError = "Plugin '"+data[0]+"' is too old (Required Version: "+minversion+", Actual Version: "+pi.getVersion()+")";
							return false;
						} else {
							if (data.length > 2) {
								// Check plugin maximum version matches.
								try {
									final int maxversion = Integer.parseInt(data[2]);
									if (pi.getVersion() > maxversion) {
										requirementsError = "Plugin '"+data[0]+"' is too new (Required Version: "+maxversion+", Actual Version: "+pi.getVersion()+")";
										return false;
									}
								} catch (NumberFormatException nfe) {
									requirementsError = "Plugin max-version '"+data[2]+"' for plugin ('"+data[0]+"') is a non-integer";
									return false;
								}
							}
						}
					} catch (NumberFormatException nfe) {
						requirementsError = "Plugin min-version '"+data[1]+"' for plugin ('"+data[0]+"') is a non-integer";
						return false;
					}
				}
				// Make sure the required plugin is loaded if its not already,
				pi.loadPlugin();
			}
		}
		return true;
	}

	/**
	 * Are the requirements for this plugin met?
	 *
	 * @return true/false (Actual error if false is in the requirementsError field)
	 */
	public boolean checkRequirements() {
		if (metaData == null) {
			// No meta-data, so no requirements.
			return true;
		}
		
		final String uiPackage;
		if (Main.getUI().getClass().getPackage() != null) {
			uiPackage = Main.getUI().getClass().getPackage().getName();
		} else {
			final String uiController = Main.getUI().getClass().getName();
			if (uiController.lastIndexOf('.') >= 0) {
				uiPackage = uiController.substring(0,uiController.lastIndexOf('.'));
			} else {
				uiPackage = uiController;
			}
		}
		
		if (!checkMinimumVersion(getMinVersion(), Main.SVN_REVISION) ||
		    !checkMaximumVersion(getMaxVersion(), Main.SVN_REVISION) ||
		    !checkFiles(getMetaInfo(new String[]{"required-files", "require-files", "required-file", "require-file"})) ||
		    !checkUI(getMetaInfo(new String[]{"required-ui", "require-ui"}), uiPackage) ||
		    !checkPlugins(getMetaInfo(new String[]{"required-plugins", "require-plugins", "required-plugin", "require-plugin"})) ||
		    !checkOS(getMetaInfo(new String[]{"required-os", "require-os"}), System.getProperty("os.name"), System.getProperty("os.version"), System.getProperty("os.arch"))
		    ) {
			return false;
		}
		
		// All requirements passed, woo \o
		return true;
	}

	/**
	 * Is this plugin loaded?
	 */
	public boolean isLoaded() {
		return (plugin != null) && !tempLoaded;
	}

	/**
	 * Is this plugin temporarily loaded?
	 */
	public boolean isTempLoaded() {
		return (plugin != null) && tempLoaded;
	}

	/**
	 * Load entire plugin.
	 * This loads all files in the jar immediately.
	 *
	 * @throws PluginException if there is an error with the resourcemanager
	 */
	private void loadEntirePlugin() throws PluginException {
		// Load the main "Plugin" from the jar
		loadPlugin();

		// Now load all the rest.
		for (String classname : myClasses) {
			loadClass(classname);
		}
		myResourceManager = null;
	}

	/**
	 * Load the plugin files temporarily.
	 */
	public void loadPluginTemp() {
		tempLoaded = true;
		loadPlugin();
	}

	/**
	 * Load a plugin permanently, if it was previously loaded as temp
	 */
	public void loadPluginPerm() {
		if (isTempLoaded()) {
			tempLoaded = false;
			plugin.onLoad();
		}
	}

	/**
	 * Load the plugin files.
	 */
	public void loadPlugin() {
		if (isLoaded() || isTempLoaded() || metaData == null) {
			return;
		}
		loadClass(getMainClass());
		if (isLoaded()) {
			ActionManager.processEvent(CoreActionType.PLUGIN_LOADED, null, this);
		}
		myResourceManager = null;
	}

	/**
	 * Load the given classname.
	 *
	 * @param classname Class to load
	 */
	private void loadClass(final String classname) {
		try {
			if (classloader == null) {
				classloader = new PluginClassLoader(this);
			}

			// Don't reload a class if its already loaded.
			if (classloader.isClassLoaded(classname, true)) {
				return;
			}

			final Class<?> c = classloader.loadClass(classname);
			final Constructor<?> constructor = c.getConstructor(new Class[] {});

			final Object temp = constructor.newInstance(new Object[] {});

			if (temp instanceof Plugin) {
				if (((Plugin) temp).checkPrerequisites()) {
					plugin = (Plugin) temp;
					if (!tempLoaded) {
						plugin.onLoad();
					}
				} else {
					if (!tempLoaded) {
						Logger.userError(ErrorLevel.LOW, "Prerequisites for plugin not met. ('"+filename+":"+getMainClass()+"')");
					}
				}
			}
		} catch (ClassNotFoundException cnfe) {
			Logger.userError(ErrorLevel.LOW, "Class not found ('"+filename+":"+getMainClass()+":"+classname+"')", cnfe);
		} catch (NoSuchMethodException nsme) {
			// Don't moan about missing constructors for any class thats not the main Class
			if (classname.equals(getMainClass())) {
				Logger.userError(ErrorLevel.LOW, "Constructor missing ('"+filename+":"+getMainClass()+":"+classname+"')", nsme);
			}
		} catch (IllegalAccessException iae) {
			Logger.userError(ErrorLevel.LOW, "Unable to access constructor ('"+filename+":"+getMainClass()+":"+classname+"')", iae);
		} catch (InvocationTargetException ite) {
			Logger.userError(ErrorLevel.LOW, "Unable to invoke target ('"+filename+":"+getMainClass()+":"+classname+"')", ite);
		} catch (InstantiationException ie) {
			Logger.userError(ErrorLevel.LOW, "Unable to instantiate plugin ('"+filename+":"+getMainClass()+":"+classname+"')", ie);
		} catch (NoClassDefFoundError ncdf) {
			Logger.userError(ErrorLevel.LOW, "Unable to instantiate plugin ('"+filename+":"+getMainClass()+":"+classname+"'): Unable to find class: " + ncdf.getMessage(), ncdf);
		} catch (VerifyError ve) {
			Logger.userError(ErrorLevel.LOW, "Unable to instantiate plugin ('"+filename+":"+getMainClass()+":"+classname+"') - Incompatible", ve);
		}
	}

	/**
	 * Unload the plugin if possible.
	 */
	public void unloadPlugin() {
		if (!isPersistant() && (isLoaded() || isTempLoaded())) {
			if (!isTempLoaded()) {
				try {
					plugin.onUnload();
				} catch (Exception e) {
					Logger.userError(ErrorLevel.MEDIUM, "Error in onUnload for "+getName()+":"+e.getMessage(), e);
				}
				ActionManager.processEvent(CoreActionType.PLUGIN_UNLOADED, null, this);
			}
			tempLoaded = false;
			plugin = null;
			classloader = null;
		}
	}

	/**
	 * Get the list of Classes
	 *
	 * @return Classes this plugin has
	 */
	public List<String> getClassList() {
		return myClasses;
	}

	/**
	 * Get the main Class
	 *
	 * @return Main Class to begin loading.
	 */
	public String getMainClass() { return metaData.getProperty("mainclass",""); }

	/**
	 * Get the Plugin for this plugin.
	 *
	 * @return Plugin
	 */
	public Plugin getPlugin() { return plugin; }

	/**
	 * Get the PluginClassLoader for this plugin.
	 *
	 * @return PluginClassLoader
	 */
	protected PluginClassLoader getPluginClassLoader() { return classloader; }

	/**
	 * Get the plugin friendly version
	 *
	 * @return Plugin friendly Version
	 */
	public String getFriendlyVersion() { return metaData.getProperty("friendlyversion",""); }

	/**
	 * Get the plugin version
	 *
	 * @return Plugin Version
	 */
	public int getVersion() {
		try {
			return Integer.parseInt(metaData.getProperty("version","0"));
		} catch (NumberFormatException nfe) {
			return -1;
		}
	}

	/**
	 * Get the id for this plugin on the addons site.
	 * If a plugin has been submitted to addons.dmdirc.com, and plugin.info
	 * contains a property addonid then this will return it.
	 * This is used along with the version property to allow the auto-updater to
	 * update the addon if the author submits a new version to the addons site.
	 *
	 * @return Addon Site ID number
	 *         -1 If not present
	 *         -2 If non-integer
	 */
	public int getAddonID() {
		try {
			return Integer.parseInt(metaData.getProperty("addonid","-1"));
		} catch (NumberFormatException nfe) {
			return -2;
		}
	}

	/**
	 * Is this a persistant plugin?
	 *
	 * @return true if persistant, else false
	 */
	public boolean isPersistant() {
		final String persistance = metaData.getProperty("persistant","no");
		return persistance.equalsIgnoreCase("true") || persistance.equalsIgnoreCase("yes");
	}

	/**
	 * Does this plugin contain any persistant classes?
	 *
	 * @return true if this plugin contains any persistant classes, else false
	 */
	public boolean hasPersistant() {
		final String persistance = metaData.getProperty("persistant","no");
		if (persistance.equalsIgnoreCase("true")) {
			return true;
		} else {
			for (Object keyObject : metaData.keySet()) {
				if (keyObject.toString().toLowerCase().startsWith("persistant-")) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Get a list of all persistant classes in this plugin
	 *
	 * @return List of all persistant classes in this plugin
	 */
	public List<String> getPersistantClasses() {
		final List<String> result = new ArrayList<String>();
		final String persistance = metaData.getProperty("persistant","no");
		if (persistance.equalsIgnoreCase("true")) {
			try {
				ResourceManager res = getResourceManager();

				for (final String filename : res.getResourcesStartingWith("")) {
					if (filename.matches("^.*\\.class$")) {
						result.add(filename.replaceAll("\\.class$", "").replace('/', '.'));
					}
				}
			} catch (IOException e) {
				// Jar no longer exists?
			}
		} else {
			for (Object keyObject : metaData.keySet()) {
				if (keyObject.toString().toLowerCase().startsWith("persistant-")) {
					result.add(keyObject.toString().substring(11));
				}
			}
		}
		return result;
	}

	/**
	 * Is this a persistant class?
	 *
	 * @param classname class to check persistance of
	 * @return true if file (or whole plugin) is persistant, else false
	 */
	public boolean isPersistant(final String classname) {
		if (isPersistant()) {
			return true;
		} else {
			final String persistance = metaData.getProperty("persistant-"+classname,"no");
			return persistance.equalsIgnoreCase("true") || persistance.equalsIgnoreCase("yes");
		}
	}

	/**
	 * Get the plugin Filename.
	 *
	 * @return Filename of plugin
	 */
	public String getFilename() { return filename; }

	/**
	 * Get the full plugin Filename (inc dirname)
	 *
	 * @return Filename of plugin
	 */
	public String getFullFilename() { return PluginManager.getPluginManager().getDirectory()+filename; }

	/**
	 * Get the plugin Author.
	 *
	 * @return Author of plugin
	 */
	public String getAuthor() { return getMetaInfo("author",""); }

	/**
	 * Get the plugin Description.
	 *
	 * @return Description of plugin
	 */
	public String getDescription() { return getMetaInfo("description",""); }

	/**
	 * Get the minimum dmdirc version required to run the plugin.
	 *
	 * @return minimum dmdirc version required to run the plugin.
	 */
	public String getMinVersion() { return getMetaInfo("minversion",""); }

	/**
	 * Get the (optional) maximum dmdirc version on which this plugin can run
	 *
	 * @return optional maximum dmdirc version on which this plugin can run
	 */
	public String getMaxVersion() { return getMetaInfo("maxversion",""); }

	/**
	 * Get the name of the plugin. (Used to identify the plugin)
	 *
	 * @return Name of plugin
	 */
	public String getName() { return getMetaInfo("name",""); }

	/**
	 * Get the nice name of the plugin. (Displayed to users)
	 *
	 * @return Nice Name of plugin
	 */
	public String getNiceName() { return getMetaInfo("nicename",getName()); }

	/**
	 * String Representation of this plugin
	 *
	 * @return String Representation of this plugin
	 */
    @Override
	public String toString() { return getNiceName()+" - "+filename; }

	/**
	 * Get misc meta-information.
	 *
	 * @param metainfo The metainfo to return
	 * @return Misc Meta Info (or "" if not found);
	 */
	public String getMetaInfo(final String metainfo) { return getMetaInfo(metainfo,""); }

	/**
	 * Get misc meta-information.
	 *
	 * @param metainfo The metainfo to return
	 * @param fallback Fallback value if requested value is not found
	 * @return Misc Meta Info (or fallback if not found);
	 */
	public String getMetaInfo(final String metainfo, final String fallback) { return metaData.getProperty(metainfo,fallback); }

	/**
	 * Get misc meta-information.
	 *
	 * @param metainfo The metainfos to look for in order. If the first item in
	 *                 the array is not found, the next will be looked for, and
	 *                 so on until either one is found, or none are found.
	 * @return Misc Meta Info (or "" if none are found);
	 */
	public String getMetaInfo(final String[] metainfo) { return getMetaInfo(metainfo,""); }

	/**
	 * Get misc meta-information.
	 *
	 * @param metainfo The metainfos to look for in order. If the first item in
	 *                 the array is not found, the next will be looked for, and
	 *                 so on until either one is found, or none are found.
	 * @param fallback Fallback value if requested values are not found
	 * @return Misc Meta Info (or "" if none are found);
	 */
	public String getMetaInfo(final String[] metainfo, final String fallback) {
		for (String meta : metainfo) {
			String result = metaData.getProperty(meta);
			if (result != null) { return result; }
		}
		return fallback;
	}

	/**
	 * Compares this object with the specified object for order.
	 * Returns a negative integer, zero, or a positive integer as per String.compareTo();
	 *
	 * @param o Object to compare to
	 * @return a negative integer, zero, or a positive integer.
	 */
    @Override
	public int compareTo(final PluginInfo o) {
		return toString().compareTo(o.toString());
	}
}
