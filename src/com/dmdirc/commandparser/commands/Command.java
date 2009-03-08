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

package com.dmdirc.commandparser.commands;

import com.dmdirc.commandparser.CommandManager;
import com.dmdirc.ui.interfaces.InputWindow;
import com.dmdirc.ui.messages.Styliser;

/**
 * Represents a generic command.
 *
 * @author chris
 */
public abstract class Command {
    
    /** The format name used for command output. */
    protected static final String FORMAT_OUTPUT = "commandOutput";
    
    /** The format name used for command errors. */
    protected static final String FORMAT_ERROR = "commandError";
    
    /**
     * Sends a line, if appropriate, to the specified target.
     * @param target The command window to send the line to
     * @param isSilent Whether this command is being silenced or not
     * @param type The type of message to send
     * @param args The arguments of the message
     */
    protected final void sendLine(final InputWindow target,
            final boolean isSilent, final String type, final Object ... args) {
        if (!isSilent && target != null) {
            target.addLine(type, args);
        }
    }
    
    /**
     * Sends a usage line, if appropriate, to the specified target.
     * 
     * @param target The command window to send the line to
     * @param isSilent Whether this command is being silenced or not
     * @param name The name of the command that's raising the error
     * @param args The arguments that the command accepts or expects
     */
    protected final void showUsage(final InputWindow target,
            final boolean isSilent, final String name, final String args) {
        sendLine(target, isSilent, "commandUsage", CommandManager.getCommandChar(),
                name, args);
    }    
    
    /**
     * Formats the specified data into a table suitable for output in the
     * textpane. It is expected that each String[] in data has the same number
     * of elements as the headers array.
     * 
     * @param headers The headers of the table.
     * @param data The contents of the table.
     * @return A string containing an ASCII table
     */
    protected static String doTable(final String[] headers, final String[][] data) {
        final StringBuilder res = new StringBuilder();
        res.append(Styliser.CODE_FIXED);
        res.append(Styliser.CODE_BOLD);
        
        int[] maxsizes = new int[headers.length];
        
        for (int i = 0; i < headers.length; i++) {
            maxsizes[i] = headers[i].length() + 3;
            
            for (int j = 0; j < data.length; j++) {
                maxsizes[i] = Math.max(maxsizes[i], data[j][i].length() + 3);
            }
            
            doPadding(res, headers[i], maxsizes[i]);
        }
                
        for (String[] source : data) {
            res.append('\n');
            res.append(Styliser.CODE_FIXED);
            
            for (int i = 0; i < source.length; i++) {
                doPadding(res, source[i], maxsizes[i]);
            }
        }
        
        return res.toString();
    }
    
    /**
     * Adds the specified data to the stringbuilder, padding with spaces to
     * the specified size.
     * 
     * @param builder The stringbuilder to append data to
     * @param data The data to be added
     * @param size The minimum size that should be used
     */
    private static void doPadding(final StringBuilder builder, final String data,
            final int size) {
        builder.append(data);
        
        for (int i = 0; i < size - data.length(); i++) {
            builder.append(' ');
        }
    }

}
