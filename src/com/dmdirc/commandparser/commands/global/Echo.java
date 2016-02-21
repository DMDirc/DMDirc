/*
 * Copyright (c) 2006-2015 DMDirc Developers
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
import com.dmdirc.events.CommandOutputEvent;
import com.dmdirc.interfaces.CommandController;
import com.dmdirc.interfaces.Connection;
import com.dmdirc.interfaces.WindowModel;
import com.dmdirc.ui.WindowManager;
import com.dmdirc.ui.input.AdditionalTabTargets;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
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
    /** Window management. */
    private final WindowManager windowManager;

    /**
     * Creates a new instance of Echo.
     *
     * @param controller    Command controller
     * @param windowManager Window management
     */
    @Inject
    public Echo(final CommandController controller, final WindowManager windowManager) {
        super(controller);

        this.windowManager = windowManager;
        handler = new CommandFlagHandler(timeStampFlag, targetFlag);
    }

    @Override
    public void execute(@Nonnull final WindowModel origin,
            final CommandArguments args, final CommandContext context) {
        @Nullable final CommandFlagResult results = handler.process(origin, args);

        if (results == null) {
            return;
        }

        Date time = new Date();
        if (results.hasFlag(timeStampFlag)) {
            try {
                time = new Date(Long.parseLong(results.getArgumentsAsString(timeStampFlag)));
            } catch (NumberFormatException ex) {
                showError(origin, args.isSilent(), "Unable to process timestamp");
                return;
            }
        }

        if (results.hasFlag(targetFlag)) {
            WindowModel frame = null;
            Optional<WindowModel> target = Optional.of(origin);

            while (frame == null && target.isPresent()) {
                frame = windowManager.findCustomWindow(target.get(),
                        results.getArgumentsAsString(targetFlag));
                target = target.flatMap(windowManager::getParent);
            }

            if (frame == null) {
                frame = windowManager.findCustomWindow(results.getArgumentsAsString(targetFlag));
            }

            if (frame == null) {
                showError(origin, args.isSilent(), "Unable to find target window");
            } else if (!args.isSilent()) {
                frame.getEventBus().publishAsync(new CommandOutputEvent(
                        LocalDateTime.ofInstant(time.toInstant(), ZoneId.systemDefault()),
                        frame, results.getArgumentsAsString()));
            }
        } else if (!args.isSilent()) {
            origin.getEventBus().publishAsync(new CommandOutputEvent(
                    LocalDateTime.ofInstant(time.toInstant(), ZoneId.systemDefault()), origin,
                    results.getArgumentsAsString()));
        }
    }

    @Override
    public AdditionalTabTargets getSuggestions(final int arg,
            final IntelligentCommandContext context) {
        final AdditionalTabTargets targets = new AdditionalTabTargets();

        if (arg == 0) {
            targets.add("--target");
            targets.add("--ts");
        } else if (arg == 1 && "--target".equals(context.getPreviousArgs().get(0))
                || arg == 3 && "--target".equals(context.getPreviousArgs().get(2))
                && "--ts".equals(context.getPreviousArgs().get(0))) {

            final Collection<WindowModel> windowList = new ArrayList<>();
            final Optional<Connection> connection = context.getWindow().getConnection();

            //Active window's Children
            windowList.addAll(windowManager.getChildren(context.getWindow()));

            //Children of Current Window's server
            connection
                    .map(Connection::getWindowModel)
                    .map(windowManager::getChildren)
                    .ifPresent(windowList::addAll);

            //Global Windows
            windowList.addAll(windowManager.getRootWindows());
            targets.addAll(
                    windowList.stream().filter(customWindow -> customWindow instanceof CustomWindow)
                            .map(WindowModel::getName).collect(Collectors.toList()));

            targets.excludeAll();
        } else if (arg == 1 && "--ts".equals(context.getPreviousArgs().get(0))) {
            targets.add(String.valueOf(new Date().getTime()));
            targets.excludeAll();
        }

        return targets;
    }

}
