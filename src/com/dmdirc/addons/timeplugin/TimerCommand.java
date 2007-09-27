/*
 * Copyright (c) 2006-2007 Chris Smith, Shane Mc Cormack, Gregory Holmes
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

package com.dmdirc.addons.timeplugin;

import com.dmdirc.commandparser.CommandManager;
import com.dmdirc.commandparser.GlobalCommand;
import com.dmdirc.ui.interfaces.InputWindow;

/**
 * The timer command allows users to schedule commands to occur after a certain
 * interval, or to repeatedly occur with a specified delay.
 * @author chris
 */
public final class TimerCommand extends GlobalCommand {
    
    /**
     * Creates a new instance of TimerCommand.
     */
    public TimerCommand() {
        CommandManager.registerCommand(this);
    }
    
    /** {@inheritDoc} */
    public void execute(final InputWindow origin, final boolean isSilent, 
            final String... args) {
        if (args.length < 3) {
            doUsage(origin, isSilent);
        } else {
            int repetitions = 0;
            int interval = 0;
            final String command = implodeArgs(2, args);
            
            try {
                repetitions = Integer.parseInt(args[0]);
                interval = Integer.parseInt(args[1]);
            } catch (NumberFormatException ex) {
                doUsage(origin, isSilent);
                return;
            }
            
            new TimedCommand(repetitions, interval, command, origin);
            sendLine(origin, isSilent, "commandOutput", "Command scheduled.");
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
    public String getName() {
        return "timer";
    }
    
    /** {@inheritDoc} */
    public boolean showInHelp() {
        return true;
    }
    
    /** {@inheritDoc} */
    public boolean isPolyadic() {
        return true;
    }
    
    /** {@inheritDoc} */
    public int getArity() {
        return 0;
    }
    
    /** {@inheritDoc} */
    public String getHelp() {
        return "timer <repetitions> <interval> <command> - schedules a command to be executed after a certain time";
    }
    
}
