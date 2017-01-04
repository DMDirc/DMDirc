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

package com.dmdirc.harness;

import com.dmdirc.commandparser.CommandArguments;
import com.dmdirc.commandparser.CommandInfo;
import com.dmdirc.commandparser.commands.Command;
import com.dmdirc.commandparser.commands.context.CommandContext;
import com.dmdirc.commandparser.parsers.BaseCommandParser;
import com.dmdirc.interfaces.CommandController;
import com.dmdirc.events.eventbus.EventBus;
import com.dmdirc.interfaces.WindowModel;
import com.dmdirc.interfaces.config.AggregateConfigProvider;

import javax.annotation.Nonnull;

public class TestCommandParser extends BaseCommandParser {

    private static final long serialVersionUID = 7073002401375438532L;

    public String nonCommandLine;

    public Command executedCommand;

    public boolean wasSilent;

    public CommandArguments commandArgs;

    public String invalidCommand;

    public TestCommandParser(final AggregateConfigProvider configManager,
            final CommandController commandManager, final EventBus eventBus) {
        super(configManager, commandManager, eventBus);
    }

    @Override
    protected void loadCommands() {
        // Do nothing
    }

    @Override
    protected void executeCommand(@Nonnull final WindowModel origin, final CommandInfo commandInfo, final Command command,
            final CommandArguments args, final CommandContext context) {
        executedCommand = command;
        wasSilent = args.isSilent();
        commandArgs = args;
    }

    @Override
    protected CommandContext getCommandContext(final WindowModel origin, final CommandInfo commandInfo,
            final Command command, final CommandArguments args) {
        return new CommandContext(origin, commandInfo);
    }

    @Override
    protected void handleNonCommand(final WindowModel origin, final String line) {
        nonCommandLine = line;
    }

    @Override
    protected void handleInvalidCommand(final WindowModel origin,
            final CommandArguments args) {
        invalidCommand = args.getCommandName();
    }

}
