/*
 * Copyright (c) 2006-2009 Chris Smith, Shane Mc Cormack, Gregory Holmes
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
import com.dmdirc.Server;
import com.dmdirc.commandparser.commands.ChannelCommand;
import com.dmdirc.commandparser.CommandManager;
import com.dmdirc.commandparser.commands.IntelligentCommand;
import com.dmdirc.parser.irc.ChannelClientInfo;
import com.dmdirc.ui.input.AdditionalTabTargets;
import com.dmdirc.ui.input.TabCompletionType;
import com.dmdirc.ui.interfaces.InputWindow;

import java.util.List;

/**
 * The kick command bans a specified user or host from the channel.
 * 
 * @author chris
 */
public final class Ban extends ChannelCommand implements IntelligentCommand {
    
    /** Creates a new instance of Ban. */
    public Ban() {
        super();
        
        CommandManager.registerCommand(this);
    }
    
    /** {@inheritDoc} */
    @Override
    public void execute(final InputWindow origin, final Server server,
            final Channel channel, final boolean isSilent, final String... args) {
        if (args.length == 0) {
            showUsage(origin, isSilent, "ban", "<user|host>");
            return;
        }
        
        String host = args[0];
        final ChannelClientInfo user = channel.getChannelInfo().getUser(args[0]);
        if (user != null && !user.getClient().getHost().isEmpty()) {
            // TODO: Customisable ban masks, somehow.
            host = "*!*@" + user.getClient().getHost();
        }
        
        server.getParser().sendLine("MODE " + channel + " +b " + host);
    }
    
    /** {@inheritDoc} */
    @Override
    public String getName() {
        return "ban";
    }
    
    /** {@inheritDoc} */
    @Override
    public boolean showInHelp() {
        return true;
    }
    
    /** {@inheritDoc} */
    @Override
    public String getHelp() {
        return "ban <user|host> - bans the specified user or host from the channel.";
    }

    /** {@inheritDoc} */
    @Override
    public AdditionalTabTargets getSuggestions(final int arg, final List<String> previousArgs) {
        final AdditionalTabTargets res = new AdditionalTabTargets().excludeAll();
        
        if (arg == 0) {
            res.include(TabCompletionType.CHANNEL_NICK);
        }
        
        return res;
    }
    
}
