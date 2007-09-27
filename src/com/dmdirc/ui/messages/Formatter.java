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

package com.dmdirc.ui.messages;

import com.dmdirc.Config;
import com.dmdirc.Main;
import com.dmdirc.logger.ErrorLevel;
import com.dmdirc.logger.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.IllegalFormatConversionException;
import java.util.InvalidPropertiesFormatException;
import java.util.Map;
import java.util.MissingFormatArgumentException;
import java.util.Properties;
import java.util.Set;
import java.util.UnknownFormatConversionException;

/**
 * The Formatter provides a standard way to format messages for display.
 */
public final class Formatter {
    
    /**
     * The format strings used by the formatter.
     */
    private static Properties properties;
    
    /**
     * The default properties we fall back to if the user hasn't defined their
     * own.
     */
    private static Properties defaultProperties;
    
    /**
     * A cache of types needed by the various formatters.
     */
    private static Map<String, Character[]> typeCache;
    
    /**
     * Creates a new instance of Formatter.
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
    public static String formatMessage(final String messageType,
            final Object... arguments) {
        if (properties == null) {
            initialise();
        }
        
        final String res = properties.getProperty(messageType);
        
        if (res == null) {
            return "<No format string for message type " + messageType + ">";
        } else {
            try {
                final Object[] newArgs = castArguments(res, arguments);
                return String.format(res, newArgs);
            } catch (IllegalFormatConversionException ex) {
                return "<Invalid format string for message type " + messageType
                        + "; Error: Illegal format conversion: " + ex.getMessage() + ">";
            } catch (UnknownFormatConversionException ex) {
                return "<Invalid format string for message type " + messageType
                        + "; Error: Unknown format conversion: " + ex.getMessage() + ">";
            } catch (MissingFormatArgumentException ex) {
                return "<Invalid format string for message type " + messageType
                        + "; Error: Missing format argument: " + ex.getMessage() + ">";
            }
        }
    }
    
    /**
     * Casts the specified arguments to the relevant classes, based on the
     * format type cache.
     *
     * @param format The format to be used
     * @param args The arguments to be casted
     * @return A new set of arguments of appropriate types
     */
    private static Object[] castArguments(final String format, final Object[] args) {
        if (!typeCache.containsKey(format)) {
            analyseFormat(format, args);
        }
        
        final Object[] res = new Object[args.length];
        
        int i = 0;
        for (Character chr : typeCache.get(format)) {
            switch (chr) {
            case 'b': case 'B': case 'h': case 'H': case 's': case 'S':
                // General (strings)
                res[i] = args[i].toString();
                break;
            case 'c': case 'C':
                // Character
                res[i] = ((String) args[i]).charAt(0);
                break;
            case 'd': case 'o': case 'x': case 'X':
                // Integers
                res[i] = Integer.valueOf((String) args[i]);
                break;
            case 'e': case 'E': case 'f': case 'g': case 'G': case 'a': case 'A':
                // Floating point
                res[i] = Float.valueOf((String) args[i]);
                break;
            case 't': case 'T':
                // Date
                if (args[i] instanceof String) {
                    // Assume it's a timestamp(?)
                    res[i] = Long.valueOf(1000 * Long.valueOf((String) args[i]));
                } else {
                    res[i] = args[i];
                }
                break;
            default:
                res[i] = args[i];
            }
            
            i++;
        }
        
        return res;
    }
    
    /**
     * Analyses the specified format string and fills in the format type cache.
     *
     * @param format The format to analyse
     * @param args The raw arguments
     */
    private static void analyseFormat(final String format, final Object[] args) {
        final Character[] types = new Character[args.length];
        
        for (int i = 0; i < args.length; i++) {
            final int index = format.indexOf("%" + (i + 1) + "$");
            
            if (index > -1) {
                types[i] = format.charAt(index + 3);
            } else {
                types[i] = 's';
            }
        }
        
        typeCache.put(format, types);
    }
    
    /**
     * Returns a list of the available formatters.
     *
     * @return Set of formatters
     */
    public static Set<String> getFormats() {
        if (properties == null) {
            initialise();
        }
        
        return properties.stringPropertyNames();
    }
    /**
     * Determines whether the formatter knows of a specific message type.
     * @param messageType the message type to check
     * @return True iff there is a matching format, false otherwise
     */
    public static boolean hasFormat(final String messageType) {
        if (properties == null) {
            initialise();
        }
        
        return properties.getProperty(messageType) != null;
    }
    
