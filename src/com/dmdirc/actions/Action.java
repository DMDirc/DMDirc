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

import com.dmdirc.logger.ErrorLevel;
import com.dmdirc.logger.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

/**
 * Describes a single action.
 *
 * @author chris
 */
public class Action extends ActionModel implements Serializable {
    
    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 1;
       
    /** The file containing this action. */
    private File file;
    
    /** The properties read for this action. */
    private Properties properties;
        
    /**
     * Creates a new instance of Action. The group and name specified must
     * be the group and name of a valid action already saved to disk.
     *
     * @param group The group the action belongs to
     * @param name The name of the action
     */
    public Action(final String group, final String name) {
        super(group, name);
        
        final String fs = System.getProperty("file.separator");
        final String location = ActionManager.getDirectory() + group + fs + name;
        
        file = new File(location);
        
        try {
            final FileInputStream inputStream = new FileInputStream(file);
            
            properties = new Properties();
            properties.load(inputStream);
            loadAction();
            
            inputStream.close();
        } catch (IOException ex) {
            Logger.userError(ErrorLevel.HIGH, "I/O error when loading action: "
                    + group + "/" + name + ": " + ex.getMessage());
        }
    }
    
    /**
     * Creates a new instance of Action with the specified properties and saves
     * it to disk.
     *
     * @param group The group the action belongs to
     * @param name The name of the action
     * @param triggers The triggers to use
     * @param response The response to use
     * @param conditions The conditions to use
     * @param newFormat The new formatter to use
     */
    public Action(final String group, final String name,
            final ActionType[] triggers, final String[] response,
            final List<ActionCondition> conditions, final String newFormat) {
        super(group, name, triggers, response, conditions, newFormat);
        
        final String fs = System.getProperty("file.separator");
        final String dir = ActionManager.getDirectory() + group + fs;
        final String location = dir + name;
        
        new File(dir).mkdirs();
        
        file = new File(location);
        
        ActionManager.registerAction(this);
    }
    
    
    /**
     * Loads the various attributes of this action from the properties instance.
     */
    private void loadAction() {
        // Read the triggers
        if (properties.containsKey("trigger")) {
            final String[] triggerStrings = properties.getProperty("trigger").split("\\|");
            
            Class[] args = null;
            
            triggers = new ActionType[triggerStrings.length];
            
            for (int i = 0; i < triggerStrings.length; i++) {
                triggers[i] = ActionManager.getActionType(triggerStrings[i]);
                
                if (triggers[i] == null) {
                    error("Invalid trigger specified");
                    return;
                } else {
                    if (i == 0) {
                        args = triggers[i].getType().getArgTypes();
                    } else {
                        if (!triggers[i].getType().equals(triggers[0].getType())) {
                            error("Triggers are not compatible");
                            return;
                        }
                    }
                }
            }
            
        } else {
            error("No trigger specified");
            return;
        }
        
        // Read the response
        if (properties.containsKey("response")) {
            response = properties.getProperty("response").split("\n");
        } else {
            error("No response specified");
            return;
        }
        
        // Read the format change
        if (properties.containsKey("format")) {
            newFormat = properties.getProperty("format");
        }
        
        // Read the conditions
        int numConditions = 0;
        
        if (properties.containsKey("conditions")) {
            try {
                numConditions = Integer.parseInt(properties.getProperty("conditions"));
            } catch (NumberFormatException ex) {
                error("Invalid number of conditions specified");
                return;
            }
        }
        
        boolean valid = true;
        
        for (int i = 0; i < numConditions; i++) {
            valid = valid & readCondition(i);
        }
        
        if (valid) {
            ActionManager.registerAction(this);
        }
    }
    
    /**
     * Called to save the action.
     */
    public void save() {
        final Properties properties = new Properties();
        final StringBuffer triggerString = new StringBuffer();
        final StringBuffer responseString = new StringBuffer();
        
        for (ActionType trigger : triggers) {
            if (trigger == null) {
                Logger.appError(ErrorLevel.LOW, "ActionType was null", 
                        new IllegalArgumentException("Triggers: "
                        + Arrays.toString(triggers)));
                continue;
            }
            
            triggerString.append('|');
            triggerString.append(trigger.toString());
        }
        
        for (String line : response) {
            responseString.append('\n');
            responseString.append(line);
        }
        
        properties.setProperty("trigger", triggerString.substring(1));
        properties.setProperty("conditions", "" + conditions.size());
        properties.setProperty("response", responseString.substring(1));
        
        if (newFormat != null) {
            properties.setProperty("format", newFormat);
        }
        
        int i = 0;
        for (ActionCondition condition : conditions) {
            properties.setProperty("condition" + i + "-arg", "" + condition.getArg());
            properties.setProperty("condition" + i + "-component", condition.getComponent().toString());
            properties.setProperty("condition" + i + "-comparison", condition.getComparison().toString());
            properties.setProperty("condition" + i + "-target", condition.getTarget());
            i++;
        }
        
        try {
            final FileOutputStream outputStream = new FileOutputStream(file);
            properties.store(outputStream, "Created by GUI actions editor");
            outputStream.close();
        } catch (IOException ex) {
            Logger.userError(ErrorLevel.HIGH, "I/O error when saving action: "
                    + group + "/" + name + ": " + ex.getMessage());
        }
    }
    
