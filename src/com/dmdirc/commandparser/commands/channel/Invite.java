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

package com.dmdirc.commandparser.commands.channel;

import com.dmdirc.Channel;
import com.dmdirc.FrameContainer;
import com.dmdirc.Server;
import com.dmdirc.commandparser.CommandArguments;
import com.dmdirc.commandparser.CommandInfo;
import com.dmdirc.commandparser.CommandType;
import com.dmdirc.commandparser.commands.Command;
import com.dmdirc.commandparser.commands.CommandOptions;
import com.dmdirc.commandparser.commands.ExternalCommand;
import com.dmdirc.commandparser.commands.context.ChannelCommandContext;
import com.dmdirc.commandparser.commands.context.CommandContext;
import com.dmdirc.parser.interfaces.ChannelInfo;

/**
 * The invite command allows the user to invite others to a channel.
 *
 * @since 0.6.4
 * @author chris
 */
@CommandOptions(allowOffline=false)
public final class Invite extends Command implements ExternalCommand, CommandInfo {

    /** Creates a new instance of Invite. */
    public Invite() {
        super();
    }

    /** {@inheritDoc} */
    @Override
    public void execute(final FrameContainer<?> origin,
            final CommandArguments args, final CommandContext context) {
        if (args.getArguments().length < 1) {
            sendLine(origin, args.isSilent(), FORMAT_ERROR,
                    "Insufficient arguments: must specify user");
        } else {
            final Channel channel = ((ChannelCommandContext) context).getChannel();
            final ChannelInfo cChannel = channel.getChannelInfo();

            cChannel.getParser().sendInvite(cChannel.getName(), args.getArgumentsAsString());
        }
    }

    /** {@inheritDoc} */
    @Override
    public void execute(final FrameContainer<?> origin, final Server server,
            final String channel, final boolean isSilent, final CommandArguments args) {
        if (args.getArguments().length < 1) {
            sendLine(origin, isSilent, FORMAT_ERROR,
                    "Insufficient arguments: must specify user");
        } else {
            server.getParser().sendInvite(channel, args.getArgumentsAsString());
        }
    }

    /** {@inheritDoc} */
    @Override
    public String getName() {
        return "invite";
    }

    /** {@inheritDoc} */
    @Override
    public boolean showInHelp() {
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public CommandType getType() {
        return CommandType.TYPE_CHANNEL;
    }

    /** {@inheritDoc} */
    @Override
    public String getHelp() {
        return "invite <user> - invites user to a channel";
    }

}
