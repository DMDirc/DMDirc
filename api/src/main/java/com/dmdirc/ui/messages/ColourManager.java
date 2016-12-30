package com.dmdirc.ui.messages;

import com.dmdirc.util.colours.Colour;

/**
 * Created by Chris on 30/12/2016.
 */
public interface ColourManager {
    /**
     * Parses either a 1-2 digit IRC colour, or a 6 digit hex colour from the target string, and
     * returns the corresponding colour. Returns the specified fallback colour if the spec can't be
     * parsed.
     *
     * @param spec     The string to parse
     * @param fallback The colour to use if the spec isn't valid
     *
     * @return A colour representation of the specified string
     */
    Colour getColourFromString(String spec, Colour fallback);

    /**
     * Returns a Colour object that corresponds to the specified 6-digit hex string. If the string
     * is invalid, logs a warning and returns white.
     *
     * @param hex The hex string to convert into a Colour
     *
     * @return A Colour object corresponding to the hex input
     */
    Colour getColourFromHex(String hex);

    /**
     * Returns a Colour object that represents the colour associated with the specified IRC colour
     * code. If the code is not found, a warning is logged with the client's Logger class, and white
     * is returned.
     *
     * @param number The IRC colour code to look up
     *
     * @return The corresponding Colour object
     */
    Colour getColourFromIrcCode(int number);
}
