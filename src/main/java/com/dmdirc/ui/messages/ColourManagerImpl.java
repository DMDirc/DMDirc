/*
 * Copyright (c) 2006-2017 DMDirc Developers
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the
 * Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS
 * OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.dmdirc.ui.messages;

import com.dmdirc.config.provider.AggregateConfigProvider;
import com.dmdirc.util.colours.Colour;
import com.dmdirc.util.validators.ColourValidator;
import com.dmdirc.util.validators.Validator;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.dmdirc.util.LogUtils.USER_ERROR;

/**
 * The colour manager manages the colour scheme for the IRC client. It allows other components to
 * use IRC colour codes instead of absolute colours.
 */
public class ColourManagerImpl implements ColourManager {

    private static final Logger LOG = LoggerFactory.getLogger(ColourManagerImpl.class);
    /** Default colours used for the standard 16 IRC colours. */
    private static final Colour[] DEFAULT_COLOURS = {
        Colour.WHITE, Colour.BLACK, new Colour(0, 0, 127), new Colour(0, 141, 0),
        Colour.RED, new Colour(127, 0, 0), new Colour(160, 15, 160), new Colour(252, 127, 0),
        Colour.YELLOW, new Colour(0, 252, 0), new Colour(0, 128, 128), new Colour(0, 255, 255),
        Colour.BLUE, new Colour(255, 0, 255), Colour.GRAY, Colour.LIGHT_GRAY,};
    /** Colour cache. */
    private final Map<String, Colour> colourCache = new HashMap<>();
    /** Config manager to read settings from. */
    private final AggregateConfigProvider configManager;
    /** Actual colours we're using for the 16 IRC colours. */
    private final Colour[] ircColours = DEFAULT_COLOURS.clone();

    /**
     * Creates a new instance of {@link ColourManagerImpl}.
     *
     * @param configManager The manager to read config settings from.
     */
    public ColourManagerImpl(final AggregateConfigProvider configManager) {
        this.configManager = configManager;

        configManager.addChangeListener("colour", (domain, key) -> initColours());

        initColours();
    }

    /**
     * Initialises the IRC_COLOURS array.
     */
    private void initColours() {
        final Validator<String> validator = new ColourValidator();
        for (int i = 0; i < 16; i++) {
            if (configManager.hasOptionString("colour", String.valueOf(i), validator)) {
                ircColours[i] = getColourFromHex(
                        configManager.getOptionString("colour", String.valueOf(i)));
                colourCache.remove(String.valueOf(i));
            } else if (!ircColours[i].equals(DEFAULT_COLOURS[i])) {
                ircColours[i] = DEFAULT_COLOURS[i];
                colourCache.remove(String.valueOf(i));
            }
        }
    }

    @Override
    public Colour getColourFromString(final String spec, final Colour fallback) {
        if (colourCache.containsKey(spec)) {
            return colourCache.get(spec);
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
                    res = getColourFromIrcCode(num);
                }
            } else if (spec.length() == 6) {
                res = getColourFromHex(spec);
            }
        }

        if (res == null) {
            LOG.warn(USER_ERROR, "Invalid colour format: {}", spec);
            res = fallback;
        } else {
            colourCache.put(spec, res);
        }

        return res;
    }

    @Override
    public Colour getColourFromHex(final String hex) {
        if (colourCache.containsKey(hex)) {
            return colourCache.get(hex);
        }

        if (hex.length() < 6) {
            LOG.warn(USER_ERROR, "Invalid colour: #{}", hex);
            return Colour.WHITE;
        }

        final Colour colour;
        try {
            colour = new Colour(
                    Integer.parseInt(hex.substring(0, 2), 16),
                    Integer.parseInt(hex.substring(2, 4), 16),
                    Integer.parseInt(hex.substring(4, 6), 16));
        } catch (NumberFormatException ex) {
            LOG.warn(USER_ERROR, "Invalid colour: #{}", hex);
            return Colour.WHITE;
        }

        colourCache.put(hex, colour);
        return colour;
    }

    @Override
    public Colour getColourFromIrcCode(final int number) {
        if (number >= 0 && number <= 15) {
            return ircColours[number];
        } else {
            LOG.warn(USER_ERROR, "Invalid colour: {}", number);
            return Colour.WHITE;
        }
    }

}
