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

package com.dmdirc.addons.audio;

import com.dmdirc.commandparser.CommandArguments;
import com.dmdirc.commandparser.CommandManager;
import com.dmdirc.commandparser.commands.GlobalCommand;
import com.dmdirc.ui.interfaces.InputWindow;

import java.awt.Toolkit;

/**
 * The Beep Command emits a beep
 *
 * @author Shane "Dataforce" Mc Cormack
 */
public final class BeepCommand extends GlobalCommand {

    /**
     * Creates a new instance of BeepCommand.
     */
    public BeepCommand() {
        super();
        CommandManager.registerCommand(this);
    }

    /** {@inheritDoc} */
    @Override
    public void execute(final InputWindow origin, final boolean isSilent,
                        final CommandArguments args) {
        Toolkit.getDefaultToolkit().beep();
    }

    /**
     * Returns this command's name.
     *
     * @return The name of this command
     */
    @Override
    public String getName() {
        return "beep";
    }

    /**
     * Returns whether or not this command should be shown in help messages.
     *
     * @return True iff the command should be shown, false otherwise
     */
    @Override
    public boolean showInHelp() {
        return true;
    }

    /**
     * Returns a string representing the help message for this command.
     *
     * @return the help message for this command
     */
    @Override
    public String getHelp() {
        return this.getName();
    }

}

