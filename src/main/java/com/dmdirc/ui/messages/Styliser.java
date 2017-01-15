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

import com.dmdirc.interfaces.Connection;
import com.dmdirc.config.provider.AggregateConfigProvider;
import com.dmdirc.config.provider.ConfigChangeListener;
import com.dmdirc.util.colours.Colour;
import com.google.common.annotations.VisibleForTesting;
import java.util.Locale;
import java.util.regex.Pattern;
import javax.annotation.Nullable;

/**
 * The styliser applies IRC styles to text. Styles are indicated by various control codes which are
 * a de-facto IRC standard.
 */
public class Styliser implements ConfigChangeListener, StyleApplier {

    /** Internal chars. */
    private static final String INTERNAL_CHARS = String.valueOf(CODE_HYPERLINK)
            + CODE_NICKNAME + CODE_CHANNEL + CODE_SMILIE + CODE_TOOLTIP;
    /** Characters used for hyperlinks. */
    private static final String HYPERLINK_CHARS = Character.toString(CODE_HYPERLINK) + CODE_CHANNEL;
    /** Regexp to match characters which shouldn't be used in channel links. */
    private static final String RESERVED_CHARS = "[^\\s" + IRCControlCodes.BOLD + IRCControlCodes.COLOUR
            + IRCControlCodes.STOP + IRCControlCodes.COLOUR_HEX + IRCControlCodes.FIXED + IRCControlCodes.ITALIC
            + IRCControlCodes.UNDERLINE + CODE_CHANNEL + CODE_NICKNAME + IRCControlCodes.NEGATE
        + "\",]";
    /** Defines all characters treated as trailing punctuation that are illegal in URLs. */
    private static final String URL_PUNCT_ILLEGAL = "\"";
    /** Defines all characters treated as trailing punctuation that're legal in URLs. */
    private static final String URL_PUNCT_LEGAL = "';:!,\\.\\?";
    /** Defines all trailing punctuation. */
    private static final String URL_PUNCT = URL_PUNCT_ILLEGAL + URL_PUNCT_LEGAL;
    /** Defines all characters allowed in URLs that aren't treated as trailing punct. */
    private static final String URL_NOPUNCT = "a-z0-9$\\-_@&\\+\\*\\(\\)=/#%~\\|";
    /** Defines all characters allowed in URLs per W3C specs. */
    private static final String URL_CHARS = '[' + URL_PUNCT_LEGAL + URL_NOPUNCT
            + "]*[" + URL_NOPUNCT + "]+[" + URL_PUNCT_LEGAL + URL_NOPUNCT + "]*";
    /** The regular expression to use for marking up URLs. */
    private static final String URL_REGEXP = "(?i)((?>(?<!" + IRCControlCodes.COLOUR_HEX
            + "[a-f0-9]{5})[a-f]|[g-z+])+://" + URL_CHARS
            + "|(?<![a-z0-9:/])www\\." + URL_CHARS + ')';
    /** Regular expression for intelligent handling of closing brackets. */
    private static final String URL_INT1 = "(\\([^\\)" + HYPERLINK_CHARS
            + "]*(?:[" + HYPERLINK_CHARS + "][^" + HYPERLINK_CHARS + "]*["
            + HYPERLINK_CHARS + "])?[^\\)" + HYPERLINK_CHARS + "]*[" + HYPERLINK_CHARS
            + "][^" + HYPERLINK_CHARS + "]+)(\\)['\";:!,\\.\\)]*)([" + HYPERLINK_CHARS + "])";
    /** Regular expression for intelligent handling of trailing single and double quotes. */
    private static final String URL_INT2 = "(^(?:[^" + HYPERLINK_CHARS + "]+|["
            + HYPERLINK_CHARS + "][^" + HYPERLINK_CHARS + "][" + HYPERLINK_CHARS
            + "]))(['\"])([^" + HYPERLINK_CHARS + "]*?[" + HYPERLINK_CHARS + "][^"
            + HYPERLINK_CHARS + "]+)(\\1[" + URL_PUNCT + "]*)([" + HYPERLINK_CHARS + "])";
    /** Regular expression for intelligent handling of surrounding quotes. */
    private static final String URL_INT3 = "(['\"])([" + HYPERLINK_CHARS
            + "][^" + HYPERLINK_CHARS + "]+?)(\\1[^" + HYPERLINK_CHARS + "]*)(["
            + HYPERLINK_CHARS + "])";
    /** Regular expression for intelligent handling of trailing punctuation. */
    private static final String URL_INT4 = "([" + HYPERLINK_CHARS + "][^"
            + HYPERLINK_CHARS + "]+?)([" + URL_PUNCT + "]?)([" + HYPERLINK_CHARS + "])";
    /** The regular expression to use for marking up channels. */
    private static final String URL_CHANNEL = "(?i)(?<![^\\s\\+@\\-<>\\(\"',])([\\Q%s\\E]"
            + RESERVED_CHARS + "+)";
    /** Whether or not we should style links. */
    private boolean styleURIs;
    /** Whether or not we should style channel names. */
    private boolean styleChannels;
    /** Colour to use for URIs. */
    private Colour uriColour;
    /** Colour to use for channel names. */
    private Colour channelColour;
    /** Connection to get channel prefixes from, or null if not applicable. */
    @Nullable
    private final Connection connection;
    /** Config manager to retrieve settings from. */
    private final AggregateConfigProvider configManager;
    /** Colour manager to use to parse colours. */
    private final ColourManager colourManager;

