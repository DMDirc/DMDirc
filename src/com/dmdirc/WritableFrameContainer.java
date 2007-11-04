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

package com.dmdirc;

import com.dmdirc.actions.ActionManager;
import com.dmdirc.actions.ActionType;
import com.dmdirc.logger.ErrorLevel;
import com.dmdirc.logger.Logger;
import com.dmdirc.parser.ChannelClientInfo;
import com.dmdirc.parser.ClientInfo;
import com.dmdirc.ui.WindowManager;
import com.dmdirc.ui.interfaces.InputWindow;
import com.dmdirc.ui.interfaces.Window;
import com.dmdirc.ui.messages.ColourManager;
import com.dmdirc.ui.messages.Styliser;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * The writable frame container adds additional methods to the frame container
 * class that allow the sending of lines back to whatever the container's
 * data source is (e.g. an IRC channel or server).
 *
 * @author chris
 */
public abstract class WritableFrameContainer extends FrameContainer {
    
    /** The name of the server notification target. */
    protected static final String NOTIFICATION_SERVER = "server".intern();
    
    /** The name of the channel notification target. */
    protected static final String NOTIFICATION_CHANNEL = "channel".intern();    
    
    /**
     * Sends a line of text to this container's source.
     *
     * @param line The line to be sent
     */
    public abstract void sendLine(String line);
    
    /**
     * Returns the internal frame associated with this object.
     *
     * @return The internal frame associated with this object
     */
    @Override
    public abstract InputWindow getFrame();
    
    /**
     * Returns the maximum length that a line passed to sendLine() should be,
     * in order to prevent it being truncated or causing protocol violations.
     *
     * @return The maximum line length for this container
     */
    public abstract int getMaxLineLength();
    
    /**
     * Returns the number of lines that the specified string would be sent as.
     *
     * @param line The string to be split and sent
     * @return The number of lines required to send the specified string
     */
    public final int getNumLines(final String line) {
        final String[] splitLines = line.split("(\n|\r\n|\r)", Integer.MAX_VALUE);
        int lines = 0;
        for (String splitLine : splitLines) {
            if (splitLine.isEmpty()) {
                lines++;
            } else {
                lines += (int) Math.ceil(splitLine.length() / (double) getMaxLineLength());
            }
        }
        return lines;
    }
    
    /**
     * Processes and displays a notification.
     *
     * @param messageType The name of the formatter to be used for the message
     * @param actionType The action type to be used
     * @param args The arguments for the message
     */
    public void doNotification(final String messageType,
            final ActionType actionType, final Object... args) {
        final List<Object> messageArgs = new ArrayList<Object>();
        final List<Object> actionArgs = new ArrayList<Object>();
        final StringBuffer buffer = new StringBuffer(messageType);

        actionArgs.add(this);

        for (Object arg : args) {
            actionArgs.add(arg);

            if (arg instanceof ClientInfo) {
                final ClientInfo clientInfo = (ClientInfo) arg;
                messageArgs.add(clientInfo.getNickname());
                messageArgs.add(clientInfo.getIdent());
                messageArgs.add(clientInfo.getHost());
            } else if (arg instanceof ChannelClientInfo) {
                final ChannelClientInfo channelInfo = (ChannelClientInfo) arg;
            } else {
                messageArgs.add(arg);
            }
        }

        ActionManager.processEvent(actionType, buffer, actionArgs.toArray());

        handleNotification(messageType, messageArgs.toArray());
    }    
    
    /**
     * Handles general server notifications (i.e., ones note tied to a
     * specific window). The user can select where the notifications should
     * go in their config. Falls back to "self" target
     *
     * @param messageType The type of message that is being sent
     * @param args The arguments for the message
     */
    public void handleNotification(final String messageType, final Object... args) {
        handleNotification(messageType, "self", args);
    }    
    
