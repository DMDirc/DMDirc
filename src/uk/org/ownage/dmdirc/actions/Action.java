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
        
        if (properties.containsKey("trigger")) {
            trigger = ActionManager.getActionType(properties.getProperty("trigger"));
        } else {
            valid = false;
        }
        
        if (properties.containsKey("response")) {
            response = properties.getProperty("response").split("\n");
        } else {
            valid = false;
        }
        
        // TODO: Read conditions
        
        if (valid) {
            ActionManager.registerAction(this);
        }
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
        // TODO: Check conditions
        
        for (String command : response) {
            final CommandWindow cw = ((FrameContainer) arguments[0]).getFrame();
            cw.getCommandParser().parseCommand(cw, command);
        }
    }
    
}
