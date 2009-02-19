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

package com.dmdirc.commandparser;

/**
 * Represents a command and its arguments.
 *
 * @author chris
 */
public class CommandArguments {

    /** The raw line that was input. */
    private final String line;

    private String[] arguments;

    public CommandArguments(final String line) {
        this.line = line;
    }
    
    public synchronized String[] getArguments() {
        if (arguments == null) {
            parse();
        }
        
        return arguments;
    }

    protected void parse() {
        arguments = line.split("\\s+");
    }

    public boolean isCommand() {
        return !line.isEmpty() && line.charAt(0) == CommandManager.getCommandChar();
    }

    public boolean isSilent() {
        return isCommand() && line.length() >= 2 &&
                line.charAt(1) == CommandManager.getSilenceChar();
    }

}