    /**
     * Returns the default format strings for the client.
     */
    private static void loadDefaults() {
        defaultProperties = new Properties();
        
        final char colour = Styliser.CODE_COLOUR;
        final char stop = Styliser.CODE_STOP;
        final char fixed = Styliser.CODE_FIXED;
        
        // Type: Timestamp
        //    1: Current timestamp
        defaultProperties.setProperty("timestamp", "%1$tH:%1$tM:%1$tS | ");
        
        // Type: Channel Message
        //    1: User mode prefixes
        //    2: User nickname
        //    3: User ident
        //    4: User host
        //    5: Message content
        //    6: Channel name
        defaultProperties.setProperty("channelMessage", "<%1$s%2$s> %5$s");
        defaultProperties.setProperty("channelHighlight", colour + "4<%1$s%2$s> %5$s");
        defaultProperties.setProperty("channelAction", colour + "6* %1$s%2$s %5$s");
        defaultProperties.setProperty("channelHighlightAction", colour + "6* %1$s%2$s %5$s");
        defaultProperties.setProperty("channelSelfMessage", "<%1$s%2$s> %5$s");
        defaultProperties.setProperty("channelSelfAction", colour + "6* %1$s%2$s %5$s");
        defaultProperties.setProperty("channelSelfExternalMessage", "<%1$s%2$s> %5$s");
        defaultProperties.setProperty("channelSelfExternalAction", colour + "6* %1$s%2$s %5$s");
        
        // Type: Channel CTCP
        //    1: User mode prefixes
        //    2: User nickname
        //    3: User ident
        //    4: User host
        //    5: CTCP type
        //    6: CTCP content
        //    7: Channel name
        defaultProperties.setProperty("channelCTCP", colour + "4-!- CTCP %5$S from %1$s%2$s");
        
        // Type: Channel Event
        //    1: User mode prefixes
        //    2: User nickname
        //    3: User ident
        //    4: User host
        //    5: Channel name
        defaultProperties.setProperty("channelJoin", colour + "3* %2$s (%3$s@%4$s) has joined %5$s" + stop + ".");
        defaultProperties.setProperty("channelPart", colour + "3* %1$s%2$s (%3$s@%4$s) has left %5$s." + stop + "");
        defaultProperties.setProperty("channelQuit", colour + "2* %1$s%2$s (%3$s@%4$s) has quit IRC.");
        defaultProperties.setProperty("channelSelfJoin", colour + "3* You are now talking in %5$s." + stop + "");
        defaultProperties.setProperty("channelSelfPart", colour + "3* You have left the channel.");
        
        // Type: Channel Event with content
        //    1: User mode prefixes
        //    2: User nickname
        //    3: User ident
        //    4: User host
        //    5: Channel name
        //    6: Content
        defaultProperties.setProperty("channelPartReason", colour + "3* %1$s%2$s (%3$s@%4$s) has left %5$s (%6$s" + stop + ").");
        defaultProperties.setProperty("channelQuitReason", colour + "2* %1$s%2$s (%3$s@%4$s) has quit IRC (%6$s" + stop + ").");
        defaultProperties.setProperty("channelTopicChange", colour + "3* %1$s%2$s has changed the topic to '%6$s" + stop + "'.");
        defaultProperties.setProperty("channelNickChange", colour + "3* %1$s%2$s is now know as %6$s" + stop + ".");
        defaultProperties.setProperty("channelModeChange", colour + "3* %1$s%2$s sets mode: %6$s" + stop + ".");
        defaultProperties.setProperty("channelSelfNickChange", colour + "3* You are now know as %6$s" + stop + ".");
        defaultProperties.setProperty("channelSelfModeChange", colour + "3* You set mode: %6$s" + stop + ".");
        defaultProperties.setProperty("channelSelfPartReason", colour + "3* You have left the channel.");
        
        // Type: Binary Channel Event
        //    1: Source user mode prefixes
        //    2: Source user nickname
        //    3: Source user ident
        //    4: Source user host
        //    5: Target user mode prefixes
        //    6: Target user nickname
        //    7: Target user ident
        //    8: Target user host
        //    9: Channel name
        defaultProperties.setProperty("channelKick", colour + "3* %1$s%2$s has kicked %5$s%6$s from %9$s" + stop + ".");
        
        // Type: Binary Channel Event with content
        //    1: Source user mode prefixes
        //    2: Source user nickname
        //    3: Source user ident
        //    4: Source user host
        //    5: Target user mode prefixes
        //    6: Target user nickname
        //    7: Target user ident
        //    8: Target user host
        //    9: Channel name
        //   10: Content
        defaultProperties.setProperty("channelKickReason", colour + "3* %1$s%2$s has kicked %5$s%6$s from %9$s (%10$s" + stop + ").");
        defaultProperties.setProperty("channelUserMode_default", colour + "3* %1$s%2$s sets mode %10$s on %6$s" + stop + ".");
        
        // Type: Channel topic sync
        //    1: Topic
        //    2: User responsible
        //    3: Time last changed
        //    4: Channel name
        defaultProperties.setProperty("channelJoinTopic", colour + "3* The topic for %4$s is '%1$s" + stop + "'.\n" + colour + "3* Topic was set by %2$s.");
        
        // Type: Channel mode discovery
        //     1: Channel modes
        //     2: Channel name
        defaultProperties.setProperty("channelNoModes", colour + "3* There are no channel modes for %2$s" + stop + ".");
        defaultProperties.setProperty("channelModeDiscovered", colour + "3* Channel modes for %2$s are: %1$s" + stop + ".");
        
        // Type: Private CTCP
        //    1: User nickname
        //    2: User ident
        //    3: User host
        //    4: CTCP type
        //    5: CTCP content
        defaultProperties.setProperty("privateCTCP", colour + "4-!- CTCP %4$S from %1$s");
        defaultProperties.setProperty("privateCTCPreply", colour + "4-!- CTCP %4$S reply from %1$s: %5$s");
        
        // Type: Private communications
        //    1: User nickname
        //    2: User ident
        //    3: User host
        //    4: Message content
        defaultProperties.setProperty("privateNotice", colour + "5-%1$s- %4$s");
        defaultProperties.setProperty("queryMessage", "<%1$s> %4$s");
        defaultProperties.setProperty("queryAction", colour + "6* %1$s %4$s");
        defaultProperties.setProperty("querySelfMessage", "<%1$s> %4$s");
        defaultProperties.setProperty("querySelfAction", colour + "6* %1$s %4$s");
        defaultProperties.setProperty("queryNickChanged", colour + "3* %1$s is now know as %4$s" + stop + ".");
        defaultProperties.setProperty("userModeChanged", colour + "3 %1$s sets user mode: %4$s" + stop + ".");
        defaultProperties.setProperty("queryQuitReason", colour + "2* %1$s has quit IRC (%4$s" + stop + ").");
        defaultProperties.setProperty("queryMessageHighlight", colour + "4<%1$s> %4$s");
        defaultProperties.setProperty("queryActionHighlight", colour + "6* %1$s %4$s");
        
        // Type: Private event
        //    1: User nickname
        //    2: User ident
        //    3: User host
        defaultProperties.setProperty("queryQuit", colour + "2* %1$s has quit IRC.");
        
        // Type: Outgoing message
        //    1: Target
        //    2: Message
        defaultProperties.setProperty("selfCTCP", colour + "4->- [%1$s] %2$s");
        defaultProperties.setProperty("selfNotice", colour + "5>%1$s> %2$s");
        defaultProperties.setProperty("selfMessage", ">[%1$s]> %2$s");
        
        // Type: Miscellaneous server
        //    1: Server name
        //    2: Miscellaneous argument
        defaultProperties.setProperty("connectError", colour + "2Error connecting: %2$s");
        defaultProperties.setProperty("connectRetry", colour + "2Reconnecting in %2$s seconds...");
        defaultProperties.setProperty("serverConnecting", "Connecting to %1$s:%2$s...");
        
        // Type: Miscellaneous
        //    1: Miscellaneous data
        defaultProperties.setProperty("authNotice", colour + "5-AUTH- %1$s");
        defaultProperties.setProperty("channelNoTopic", colour + "3* There is no topic set for %1$s" + stop + ".");
        defaultProperties.setProperty("rawCommand", colour + "10>>> %1$s");
        defaultProperties.setProperty("unknownCommand", colour + "14Unknown command %1$s" + stop + ".");
        defaultProperties.setProperty("socketClosed", colour + "2-!- You have been disconnected from the server.");
        defaultProperties.setProperty("stonedServer", colour + "2-!- Disconnected from a non-responsive server.");
        defaultProperties.setProperty("motdStart", colour + "10%1$s");
        defaultProperties.setProperty("motdLine", colour + "10" + fixed + "%1$s");
        defaultProperties.setProperty("motdEnd", colour + "10%1$s");
        defaultProperties.setProperty("rawIn", "<< %1$s");
        defaultProperties.setProperty("rawOut", ">> %1$s");
        defaultProperties.setProperty("commandOutput", "%1$s");
        defaultProperties.setProperty("commandError", colour + "7%1$s");
        defaultProperties.setProperty("actionTooLong", "Warning: action too long to be sent");
        defaultProperties.setProperty("tabCompletion", colour + "14Multiple possibilities: %1$s");
        
        // Type: Unknown target events
        //    1: Source
        //    2: Target
        //    3: Message
        defaultProperties.setProperty("unknownNotice", colour + "5-[%1$s:%2$s]- %3$s");
        
        // Type: Command usage
        //    1: Command char
        //    2: Command name
        //    3: Arguments
        defaultProperties.setProperty("commandUsage", colour + "7Usage: %1$s%2$s %3$s");
        
        // Type: Numerical data
        defaultProperties.setProperty("numeric_301", "%4$s is away: %5$s");
        defaultProperties.setProperty("numeric_311", "-\n%4$s is %5$s@%6$s (%8$s" + stop + ").");
        defaultProperties.setProperty("numeric_312", "%4$s is connected to %5$s (%6$s" + stop + ").");
        defaultProperties.setProperty("numeric_313", "%4$s %5$s.");
        defaultProperties.setProperty("numeric_317", "%4$s has been idle for %5$s seconds; signed on at %6$TT on %6$TF.");
        defaultProperties.setProperty("numeric_318", "End of WHOIS info for %4$s" + stop + ".\n-");
        defaultProperties.setProperty("numeric_319", "%4$s is on: %5$s");
        defaultProperties.setProperty("numeric_330", "%4$s %6$s %5$s" + stop + ".");
        defaultProperties.setProperty("numeric_343", "%4$s %6$s %5$s" + stop + ".");
        
        defaultProperties.setProperty("numeric_401", "6A7000%4$s" + stop + ": %5$s");
        defaultProperties.setProperty("numeric_421", "6A7000%4$s" + stop + ": %5$s");
        defaultProperties.setProperty("numeric_433", "6A7000%4$s" + stop + ": %5$s");
        defaultProperties.setProperty("numeric_461", "6A7000%4$s" + stop + ": %5$s");
        defaultProperties.setProperty("numeric_471", "6A7000%4$s" + stop + ": %5$s");
        defaultProperties.setProperty("numeric_472", "6A7000%4$s" + stop + ": %5$s");
        defaultProperties.setProperty("numeric_473", "6A7000%4$s" + stop + ": %5$s");
        defaultProperties.setProperty("numeric_474", "6A7000%4$s" + stop + ": %5$s");
        defaultProperties.setProperty("numeric_475", "6A7000%4$s" + stop + ": %5$s");
        defaultProperties.setProperty("numeric_479", "6A7000%4$s" + stop + ": %5$s");
    }
    
