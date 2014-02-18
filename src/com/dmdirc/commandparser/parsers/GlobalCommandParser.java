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

package com.dmdirc.commandparser.parsers;

import com.dmdirc.ClientModule;
import com.dmdirc.FrameContainer;
import com.dmdirc.commandparser.CommandArguments;
import com.dmdirc.commandparser.CommandInfo;
import com.dmdirc.commandparser.CommandType;
import com.dmdirc.commandparser.commands.Command;
import com.dmdirc.commandparser.commands.context.CommandContext;
import com.dmdirc.interfaces.CommandController;
import com.dmdirc.interfaces.config.AggregateConfigProvider;
import com.dmdirc.logger.ErrorLevel;
import com.dmdirc.logger.Logger;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * The command parser used for global commands.
 */
@Singleton
public class GlobalCommandParser extends CommandParser {

    /**
     * A version number for this class. It should be changed whenever the class structure is changed
     * (or anything else that would prevent serialized objects being unserialized with the new
     * class).
     */
    private static final long serialVersionUID = 1;

    /**
     * Creates a new command parser for global commands.
     *
     * @param configManager  Config manager to read settings from
     * @param commandManager Command manager to load commands from
     */
    @Inject
    public GlobalCommandParser(
            @ClientModule.GlobalConfig final AggregateConfigProvider configManager,
            final CommandController commandManager) {
        super(configManager, commandManager);
    }

    /** {@inheritDoc} */
    @Override
    public void setOwner(final FrameContainer owner) {
        // Don't care
    }

    /** Loads the relevant commands into the parser. */
    @Override
    protected void loadCommands() {
        commandManager.loadCommands(this, CommandType.TYPE_GLOBAL);
    }

    @Override
    protected CommandContext getCommandContext(
            final FrameContainer origin,
            final CommandInfo commandInfo,
            final Command command,
            final CommandArguments args) {
        return new CommandContext(origin, commandInfo);
    }

    @Override
    protected void executeCommand(
            final FrameContainer origin,
            final CommandInfo commandInfo,
            final Command command,
            final CommandArguments args,
            final CommandContext context) {
        command.execute(origin, args, context);
    }

    /**
     * Called when the input was a line of text that was not a command. This normally means it is
     * sent to the server/channel/user as-is, with no further processing.
     *
     * @param origin The window in which the command was typed
     * @param line   The line input by the user
     */
    @Override
    protected void handleNonCommand(final FrameContainer origin, final String line) {
        if (origin == null) {
            Logger.userError(ErrorLevel.MEDIUM, "Invalid global command: " + line);
        } else {
            origin.addLine("commandError", "Invalid global command: " + line);
        }
    }

}
