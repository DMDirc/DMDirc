/*
 * Copyright (c) 2006-2013 DMDirc Developers
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
import com.dmdirc.actions.Action;
import com.dmdirc.actions.ActionComponentChain;
import com.dmdirc.actions.ActionCondition;
import com.dmdirc.actions.ActionGroup;
import com.dmdirc.actions.CoreActionComparison;
import com.dmdirc.actions.CoreActionComponent;
import com.dmdirc.actions.CoreActionType;
import com.dmdirc.interfaces.actions.ActionComponent;
import com.dmdirc.interfaces.actions.ActionType;
import com.dmdirc.logger.ErrorLevel;
import com.dmdirc.logger.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * An action wrapper for performs.
 */
public class PerformWrapper extends ActionGroup {

    /** A singleton instance of the Perform Wrapper. */
    private static final PerformWrapper ME = new PerformWrapper();

    /** The component name for per-profile perform conditions. */
    private static final String PP_COMP_NAME = "SERVER_PROFILE.IDENTITY_NAME";

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
        return ME;
    }

    /** {@inheritDoc} */
    @Override
    public void add(final Action action) {
        if (action.getTriggers().length != 1) {
            Logger.userError(ErrorLevel.MEDIUM,
                    "Invalid perform action: " + action.getName(),
                    "Perform actions may only have one trigger");
        } else if (action.getTriggers()[0] != CoreActionType.SERVER_CONNECTED) {
            Logger.userError(ErrorLevel.MEDIUM,
                    "Invalid perform action: " + action.getName(),
                    "Perform actions must be triggered when a server connects");
        } else if (!checkConditions(action.getConditions())) {
            Logger.userError(ErrorLevel.MEDIUM,
                    "Invalid perform action: " + action.getName(),
                    "Perform actions must have exactly one or two conditions, "
                    + "one may target the server's name or network, and one may "
                    + "target the server's profile name. No other targets are "
                    + "allowed.");
        } else {
            synchronized (this) {
                super.add(action);
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public void deleteAction(final Action action) {
        synchronized (this) {
            super.deleteAction(action);
        }
    }

    /**
     * Sets the perform for the specified target of the specified type.
     * If the specified perform is empty - that is, any non-null elements are
     * empty Strings - then the perform is removed. If a profile is specified,
     * the perform will only be executed for that profile.
     *
     * @param perform The perform to be set
     * @param content The new content of that perform
     * @since 0.6.4
     */
    public void setPerform(final PerformDescription perform, final String[] content) {
        synchronized (this) {
            Action action = getAction(perform.getType() == PerformType.NETWORK
                    ? CoreActionComponent.SERVER_NETWORK : CoreActionComponent.SERVER_NAME,
                    perform.getTarget(), perform.getProfile());

            final boolean empty = isEmpty(content);

            if (action == null && !empty) {
                // They want to set a perform but we don't have an action
                action = createAction(
                        perform.getType() == PerformType.SERVER ? perform.getTarget() : "",
                        perform.getType() == PerformType.NETWORK ? perform.getTarget() : "",
                        perform.getProfile());
                action.setResponse(content);
                action.save();
            }

            if (action != null) {
                if (empty) {
                    // They want to clear the perform but we have an action
                    deleteAction(action);
                } else {
                    // They want to set a perform and we have an action
                    action.setResponse(content);
                    action.save();
                }
            }
        }
    }

    /**
     * Retrieves the perform for the relevant target. If no such perform exists,
     * a zero-length array is returned.
     *
     * @param perform The perform to be retrieved
     * @return The corresponding perform, or a zero-length array if none set
     * @since 0.6.4
     */
    public String[] getPerform(final PerformDescription perform) {
        final Action action = getAction(perform.getType() == PerformType.NETWORK
                ? CoreActionComponent.SERVER_NETWORK : CoreActionComponent.SERVER_NAME,
                perform.getTarget(), perform.getProfile());

        if (action == null || action.getResponse() == null) {
            return new String[0];
        } else {
            return action.getResponse();
        }
    }

    /**
     * Determines if the specified perform is "empty". An empty perform is one
     * that does not contain any non-empty Strings.
     *
     * @param perform The perform to test
     * @return True if the perform is empty, false otherwise
     * @since 0.6.4
     */
    private static boolean isEmpty(final String[] perform) {
        for (String part : perform) {
            if (part != null && !part.isEmpty()) {
                return false;
            }
        }

        return true;
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

        return new Action(getName(), server + network
                + (profile == null ? "" : " - " + profile),
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

    /**
     * Describes one specific perform.
     *
     * @since 0.6.4
     */
    public static class PerformDescription {

        /** The type of the perform being described. */
        private final PerformType type;
        /** The target of the perform. */
        private final String target;
        /** The profile (if any) of the perform. */
        private final String profile;

        /**
         * Creates a new perform description with the specified arguments.
         *
         * @param type The type of the perform in question
         * @param target The target of the perform
         * @param profile The profile of the perform (or null)
         */
        public PerformDescription(final PerformType type, final String target,
                final String profile) {
            this.type = type;
            this.target = target;
            this.profile = profile;

            if (target == null) {
                throw new NullPointerException("Target may not be null");
            }
        }

        /**
         * Creates a new perform description with the specified arguments.
         *
         * @param type The type of the perform in question
         * @param target The target of the perform
         */
        public PerformDescription(final PerformType type, final String target) {
            this.type = type;
            this.target = target;
            this.profile = null;

            if (target == null) {
                throw new NullPointerException("Target may not be null");
            }
        }

        /**
         * Retrieves the profile of this perform.
         *
         * @return This perform's profile
         */
        public String getProfile() {
            return profile;
        }

        /**
         * Retrieves the target of this perform.
         *
         * @return This perform's target
         */
        public String getTarget() {
            return target;
        }

        /**
         * Retrieves the type of this perform.
         *
         * @return This perform's type
         */
        public PerformType getType() {
            return type;
        }

        /** {@inheritDoc} */
        @Override
        public boolean equals(final Object obj) {
            if (obj == null || getClass() != obj.getClass()) {
                return false;
            }

            final PerformDescription other = (PerformDescription) obj;

            if (this.type != other.type || !this.target.equals(other.target)) {
                return false;
            }

            if ((this.profile == null) ? (other.profile != null)
                    : !this.profile.equals(other.profile)) {
                return false;
            }

            return true;
        }

        /** {@inheritDoc} */
        @Override
        public int hashCode() {
            int hash = 7;
            hash = 89 * hash + (this.type != null ? this.type.hashCode() : 0);
            hash = 89 * hash + (this.target != null ? this.target.hashCode() : 0);
            hash = 89 * hash + (this.profile != null ? this.profile.hashCode() : 0);
            return hash;
        }

    }

}
