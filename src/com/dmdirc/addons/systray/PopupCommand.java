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

package com.dmdirc.addons.systray;

import com.dmdirc.Server;
import com.dmdirc.commandparser.CommandArguments;
import com.dmdirc.commandparser.CommandManager;
import com.dmdirc.commandparser.commands.ServerCommand;
import com.dmdirc.ui.interfaces.InputWindow;

/**
 * The /popup command allows the user to show a popup message from the system
 * tray icon.
 * @author chris
 */
public final class PopupCommand extends ServerCommand {
    
    /** The SystrayPlugin that we belong to. */
    private final SystrayPlugin parent;
    
    /**
     * Creates a new instance of PopupCommand.
     * @param newParent The plugin that this command belongs to
     */
    public PopupCommand(final SystrayPlugin newParent) {
        super();
        
        this.parent = newParent;
        
        CommandManager.registerCommand(this);
    }

    /**
     * Used to show a notification using this plugin.
     *
     * @param title Title of dialog if applicable
     * @param message Message to show
     * @return True if the notification was shown.
     */
    public boolean showPopup(final String title, final String message) {
        parent.notify(title, message);
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public void execute(final InputWindow origin, final Server server,
            final boolean isSilent, final CommandArguments args) {
        showPopup("DMDirc", args.getArgumentsAsString());
    }

    /** {@inheritDoc} */
    @Override
    public String getName() {
        return "popup";
    }

    /** {@inheritDoc} */
    @Override
    public boolean showInHelp() {
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public String getHelp() {
        return "popup <message> - shows the message as a system tray popup";
    }
    
    /** Unregisters this command from the CommandManager. */
    public void unregister() {
        CommandManager.unregisterCommand(this);
    }
    
}
