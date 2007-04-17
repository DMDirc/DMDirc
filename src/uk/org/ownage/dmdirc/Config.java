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
import java.util.Enumeration;
import java.util.InvalidPropertiesFormatException;
import java.util.Properties;

import javax.swing.UIManager;

import uk.org.ownage.dmdirc.identities.IdentityManager;
import uk.org.ownage.dmdirc.logger.ErrorLevel;
import uk.org.ownage.dmdirc.logger.LogLevel;
import uk.org.ownage.dmdirc.logger.Logger;

/**
 * Reads/writes the application's config file.
 * @author chris
 */
public final class Config {
    
    /**
     * The application's current configuration.
     */
    private static Properties properties;
    
    /** Disallow creation of a new instance of Config. */
    private Config() {
    }
    
    /**
     * Returns the singleton instance of ServerManager.
     * @return Instance of ServerManager
     */
    public static Properties getConfig() {
        if (properties == null) {
            initialise();
        }
        return properties;
    }
    
    /**
     * Returns the full path to the application's config file.
     * @return config file
     */
    private static String getConfigFile() {
        return getConfigDir() + "dmdirc.xml";
    }
    
    /**
     * Returns the application's config directory.
     * @return configuration directory
     */
    public static String getConfigDir() {
        final String fs = System.getProperty("file.separator");
        final String osName = System.getProperty("os.name");
        String configDir = null;
        if (osName.startsWith("Mac OS")) {
            configDir = System.getProperty("user.home") + "/Library/Preferences/DMDirc/";
        } else if (osName.startsWith("Windows")) {
            if (System.getenv("APPDATA") == null) {
                configDir = System.getProperty("user.home") + fs + "DMDirc" + fs;
            } else {
                configDir = System.getenv("APPDATA") + fs + "DMDirc" + fs;
            }
        } else {
            configDir = System.getProperty("user.home") + fs + ".DMDirc" + fs;
        }
        return configDir;
    }
    
    /**
     * Returns the default settings for DMDirc.
     * @return default settings
     */
    private static Properties getDefaults() {
        final Properties defaults = new Properties();
        
        defaults.setProperty("general.commandchar", "/");
        defaults.setProperty("general.closemessage", "DMDirc exiting");
        defaults.setProperty("general.quitmessage", "Using DMDirc");
        defaults.setProperty("general.partmessage", "Using DMDirc");
        defaults.setProperty("general.cyclemessage", "Cycling");
        defaults.setProperty("general.kickmessage", "Bye!");
        
        defaults.setProperty("general.closechannelsonquit", "false");
        defaults.setProperty("general.closequeriesonquit", "false");
        
        defaults.setProperty("general.closechannelsondisconnect", "false");
        defaults.setProperty("general.closequeriesondisconnect", "false");        
        
        // These are temporary until we get server list support
        defaults.setProperty("general.server", "blueyonder.uk.quakenet.org");
        defaults.setProperty("general.port", "7000");
        defaults.setProperty("general.password", "");
        
        // These control where notifications will go. Expected values are
        // "active", "all", or "server".
        // TODO: Some kind of validation in the config class itself, rather
        //       than elsewhere?
        defaults.setProperty("notifications.socketClosed", "all");
        defaults.setProperty("notifications.privateNotice", "all");
        defaults.setProperty("notifications.privateCTCP", "server");
        defaults.setProperty("notifications.privateCTCPreply", "server");
        
        // Send whois info to active window by default
        defaults.setProperty("notifications.numeric_301", "active");
        defaults.setProperty("notifications.numeric_311", "active");
        defaults.setProperty("notifications.numeric_312", "active");
        defaults.setProperty("notifications.numeric_313", "active");
        defaults.setProperty("notifications.numeric_318", "active");
        defaults.setProperty("notifications.numeric_319", "active");
        defaults.setProperty("notifications.numeric_330", "active");
        
        defaults.setProperty("ui.backgroundcolour", "0");
        defaults.setProperty("ui.foregroundcolour", "1");
        defaults.setProperty("ui.maximisewindows", "false");
        defaults.setProperty("ui.sortByMode", "true");
        defaults.setProperty("ui.sortByCase", "false");
        defaults.setProperty("ui.inputbuffersize", "50");
        defaults.setProperty("ui.showversion", "true");
        defaults.setProperty("ui.lookandfeel", UIManager.getCrossPlatformLookAndFeelClassName());
        defaults.setProperty("ui.quickCopy", "false");
        defaults.setProperty("ui.pasteProtectionLimit", "1");
        
        // TODO: These should probably be renamed to treeview.* or so?
        defaults.setProperty("ui.rolloverEnabled", "true");
        defaults.setProperty("ui.rolloverColour", "f0f0f0");
        defaults.setProperty("ui.sortwindows", "true");
        defaults.setProperty("ui.sortservers", "true");
        
        defaults.setProperty("channel.splitusermodes", "false");
        
        defaults.setProperty("tabcompletion.casesensitive", "false");
        
        defaults.setProperty("logging.dateFormat", "EEE, d MMM yyyy HH:mm:ss Z");
        defaults.setProperty("logging.programLogging", "true");
        defaults.setProperty("logging.debugLogging", "true");
        defaults.setProperty("logging.debugLoggingSysOut", "true");
        
        
        return defaults;
    }
    
