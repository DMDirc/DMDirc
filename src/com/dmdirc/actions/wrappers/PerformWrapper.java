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

package com.dmdirc.actions.wrappers;

import com.dmdirc.Precondition;
import com.dmdirc.Server;
import com.dmdirc.actions.Action;
import com.dmdirc.actions.ActionComponentChain;
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

    /** The component name for per-profile perform conditions. */
    private static String PP_COMP_NAME = "SERVER_IDENTITY.IDENTITY_NAME";
    
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
                && checkConditions(action.getConditions())) {
            super.add(action);
        } else {
            Logger.userError(ErrorLevel.MEDIUM, "Invalid perform action: " + action.getName());
        }
    }

    /**
     * Checks that the specified conditions are valid for a perform action.
     *
     * @param conditions The conditions to be checked
     * @since 0.6.3m2
     * @return True if the conditions are valid, false otherwise
     */
    protected boolean checkConditions(final List<ActionCondition> conditions) {
        boolean target = false, profile = false;

        for (ActionCondition condition : conditions) {
            if ((condition.getComponent() == CoreActionComponent.SERVER_NETWORK
                    || condition.getComponent() == CoreActionComponent.SERVER_NAME)
                    && !target) {
                target = true;
            } else if (condition.getComponent() instanceof ActionComponentChain
                    && PP_COMP_NAME.equals(condition.getComponent().toString())
                    && !profile) {
                profile = true;
            } else {
                return false;
            }
        }

        return target || profile;
    }

    /**
     * Determines if the specified action is a per-profile perform, as opposed
     * to a generic perform.
     *
     * @param action The action to be tested
     * @return True if the action is per-profile, false otherwise
     * @since 0.6.3
     */
    public boolean isPerProfilePerform(final Action action) {
        return contains(action) && action.getConditions().size() == 2;
    }

    /**
     * Retrieves the name of the profile that is targetted by the specified
     * per-profile perform action.
     *
     * @param action The action whose perform should be retrieved
     * @return The action's targetted profile name
     * @since 0.6.3
     */
    @Precondition("The action is a per-profile perform")
    public String getProfileName(final Action action) {
        Logger.assertTrue(isPerProfilePerform(action));

        for (ActionCondition condition : action.getConditions()) {
            if (PP_COMP_NAME.equals(condition.getComponent().toString())) {
                return condition.getTarget();
            }
        }

        throw new IllegalStateException("Profile component not found in action");
    }

    /**
     * Retrieve the action that handles the perform for the specified server,
     * or null if no such action exists.
     *
     * @param server The server to look for
     * @return The action that handles the server's perform, or null
     */
    public Action getActionForServer(final String server) {
        return getActionForServer(server, null);
    }
    
    /**
     * Retrieve the action that handles the perform for the specified server,
     * or null if no such action exists.
     *
     * @param server The server to look for
     * @param profile The name of the profile the perform works for
     * @return The action that handles the servers's perform for the specified
     * profile, or null
     * @since 0.6.3
     */
    public Action getActionForServer(final String server, final String profile) {
        return getAction(CoreActionComponent.SERVER_NAME, server, profile);
    }

    /**
     * Retrieve the action that handles the perform for the specified network,
     * or null if no such action exists.
     *
     * @param network The network to look for
     * @return The action that handles the network's perform, or null
     */
    public Action getActionForNetwork(final String network) {
        return getActionForNetwork(network, null);
    }

    /**
     * Retrieve the action that handles the perform for the specified network,
     * or null if no such action exists.
     *
     * @param network The network to look for
     * @param profile The name of the profile the perform works for
     * @return The action that handles the network's perform for the specified
     * profile, or null
     * @since 0.6.3
     */    
    public Action getActionForNetwork(final String network, final String profile) {
        return getAction(CoreActionComponent.SERVER_NETWORK, network, profile);
    }
    
    /**
     * Creates a new, empty, perform wrapper for the specified server.
     * 
     * @param server The server to create the action for
     * @return The new perform wrapper action
     */
    public Action createActionForServer(final String server) {
        return createActionForServer(server, null);
    }

    /**
     * Creates a new, empty, perform wrapper for the specified server, which
     * is only applicable when the specified profile is in use.
     *
     * @param server The server to create the action for
     * @param profile The name of the profile which must be in use
     * @return The new perform wrapper action
     * @since 0.6.3
     */
    public Action createActionForServer(final String server, final String profile) {
        return createAction(server, "", profile);
    }

    /**
     * Creates a new, empty, perform wrapper for the specified network.
     * 
     * @param network The network to create the action for
     * @return The new perform wrapper action
     */    
    public Action createActionForNetwork(final String network) {
        return createActionForNetwork(network, null);
    }

    /**
     * Creates a new, empty, perform wrapper for the specified network, which
     * is only applicable when the specified profile is in use.
     *
     * @param network The network to create the action for
     * @param profile The name of the profile which must be in use
     * @return The new perform wrapper action
     * @since 0.6.3
     */
    public Action createActionForNetwork(final String network, final String profile) {
        return createAction("", network, profile);
    }
    
    /**
     * Creates a new, empty, perform wrapper for the specified server or
     * network. Note that both server and network must be specified, and
     * exactly one of them must be empty.
     * 
     * @param server The server to create the action for
     * @param network The network to create the action for
     * @param profile The profile the action is for (or null if "global")
     * @since 0.6.3
     * @return The new perform wrapper action
     */    
    private Action createAction(final String server, final String network,
            final String profile) {
        final List<ActionCondition> conditions = new ArrayList<ActionCondition>();
        final CoreActionComponent component =
                server.isEmpty() ? CoreActionComponent.SERVER_NETWORK
                : CoreActionComponent.SERVER_NAME;
        
        conditions.add(new ActionCondition(0, component, 
                CoreActionComparison.STRING_EQUALS, server + network));

        if (profile != null) {
            conditions.add(new ActionCondition(0,
                    new ActionComponentChain(Server.class, PP_COMP_NAME),
                    CoreActionComparison.STRING_EQUALS, profile));
        }
        
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
     * @param profile The name of the profile that the action must target, or
     * null for a non-profile specific action
     * @since 0.6.3
     * @return The matching action if one exists, or null
     */    
    private Action getAction(final ActionComponent component, final String target,
            final String profile) {
        for (Action action : this) {
            int matches = profile == null ? 1 : 2;

            for (ActionCondition condition : action.getConditions()) {
                if (condition.getComponent() == component
                        && condition.getTarget().equalsIgnoreCase(target)) {
                    matches--;
                } else if (profile != null
                        && PP_COMP_NAME.equals(condition.getComponent().toString())
                        && condition.getTarget().equalsIgnoreCase(profile)) {
                    matches--;
                }
            }

            if (matches == 0) {
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
