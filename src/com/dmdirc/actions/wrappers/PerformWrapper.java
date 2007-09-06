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

package com.dmdirc.actions.wrappers;

import com.dmdirc.actions.Action;
import com.dmdirc.actions.ActionComponent;
import com.dmdirc.actions.ActionManager;
import com.dmdirc.actions.CoreActionComponent;
import com.dmdirc.actions.CoreActionType;
import com.dmdirc.logger.ErrorLevel;
import com.dmdirc.logger.Logger;

/**
 * An action wrapper for performs.
 *
 * @author Chris
 */
public class PerformWrapper extends ActionWrapper {
    
    private static PerformWrapper me = new PerformWrapper();
    
    /**
     * Creates a new instance of PerformWrapper.
     */
    private PerformWrapper() {
        super();
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
    public String getGroupName() {
        return "performs";
    }
    
    /** {@inheritDoc} */
    @Override
    public void registerAction(final Action action) {
        if (action.getTriggers().length == 1
                && action.getTriggers()[0] == CoreActionType.SERVER_CONNECTED
                && action.getConditions().size() == 1
                && (action.getConditions().get(0).getComponent() == CoreActionComponent.SERVER_NETWORK
                || action.getConditions().get(0).getComponent() == CoreActionComponent.SERVER_NAME)) {
            super.registerAction(action);
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
     * Retrieve an action with a condition that checks the specified component,
     * and matches it against the specified target.
     *
     * @param component The action component to look for
     * @param target The string the component is matched against
     * @return The matching action if one exists, or null
     */    
    private Action getAction(final ActionComponent component, final String target) {
        for (Action action : actions) {
            if (action.getConditions().get(0).getComponent() == component
                    && action.getConditions().get(0).getTarget().equalsIgnoreCase(target)) {
                return action;
            }
        }
        
        return null;
    }
    
}
