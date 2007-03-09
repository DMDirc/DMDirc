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
        //This is nasty.
        String baseDir = System.getenv("APPDATA");
        if (baseDir == null) {
            baseDir = System.getProperty("user.home");
        }
        //End nasty
        return baseDir + fs + ".DMDirc" + fs;
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
        defaults.setProperty("general.globaldisconnectmessage", "true");
        defaults.setProperty("general.sendinfomessagestoactive", "true");
        
        // These are temporary until we get identity support
        defaults.setProperty("general.defaultnick", "DMDircUser");
        defaults.setProperty("general.alternatenick", "DMDircUser_");
        defaults.setProperty("general.server", "blueyonder.uk.quakenet.org");
        defaults.setProperty("general.port", "7000");
        defaults.setProperty("general.password", "");
        
        defaults.setProperty("ui.backgroundcolour", "0");
        defaults.setProperty("ui.foregroundcolour", "1");
        defaults.setProperty("ui.maximisewindows", "false");
        defaults.setProperty("ui.sortByMode", "true");
        defaults.setProperty("ui.sortByCase", "false");
        defaults.setProperty("ui.inputbuffersize", "50");
        defaults.setProperty("ui.showversion", "true");
        defaults.setProperty("ui.rolloverEnabled", "true");
        defaults.setProperty("ui.rolloverColour", "f0f0f0");
        
        defaults.setProperty("channel.splitusermodes", "false");
        
        defaults.setProperty("server.modec", "No formatting");
        defaults.setProperty("server.modeC", "No channel CTCPs");
        defaults.setProperty("server.modeD", "Auditorium mode");
        defaults.setProperty("server.modei", "Invite only");
        defaults.setProperty("server.modem", "Moderated");
        defaults.setProperty("server.moden", "No external messages");
        defaults.setProperty("server.modeN", "No channel notices");
        defaults.setProperty("server.modek", "Channel key");
        defaults.setProperty("server.model", "User limit");
        defaults.setProperty("server.modep", "Private");
        defaults.setProperty("server.moder", "Registered users only");
        defaults.setProperty("server.modes", "Secret");
        defaults.setProperty("server.modet", "Only ops can set topic");
        defaults.setProperty("server.modeu", "No quit messages");
        
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
                Logger.error(ErrorLevel.INFO, ex);
            } catch (FileNotFoundException ex) {
                Logger.log(LogLevel.CORE, "No config file, using defaults");
            } catch (IOException ex) {
                Logger.error(ErrorLevel.WARNING, ex);
            }
        } else {
            try {
                (new File(getConfigDir())).mkdirs();
                file.createNewFile();
                Config.save();
            } catch (IOException ex) {
                Logger.error(ErrorLevel.WARNING, ex);
            }
        }
    }
    
    
    /**
     * Saves the config file to disc.
     */
    public static void save() {
        if (properties == null) {
            initialise();
        }
        try {
            properties.storeToXML(new FileOutputStream(
                    new File(getConfigFile())), null);
        } catch (FileNotFoundException ex) {
            Logger.error(ErrorLevel.INFO, ex);
        } catch (IOException ex) {
            Logger.error(ErrorLevel.WARNING, ex);
        }
    }
    
}
