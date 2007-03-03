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

package uk.org.ownage.dmdirc.ui.messages;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.InvalidPropertiesFormatException;
import java.util.Properties;
import uk.org.ownage.dmdirc.Config;
import uk.org.ownage.dmdirc.logger.ErrorLevel;
import uk.org.ownage.dmdirc.logger.Logger;

/**
 * The Formatter provides a standard way to format messages for display.
 */
public class Formatter {
    
    /**
     * The format strings used by the formatter
     */
    private static Properties properties;
    
    /**
     * Creates a new instance of Formatter
     */
    private Formatter() {
    }
    
    /**
     * Inserts the supplied arguments into a format string for the specified
     * message type.
     * @param messageType The message type that the arguments should be formatted as
     * @param arguments The arguments to this message type
     * @return A formatted string
     */
    public static String formatMessage(String messageType, Object... arguments) {
        if (properties == null) initialise();
        
        if (properties.containsKey(messageType)) {
                return String.format(properties.getProperty(messageType), arguments);
        } else {
            Logger.error(ErrorLevel.ERROR, "Format string not found: "+messageType);
            return "<No format string for message type "+messageType+">";
        }
    }
    
    /**
     * Returns the default format strings for the client.
     * @return The default format strings
     */
    private static Properties getDefaults() {
        Properties properties = new Properties();
        
        char colour = 3;
        char stop = 15;
        
        properties.setProperty("channelMessage", "<%1$s%2$s> %3$s");
        properties.setProperty("channelAction", colour+"6* %1$s%2$s %3$s");
        
        properties.setProperty("channelJoin", colour+"3* %2$s has joined %3$s.");
        properties.setProperty("channelPart", colour+"3* %1$s%2$s has left %3$s.");
        properties.setProperty("channelPartReason", colour+"3* %1$s%2$s has left %3$s (%4$s"+stop+").");
        properties.setProperty("channelQuit", colour+"2* %1$s%2$s has quit IRC.");
        properties.setProperty("channelQuitReason", colour+"2* %1$s%2$s has quit IRC (%3$s"+stop+").");        
        
        properties.setProperty("channelKick", colour+"3* %1$s%2$s has kicked %3$s%4$s from %5$s.");
        properties.setProperty("channelKickReason", colour+"3* %1$s%2$s has kicked %3$s%4$s from %5$s (%6$s"+stop+").");
        
        properties.setProperty("channelSelfMessage", "<%1$s%2$s> %3$s");
        properties.setProperty("channelSelfAction", colour+"6* %1$s%2$s %3$s");
        
        properties.setProperty("queryMessage", "<%1$s> %2$s");
        properties.setProperty("queryAction", colour+"6* %1$s %2$s");
        
        properties.setProperty("querySelfMessage", "<%1$s> %2$s");
        properties.setProperty("querySelfAction", colour+"6* %1$s %2$s");
        
        return properties;
    }
    
    /**
     * Reads the format strings from disk (if available), and initialises the
     * properties object.
     */
    private static void initialise() {
        if (properties == null) {
            File file;
            if (Config.hasOption("ui", "formatter")) {
                file = new File(Config.getOption("ui", "formatter"));
            } else {
                file = new File(Config.getConfigDir()+"format.properties");
            }
            properties = getDefaults();
            if (file.exists()) {
                try {
                    properties.load(new FileInputStream(file));
                } catch (FileNotFoundException ex) {
                    //Do nothing, defaults used
                } catch (InvalidPropertiesFormatException ex) {
                    Logger.error(ErrorLevel.INFO, ex);
                } catch (IOException ex) {
                    Logger.error(ErrorLevel.WARNING, ex);
                }
            } else {
                try {
                    file.createNewFile();
                } catch (IOException ex) {
                    Logger.error(ErrorLevel.WARNING, ex);
                }
            }
        }
    }
}
