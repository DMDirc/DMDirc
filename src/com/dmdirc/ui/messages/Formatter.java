/*
 * Copyright (c) 2006-2015 DMDirc Developers
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
import com.dmdirc.interfaces.config.AggregateConfigProvider;
import com.dmdirc.util.DateUtils;

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
    private static final Map<String, Character[]> TYPE_CACHE = new HashMap<>();

    /**
     * Creates a new instance of Formatter.
     */
    private Formatter() {
        // Shouldn't be used
    }

    /**
     * Inserts the supplied arguments into a format string for the specified message type.
     *
     * @param messageType The message type that the arguments should be formatted as
     * @param config      The config manager to use to format the message
     * @param arguments   The arguments to this message type
     *
     * @return A formatted string
     */
    @Precondition("The specified message type is not null")
    public static String formatMessage(final AggregateConfigProvider config,
            final String messageType,
            final Object... arguments) {
        assert messageType != null;

        final String res = config.hasOptionString("formatter", messageType)
                ? config.getOption("formatter", messageType).replace("%-1$", "%"
                        + arguments.length + "$") : null;

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
            } catch (NumberFormatException ex) {
                return "<Invalid format string for message type " + messageType
                        + "; Error: Invalid number conversion: " + ex.getMessage() + ">";
            }
        }
    }

    /**
     * Casts the specified arguments to the relevant classes, based on the format type cache.
     *
     * @param format The format to be used
     * @param args   The arguments to be casted
     *
     * @return A new set of arguments of appropriate types
     */
    @Precondition("The specified format is not null")
    private static Object[] castArguments(final String format, final Object[] args) {
        assert format != null;

        if (!TYPE_CACHE.containsKey(format)) {
            analyseFormat(format, args);
        }

        final Object[] res = new Object[args.length];

        int i = 0;
        for (Character chr : TYPE_CACHE.get(format)) {
            if (i >= args.length) {
                break;
            }

            switch (chr) {
                case 'b':
                case 'B':
                case 'h':
                case 'H':
                case 's':
                case 'S':
                    // General (strings)
                    res[i] = String.valueOf(args[i]);
                    break;
                case 'c':
                case 'C':
                    // Character
                    res[i] = String.valueOf(args[i]).charAt(0);
                    break;
                case 'd':
                case 'o':
                case 'x':
                case 'X':
                    // Integers
                    res[i] = Integer.valueOf((String) args[i]);
                    break;
                case 'e':
                case 'E':
                case 'f':
                case 'g':
                case 'G':
                case 'a':
                case 'A':
                    // Floating point
                    res[i] = Float.valueOf((String) args[i]);
                    break;
                case 't':
                case 'T':
                    // Date
                    if (args[i] instanceof String) {
                        // Assume it's a timestamp(?)
                        res[i] = 1000 * Long.valueOf((String) args[i]);
                    } else {
                        res[i] = args[i];
                    }
                    break;
                case 'u':
                    // Duration hacks
                    res[i] = DateUtils.formatDuration(Integer.valueOf(
                            String.valueOf(args[i].toString())));
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
     * @param args   The raw arguments
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

        TYPE_CACHE.put(format, types);
    }

}
