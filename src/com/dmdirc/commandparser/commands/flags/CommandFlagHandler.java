/*
 * Copyright (c) 2006-2013 DMDirc Developers
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

package com.dmdirc.commandparser.commands.flags;

import com.dmdirc.FrameContainer;
import com.dmdirc.commandparser.CommandArguments;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Utility class for commands which allow the user to specify shell-like
 * flags (<code>--foo</code>).
 *
 * @since 0.6.5
 */
public class CommandFlagHandler {

    /** A map of all known flag names to their flag objects. */
    private final Map<String, CommandFlag> flags = new HashMap<String, CommandFlag>();
    /** A map of currently enabled flag names to their flag objects. */
    private final Map<String, CommandFlag> enabledFlags = new HashMap<String, CommandFlag>();
    /** A map of currently disabled flag names to their flag objects. */
    private final Map<String, CommandFlag> disabledFlags = new HashMap<String, CommandFlag>();
    /** A map of disabled flag names to the flag objects that caused them to be disabled. */
    private final Map<String, CommandFlag> disabledBy = new HashMap<String, CommandFlag>();

    /**
     * Creates a new command flag handler which will handle all of the specified
     * flags.
     *
     * @param flags The flags that will be handled
     */
    public CommandFlagHandler(final CommandFlag ... flags) {
        for (CommandFlag flag : flags) {
            this.flags.put(flag.getName(), flag);
        }
    }

    /**
     * Processes the specified arguments and parses out command flags. If
     * the specified arguments aren't valid given the flags belonging to
     * this command flag handler, an error message is sent to the origin and
     * <code>null</code> is returned from this method.
     *
     * @param origin The container where the command was entered
     * @param arguments The arguments passed to the command
     * @return A corresponding {@link CommandFlagResult} object, or null
     * if some problem was encountered.
     */
    public CommandFlagResult process(final FrameContainer origin,
            final CommandArguments arguments) {
        final Map<CommandFlag, Integer> results = parse(origin, arguments);

        return results == null ? null : new CommandFlagResult(arguments, results);
    }

    /**
     * Parses the specified arguments and returns the offsets of the arguments
     * for each found command flag.
     *
     * @param origin The container where the command was entered
     * @param arguments The arguments passed to the command
     * @return A map of discovered command flags to the offset of the flag's
     * first argument within the <code>arguments</code> object. If an error
     * occurs, null is returned.
     */
    protected Map<CommandFlag, Integer> parse(final FrameContainer origin,
            final CommandArguments arguments) {
        enabledFlags.clear();
        disabledBy.clear();
        disabledFlags.clear();

        for (CommandFlag flag : flags.values()) {
            (flag.isEnabled() ? enabledFlags : disabledFlags).put(flag.getName(), flag);
        }

        final Map<CommandFlag, Integer> results = new HashMap<CommandFlag, Integer>();
        final List<CommandFlag> delayedFlags = new ArrayList<CommandFlag>(flags.size());

        int offset;
        for (offset = 0; offset < arguments.getArguments().length; offset++) {
            final String arg = arguments.getArguments()[offset];
            String name;

            if (arg.startsWith("--")
                    && flags.containsKey(name = arg.substring(2).toLowerCase())) {
                final CommandFlag flag = flags.get(name);

                if (enabledFlags.containsKey(name)) {
                    // It's enabled!
                    handleEnable(flag);

                    // Handle any immediate arguments
                    if ((offset = readArguments(flag, arguments, offset + 1,
                            flag.getImmediateArgs(), origin, results)) == -1) {
                        return null;
                    }

                    // Handle delayed arguments (if any)
                    if (flag.getDelayedArgs() > 0) {
                        delayedFlags.add(flag);
                    }
                } else if (disabledBy.containsKey(name)) {
                    // Disabled by another flag
                    sendLine(origin, arguments.isSilent(), "commandError",
                            "Cannot use flag --" + name
                            + " in conjunction with --" + disabledBy.get(name).getName());
                    return null;
                } else {
                    // Disabled because not yet enabled
                    sendLine(origin, arguments.isSilent(), "commandError",
                            "Cannot use flag --" + name
                            + " without " + getEnablers(flag));
                    return null;
                }
            } else {
                break;
            }
        }

        // Handle any stored delayed arguments
        for (CommandFlag flag : delayedFlags) {
            if ((offset = readArguments(flag, arguments, offset,
                    flag.getDelayedArgs(), origin, results)) == -1) {
                return null;
            }

            offset++;
        }

        results.put(null, offset);

        return results;
    }

    /**
     * Reads the arguments for the specified flag.
     *
     * @param flag The flag that is being read
     * @param arguments The raw arguments for the command
     * @param offset The index that the first argument will be at
     * @param argCount The number of arguments that need to be read
     * @param origin The source of the command (for error messages)
     * @param results The map to place results into
     * @return The index of the last argument that was handled, or -1 if
     * there were insufficient arguments for the flag
     */
    protected int readArguments(final CommandFlag flag,
            final CommandArguments arguments, final int offset, final int argCount,
            final FrameContainer origin, final Map<CommandFlag, Integer> results) {
        final int lastArg = argCount + offset - 1;

        if (arguments.getArguments().length <= lastArg) {
            sendLine(origin, arguments.isSilent(),
                    "commandError", "Flag --" + flag.getName() + " expects "
                    + argCount + " argument"
                    + (argCount == 1 ? "" : "s"));
            return -1;
        }

        results.put(flag, offset);

        return lastArg;
    }

    /**
     * Processes the enabled and disabled lists for the specified flag, and
     * adds them to the relevant properties.
     *
     * @param flag The flag whose enables/disables lists should be processed
     */
    protected void handleEnable(final CommandFlag flag) {
        for (CommandFlag target : flag.getDisables()) {
            if (enabledFlags.containsKey(target.getName())) {
                enabledFlags.remove(target.getName());
                disabledFlags.put(target.getName(), target);
                disabledBy.put(target.getName(), flag);
            }
        }

        for (CommandFlag target : flag.getEnables()) {
            if (disabledFlags.containsKey(target.getName())) {
                disabledFlags.remove(target.getName());
                enabledFlags.put(target.getName(), target);
                disabledBy.remove(target.getName());
            }
        }
    }

    /**
     * Constructs a user-friendly string describing the flag(s) which must
     * be used in order to enable the specified flag. This is useful for
     * error messages when the user tries to use a disabled flag.
     *
     * @param flag The flag to find enablers for
     * @return A user-friendly string describing flags which enable the
     * specified flag.
     */
    protected String getEnablers(final CommandFlag flag) {
        final List<CommandFlag> enablers = new LinkedList<CommandFlag>();

        for (CommandFlag target : flags.values()) {
            if (target.getEnables().contains(flag)) {
                enablers.add(target);
            }
        }

        if (enablers.size() == 1) {
            return "--" + enablers.get(0).getName();
        }

        final StringBuilder res = new StringBuilder("one of ");
        for (CommandFlag enabler : enablers) {
            res.append("--");
            res.append(enabler.getName());
            res.append(", ");
        }

        return res.substring(0, res.length() - 2);
    }

    /**
     * Convenience method to send a line to the specified frame container.
     *
     * @param origin The container to send the line to
     * @param isSilent Whether the command is silenced or not
     * @param messageType The type of the line to be sent
     * @param args The arguments for the specified messageType
     */
    protected static void sendLine(final FrameContainer origin, final boolean isSilent,
            final String messageType, final Object ... args) {
        if (origin != null && !isSilent) {
            origin.addLine(messageType, args);
        }
    }
}
