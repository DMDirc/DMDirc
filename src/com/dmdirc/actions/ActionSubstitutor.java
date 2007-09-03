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

import com.dmdirc.Config;
import com.dmdirc.FrameContainer;
import com.dmdirc.Server;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Handles the substitution of variables into action targets and responses.
 *
 * @author Chris
 */
public class ActionSubstitutor {
    
    /** The action type this substitutor is for. */
    private final ActionType type;
    
    /**
     * Creates a new substitutor for the specified action type.
     *
     * @param type The action type this substitutor is for
     */
    public ActionSubstitutor(final ActionType type) {
        this.type = type;
    }
    
    /**
     * Retrieves a list of global config variables that will be substituted.
     * Note: does not include initial $.
     *
     * @return A list of global variable names that will be substituted
     */
    public List<String> getConfigSubstitutions() {
        return Config.getOptions("actions");
    }
    
    /**
     * Retrieves a list of substitutions derived from argument and component
     * combinations, along with a corresponding friendly name for them.
     * Note: does not include initial $.
     *
     * @return A map of component substitution names and their descriptions
     */
    public Map<String, String> getComponentSubstitutions() {
        final Map<String, String> res = new HashMap<String, String>();
        
        int i = 0;
        for (Class myClass : type.getType().getArgTypes()) {
            for (ActionComponent comp : ActionManager.getCompatibleComponents(myClass)) {
                final String key = "{" + i + "." + comp.toString() + "}";
                final String desc = type.getType().getArgNames()[i] + "'s " + comp.getName();
                
                res.put(key, desc);
            }
            
            i++;
        }
        
        return res;
    }
    
    /**
     * Retrieves a list of server substitutions, if this action type supports
     * them.
     * Note: does not include initial $.
     *
     * @return A map of server substitution names and their descriptions.
     */
    public Map<String, String> getServerSubstitutions() {
        final Map<String, String> res = new HashMap<String, String>();
        
        if (type.getType().getArgTypes().length > 0) {
            Class target = type.getType().getArgTypes()[0];
            
            while (target != null && target != FrameContainer.class) {
                target = target.getSuperclass();
            }
            
            if (target == FrameContainer.class) {
                for (ActionComponent comp : ActionManager.getCompatibleComponents(Server.class)) {
                    final String key = "{" + comp.toString() + "}";
                    final String desc = "The connection's " + comp.getName();
                    
                    res.put(key, desc);
                }
            }
        }
        
        return res;
    }
    
    /**
     * Determines whether or not word substitutions will work for this action
     * type. Word substitutions take the form $1, $1-5, $6-, etc.
     *
     * @return True if word substitutions are supported, false otherwise.
     */
    public boolean usesWordSubstitutions() {
        return type.getType().getArgTypes().length > 2 && type.getType().getArgTypes()[2] == String[].class;
    }
    
}
