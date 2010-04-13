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

package com.dmdirc.commandparser.commands.global;

import com.dmdirc.CustomWindow;
import com.dmdirc.FrameContainer;
import com.dmdirc.Server;
import com.dmdirc.commandparser.CommandArguments;
import com.dmdirc.commandparser.CommandInfo;
import com.dmdirc.commandparser.CommandManager;
import com.dmdirc.commandparser.CommandType;
import com.dmdirc.commandparser.commands.Command;
import com.dmdirc.commandparser.commands.IntelligentCommand;
import com.dmdirc.commandparser.commands.context.CommandContext;
import com.dmdirc.ui.WindowManager;
import com.dmdirc.ui.input.AdditionalTabTargets;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * The echo commands simply echos text to the current window.
 *
 * @author chris
 */
public final class Echo extends Command implements IntelligentCommand,
        CommandInfo {

    /**
     * Creates a new instance of Echo.
     */
    public Echo() {
        super();

        CommandManager.registerCommand(this);
    }

    /** {@inheritDoc} */
    @Override
    public void execute(final FrameContainer<?> origin,
            final CommandArguments args, final CommandContext context) {
        int offset = 0;
        Date time = new Date();

        if (args.getArguments().length > 1
                && args.getArguments()[offset].equalsIgnoreCase("--ts")) {
            try {
                time = new Date(Long.parseLong(args.getWordsAsString(2, 2)));
            } catch (NumberFormatException ex) {
                sendLine(origin, args.isSilent(), FORMAT_ERROR, "Unable to process timestamp");
                return;
            }

            offset = 2;
        }

        if (args.getArguments().length > offset
                && args.getArguments()[offset].equalsIgnoreCase("--active")) {
            if (!args.isSilent()) {
                final FrameContainer<?> frame = WindowManager.getActiveWindow();
                frame.addLine(FORMAT_OUTPUT, time, args.getArgumentsAsString(offset + 1));
            }
        } else if (args.getArguments().length > offset + 1
                && args.getArguments()[offset].equalsIgnoreCase("--target")) {
            FrameContainer<?> frame = null;
            FrameContainer<?> target = origin;

            while (frame == null && target != null) {
                frame = WindowManager.findCustomWindow(target, args.getArguments()[offset + 1]);
            }

            if (frame == null) {
                frame = WindowManager.findCustomWindow(args.getArguments()[offset + 1]);
            }

            if (frame == null) {
                sendLine(origin, args.isSilent(), FORMAT_ERROR,
                        "Unable to find target window");
            } else if (!args.isSilent()) {
                frame.addLine(FORMAT_OUTPUT, time, args.getArgumentsAsString(offset + 2));
            }

        } else if (origin != null && !args.isSilent()) {
            origin.addLine(FORMAT_OUTPUT, time, args.getArgumentsAsString(offset));
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
    public CommandType getType() {
        return CommandType.TYPE_GLOBAL;
    }

    /** {@inheritDoc} */
    @Override
    public String getHelp() {
        return "echo [--ts <timestamp>] [--active|--target <window>] <line> "
                + "- echos the specified line to the window";
    }

    /** {@inheritDoc} */
    @Override
    public AdditionalTabTargets getSuggestions(final int arg,
            final IntelligentCommandContext context) {
        final AdditionalTabTargets targets = new AdditionalTabTargets();

        if (arg == 0) {
            targets.add("--active");
            targets.add("--target");
            targets.add("--ts");
        } else if ((arg == 1 && context.getPreviousArgs().get(0).equals("--target"))
                || (arg == 3 && context.getPreviousArgs().get(2).equals("--target")
                && context.getPreviousArgs().get(0).equals("--ts"))) {

            final List<FrameContainer<?>> windowList = new ArrayList<FrameContainer<?>>();
            final Server currentServer = context.getWindow().getContainer()
                    .getServer();

            //Active window's Children
            windowList.addAll(context.getWindow().getContainer().getChildren());

            //Children of Current Window's server
            if (currentServer != null) {
                windowList.addAll(currentServer.getChildren());
            }

            //Global Windows
            windowList.addAll(WindowManager.getRootWindows());
            for (FrameContainer<?> customWindow : windowList) {
                if (customWindow instanceof CustomWindow) {
                    targets.add(customWindow.getName());
                }
            }

            targets.excludeAll();
        } else if (arg == 1 && context.getPreviousArgs().get(0).equals("--ts")) {
            targets.add(String.valueOf(new Date().getTime()));
            targets.excludeAll();
        }

        return targets;
    }

}
