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

package com.dmdirc.commandparser.auto;

import com.dmdirc.FrameContainer;
import com.dmdirc.GlobalWindow;
import com.dmdirc.commandparser.parsers.CommandParser;
import com.dmdirc.commandparser.parsers.GlobalCommandParser;
import com.dmdirc.events.ClientOpenedEvent;
import com.dmdirc.events.ServerConnectedEvent;
import com.dmdirc.interfaces.CommandController;
import com.dmdirc.interfaces.WindowModel;

import java.util.Optional;

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
        if (isGlobalCommand()) {
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
        if (appliesToServer(event.getConnection().getNetwork(),
                event.getConnection().getAddress(), event.getConnection().getProfile().getName())) {
            final WindowModel container = event.getConnection().getWindowModel();
            final CommandParser parser = container.getCommandParser();
            execute((FrameContainer) container, parser);
        }
    }

    private boolean appliesToServer(final String network, final String server,
            final String profile) {
        return !isGlobalCommand()
                && matchesIfPresent(autoCommand.getNetwork(), network)
                && matchesIfPresent(autoCommand.getServer(), server)
                && matchesIfPresent(autoCommand.getProfile(), profile);
    }

    private boolean isGlobalCommand() {
        return !autoCommand.getServer().isPresent() && !autoCommand.getNetwork().isPresent();
    }

    private boolean matchesIfPresent(final Optional<String> target, final String value) {
        return target.map(value::equalsIgnoreCase).orElse(true);
    }

    private void execute(final FrameContainer origin, final CommandParser parser) {
        for (String line : autoCommand.getResponse().split("\n")) {
            parser.parseCommand(origin, commandController.getCommandChar() + line);
        }
    }

}
