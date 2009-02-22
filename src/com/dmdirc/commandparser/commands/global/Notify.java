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

package com.dmdirc.commandparser.commands.global;

import com.dmdirc.commandparser.CommandArguments;
import com.dmdirc.commandparser.CommandManager;
import com.dmdirc.commandparser.commands.GlobalCommand;
import com.dmdirc.commandparser.commands.IntelligentCommand;
import com.dmdirc.ui.input.AdditionalTabTargets;
import com.dmdirc.ui.interfaces.InputWindow;
import com.dmdirc.ui.messages.ColourManager;

import java.awt.Color;
import java.util.List;

/**
 * The notify command allows the user to set the notification colour for a
 * window.
 * 
 * @author chris
 */
public final class Notify extends GlobalCommand implements IntelligentCommand {
    
    /**
     * Creates a new instance of Notify.
     */
    public Notify() {
        super();
        
        CommandManager.registerCommand(this);
    }
    
    /** {@inheritDoc} */
    @Override
    public void execute(final InputWindow origin, final boolean isSilent,
            final CommandArguments args) {
        if (args.getArguments().length == 0) {
            showUsage(origin, isSilent, "notify", "<colour>");
            return;
        }
        
        final Color colour = ColourManager.parseColour(args.getArguments()[0], null);
        
        if (colour == null) {
            showUsage(origin, isSilent, "notify",
                    "<colour> - colour must be an IRC colour code (0-15) or a " +
                    "hex string (e.g. FFFF00).");
        } else if (origin != null) {            
            // There's not much point echoing an error if the origin isn't
            // valid, as errors go to the origin!
            origin.getContainer().sendNotification(colour);
        }
    }
    
    /** {@inheritDoc} */
    @Override
    public String getName() {
        return "notify";
    }
    
    /** {@inheritDoc} */
    @Override
    public boolean showInHelp() {
        return false;
    }
    
    /** {@inheritDoc} */
    @Override
    public String getHelp() {
        return "notify <colour> - sets the notification colour for this window";
    }

    /** {@inheritDoc} */
    @Override
    public AdditionalTabTargets getSuggestions(final int arg, final List<String> previousArgs) {
        return new AdditionalTabTargets().excludeAll();
    } 
    
}