    /**
     * Allows plugins (etc) to register new default formats.
     * @param name The name of the format
     * @param format The actual format itself
     */
    public static void registerDefault(final String name, final String format) {
        if (defaultProperties == null) {
            initialise();
        }
        
        typeCache.remove(name);
        defaultProperties.setProperty(name, format);
    }
    
    /**
     * Reads the format strings from disk (if available), and initialises the
     * properties object.
     */
    private static void initialise() {
        typeCache = new HashMap<String, Character[]>();
        
        if (defaultProperties == null) {
            loadDefaults();
        }
        
        properties = new Properties(defaultProperties);
        
        if (Config.hasOption("general", "formatters")) {
            for (String file : Config.getOption("general", "formatters").split("\n")) {
                loadFile(file);
            }
        }
    }
    
    /**
     * Loads the specified file into the formatter.
     *
     * @param file File to be loaded
     * @return True iff the operation succeeeded, false otherwise
     */
    public static boolean loadFile(final String file) {
        final File myFile = new File(Main.getConfigDir() + file);
        if (myFile.exists()) {
            try {
                final FileInputStream in = new FileInputStream(myFile);
                loadFile(in);
                in.close();
            } catch (FileNotFoundException ex) {
                return false;
            } catch (IOException ex) {
                Logger.userError(ErrorLevel.LOW, "unable to load formatter");
                return false;
            }
            
            return true;
        } else {
            return false;
        }
    }
    
