/*
 * Copyright (c) 2006-2007 Chris Smith, Shane Mc Cormack, Gregory Holmes
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

package uk.org.ownage.dmdirc.commandparser.commands.channel;

import java.awt.Color;
import uk.org.ownage.dmdirc.Channel;
import uk.org.ownage.dmdirc.Server;
import uk.org.ownage.dmdirc.commandparser.ChannelCommand;
import uk.org.ownage.dmdirc.commandparser.CommandManager;
import uk.org.ownage.dmdirc.commandparser.CommandWindow;
import uk.org.ownage.dmdirc.ui.messages.ColourManager;

/**
 * The notify command allows the user to set the notification colour for a
 * channel.
 * @author chris
 */
public final class Notify extends ChannelCommand {
    
    /** Creates a new instance of Notify. */
    public Notify() {
        super();
        
        CommandManager.registerCommand(this);
    }
    
    /**
     * Executes this command.
     * @param origin The frame in which this command was issued
     * @param server The server object that this command is associated with
     * @param channel The channel object that this command is associated with
     * @param args The user supplied arguments
     */
    public void execute(final CommandWindow origin, final Server server,
            final Channel channel, final String... args) {
        Color colour = null;
        
        if (args[0].length() > 0 && args[0].length() <= 2) {
            int num = -1;
            
            try {
                num = Integer.parseInt(args[0]);
            } catch (NumberFormatException ex) {
                // Do nothing
            }
            
            if (num >= 0 && num <= 15) {
                colour = ColourManager.getColour(num);
            }
        } else if (args[0].length() == 6) {
            colour = ColourManager.getColour(args[0]);
        }
        
        if (colour == null) {
            origin.addLine("Usage: notify <colour> - colour must be an IRC colour code (0-15) or a hex string (e.g. FFFF00).");
        } else {
            channel.sendNotification(colour);
        }
    }
    
    /** {@inheritDoc}. */
    public String getName() {
        return "notify";
    }
    
    /** {@inheritDoc}. */
    public boolean showInHelp() {
        return false;
    }
    
    /** {@inheritDoc}. */
    public boolean isPolyadic() {
        return false;
    }
    
    /** {@inheritDoc}. */
    public int getArity() {
        return 1;
    }
    
    /** {@inheritDoc}. */
    public String getHelp() {
        return "notify <colour> - sets the notification colour for this channel";
    }
    
}
