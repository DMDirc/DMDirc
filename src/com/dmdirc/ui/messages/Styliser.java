/*
 * Copyright (c) 2006-2011 Chris Smith, Shane Mc Cormack, Gregory Holmes
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

import com.dmdirc.FrameContainer;
import com.dmdirc.Server;
import com.dmdirc.actions.ActionManager;
import com.dmdirc.actions.CoreActionType;
import com.dmdirc.config.ConfigManager;
import com.dmdirc.interfaces.ConfigChangeListener;
import com.dmdirc.logger.ErrorLevel;
import com.dmdirc.logger.Logger;

import java.awt.Color;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

import javax.swing.UIManager;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

/**
 * The styliser applies IRC styles to text. Styles are indicated by various
 * control codes which are a de-facto IRC standard.
 */
public class Styliser implements ConfigChangeListener {

    /** The character used for marking up bold text. */
    public static final char CODE_BOLD = 2;
    /** The character used for marking up coloured text. */
    public static final char CODE_COLOUR = 3;
    /** The character used for marking up coloured text (using hex). */
    public static final char CODE_HEXCOLOUR = 4;
    /** Character used to indicate hyperlinks. */
    public static final char CODE_HYPERLINK = 5;
    /** Character used to indicate channel links. */
    public static final char CODE_CHANNEL = 6;
    /** Character used to indicate smilies. */
    public static final char CODE_SMILIE = 7;
    /** The character used for stopping all formatting. */
    public static final char CODE_STOP = 15;
    /** Character used to indicate nickname links. */
    public static final char CODE_NICKNAME = 16;
    /** The character used for marking up fixed pitch text. */
    public static final char CODE_FIXED = 17;
    /** The character used for negating control codes. */
    public static final char CODE_NEGATE = 18;
    /** The character used for tooltips. */
    public static final char CODE_TOOLTIP = 19;
    /** The character used for marking up italic text. */
    public static final char CODE_ITALIC = 29;
    /** The character used for marking up underlined text. */
    public static final char CODE_UNDERLINE = 31;

    /** Internal chars. */
    private static final String INTERNAL_CHARS = String.valueOf(CODE_HYPERLINK)
            + CODE_NICKNAME + CODE_CHANNEL + CODE_SMILIE + CODE_TOOLTIP;

    /** Characters used for hyperlinks. */
    private static final String HYPERLINK_CHARS = CODE_HYPERLINK + "" + CODE_CHANNEL;

    /** Regexp to match characters which shouldn't be used in channel links. */
    private static final String RESERVED_CHARS = "[^\\s" + CODE_BOLD + CODE_COLOUR
            + CODE_STOP + CODE_HEXCOLOUR + CODE_FIXED + CODE_ITALIC
            + CODE_UNDERLINE + CODE_CHANNEL + CODE_NICKNAME + CODE_NEGATE + "\",]";

    /** Defines all characters treated as trailing punctuation that are illegal in URLs. */
    private static final String URL_PUNCT_ILLEGAL = "\"";

    /** Defines all characters treated as trailing punctuation that're legal in URLs. */
    private static final String URL_PUNCT_LEGAL = "';:!,\\.\\?";

    /** Defines all trailing punctuation. */
    private static final String URL_PUNCT = URL_PUNCT_ILLEGAL + URL_PUNCT_LEGAL;

    /** Defines all characters allowed in URLs that aren't treated as trailing punct. */
    private static final String URL_NOPUNCT = "a-z0-9$\\-_@&\\+\\*\\(\\)=/#%~\\|";

    /** Defines all characters allowed in URLs per W3C specs. */
    private static final String URL_CHARS = "[" + URL_PUNCT_LEGAL + URL_NOPUNCT
            + "]*[" + URL_NOPUNCT + "]+[" + URL_PUNCT_LEGAL + URL_NOPUNCT + "]*";

    /** The regular expression to use for marking up URLs. */
    private static final String URL_REGEXP = "(?i)((?>(?<!" + CODE_HEXCOLOUR
            + "[a-f0-9]{5})[a-f]|[g-z+])+://" + URL_CHARS
            + "|(?<![a-z0-9:/])www\\." + URL_CHARS + ")";

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
    private boolean styleURIs, styleChannels;

