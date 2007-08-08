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

import com.dmdirc.actions.Action;
import com.dmdirc.actions.ActionCondition;
import com.dmdirc.actions.ActionType;
import com.dmdirc.actions.CoreActionComparison;
import com.dmdirc.actions.CoreActionComponent;
import com.dmdirc.actions.CoreActionType;
import com.dmdirc.actions.wrappers.AliasWrapper;

import java.util.ArrayList;
import java.util.List;

/**
 * Actions alias wrapper.
 */
public class Alias {
    
    private boolean isNewAlias;
    
    private boolean isDeletedAlias;
    
    private boolean isModifiedAlias;
    
    private String name;
    
    private List<ActionCondition> arguments;
    
    private String[] response;
    
    /**
     * Creates a new Alias wrapper.
     *
     * @param name Alias name
     */
    public Alias(final String name) {
        this.name = name;
        this.isNewAlias = true;
        this.arguments = new ArrayList<ActionCondition>();
        this.arguments.add(new ActionCondition(1, CoreActionComponent.STRING_STRING,
                CoreActionComparison.STRING_EQUALS, name));
        this.response = new String[]{"", };
    }
    
    /**
     * Wraps an existing Action in an Alias.
     *
     * @param name Alias name
     */
    public Alias(final String name, final List<ActionCondition> arguments,
            final String[] response) {
        this.name = name;
        this.isNewAlias = false;
        this.arguments = new ArrayList<ActionCondition>(arguments);
        this.response = response.clone();
    }
    
    /**
     * Checks whether this is a new alias
     *
     * @return true iff the alias is new
     */
    public boolean isNew() {
        return isNewAlias;
    }
    
    public boolean isDeleted() {
        return isDeletedAlias;
    }
    
    public void setDeleted(final boolean isDeleted) {
        this.isDeletedAlias = isDeleted;
    }
    
    public boolean isModified() {
        return isModifiedAlias;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(final String name) {
        this.name = name;
        isModifiedAlias = true;
    }
    
    public List<ActionCondition> getArguments() {
        return new ArrayList<ActionCondition>(arguments);
    }
    
    public void setArguments(final List<ActionCondition> arguments) {
        this.arguments = new ArrayList<ActionCondition>(arguments);
        isModifiedAlias = true;
    }
    
    public String[] getResponse() {
        return response.clone();
    }
    
    public void setResponse(final String[] response) {
        this.response = response.clone();
        isModifiedAlias = true;
    }
    
    public void save() {
        Action action = null;
        
        if (isDeletedAlias) {
            final List<Action> actions = AliasWrapper.getAliasWrapper().getActions();
            
            for(Action loopAction : actions) {
                if (name.equals(loopAction.getName())) {
                    action = loopAction;
                    break;
                }
            }
            if (action != null) {
                action.delete();
            }
        }
        
        if (isModifiedAlias) {
            //find alias
            final List<Action> actions = AliasWrapper.getAliasWrapper().getActions();
            
            for(Action loopAction : actions) {
                if (name.equals(loopAction.getName())) {
                    action = loopAction;
                    break;
                }
            }
        }
        
        if (action == null) {
            action = new Action(
                    AliasWrapper.getAliasWrapper().getGroupName(),
                    name,
                    new ActionType[] {CoreActionType.UNKNOWN_COMMAND, },
                    response,
                    arguments,
                    "");
        }
        
        action.save();
    }
    
}
