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
import com.dmdirc.ChannelClientProperty;
import com.dmdirc.Server;
import com.dmdirc.commandparser.CommandArguments;
import com.dmdirc.commandparser.CommandManager;
import com.dmdirc.commandparser.commands.ChannelCommand;
import com.dmdirc.commandparser.commands.IntelligentCommand;
import com.dmdirc.parser.irc.ChannelClientInfo;
import com.dmdirc.ui.input.AdditionalTabTargets;
import com.dmdirc.ui.input.TabCompletionType;
import com.dmdirc.ui.interfaces.ChannelWindow;
import com.dmdirc.ui.interfaces.InputWindow;
import com.dmdirc.ui.messages.ColourManager;

import java.awt.Color;
import java.util.List;

/**
 * Allows the user to set a nickname on the channel to use a custom colour.
 * @author chris
 */
public final class SetNickColour extends ChannelCommand implements IntelligentCommand {
    
    /** Creates a new instance of SetNickColour. */
    public SetNickColour() {
        super();
        
        CommandManager.registerCommand(this);
    }
    
    /** {@inheritDoc} */
    @SuppressWarnings("unchecked") @Override
    public void execute(final InputWindow origin, final Server server,
            final Channel channel, final boolean isSilent, final CommandArguments args) {
        
        int offset = 0;
        boolean nicklist = true;
        boolean text = true;
        
        if (args.getArguments().length > offset && args.getArguments()[offset].equalsIgnoreCase("--nicklist")) {
            text = false;
            offset++;
        } else if (args.getArguments().length > offset && args.getArguments()[offset].equalsIgnoreCase("--text")) {
            nicklist = false;
            offset++;
        }
        
        if (args.getArguments().length <= offset) {
            showUsage(origin, isSilent, "setnickcolour", "[--nicklist|--text] <nick> [colour]");
            return;
        }
        
        final ChannelClientInfo target = channel.getChannelInfo().getUser(args.getArguments()[offset]);
        offset++;
        
        if (target == null) {
            sendLine(origin, isSilent, FORMAT_ERROR, "No such nickname (" + args.getArguments()[offset - 1] + ")!");
        } else if (args.getArguments().length <= offset) {
            // We're removing the colour
            if (nicklist) {
                target.getMap().remove(ChannelClientProperty.NICKLIST_FOREGROUND);
            }
            if (text) {
                target.getMap().remove(ChannelClientProperty.TEXT_FOREGROUND);
            }
            ((ChannelWindow) channel.getFrame()).redrawNicklist();
        } else {
            // We're setting the colour
            final Color newColour = ColourManager.parseColour(args.getArguments()[offset], null);
            if (newColour == null) {
                sendLine(origin, isSilent, FORMAT_ERROR, "Invalid colour specified.");
                return;
            }

            if (nicklist) {
                target.getMap().put(ChannelClientProperty.NICKLIST_FOREGROUND, newColour);
            }

            if (text) {
                target.getMap().put(ChannelClientProperty.TEXT_FOREGROUND, newColour);
            }

            ((ChannelWindow) channel.getFrame()).updateNames();
        }
    }
    
    /** {@inheritDoc} */
    @Override
    public String getName() {
        return "setnickcolour";
    }
    
    /** {@inheritDoc} */
    @Override
    public boolean showInHelp() {
        return true;
    }
    
    /** {@inheritDoc} */
    @Override
    public String getHelp() {
        return "setnickcolour [--nicklist|--text] <nick> [colour] - "
                + "set the specified person's display colour";
    }

    /** {@inheritDoc} */
    @Override
    public AdditionalTabTargets getSuggestions(final int arg, final List<String> previousArgs) {
        final AdditionalTabTargets targets = new AdditionalTabTargets();
        targets.excludeAll();
        
        if (arg == 0) {
            targets.include(TabCompletionType.CHANNEL_NICK);
            targets.add("--nicklist");
            targets.add("--text");
        } else if (arg == 1 && (previousArgs.get(0).equals("--text")
                || previousArgs.get(0).equals("--nicklist"))) {
            targets.include(TabCompletionType.CHANNEL_NICK);            
        }
        
        return targets;
    }
    
}