    /**
     * Reads the specified condition.
     *
     * @param condition Condition number to read
     * @return True if the condition was read successfully.
     */
    private boolean readCondition(final int condition) {
        // It may help to close your eyes while reading this method.
        
        int arg = -1;
        ActionComponent component = null;
        ActionComparison comparison = null;
        String target = "";
        
        if (properties.containsKey("condition" + condition + "-arg")) {
            try {
                arg = Integer.parseInt(properties.getProperty("condition" + condition + "-arg"));
            } catch (NumberFormatException ex) {
                error("Invalid argument number for condition " + condition);
                return false;
            }
        }
        
        if (arg < 0 || arg >= triggers[0].getType().getArity()) {
            error("Invalid argument number for condition " + condition);
            return false;
        }
        
        if (properties.containsKey("condition" + condition + "-component")) {
            final String componentName = properties.getProperty("condition" + condition + "-component");
            
            if (componentName.indexOf('.') == -1) {
            component = ActionManager.getActionComponent(componentName);
            } else {
                try {
                    component = new ActionComponentChain(triggers[0].getType().getArgTypes()[arg],
                            componentName);
                } catch (IllegalArgumentException iae) {
                    error(iae.getMessage());
                    return false;
                }
            }
            
            if (component == null) {
                error("Invalid component for condition " + condition);
                return false;
            }
            
            if (!component.appliesTo().equals(triggers[0].getType().getArgTypes()[arg])) {
                error("Component cannot be applied to specified arg in condition " + condition);
                return false;
            }
        } else {
            error("No component specified for condition " + condition);
            return false;
        }
        
        if (properties.containsKey("condition" + condition + "-comparison")) {
            comparison = ActionManager.getActionComparison(properties.getProperty("condition" + condition + "-comparison"));
            if (comparison == null) {
                error("Invalid comparison for condition " + condition);
                return false;
            }
            
            if (!comparison.appliesTo().equals(component.getType())) {
                error("Comparison cannot be applied to specified component in condition " + condition);
                return false;
            }
        } else {
            error("No comparison specified for condition " + condition);
            return false;
        }
        
        if (properties.containsKey("condition" + condition + "-target")) {
            target = properties.getProperty("condition" + condition + "-target");
        } else {
            error("No target specified for condition " + condition);
            return false;
        }
        
        conditions.add(new ActionCondition(arg, component, comparison, target));
        return true;
    }
    
    /**
     * Raises a trivial error, informing the user of the problem.
     *
     * @param message The message to be raised
     */
    private void error(final String message) {
        Logger.userError(ErrorLevel.LOW, "Error when parsing action: "
                + group + "/" + name + ": " + message);
    }
    
    /**
     * Renames this action to the specified new name.
     *
     * @param newName The new name for this action
     * @deprecated Use setName instead
     */
    @Deprecated
    public void rename(final String newName) {
        setName(newName);
    }
    
    /** {@inheritDoc} */
    @Override
    public void setName(final String newName) {
        super.setName(newName);
        
        file.delete();
        
        file = new File(file.getParent() + System.getProperty("file.separator") + newName);
        
        save();
    }    
    
    /** {@inheritDoc} */
    @Override
    public void setGroup(final String newGroup) {
        super.setGroup(newGroup);
        
        final String fs = System.getProperty("file.separator");
        final String location = ActionManager.getDirectory() + group + fs + name;
        
        file.delete();
        file = new File(location);
        
        save();
    }
    
    /**
     * Deletes this action.
     */
    public void delete() {
        file.delete();
    }
    
    /** {@inheritDoc} */
    @Override
    public String toString() {
        final String parent = super.toString();
        
        return parent.substring(0, parent.length() - 1)
                + ",file=" + file + "]";
    }
    
}
