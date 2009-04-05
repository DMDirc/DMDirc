/*
 * Copyright (c) 2006-2009 Chris Smith, Shane Mc Cormack, Gregory Holmes
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

import com.dmdirc.Precondition;
import com.dmdirc.config.ConfigManager;

import java.util.HashMap;
import java.util.IllegalFormatConversionException;
import java.util.Map;
import java.util.MissingFormatArgumentException;
import java.util.UnknownFormatConversionException;

/**
 * The Formatter provides a standard way to format messages for display.
 */
public final class Formatter {
       
    /**
     * A cache of types needed by the various formatters.
     */
    private static final Map<String, Character[]> typeCache
            = new HashMap<String, Character[]>();
   
    /**
     * Creates a new instance of Formatter.
     */
    private Formatter() {
        // Shouldn't be used
    }
    
    /**
     * Inserts the supplied arguments into a format string for the specified
     * message type.
     * 
     * @param messageType The message type that the arguments should be formatted as
     * @param config The config manager to use to format the message
     * @param arguments The arguments to this message type
     * @return A formatted string
     */
    @Precondition("The specified message type is not null")
    public static String formatMessage(final ConfigManager config, final String messageType,
            final Object... arguments) {
        assert(messageType != null);
                
        final String res = config.hasOptionString("formatter", messageType) ?
            config.getOption("formatter", messageType).replace("%-1$", "%"
            + arguments.length + "$"): null;
        
        if (res == null) {
            return "<No format string for message type " + messageType + ">";
        } else {
            try {
                final Object[] newArgs = castArguments(res, arguments);
                return String.format(res.replaceAll("(%[0-9]+\\$)u", "$1s"), newArgs);
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
    @Precondition("The specified format is not null")
    private static Object[] castArguments(final String format, final Object[] args) {
        assert(format != null);
        
        if (!typeCache.containsKey(format)) {
            analyseFormat(format, args);
        }
        
        final Object[] res = new Object[args.length];
        
        int i = 0;
        for (Character chr : typeCache.get(format)) {
            if (i >= args.length) {
                break;
            }
            
            switch (chr) {
            case 'b': case 'B': case 'h': case 'H': case 's': case 'S':
                // General (strings)
                res[i] = String.valueOf(args[i]);
                break;
            case 'c': case 'C':
                // Character
                res[i] = String.valueOf(args[i]).charAt(0);
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
            case 'u':
                // Duration hacks
                res[i] = formatDuration(Integer.valueOf(String.valueOf(args[i].toString())));
                break;
            default:
                res[i] = args[i];
            }
            
            i++;
        }
        
        return res;
    }
    
    /**
     * Tests for and adds one component of the duration format.
     * 
     * @param builder The string builder to append text to
     * @param current The number of seconds in the duration
     * @param duration The number of seconds in this component
     * @param name The name of this component
     * @return The number of seconds used by this component
     */
    private static int doDuration(final StringBuilder builder, final int current,
            final int duration, final String name) {
        int res = 0;
        
        if (current >= duration) {
            final int units = current / duration;
            res = units * duration;
            
            if (builder.length() > 0) {
                builder.append(", ");
            }
            
            builder.append(units);
            builder.append(' ');
            builder.append(name + (units != 1 ? 's' : ""));
        }
        
        return res;
    }
    
    /**
     * Formats the specified number of seconds as a string containing the
     * number of days, hours, minutes and seconds.
     * 
     * @param duration The duration in seconds to be formatted
     * @return A textual version of the duration
     */
    public static String formatDuration(final int duration) {
        final StringBuilder buff = new StringBuilder();
        
        int seconds = duration;
        
        seconds -= doDuration(buff, seconds, 60*60*24, "day");
        seconds -= doDuration(buff, seconds, 60*60, "hour");
        seconds -= doDuration(buff, seconds, 60, "minute");
        seconds -= doDuration(buff, seconds, 1, "second");
        
        return buff.toString();
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

}
