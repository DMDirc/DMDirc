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
    
    /** The ActionType that triggers this action. */
    private ActionType trigger;
    
    /** The commands to execute if this action is triggered. */
    private String[] response;
    
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
        boolean valid = true;
        
        // Read the trigger
        if (properties.containsKey("trigger")) {
            trigger = ActionManager.getActionType(properties.getProperty("trigger"));
            
            if (trigger == null) {
                System.out.println("Invalid trigger");
                valid = false;
            }
        } else {
            valid = false;
        }
        
        // Read the response
        if (properties.containsKey("response")) {
            response = properties.getProperty("response").split("\n");
        } else {
            System.out.println("Invalid response");
            properties.list(System.out);
            valid = false;
        }
        
        // Read the conditions
        int numConditions = 0;
        
        if (properties.containsKey("conditions")) {
            try {
                numConditions = Integer.parseInt(properties.getProperty("conditions"));
            } catch (NumberFormatException ex) {
                System.out.println("Invalid conditions");
                valid = false;
            }
        }
        
        for (int i = 0; i < numConditions; i++) {
            valid = valid & readCondition(i);
        }
        
        if (valid) {
            ActionManager.registerAction(this);
        }
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
                System.out.println("Invalid condition arg");
                return false;
            }
        }
        
        if (arg < 0 || arg >= trigger.getType().getArity()) {
            System.out.println("Condition arg out of range");
            return false;
        }
        
        if (properties.containsKey("condition" + condition + "-component")) {
            try {
                component = ActionComponent.valueOf(properties.getProperty("condition" + condition + "-component"));
            } catch (IllegalArgumentException ex) {
                System.out.println("Invalid condition component");
                return false;
            }
            
            if (!component.appliesTo().equals(trigger.getType().getArgTypes()[arg])) {
                System.out.println("Condition component cannot be applied to specified arg");
                System.out.println(component.appliesTo().getName());
                System.out.println(trigger.getType().getArgTypes()[arg].getName());
                return false;
            }
        } else {
            System.out.println("No component specified");
            return false;
        }
        
        if (properties.containsKey("condition" + condition + "-comparison")) {
            try {
                comparison = ActionComparison.valueOf(properties.getProperty("condition" + condition + "-comparison"));
            } catch (IllegalArgumentException ex) {
                System.out.println("Invalid comparison");
                return false;
            }
            
            if (!comparison.appliesTo().equals(component.getType())) {
                System.out.println("Comparison cannot be applied to component type");
                return false;
            }
        } else {
            System.out.println("No comparison specified");
            return false;
        }
        
        if (properties.containsKey("condition" + condition + "-target")) {
            target = properties.getProperty("condition" + condition + "-target");
        } else {
            System.out.println("No target specified");
            return false;
        }
        
        conditions.add(new ActionCondition(arg, component, comparison, target));
        return true;
    }
    
    /**
     * Retrieves this action's trigger.
     * @return The action type that triggers this action
     */
    public ActionType getTrigger() {
        return trigger;
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
     * @param arguments The arguments from the action that caused this trigger.
     */
    public void trigger(final Object ... arguments) {
        for (ActionCondition condition : conditions) {
            if (!condition.test(arguments)) {
                return;
            }
        }
        
        for (String command : response) {
            final CommandWindow cw = ((FrameContainer) arguments[0]).getFrame();
            cw.getCommandParser().parseCommand(cw, command);
        }
    }
    
}
