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

package com.dmdirc.addons.redirectplugin;

import com.dmdirc.MessageTarget;
import com.dmdirc.Server;
import com.dmdirc.commandparser.ChatCommand;
import com.dmdirc.ui.interfaces.InputWindow;

/**
 * The redirect command allows the user to redirect the output from another
 * command that would normally echo results locally to a query or channel
 * window instead.
 *
 * @author Chris
 */
public class RedirectCommand extends ChatCommand {
    
    public RedirectCommand() {
    }
    
    /** {@inheritDoc} */
    public void execute(final InputWindow origin, final Server server,
            final MessageTarget target, final boolean isSilent, final String... args) {
        target.getFrame().getCommandParser().parseCommand(new FakeInputWindow(target), implodeArgs(args));
    }
    
    /** {@inheritDoc} */
    public String getName() {
        return "redirect";
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
        return "redirect <command> - sends the output of the command to a channel or query window";
    }
    
}
