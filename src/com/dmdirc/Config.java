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

package com.dmdirc;

import com.dmdirc.config.ConfigManager;
import com.dmdirc.config.IdentityManager;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

/**
 * Reads/writes the application's config file.
 * @author chris
 */
public final class Config {
    
    /** The config manager used for the global config. */
    private static ConfigManager manager;
    
    /** Disallow creation of a new instance of Config. */
    private Config() {
    }
    
    /** Initialises the config manager. */
    public static void init() {
        manager = new ConfigManager("", "", "");
    }
        
    /**
     * Determines if the specified option exists.
     * @return true iff the option exists, false otherwise
     * @param domain the domain of the option
     * @param option the name of the option
     */
    public static boolean hasOption(final String domain, final String option) {
        return manager.hasOption(domain, option);
    }
    
    /**
     * Returns the application's command character.
     * @return The command character (general.commandchar)
     */
    public static String getCommandChar() {
        return getOption("general", "commandchar", "/");
    }
    
    /**
     * Returns the specified option.
     * @return the value of the specified option
     * @param domain the domain of the option
     * @param option the name of the option
     */
    public static String getOption(final String domain, final String option) {
        return manager.getOption(domain, option, "");
    }
    
    /**
     * Returns the specified option.
     * @return the value of the specified option
     * @param domain the domain of the option
     * @param option the name of the option
     * @param fallback the balue to be returned if the option is not found
     */
    public static String getOption(final String domain, final String option, final String fallback) {
        return manager.getOption(domain, option, fallback);
    }
    
    /**
     * Returns the specified option parsed as a boolean.
     * @return the boolean value of the specified option
     * @param domain the domain of the option
     * @param option the name of the option
     */
    public static boolean getOptionBool(final String domain, final String option) {
        return manager.getOptionBool(domain, option);
    }
    
    /**
     * Returns the specified option parsed as an integer.
     * @return the integer value of the specified option
     * @param domain the domain of the option
     * @param option the name of the option
     * @param fallback The value to use if the option can't be parsed
     */
    public static int getOptionInt(final String domain, final String option, final int fallback) {
        return manager.getOptionInt(domain, option, fallback);
    }
    
    /**
     * Returns the specified option parsed as a colour.
     * @return the colour object representing the specified option
     * @param domain the domain of the option
     * @param option the name of the option
     * @param fallback The value to use if the colour can't be parsed
     */
    public static Color getOptionColor(final String domain, final String option, final Color fallback) {
        return manager.getOptionColour(domain, option, fallback);
    }
    
    /**
     * Returns the name of all the options in the specified domain.
     * @param domain The domain to search
     * @return A list of options in the specified domain
     */
    public static List<String> getOptions(final String domain) {
        return manager.getOptions(domain);
    }
    
    /**
     * Returns the name of all domains in the global config.
     * @return A list of domains in this config
     */
    public static List<String> getDomains() {
        final ArrayList<String> res = new ArrayList<String>();
        String domain;
        
        for (String key : manager.getOptions()) {
            domain = key.substring(0, key.indexOf('.'));
            if (!res.contains(domain)) {
                res.add(domain);
            }
        }
        
        return res;
    }
    
    /**
     * Sets a specified option.
     * @param domain domain of the option
     * @param option name of the option
     * @param value value of the option
     */
    public static void setOption(final String domain, final String option,
            final String value) {
        IdentityManager.getConfigIdentity().setOption(domain, option, value);
    }
    
    /**
     * Unsets a specified option.
     * @param domain domain of the option
     * @param option name of the option
     */
    public static void unsetOption(final String domain, final String option) {
        IdentityManager.getConfigIdentity().unsetOption(domain, option);
    }
    
}