    /**
     * Creates a new instance of Styliser.
     *
     * @param connection    The {@link Connection} that this styliser is for. May be {@code null}.
     * @param configManager the {@link AggregateConfigProvider} to get settings from.
     * @param colourManager The {@link ColourManager} to get colours from.
     *
     * @since 0.6.3
     */
    public Styliser(@Nullable final Connection connection,
            final AggregateConfigProvider configManager,
            final ColourManager colourManager) {
        this.connection = connection;
        this.configManager = configManager;
        this.colourManager = colourManager;

        configManager.addChangeListener("ui", "linkcolour", this);
        configManager.addChangeListener("ui", "channelcolour", this);
        configManager.addChangeListener("ui", "stylelinks", this);
        configManager.addChangeListener("ui", "stylechannels", this);
        styleURIs = configManager.getOptionBool("ui", "stylelinks");
        styleChannels = configManager.getOptionBool("ui", "stylechannels");
        uriColour = colourManager.getColourFromString(
                configManager.getOptionString("ui", "linkcolour"), null);
        channelColour = colourManager.getColourFromString(
                configManager.getOptionString("ui", "channelcolour"), null);
    }

    @Override
    public void addStyledString(final StyledMessageMaker<?> maker, final String... strings) {
        maker.resetAllStyles();

        for (String string : strings) {
            final char[] chars = string.toCharArray();

            for (int j = 0; j < chars.length; j++) {
                if (chars[j] == 65533) {
                    chars[j] = '?';
                }
            }

            int position = 0;

            final String target =
                    doSmilies(doLinks(new String(chars).replaceAll(INTERNAL_CHARS, "")));
            final StyliserState state = new StyliserState();

            while (position < target.length()) {
                final String next = readUntilControl(target.substring(position));
                maker.appendString(next);
                position += next.length();

                if (position < target.length()) {
                    position += readControlChars(target.substring(position), state, maker);
                }
            }
        }
    }

    @Override
    public String doLinks(final String string) {
        String target = string;
        final String prefixes = connection == null ? null
                : connection.getGroupChatManager().getChannelPrefixes();

        String target2 = target;
        target = target.replaceAll(URL_REGEXP, CODE_HYPERLINK + "$0" + CODE_HYPERLINK);

        if (prefixes != null) {
            target = target.replaceAll(String.format(URL_CHANNEL, prefixes),
                    CODE_CHANNEL + "$0" + CODE_CHANNEL);
        }

        for (int j = 0; j < 5 && !target.equals(target2); j++) {
            target2 = target;
            target = target
                    .replaceAll(URL_INT1, "$1$3$2")
                    .replaceAll(URL_INT2, "$1$2$3$5$4")
                    .replaceAll(URL_INT3, "$1$2$4$3")
                    .replaceAll(URL_INT4, "$1$3$2");
        }

        return target;
    }

    /**
     * Applies the smilie styles to the target.
     *
     * @param string The string to be smilified
     *
     * @return A copy of the string with smilies marked up
     *
     * @since 0.6.3m1
     */
    private String doSmilies(final String string) {
        // TODO: Check if they're enabled.
        // TODO: Store the list instead of building it every line

        final StringBuilder smilies = new StringBuilder();

        configManager.getOptions("icon").entrySet().stream()
                .filter(icon -> icon.getKey().startsWith("smilie-")).forEach(icon -> {
            if (smilies.length() > 0) {
                smilies.append('|');
            }

            smilies.append(Pattern.quote(icon.getKey().substring(7)));
        });

        return string.replaceAll("(\\s|^)(" + smilies + ")(?=\\s|$)",
                "$1" + CODE_SMILIE + "$2" + CODE_SMILIE);
    }

