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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.dmdirc.Channel;
import com.dmdirc.Config;
import com.dmdirc.Server;
import com.dmdirc.logger.ErrorLevel;
import com.dmdirc.logger.Logger;
import com.dmdirc.parser.ChannelClientInfo;
import com.dmdirc.plugins.PluginManager;

/**
 * Manages all actions for the client.
 * @author chris
 */
public class ActionManager {
    
    /** A list of registered action types. */
    private static List<ActionType> actionTypes;
    
    /** A list of registered action components. */
    private static List<ActionComponent> actionComponents;
    
    /** A list of registered action comparisons. */
    private static List<ActionComparison> actionComparisons;
    
    /** A map linking types and a list of actions that're registered for them. */
    private static HashMap<ActionType, List<Action>> actions;
    
    /** A map linking groups and a list of actions that're in them. */
    private static HashMap<String, List<Action>> groups;
    
    /** Creates a new instance of ActionManager. */
    private ActionManager() {
        
    }
    
    /** Initialises the action manager. */
    public static void init() {
        actionTypes = new ArrayList<ActionType>();
        actionComparisons = new ArrayList<ActionComparison>();
        actionComponents = new ArrayList<ActionComponent>();
        
        registerActionTypes(CoreActionType.values());
        registerActionComparisons(CoreActionComparison.values());
        registerActionComponents(CoreActionComponent.values());
    }
    
    /**
     * Registers a set of actiontypes with the manager.
     * @param types An array of ActionTypes to be registered
     */
    public static void registerActionTypes(final ActionType[] types) {
        for (ActionType type : types) {
            actionTypes.add(type);
        }
    }
    
    /**
     * Registers a set of action components with the manager.
     * @param comps An array of ActionComponents to be registered
     */
    public static void registerActionComponents(final ActionComponent[] comps) {
        for (ActionComponent comp : comps) {
            actionComponents.add(comp);
        }
    }
    
    /**
     * Registers a set of action comparisons with the manager.
     * @param comps An array of ActionComparisons to be registered
     */
    public static void registerActionComparisons(final ActionComparison[] comps) {
        for (ActionComparison comp : comps) {
            actionComparisons.add(comp);
        }
    }
    
    /**
     * Returns a map of groups to action lists.
     * @return a map of groups to action lists
     */
    public static Map<String, List<Action>> getGroups() {
        return groups;
    }
    
    /**
     * Loads actions from the user's directory.
     */
    public static void loadActions() {
        actions = new HashMap<ActionType, List<Action>>();
        groups = new HashMap<String, List<Action>>();
        
        final File dir = new File(getDirectory());
        
        if (!dir.exists()) {
            try {
                dir.mkdirs();
                dir.createNewFile();
            } catch (IOException ex) {
                Logger.error(ErrorLevel.ERROR, "Unable to create actions dir", ex);
            }
        }
        
        if (dir == null || dir.listFiles() == null) {
            Logger.error(ErrorLevel.WARNING, "Unable to load user action files");
        } else {
            for (File file : dir.listFiles()) {
                if (file.isDirectory()) {
                    groups.put(file.getName(), new ArrayList<Action>());
                    loadActions(file);
                }
            }
        }
    }
    
    /**
     * Loads action files from a specified group directory.
     * @param dir The directory to scan.
     */
    private static void loadActions(final File dir) {
        for (File file : dir.listFiles()) {
            new Action(dir.getName(), file.getName());
        }
    }
    
    /**
     * Registers an action with the manager.
     * @param action The action to be registered
     */
    public static void registerAction(final Action action) {
        if (actions == null) {
            init();
        }
        
        for (ActionType trigger : action.getTriggers()) {
            if (!actions.containsKey(trigger)) {
                actions.put(trigger, new ArrayList<Action>());
            }
            
            actions.get(trigger).add(action);
        }
        
        if (!groups.containsKey(action.getGroup())) {
            groups.put(action.getGroup(), new ArrayList<Action>());
        }
        
        groups.get(action.getGroup()).add(action);
    }
    
    /**
     * Unregisters an action with the manager.
     * @param action The action to be unregistered
     */
    public static void unregisterAction(final Action action) {
        if (actions == null) {
            return;
        }
        
        for (Map.Entry<ActionType,List<Action>> map : actions.entrySet()) {
            if (map.getValue().contains(action)) {
                map.getValue().remove(action);
            }
        }
        
        for (Map.Entry<String,List<Action>> map : groups.entrySet()) {
            if (map.getValue().contains(action)) {
                map.getValue().remove(action);
            }
        }        
    }
    
    /**
     * Deletes the specified action.
     * @param action The action to be deleted
     */
    public static void deleteAction(final Action action) {
        if (actions == null) {
            init();
        }
        
        unregisterAction(action);
        
        action.delete();
    }
    
    /**
     * Processes an event of the specified type.
     * @param type The type of the event to process
     * @param format The format of the message that's going to be displayed for
     * the event. Actions may change this format.
     * @param arguments The arguments for the event
     */
    public static void processEvent(final ActionType type,
            final StringBuffer format, final Object ... arguments) {
        if (actionTypes == null) {
            init();
        }
        
        if (type.getType().getArity() == arguments.length) {
            PluginManager.getPluginManager().processEvent(type, format, arguments);
            triggerActions(type, format, arguments);
        } else {
            Logger.error(ErrorLevel.ERROR, "Invalid number of arguments for action " + type);
        }
    }
    
