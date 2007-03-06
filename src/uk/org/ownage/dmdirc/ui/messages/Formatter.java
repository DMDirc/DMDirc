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
        
        // Type: Timestamp
        //    1: Current timestamp
        properties.setProperty("timestamp","%1$tH:%1$tM:%1$tS | ");
        
        // Type: Channel Message
        //    1: User mode prefixes
        //    2: User nickname
        //    3: User ident
        //    4: User host
        //    5: Message content
        //    6: Channel name
        properties.setProperty("channelMessage", "<%1$s%2$s> %5$s");
        properties.setProperty("channelAction", colour+"6* %1$s%2$s %5$s");
        properties.setProperty("channelSelfMessage", "<%1$s%2$s> %5$s");
        properties.setProperty("channelSelfAction", colour+"6* %1$s%2$s %5$s");
        properties.setProperty("channelSelfExternalMessage", "<%1$s%2$s> %5$s");
        properties.setProperty("channelSelfExternalAction", colour+"6* %1$s%2$s %5$s");
        
        // Type: Channel Event
        //    1: User mode prefixes
        //    2: User nickname
        //    3: User ident
        //    4: User host
        //    5: Channel name
        properties.setProperty("channelJoin", colour+"3* %2$s (%3$s@%4$s) has joined %5$s.");
        properties.setProperty("channelPart", colour+"3* %1$s%2$s (%3$s@%4$s) has left %5$s.");
        properties.setProperty("channelQuit", colour+"2* %1$s%2$s (%3$s@%4$s) has quit IRC.");
        properties.setProperty("channelSelfJoin", colour+"3* You are now talking in %5$s.");
        properties.setProperty("channelSelfPart", colour+"3* You have left the channel.");
        
        // Type: Channel Event with content
        //    1: User mode prefixes
        //    2: User nickname
        //    3: User ident
        //    4: User host
        //    5: Content
        //    6: Channel name
        properties.setProperty("channelPartReason", colour+"3* %1$s%2$s (%3$s@%4$s) has left %6$s (%5$s"+stop+").");
        properties.setProperty("channelQuitReason", colour+"2* %1$s%2$s (%3$s@%4$s) has quit IRC (%5$s"+stop+").");
        properties.setProperty("channelTopicChange", colour+"3* %1$s%2$s has changed the topic to '%5$s"+stop+"'.");
        properties.setProperty("channelNickChange", colour+"3* %1$s%2$s is now know as %5$s.");
        properties.setProperty("channelModeChange", colour+"3* %1$s%2$s sets mode: %5$s.");
        properties.setProperty("channelSelfNickChange", colour+"3* You are now know as %5$s.");
        properties.setProperty("channelSelfModeChange", colour+"3* You set mode: %5$s.");
        properties.setProperty("channelSelfPartReason", colour+"3* You have left the channel.");
        
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
        properties.setProperty("channelKick", colour+"3* %1$s%2$s has kicked %5$s%6$s from %9$s.");
        
        // Type: Binary Channel Event with content
        //    1: Source user mode prefixes
        //    2: Source user nickname
        //    3: Source user ident
        //    4: Source user host
        //    5: Target user mode prefixes
        //    6: Target user nickname
        //    7: Target user ident
        //    8: Target user host
        //    9: Content
        //   10: Channel name
        properties.setProperty("channelKickReason", colour+"3* %1$s%2$s has kicked %5$s%6$s from %10$s (%9$s"+stop+").");
        
        // Type: Channel topic sync
        //    1: Topic
        //    2: User responsible
        //    3: Time last changed
        //    4: Channel name
        properties.setProperty("channelJoinTopic", colour+"3* The topic for %4$s is '%1$s"+stop+"'.\n"+colour+"3* Topic was set by %2$s.");
        
        // Type: Channel mode discovery
        //     1: Channel modes
        //     2: Channel name
        properties.setProperty("channelModeDiscovered", colour+"3* Channel modes for %2$s are: %1$s.");
        
        // Type: Private CTCP
        //    1: User nickname
        //    2: User ident
        //    3: User host
        //    4: CTCP type
        //    5: CTCP content
        properties.setProperty("privateCTCP", colour+"4-!- CTCP %4$S from %1$s");
        properties.setProperty("privateCTCPreply", colour+"4-!- CTCP %4$S reply from %1$s: %5$s");
        
        // Type: Private communications
        //    1: User nickname
        //    2: User ident
        //    3: User host
        //    4: Message content
        properties.setProperty("privateNotice", colour+"5-%1$s- %4$s");
        properties.setProperty("queryMessage", "<%1$s> %4$s");
        properties.setProperty("queryAction", colour+"6* %1$s %4$s");
        properties.setProperty("querySelfMessage", "<%1$s> %4$s");
        properties.setProperty("querySelfAction", colour+"6* %1$s %4$s");
        properties.setProperty("queryNickChanged", colour+"3* %1$s is now know asn %4$s.");
        
        // Type: Miscellaneous
        //    1: Miscellaneous data
        properties.setProperty("rawCommand", colour+"10 >>> %1$s");
        properties.setProperty("socketClosed", colour+"2 -!- You have been disconnected from the server.");
        
        return properties;
    }
    
    /**
     * Reads the format strings from disk (if available), and initialises the
     * properties object.
     */
    private static void initialise() {
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
    
    /**
     * reloads the formatter
     */
    public static void reload() {
        initialise();
    }
}