    /** Colours to use for URI and channel links. */
    private Color uriColour, channelColour;

    /** Server to get channel prefixes from, or null if not applicable. */
    private final Server server;
    /** Config manager to retrive settings from. */
    private final ConfigManager configManager;

    /**
     * Creates a new instance of Styliser.
     *
     * @param owner The {@link FrameContainer} that owns this styliser.
     */
    public Styliser(final FrameContainer<?> owner) {
        this(owner.getServer(), owner.getConfigManager());
    }

    /**
     * Creates a new instance of Styliser.
     *
     * @param server The {@link Server} that owns this styliser or null if n/a.
     * @param configManager the {@link ConfigManager} to get settings from.
     * @since 0.6.3
     */
    public Styliser(final Server server, final ConfigManager configManager) {
        this.server = server;
        this.configManager = configManager;

        configManager.addChangeListener("ui", "linkcolour", this);
        configManager.addChangeListener("ui", "channelcolour", this);
        configManager.addChangeListener("ui", "stylelinks", this);
        configManager.addChangeListener("ui", "stylechannels", this);
        styleURIs = configManager.getOptionBool("ui", "stylelinks");
        styleChannels = configManager.getOptionBool("ui", "stylechannels");
        uriColour = configManager.getOptionColour("ui", "linkcolour");
        channelColour = configManager.getOptionColour("ui", "channelcolour");
    }

    /**
     * Stylises the specified strings and adds them to the specified document.
     *
     * @param styledDoc Document to add the styled strings to
     * @param strings The lines to be stylised
     */
    public void addStyledString(final StyledDocument styledDoc, final String[] strings) {
        addStyledString(styledDoc, strings, new SimpleAttributeSet());
    }

    /**
     * Stylises the specified strings and adds them to the specified document.
     *
     * @param styledDoc Document to add the styled strings to
     * @param strings The lines to be stylised
     * @param attribs Base attribute set
     */
    public void addStyledString(final StyledDocument styledDoc,
            final String[] strings, final SimpleAttributeSet attribs) {
        resetAttributes(attribs);
        for (int i = 0; i < strings.length; i++) {
            final char[] chars = strings[i].toCharArray();

            for (int j = 0; j < chars.length; j++) {
                if (chars[j] == 65533) {
                    chars[j] = '?';
                }
            }

            try {
                final int ooffset = styledDoc.getLength();
                int offset = ooffset;
                int position = 0;

                final String target = doSmilies(doLinks(new String(chars)
                        .replaceAll(INTERNAL_CHARS, "")));

                attribs.addAttribute("DefaultFontFamily", UIManager.getFont("TextPane.font"));

                while (position < target.length()) {
                    final String next = readUntilControl(target.substring(position));

                    styledDoc.insertString(offset, next, attribs);

                    position += next.length();
                    offset += next.length();

                    if (position < target.length()) {
                        position += readControlChars(target.substring(position),
                                attribs, position == 0);
                    }
                }

                ActionManager.processEvent(CoreActionType.CLIENT_STRING_STYLED,
                        null, styledDoc, ooffset, styledDoc.getLength() - ooffset);

            } catch (BadLocationException ex) {
                Logger.userError(ErrorLevel.MEDIUM,
                        "Unable to insert styled string: " + ex.getMessage());
            }
        }
    }

    /**
     * Stylises the specified string.
     *
     * @param strings The line to be stylised
     *
     * @return StyledDocument for the inputted strings
     */
    public StyledDocument getStyledString(final String[] strings) {
        final StyledDocument styledDoc = new DefaultStyledDocument();

        addStyledString(styledDoc, strings);

        return styledDoc;
    }

