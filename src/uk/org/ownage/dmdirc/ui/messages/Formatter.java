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
        return String.format(properties.getProperty(messageType), arguments);
    }
    
    /**
     * Returns the default format strings for the client.
     * @return The default format strings
     */
    private static Properties getDefaults() {
        Properties properties = new Properties();
        properties.setProperty("channelMessage", "<%1$s%2$s> %3$s");
        properties.setProperty("channelAction", (char)3 + "6* %1$s%2$s %3$s");
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
