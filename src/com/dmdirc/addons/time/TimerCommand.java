/*
 * Copyright (c) 2006-2010 Chris Smith, Shane Mc Cormack, Gregory Holmes
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

package com.dmdirc.addons.time;

import com.dmdirc.commandparser.CommandArguments;
import com.dmdirc.commandparser.CommandManager;
import com.dmdirc.commandparser.commands.GlobalCommand;
import com.dmdirc.commandparser.commands.IntelligentCommand;
import com.dmdirc.ui.input.AdditionalTabTargets;
import com.dmdirc.ui.input.TabCompleter;
import com.dmdirc.ui.interfaces.InputWindow;
import java.util.List;

/**
 * The timer command allows users to schedule commands to occur after a certain
 * interval, or to repeatedly occur with a specified delay.
 * @author chris
 */
public final class TimerCommand extends GlobalCommand implements IntelligentCommand {
    
    /**
     * Creates a new instance of TimerCommand.
     */
    public TimerCommand() {
        CommandManager.registerCommand(this);
    }
    
    /** {@inheritDoc} */
    @Override
    public void execute(final InputWindow origin, final boolean isSilent, 
            final CommandArguments args) {
        if (args.getArguments().length < 3) {
            doUsage(origin, isSilent);
        } else {
            int repetitions = 0;
            int interval = 0;
            final String command = args.getArgumentsAsString(2);
            
            try {
                repetitions = Integer.parseInt(args.getArguments()[0]);
                interval = Integer.parseInt(args.getArguments()[1]);
            } catch (NumberFormatException ex) {
                doUsage(origin, isSilent);
                return;
            }
            
            new TimedCommand(repetitions, interval, command, origin);
            sendLine(origin, isSilent, FORMAT_OUTPUT, "Command scheduled.");
        }
    }
    
    /**
     * Displays usage information for this command.
     * @param origin The window that the command was entered in
     * @param isSilent Whether this command is being silenced or not
     */
    private void doUsage(final InputWindow origin, final boolean isSilent) {
        showUsage(origin, isSilent, "timer", "<repetitions> <interval> <command>");
    }
    
    /** {@inheritDoc} */
    @Override
    public String getName() {
        return "timer";
    }
    
    /** {@inheritDoc} */
    @Override
    public boolean showInHelp() {
        return true;
    }
    
    /** {@inheritDoc} */
    @Override
    public String getHelp() {
        return "timer <repetitions> <interval> <command> " +
                "- schedules a command to be executed after a certain time";
    }

    /** {@inheritDoc} */
    @Override
    public AdditionalTabTargets getSuggestions(final int arg, final List<String> previousArgs) {
        AdditionalTabTargets res;
        
        if (arg <= 1) {
            res = new AdditionalTabTargets().excludeAll();
        } else {
            res = TabCompleter.getIntelligentResults(arg, previousArgs, 2);
        }
        
        return res;
    } 
    
}
