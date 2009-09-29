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
package com.dmdirc.addons.dcc;

import com.dmdirc.commandparser.CommandArguments;
import com.dmdirc.commandparser.CommandManager;
import com.dmdirc.commandparser.CommandType;
import com.dmdirc.commandparser.commands.Command;
import com.dmdirc.commandparser.commands.GlobalCommand;
import com.dmdirc.commandparser.parsers.CommandParser;
import com.dmdirc.ui.interfaces.InputWindow;


/**
 * DCC CommandParser
 */
public class DCCCommandParser extends CommandParser {

    /** The singleton instance of the DCC command parser. */
    private static DCCCommandParser me;

    /** A version number for this class. */
    private static final long serialVersionUID = 2009290901;

    /**
     * Creates a new instance of the GlobalCommandParser.
     */
    private DCCCommandParser() {
        super();
    }

    /**
     * Retrieves the singleton dcc command parser.
     *
     * @return The singleton DCCCommandParser
     */
    public static synchronized DCCCommandParser getDCCCommandParser() {
        if (me == null) {
            me = new DCCCommandParser();
        }

        return me;
    }

    /** Loads the relevant commands into the parser. */
    @Override
    protected void loadCommands() {
        CommandManager.loadCommands(this, CommandType.TYPE_GLOBAL);
    }

    /**
     * Executes the specified command with the given arguments.
     *
     * @param origin The window in which the command was typed
     * @param isSilent Whether the command is being silenced or not
     * @param command The command to be executed
     * @param args The arguments to the command
     */
    @Override
    protected void executeCommand(final InputWindow origin, final boolean isSilent, final Command command, final CommandArguments args) {
        ((GlobalCommand) command).execute(origin, isSilent, args);
    }

    /**
     * Called when the input was a line of text that was not a command.
     * This normally means it is sent to the server/channel/user as-is, with
     * no further processing.
     *
     * @param origin The window in which the command was typed
     * @param line The line input by the user
     */
    @Override
    protected void handleNonCommand(final InputWindow origin, final String line) {
        origin.getContainer().sendLine(line);
    }
}
