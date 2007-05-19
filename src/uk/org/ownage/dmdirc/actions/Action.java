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
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import uk.org.ownage.dmdirc.FrameContainer;
import uk.org.ownage.dmdirc.ServerManager;
import uk.org.ownage.dmdirc.commandparser.CommandParser;
import uk.org.ownage.dmdirc.commandparser.CommandWindow;
import uk.org.ownage.dmdirc.commandparser.GlobalCommandParser;
import uk.org.ownage.dmdirc.logger.ErrorLevel;
import uk.org.ownage.dmdirc.logger.Logger;
import uk.org.ownage.dmdirc.ui.MainFrame;

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
    private List<ActionCondition> conditions = new ArrayList<ActionCondition>();
    
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
            
            inputStream.close();
        } catch (IOException ex) {
            Logger.error(ErrorLevel.ERROR, "Unable to load action: " + group + "/" + name, ex);
        }
    }
    
    /**
     * Creates a new instance of Action with the specified properties and saves
     * it to disk.
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
        this.group = group;
        this.name = name;
        this.triggers = triggers.clone();
        this.response = response.clone();
        this.conditions = conditions;
        this.newFormat = newFormat;
        
        final String fs = System.getProperty("file.separator");
        final String location = ActionManager.getDirectory() + group + fs + name;
        
        file = new File(location);        
        
        save();
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
    
    /** Called to save the action. */
    public void save() {
        final Properties properties = new Properties();
        final StringBuffer triggerString = new StringBuffer();
        final StringBuffer responseString = new StringBuffer();
        
        for (ActionType trigger : triggers) {
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
            Logger.error(ErrorLevel.ERROR, "Unable to save action: " + group + "/" + name, ex);
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
     * Renames this action to the specified new name.
     * @param newName The new name for this action
     */
    public void rename(final String newName) {
        file.renameTo(new File(file.getParent() + System.getProperty("file.separator") + newName));
        name = newName;
    }
    
    /**
     * Retrieves a list of this action's conditions.
     * @return A list of this action's conditions
     */
    public List<ActionCondition> getConditions() {
        return conditions;
    }
    
    /**
     * Sets this action's conditions
     * @param conditions A list of conditions to use
     */
    public void setConditions(final List<ActionCondition> conditions) {
        this.conditions = conditions;
    }
    
    /**
     * Retrieves this action's triggers.
     * @return The triggers used by this action
     */
    public ActionType[] getTriggers() {
        return triggers.clone();
    }
    
    /**
     * Sets this action's triggers.
     * @param triggers The new triggers to use
     */
    public void setTriggers(final ActionType[] triggers) {
        this.triggers = triggers.clone();
    }
    
    /**
     * Retrieves this action's new format setting
     * @return The format that this action will use, or null if no change
     */
    public String getNewFormat() {
        return newFormat;
    }
    
    /**
     * Sets this action's new format setting
     * @param newFormat The new 'new format' setting
     */
    public void setNewFormat(final String newFormat) {
        this.newFormat = newFormat;
    }
    
    /**
     * Retrieves this action's response.
     * @return The commands that will be executed if this action is triggered
     */
    public String[] getResponse() {
        return response.clone();
    }
    
    /**
     * Sets this action's response.
     * @param The new response to use
     */
    public void setResponse(final String[] response) {
        this.response = response.clone();
    }
    
    /**
     * Retrieves this action's group name.
     * @return This action's group name
     */
    public String getGroup() {
        return group;
    }
    
    /**
     * Retrieves this action's name.
     * @return This action's name
     */
    public String getName() {
        return name;
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
        
        CommandWindow cw;
        CommandParser cp = null;
        
        if (arguments.length > 0 && arguments[0] instanceof FrameContainer) {
            cw = ((FrameContainer) arguments[0]).getFrame();
        } else if (MainFrame.getMainFrame().getActiveFrame() != null) {
            cw = (CommandWindow) MainFrame.getMainFrame().getActiveFrame();
        } else if (ServerManager.getServerManager().numServers() > 0) {
            cw = ServerManager.getServerManager().getServers().get(0).getFrame();
        } else {
            cw = null;
            cp = GlobalCommandParser.getGlobalCommandParser();
        }
        
        if (cw != null) {
            cp = cw.getCommandParser();
        }
        
        for (String command : response) {
            cp.parseCommand(cw, ActionManager.substituteVars(command, arguments));
        }
        
        if (newFormat != null && format != null) {
            format.setLength(0);
            format.append(newFormat);
        }
    }
    
}
