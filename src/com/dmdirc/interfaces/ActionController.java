/*
 * Copyright (c) 2006-2015 DMDirc Developers
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

package com.dmdirc.interfaces;

import com.dmdirc.Precondition;
import com.dmdirc.actions.Action;
import com.dmdirc.actions.ActionGroup;
import com.dmdirc.interfaces.actions.ActionComparison;
import com.dmdirc.interfaces.actions.ActionComponent;
import com.dmdirc.interfaces.actions.ActionType;
import com.dmdirc.util.collections.MapList;

import java.util.List;
import java.util.Map;

/**
 * Manages all known actions, action types, comparisons, components, etc, and facilitates loading
 * and triggering of actions.
 */
public interface ActionController {

    /**
     * Registers an action with the manager.
     *
     * @param action The action to be registered
     */
    @Precondition("The specified action is not null")
    void addAction(final Action action);

    /**
     * Registers the specified group of actions with the manager.
     *
     * @param group The group of actions to be registered
     */
    void addGroup(final ActionGroup group);

    /**
     * Renames the specified group.
     *
     * @param oldName The old name of the group
     * @param newName The new name of the group
     */
    @Precondition({"The old name is non-null and not empty", "The old name is an existing group",
            "The new name is non-null and not empty", "The new name is not an existing group",
            "The old name does not equal the new name"})
    void changeGroupName(final String oldName, final String newName);

    /**
     * Creates a new group with the specified name.
     *
     * @param group The group to be created
     *
     * @return The newly created group
     */
    @Precondition({"The specified group is non-null and not empty",
            "The specified group is not an existing group"})
    ActionGroup createGroup(final String group);

    /**
     * Removes the group with the specified name.
     *
     * @param group The group to be removed
     */
    @Precondition({"The specified group is non-null and not empty",
            "The specified group is an existing group"})
    void deleteGroup(final String group);

    /**
     * Returns a list of action comparisons that are compatible with the specified class.
     *
     * @param target The class to be tested
     *
     * @return A list of compatible action comparisons
     */
    @Precondition("The specified target is not null")
    List<ActionComparison> findCompatibleComparisons(final Class<?> target);

    /**
     * Returns a list of action components that are compatible with the specified class.
     *
     * @param target The class to be tested
     *
     * @return A list of compatible action components
     */
    @Precondition("The specified target is not null")
    List<ActionComponent> findCompatibleComponents(final Class<?> target);

    /**
     * Returns a list of action types that are compatible with the one specified.
     *
     * @param type The type to be checked against
     *
     * @return A list of compatible action types
     */
    @Precondition("The specified type is not null")
    List<ActionType> findCompatibleTypes(final ActionType type);

    /**
     * Returns the action type specified by the given string, or null if it doesn't match a valid
     * registered action type.
     *
     * @param type The name of the action type to try and find
     *
     * @return The actiontype with the specified name, or null on failure
     */
    @Precondition("The specified type is non-null and not empty")
    ActionComparison getComparison(final String type);

    /**
     * Returns the action component specified by the given string, or null if it doesn't match a
     * valid registered action component.
     *
     * @param type The name of the action component to try and find
     *
     * @return The actioncomponent with the specified name, or null on failure
     */
    @Precondition("The specified type is non-null and not empty")
    ActionComponent getComponent(final String type);

    /**
     * Returns a map of type groups to types.
     *
     * @return A map of type groups to types
     */
    MapList<String, ActionType> getGroupedTypes();

    /**
     * Returns a map of groups to action lists.
     *
     * @return a map of groups to action lists
     */
    Map<String, ActionGroup> getGroupsMap();

    /**
     * Retrieves the action group with the specified name. A new group is created if it doesn't
     * already exist.
     *
     * @param name The name of the group to retrieve
     *
     * @return The corresponding ActionGroup
     */
    ActionGroup getOrCreateGroup(final String name);

    /**
     * Returns the action comparison specified by the given string, or null if it doesn't match a
     * valid registered action comparison.
     *
     * @param type The name of the action comparison to try and find
     *
     * @return The type with the specified name, or null on failure
     */
    ActionType getType(final String type);

    /**
     * Loads actions from the user's directory.
     */
    void loadUserActions();

    /**
     * Registers a set of action comparisons with the manager.
     *
     * @param comps An array of ActionComparisons to be registered
     */
    @Precondition("None of the specified ActionComparisons are null")
    void registerComparisons(final ActionComparison[] comps);

    /**
     * Registers a set of action components with the manager.
     *
     * @param comps An array of ActionComponents to be registered
     */
    @Precondition("None of the specified ActionComponents are null")
    void registerComponents(final ActionComponent[] comps);

    /**
     * Registers the specified default setting for actions.
     *
     * @param name  The name of the setting to be registered
     * @param value The default value for the setting
     */
    void registerSetting(final String name, final String value);

    /**
     * Registers a set of action types with the manager.
     *
     * @param newTypes An array of ActionTypes to be registered
     */
    @Precondition("None of the specified ActionTypes are null")
    void registerTypes(final ActionType[] newTypes);

    /**
     * Unregisters an action with the manager.
     *
     * @param action The action to be unregistered
     */
    @Precondition("The specified action is not null")
    void removeAction(final Action action);

    /**
     * Reregisters the specified action. Should be used when the action's triggers change.
     *
     * @param action The action to be reregistered
     */
    void reregisterAction(final Action action);

    /**
     * Saves all actions.
     */
    void saveAllActions();

    /**
     * Processes an event of the specified type.
     *
     * @param type      The type of the event to process
     * @param format    The format of the message that's going to be displayed for the event.
     *                  Actions may change this format.
     * @param arguments The arguments for the event
     *
     * @return True if the event should be processed, or false if an action listener has requested
     *         the event be skipped.
     */
    @Precondition({"The specified ActionType is not null",
            "The specified ActionType has a valid ActionMetaType",
            "The length of the arguments array equals the arity of the ActionType's " +
                    "ActionMetaType"})
    boolean triggerEvent(final ActionType type, final StringBuffer format, final Object... arguments);

}
