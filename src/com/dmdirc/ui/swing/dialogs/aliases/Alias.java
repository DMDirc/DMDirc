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

package com.dmdirc.ui.swing.dialogs.aliases;

import com.dmdirc.actions.ActionCondition;
import com.dmdirc.actions.CoreActionComparison;
import com.dmdirc.actions.CoreActionComponent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Actions alias wrapper.
 */
public final class Alias {
    
    /** Alias name. */
    private String name;
    
    /** Alias arguments. */
    private List<ActionCondition> arguments;
    
    /** Alias response. */
    private String[] response;
    
    /**
     * Creates a new Alias wrapper.
     *
     * @param name Alias name
     */
    public Alias(final String name) {
        this.name = name;
        this.arguments = new ArrayList<ActionCondition>();
        this.arguments.add(new ActionCondition(1, CoreActionComponent.STRING_STRING,
                CoreActionComparison.STRING_EQUALS, name));
        this.response = new String[]{"", };
    }
    
    /**
     * Wraps an existing Action in an Alias.
     *
     * @param name Alias name
     * @param arguments List of arguments for the alias
     * @param response Response for the alias
     */
    public Alias(final String name, final List<ActionCondition> arguments,
            final String[] response) {
        this.name = name;
        this.arguments = new ArrayList<ActionCondition>(arguments);
        this.response = response.clone();
    }
    
    /**
     * Returns the aliases name.
     *
     * @return Aliases name
     */
    public String getName() {
        return name;
    }
    
    /**
     * Sets the aliases name.
     *
     * @param name Name to give the alias
     */
    public void setName(final String name) {
        if (!this.name.equals(name)) {
            this.name = name;
            
            ActionCondition argument;
            
            argument = arguments.get(0);
            
            if (argument.getComparison() != CoreActionComparison.STRING_EQUALS) {
                argument = arguments.get(1);
            }
            
            argument.setTarget(name);
        }
    }
    
    /**
     * Gets the aliases arguments.
     *
     * @return Argument list
     */
    public List<ActionCondition> getArguments() {
        return new ArrayList<ActionCondition>(arguments);
    }
    
    /**
     * Gets the aliases number of arguments argument
     *
     * @return Number of arguments ActionCondition or null
     */
    public ActionCondition getArgsArgument() {
        ActionCondition argument;
        
        argument = arguments.get(0);
        
        if (argument.getComparison() == CoreActionComparison.STRING_EQUALS) {
            if (arguments.size() > 1) {
                argument = arguments.get(1);
            } else {
                argument = null;
            }
        }
        
        return argument;
    }
    
    /**
     * Sets the aliases arguments.
     *
     * @param arguments A new list of arguments to set
     */
    public void setArguments(final List<ActionCondition> arguments) {
        if (!this.arguments.equals(arguments)) {
            this.arguments = new ArrayList<ActionCondition>(arguments);
        }
    }
    
    /**
     * Gets the aliases response.
     *
     * @return Response
     */
    public String[] getResponse() {
        return response.clone();
    }
    
    /**
     * Sets the aliases response.
     *
     * @param response New Response
     */
    public void setResponse(final String[] response) {
        if (!Arrays.equals(this.response, response)) {
            this.response = response.clone();
        }
    }
}
