/*
 * Copyright (c) 2006-2011 DMDirc Developers
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

import com.dmdirc.config.IdentityManager;
import com.dmdirc.interfaces.ConfigChangeListener;
import com.dmdirc.logger.ErrorLevel;
import com.dmdirc.logger.Logger;
import com.dmdirc.ui.Colour;

import java.util.HashMap;
import java.util.Map;

/**
 * The colour manager manages the colour scheme for the IRC client. It allows
 * other components to use IRC colour codes instead of absolute colours.
 */
public final class ColourManager {

    /** Colour cache. */
    private static final Map<String, Colour> COLOUR_CACHE = new HashMap<String, Colour>();

    /** Default colours used for the standard 16 IRC colours. */
    private static final Colour[] DEFAULT_COLOURS = {
        Colour.WHITE, Colour.BLACK, new Colour(0, 0, 127), new Colour(0, 141, 0),
        Colour.RED, new Colour(127, 0, 0), new Colour(160, 15, 160), new Colour(252, 127, 0),
        Colour.YELLOW, new Colour(0, 252, 0), new Colour(0, 128, 128), new Colour(0, 255, 255),
        Colour.BLUE, new Colour(255, 0, 255), Colour.GRAY, Colour.LIGHT_GRAY,
    };

    /** Actual colours we're using for the 16 IRC colours. */
    private static Colour[] ircColours = DEFAULT_COLOURS.clone();

    /** Creates a new instance of ColourManager. */
    private ColourManager() {
    }

    /**
     * Initialises the IRC_COLOURS array.
     */
    private static void initColours() {
        for (int i = 0; i < 16; i++) {
            if (IdentityManager.getGlobalConfig().hasOptionColour("colour", String.valueOf(i))) {
                ircColours[i] = IdentityManager.getGlobalConfig()
                        .getOptionColour("colour", String.valueOf(i));
                COLOUR_CACHE.remove(String.valueOf(i));
            } else if (!ircColours[i].equals(DEFAULT_COLOURS[i])) {
                ircColours[i] = DEFAULT_COLOURS[i];
                COLOUR_CACHE.remove(String.valueOf(i));
            }
        }
    }

    /**
     * Parses either a 1-2 digit IRC colour, or a 6 digit hex colour from the
     * target string, and returns the corresponding colour. Returns the
     * specified fallback colour if the spec can't be parsed.
     * @param spec The string to parse
     * @param fallback The colour to use if the spec isn't valid
     * @return A colour representation of the specified string
     */
    public static Colour parseColour(final String spec, final Colour fallback) {
        if (COLOUR_CACHE.containsKey(spec)) {
            return COLOUR_CACHE.get(spec);
        }

        Colour res = null;

        if (spec != null) {
            if (spec.length() < 3) {
                int num;

                try {
                    num = Integer.parseInt(spec);
                } catch (NumberFormatException ex) {
                    num = -1;
                }

                if (num >= 0 && num <= 15) {
                    res = getColour(num);
                }
            } else if (spec.length() == 6) {
                res = getColour(spec);
            }
        }

        if (res == null) {
            Logger.userError(ErrorLevel.MEDIUM, "Invalid colour format: " + spec);
            res = fallback;
        } else {
            COLOUR_CACHE.put(spec, res);
        }

        return res;
    }

    /**
     * Parses either a 1-2 digit IRC colour, or a 6 digit hex colour from the
     * target string, and returns the corresponding colour. Returns white if the
     * spec can't be parsed.
     * @param spec The string to parse
     * @return A colour representation of the specified string
     */
    public static Colour parseColour(final String spec) {
        return parseColour(spec, Colour.WHITE);
    }

    /**
     * Returns a Colour object that corresponds to the specified 6-digit hex
     * string. If the string is invalid, logs a warning and returns white.
     * @param hex The hex string to convert into a Colour
     * @return A Colour object corresponding to the hex input
     */
    public static Colour getColour(final String hex) {
        if (COLOUR_CACHE.containsKey(hex)) {
            return COLOUR_CACHE.get(hex);
        }

        if (hex.length() < 6) {
            Logger.userError(ErrorLevel.MEDIUM, "Invalid colour #" + hex);
            return Colour.WHITE;
        }

        Colour colour = null;

        try {
            colour = new Colour(
                    Integer.parseInt(hex.substring(0, 2), 16),
                    Integer.parseInt(hex.substring(2, 4), 16),
                    Integer.parseInt(hex.substring(4, 6), 16));
        } catch (NumberFormatException ex) {
            Logger.userError(ErrorLevel.MEDIUM, "Invalid colour #" + hex);
            return Colour.WHITE;
        }

        COLOUR_CACHE.put(hex, colour);
        return colour;
    }

    /**
     * Returns a Colour object that represents the colour associated with the
     * specified IRC colour code. If the code is not found, a warning is logged
     * with the client's Logger class, and white is returned.
     * @param number The IRC colour code to look up
     * @return The corresponding Colour object
     */
    public static Colour getColour(final int number) {
        if (number >= 0 && number <= 15) {
            return ircColours[number];
        } else {
            Logger.userError(ErrorLevel.MEDIUM, "Invalid colour: " + number);
            return Colour.WHITE;
        }
    }

    /**
     * Retrieves the hex representation of the specified colour.
     * @param colour The colour to be parsed
     * @return A 6-digit hex string representing the colour
     */
    public static String getHex(final Colour colour) {
        final int r = colour.getRed();
        final int g = colour.getGreen();
        final int b = colour.getBlue();

        return toHex(r) + toHex(g) + toHex(b);
    }

    /**
     * Converts the specified integer (in the range 0-255) into a hex string.
     * @param value The integer to convert
     * @return A 2 char hex string representing the specified integer
     */
    private static String toHex(final int value) {
        final String hex = Integer.toHexString(value);
        return (hex.length() < 2 ? "0" : "") + hex;
    }

    static {
        IdentityManager.getIdentityManager().getGlobalConfiguration()
                .addChangeListener("colour", new ConfigChangeListener() {
            /** {@inheritDoc} */
            @Override
            public void configChanged(final String domain, final String key) {
                initColours();
            }
        });

        initColours();
    }

}
