/*
 * Copyright (c) 2006-2014 DMDirc Developers
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

import com.dmdirc.ClientModule.GlobalConfig;
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
import com.dmdirc.interfaces.CommandController;
import com.dmdirc.interfaces.config.AggregateConfigProvider;
import com.dmdirc.ui.WindowManager;
import com.dmdirc.ui.input.AdditionalTabTargets;
import com.dmdirc.util.URLBuilder;

import com.google.common.eventbus.EventBus;

import javax.inject.Inject;

/**
 * Opens a new window.
 */
public class OpenWindow extends Command implements IntelligentCommand {

    /** A command info object for this command. */
    public static final CommandInfo INFO = new BaseCommandInfo("openwindow",
            "openwindow [--server|--child] <name> [title] "
            + "- opens a window with the specified name and title",
            CommandType.TYPE_GLOBAL);
    /** Window management. */
    private final WindowManager windowManager;
    /** The URL builder to use when finding icons. */
    private final URLBuilder urlBuilder;
    /** The bus to despatch events on. */
    private final EventBus eventBus;
    /** The config provider to retrieve settings from. */
    private final AggregateConfigProvider configProvider;

    /**
     * Creates a new instance of this command.
     *
     * @param controller     The controller to use for command information.
     * @param windowManager  Window management
     * @param urlBuilder     The URL builder to use when finding icons.
     * @param eventBus       The bus to despatch events on.
     * @param configProvider The config provider to retrieve settings from.
     */
    @Inject
    public OpenWindow(
            final CommandController controller,
            final WindowManager windowManager,
            final URLBuilder urlBuilder,
            final EventBus eventBus,
            @GlobalConfig final AggregateConfigProvider configProvider) {
        super(controller);

        this.windowManager = windowManager;
        this.urlBuilder = urlBuilder;
        this.eventBus = eventBus;
        this.configProvider = configProvider;
    }

    @Override
    public void execute(final FrameContainer origin,
            final CommandArguments args, final CommandContext context) {
        int start = 0;
        FrameContainer parent = null;

        if (args.getArguments().length > 0 && "--server".equals(args.getArguments()[0])) {
            if (origin.getConnection() == null) {
                sendLine(origin, args.isSilent(), FORMAT_ERROR,
                        "This window doesn't have an associated server.");
                return;
            }

            parent = (Server) origin.getConnection();
            start = 1;
        } else if (args.getArguments().length > 0 && "--child".equals(args.getArguments()[0])) {
            parent = origin;
            start = 1;
        }

        if (args.getArguments().length == start || args.getArguments()[start].isEmpty()) {
            showUsage(origin, args.isSilent(), "openwindow",
                    "[--server|--child] <name> [title]");
        } else {
            final FrameContainer window;

            if (parent == null) {
                window = windowManager.findCustomWindow(args.getArguments()[start]);
            } else {
                window = windowManager.findCustomWindow(parent, args.getArguments()[start]);
            }

            final String title = args.getArguments().length > start + 1
                    ? args.getArgumentsAsString(start + 1) : args.getArguments()[start];

            if (window == null) {
                CustomWindow newWindow;
                if (parent == null) {
                    newWindow = new CustomWindow(args.getArguments()[start], title,
                            configProvider, urlBuilder, eventBus);
                    windowManager.addWindow(newWindow);
                } else {
                    newWindow = new CustomWindow(args.getArguments()[start], title, parent,
                            urlBuilder, eventBus);
                    windowManager.addWindow(parent, newWindow);
                }
            } else {
                sendLine(origin, args.isSilent(), FORMAT_ERROR,
                        "A custom window by that name already exists.");
            }
        }
    }

    @Override
    public AdditionalTabTargets getSuggestions(final int arg,
            final IntelligentCommandContext context) {
        final AdditionalTabTargets res = new AdditionalTabTargets();

        if (arg == 0) {
            res.add("--server");
            res.add("--child");
        }

        return res;
    }

}
