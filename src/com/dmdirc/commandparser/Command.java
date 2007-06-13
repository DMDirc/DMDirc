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

package com.dmdirc.commandparser;

import com.dmdirc.ui.interfaces.InputWindow;

/**
 * Represents a generic command.
 * @author chris
 */
public abstract class Command implements Comparable<Command> {
            
    /**
     * Returns the signature of this command. For polyadic commands, the signature
     * is simply the name. For other commands, the signature is a concatenation of
     * the name, a literal "/", and the arity.
     * @return The signature of this command
     */
    public final String getSignature() {
        if (isPolyadic()) {
            return getName();
        } else {
            return getName() + "/" + getArity();
        }
    }
        
    /**
     * Returns this command's name.
     * @return The name of this command
     */
    public abstract String getName();
    
    /**
     * Returns whether or not this command should be shown in help messages.
     * @return True iff the command should be shown, false otherwise
     */
    public abstract boolean showInHelp();
    
    /**
     * Indicates whether this command is polyadic or not.
     * @return True iff this command is polyadic, false otherwise
     */
    public abstract boolean isPolyadic();
    
    /**
     * Returns the arity of this command.
     * @return This command's arity
     */
    public abstract int getArity();
    
    /**
     * Returns a string representing the help message for this command.
     * @return the help message for this command
     */
    public abstract String getHelp();
    
    /**
     * Implodes the given list of arguments.
     * @param offset The index to start at
     * @param args The arguments to implode
     * @return A string containing each argument seperated by a space
     */
    protected final String implodeArgs(final int offset, final String... args) {
        String res = "";
        for (int i = offset; i < args.length; i++) {
            if (res.length() == 0) {
                res = args[i];
            } else {
                res = res.concat(" " + args[i]);
            }
        }
        return res;
    }
    
    /**
     * Implodes the given list of arguments.
     * @param args The arguments to implode
     * @return A string containing each argument seperated by a space
     */
    protected final String implodeArgs(final String... args) {
        return implodeArgs(0, args);
    } 
    
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
    
    /** {@inheritDoc} */
    public final int compareTo(final Command o) {
        return getSignature().compareTo(o.getSignature());
    }
}
