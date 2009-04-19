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

package com.dmdirc.harness;

import com.dmdirc.commandparser.CommandArguments;
import com.dmdirc.commandparser.parsers.*;
import com.dmdirc.commandparser.CommandManager;
import com.dmdirc.commandparser.CommandType;
import com.dmdirc.commandparser.commands.Command;
import com.dmdirc.ui.interfaces.InputWindow;

public class TestCommandParser extends CommandParser {
    private static final long serialVersionUID = 7073002401375438532L;

    public String nonCommandLine;

    public Command executedCommand;

    public boolean wasSilent;

    public CommandArguments commandArgs;

    public String invalidCommand;

    @Override
    protected void loadCommands() {
        CommandManager.loadCommands(this, CommandType.TYPE_GLOBAL);
    }

    @Override
    protected void executeCommand(InputWindow origin, boolean isSilent,
                                  Command command, CommandArguments args) {
        executedCommand = command;
        wasSilent = isSilent;
        commandArgs = args;
    }

    @Override
    protected void handleNonCommand(InputWindow origin, String line) {
        nonCommandLine = line;
    }

    @Override
    protected void handleInvalidCommand(InputWindow origin,
                                        CommandArguments args) {
        invalidCommand = args.getCommandName();
    }
}
