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

package com.dmdirc.plugins.implementations;

import com.dmdirc.ClientModule;
import com.dmdirc.interfaces.CommandController;

import javax.inject.Inject;

import dagger.Module;

/**
 * Helper class for {@link BaseCommandPlugin} to facilitate retrieving a {@link CommandController}.
 *
 * <p>
 * Because the plugins themselves aren't dependency injected, but require a command controller
 * themselves to sensibly register commands, we use this helper and module to obtain a reference.
 */
public class CommandHelper {

    /** The command controller. */
    private final CommandController commandController;

    /**
     * Creates a new instance of this class.
     *
     * @param commandController The command controller to return.
     */
    @Inject
    public CommandHelper(final CommandController commandController) {
        this.commandController = commandController;
    }

    public CommandController getCommandController() {
        return commandController;
    }

    /** Module that allows inflation of {@link CommandHelper}. */
    @Module(injects = CommandHelper.class, addsTo = ClientModule.class)
    public static class CommandHelperModule {
    }

}
