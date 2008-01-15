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
import com.dmdirc.actions.interfaces.ActionType;
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
		
		final String requirements = checkRequirements();
		if (requirements.isEmpty()) {
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
			throw new PluginException("Plugin "+filename+" was not loaded, one or more requirements not met ("+requirements+")");
		}
		myResourceManager = null;
	}

	/**
	 * Get the resource manager for this plugin
	 *
	 * @throws IOException if there is any problem getting a ResourceManager for this plugin
	 */
	protected synchronized ResourceManager getResourceManager() throws IOException {
		if (myResourceManager == null) {
			final String directory = PluginManager.getPluginManager().getDirectory();
			myResourceManager = ResourceManager.getResourceManager("jar://"+directory+filename);
		}
		return myResourceManager;
	}

	/**
	 * Are the requirements for this plugin met?
	 *
	 * @return Empty string if ok, else a reason for failure
	 */
	public String checkRequirements() {
		if (metaData == null) { return ""; }
		// Check minimum version
		try {
			final int version = Integer.parseInt(getMinVersion());
			if (version == -1) { return ""; }
			if (Main.RELEASE_DATE > 0 && version > 0) {
				if (Main.RELEASE_DATE < version) {
					return "Plugin is for a newer version of DMDirc";
				}
			}
		} catch (NumberFormatException nfe) {
			return "'minversion' is a non-integer";
		}
		
		// Check maximum version (if found)
		try {
			if (!getMaxVersion().isEmpty()) {
				final int version = Integer.parseInt(getMaxVersion());
				if (Main.RELEASE_DATE > 0 && version > 0) {
					if (Main.RELEASE_DATE > version) {
						return "Plugin is for an older version of DMDirc";
					}
				}
			}
		} catch (NumberFormatException nfe) {
			return "'maxversion' is a non-integer";
		}
		
		// Now check other requirements
		
		// Required OS
		final String requiredOS = getMetaInfo(new String[]{"required-os", "require-os"});
		if (!requiredOS.isEmpty()) {
			final String[] data = requiredOS.split(":");
			if (!System.getProperty("os.name").toLowerCase().matches(data[0])) {
				return "Incorrect OS. (Wanted: '"+data[0]+"', Actual: '"+System.getProperty("os.name")+"')";
			} else {
				if (data.length > 1) {
					if (!System.getProperty("os.version").toLowerCase().matches(data[1])) {
						return "Incorrect OS Version. (Wanted: '"+data[1]+"', Actual: '"+System.getProperty("os.version")+"')";
					} else {
						if (data.length > 2) {
							if (!System.getProperty("os.arch").toLowerCase().matches(data[2])) {
								return "Incorrect OS Arch. (Wanted: '"+data[2]+"', Actual: '"+System.getProperty("os.arch")+"')";
							}
						}
					}
				}
			}
		}
		
		// Required Files
		final String requiredFiles = getMetaInfo(new String[]{"required-files", "require-files", "required-file", "require-file"});
		if (!requiredFiles.isEmpty()) {
			for (String files : requiredFiles.split(",")) {
				final String[] filelist = files.split("\\|");
				boolean foundFile = false;
				for (String file : filelist) {
					if ((new File(file)).exists()) {
						foundFile = true;
						break;
					}
				}
				if (!foundFile) {
					return "Required file '"+files+"' not found";
				}
			}
		}
		
		// Required Plugins
		final String requiredPlugins = getMetaInfo(new String[]{"required-plugins", "require-plugins", "required-plugin", "require-plugin"});
		if (!requiredPlugins.isEmpty()) {
			for (String plugin : requiredPlugins.split(",")) {
				final String[] data = plugin.split(":");
				final PluginInfo pi = PluginManager.getPluginManager().getPluginInfoByName(data[0]);
				if (pi == null) {
					return "Required plugin '"+data[0]+"' was not found";
				} else {
					if (data.length > 1) {
						// Check plugin minimum version matches.
						try {
							final int minversion = Integer.parseInt(data[1]);
							if (pi.getVersion() < minversion) {
								return "Plugin '"+data[0]+"' is too old (Required Version: "+minversion+", Actual Version: "+pi.getVersion()+")";
							} else {
								if (data.length > 2) {
									// Check plugin maximum version matches.
									try {
										final int maxversion = Integer.parseInt(data[2]);
										if (pi.getVersion() > maxversion) {
											return "Plugin '"+data[0]+"' is too new (Required Version: "+maxversion+", Actual Version: "+pi.getVersion()+")";
										}
									} catch (NumberFormatException nfe) {
										return "Plugin max-version '"+data[2]+"' for plugin ('"+data[0]+"') is a non-integer";
									}
								}
							}
						} catch (NumberFormatException nfe) {
							return "Plugin min-version '"+data[1]+"' for plugin ('"+data[0]+"') is a non-integer";
						}
					}
					// Make sure the required plugin is loaded if its not already,
					pi.loadPlugin();
				}
			}
		}
		
		// All requirements passed, woo \o
		return "";
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
			classloader = new PluginClassLoader(this);

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
			Logger.userError(ErrorLevel.LOW, "Class not found ('"+filename+":"+getMainClass()+"')", cnfe);
		} catch (NoSuchMethodException nsme) {
			Logger.userError(ErrorLevel.LOW, "Constructor missing ('"+filename+":"+getMainClass()+"')", nsme);
		} catch (IllegalAccessException iae) {
			Logger.userError(ErrorLevel.LOW, "Unable to access constructor ('"+filename+":"+getMainClass()+"')", iae);
		} catch (InvocationTargetException ite) {
			Logger.userError(ErrorLevel.LOW, "Unable to invoke target ('"+filename+":"+getMainClass()+"')", ite);
		} catch (InstantiationException ie) {
			Logger.userError(ErrorLevel.LOW, "Unable to instantiate plugin ('"+filename+":"+getMainClass()+"')", ie);
		} catch (NoClassDefFoundError ncdf) {
			Logger.userError(ErrorLevel.LOW, "Unable to instantiate plugin ('"+filename+":"+getMainClass()+"'): Unable to find class: " + ncdf.getMessage(), ncdf);
		} catch (VerifyError ve) {
			Logger.userError(ErrorLevel.LOW, "Unable to instantiate plugin ('"+filename+":"+getMainClass()+"') - Incompatible", ve);
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
					String classname = filename.replace('.', '/');
					if (classname.matches("^.*\\.class$")) {
						classname = classname.replaceAll("\\.class$", "");
						result.add(classname);
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
	 * @param metainfo[] The metainfos to look for in order. If the first item in
	 *                   the array is not found, the next will be looked for, and
	 *                   so on until either one is found, or none are found.
	 * @return Misc Meta Info (or "" if none are found);
	 */
	public String getMetaInfo(final String[] metainfo) { return getMetaInfo(metainfo,""); }
	
	/**
	 * Get misc meta-information.
	 *
	 * @param metainfo[] The metainfos to look for in order. If the first item in
	 *                   the array is not found, the next will be looked for, and
	 *                   so on until either one is found, or none are found.
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
	public int compareTo(PluginInfo o) {
		return toString().compareTo(o.toString());
	}
}
