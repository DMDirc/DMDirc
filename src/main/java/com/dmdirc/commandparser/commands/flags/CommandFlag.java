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

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Describes a flag that may be used in a command. Each flag may be enabled or disabled initially,
 * can specify a number of immediate arguments (which must follow the flag directly), a number of
 * delayed arguments (which are specified after any other flags), and a list of other flags that the
 * use of this one causes to be enabled or disabled.
 *
 * @since 0.6.5
 * @see CommandFlagHandler
 */
public class CommandFlag {

    /** The name of the flag. */
    private final String name;
    /** The list of flags that become enabled if this one is used. */
    private final List<CommandFlag> enables = new LinkedList<>();
    /** The list of flags that become disabled if this one is used. */
    private final List<CommandFlag> disables = new LinkedList<>();
    /** The number of args expected following this flag. */
    private final int immediateArgs;
    /** The number of args expected after flags are finished. */
    private final int delayedArgs;
    /** The initial state of this flag. */
    private final boolean enabled;

    /**
     * Creates a new enabled flag with the specified name and no arguments.
     *
     * @param name The name of this flag
     */
    public CommandFlag(final String name) {
        this(name, true);
    }

    /**
     * Creates a new flag with the specified name and no arguments.
     *
     * @param name    The name of this flag
     * @param enabled Whether or not this flag is initially enabled
     */
    public CommandFlag(final String name, final boolean enabled) {
        this(name, enabled, 0, 0);
    }

    /**
     * Creates a new flag with the specified name and the specified number of arguments. Flags may
     * only use immediate OR delayed arguments, not both.
     *
     * @param name          The name of this flag
     * @param enabled       Whether or not this flag is initially enabled
     * @param immediateArgs The number of immediate arguments
     * @param delayedArgs   The number of delayed arguments
     */
    public CommandFlag(final String name, final boolean enabled,
            final int immediateArgs, final int delayedArgs) {
        this.name = name;
        this.immediateArgs = immediateArgs;
        this.delayedArgs = delayedArgs;
        this.enabled = enabled;

        if (delayedArgs > 0 && immediateArgs > 0) {
            throw new IllegalArgumentException("May not use both delayed and immediate arguments");
        }
    }

    /**
     * Indicates that the specified flags will be enabled if this flag is used.
     *
     * @param enabled The flags which will be enabled
     */
    public void addEnabled(final CommandFlag... enabled) {
        enables.addAll(Arrays.asList(enabled));
    }

    /**
     * Indicates that the specified flags will be disabled if this flag is used.
     *
     * @param disabled The flags which will be disabled
     */
    public void addDisabled(final CommandFlag... disabled) {
        disables.addAll(Arrays.asList(disabled));
    }

    /**
     * Retrieves the number of delayed arguments required by this flag.
     *
     * @return The number of delayed arguments
     */
    public int getDelayedArgs() {
        return delayedArgs;
    }

    /**
     * Retrieves the set of flags which become disabled if this one is used.
     *
     * @return A set of flags disabled by the use of this one
     */
    public Collection<CommandFlag> getDisables() {
        return Collections.unmodifiableCollection(disables);
    }

    /**
     * Determines whether or not this flag is enabled initially.
     *
     * @return The initial enabled/disabled state of this action
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Retrieves the set of flags which become enabled if this one is used.
     *
     * @return A set of flags enabled by the use of this one
     */
    public Collection<CommandFlag> getEnables() {
        return Collections.unmodifiableCollection(enables);
    }

    /**
     * Retrieves the number of immediate arguments required by this flag.
     *
     * @return The number of immediate arguments
     */
    public int getImmediateArgs() {
        return immediateArgs;
    }

    /**
     * Retrieves the name of this flag.
     *
     * @return This flag's name
     */
    public String getName() {
        return name;
    }

}
