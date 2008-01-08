/*
 * Copyright (c) 2006-2008 Chris Smith, Shane Mc Cormack, Gregory Holmes
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

package com.dmdirc.actions.wrappers;

import com.dmdirc.Server;
import com.dmdirc.ServerManager;
import com.dmdirc.actions.Action;
import com.dmdirc.actions.ActionCondition;
import com.dmdirc.actions.CoreActionType;
import com.dmdirc.commandparser.CommandManager;
import com.dmdirc.logger.ErrorLevel;
import com.dmdirc.logger.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * Encapsulates alias actions.
 *
 * @author chris
 */
public final class AliasWrapper extends ActionWrapper {
    
    /** Singleton instance of the alias wrapper. */
    private static AliasWrapper me;
    
    /** A list of registered alias names. */
    private final List<String> aliases = new ArrayList<String>();
    
    /**
     * Creates a new instance of AliasWrapper.
     */
    private AliasWrapper() {
        super();
    }
    
    /**
     * Retrieves a singleton instance of this alias wrapper.
     *
     * @return A singleton instance of AliasWrapper
     */
    public static synchronized AliasWrapper getAliasWrapper() {
        if (me == null) {
            me = new AliasWrapper();
        }
        
        return me;
    }
    
    /**
     * Retrieves a list of alias names registered with this wrapper.
     * 
     * @return A list of alias names
     */
    public List<String> getAliases() {
        return new ArrayList<String>(aliases);
    }
    
    /** {@inheritDoc} */
    @Override
    public void registerAction(final Action action) {
        if (action.getTriggers()[0].equals(CoreActionType.UNKNOWN_COMMAND)) {
            super.registerAction(action);
            
            final String commandName = getCommandName(action);
            
            aliases.add(commandName);
            
            for (Server server : ServerManager.getServerManager().getServers()) {
                server.getTabCompleter().addEntry(commandName);
            }
        } else {
            Logger.userError(ErrorLevel.MEDIUM, "Invalid alias action: " + action.getName());
        }
    }
    
    /** {@inheritDoc} */
    @Override
    public void unregisterAction(final Action action) {
        if (action.getTriggers()[0].equals(CoreActionType.UNKNOWN_COMMAND)) {
            super.unregisterAction(action);
            
            final String commandName = getCommandName(action);
            
            aliases.remove(commandName);
            
            for (Server server : ServerManager.getServerManager().getServers()) {
                server.getTabCompleter().removeEntry(commandName);
            }
        }
    }
    
    /** {@inheritDoc} */
    @Override
    public String getGroupName() {
        return "aliases";
    }
    
    /**
     * Retrieves the command name of the specified alias action.
     *
     * @param action The action whose name is to be determined
     * @return The command name for the specified alias
     */
    public static String getCommandName(final Action action) {
        for (ActionCondition condition : action.getConditions()) {
            if (condition.getArg() == 1) {
                return CommandManager.getCommandChar() + condition.getTarget();
            }
        }
        
        // How can we have an alias without a command name?
        return "";
    }
    
}
