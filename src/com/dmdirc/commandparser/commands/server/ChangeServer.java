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

package com.dmdirc.commandparser.commands.server;

import com.dmdirc.FrameContainer;
import com.dmdirc.Server;
import com.dmdirc.commandparser.CommandArguments;
import com.dmdirc.commandparser.CommandInfo;
import com.dmdirc.commandparser.CommandManager;
import com.dmdirc.commandparser.CommandType;
import com.dmdirc.commandparser.commands.Command;
import com.dmdirc.commandparser.commands.context.CommandContext;
import com.dmdirc.commandparser.commands.context.ServerCommandContext;
import com.dmdirc.commandparser.commands.global.NewServer;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * The /server command allows the user to connect to a new server.
 * 
 * @author chris
 */
public final class ChangeServer extends Command implements CommandInfo {
    
    /**
     * Creates a new instance of ChangeServer.
     */
    public ChangeServer() {
        super();
        
        CommandManager.registerCommand(this);
    }
    
    /** {@inheritDoc} */
    @Override
    public void execute(final FrameContainer<?> origin,
            final CommandArguments args, final CommandContext context) {
        final Server server = ((ServerCommandContext) context).getServer();
        URI address = null;
        if (args.getArguments().length == 0) {
            showUsage(origin, args.isSilent(), "server", "<host[:[+]port]> [password]");
            address = null;
            return;
        } else if (args.getArguments().length == 1
                && args.getArgumentsAsString().contains("://")) {
            try {
                address = NewServer.getURI(args.getArgumentsAsString());
            } catch (URISyntaxException ex) {
                address = null;
            }
        }
        if (address == null) {
            address = NewServer.parseInput(origin, args.isSilent(), args);
        }

        if (address == null) {
            return;
        }

        server.connect(address, server.getProfile());
    }
    
    /** {@inheritDoc} */
    @Override
    public String getName() {
        return "server";
    }
    
    /** {@inheritDoc} */
    @Override
    public boolean showInHelp() {
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public CommandType getType() {
        return CommandType.TYPE_SERVER;
    }
    
    /** {@inheritDoc} */
    @Override
    public String getHelp() {
        return "server <host[:[+]port]> [password] - connect to a different server";
    }
    
}
