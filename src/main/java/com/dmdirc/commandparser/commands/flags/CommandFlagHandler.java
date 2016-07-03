/*
 * Copyright (c) 2006-2015 DMDirc Developers
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

import com.dmdirc.commandparser.CommandArguments;
import com.dmdirc.events.CommandErrorEvent;
import com.dmdirc.interfaces.WindowModel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

/**
 * Utility class for commands which allow the user to specify shell-like flags (
 * <code>--foo</code>).
 *
 * @since 0.6.5
 */
public class CommandFlagHandler {

    /** A map of all known flag names to their flag objects. */
    private final Map<String, CommandFlag> flags = new HashMap<>();
    /** A map of currently enabled flag names to their flag objects. */
    private final Map<String, CommandFlag> enabledFlags = new HashMap<>();
    /** A map of currently disabled flag names to their flag objects. */
    private final Map<String, CommandFlag> disabledFlags = new HashMap<>();
    /** A map of disabled flag names to the flag objects that caused them to be disabled. */
    private final Map<String, CommandFlag> disabledBy = new HashMap<>();

    /**
     * Creates a new command flag handler which will handle all of the specified flags.
     *
     * @param flags The flags that will be handled
     */
    public CommandFlagHandler(final CommandFlag... flags) {
        for (CommandFlag flag : flags) {
            this.flags.put(flag.getName(), flag);
        }
    }

    /**
     * Processes the specified arguments and parses out command flags. If the specified arguments
     * aren't valid given the flags belonging to this command flag handler, an error message is sent
     * to the origin and <code>null</code> is returned from this method.
     *
     * @param origin    The container where the command was entered
     * @param arguments The arguments passed to the command
     *
     * @return A corresponding {@link CommandFlagResult} object, or null if some problem was
     *         encountered.
     */
    @Nullable
    public CommandFlagResult process(final WindowModel origin,
            final CommandArguments arguments) {
        final Map<CommandFlag, Integer> results = parse(origin, arguments);

        return results == null ? null : new CommandFlagResult(arguments, results);
    }

    /**
     * Parses the specified arguments and returns the offsets of the arguments for each found
     * command flag.
     *
     * @param origin    The container where the command was entered
     * @param arguments The arguments passed to the command
     *
     * @return A map of discovered command flags to the offset of the flag's first argument within
     *         the <code>arguments</code> object. If an error occurs, null is returned.
     */
    @Nullable
    protected Map<CommandFlag, Integer> parse(final WindowModel origin,
            final CommandArguments arguments) {
        enabledFlags.clear();
        disabledBy.clear();
        disabledFlags.clear();

        for (CommandFlag flag : flags.values()) {
            (flag.isEnabled() ? enabledFlags : disabledFlags).put(flag.getName(), flag);
        }

        final Map<CommandFlag, Integer> results = new HashMap<>();
        final Collection<CommandFlag> delayedFlags = new ArrayList<>(flags.size());

        int offset;
        for (offset = 0; offset < arguments.getArguments().length; offset++) {
            final String arg = arguments.getArguments()[offset];
            final String name;

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
                    sendError(origin, arguments.isSilent(),
                            "Cannot use flag --" + name
                            + " in conjunction with --" + disabledBy.get(name).getName());
                    return null;
                } else {
                    // Disabled because not yet enabled
                    sendError(origin, arguments.isSilent(),
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
     * @param flag      The flag that is being read
     * @param arguments The raw arguments for the command
     * @param offset    The index that the first argument will be at
     * @param argCount  The number of arguments that need to be read
     * @param origin    The source of the command (for error messages)
     * @param results   The map to place results into
     *
     * @return The index of the last argument that was handled, or -1 if there were insufficient
     *         arguments for the flag
     */
    protected int readArguments(final CommandFlag flag,
            final CommandArguments arguments, final int offset, final int argCount,
            final WindowModel origin, final Map<CommandFlag, Integer> results) {
        final int lastArg = argCount + offset - 1;

        if (arguments.getArguments().length <= lastArg) {
            sendError(origin, arguments.isSilent(),
                    "Flag --" + flag.getName() + " expects "
                    + argCount + " argument"
                    + (argCount == 1 ? "" : "s"));
            return -1;
        }

        results.put(flag, offset);

        return lastArg;
    }

    /**
     * Processes the enabled and disabled lists for the specified flag, and adds them to the
     * relevant properties.
     *
     * @param flag The flag whose enables/disables lists should be processed
     */
    protected void handleEnable(final CommandFlag flag) {
        flag.getDisables().stream().filter(target -> enabledFlags.containsKey(target.getName()))
                .forEach(target -> {
                    enabledFlags.remove(target.getName());
                    disabledFlags.put(target.getName(), target);
                    disabledBy.put(target.getName(), flag);
                });

        flag.getEnables().stream().filter(target -> disabledFlags.containsKey(target.getName()))
                .forEach(target -> {
                    disabledFlags.remove(target.getName());
                    enabledFlags.put(target.getName(), target);
                    disabledBy.remove(target.getName());
                });
    }

    /**
     * Constructs a user-friendly string describing the flag(s) which must be used in order to
     * enable the specified flag. This is useful for error messages when the user tries to use a
     * disabled flag.
     *
     * @param flag The flag to find enablers for
     *
     * @return A user-friendly string describing flags which enable the specified flag.
     */
    protected String getEnablers(final CommandFlag flag) {
        final List<CommandFlag> enablers =
                flags.values().stream().filter(target -> target.getEnables().contains(flag))
                        .collect(Collectors.toCollection(LinkedList::new));

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
     * Convenience method to send a command error event.
     *
     * @param origin      The container to send the event to
     * @param isSilent    Whether the command is silenced or not
     * @param message     The error message
     */
    private static void sendError(final WindowModel origin, final boolean isSilent,
            final String message) {
        if (origin != null && !isSilent) {
            origin.getEventBus().publishAsync(new CommandErrorEvent(origin, message));
        }
    }

}