    /**
     * Triggers actions that respond to the specified type.
     * @param type The type of the event to process
     * @param format The format of the message that's going to be displayed for
     * the event. Actions may change this format.*
     * @param arguments The arguments for the event
     */
    private static void triggerActions(final ActionType type,
            final StringBuffer format, final Object ... arguments) {
        if (actions.containsKey(type)) {
            for (Action action : actions.get(type)) {
                action.trigger(format, arguments);
            }
        }
    }
    
    /**
     * Returns the directory that should be used to store actions.
     * @return The directory that should be used to store actions
     */
    public static String getDirectory() {
        final String fs = System.getProperty("file.separator");
        return Config.getConfigDir() + "actions" + fs;
    }
    
    /**
     * Creates a new group with the specified name
     * @param group The group to be created
     */
    public static void makeGroup(final String group) {
        if (!groups.containsKey(group) && new File(getDirectory() + group).mkdir()) {
            groups.put(group, new ArrayList<Action>());
        }
    }
    
    /**
     * Removes the group with the specified name.
     * @param group The group to be removed
     */
    public static void removeGroup(final String group) {
        if (groups.containsKey(group)) {
            final File dir = new File(getDirectory() + group);
            for (File file : dir.listFiles()) {
                if (!file.delete()) {
                    Logger.error(ErrorLevel.ERROR, "Unable to remove file: " + file.getAbsoluteFile());
                    return;
                }
            }
            
            if (!dir.delete()) {
                Logger.error(ErrorLevel.ERROR, "Unable to remove dir: " + dir.getAbsoluteFile());
                return;
            }
            
            groups.remove(group);
        }
    }
    
    /**
     * Renames the specified group.
     * @param oldName The old name of the group
     * @param newName The new name of the group
     */
    public static void renameGroup(final String oldName, final String newName) {
        if (groups.containsKey(oldName)) {
            makeGroup(newName);
            
            for (Action action : groups.get(oldName)) {
                action.setGroup(newName);
                groups.get(newName).add(action);
            }
            
            groups.get(oldName).clear();
            
            removeGroup(oldName);
        }
    }
    
    /**
     * Returns the action comparison specified by the given string, or null if it
     * doesn't match a valid registered action comparison.
     * @param type The name of the action comparison to try and find
     * @return The actioncomparison with the specified name, or null on failure
     */
    public static ActionType getActionType(final String type) {
        if (actionTypes == null) {
            init();
        }
        
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
     * @param type The type to be checked against
     * @return A list of compatible action types
     */
    public static List<ActionType> getCompatibleTypes(final ActionType type) {
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
     * specified class
     * @param target The class to be tested
     * @return A list of compatible action components
     */
    public static List<ActionComponent> getCompatibleComponents(final Class target) {
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
     * specified class
     * @param target The class to be tested
     * @return A list of compatible action comparisons
     */
    public static List<ActionComparison> getCompatibleComparisons(final Class target) {
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
     */
    public static List<ActionType> getTypes() {
        return actionTypes;
    }
    
    /**
     * Returns a list of all the action types registered by this manager.
     */
    public static List<ActionComparison> getComparisons() {
        return actionComparisons;
    }
    
    /**
     * Returns the action component specified by the given string, or null if it
     * doesn't match a valid registered action component.
     * @param type The name of the action component to try and find
     * @return The actioncomponent with the specified name, or null on failure
     */
    public static ActionComponent getActionComponent(final String type) {
        if (actionComponents == null) {
            init();
        }
        
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
     * @param type The name of the action type to try and find
     * @return The actiontype with the specified name, or null on failure
     */
    public static ActionComparison getActionComparison(final String type) {
        if (actionComparisons == null) {
            init();
        }
        
        for (ActionComparison target : actionComparisons) {
            if (((Enum) target).name().equals(type)) {
                return target;
            }
        }
        
        return null;
    }
    
    /**
     * Substitutes variables into the string based on the arguments.
     * @param target The string to be altered
     * @param arguments The arguments for the action
     */
    public static String substituteVars(final String target, final Object ... arguments) {
        String res = target;
        Server server = null;
        Channel channel = null;
        ChannelClientInfo source = null;
        
        if (arguments.length > 0 && arguments[0] instanceof Server) {
            server = (Server) arguments[0];
        } else if (arguments.length > 0 && arguments[0] instanceof Channel) {
            channel = (Channel) arguments[0];
            server = channel.getServer();
        }
        
        if (arguments.length > 1 && arguments[1] instanceof ChannelClientInfo) {
            source = (ChannelClientInfo) arguments[1];
        }
        
        if (server != null) {
            res = res.replaceAll("\\$nick", server.getParser().getMyself().getNickname());
            if (server.isAway()) {
                res = res.replaceAll("\\$awaymsg", server.getAwayMessage());
            }
        }
        
        if (channel != null) {
            res = res.replaceAll("\\$chan", channel.getChannelInfo().getName());
        }
        
        if (source != null) {
            res = res.replaceAll("\\$source", source.getNickname());
        }
        
        if (arguments.length > 2 && arguments[2] instanceof String) {
            res = res.replaceAll("\\$message", (String) arguments[2]);
        }
        
        for (String key : Config.getOptions("actions")) {
            res = res.replaceAll("\\$" + key, Config.getOption("actions", key));
        }
        
        if (arguments.length > 2 && arguments[2] instanceof String[]) {
            int i = 1;
            for (String arg : (String[]) arguments[2]) {
                res = res.replaceAll("\\$" + i, arg);
                i++;
            }
        }
        
        return res;
    }
}
