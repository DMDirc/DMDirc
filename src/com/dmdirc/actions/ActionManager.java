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

package com.dmdirc.actions;

import com.dmdirc.Main;
import com.dmdirc.actions.wrappers.ActionWrapper;
import com.dmdirc.actions.wrappers.AliasWrapper;
import com.dmdirc.actions.wrappers.PerformWrapper;
import com.dmdirc.config.ConfigChangeListener;
import com.dmdirc.config.IdentityManager;
import com.dmdirc.logger.ErrorLevel;
import com.dmdirc.logger.Logger;
import com.dmdirc.plugins.PluginManager;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Manages all actions for the client.
 *
 * @author chris
 */
public final class ActionManager {
    
    /** A list of registered action types. */
    private final static List<ActionType> actionTypes = new ArrayList<ActionType>();
    
    /** A list of registered action components. */
    private final static List<ActionComponent> actionComponents = new ArrayList<ActionComponent>();
    
    /** A list of registered action comparisons. */
    private final static List<ActionComparison> actionComparisons = new ArrayList<ActionComparison>();
    
    /** A list of all action wrappers that have registered with us. */
    private final static List<ActionWrapper> actionWrappers = new ArrayList<ActionWrapper>();
    
    /** A map linking types and a list of actions that're registered for them. */
    private final static Map<ActionType, List<Action>> actions = new HashMap<ActionType, List<Action>>();
    
    /** A map linking groups and a list of actions that're in them. */
    private final static Map<String, List<Action>> groups = new HashMap<String, List<Action>>();
    
    /** A map of the action type groups to the action types within. */
    private final static Map<String, List<ActionType>> actionTypeGroups
            = new LinkedHashMap<String, List<ActionType>>();
    
    /** Indicates whether or not user actions should be killed (not processed). */
    private static boolean killSwitch
            = IdentityManager.getGlobalConfig().getOptionBool("actions", "killswitch", false);
   
    /** Creates a new instance of ActionManager. */
    private ActionManager() {
        // Shouldn't be instansiated
    }
    
    /**
     * Initialises the action manager.
     */
    public static void init() {
        registerActionTypes(CoreActionType.values());
        registerActionComparisons(CoreActionComparison.values());
        registerActionComponents(CoreActionComponent.values());
        
        registerWrapper(AliasWrapper.getAliasWrapper());
        registerWrapper(PerformWrapper.getPerformWrapper());
        
        IdentityManager.getGlobalConfig().addChangeListener("actions", "killswitch",
                new ConfigChangeListener() {
            public void configChanged(final String domain, final String key) {
                killSwitch = IdentityManager.getGlobalConfig().getOptionBool("actions", "killswitch", false);
            }
        });
    }
    
    /**
     * Registers the specified action wrapper with the manager.
     *
     * @param wrapper The wrapper to be registered
     */
    public static void registerWrapper(final ActionWrapper wrapper) {
        Logger.doAssertion(wrapper != null && wrapper.getGroupName() != null
                 && !wrapper.getGroupName().isEmpty());
        
        actionWrappers.add(wrapper);
    }
    
    /**
     * Registers a set of actiontypes with the manager.
     *
     * @param types An array of ActionTypes to be registered
     */
    public static void registerActionTypes(final ActionType[] types) {
        for (ActionType type : types) {
            Logger.doAssertion(type != null);
            
            if (!actionTypeGroups.containsKey(type.getType().getGroup())) {
                actionTypeGroups.put(type.getType().getGroup(), new ArrayList<ActionType>());
            }
            
            actionTypes.add(type);
            actionTypeGroups.get(type.getType().getGroup()).add(type);
        }
    }
    
    /**
     * Registers a set of action components with the manager.
     *
     * @param comps An array of ActionComponents to be registered
     */
    public static void registerActionComponents(final ActionComponent[] comps) {
        for (ActionComponent comp : comps) {
            Logger.doAssertion(comp != null);
            
            actionComponents.add(comp);
        }
    }
    
    /**
     * Registers a set of action comparisons with the manager.
     *
     * @param comps An array of ActionComparisons to be registered
     */
    public static void registerActionComparisons(final ActionComparison[] comps) {
        for (ActionComparison comp : comps) {
            Logger.doAssertion(comp != null);
            
            actionComparisons.add(comp);
        }
    }
    
    /**
     * Returns a map of groups to action lists.
     *
     * @return a map of groups to action lists
     */
    public static Map<String, List<Action>> getGroups() {
        return groups;
    }
    
    /**
     * Returns a map of type groups to types.
     *
     * @return A map of type groups to types
     */
    public static Map<String, List<ActionType>> getTypeGroups() {
        return actionTypeGroups;
    }
    
