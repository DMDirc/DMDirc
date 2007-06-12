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

package com.dmdirc.commandparser.commands.global;

import com.dmdirc.Config;
import com.dmdirc.commandparser.GlobalCommand;
import com.dmdirc.commandparser.CommandManager;
import com.dmdirc.commandparser.CommandWindow;
import com.dmdirc.ui.messages.ColourManager;

import java.awt.Color;

/**
 * The notify command allows the user to set the notification colour for a
 * window.
 * 
 * @author chris
 */
public final class Notify extends GlobalCommand {
    
    /**
     * Creates a new instance of Notify.
     */
    public Notify() {
        super();
        
        CommandManager.registerCommand(this);
    }
    
    /**
     * Executes this command.
     * 
     * @param origin The frame in which this command was issued
     * @param isSilent Whether this command is silenced or not
     * @param args The user supplied arguments
     */
    public void execute(final CommandWindow origin, final boolean isSilent,
            final String... args) {
        final Color colour = ColourManager.parseColour(args[0], null);
        
        if (colour == null) {
            sendLine(origin, isSilent, "commandUsage", Config.getCommandChar(), "notify",
                    "<colour> - colour must be an IRC colour code (0-15) or a hex string (e.g. FFFF00).");
        } else if (origin != null) {            
            // There's not much point echoing an error if the origin isn't
            // valid, as errors go to the origin!
            origin.getContainer().sendNotification(colour);
        }
    }
    
    /** {@inheritDoc}. */
    public String getName() {
        return "notify";
    }
    
    /** {@inheritDoc}. */
    public boolean showInHelp() {
        return false;
    }
    
    /** {@inheritDoc}. */
    public boolean isPolyadic() {
        return false;
    }
    
    /** {@inheritDoc}. */
    public int getArity() {
        return 1;
    }
    
    /** {@inheritDoc}. */
    public String getHelp() {
        return "notify <colour> - sets the notification colour for this window";
    }
    
}
