/*
 * Copyright (c) 2006-2010 Chris Smith, Shane Mc Cormack, Gregory Holmes,
 * Simon Mott
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

import com.dmdirc.FrameContainer;
import com.dmdirc.WritableFrameContainer;
import com.dmdirc.commandparser.CommandArguments;
import com.dmdirc.commandparser.CommandManager;
import com.dmdirc.commandparser.commands.GlobalCommand;
import com.dmdirc.commandparser.commands.IntelligentCommand;
import com.dmdirc.ui.input.AdditionalTabTargets;

/**
 * The input command allows you to maniplulate text in a windows inputField.
 *
 * @author Simon
 * @since 0.6.4
 */
public class Input extends GlobalCommand implements IntelligentCommand {

    /**
     * Creates a new instance of Input.
     */
    public Input() {
        super();

        CommandManager.registerCommand(this);
    }

    /** {@inheritDoc} */
    @Override
    public void execute(final FrameContainer origin, final boolean isSilent,
            final CommandArguments args) {

        if (args.getArguments().length == 0) {
            showUsage(origin, isSilent, "input",
                    "[--clear] <text to insert into inputfield");
            return;
        } else if (args.getArguments().length == 1
                && "--clear".equals(args.getArgumentsAsString(0))) {
            ((WritableFrameContainer) origin).getFrame()
                    .getInputHandler().clearInputField();
        } else {
            ((WritableFrameContainer) origin).getFrame()
                    .getInputHandler().addToInputField(args.getArgumentsAsString());
        }
    }

    /** {@inheritDoc} */
    @Override
    public String getName() {
        return "input";
    }

    /** {@inheritDoc} */
    @Override
    public boolean showInHelp() {
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public String getHelp() {
        return "input [--clear] <text to insert into inputfield> - Adds text to" +
                " the active window's input field";
    }

    /** {@inheritDoc} */
    @Override
    public AdditionalTabTargets getSuggestions(final int arg,
            final IntelligentCommandContext context) {
        final AdditionalTabTargets res = new AdditionalTabTargets();
        
        if (arg == 0) {
            res.add("--clear");
        }
        return res;
    }

}