    /**
     * Retrieves the styled String contained within the unstyled offsets
     * specified. That is, the <code>from</code> and <code>to</code> arguments
     * correspond to indexes in an unstyled version of the <code>styled</code>
     * string. The unstyled indices are translated to offsets within the
     * styled String, and the return value includes all text and control codes
     * between those indices.
     * <p>
     * The index translation is left-biased; that is, the indices are translated
     * to be as far left as they possibly can be. This means that the start of
     * the string will include any control codes immediately preceeding the
     * desired text, and the end will not include any trailing codes.
     * <p>
     * This method will NOT include "internal" control codes in the output.
     *
     * @param styled The styled String to be operated on
     * @param from The starting index in the unstyled string
     * @param to The ending index in the unstyled string
     * @return The corresponding text between the two indices
     * @since 0.6.3
     */
    public static String getStyledText(final String styled, final int from, final int to) {
        final String unstyled = stipControlCodes(styled);
        final String startBit = unstyled.substring(0, from);
        final String middleBit = unstyled.substring(from, to);
        final String sanitised = stipInternalControlCodes(styled);
        int start = from;

        while (!stipControlCodes(sanitised.substring(0, start)).equals(startBit)) {
            start++;
        }

        int end = to + start - from;

        while (!stipControlCodes(sanitised.substring(start, end)).equals(middleBit)) {
            end++;
        }

        return sanitised.substring(start, end);
    }

