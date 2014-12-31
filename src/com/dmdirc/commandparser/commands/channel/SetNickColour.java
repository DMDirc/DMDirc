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

package com.dmdirc.commandparser.commands.channel;

import com.dmdirc.Channel;
import com.dmdirc.FrameContainer;
import com.dmdirc.commandparser.BaseCommandInfo;
import com.dmdirc.commandparser.CommandArguments;
import com.dmdirc.commandparser.CommandInfo;
import com.dmdirc.commandparser.CommandType;
import com.dmdirc.commandparser.commands.Command;
import com.dmdirc.commandparser.commands.IntelligentCommand;
import com.dmdirc.commandparser.commands.context.ChannelCommandContext;
import com.dmdirc.commandparser.commands.context.CommandContext;
import com.dmdirc.events.DisplayProperty;
import com.dmdirc.interfaces.CommandController;
import com.dmdirc.interfaces.GroupChatUser;
import com.dmdirc.ui.input.AdditionalTabTargets;
import com.dmdirc.ui.input.TabCompletionType;
import com.dmdirc.ui.messages.ColourManagerFactory;
import com.dmdirc.util.colours.Colour;

import java.util.Optional;

import javax.annotation.Nonnull;
import javax.inject.Inject;

/**
 * Allows the user to set a nickname on the channel to use a custom colour.
 */
public class SetNickColour extends Command implements IntelligentCommand {

    /** A command info object for this command. */
    public static final CommandInfo INFO = new BaseCommandInfo("setnickcolour",
            "setnickcolour <nick> [colour] - set the specified person's display colour",
            CommandType.TYPE_CHANNEL);
    /** Manager to use to convert colours. */
    private final ColourManagerFactory colourManagerFactory;

    /**
     * Creates a new instance of the {@link SetNickColour} command.
     *
     * @param controller           The command controller that owns this command.
     * @param colourManagerFactory The colour manager factory to use to convert colours.
     */
    @Inject
    public SetNickColour(final CommandController controller,
            final ColourManagerFactory colourManagerFactory) {
        super(controller);
        this.colourManagerFactory = colourManagerFactory;
    }

    @Override
    public void execute(@Nonnull final FrameContainer origin,
            final CommandArguments args, final CommandContext context) {
        final Channel channel = ((ChannelCommandContext) context).getChannel();

        if (args.getArguments().length == 0) {
            showUsage(origin, args.isSilent(), INFO.getName(), INFO.getHelp());
            return;
        }

        final Optional<GroupChatUser> target = channel.getUser(channel.getConnection().get()
                .getUser(args.getArguments()[0]));

        if (!target.isPresent()) {
            sendLine(origin, args.isSilent(), FORMAT_ERROR, "No such nickname ("
                    + args.getArguments()[0] + ")!");
        } else if (args.getArguments().length == 1) {
            // We're removing the colour
            target.get().removeDisplayProperty(DisplayProperty.FOREGROUND_COLOUR);
            channel.refreshClients();
        } else {
            // We're setting the colour
            final Colour newColour = colourManagerFactory.getColourManager(origin.getConfigManager())
                    .getColourFromString(args.getArguments()[1], null);
            if (newColour == null) {
                sendLine(origin, args.isSilent(), FORMAT_ERROR, "Invalid colour specified ("
                        + args.getArguments()[1] + ").");
                return;
            }

            target.get().setDisplayProperty(DisplayProperty.FOREGROUND_COLOUR, newColour);

            channel.refreshClients();
        }
    }

    @Override
    public AdditionalTabTargets getSuggestions(final int arg,
            final IntelligentCommandContext context) {
        final AdditionalTabTargets targets = new AdditionalTabTargets();
        targets.excludeAll();

        if (arg == 0) {
            targets.include(TabCompletionType.CHANNEL_NICK);
        }

        return targets;
    }

}