    /**
     * Returns a substring of the input string such that no control codes are present in the output.
     * If the returned value isn't the same as the input, then the character immediately after is a
     * control character.
     *
     * @param input The string to read from
     *
     * @return A substring of the input containing no control characters
     */
    @VisibleForTesting
    static String readUntilControl(final String input) {
        int pos = input.length();

        pos = checkChar(pos, input.indexOf(IRCControlCodes.BOLD));
        pos = checkChar(pos, input.indexOf(IRCControlCodes.UNDERLINE));
        pos = checkChar(pos, input.indexOf(IRCControlCodes.STOP));
        pos = checkChar(pos, input.indexOf(IRCControlCodes.COLOUR));
        pos = checkChar(pos, input.indexOf(IRCControlCodes.COLOUR_HEX));
        pos = checkChar(pos, input.indexOf(IRCControlCodes.ITALIC));
        pos = checkChar(pos, input.indexOf(IRCControlCodes.FIXED));
        pos = checkChar(pos, input.indexOf(CODE_HYPERLINK));
        pos = checkChar(pos, input.indexOf(CODE_NICKNAME));
        pos = checkChar(pos, input.indexOf(CODE_CHANNEL));
        pos = checkChar(pos, input.indexOf(CODE_SMILIE));
        pos = checkChar(pos, input.indexOf(IRCControlCodes.NEGATE));
        pos = checkChar(pos, input.indexOf(CODE_TOOLTIP));

        return input.substring(0, pos);
    }

    /**
     * Helper function used in readUntilControl. Checks if i is a valid index of the string (i.e.,
     * it's not -1), and then returns the minimum of pos and i.
     *
     * @param pos The current position in the string
     * @param i   The index of the first occurrence of some character
     *
     * @return The new position (see implementation)
     */
    private static int checkChar(final int pos, final int i) {
        if (i < pos && i != -1) {
            return i;
        }
        return pos;
    }

    /**
     * Reads the first control character from the input string (and any arguments it takes), and
     * applies it to the specified attribute set.
     *
     * @return The number of characters read as control characters
     * @param string  The string to read from
     * @param maker The attribute set that new attributes will be applied to
     */
    private int readControlChars(final String string, final StyliserState state, final StyledMessageMaker<?> maker) {
        final boolean isNegated = state.isNegated;

        // Bold
        if (string.charAt(0) == IRCControlCodes.BOLD) {
            if (!isNegated) {
                maker.toggleBold();
            }

            return 1;
        }

        // Underline
        if (string.charAt(0) == IRCControlCodes.UNDERLINE) {
            if (!isNegated) {
                maker.toggleUnderline();
            }

            return 1;
        }

        // Italic
        if (string.charAt(0) == IRCControlCodes.ITALIC) {
            if (!isNegated) {
                maker.toggleItalic();
            }

            return 1;
        }

        // Hyperlinks
        if (string.charAt(0) == CODE_HYPERLINK) {
            if (!isNegated && styleURIs) {
                maker.toggleHyperlinkStyle(uriColour);
            }

            if (state.isInLink) {
                maker.endHyperlink();
            } else {
                maker.startHyperlink(readUntilControl(string.substring(1)));
            }
            state.isInLink = !state.isInLink;

            return 1;
        }

        // Channel links
        if (string.charAt(0) == CODE_CHANNEL) {
            if (!isNegated && styleChannels) {
                maker.toggleChannelLinkStyle(channelColour);
            }

            if (state.isInLink) {
                maker.endChannelLink();
            } else {
                maker.startChannelLink(readUntilControl(string.substring(1)));
            }
            state.isInLink = !state.isInLink;

            return 1;
        }

        // Nickname links
        if (string.charAt(0) == CODE_NICKNAME) {
            int count = 1;
            if (state.isInLink) {
                maker.endNicknameLink();
            } else {
                int next = string.indexOf(CODE_NICKNAME, 1);
                maker.startNicknameLink(string.substring(1, next));
                count += next;
            }
            state.isInLink = !state.isInLink;

            return count;
        }

        // Fixed pitch
        if (string.charAt(0) == IRCControlCodes.FIXED) {
            if (!isNegated) {
                maker.toggleFixedWidth();
            }

            return 1;
        }

        // Stop formatting
        if (string.charAt(0) == IRCControlCodes.STOP) {
            if (!isNegated) {
                maker.resetAllStyles();
            }

            return 1;
        }

        // Colours
        if (string.charAt(0) == IRCControlCodes.COLOUR) {
            int count = 1;
            // This isn't too nice!
            if (string.length() > count && isInt(string.charAt(count))) {
                int foreground = string.charAt(count) - '0';
                count++;
                if (string.length() > count && isInt(string.charAt(count))) {
                    foreground = foreground * 10 + string.charAt(count) - '0';
                    count++;
                }
                foreground %= 16;

                if (!isNegated) {
                    maker.setForeground(colourManager.getColourFromString(
                            String.valueOf(foreground), Colour.WHITE));
                }

                // Now background
                if (string.length() > count && string.charAt(count) == ','
                        && string.length() > count + 1
                        && isInt(string.charAt(count + 1))) {
                    int background = string.charAt(count + 1) - '0';
                    count += 2; // Comma and first digit
                    if (string.length() > count && isInt(string.charAt(count))) {
                        background = background * 10 + string.charAt(count) - '0';
                        count++;
                    }
                    background %= 16;

                    if (!isNegated) {
                        maker.setBackground(colourManager
                                .getColourFromString(String.valueOf(background), Colour.WHITE));
                    }
                }
            } else if (!isNegated) {
                maker.resetColours();
            }
            return count;
        }

        // Hex colours
        if (string.charAt(0) == IRCControlCodes.COLOUR_HEX) {
            int count = 1;
            if (hasHexString(string, 1)) {
                if (!isNegated) {
                    maker.setForeground(
                            colourManager.getColourFromString(string.substring(1, 7).toUpperCase(),
                                    Colour.WHITE));
                }

                count += 6;

                if (string.length() == count) {
                    return count;
                }
                // Now for background
                if (string.charAt(count) == ',' && hasHexString(string, count + 1)) {
                    count++;

                    if (!isNegated) {
                        maker.setBackground(colourManager.getColourFromString(
                                string.substring(count, count + 6).toUpperCase(), Colour.WHITE));
                    }

                    count += 6;
                }
            } else if (!isNegated) {
                maker.resetColours();
            }
            return count;
        }

        // Control code negation
        if (string.charAt(0) == IRCControlCodes.NEGATE) {
            state.isNegated = !state.isNegated;
            return 1;
        }

        // Smilies!!
        if (string.charAt(0) == CODE_SMILIE) {
            if (state.isInSmilie) {
                maker.endSmilie();
            } else {
                maker.startSmilie("smilie-" + readUntilControl(string.substring(1)));
            }
            state.isInSmilie = !state.isInSmilie;

            return 1;
        }

        // Tooltips
        if (string.charAt(0) == CODE_TOOLTIP) {
            if (state.isInToolTip) {
                maker.endToolTip();
            } else {
                final int index = string.indexOf(CODE_TOOLTIP, 1);

                if (index == -1) {
                    // Doesn't make much sense, let's ignore it!
                    return 1;
                }

                final String tooltip = string.substring(1, index);

                maker.startToolTip(tooltip);

                state.isInToolTip = !state.isInToolTip;
                return tooltip.length() + 2;
            }
            state.isInToolTip = !state.isInToolTip;

            return 1;
        }

        return 0;
    }

