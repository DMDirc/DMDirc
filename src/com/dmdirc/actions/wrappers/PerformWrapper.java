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

package com.dmdirc.actions.wrappers;

import com.dmdirc.actions.Action;
import com.dmdirc.actions.interfaces.ActionComponent;
import com.dmdirc.actions.ActionCondition;
import com.dmdirc.actions.ActionGroup;
import com.dmdirc.actions.interfaces.ActionType;
import com.dmdirc.actions.CoreActionComparison;
import com.dmdirc.actions.CoreActionComponent;
import com.dmdirc.actions.CoreActionType;
import com.dmdirc.logger.ErrorLevel;
import com.dmdirc.logger.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * An action wrapper for performs.
 *
 * @author Chris
 */
public class PerformWrapper extends ActionGroup {
    
    /** A singleton instance of the Perform Wrapper. */
    private static PerformWrapper me = new PerformWrapper();
    
    /**
     * Creates a new instance of PerformWrapper.
     */
    private PerformWrapper() {
        super("performs");
    }
    
    /**
     * Retrieves a singleton instance of this perform wrapper.
     *
     * @return A singleton instance of PerformWrapper
     */
    public static PerformWrapper getPerformWrapper() {
        return me;
    }
    
    /** {@inheritDoc} */
    @Override
    public void add(final Action action) {
        if (action.getTriggers().length == 1
                && action.getTriggers()[0] == CoreActionType.SERVER_CONNECTED
                && action.getConditions().size() == 1
                && (action.getConditions().get(0).getComponent() == CoreActionComponent.SERVER_NETWORK
                || action.getConditions().get(0).getComponent() == CoreActionComponent.SERVER_NAME)) {
            super.add(action);
        } else {
            Logger.userError(ErrorLevel.MEDIUM, "Invalid perform action: " + action.getName());
        }
    }
    
    /**
     * Retrieve the action that handles the perform for the specified server,
     * or null if no such action exists.
     *
     * @param server The server to look for
     * @return The action that handles the server's perform, or null
     */
    public Action getActionForServer(final String server) {
        return getAction(CoreActionComponent.SERVER_NAME, server);
    }

    /**
     * Retrieve the action that handles the perform for the specified network,
     * or null if no such action exists.
     *
     * @param network The network to look for
     * @return The action that handles the network's perform, or null
     */    
    public Action getActionForNetwork(final String network) {
        return getAction(CoreActionComponent.SERVER_NETWORK, network);
    }
    
    /**
     * Creates a new, empty, perform wrapper for the specified server.
     * 
     * @param server The server to create the action for
     * @return The new perform wrapper action
     */
    public Action createActionForServer(final String server) {
        return createAction(server, "");
    }

    /**
     * Creates a new, empty, perform wrapper for the specified network.
     * 
     * @param network The network to create the action for
     * @return The new perform wrapper action
     */    
    public Action createActionForNetwork(final String network) {
        return createAction("", network);
    }
    
    /**
     * Creates a new, empty, perform wrapper for the specified server or
     * network. Note that both server and network must be specified, and
     * exactly one of them must be empty.
     * 
     * @param server The server to create the action for
     * @param network The network to create the action for
     * @return The new perform wrapper action
     */    
    private Action createAction(final String server, final String network) {
        final List<ActionCondition> conditions = new ArrayList<ActionCondition>();
        final CoreActionComponent component =
                server.isEmpty() ? CoreActionComponent.SERVER_NETWORK
                : CoreActionComponent.SERVER_NAME;
        
        conditions.add(new ActionCondition(0, component, 
                CoreActionComparison.STRING_EQUALS, server + network));
        
        return new Action(getName(), server + network,
                new ActionType[]{CoreActionType.SERVER_CONNECTED},
                new String[0], conditions, null);
    }
    
    /**
     * Retrieve an action with a condition that checks the specified component,
     * and matches it against the specified target.
     *
     * @param component The action component to look for
     * @param target The string the component is matched against
     * @return The matching action if one exists, or null
     */    
    private Action getAction(final ActionComponent component, final String target) {
        for (Action action : this) {
            if (action.getConditions().get(0).getComponent() == component
                    && action.getConditions().get(0).getTarget().equalsIgnoreCase(target)) {
                return action;
            }
        }
        
        return null;
    }
    
    /** {@inheritDoc} */
    @Override
    public boolean isDelible() {
        return false;
    }
    
    /** {@inheritDoc} */
    @Override
    public String getDescription() {
        return "Performs allow you to automatically execute commands when"
                + " you connect to a specific server or network. You can edit"
                + " the perform for the current server or network in the "
                + "\"Server Settings\" dialog, which can be accessed through "
                + "the Settings menu.";
    }
    
}
