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

package uk.org.ownage.dmdirc.plugins.plugins.systray;

import uk.org.ownage.dmdirc.Server;
import uk.org.ownage.dmdirc.commandparser.CommandManager;
import uk.org.ownage.dmdirc.commandparser.CommandWindow;
import uk.org.ownage.dmdirc.commandparser.ServerCommand;

/**
 * The /popup command allows the user to show a popup message from the system
 * tray icon.
 * @author chris
 */
public class PopupCommand extends ServerCommand {
    
    /** The SystrayPlugin that we belong to. */
    private SystrayPlugin parent;
    
    /**
     * Creates a new instance of PopupCommand.
     * @param parent The plugin that this command belongs to
     */
    public PopupCommand(final SystrayPlugin parent) {
        super();
        
        this.parent = parent;
        
        CommandManager.registerCommand(this);
    }

    /** {@inheritDoc} */
    public void execute(final CommandWindow origin, final Server server,
            final String ... args) {
        parent.notify("DMDirc", implodeArgs(args));
    }

    /** {@inheritDoc} */
    public String getName() {
        return "popup";
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
        return "popup <message> - shows the message as a system tray popup";
    }
    
}