    /**
     * Handles general server notifications (i.e., ones note tied to a
     * specific window). The user can select where the notifications should
     * go in their config.
     *
     * @param messageType The type of message that is being sent
     * @param fallback The notifi cation type to use if none is defined
     * @param args The arguments for the message
     */
    public void handleNotification(final String messageType, final String fallback,
            final Object... args) {
        String target = getConfigManager().getOption("notifications", messageType,
                fallback);

        if (target.startsWith("group:")) {
            target = getConfigManager().getOption("notifications",
                    target.substring(6), fallback);
        }

        if ("self".equals(target)) {
            addLine(messageType, args);
        }  else if (NOTIFICATION_SERVER.equals(target)) {
            getServer().addLine(messageType, args);
        } else if ("all".equals(target)) {
            getServer().addLineToAll(messageType, args);
        } else if ("active".equals(target)) {
            getServer().addLineToActive(messageType, args);
        } else if (target.startsWith("window:")) {
            final String windowName = target.substring(7);

            Window targetWindow = WindowManager.findCustomWindow(getServer().getFrame(), windowName);

            if (targetWindow == null) {
                targetWindow = new CustomWindow(windowName, windowName, getFrame()).getFrame();
            }

            targetWindow.addLine(messageType, args);
        } else if (target.startsWith("lastcommand:")) {
            final Object[] escapedargs = new Object[args.length];

            for (int i = 0; i < args.length; i++) {
                escapedargs[i] = "\\Q" + args[i] + "\\E";
            }

            final String command = String.format(target.substring(12), escapedargs);

            WritableFrameContainer best = this;
            long besttime = 0;

            final List<WritableFrameContainer> containers
                    = new ArrayList<WritableFrameContainer>();
            
            containers.add(getServer());
            containers.addAll(getServer().getChildren());

            for (WritableFrameContainer container: containers) {
                final long time
                        = container.getFrame().getCommandParser().getCommandTime(command);
                if (time > besttime) {
                    besttime = time;
                    best = container;
                }
            }

            best.addLine(messageType, args);
        } else if (target.startsWith(NOTIFICATION_CHANNEL + ":")) {
           final String channel = String.format(target.substring(8), args);

           if (getServer().hasChannel(channel)) {
               getServer().getChannel(channel).addLine(messageType, args);
           } else {
               addLine(messageType, args);
               Logger.userError(ErrorLevel.LOW,
                       "Invalid notification target for type " + messageType
                       + ": channel " + channel + " doesn't exist");
           }
        } else if (!"none".equals(target)) {
            addLine(messageType, args);
            Logger.userError(ErrorLevel.MEDIUM,
                    "Invalid notification target for type " + messageType + ": " + target);
        }
    }        
    
    /**
     * Returns a string[] containing the nickname/ident/host of a channel client.
     * 
     * @param client The channel client to check
     * @return A string[] containing displayable components
     */
    protected String[] getDetails(final ChannelClientInfo client) {
        if (client == null) {
            // WTF?
            Logger.appError(ErrorLevel.HIGH,
                    "Channel.getDetails called with null ChannelClientInfo",
                    new UnsupportedOperationException());
            return new String[]{"null", "null", "nullity.dmdirc.com"};
        }
        
        final String[] res = new String[3];
        res[0] = Styliser.CODE_NICKNAME + client.getNickname() + Styliser.CODE_NICKNAME;
        res[1] = client.getClient().getIdent();
        res[2] = client.getClient().getHost();
        
        if (getConfigManager().getOptionBool("ui", "shownickcoloursintext", false)) {
            final Map map = client.getMap();
            String prefix = null;
            Color colour;
            
            if (map.containsKey(ChannelClientProperty.TEXT_FOREGROUND)) {
                colour = (Color) map.get(ChannelClientProperty.TEXT_FOREGROUND);
                prefix = Styliser.CODE_HEXCOLOUR + ColourManager.getHex(colour);
                if (map.containsKey(ChannelClientProperty.TEXT_BACKGROUND)) {
                    colour = (Color) map.get(ChannelClientProperty.TEXT_BACKGROUND);
                    prefix = "," + ColourManager.getHex(colour);
                }
            }
            
            if (prefix != null) {
                res[0] = prefix + res[0] + Styliser.CODE_HEXCOLOUR;
            }
        }
        
        return res;
    }    
    
}
