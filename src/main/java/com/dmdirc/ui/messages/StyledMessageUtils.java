package com.dmdirc.ui.messages;

import javax.inject.Inject;
import javax.inject.Singleton;

import static com.dmdirc.ui.messages.Styliser.CODE_CHANNEL;
import static com.dmdirc.ui.messages.Styliser.CODE_HYPERLINK;
import static com.dmdirc.ui.messages.Styliser.CODE_NICKNAME;
import static com.dmdirc.ui.messages.Styliser.CODE_SMILIE;
import static com.dmdirc.ui.messages.Styliser.CODE_TOOLTIP;
import static com.google.common.base.Preconditions.checkArgument;

/**
 * Utilities for dealing with styled messages.
 */
@Singleton
public class StyledMessageUtils {

    @Inject
    public StyledMessageUtils() {
    }

    /**
     * Strips all recognised control codes from the input string.
     *
     * @param input the String to be stripped
     *
     * @return a copy of the input with control codes removed
     */
    public String stripControlCodes(final String input) {
        return input.replaceAll("[" + IRCControlCodes.BOLD + CODE_CHANNEL + IRCControlCodes.FIXED
                + CODE_HYPERLINK + IRCControlCodes.ITALIC + IRCControlCodes.NEGATE + CODE_NICKNAME
                + CODE_SMILIE + IRCControlCodes.STOP + IRCControlCodes.UNDERLINE + "]|"
                + IRCControlCodes.COLOUR_HEX + "([A-Za-z0-9]{6}(,[A-Za-z0-9]{6})?)?|"
                + IRCControlCodes.COLOUR + "([0-9]{1,2}(,[0-9]{1,2})?)?", "")
                .replaceAll(CODE_TOOLTIP + ".*?" + CODE_TOOLTIP + "(.*?)" + CODE_TOOLTIP, "$1");
    }

    /**
     * Retrieves the styled String contained within the unstyled offsets specified. That is, the
     * <code>from</code> and <code>to</code> arguments correspond to indexes in an unstyled version
     * of the <code>styled</code> string. The unstyled indices are translated to offsets within the
     * styled String, and the return value includes all text and control codes between those
     * indices.
     * <p>
     * The index translation is left-biased; that is, the indices are translated to be as far left
     * as they possibly can be. This means that the start of the string will include any control
     * codes immediately preceding the desired text, and the end will not include any trailing
     * codes.
     * <p>
     * This method will NOT include "internal" control codes in the output.
     *
     * @param styled The styled String to be operated on
     * @param from   The starting index in the unstyled string
     * @param to     The ending index in the unstyled string
     *
     * @return The corresponding text between the two indices
     */
    public String getStyledText(final String styled, final int from, final int to) {
        checkArgument(from < to, "'from' (" + from + ") must be less than 'to' (" + to + ')');
        checkArgument(from >= 0, "'from' (" + from + ") must be non-negative");

        final String unstyled = stripControlCodes(styled);

        checkArgument(to <= unstyled.length(), "'to' (" + to + ") must be less than or equal to "
                + "the unstyled length (" + unstyled.length() + ')');

        final String startBit = unstyled.substring(0, from);
        final String middleBit = unstyled.substring(from, to);
        final String sanitised = stripInternalControlCodes(styled);
        int start = from;

        while (!stripControlCodes(sanitised.substring(0, start)).equals(startBit)) {
            start++;
        }

        int end = to + start - from;

        while (!stripControlCodes(sanitised.substring(start, end)).equals(middleBit)) {
            end++;
        }

        return sanitised.substring(start, end);
    }

    /**
     * Strips all recognised internal control codes from the input string.
     *
     * @param input the String to be stripped
     *
     * @return a copy of the input with control codes removed
     */
    private String stripInternalControlCodes(final String input) {
        return input.replaceAll("[" + CODE_CHANNEL + CODE_HYPERLINK + CODE_NICKNAME
                + CODE_SMILIE + IRCControlCodes.STOP + IRCControlCodes.UNDERLINE + ']', "")
                .replaceAll(CODE_TOOLTIP + ".*?" + CODE_TOOLTIP + "(.*?)"
                        + CODE_TOOLTIP, "$1");
    }

}
