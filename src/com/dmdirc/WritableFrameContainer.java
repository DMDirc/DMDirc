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

package com.dmdirc;

import com.dmdirc.actions.ActionManager;
import com.dmdirc.actions.interfaces.ActionType;
import com.dmdirc.config.ConfigManager;
import com.dmdirc.logger.ErrorLevel;
import com.dmdirc.logger.Logger;
import com.dmdirc.ui.WindowManager;
import com.dmdirc.ui.interfaces.InputWindow;
import com.dmdirc.ui.interfaces.Window;

import java.util.ArrayList;
import java.util.List;

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
     * Creates a new WritableFrameContainer.
     * 
     * @param icon The icon to use for this container
     * @param name The name of this container
     * @param config The config manager for this container
     * @since 0.6.3m2
     */
    public WritableFrameContainer(final String icon, final String name, final ConfigManager config) {
        super(icon, name, config);
    }
    
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
     * Splits the specified line into chunks that contain a number of bytes
     * less than or equal to the value returned by {@link #getMaxLineLength()}.
     *
     * @param line The line to be split
     * @return An ordered list of chunks of the desired length
     */
    protected List<String> splitLine(final String line) {
        final List<String> result = new ArrayList<String>();

        if (line.indexOf('\n') > -1) {
            for (String part : line.split("\n")) {
                result.addAll(splitLine(part));
            }
        } else {
            final StringBuilder remaining = new StringBuilder(line);

            while (getMaxLineLength() > -1 && remaining.toString().getBytes().length
                    > getMaxLineLength()) {
                int number = Math.min(remaining.length(), getMaxLineLength());

                while (remaining.substring(0, number).getBytes().length > getMaxLineLength()) {
                    number--;
                }

                result.add(remaining.substring(0, number));
                remaining.delete(0, number);
            }

            result.add(remaining.toString());
        }

        return result;
    }
    
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
            if (getMaxLineLength() <= 0) {
                lines++;
            } else {
                lines += (int) Math.ceil(splitLine.getBytes().length
                        / (double) getMaxLineLength());
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

            if (!processNotificationArg(arg, messageArgs)) {
                messageArgs.add(arg);
            }
        }
        
        modifyNotificationArgs(actionArgs, messageArgs);

        ActionManager.processEvent(actionType, buffer, actionArgs.toArray());

        handleNotification(buffer.toString(), messageArgs.toArray());
    } 
    
    /**
     * Allows subclasses to modify the lists of arguments for notifications.
     * 
     * @param actionArgs The list of arguments to be passed to the actions system
     * @param messageArgs The list of arguments to be passed to the formatter
     */
    protected void modifyNotificationArgs(final List<Object> actionArgs,
            final List<Object> messageArgs) {
        // Do nothing
    }
    
    /**
     * Allows subclasses to process specific types of notification arguments.
     * 
     * @param arg The argument to be processed
     * @param args The list of arguments that any data should be appended to
     * @return True if the arg has been processed, false otherwise
     */
    protected boolean processNotificationArg(final Object arg, final List<Object> args) {
        return false;
    }
    
    /**
     * Handles general server notifications (i.e., ones not tied to a
     * specific window). The user can select where the notifications should
     * go in their config.
     *
     * @param messageType The type of message that is being sent
     * @param args The arguments for the message
     */
    public void handleNotification(final String messageType, final Object... args) {        
        despatchNotification(messageType, getConfigManager().hasOptionString("notifications",
                messageType) ? getConfigManager().getOption("notifications", messageType)
                : "self", args);
    }
    
    /**
     * Despatches a notification of the specified type to the specified target.
     * 
     * @param messageType The type of the message that is being sent
     * @param messageTarget The target of the message
     * @param args The arguments for the message
     */
    protected void despatchNotification(final String messageType,
            final String messageTarget, final Object... args) {
        
        String target = messageTarget;
        String format = messageType;
        
        if (target.startsWith("format:")) {
            format = target.substring(7);
            format = format.substring(0, format.indexOf(':'));
            target = target.substring(8 + format.length());
        }

        if (target.startsWith("group:")) {
            target = getConfigManager().hasOptionString("notifications", target.substring(6))
                    ? getConfigManager().getOption("notifications", target.substring(6))
                    : "self";
        }
        
        if (target.startsWith("fork:")) {
            for (String newtarget : target.substring(5).split("\\|")) {
                despatchNotification(format, newtarget, args);
            }
            
            return;
        }

        if ("self".equals(target)) {
            addLine(format, args);
        }  else if (NOTIFICATION_SERVER.equals(target)) {
            getServer().addLine(format, args);
        } else if ("all".equals(target)) {
            getServer().addLineToAll(format, args);
        } else if ("active".equals(target)) {
            getServer().addLineToActive(format, args);
        } else if (target.startsWith("window:")) {
            final String windowName = target.substring(7);

            Window targetWindow = WindowManager.findCustomWindow(getServer().getFrame(),
                    windowName);

            if (targetWindow == null) {
                targetWindow = new CustomWindow(windowName, windowName,
                        getServer().getFrame()).getFrame();
            }

            targetWindow.addLine(format, args);
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

            for (WritableFrameContainer container : containers) {
                final long time
                        = container.getFrame().getCommandParser().getCommandTime(command);
                if (time > besttime) {
                    besttime = time;
                    best = container;
                }
            }

            best.addLine(format, args);
        } else if (target.startsWith(NOTIFICATION_CHANNEL + ":")) {
           final String channel = String.format(target.substring(8), args);

           if (getServer().hasChannel(channel)) {
               getServer().getChannel(channel).addLine(messageType, args);
           } else {
               addLine(format, args);
               Logger.userError(ErrorLevel.LOW,
                       "Invalid notification target for type " + messageType
                       + ": channel " + channel + " doesn't exist");
           }
        } else if (!"none".equals(target)) {
            addLine(format, args);
            Logger.userError(ErrorLevel.MEDIUM,
                    "Invalid notification target for type " + messageType + ": " + target);
        }
    }          
    
}
