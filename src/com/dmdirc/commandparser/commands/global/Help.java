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

package com.dmdirc.commandparser.commands.global;

import com.dmdirc.commandparser.BaseCommandInfo;
import com.dmdirc.commandparser.CommandArguments;
import com.dmdirc.commandparser.CommandInfo;
import com.dmdirc.commandparser.CommandType;
import com.dmdirc.commandparser.commands.Command;
import com.dmdirc.commandparser.commands.IntelligentCommand;
import com.dmdirc.commandparser.commands.context.CommandContext;
import com.dmdirc.interfaces.CommandController;
import com.dmdirc.interfaces.WindowModel;
import com.dmdirc.ui.input.AdditionalTabTargets;
import com.dmdirc.ui.input.TabCompletionType;
import com.dmdirc.ui.messages.Styliser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.inject.Inject;

/**
 * The help command shows the user a list of available commands, along with their arguments, and a
 * description. It is context-aware, so channel commands are only displayed when in a channel
 * window, for example.
 */
public class Help extends Command implements IntelligentCommand {

    /** A command info object for this command. */
    public static final CommandInfo INFO = new BaseCommandInfo("help",
            "help [command] - shows client command help",
            CommandType.TYPE_GLOBAL);

    /**
     * Creates a new instance of this command.
     *
     * @param controller The controller to use for command information.
     */
    @Inject
    public Help(final CommandController controller) {
        super(controller);
    }

    @Override
    public void execute(@Nonnull final WindowModel origin,
            final CommandArguments args, final CommandContext context) {
        if (args.getArguments().length == 0) {
            showAllCommands(origin, args.isSilent());
        } else {
            showCommand(origin, args.isSilent(), args.getArguments()[0]);
        }
    }

    /**
     * Shows a list of all commands valid for the current window.
     *
     * @param origin   The window the command was executed in
     * @param isSilent Whether this command has been silenced or not
     */
    private void showAllCommands(final WindowModel origin, final boolean isSilent) {
        final List<String> commands = new ArrayList<>(origin.getInputModel().get()
                .getCommandParser().getCommands().keySet());

        Collections.sort(commands);

        showOutput(origin, isSilent, Styliser.CODE_FIXED
                + "----------------------- Available commands -------");

        final StringBuilder builder = new StringBuilder();

        for (String command : commands) {
            if (builder.length() + command.length() + 1 > 50) {
                showOutput(origin, isSilent, Styliser.CODE_FIXED + builder.toString());
                builder.delete(0, builder.length());
            } else if (builder.length() > 0) {
                builder.append(' ');
            }

            builder.append(command);
        }

        if (builder.length() > 0) {
            showOutput(origin, isSilent, Styliser.CODE_FIXED + builder.toString());
        }

        showOutput(origin, isSilent, Styliser.CODE_FIXED
                + "--------------------------------------------------");
    }

    /**
     * Shows information about the specified command.
     *
     * @param origin   The window the command was executed in
     * @param isSilent Whether this command has been silenced or not
     * @param name     The name of the command to display info for
     */
    private void showCommand(final WindowModel origin, final boolean isSilent,
            final String name) {
        final Map.Entry<CommandInfo, Command> command;

        if (!name.isEmpty() && name.charAt(0) == getController().getCommandChar()) {
            command = getController().getCommand(name.substring(1));
        } else {
            command = getController().getCommand(name);
        }

        if (command == null) {
            showError(origin, isSilent, "Command '" + name + "' not found.");
        } else {
            showOutput(origin, isSilent, Styliser.CODE_FIXED
                    + "---------------------- Command information -------");
            showOutput(origin, isSilent, Styliser.CODE_FIXED
                    + " Name: " + name);
            showOutput(origin, isSilent, Styliser.CODE_FIXED
                    + " Type: " + command.getKey().getType());
            showOutput(origin, isSilent, Styliser.CODE_FIXED
                    + "Usage: " + command.getKey().getHelp());
            showOutput(origin, isSilent, Styliser.CODE_FIXED
                    + "--------------------------------------------------");
        }
    }

    @Override
    public AdditionalTabTargets getSuggestions(final int arg,
            final IntelligentCommandContext context) {
        final AdditionalTabTargets res = new AdditionalTabTargets().excludeAll();

        if (arg == 0) {
            res.include(TabCompletionType.COMMAND);
        }

        return res;
    }

}
