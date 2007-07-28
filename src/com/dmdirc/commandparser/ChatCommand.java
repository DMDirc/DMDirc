/*
 * ChatCommand.java
 *
 * Created on 28-Jul-2007, 20:42:42
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.dmdirc.commandparser;

import com.dmdirc.MessageTarget;
import com.dmdirc.Server;
import com.dmdirc.ui.interfaces.InputWindow;

/**
 * Represents a command that can be performed in the context of a "chat" window
 * (i.e., a channel or a query).
 *
 * @author Chris
 */
public abstract class ChatCommand extends Command {
    
    /**
     * Executes this command.
     * 
     * @param origin The window in which the command was typed
     * @param server The server instance that this command is being executed on
     * @param target The target of this command
     * @param isSilent Whether this command is silenced or not
     * @param args Arguments passed to this command
     */
    public abstract void execute(InputWindow origin, Server server, MessageTarget target,
            boolean isSilent, String... args);
}
