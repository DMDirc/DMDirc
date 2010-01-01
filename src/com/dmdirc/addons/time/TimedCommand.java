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

import com.dmdirc.commandparser.parsers.CommandParser;
import com.dmdirc.commandparser.parsers.GlobalCommandParser;
import com.dmdirc.ui.interfaces.InputWindow;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Timed command represents a command that has been scheduled by the user.
 */
public final class TimedCommand extends TimerTask {
    
    /** The number of repetitions remaining. */
    private int repetitions;
    
    /** The command to execute. */
    private final String command;
    
    /** The window to use for executing commands. */
    private final InputWindow origin;
    
    /** The timer we're using for scheduling this command. */
    private final Timer timer;
    
    /**
     * Creates a new instance of TimedCommand.
     * @param repetitions The number of times this command will be executed
     * @param delay The number of seconds between each execution
     * @param command The command to be executed
     * @param origin The command window to use for the execution
     */
    public TimedCommand(final int repetitions, final int delay,
            final String command, final InputWindow origin) {
        super();
        
        this.repetitions = repetitions;
        this.command = command;
        this.origin = origin;
        
        timer = new Timer("Timed Command Timer");
        timer.schedule(this, delay * 1000L, delay * 1000L);
    }

    /** {@inheritDoc} */
    @Override
    public void run() {
        CommandParser parser;
        if (origin == null) {
            parser = GlobalCommandParser.getGlobalCommandParser();
        } else {
            parser = origin.getCommandParser();
        }
        
        parser.parseCommand(origin, command);
                
        if (--repetitions <= 0) {
            timer.cancel();
        }
    }
}