    /**
     * Determines if the specified option exists.
     * @return true iff the option exists, false otherwise
     * @param domain the domain of the option
     * @param option the name of the option
     */
    public static boolean hasOption(final String domain, final String option) {
        if (properties == null) {
            initialise();
        }
        
        return properties.getProperty(domain + "." + option) != null;
    }
    
    /**
     * Returns the specified option.
     * @return the value of the specified option
     * @param domain the domain of the option
     * @param option the name of the option
     */
    public static String getOption(final String domain, final String option) {
        if (properties == null) {
            initialise();
        }
        
        return properties.getProperty(domain + "." + option);
    }
    
    /**
     * Sets a specified option.
     * @param domain domain of the option
     * @param option name of the option
     * @param value value of the option
     */
    public static void setOption(final String domain, final String option,
            final String value) {
        if (properties == null) {
            initialise();
        }
        
        properties.setProperty(domain + "." + option, value);
    }
    
    /**
     * Unsets a specified option.
     * @param domain domain of the option
     * @param option name of the option
     */
    public static void unsetOption(final String domain, final String option) {
        if (properties == null) {
            initialise();
        }
        
        properties.remove(domain + "." + option);
    }
    
    /**
     * Loads the config file from disc, if it exists else initialises defaults
     * and creates file.
     */
    private static void initialise() {
        
        properties = getDefaults();
        
        final File file = new File(getConfigFile());
        
        if (file.exists()) {
            try {
                properties.loadFromXML(new FileInputStream(file));
            } catch (InvalidPropertiesFormatException ex) {
                Logger.error(ErrorLevel.TRIVIAL, "Invalid properties file", ex);
            } catch (FileNotFoundException ex) {
                Logger.log(LogLevel.CORE, "No config file, using defaults");
            } catch (IOException ex) {
                Logger.error(ErrorLevel.WARNING, "Unable to load config file", ex);
            }
        } else {
            try {
                (new File(getConfigDir())).mkdirs();
                file.createNewFile();
                Config.save();
            } catch (IOException ex) {
                Logger.error(ErrorLevel.WARNING, "Unable to load config file", ex);
            }
        }
    }
    
    
    /**
     * Saves the config file to disc.
     */
    public static void save() {
        IdentityManager.save();
        
        if (properties == null) {
            return;
        }
        
        final Properties defaults = getDefaults();
        final Properties output = new Properties();
        
        final Enumeration<Object> keys = properties.keys();
        
        while (keys.hasMoreElements()) {
            final String key = (String) keys.nextElement();
            if (!defaults.containsKey(key) || !defaults.getProperty(key).equals(properties.getProperty(key))) {
                output.setProperty(key, properties.getProperty(key));
            }
        }
        
        try {
            output.storeToXML(new FileOutputStream(
                    new File(getConfigFile())), null);
        } catch (FileNotFoundException ex) {
            Logger.error(ErrorLevel.TRIVIAL, "Unable to save config file", ex);
        } catch (IOException ex) {
            Logger.error(ErrorLevel.WARNING, "Unable to save config file", ex);
        }
    }
    
}
