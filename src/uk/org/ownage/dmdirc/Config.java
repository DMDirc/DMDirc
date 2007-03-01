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

package uk.org.ownage.dmdirc;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.InvalidPropertiesFormatException;
import java.util.Properties;

/**
 * Reads/writes the application's config file
 * @author chris
 */
public class Config {
    
    /**
     * The application's current configuration
     */
    private static Properties properties;
    
    /** Disallow creation of a new instance of Config */
    private Config() {
    }
    
    /**
     * Returns the singleton instance of ServerManager
     * @return Instance of ServerManager
     */
    public static Properties getConfig() {
	if (properties == null) {
	    initialise();
	}
	return properties;
    }
    
    /**
     * Returns the full path to the application's config file
     * @return config file
     */
    private static String getConfigFile() {
	return getConfigDir()+"dmdirc.xml";
    }
    
    /**
     * Returns the application's config directory
     * @return configuration directory
     */
    public static String getConfigDir() {
	String fs = System.getProperty("file.separator");
	return System.getProperty("user.home")+fs+".DMDirc"+fs;
    }
    
    /**
     * Returns the default settings for DMDirc
     * @return default settings
     */
    private static Properties getDefaults() {
	Properties defaults = new Properties();
	
	defaults.setProperty("general.commandchar","/");
        defaults.setProperty("general.quitmessage","Using DMDirc");
        defaults.setProperty("general.partmessage","Using DMDirc");
	defaults.setProperty("ui.maximisewindows","true");
        defaults.setProperty("ui.sortByMode", "true");
        defaults.setProperty("ui.sortByCase", "false");
        defaults.setProperty("logging.dateFormat","EEE, d MMM yyyy HH:mm:ss Z");
	
	return defaults;
    }
    
    /**
     * Determines if the specified option exists
     * @return true iff the option exists, false otherwise
     * @param domain the domain of the option
     * @param option the name of the option
     */
    public static boolean hasOption(String domain, String option) {
	if (properties == null) {
	    initialise();
	}
	
	return (properties.getProperty(domain+"."+option) != null);
    }
    
    /**
     * Returns the specified option
     * @return the value of the specified option
     * @param domain the domain of the option
     * @param option the name of the option
     */
    public static String getOption(String domain, String option) {
	if (properties == null) {
	    initialise();
	}
	
	return properties.getProperty(domain+"."+option);
    }
    
    /**
     * Sets a specified option
     * @param domain domain of the option
     * @param option name of the option
     * @param value value of the option
     */
    public static void setOption(String domain, String option, String value) {
	if (properties == null) {
	    initialise();
	}
	
	properties.setProperty(domain+"."+option, value);
    }
    
    /**
     * Loads the config file from disc, if it exists else initialises defaults
     * and creates file
     */
    private static void initialise() {
	
	properties = getDefaults();
	
	File file = new File(getConfigFile());
	
	if (file.exists()) {
	    try {
		properties.loadFromXML(new FileInputStream(file));
	    } catch (InvalidPropertiesFormatException ex) {
		//Do nothing, defaults used
	    } catch (FileNotFoundException ex) {
		//Do nothing, defaults used
	    } catch (IOException ex) {
		//Do nothing, defaults used
	    }
	} else {
	    try {
		(new File(getConfigDir())).mkdirs();
		file.createNewFile();
		Config.save();
	    } catch (IOException ex) {
		//TODO Alert user, use defaults, saving will fail
		ex.printStackTrace();
	    }
	}
    }
    
    
    /**
     * Saves the config file to disc
     */
    public static void save() {
	if (properties == null) {
	    initialise();
	}
	try {
	    properties.storeToXML(new FileOutputStream(new File(getConfigFile())), null);
	} catch (FileNotFoundException ex) {
	    //Do nothing, shouldnt be able to happen
	} catch (IOException ex) {
	    //Do nothing
	}
    }
    
}
