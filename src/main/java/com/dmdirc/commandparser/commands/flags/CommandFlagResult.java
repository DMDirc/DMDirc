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

package com.dmdirc.commandparser.commands.flags;

import com.dmdirc.commandparser.CommandArguments;

import java.util.Map;

/**
 * Convenient wrapper around the results of a {@link CommandFlagHandler}'s parsing routines.
 */
public class CommandFlagResult {

    /** The original arguments for the command. */
    private final CommandArguments arguments;
    /** The offsets of the first argument for each flag. */
    private final Map<CommandFlag, Integer> offsets;

    /**
     * Creates a new CommandFlagResult with the specified results.
     *
     * @param arguments The original arguments for the command
     * @param offsets   The offsets for each flag's arguments
     */
    public CommandFlagResult(final CommandArguments arguments,
            final Map<CommandFlag, Integer> offsets) {
        this.arguments = arguments;
        this.offsets = offsets;
    }

    /**
     * Determines if the specified command flag has been used when the command was executed.
     *
     * @param flag The flag to be checked
     *
     * @return True iff the flag was specified legally, false otherwise
     */
    public boolean hasFlag(final CommandFlag flag) {
        return offsets.containsKey(flag);
    }

    /**
     * Retrieves all of the arguments passed for the specified flag as a string.
     *
     * @param flag The flag to retrieve arguments for
     *
     * @return The arguments passed with the specified flag
     *
     * @see #getArguments(com.dmdirc.commandparser.commands.flags.CommandFlag)
     */
    public String getArgumentsAsString(final CommandFlag flag) {
        return flag == null ? (offsets.get(null) > arguments.getArguments().length
                ? "" : arguments.getArgumentsAsString(offsets.get(null)))
                : arguments.getArgumentsAsString(offsets.get(flag),
                        offsets.get(flag) + flag.getDelayedArgs() + flag.getImmediateArgs() - 1);
    }

    /**
     * Retrieves all arguments not associated with any flags as a string.
     *
     * @see CommandArguments#getArgumentsAsString()
     * @see #getArguments()
     * @return All arguments that aren't associated with a flag, as a string
     */
    public String getArgumentsAsString() {
        return getArgumentsAsString(null);
    }

    /**
     * Retrieves a subset of the arguments not associated with any flags, starting at the specified
     * offset.
     *
     * @see CommandArguments#getArgumentsAsString(int)
     * @param offset The offset of the argument to start at
     *
     * @return All arguments that aren't associated with a flag, as a string, starting with the
     *         argument at the specified offset
     */
    public String getArgumentsAsString(final int offset) {
        return arguments.getArgumentsAsString(offsets.get(null) + offset);
    }

    /**
     * Retrieves the arguments passed with the specified flag as an array.
     *
     * @param flag The flag to be checked
     *
     * @return The arguments passed with the specified flag
     *
     * @see #getArgumentsAsString(com.dmdirc.commandparser.commands.flags.CommandFlag)
     */
    public String[] getArguments(final CommandFlag flag) {
        final String[] args = arguments.getArguments();
        final int end = flag == null ? args.length
                : offsets.get(flag) + flag.getDelayedArgs() + flag.getImmediateArgs();
        final int offset = offsets.get(flag);
        final int size = end - offset;

        final String[] result = new String[size];
        System.arraycopy(args, offset, result, 0, size);

        return result;
    }

    /**
     * Retrieves all arguments not associated with any flags.
     *
     * @see CommandArguments#getArguments()
     * @see #getArgumentsAsString()
     * @return All arguments that aren't associated with a flag
     */
    public String[] getArguments() {
        return getArguments(null);
    }

}
