/*
 * Copyright (c) 2006-2008 Chris Smith, Shane Mc Cormack, Gregory Holmes
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

package com.dmdirc.commandparser.commands.chat;

import com.dmdirc.MessageTarget;
import com.dmdirc.Server;
import com.dmdirc.commandparser.commands.ChatCommand;
import com.dmdirc.commandparser.CommandManager;
import com.dmdirc.commandparser.commands.ValidatingCommand;
import com.dmdirc.config.prefs.validator.ValidationResponse;
import com.dmdirc.ui.interfaces.InputWindow;

import java.util.List;

/**
 * The me command sends a CTCP action to the current channel.
 * @author chris
 */
public final class Me extends ChatCommand implements ValidatingCommand {
    
    /** Creates a new instance of Me. */
    public Me() {
        super();
        
        CommandManager.registerCommand(this);
    }
    
    /** {@inheritDoc} */
    @Override
    public void execute(final InputWindow origin, final Server server,
            final MessageTarget target, final boolean isSilent, final String... args) {
        if (args.length == 0) {
            showUsage(origin, isSilent, "me", "<action>");
        } else {
            target.sendAction(implodeArgs(args));
        }
    }
    
    /** {@inheritDoc} */
    @Override
    public String getName() {
        return "me";
    }
    
    /** {@inheritDoc} */
    @Override
    public boolean showInHelp() {
        return true;
    }
    
    /** {@inheritDoc} */
    @Override
    public String getHelp() {
        return "me <action> - sends the specified action";
    }

    /** {@inheritDoc} */
    @Override
    public ValidationResponse validateArguments(final InputWindow origin, 
            final List<String> arguments) {
        final int length = implodeArgs(arguments.toArray(new String[0])).length();
        
        if (origin.getContainer().getServer().getParser().getMaxLength("PRIVMSG",
                origin.getContainer().toString()) <= length) {
            return new ValidationResponse("Too long");
        } else {
            return new ValidationResponse();
        }
    }
    
}