    /**
     * Reads the specified input stream as a properties file and loads the
     * contained formatter settings into the formatter.
     *
     * @param stream The stream to be read
     * @return True iff the operation succeeded, false otherwise
     */
    public static boolean loadFile(final InputStream stream) {
        try {
            properties.load(stream);
            typeCache.clear();
        } catch (InvalidPropertiesFormatException ex) {
            Logger.userError(ErrorLevel.LOW, "Unable to load formatter");
            return false;
        } catch (IOException ex) {
            Logger.userError(ErrorLevel.LOW, "Unable to load formatter");
            return false;
        }
        
        return true;
    }
    
    /**
     * Saves the current formatter into the specified file.
     * @param target The target file
     * @return True iff the operation succeeded, false otherwise
     */
    public static boolean saveAs(final String target) {
        if (properties == null) {
            initialise();
        }
        
        final File myFile = new File(Main.getConfigDir() + target);
        FileOutputStream file = null;
        
        try {
            file = new FileOutputStream(myFile);
            properties.store(file, null);
            defaultProperties.store(file, null);
        } catch (IOException ex) {
            Logger.userError(ErrorLevel.MEDIUM, "Error saving formatter");
            return false;
        } finally {
            if (file != null) {
                try {
                    file.close();
                } catch (IOException ex) {
                    // Bleh?
                }
            }
        }
        
        return true;
    }
    
    /**
     * Reloads the formatter.
     */
    public static void reload() {
        loadDefaults();
        
        initialise();
    }
}
