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
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import uk.org.ownage.dmdirc.FrameContainer;

import uk.org.ownage.dmdirc.commandparser.CommandWindow;
import uk.org.ownage.dmdirc.logger.ErrorLevel;
import uk.org.ownage.dmdirc.logger.Logger;

/**
 * Describes a single action.
 * @author chris
 */
public class Action {
    
    /** The group this action belongs to. */
    private String group;
    
    /** The name of this action. */
    private String name;
    
    /** The file containing this action. */
    private final File file;
    
    /** The properties read for this action. */
    private Properties properties;
    
    /** The ActionTypes that trigger this action. */
    private ActionType[] triggers;
    
    /** The commands to execute if this action is triggered. */
    private String[] response;
    
    /** The change that should be made to the format string, if any. */
    private String newFormat = null;
    
    /** The conditions for this action. */
    private final List<ActionCondition> conditions = new ArrayList<ActionCondition>();
    
    /**
     * Creates a new instance of Action. The group and name specified must
     * be the group and name of a valid action already saved to disk.
     * @param group The group the action belongs to
     * @param name The name of the action
     */
    public Action(final String group, final String name) {
        this.group = group;
        this.name = name;
        
        final String fs = System.getProperty("file.separator");
        final String location = ActionManager.getDirectory() + group + fs + name;
        
        file = new File(location);
        
        try {
            final FileInputStream inputStream = new FileInputStream(file);
            
            properties = new Properties();
            properties.load(inputStream);
            loadAction();
        } catch (IOException ex) {
            Logger.error(ErrorLevel.ERROR, "Unable to load action: " + group + "/" + name, ex);
        }
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
                        if (!checkArgs(args, triggers[i].getType().getArgTypes())) {
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
            properties.list(System.out);
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
     * Checks to see if the two sets of arguments are equal.
     * @param arg1 The first argument to be tested
     * @param arg2 The second argument to be tested
     * @return True iff the args are equal, false otherwise
     */
    private boolean checkArgs(final Class[] arg1, final Class[] arg2) {
        if (arg1.length != arg2.length) {
            return false;
        }
        
        for (int i = 0; i < arg1.length; i++) {
            if (!arg1[i].getName().equals(arg2[i].getName())) {
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * Reads the specified condition.
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
            component = ActionManager.getActionComponent(properties.getProperty("condition" + condition + "-component"));
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
     * @param message The message to be raised
     */
    private void error(final String message) {
        Logger.error(ErrorLevel.TRIVIAL, "Unable to parse action " + group + "/" + name + ": " + message);
    }
    
    /**
     * Retrieves this action's trigger.
     * @return The action type that triggers this action
     */
    public ActionType[] getTrigger() {
        return triggers;
    }
    
    /**
     * Retrieves this action's group name.
     * @return This action's group name
     */
    public String getGroup() {
        return group;
    }
    
    /**
     * Triggers this action.
     * @param format The format of the message that's going to be displayed.
     * @param arguments The arguments from the action that caused this trigger.
     */
    public void trigger(final StringBuffer format, final Object ... arguments) {
        for (ActionCondition condition : conditions) {
            if (!condition.test(arguments)) {
                return;
            }
        }
        
        for (String command : response) {
            final CommandWindow cw = ((FrameContainer) arguments[0]).getFrame();
            cw.getCommandParser().parseCommand(cw, ActionManager.substituteVars(command, arguments));
        }
        
        if (newFormat != null && format != null) {
            format.setLength(0);
            format.append(newFormat);
        }
    }
    
}
