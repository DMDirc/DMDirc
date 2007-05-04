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

package uk.org.ownage.dmdirc.actions;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import uk.org.ownage.dmdirc.Channel;
import uk.org.ownage.dmdirc.Config;
import uk.org.ownage.dmdirc.Server;
import uk.org.ownage.dmdirc.logger.ErrorLevel;
import uk.org.ownage.dmdirc.logger.Logger;
import uk.org.ownage.dmdirc.parser.ChannelClientInfo;
import uk.org.ownage.dmdirc.plugins.PluginManager;

/**
 * Manages all actions for the client.
 * @author chris
 */
public class ActionManager {
    
    /** A list of registered action types. */
    private static List<ActionType> actionTypes;
    
    /** A map linking types and a list of actions that're registered for them. */
    private static HashMap<ActionType, List<Action>> actions;
    
    /** Creates a new instance of ActionManager. */
    private ActionManager() {
        
    }
    
    /** Initialises the action manager. */
    public static void init() {
        actionTypes = new ArrayList<ActionType>();
        actions = new HashMap<ActionType, List<Action>>();
        
        registerActionTypes(CoreActionType.values());
        
        loadActions();
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
     * Loads actions from the user's directory.
     */
    private static void loadActions() {
        final String fs = System.getProperty("file.separator");
        final String location = Config.getConfigDir() + "actions" + fs;
        final File dir = new File(location);
        
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
        
        if (!actions.containsKey(action.getTrigger())) {
            actions.put(action.getTrigger(), new ArrayList<Action>());
        }
        
        actions.get(action.getTrigger()).add(action);
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
            // TODO: Work with shane to pass format buffer
            PluginManager.getPluginManager().processEvent(type, arguments);
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
     * Returns the action type specified by the given string, or null if it
     * doesn't match a valid registered action type.
     * @param type The name of the action type to try and find
     * @return The actiontype with the specified name, or null on failure
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
        } else if (arguments[0] instanceof Channel) {
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
