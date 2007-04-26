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

import java.util.ArrayList;
import java.util.List;

import uk.org.ownage.dmdirc.logger.ErrorLevel;
import uk.org.ownage.dmdirc.logger.Logger;
import uk.org.ownage.dmdirc.plugins.PluginManager;

/**
 * Manages all actions for the client.
 * @author chris
 */
public class ActionManager {
    
    private static List<ActionType> actionTypes;
    
    /** Creates a new instance of ActionManager. */
    private ActionManager() {
        
    }
    
    /** Initialises the action manager. */
    private static void init() {
         actionTypes = new ArrayList<ActionType>();
         
         registerActionTypes(CoreActionType.values());
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
     * Processes an event of the specified type.
     * @param type The type of the event to process
     * @param arguments The arguments for the event
     */
    public static void processEvent(final ActionType type, final Object ... arguments) {
        if (actionTypes == null) {
            init();
        }
        
        if (type.getType().getArity() == arguments.length) {
            PluginManager.getPluginManager().processEvent(type, arguments);
        } else {
            Logger.error(ErrorLevel.ERROR, "Invalid number of arguments for action " + type);
        }
    }
    
}
