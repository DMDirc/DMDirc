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

import com.dmdirc.actions.Action;

import java.util.ArrayList;
import java.util.List;

/**
 * An action wrapper deals with a specific group of actions in order to provide
 * an even more user-friendly experience.
 * 
 * @author chris
 */
public abstract class ActionWrapper {
    
    /** A list of registered actions. */
    protected final List<Action> actions = new ArrayList<Action>();

    /**
     * Registers the specified action with this manager.
     * 
     * @param action The action to be registered
     */
    public void registerAction(final Action action) {
        actions.add(action);
    }
    
    /**
     * Unregisters the specified action with this manager.
     * 
     * @param action The action to be unregistered
     */
    public void unregisterAction(final Action action) {
        actions.remove(action);
    }
    
    /**
     * Removes all actions from this manager.
     */
    public void clearActions() {
        actions.clear();
    }
    
    /**
     * Retrieves a list of actions registered with this wrapper.
     * 
     * @return A list of registered actions
     */
    public List<Action> getActions() {
        return actions;
    }
    
    /**
     * Retrieve the group name that this wrapper is using.
     * 
     * @return This wrapper's group name
     */
    public abstract String getGroupName();
    
}
