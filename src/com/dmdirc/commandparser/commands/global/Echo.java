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

import com.dmdirc.Main;
import com.dmdirc.commandparser.CommandArguments;
import com.dmdirc.commandparser.CommandManager;
import com.dmdirc.commandparser.commands.GlobalCommand;
import com.dmdirc.commandparser.commands.IntelligentCommand;
import com.dmdirc.ui.WindowManager;
import com.dmdirc.ui.input.AdditionalTabTargets;
import com.dmdirc.ui.interfaces.InputWindow;
import com.dmdirc.ui.interfaces.Window;

import java.util.List;

/**
 * The echo commands simply echos text to the current window.
 * 
 * @author chris
 */
public final class Echo extends GlobalCommand implements IntelligentCommand {

    /**
     * Creates a new instance of Echo.
     */
    public Echo() {
        super();

        CommandManager.registerCommand(this);
    }

    /** {@inheritDoc} */
    @Override
    public void execute(final InputWindow origin, final boolean isSilent,
            final CommandArguments args) {
        if (args.getArguments().length > 0
                && args.getArguments()[0].equalsIgnoreCase("--active")) {
            final Window frame = Main.getUI().getActiveWindow();
            if (frame instanceof InputWindow) {
                ((InputWindow) frame).addLine(FORMAT_OUTPUT, args.getArgumentsAsString(1));
            }
        } else if (args.getArguments().length > 1
                && args.getArguments()[0].equalsIgnoreCase("--target")) {
            Window frame = null;
            Window target = origin;

            while (frame == null && target != null) {
                frame = WindowManager.findCustomWindow(target, args.getArguments()[1]);
                target = WindowManager.getParent(target);
            }

            if (frame == null) {
                frame = WindowManager.findCustomWindow(args.getArguments()[1]);
            }

            if (frame == null) {
                sendLine(origin, isSilent, FORMAT_ERROR,
                        "Unable to find target window");
            } else {
                frame.addLine(FORMAT_OUTPUT, args.getArgumentsAsString(2));
            }

        } else {
            sendLine(origin, isSilent, FORMAT_OUTPUT, args.getArgumentsAsString());
        }
    }

    /** {@inheritDoc} */
    @Override
    public String getName() {
        return "echo";
    }

    /** {@inheritDoc} */
    @Override
    public boolean showInHelp() {
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public String getHelp() {
        return "echo [--active|--target <window>] <line> "
                + "- echos the specified line to the window";
    }

    /** {@inheritDoc} */
    @Override
    public AdditionalTabTargets getSuggestions(final int arg, final List<String> previousArgs) {
        final AdditionalTabTargets targets = new AdditionalTabTargets();
        
        if (arg == 0) {
            targets.add("--active");
            targets.add("--target");
        } else if (arg == 1 && previousArgs.get(0).equals("--target")) {
            targets.excludeAll();
            // TODO: Include window names
        }
        
        return targets;
    }

}