    /**
     * Loads actions from the user's directory.
     */
    public static void loadActions() {
        actions.clear();
        groups.clear();
        
        for (ActionWrapper wrapper : actionWrappers) {
            wrapper.clearActions();
        }
        
        final File dir = new File(getDirectory());
        
        if (!dir.exists()) {
            try {
                dir.mkdirs();
                dir.createNewFile();
            } catch (IOException ex) {
                Logger.userError(ErrorLevel.HIGH, "I/O error when creating actions directory: "
                        + ex.getMessage());
            }
        }
        
        if (dir.listFiles() == null) {
            Logger.userError(ErrorLevel.MEDIUM, "Unable to load user action files");
        } else {
            for (File file : dir.listFiles()) {
                if (file.isDirectory()) {
                    if (!isWrappedGroup(file.getName())) {
                        groups.put(file.getName(), new ArrayList<Action>());
                    }
                    
                    loadActions(file);
                }
            }
        }
    }
    
    /**
     * Retrieves the action wrapper with the specified group name.
     *
     * @param name The group name to find
     * @return An ActionWrapper with the specified group name, or null
     */
    private static ActionWrapper getWrapper(final String name) {
        Logger.doAssertion(name != null && !name.isEmpty());
        
        for (ActionWrapper wrapper : actionWrappers) {
            if (name.equals(wrapper.getGroupName())) {
                return wrapper;
            }
        }
        
        return null;
    }
    
    /**
     * Determines whether the specified group name is one used by an action
     * wrapper.
     *
     * @param name The group name to test
     * @return True if the group is part of a wrapper, false otherwise
     */
    private static boolean isWrappedGroup(final String name) {
        Logger.doAssertion(name != null && !name.isEmpty());
        
        return getWrapper(name) != null;
    }
    
    /**
     * Loads action files from a specified group directory.
     *
     * @param dir The directory to scan.
     */
    private static void loadActions(final File dir) {
        Logger.doAssertion(dir != null && dir.isDirectory());
        
        for (File file : dir.listFiles()) {
            new Action(dir.getName(), file.getName());
        }
    }
    
    /**
     * Registers an action with the manager.
     *
     * @param action The action to be registered
     */
    public static void registerAction(final Action action) {
        Logger.doAssertion(action != null);
        
        for (ActionType trigger : action.getTriggers()) {
            if (!actions.containsKey(trigger)) {
                actions.put(trigger, new ArrayList<Action>());
            }
            
            actions.get(trigger).add(action);
        }
        
        if (isWrappedGroup(action.getGroup())) {
            getWrapper(action.getGroup()).registerAction(action);
        } else {
            if (!groups.containsKey(action.getGroup())) {
                groups.put(action.getGroup(), new ArrayList<Action>());
            }
            
            groups.get(action.getGroup()).add(action);
        }
    }
    
    /**
     * Unregisters an action with the manager.
     *
     * @param action The action to be unregistered
     */
    public static void unregisterAction(final Action action) {
        Logger.doAssertion(action != null);
        
        for (Map.Entry<ActionType, List<Action>> map : actions.entrySet()) {
            if (map.getValue().contains(action)) {
                map.getValue().remove(action);
            }
        }
        
        for (Map.Entry<String, List<Action>> map : groups.entrySet()) {
            if (map.getValue().contains(action)) {
                map.getValue().remove(action);
            }
        }
    }
    
    /**
     * Reregisters the specified action. Should be used when the action's
     * triggers change.
     * 
     * @param action The action to be reregistered
     */
    public static void reregisterAction(final Action action) {
        unregisterAction(action);
        reregisterAction(action);
    }
    
    /**
     * Deletes the specified action.
     *
     * @param action The action to be deleted
     */
    public static void deleteAction(final Action action) {
        Logger.doAssertion(action != null);
        
        unregisterAction(action);
        
        action.delete();
    }
    
    /**
     * Processes an event of the specified type.
     *
     * @param type The type of the event to process
     * @param format The format of the message that's going to be displayed for
     * the event. Actions may change this format.
     * @param arguments The arguments for the event
     */
    public static void processEvent(final ActionType type,
            final StringBuffer format, final Object ... arguments) {
        Logger.doAssertion(type != null &&type.getType() != null 
                && type.getType().getArity() == arguments.length);
        
        PluginManager.getPluginManager().processEvent(type, format, arguments);
        
        if (!killSwitch) {
            triggerActions(type, format, arguments);
        }
    }
    
    /**
     * Triggers actions that respond to the specified type.
     *
     * @param type The type of the event to process
     * @param format The format of the message that's going to be displayed for
     * the event. Actions may change this format.*
     * @param arguments The arguments for the event
     */
    private static void triggerActions(final ActionType type,
            final StringBuffer format, final Object ... arguments) {
        Logger.doAssertion(type != null);
        
        if (actions.containsKey(type)) {
            for (Action action : actions.get(type)) {
                action.trigger(format, arguments);
            }
        }
    }
    
    /**
     * Returns the directory that should be used to store actions.
     *
     * @return The directory that should be used to store actions
     */
    public static String getDirectory() {
        return Main.getConfigDir() + "actions" + System.getProperty("file.separator");
    }
    
    /**
     * Creates a new group with the specified name.
     *
     * @param group The group to be created
     */
    public static void makeGroup(final String group) {
        Logger.doAssertion(group != null && !group.isEmpty()
               && !groups.containsKey(group));
        
        if (new File(getDirectory() + group).mkdir()) {
            groups.put(group, new ArrayList<Action>());
        }
    }
    
