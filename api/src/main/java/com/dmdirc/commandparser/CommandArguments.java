/*
 * Copyright (c) 2006-2017 DMDirc Developers
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

package com.dmdirc.commandparser;

import com.dmdirc.interfaces.CommandController;

import java.util.Arrays;
import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.google.common.base.Preconditions.checkPositionIndex;

/**
 * Represents a command and its arguments. In this class, input is split into 'words' which are
 * separated by any number of whitespace characters; 'arguments' are the same but exclude the first
 * word, which will normally be the command name.
 *
 * @since 0.6.3m1
 */
public class CommandArguments {

    /** The raw line that was input. */
    private final String line;
    /** The line split into whitespace-delimited words. */
    private String[] words;
    /** Command controller to consult for command chars, etc. */
    private final CommandController controller;

    /**
     * Creates a new command arguments parser for the specified line.
     *
     * @param controller The command controller to consult for information about command characters,
     *                   etc.
     * @param line       The line to be parsed
     *
     * @since 0.6.7
     */
    public CommandArguments(final CommandController controller, final String line) {
        this.controller = controller;
        this.line = line;
    }

    /**
     * Creates a new command arguments parser for the specified words.
     *
     * @param controller The command controller to consult for information about command characters,
     *                   etc.
     * @param words      The words which form the line ot be parsed
     *
     * @since 0.6.7
     */
    public CommandArguments(final CommandController controller, final Collection<String> words) {
        this.controller = controller;
        this.words = words.toArray(new String[words.size()]);

        final StringBuilder builder = new StringBuilder();

        for (String word : words) {
            if (builder.length() > 0) {
                builder.append(' ');
            }

            builder.append(word);
        }

        this.line = builder.toString();
    }

    /**
     * Retrieves the raw line that was input, including any command character(s) and names.
     *
     * @return The raw line entered
     */
    public String getLine() {
        return line;
    }

    /**
     * Retrieves the raw line that was input, including the command name but stripped of any command
     * characters.
     *
     * @return The raw line entered, without command chars
     */
    public String getStrippedLine() {
        final int offset = isCommand() ? isSilent() ? 2 : 1 : 0;

        return line.substring(offset);
    }

    /**
     * Retrieves the input split into distinct, whitespace-separated words. The first item in the
     * array will be the command name complete with any command characters.
     *
     * @return An array of 'words' that make up the input
     */
    public String[] getWords() {
        parse();

        return words;
    }

    /**
     * Retrieves the arguments to the command split into distinct, whitespace-separated words.
     *
     * @return An array of 'words' that make up the command's arguments
     */
    public String[] getArguments() {
        parse();

        return Arrays.copyOfRange(words, Math.min(1, words.length), words.length);
    }

    /**
     * Retrieves all the arguments to the command (i.e., not including the command name) with their
     * original whitespace separation preserved.
     *
     * @return A String representation of the command arguments
     */
    public String getArgumentsAsString() {
        return getArgumentsAsString(0);
    }

    /**
     * Retrieves arguments to the command (i.e., not including the command name) starting with the
     * specified argument, with their original whitespace separation preserved.
     *
     * @param start The index of the first argument to include
     *
     * @return A String representation of the command arguments
     */
    public String getArgumentsAsString(final int start) {
        parse();

        return getArgumentsAsString(start, Math.max(start, words.length - 2));
    }

    /**
     * Retrieves arguments to the command (i.e., not including the command name) starting with the
     * specified argument, with their original whitespace separation preserved.
     *
     * @param start The index of the first argument to include
     * @param end   The index of the last argument to include
     *
     * @return A String representation of the command arguments
     */
    public String getArgumentsAsString(final int start, final int end) {
        return getWordsAsString(start + 1, end + 1);
    }

    /**
     * Retrieves the specified words with their original whitespace separation preserved.
     *
     * @param start The index of the first word to include (starting at 0)
     *
     * @return A String representation of the requested words
     */
    public String getWordsAsString(final int start) {
        parse();

        return getWordsAsString(start, words.length - 1);
    }

    /**
     * Retrieves the specified words with their original whitespace separation preserved.
     *
     * @param start The index of the first word to include (starting at 0)
     * @param end   The index of the last word to include
     *
     * @return A String representation of the requested words
     */
    public String getWordsAsString(final int start, final int end) {
        checkPositionIndex(start, end);

        final Pattern pattern = Pattern.compile("(\\S+\\s+){" + start + '}'
                + "((\\S+\\s+){" + Math.max(0, end - start) + "}\\S+(\\s+$)?).*?");
        final Matcher matcher = pattern.matcher(line);

        return matcher.matches() ? matcher.group(2) : "";
    }

    /**
     * Parses the input into a set of words, if it has not been done before.
     */
    protected synchronized void parse() {
        if (words == null) {
            words = line.split("\\s+");
        }
    }

    /**
     * Determines if the input was a command or not.
     *
     * @return True if the input was a command, false otherwise
     */
    public boolean isCommand() {
        return !line.isEmpty() && line.charAt(0) == controller.getCommandChar();
    }

    /**
     * Determines if the input was a silenced command or not.
     *
     * @return True if the input was a silenced command, false otherwise
     */
    public boolean isSilent() {
        return isCommand() && line.length() >= 2
                && line.charAt(1) == controller.getSilenceChar();
    }

    /**
     * Retrieves the name of the command that was used.
     *
     * @return The command name used
     */
    public String getCommandName() {
        final int offset = isCommand() ? isSilent() ? 2 : 1 : 0;
        return getWords()[0].substring(offset);
    }

}
