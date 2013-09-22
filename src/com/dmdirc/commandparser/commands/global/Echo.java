/*
 * Copyright (c) 2006-2013 DMDirc Developers
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
import com.dmdirc.commandparser.BaseCommandInfo;
import com.dmdirc.commandparser.CommandArguments;
import com.dmdirc.commandparser.CommandInfo;
import com.dmdirc.commandparser.CommandType;
import com.dmdirc.commandparser.commands.Command;
import com.dmdirc.commandparser.commands.IntelligentCommand;
import com.dmdirc.commandparser.commands.context.CommandContext;
import com.dmdirc.commandparser.commands.flags.CommandFlag;
import com.dmdirc.commandparser.commands.flags.CommandFlagHandler;
import com.dmdirc.commandparser.commands.flags.CommandFlagResult;
import com.dmdirc.interfaces.CommandController;
import com.dmdirc.ui.WindowManager;
import com.dmdirc.ui.input.AdditionalTabTargets;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;

/**
 * The echo commands simply echos text to the current window.
 */
public class Echo extends Command implements IntelligentCommand {

    /** A command info object for this command. */
    public static final CommandInfo INFO = new BaseCommandInfo("echo",
            "echo [--ts <timestamp>] [--target <window>] <line> "
            + "- echos the specified line to the window",
            CommandType.TYPE_GLOBAL);

    /** The flag used to specify a timestamp for the echo command. */
    private final CommandFlag timeStampFlag = new CommandFlag("ts", true, 1, 0);
    /** The flag used to specify a target for the echo command. */
    private final CommandFlag targetFlag = new CommandFlag("target", true, 1, 0);
    /** The command flag handler for this command. */
    private final CommandFlagHandler handler;

    /**
     * Creates a new instance of Echo.
     */
    public Echo() {
        super();

        handler = new CommandFlagHandler(timeStampFlag, targetFlag);
    }

    /**
     * Creates a new instance of Echo.
     *
     * @param controller Command controller
     */
    @Inject
    public Echo(final CommandController controller) {
        super(controller);

        handler = new CommandFlagHandler(timeStampFlag, targetFlag);
    }

    /** {@inheritDoc} */
    @Override
    public void execute(final FrameContainer origin,
            final CommandArguments args, final CommandContext context) {
        final CommandFlagResult results = handler.process(origin, args);

        if (results == null) {
            return;
        }

        Date time = new Date();
        if (results.hasFlag(timeStampFlag)) {
            try {
                time = new Date(Long.parseLong(results.getArgumentsAsString(timeStampFlag)));
            } catch (NumberFormatException ex) {
                sendLine(origin, args.isSilent(), FORMAT_ERROR, "Unable to process timestamp");
                return;
            }
        }

        if (results.hasFlag(targetFlag)) {
            FrameContainer frame = null;
            FrameContainer target = origin;

            while (frame == null && target != null) {
                frame = WindowManager.getWindowManager().findCustomWindow(target,
                        results.getArgumentsAsString(targetFlag));
                target = target.getParent();
            }

            if (frame == null) {
                frame = WindowManager.getWindowManager().findCustomWindow(results.getArgumentsAsString(targetFlag));
            }

            if (frame == null) {
                sendLine(origin, args.isSilent(), FORMAT_ERROR,
                        "Unable to find target window");
            } else if (!args.isSilent()) {
                frame.addLine(FORMAT_OUTPUT, time, results.getArgumentsAsString());
            }
        } else if (origin != null && !args.isSilent()) {
            origin.addLine(FORMAT_OUTPUT, time, results.getArgumentsAsString());
        }
    }

    /** {@inheritDoc} */
    @Override
    public AdditionalTabTargets getSuggestions(final int arg,
            final IntelligentCommandContext context) {
        final AdditionalTabTargets targets = new AdditionalTabTargets();

        if (arg == 0) {
            targets.add("--target");
            targets.add("--ts");
        } else if ((arg == 1 && context.getPreviousArgs().get(0).equals("--target"))
                || (arg == 3 && context.getPreviousArgs().get(2).equals("--target")
                && context.getPreviousArgs().get(0).equals("--ts"))) {

            final List<FrameContainer> windowList = new ArrayList<FrameContainer>();
            final Server currentServer = context.getWindow().getServer();

            //Active window's Children
            windowList.addAll(context.getWindow().getChildren());

            //Children of Current Window's server
            if (currentServer != null) {
                windowList.addAll(currentServer.getChildren());
            }

            //Global Windows
            windowList.addAll(WindowManager.getWindowManager().getRootWindows());
            for (FrameContainer customWindow : windowList) {
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
