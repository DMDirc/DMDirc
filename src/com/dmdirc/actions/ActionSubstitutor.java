/*
 * Copyright (c) 2006-2009 Chris Smith, Shane Mc Cormack, Gregory Holmes
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

import com.dmdirc.actions.interfaces.ActionType;
import com.dmdirc.actions.interfaces.ActionComponent;
import com.dmdirc.FrameContainer;
import com.dmdirc.Precondition;
import com.dmdirc.Server;
import com.dmdirc.ServerState;
import com.dmdirc.config.ConfigManager;
import com.dmdirc.config.IdentityManager;

import com.dmdirc.ui.interfaces.Window;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

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
    public Set<String> getConfigSubstitutions() {
        return IdentityManager.getGlobalConfig().getOptions("actions").keySet();
    }
    
    /**
     * Substitutes in config variables into the specified target.
     *
     * @param config The configuration manager to use for options
     * @param target The StringBuilder to modify
     * @since 0.6.3m2
     */
    private void doConfigSubstitutions(final ConfigManager config, final StringBuilder target) {
        for (Map.Entry<String, String> option : config.getOptions("actions").entrySet()) {
            doReplacement(target, "$" + option.getKey(), option.getValue());
        }
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
     * Substitutes in component-style substitutions.
     *
     * @param target The stringbuilder to be changed
     * @param args The arguments passed for this action type
     */
    private void doComponentSubstitutions(final StringBuilder target, final Object ... args) {
        int i = 0;
        for (Class myClass : type.getType().getArgTypes()) {
            if (args[i] != null) {
                for (ActionComponent comp : ActionManager.getCompatibleComponents(myClass)) {
                    final String needle = "${" + i + "." + comp.toString() + "}";

                    if (target.indexOf(needle) > -1) {
                        final Object replacement = comp.get(args[i]);

                        if (replacement != null) {
                            doReplacement(target, needle, replacement.toString());
                        }
                    }
                }
            }
            
            i++;
        }
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
        
        if (hasFrameContainer()) {
            for (ActionComponent comp : ActionManager.getCompatibleComponents(Server.class)) {
                final String key = "{" + comp.toString() + "}";
                final String desc = "The connection's " + comp.getName();
                
                res.put(key, desc);
            }
        }
        
        return res;
    }
    
    /**
     * Substitutes in server substitutions.
     *
     * @param target The stringbuilder to be changed
     * @param args The arguments passed for this action type
     */
    private void doServerSubstitutions(final StringBuilder target, final Object ... args) {
        if (args.length > 0 && args[0] instanceof FrameContainer) {
            final Server server = ((FrameContainer) args[0]).getServer();
        
            if (server != null) {
                synchronized (server.getState()) {
                    if (!server.getState().equals(ServerState.CONNECTED)) {
                        return;
                    }
                    
                    for (ActionComponent comp : ActionManager.getCompatibleComponents(Server.class)) {
                        final String key = "${" + comp.toString() + "}";

                        if (target.indexOf(key) > -1) {
                            final Object res = comp.get(((FrameContainer) args[0]).getServer());

                            if (res != null) {
                                doReplacement(target, key, res.toString());
                            }
                        }
                    }
                }
            }
        }
    }
    
    /**
     * Returns true if this action type's first argument is a frame container,
     * or descendent of one.
     *
     * @return True if this action type's first arg extends or is a FrameContainer
     */
    private boolean hasFrameContainer() {
        Class target = null;
        
        if (type.getType().getArgTypes().length > 0) {
            target = type.getType().getArgTypes()[0];
            
            while (target != null && target != FrameContainer.class) {
                target = target.getSuperclass();
            }
        }
        
        return target == FrameContainer.class;
    }
    
    /**
     * Determines whether or not word substitutions will work for this action
     * type. Word substitutions take the form $1, $1-5, $6-, etc.
     *
     * @return True if word substitutions are supported, false otherwise.
     */
    public boolean usesWordSubstitutions() {
        return type.getType().getArgTypes().length > 2
                && type.getType().getArgTypes()[2] == String[].class;
    }
    
    /**
     * Substitutes in word substitutions.
     *
     * @param target The stringbuilder to be changed
     * @param args The arguments passed for this action type
     */
    private void doWordSubstitutions(final StringBuilder target, final Object ... args) {
        if (args.length > 1) {
            String[] words = null;
            
            if (args.length > 2 && args[2] instanceof String[]) {
                words = (String[]) args[2];
            } else if (args.length > 2 && args[2] instanceof String) {
                words = ((String) args[2]).split(" ");
            } else if (args[1] instanceof String[]) {
                words = (String[]) args[1];
            } else if (args[1] instanceof String) {
                words = ((String) args[1]).split(" ");
            } else {
                return;
            }
            
            final StringBuffer compound = new StringBuffer();
            for (int i = words.length - 1; i >= 0; i--) {
                if (compound.length() > 0) {
                    compound.insert(0, ' ');
                }
                compound.insert(0, words[i]);
                
                doReplacement(target, "$" + (i + 1) + "-", compound.toString());
                doReplacement(target, "$" + (i + 1), words[i]);
            }
        }
    }
    
    /**
     * Performs all applicable substitutions on the specified string, with the
     * specified arguments.
     *
     * @param target The string to be altered
     * @param args The arguments for the action type
     * @return The substituted string
     */
    @Precondition("Number of arguments given equals the number of arguments " +
    "required by this substitutor's type")
    public String doSubstitution(final String target, final Object ... args) {
        if (type.getType().getArity() != args.length) {
            throw new IllegalArgumentException("Invalid number of arguments "
                    + "for doSubstitution: expected " + type.getType().getArity() + ", got "
                    + args.length + ". Type: " + type.getName());
        }

        final StringBuilder res = new StringBuilder(target);
        
        doConfigSubstitutions(getConfigManager(args), res);
        doServerSubstitutions(res, args);
        doComponentSubstitutions(res, args);
        doWordSubstitutions(res, args);
        
        return res.toString();
    }

    /**
     * Tries to retrieve an appropriate configuration manager from the
     * specified set of arguments. If any of the arguments is an instance of
     * {@link FrameContainer} or {@link Window}, the config manager is
     * requested from them. Otherwise, the global config is returned.
     * 
     * @param args The arguments to be tested
     * @return The best config manager to use for those arguments
     * @since 0.6.3m2
     */
    protected ConfigManager getConfigManager(final Object ... args) {
        for (Object arg : args) {
            if (arg instanceof FrameContainer) {
                return ((FrameContainer) arg).getConfigManager();
            } else if (arg instanceof Window) {
                return ((Window) arg).getConfigManager();
            }
        }

        return IdentityManager.getGlobalConfig();
    }
    
    /**
     * Replaces all occurances of needle in haystack with replacement.
     *
     * @param haystack The stringbuilder that is to be modified
     * @param needle The search string
     * @param replacement The string to be substituted in
     */
    private void doReplacement(final StringBuilder haystack, final String needle,
            final String replacement) {
        int i = -1;
        
        do {
            i = haystack.indexOf(needle);
            
            if (i != -1) {
                haystack.replace(i, i + needle.length(), replacement);
            }
        } while (i != -1);
    }
    
}
