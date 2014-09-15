/*
 * Copyright (c) 2006-2014 DMDirc Developers
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

package com.dmdirc.commandparser.auto;

import com.dmdirc.FrameContainer;
import com.dmdirc.GlobalWindow;
import com.dmdirc.commandparser.parsers.CommandParser;
import com.dmdirc.commandparser.parsers.GlobalCommandParser;
import com.dmdirc.events.ClientOpenedEvent;
import com.dmdirc.events.ServerConnectedEvent;
import com.dmdirc.interfaces.CommandController;

import net.engio.mbassy.listener.Handler;

/**
 * Handles execution of {@link AutoCommand}s.
 */
public class AutoCommandHandler {

    private final CommandController commandController;
    private final GlobalCommandParser globalCommandParser;
    private final GlobalWindow globalWindow;
    private final AutoCommand autoCommand;

    public AutoCommandHandler(
            final CommandController commandController,
            final GlobalCommandParser globalCommandParser,
            final GlobalWindow globalWindow,
            final AutoCommand autoCommand) {
        this.commandController = commandController;
        this.globalCommandParser = globalCommandParser;
        this.globalWindow = globalWindow;
        this.autoCommand = autoCommand;
    }

    /**
     * Handles auto-commands that respond to the client opening.
     *
     * @param event The event triggering the command.
     */
    @Handler
    public void checkAutoCommand(final ClientOpenedEvent event) {
        if (!autoCommand.getServer().isPresent() && !autoCommand.getNetwork().isPresent()) {
            execute(globalWindow, globalCommandParser);
        }
    }

    /**
     * Handles auto-commands that respond to a server connecting.
     *
     * @param event The event triggering the command.
     */
    @Handler
    public void checkAutoCommand(final ServerConnectedEvent event) {
        if (!autoCommand.getServer().isPresent() && !autoCommand.getNetwork().isPresent()) {
            // This is a global auto command, shouldn't be executed for servers.
            return;
        }

        if (autoCommand.getProfile().isPresent() && !event.getConnection().getProfile().getName()
                .equalsIgnoreCase(autoCommand.getProfile().get())) {
            // There's a profile specified in the command that isn't matched
            return;
        }

        if (autoCommand.getServer().isPresent() && !event.getConnection().getAddress()
                .equalsIgnoreCase(autoCommand.getServer().get())) {
            // There's a server specified in the command that isn't matched
            return;
        }


        if (autoCommand.getNetwork().isPresent() && !event.getConnection().getNetwork()
                .equalsIgnoreCase(autoCommand.getNetwork().get())) {
            // There's a network specified in the command that isn't matched
            return;
        }

        final FrameContainer container = event.getConnection().getWindowModel();
        final CommandParser parser = container.getCommandParser();
        execute(container, parser);
    }

    private void execute(final FrameContainer origin, final CommandParser parser) {
        for (String line : autoCommand.getResponse().split("\n")) {
            parser.parseCommand(origin, commandController.getCommandChar() + line);
        }
    }

}