    /**
     * Removes the group with the specified name.
     *
     * @param group The group to be removed
     */
    public static void removeGroup(final String group) {
        Logger.doAssertion(group != null && !group.isEmpty()
                && groups.containsKey(group));
        
        for (Action action : new ArrayList<Action>(groups.get(group))) {
            unregisterAction(action);
        }
        
        final File dir = new File(getDirectory() + group);
        
        for (File file : dir.listFiles()) {
            if (!file.delete()) {
                Logger.userError(ErrorLevel.MEDIUM, "Unable to remove file: "
                        + file.getAbsolutePath());
                return;
            }
        }
        
        if (!dir.delete()) {
            Logger.userError(ErrorLevel.MEDIUM, "Unable to remove directory: "
                    + dir.getAbsolutePath());
            return;
        }
        
        groups.remove(group);
    }
    
    /**
     * Renames the specified group.
     *
     * @param oldName The old name of the group
     * @param newName The new name of the group
     */
    public static void renameGroup(final String oldName, final String newName) {
        Logger.doAssertion(oldName != null && !oldName.isEmpty()
                && newName != null && !newName.isEmpty() && groups.containsKey(oldName)
                &&!groups.containsKey(newName) && !newName.equals(oldName));
        
        makeGroup(newName);
        
        for (Action action : groups.get(oldName)) {
            action.setGroup(newName);
            groups.get(newName).add(action);
        }
        
        groups.get(oldName).clear();
        
        removeGroup(oldName);
    }
    
    /**
     * Returns the action comparison specified by the given string, or null if it
     * doesn't match a valid registered action comparison.
     *
     * @param type The name of the action comparison to try and find
     * @return The actioncomparison with the specified name, or null on failure
     */
    public static ActionType getActionType(final String type) {
        Logger.doAssertion(type != null && !type.isEmpty());
        
        for (ActionType target : actionTypes) {
            if (((Enum) target).name().equals(type)) {
                return target;
            }
        }
        
        return null;
    }
    
    /**
     * Returns a list of action types that are compatible with the one
     * specified.
     *
     * @param type The type to be checked against
     * @return A list of compatible action types
     */
    public static List<ActionType> getCompatibleTypes(final ActionType type) {
        Logger.doAssertion(type != null);
        
        final List<ActionType> res = new ArrayList<ActionType>();
        for (ActionType target : actionTypes) {
            if (!target.equals(type) && target.getType().equals(type.getType())) {
                res.add(target);
            }
        }
        
        return res;
    }
    
    /**
     * Returns a list of action components that are compatible with the
     * specified class.
     *
     * @param target The class to be tested
     * @return A list of compatible action components
     */
    public static List<ActionComponent> getCompatibleComponents(final Class target) {
        Logger.doAssertion(target != null);
        
        final List<ActionComponent> res = new ArrayList<ActionComponent>();
        for (ActionComponent subject : actionComponents) {
            if (subject.appliesTo().equals(target)) {
                res.add(subject);
            }
        }
        
        return res;
    }
    
    /**
     * Returns a list of action comparisons that are compatible with the
     * specified class.
     *
     * @param target The class to be tested
     * @return A list of compatible action comparisons
     */
    public static List<ActionComparison> getCompatibleComparisons(final Class target) {
        Logger.doAssertion(target != null);
        
        final List<ActionComparison> res = new ArrayList<ActionComparison>();
        for (ActionComparison subject : actionComparisons) {
            if (subject.appliesTo().equals(target)) {
                res.add(subject);
            }
        }
        
        return res;
    }
    
    /**
     * Returns a list of all the action types registered by this manager.
     *
     * @return A list of registered action types
     */
    public static List<ActionType> getTypes() {
        return actionTypes;
    }
    
    /**
     * Returns a list of all the action types registered by this manager.
     *
     * @return A list of registered action comparisons
     */
    public static List<ActionComparison> getComparisons() {
        return actionComparisons;
    }
    
    /**
     * Returns the action component specified by the given string, or null if it
     * doesn't match a valid registered action component.
     *
     * @param type The name of the action component to try and find
     * @return The actioncomponent with the specified name, or null on failure
     */
    public static ActionComponent getActionComponent(final String type) {
        Logger.doAssertion(type != null && !type.isEmpty());
        
        for (ActionComponent target : actionComponents) {
            if (((Enum) target).name().equals(type)) {
                return target;
            }
        }
        
        return null;
    }
    
    /**
     * Returns the action type specified by the given string, or null if it
     * doesn't match a valid registered action type.
     *
     * @param type The name of the action type to try and find
     * @return The actiontype with the specified name, or null on failure
     */
    public static ActionComparison getActionComparison(final String type) {
        Logger.doAssertion(type != null && !type.isEmpty());
        
        for (ActionComparison target : actionComparisons) {
            if (((Enum) target).name().equals(type)) {
                return target;
            }
        }
        
        return null;
    }
}
