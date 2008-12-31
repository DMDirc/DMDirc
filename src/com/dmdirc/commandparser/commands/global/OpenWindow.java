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

package com.dmdirc.commandparser.commands.global;

import com.dmdirc.CustomWindow;
import com.dmdirc.commandparser.CommandManager;
import com.dmdirc.commandparser.commands.GlobalCommand;
import com.dmdirc.commandparser.commands.IntelligentCommand;
import com.dmdirc.ui.WindowManager;
import com.dmdirc.ui.input.AdditionalTabTargets;
import com.dmdirc.ui.interfaces.InputWindow;
import com.dmdirc.ui.interfaces.Window;

import java.util.List;

/**
 * @author chris
 * 
 */
public class OpenWindow extends GlobalCommand implements IntelligentCommand {
    
    /**
     * Creates a new instance of OpenWindow.
     */
    public OpenWindow() {
        CommandManager.registerCommand(this);
    }    

    /** {@inheritDoc} */
    @Override
    public void execute(final InputWindow origin, final boolean isSilent,
            final String... args) {
        int start = 0;
        Window parent = null;

        if (args.length > 0 && "--server".equals(args[0])) {
            if (origin.getContainer().getServer() == null) {
                sendLine(origin, isSilent, FORMAT_ERROR,
                        "This window doesn't have an associated server.");
                return;
            }

            parent = origin.getContainer().getServer().getFrame();
            start = 1;
        } else if (args.length > 0 && "--child".equals(args[0])) {
            parent = origin;
            start = 1;
        }

        if (args.length == start || args[start].isEmpty()) {
            showUsage(origin, isSilent, "openwindow",
                    "[--server|--child] <name> [title]");
        } else {
            Window window;
            
            if (parent == null) {
                window = WindowManager.findCustomWindow(args[start]);
            } else {
                window = WindowManager.findCustomWindow(parent, args[start]);
            }
            
            final String title = args.length > start + 1 ? implodeArgs(
                    start + 1, args) : args[start];

            if (window == null) {
                if (parent == null) {
                    new CustomWindow(args[start], title);
                } else {
                    new CustomWindow(args[start], title, parent);
                }
            } else {
                sendLine(origin, isSilent, FORMAT_ERROR,
                "A custom window by that name already exists.");
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public String getHelp() {
        return "openwindow [--server|--child] <name> [title] "
                + "- opens a window with the specified name and title";
    }

    /** {@inheritDoc} */
    @Override
    public String getName() {
        return "openwindow";
    }

    /** {@inheritDoc} */
    @Override
    public boolean showInHelp() {
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public AdditionalTabTargets getSuggestions(final int arg, final List<String> previousArgs) {
        final AdditionalTabTargets res = new AdditionalTabTargets();
        
        if (arg == 0) {
            res.add("--server");
            res.add("--child");
        }
        
        return res;
    } 

}