    /**
     * Determines if the specified character represents a single integer (i.e. 0-9).
     *
     * @param c The character to check
     *
     * @return True iff the character is in the range [0-9], false otherwise
     */
    private static boolean isInt(final char c) {
        return c >= '0' && c <= '9';
    }

    /**
     * Determines if the specified character represents a single hex digit (i.e., 0-F).
     *
     * @param c The character to check
     *
     * @return True iff the character is in the range [0-F], false otherwise
     */
    private static boolean isHex(final char c) {
        return isInt(c) || c >= 'A' && c <= 'F';
    }

    /**
     * Determines if the specified string has a 6-digit hex string starting at the specified offset.
     *
     * @param input  The string to check
     * @param offset The offset to start at
     *
     * @return True iff there is a hex string preset at the offset
     */
    private static boolean hasHexString(final String input, final int offset) {
        // If the string's too short, it can't have a hex string
        if (input.length() < offset + 6) {
            return false;
        }
        boolean res = true;
        for (int i = offset; i < 6 + offset; i++) {
            res = res && isHex(input.toUpperCase(Locale.getDefault()).charAt(i));
        }

        return res;
    }

    @Override
    public void configChanged(final String domain, final String key) {
        switch (key) {
            case "stylelinks":
                styleURIs = configManager.getOptionBool("ui", "stylelinks");
                break;
            case "stylechannels":
                styleChannels = configManager.getOptionBool("ui", "stylechannels");
                break;
            case "linkcolour":
                uriColour = colourManager.getColourFromString(
                        configManager.getOptionString("ui", "linkcolour"), null);
                break;
            case "channelcolour":
                channelColour = colourManager.getColourFromString(
                        configManager.getOptionString("ui", "channelcolour"), null);
                break;
        }
    }

    private static class StyliserState {

        boolean isNegated;
        boolean isInLink;
        boolean isInSmilie;
        boolean isInToolTip;

    }

}
