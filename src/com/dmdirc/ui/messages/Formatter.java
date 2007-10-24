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

import com.dmdirc.Precondition;
import com.dmdirc.config.ConfigChangeListener;
import com.dmdirc.config.ConfigManager;
import com.dmdirc.config.IdentityManager;

import java.io.InputStream;
import java.util.HashMap;
import java.util.IllegalFormatConversionException;
import java.util.List;
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
     * The config manager we're using.
     */
    private static final ConfigManager config = IdentityManager.getGlobalConfig();
    
    static {
        config.addChangeListener("formatter", new ConfigChangeListener() {
            @Override
            public void configChanged(final String domain, final String key) {
                Formatter.typeCache.clear();
            }
        });
    }
    
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
     * @param arguments The arguments to this message type
     * @return A formatted string
     */
    @Precondition("The specified message type is not null")
    public static String formatMessage(final String messageType,
            final Object... arguments) {
        assert(messageType != null);
                
        final String res = config.getOption("formatter", messageType, null);
        
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
    @Precondition("The specified format is not null")
    private static Object[] castArguments(final String format, final Object[] args) {
        assert(format != null);
        
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
     * @deprecated This should be done via a config manager directly
     */
    @Deprecated
    public static List<String> getFormats() {
        return config.getOptions("formatter");
    }
    /**
     * Determines whether the formatter knows of a specific message type.
     * 
     * @param messageType the message type to check
     * @return True iff there is a matching format, false otherwise
     */
    public static boolean hasFormat(final String messageType) {
        return config.hasOption("formatter", messageType);
    }
    
    /**
     * Allows plugins (etc) to register new default formats.
     * 
     * @param name The name of the format
     * @param format The actual format itself
     * @deprecated This should now be done via the identities system
     */
    @Deprecated
    public static void registerDefault(final String name, final String format) {        
        typeCache.remove(name);
        
        IdentityManager.getConfigIdentity().setOption("formatter", name, format);
    }
    
    /**
     * Loads the specified file into the formatter.
     *
     * @param file File to be loaded
     * @return True iff the operation succeeeded, false otherwise
     * @deprecated No longer has any effect
     */
    @Deprecated
    public static boolean loadFile(final String file) {
        return false;
    }
    
    /**
     * Reads the specified input stream as a properties file and loads the
     * contained formatter settings into the formatter.
     *
     * @param stream The stream to be read
     * @return True iff the operation succeeded, false otherwise
     * @deprecated No longer has any effect
     */
    @Deprecated
    public static boolean loadFile(final InputStream stream) {
        return false;
    }
    
    /**
     * Saves the current formatter into the specified file.
     * 
     * @param target The target file
     * @return True iff the operation succeeded, false otherwise
     * @deprecated No longer has any effect
     */
    @Deprecated
    public static boolean saveAs(final String target) {
        return false;        
    }
    
    /**
     * Reloads the formatter.
     * 
     * @deprecated No longer has any effct
     */
    @Deprecated
    public static void reload() {

    }
}
