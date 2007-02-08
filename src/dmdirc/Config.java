/*
 * Copyright (c) 2006-2007 Chris Smith, Shane Mc Cormack
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

package dmdirc;

import java.io.File;
import java.util.Properties;

/**
 * Reads/writes the application's config file
 * @author chris
 */
public class Config {
    
    private static Properties properties;
    
    /** Creates a new instance of Config */
    private Config() {
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
    private static String getConfigDir() {
        String fs = System.getProperty("file.seperator");
        return System.getProperty("user.home")+fs+".DMDirc"+fs;
    }
    
    private static Properties getDefaults() {
        Properties defaults = new Properties();
        
        defaults.setProperty("general.commandchar","/");
        defaults.setProperty("ui.maximisewindows","true");
        
        return defaults;
    }
    
    public static boolean hasOption(String domain, String option) {
        assert(properties != null);
        
        return (properties.getProperty(domain+"."+option) != null);
    }
    
    public static String getOption(String domain, String option) {
        assert(properties != null);
        
        return properties.getProperty(domain+"."+option);
    }
    
    /**
     * Loads the config file from disc, if it exists
     */
    public static void initialise() {
        File dir = new File(getConfigFile());
        dir.mkdirs();
        
        properties = new Properties(getDefaults());
        
        if (dir.exists()) {
            // read file
        }
    }
    
}
