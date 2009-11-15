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

package com.dmdirc.commandparser;

import com.dmdirc.Precondition;
import com.dmdirc.logger.Logger;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Represents a command and its arguments. In this class, input is split into
 * 'words' which are separated by any number of whitespace characters;
 * 'arguments' are the same but exclude the first word, which will normally be
 * the command name.
 *
 * @since 0.6.3m1
 * @author chris
 */
public class CommandArguments {

    /** The raw line that was input. */
    private final String line;

    /** The line split into whitespace-delimited words. */
    private String[] words;

    /**
     * Creates a new command arguments parser for the specified line.
     *
     * @param line The line to be parsed
     */
    public CommandArguments(final String line) {
        this.line = line;
    }

    /**
     * Retrieves the raw line that was input, including any command character(s)
     * and names.
     *
     * @return The raw line entered
     */
    public String getLine() {
        return line;
    }

    /**
     * Retrieves the raw line that was input, including the command name but
     * stripped of any command characters.
     *
     * @return The raw line entered, without command chars
     */
    public String getStrippedLine() {
        final int offset = isCommand() ? isSilent() ? 2 : 1 : 0;

        return line.substring(offset);
    }

    /**
     * Retrieves the input split into distinct, whitespace-separated words. The
     * first item in the array will be the command name complete with any
     * command characters.
     *
     * @return An array of 'words' that make up the input
     */
    public String[] getWords() {
        parse();
        
        return words;
    }

    /**
     * Retrieves the arguments to the command split into disticnt,
     * whitespace-separated words.
     *
     * @return An array of 'words' that make up the command's arguments
     */
    public String[] getArguments() {
        parse();

        return Arrays.copyOfRange(words, 1, words.length);
    }

    /**
     * Retrieves all the arguments to the command (i.e., not including the
     * command name) with their original whitespace separation preserved.
     *
     * @return A String representation of the command arguments
     */
    public String getArgumentsAsString() {
        parse();
        
        return getArgumentsAsString(0);
    }

    /**
     * Retrieves arguments to the command (i.e., not including the
     * command name) starting with the specified argument, with their original
     * whitespace separation preserved.
     *
     * @param start The index of the first argument to include
     * @return A String representation of the command arguments
     */
    public String getArgumentsAsString(final int start) {
        parse();

        return getWordsAsString(start + 1);
    }

    /**
     * Retrieves the specified words with their original whitespace separation
     * preserved.
     *
     * @param start The index of the first word to include (starting at 0)
     * @return A String representation of the requested words
     */
    public String getWordsAsString(final int start) {
        return getWordsAsString(start, words.length);
    }

    /**
     * Retrieves the specified words with their original whitespace separation
     * preserved.
     *
     * @param start The index of the first word to include (starting at 0)
     * @param end The index of the last word to include
     * @return A String representation of the requested words
     */
    @Precondition("Start index is less than or equal to end index")
    public String getWordsAsString(final int start, final int end) {
        Logger.assertTrue(start <= end);

        final Pattern pattern = Pattern.compile("(\\S+\\s*){" + (start) + "}"
                + "((\\S+\\s*){" + (end - start) + "}).*?");
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
        return !line.isEmpty() && line.charAt(0) == CommandManager.getCommandChar();
    }

    /**
     * Determines if the input was a silenced command or not.
     *
     * @return True if the input was a silenced command, false otherwise
     */
    public boolean isSilent() {
        return isCommand() && line.length() >= 2 &&
                line.charAt(1) == CommandManager.getSilenceChar();
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
