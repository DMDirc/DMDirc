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
import com.dmdirc.interfaces.CommandController;
import com.dmdirc.parser.interfaces.ChannelClientInfo;
import com.dmdirc.ui.input.AdditionalTabTargets;
import com.dmdirc.ui.input.TabCompletionType;

import javax.inject.Inject;

/**
 * The kick command bans a specified user or host from the channel.
 */
public class Ban extends Command implements IntelligentCommand {

    /** A command info object for this command. */
    public static final CommandInfo INFO = new BaseCommandInfo("ban",
            "ban <user|host> - bans the specified user or host from the channel",
            CommandType.TYPE_CHANNEL);

    /**
     * Creates a new instance of {@link Ban} using the given command controller.
     *
     * @param controller The controller to use for command information.
     */
    @Inject
    public Ban(final CommandController controller) {
        super(controller);
    }

    /** {@inheritDoc} */
    @Override
    public void execute(final FrameContainer origin,
            final CommandArguments args, final CommandContext context) {
        final Channel channel = ((ChannelCommandContext) context).getChannel();

        if (args.getArguments().length == 0) {
            showUsage(origin, args.isSilent(), "ban", "<user|host>");
            return;
        }

        String host = args.getArguments()[0];
        final ChannelClientInfo user = channel.getChannelInfo().getChannelClient(host);
        if (user != null && !user.getClient().getHostname().isEmpty()) {
            // TODO: Customisable ban masks, somehow.
            host = "*!*@" + user.getClient().getHostname();
        }

        channel.getChannelInfo().alterMode(true, 'b', host);
        channel.getChannelInfo().flushModes();
    }

    /** {@inheritDoc} */
    @Override
    public AdditionalTabTargets getSuggestions(final int arg,
            final IntelligentCommandContext context) {
        final AdditionalTabTargets res = new AdditionalTabTargets().excludeAll();

        if (arg == 0) {
            res.include(TabCompletionType.CHANNEL_NICK);
        }

        return res;
    }

}