    /**
     * Applies the hyperlink styles and intelligent linking regexps to the
     * target.
     *
     * @param string The string to be linked
     * @return A copy of the string with hyperlinks marked up
     */
    public String doLinks(final String string) {
        String target = string;
        final String prefixes = server == null ? null
                : server.getChannelPrefixes();

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
     * @return A copy of the string with smilies marked up
     * @since 0.6.3m1
     */
    public String doSmilies(final String string) {
        // TODO: Check if they're enabled.
        // TODO: Store the list instead of building it every line

        final StringBuilder smilies = new StringBuilder();

        for (Map.Entry<String, String> icon
                : configManager.getOptions("icon").entrySet()) {
            if (icon.getKey().startsWith("smilie-")) {
                if (smilies.length() > 0) {
                    smilies.append('|');
                }

                smilies.append(Pattern.quote(icon.getKey().substring(7)));
            }
        }

        return string.replaceAll("(\\s|^)(" + smilies + ")(?=\\s|$)",
                "$1" + CODE_SMILIE + "$2" + CODE_SMILIE);
    }

    /**
     * Strips all recognised control codes from the input string.
     * @param input the String to be stripped
     * @return a copy of the input with control codes removed
     */
    public static String stipControlCodes(final String input) {
        return input.replaceAll("[" + CODE_BOLD + CODE_CHANNEL + CODE_FIXED
                + CODE_HYPERLINK + CODE_ITALIC + CODE_NEGATE + CODE_NICKNAME
                + CODE_SMILIE + CODE_STOP + CODE_UNDERLINE + "]|"
                + CODE_HEXCOLOUR + "([A-Za-z0-9]{6}(,[A-Za-z0-9]{6})?)?|"
                + CODE_COLOUR + "([0-9]{1,2}(,[0-9]{1,2})?)?", "")
                .replaceAll(CODE_TOOLTIP + ".*?" + CODE_TOOLTIP + "(.*?)"
                + CODE_TOOLTIP, "$1");
    }

    /**
     * St(r)ips all recognised internal control codes from the input string.
     *
     * @param input the String to be stripped
     * @return a copy of the input with control codes removed
     * @since 0.6.5
     */
    public static String stipInternalControlCodes(final String input) {
        return input.replaceAll("[" + CODE_CHANNEL + CODE_HYPERLINK + CODE_NICKNAME
                + CODE_SMILIE + CODE_STOP + CODE_UNDERLINE + "]", "")
                .replaceAll(CODE_TOOLTIP + ".*?" + CODE_TOOLTIP + "(.*?)"
                + CODE_TOOLTIP, "$1");
    }

    /**
     * Returns a substring of the input string such that no control codes are present
     * in the output. If the returned value isn't the same as the input, then the
     * character immediately after is a control character.
     * @param input The string to read from
     * @return A substring of the input containing no control characters
     */
    public static String readUntilControl(final String input) {
        int pos = input.length();

        pos = checkChar(pos, input.indexOf(CODE_BOLD));
        pos = checkChar(pos, input.indexOf(CODE_UNDERLINE));
        pos = checkChar(pos, input.indexOf(CODE_STOP));
        pos = checkChar(pos, input.indexOf(CODE_COLOUR));
        pos = checkChar(pos, input.indexOf(CODE_HEXCOLOUR));
        pos = checkChar(pos, input.indexOf(CODE_ITALIC));
        pos = checkChar(pos, input.indexOf(CODE_FIXED));
        pos = checkChar(pos, input.indexOf(CODE_HYPERLINK));
        pos = checkChar(pos, input.indexOf(CODE_NICKNAME));
        pos = checkChar(pos, input.indexOf(CODE_CHANNEL));
        pos = checkChar(pos, input.indexOf(CODE_SMILIE));
        pos = checkChar(pos, input.indexOf(CODE_NEGATE));
        pos = checkChar(pos, input.indexOf(CODE_TOOLTIP));

        return input.substring(0, pos);
    }

    /**
     * Helper function used in readUntilControl. Checks if i is a valid index of
     * the string (i.e., it's not -1), and then returns the minimum of pos and i.
     * @param pos The current position in the string
     * @param i The index of the first occurance of some character
     * @return The new position (see implementation)
     */
    private static int checkChar(final int pos, final int i) {
        if (i < pos && i != -1) { return i; }
        return pos;
    }

    /**
     * Reads the first control character from the input string (and any arguments
     * it takes), and applies it to the specified attribute set.
     * @return The number of characters read as control characters
     * @param string The string to read from
     * @param attribs The attribute set that new attributes will be applied to
     * @param isStart Whether this is at the start of the string or not
     */
    private int readControlChars(final String string,
            final SimpleAttributeSet attribs, final boolean isStart) {
        final boolean isNegated = attribs.containsAttribute("NegateControl", Boolean.TRUE);

        // Bold
        if (string.charAt(0) == CODE_BOLD) {
            if (!isNegated) {
                toggleAttribute(attribs, StyleConstants.FontConstants.Bold);
            }

            return 1;
        }

        // Underline
        if (string.charAt(0) == CODE_UNDERLINE) {
            if (!isNegated) {
                toggleAttribute(attribs, StyleConstants.FontConstants.Underline);
            }

            return 1;
        }

        // Italic
        if (string.charAt(0) == CODE_ITALIC) {
            if (!isNegated) {
                toggleAttribute(attribs, StyleConstants.FontConstants.Italic);
            }

            return 1;
        }

        // Hyperlinks
        if (string.charAt(0) == CODE_HYPERLINK) {
            if (!isNegated) {
                toggleURI(attribs);
            }

            if (attribs.getAttribute(IRCTextAttribute.HYPERLINK) == null) {
                attribs.addAttribute(IRCTextAttribute.HYPERLINK,
                        readUntilControl(string.substring(1)));
            } else {
                attribs.removeAttribute(IRCTextAttribute.HYPERLINK);
            }
            return 1;
        }

        // Channel links
        if (string.charAt(0) == CODE_CHANNEL) {
            if (!isNegated) {
                toggleChannel(attribs);
            }

            if (attribs.getAttribute(IRCTextAttribute.CHANNEL) == null) {
                attribs.addAttribute(IRCTextAttribute.CHANNEL,
                        readUntilControl(string.substring(1)));
            } else {
                attribs.removeAttribute(IRCTextAttribute.CHANNEL);
            }

            return 1;
        }

        // Nickname links
        if (string.charAt(0) == CODE_NICKNAME) {
            if (attribs.getAttribute(IRCTextAttribute.NICKNAME) == null) {
                attribs.addAttribute(IRCTextAttribute.NICKNAME,
                        readUntilControl(string.substring(1)));
            } else {
                attribs.removeAttribute(IRCTextAttribute.NICKNAME);
            }

            return 1;
        }

        // Fixed pitch
        if (string.charAt(0) == CODE_FIXED) {
            if (!isNegated) {
                if (attribs.containsAttribute(StyleConstants.FontConstants.FontFamily, "monospaced")) {
                    attribs.removeAttribute(StyleConstants.FontConstants.FontFamily);
                } else {
                    attribs.removeAttribute(StyleConstants.FontConstants.FontFamily);
                    attribs.addAttribute(StyleConstants.FontConstants.FontFamily, "monospaced");
                }
            }

            return 1;
        }

        // Stop formatting
        if (string.charAt(0) == CODE_STOP) {
            if (!isNegated) {
                resetAttributes(attribs);
            }

            return 1;
        }

        // Colours
        if (string.charAt(0) == CODE_COLOUR) {
            int count = 1;
            // This isn't too nice!
            if (string.length() > count && isInt(string.charAt(count))) {
                int foreground = string.charAt(count) - '0';
                count++;
                if (string.length() > count && isInt(string.charAt(count))) {
                    foreground = foreground * 10 + (string.charAt(count) - '0');
                    count++;
                }
                foreground = foreground % 16;

                if (!isNegated) {
                    setForeground(attribs, String.valueOf(foreground));
                    if (isStart) {
                        setDefaultForeground(attribs, String.valueOf(foreground));
                    }
                }

                // Now background
                if (string.length() > count && string.charAt(count) == ','
                        && string.length() > count + 1
                        && isInt(string.charAt(count + 1))) {
                    int background = string.charAt(count + 1) - '0';
                    count += 2; // Comma and first digit
                    if (string.length() > count && isInt(string.charAt(count))) {
                        background = background * 10 + (string.charAt(count) - '0');
                        count++;
                    }
                    background = background % 16;

                    if (!isNegated) {
                        setBackground(attribs, String.valueOf(background));
                        if (isStart) {
                            setDefaultBackground(attribs, String.valueOf(background));
                        }
                    }
                }
            } else if (!isNegated) {
                resetColour(attribs);
            }
            return count;
        }

        // Hex colours
        if (string.charAt(0) == CODE_HEXCOLOUR) {
            int count = 1;
            if (hasHexString(string, 1)) {
                if (!isNegated) {
                    setForeground(attribs, string.substring(1, 7).toUpperCase());
                    if (isStart) {
                        setDefaultForeground(attribs, string.substring(1, 7).toUpperCase());
                    }
                }

                count = count + 6;

                if (string.length() == count) {
                    return count;
                }
                // Now for background
                if (string.charAt(count) == ',' && hasHexString(string, count + 1)) {
                    count++;

                    if (!isNegated) {
                        setBackground(attribs, string.substring(count, count + 6).toUpperCase());
                        if (isStart) {
                            setDefaultBackground(attribs,
                                    string.substring(count, count + 6).toUpperCase());
                        }
                    }

                    count += 6;
                }
            } else if (!isNegated) {
                resetColour(attribs);
            }
            return count;
        }

        // Control code negation
        if (string.charAt(0) == CODE_NEGATE) {
            toggleAttribute(attribs, "NegateControl");
            return 1;
        }

        // Smilies!!
        if (string.charAt(0) == CODE_SMILIE) {
            if (attribs.getAttribute(IRCTextAttribute.SMILEY) == null) {
                final String smilie = readUntilControl(string.substring(1));

                attribs.addAttribute(IRCTextAttribute.SMILEY, "smilie-" + smilie);
            } else {
                attribs.removeAttribute(IRCTextAttribute.SMILEY);
            }

            return 1;
        }

        // Tooltips
        if (string.charAt(0) == CODE_TOOLTIP) {
            if (attribs.getAttribute(IRCTextAttribute.TOOLTIP) == null) {
                final int index = string.indexOf(CODE_TOOLTIP, 1);

                if (index == -1) {
                    // Doesn't make much sense, let's ignore it!
                    return 1;
                }

                final String tooltip = string.substring(1, index);

                attribs.addAttribute(IRCTextAttribute.TOOLTIP, tooltip);

                return tooltip.length() + 2;
            } else {
                attribs.removeAttribute(IRCTextAttribute.TOOLTIP);
            }

            return 1;
        }

        return 0;
    }

    /**
     * Determines if the specified character represents a single integer (i.e. 0-9).
     * @param c The character to check
     * @return True iff the character is in the range [0-9], false otherwise
     */
    private static boolean isInt(final char c) {
        return c >= '0' && c <= '9';
    }

    /**
     * Determines if the specified character represents a single hex digit
     * (i.e., 0-F).
     * @param c The character to check
     * @return True iff the character is in the range [0-F], false otherwise
     */
    private static boolean isHex(final char c) {
        return isInt(c) || (c >= 'A' && c <= 'F');
    }

    /**
     * Determines if the specified string has a 6-digit hex string starting at
     * the specified offset.
     * @param input The string to check
     * @param offset The offset to start at
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

    /**
     * Toggles the various channel link-related attributes.
     *
     * @param attribs The attributes to be modified.
     */
    private void toggleChannel(final SimpleAttributeSet attribs) {
        if (styleChannels) {
            toggleLink(attribs, IRCTextAttribute.CHANNEL, channelColour);
        }
    }

    /**
     * Toggles the various hyperlink-related attributes.
     *
     * @param attribs The attributes to be modified.
     */
    private void toggleURI(final SimpleAttributeSet attribs) {
        if (styleURIs) {
            toggleLink(attribs, IRCTextAttribute.HYPERLINK, uriColour);
        }
    }

    /**
     * Toggles the attributes for a link.
     *
     * @since 0.6.4
     * @param attribs The attributes to modify
     * @param attribute The attribute indicating whether the link is open or closed
     * @param colour The colour to colour the link
     */
    private void toggleLink(final SimpleAttributeSet attribs,
            final IRCTextAttribute attribute, final Color colour) {

        if (attribs.getAttribute(attribute) == null) {
            // Add the hyperlink style

            if (attribs.containsAttribute(StyleConstants.FontConstants.Underline, Boolean.TRUE)) {
                attribs.addAttribute("restoreUnderline", Boolean.TRUE);
            } else {
                attribs.addAttribute(StyleConstants.FontConstants.Underline, Boolean.TRUE);
            }

            if (colour != null) {
                final Object foreground = attribs.getAttribute(
                        StyleConstants.FontConstants.Foreground);

                if (foreground != null) {
                    attribs.addAttribute("restoreColour", foreground);
                    attribs.removeAttribute(StyleConstants.FontConstants.Foreground);
                }

                attribs.addAttribute(StyleConstants.FontConstants.Foreground, colour);
            }

        } else {
            // Remove the hyperlink style

            if (attribs.containsAttribute("restoreUnderline", Boolean.TRUE)) {
                attribs.removeAttribute("restoreUnderline");
            } else {
                attribs.removeAttribute(StyleConstants.FontConstants.Underline);
            }

            if (colour != null) {
                attribs.removeAttribute(StyleConstants.FontConstants.Foreground);
                final Object foreground = attribs.getAttribute("restoreColour");
                if (foreground != null) {
                    attribs.addAttribute(StyleConstants.FontConstants.Foreground, foreground);
                    attribs.removeAttribute("restoreColour");
                }
            }
        }
    }

    /**
     * Toggles the specified attribute. If the attribute exists in the attribute
     * set, it is removed. Otherwise, it is added with a value of Boolean.True.
     * @param attribs The attribute set to check
     * @param attrib The attribute to toggle
     */
    private static void toggleAttribute(final SimpleAttributeSet attribs,
            final Object attrib) {
        if (attribs.containsAttribute(attrib, Boolean.TRUE)) {
            attribs.removeAttribute(attrib);
        } else {
            attribs.addAttribute(attrib, Boolean.TRUE);
        }
    }

    /**
     * Resets all attributes in the specified attribute list.
     * @param attribs The attribute list whose attributes should be reset
     */
    private static void resetAttributes(final SimpleAttributeSet attribs) {
        if (attribs.containsAttribute(StyleConstants.FontConstants.Bold, Boolean.TRUE)) {
            attribs.removeAttribute(StyleConstants.FontConstants.Bold);
        }
        if (attribs.containsAttribute(StyleConstants.FontConstants.Underline, Boolean.TRUE)) {
            attribs.removeAttribute(StyleConstants.FontConstants.Underline);
        }
        if (attribs.containsAttribute(StyleConstants.FontConstants.Italic, Boolean.TRUE)) {
            attribs.removeAttribute(StyleConstants.FontConstants.Italic);
        }
        if (attribs.containsAttribute(StyleConstants.FontConstants.FontFamily, "monospaced")) {
            final Object defaultFont = attribs.getAttribute("DefaultFontFamily");
            attribs.removeAttribute(StyleConstants.FontConstants.FontFamily);
            attribs.addAttribute(StyleConstants.FontConstants.FontFamily, defaultFont);
        }
        resetColour(attribs);
    }

    /**
     * Resets the colour attributes in the specified attribute set.
     * @param attribs The attribute set whose colour attributes should be reset
     */
    private static void resetColour(final SimpleAttributeSet attribs) {
        if (attribs.isDefined(StyleConstants.Foreground)) {
            attribs.removeAttribute(StyleConstants.Foreground);
        }
        if (attribs.isDefined("DefaultForeground")) {
            attribs.addAttribute(StyleConstants.Foreground,
                    attribs.getAttribute("DefaultForeground"));
        }
        if (attribs.isDefined(StyleConstants.Background)) {
            attribs.removeAttribute(StyleConstants.Background);
        }
        if (attribs.isDefined("DefaultBackground")) {
            attribs.addAttribute(StyleConstants.Background,
                    attribs.getAttribute("DefaultBackground"));
        }
    }

    /**
     * Sets the foreground colour in the specified attribute set to the colour
     * corresponding to the specified colour code or hex.
     * @param attribs The attribute set to modify
     * @param foreground The colour code/hex of the new foreground colour
     */
    private static void setForeground(final SimpleAttributeSet attribs,
            final String foreground) {
        if (attribs.isDefined(StyleConstants.Foreground)) {
            attribs.removeAttribute(StyleConstants.Foreground);
        }
        attribs.addAttribute(StyleConstants.Foreground, ColourManager.parseColour(foreground));
    }

    /**
     * Sets the background colour in the specified attribute set to the colour
     * corresponding to the specified colour code or hex.
     * @param attribs The attribute set to modify
     * @param background The colour code/hex of the new background colour
     */
    private static void setBackground(final SimpleAttributeSet attribs,
            final String background) {
        if (attribs.isDefined(StyleConstants.Background)) {
            attribs.removeAttribute(StyleConstants.Background);
        }
        attribs.addAttribute(StyleConstants.Background, ColourManager.parseColour(background));
    }

    /**
     * Sets the default foreground colour (used after an empty ctrl+k or a ctrl+o).
     * @param attribs The attribute set to apply this default on
     * @param foreground The default foreground colour
     */
    private static void setDefaultForeground(final SimpleAttributeSet attribs,
            final String foreground) {
        attribs.addAttribute("DefaultForeground", ColourManager.parseColour(foreground));
    }

    /**
     * Sets the default background colour (used after an empty ctrl+k or a ctrl+o).
     * @param attribs The attribute set to apply this default on
     * @param background The default background colour
     */
    private static void setDefaultBackground(final SimpleAttributeSet attribs,
            final String background) {
        attribs.addAttribute("DefaultBackground", ColourManager.parseColour(background));
    }

    /** {@inheritDoc} */
    @Override
    public void configChanged(final String domain, final String key) {
        if ("stylelinks".equals(key)) {
            styleURIs = configManager.getOptionBool("ui", "stylelinks");
        } else if ("stylechannels".equals(key)) {
            styleChannels = configManager.getOptionBool("ui", "stylechannels");
        } else if ("linkcolour".equals(key)) {
            uriColour = configManager.getOptionColour("ui", "linkcolour");
        } else if ("channelcolour".equals(key)) {
            channelColour = configManager.getOptionColour("ui", "channelcolour");
        }
    }

}
