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
import com.dmdirc.Server;
import com.dmdirc.commandparser.BaseCommandInfo;
import com.dmdirc.commandparser.CommandArguments;
import com.dmdirc.commandparser.CommandInfo;
import com.dmdirc.commandparser.CommandType;
import com.dmdirc.commandparser.commands.Command;
import com.dmdirc.commandparser.commands.CommandOptions;
import com.dmdirc.commandparser.commands.ExternalCommand;
import com.dmdirc.commandparser.commands.IntelligentCommand;
import com.dmdirc.commandparser.commands.context.ChannelCommandContext;
import com.dmdirc.commandparser.commands.context.CommandContext;
import com.dmdirc.interfaces.CommandController;
import com.dmdirc.ui.input.AdditionalTabTargets;

import javax.inject.Inject;

/**
 * Sends a names request.
 */
@CommandOptions(allowOffline = false)
public class Names extends Command implements IntelligentCommand,
        ExternalCommand {

    /** A command info object for this command. */
    public static final CommandInfo INFO = new BaseCommandInfo("names",
            "names - Requests a list of users that are in the channel",
            CommandType.TYPE_CHANNEL);

    /**
     * Creates a new instance of this command.
     *
     * @param controller The controller to use for command information.
     */
    @Inject
    public Names(final CommandController controller) {
        super(controller);
    }

    /** {@inheritDoc} */
    @Override
    public void execute(final FrameContainer origin,
            final CommandArguments args, final CommandContext context) {
        final Channel channel = ((ChannelCommandContext) context).getChannel();
        channel.getConnection().getParser().sendRawMessage("NAMES "
                + channel.getChannelInfo().getName());
    }

    /** {@inheritDoc} */
    @Override
    public void execute(final FrameContainer origin, final Server server,
            final String channel, final boolean isSilent, final CommandArguments args) {
        server.getParser().sendRawMessage("NAMES " + channel);
    }

    /** {@inheritDoc} */
    @Override
    public AdditionalTabTargets getSuggestions(final int arg,
            final IntelligentCommandContext context) {
        return new AdditionalTabTargets().excludeAll();
    }

}
