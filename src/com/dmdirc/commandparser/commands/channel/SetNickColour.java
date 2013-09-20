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

package com.dmdirc.commandparser.commands.channel;

import com.dmdirc.Channel;
import com.dmdirc.ChannelClientProperty;
import com.dmdirc.FrameContainer;
import com.dmdirc.commandparser.BaseCommandInfo;
import com.dmdirc.commandparser.CommandArguments;
import com.dmdirc.commandparser.CommandInfo;
import com.dmdirc.commandparser.CommandType;
import com.dmdirc.commandparser.commands.Command;
import com.dmdirc.commandparser.commands.IntelligentCommand;
import com.dmdirc.commandparser.commands.context.ChannelCommandContext;
import com.dmdirc.commandparser.commands.context.CommandContext;
import com.dmdirc.interfaces.CommandController;
import com.dmdirc.parser.interfaces.ChannelClientInfo;
import com.dmdirc.ui.Colour;
import com.dmdirc.ui.input.AdditionalTabTargets;
import com.dmdirc.ui.input.TabCompletionType;
import com.dmdirc.ui.messages.ColourManager;

/**
 * Allows the user to set a nickname on the channel to use a custom colour.
 */
public class SetNickColour extends Command implements IntelligentCommand {

    /** A command info object for this command. */
    public static final CommandInfo INFO = new BaseCommandInfo("setnickcolour",
            "setnickcolour [--nicklist|--text] <nick> [colour] - "
            + "set the specified person's display colour",
            CommandType.TYPE_CHANNEL);

    /** Manager to use to convert colours. */
    private final ColourManager colourManager;

    /**
     * Creates a new instance of the {@link SetNickColour} command.
     *
     * @param colourManager The colour manager to use to convert colours.
     */
    public SetNickColour(final ColourManager colourManager) {
        this.colourManager = colourManager;
    }

    /**
     * Creates a new instance of the {@link SetNickColour} command.
     *
     * @param controller The command controller that owns this command.
     * @param colourManager The colour manager to use to convert colours.
     */
    public SetNickColour(final CommandController controller, final ColourManager colourManager) {
        super(controller);
        this.colourManager = colourManager;
    }

    /** {@inheritDoc} */
    @Override
    public void execute(final FrameContainer origin,
            final CommandArguments args, final CommandContext context) {
        final Channel channel = ((ChannelCommandContext) context).getChannel();

        int offset = 0;
        boolean nicklist = true;
        boolean text = true;

        if (args.getArguments().length > offset && args.getArguments()[offset]
                .equalsIgnoreCase("--nicklist")) {
            text = false;
            offset++;
        } else if (args.getArguments().length > offset && args.getArguments()[offset]
                .equalsIgnoreCase("--text")) {
            nicklist = false;
            offset++;
        }

        if (args.getArguments().length <= offset) {
            showUsage(origin, args.isSilent(), "setnickcolour", "[--nicklist|--text] <nick> [colour]");
            return;
        }

        final ChannelClientInfo target = channel.getChannelInfo()
                .getChannelClient(args.getArguments()[offset]);
        offset++;

        if (target == null) {
            sendLine(origin, args.isSilent(), FORMAT_ERROR, "No such nickname ("
                    + args.getArguments()[offset - 1] + ")!");
        } else if (args.getArguments().length <= offset) {
            // We're removing the colour
            if (nicklist) {
                target.getMap().remove(ChannelClientProperty.NICKLIST_FOREGROUND);
            }
            if (text) {
                target.getMap().remove(ChannelClientProperty.TEXT_FOREGROUND);
            }

            channel.refreshClients();
        } else {
            // We're setting the colour
            final Colour newColour = colourManager.getColourFromString(args.getArguments()[offset], null);
            if (newColour == null) {
                sendLine(origin, args.isSilent(), FORMAT_ERROR, "Invalid colour specified.");
                return;
            }

            if (nicklist) {
                target.getMap().put(ChannelClientProperty.NICKLIST_FOREGROUND, newColour);
            }

            if (text) {
                target.getMap().put(ChannelClientProperty.TEXT_FOREGROUND, newColour);
            }

            channel.refreshClients();
        }
    }

    /** {@inheritDoc} */
    @Override
    public AdditionalTabTargets getSuggestions(final int arg,
            final IntelligentCommandContext context) {
        final AdditionalTabTargets targets = new AdditionalTabTargets();
        targets.excludeAll();

        if (arg == 0) {
            targets.include(TabCompletionType.CHANNEL_NICK);
            targets.add("--nicklist");
            targets.add("--text");
        } else if (arg == 1 && (context.getPreviousArgs().get(0).equals("--text")
                || context.getPreviousArgs().get(0).equals("--nicklist"))) {
            targets.include(TabCompletionType.CHANNEL_NICK);
        }

        return targets;
    }

}
