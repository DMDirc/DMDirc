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

package com.dmdirc.actions;

import com.dmdirc.actions.interfaces.ActionType;
import com.dmdirc.actions.interfaces.ActionComponent;
import com.dmdirc.FrameContainer;
import com.dmdirc.Precondition;
import com.dmdirc.Server;
import com.dmdirc.ServerState;
import com.dmdirc.commandparser.CommandArguments;
import com.dmdirc.config.ConfigManager;
import com.dmdirc.config.IdentityManager;
import com.dmdirc.ui.interfaces.Window;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Handles the substitution of variables into action targets and responses.
 *
 * @author Chris
 */
public class ActionSubstitutor {

    /** Substitution to use when a component requires a connected server. */
    private static final String ERR_NOT_CONNECTED = "not_connected";
    /** Substitution to use to replace an unknown substitution. */
    private static final String ERR_NOT_DEFINED = "not_defined";
    /** Substitution to use to replace subs with illegal components. */
    private static final String ERR_ILLEGAL_COMPONENT = "illegal_component";
    
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
                && (type.getType().getArgTypes()[2] == String[].class
                || type.getType().getArgTypes()[2] == String.class);
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

        final Pattern bracesPattern = Pattern.compile("(?<!\\\\)((?:\\\\\\\\)*)"
                + "(\\$\\{([^{}]*?)\\})");
        final Pattern otherPattern = Pattern.compile("(?<!\\\\)((?:\\\\\\\\)*)(\\$("
                + "[0-9]+(-([0-9]+)?)?|" // Word subs - $1, $1-, $1-2
                + "[0-9]+(\\.([A-Z_]+))+|" // Component subs - 2.FOO_BAR
                + "[a-z0-9A-Z_\\.]+" // Config/server subs
                + "))");

        Matcher bracesMatcher = bracesPattern.matcher(res);
        Matcher otherMatcher = otherPattern.matcher(res);

        boolean first;

        while ((first = bracesMatcher.find()) || otherMatcher.find()) {
            final Matcher matcher = first ? bracesMatcher : otherMatcher;

            final String group = matcher.group(3);
            final int start = matcher.start() + matcher.group(1).length(), end = matcher.end();

            res.delete(start, end);
            res.insert(start, getSubstitution(doSubstitution(group, args), args));
            
            bracesMatcher = bracesPattern.matcher(res);
            otherMatcher = otherPattern.matcher(res);
        }

        return res.toString().replaceAll("\\\\(.)", "$1");
    }

    /**
     * Retrieves the value which should be used for the specified substitution.
     *
     * @param substitution The substitution, without leading $
     * @param args The arguments for the action
     * @return The substitution to be used
     */
    private String getSubstitution(final String substitution, final Object ... args) {
        final Pattern numberPattern = Pattern.compile("([0-9]+)(-([0-9]+)?)?");
        final Matcher numberMatcher = numberPattern.matcher(substitution);

        final Pattern compPattern = Pattern.compile("([0-9]+)\\.([A-Z_]+(\\.[A-Z_]+)*)");
        final Matcher compMatcher = compPattern.matcher(substitution);

        if (usesWordSubstitutions() && numberMatcher.matches()) {
            final CommandArguments words = args[2] instanceof String ?
                new CommandArguments((String) args[2]) :
                new CommandArguments(Arrays.asList((String[]) args[2]));

            int start, end;

            start = end = Integer.parseInt(numberMatcher.group(1)) - 1;

            if (numberMatcher.group(3) != null) {
                end = Integer.parseInt(numberMatcher.group(3)) - 1;
            } else if (numberMatcher.group(2) != null) {
                end = words.getWords().length - 1;
            }

            return words.getWordsAsString(start, end);
        }

        if (compMatcher.matches()) {
            final int argument = Integer.parseInt(compMatcher.group(1));

            try {
                final ActionComponentChain chain = new ActionComponentChain(
                        type.getType().getArgTypes()[argument], compMatcher.group(2));
                return checkConnection(chain, args, args[argument]);
            } catch (IllegalArgumentException ex) {
                return ERR_ILLEGAL_COMPONENT;
            }
        }

        final ConfigManager manager = getConfigManager(args);

        if (manager.hasOptionString("actions", substitution)) {
            return manager.getOption("actions", substitution);
        }

        if (hasFrameContainer()) {
            final Server server = ((FrameContainer) args[0]).getServer();

            if (server != null) {
                try {
                    final ActionComponentChain chain = new ActionComponentChain(
                        Server.class, substitution);
                    return checkConnection(chain, args, server);
                } catch (IllegalArgumentException ex) {
                    return ERR_ILLEGAL_COMPONENT;
                }
            }
        }

        return ERR_NOT_DEFINED;
    }

    /**
     * Checks the connection status of any server associated with the specified
     * arguments. If the specified component chain requires a server with an
     * established connection and no such server is present, this method
     * returns the string <code>not_connected</code> without attempting to
     * evaluate any components in the chain.
     *
     * @since 0.6.4
     * @param chain The chain to be checked
     * @param args The arguments for this invocation
     * @param argument The argument used as a base for the chain
     * @return The value of the evaluated chain, or <code>not_connected</code>
     */
    protected String checkConnection(final ActionComponentChain chain,
            final Object[] args, final Object argument) {
        if ((chain.requiresConnection() && args[0] instanceof FrameContainer
                    && ((FrameContainer) args[0]).getServer().getState()
                    == ServerState.CONNECTED) || !chain.requiresConnection()) {
            return chain.get(argument).toString();
        }

        return ERR_NOT_CONNECTED;
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
    
}
