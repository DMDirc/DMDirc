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
import com.dmdirc.actions.interfaces.ActionComparison;
import com.dmdirc.config.prefs.PreferencesSetting;
import com.dmdirc.config.prefs.PreferencesType;
import com.dmdirc.logger.ErrorLevel;
import com.dmdirc.logger.Logger;
import com.dmdirc.util.ConfigFile;
import com.dmdirc.util.InvalidConfigFileException;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    /** The domain name for condition trees. */
    private static final String DOMAIN_CONDITIONTREE = "conditiontree".intern();
    /** The domain name for format changes. */
    private static final String DOMAIN_FORMAT = "format".intern();
    /** The domain name for meta-data. */
    private static final String DOMAIN_METADATA = "metadata".intern();
    /** The domain name for response information. */
    private static final String DOMAIN_RESPONSE = "response".intern();
    /** The domain name for triggers. */
    private static final String DOMAIN_TRIGGERS = "triggers".intern();

    /** The location of the file we're reading/saving. */
    private String location;

    /** The config file we're using. */
    protected ConfigFile config;

    /**
     * Creates a new instance of Action. The group and name specified must
     * be the group and name of a valid action already saved to disk.
     *
     * @param group The group the action belongs to
     * @param name The name of the action
     */
    public Action(final String group, final String name) {
        super(group, name);

        location = ActionManager.getDirectory() + group + File.separator + name;

        try {
            config = new ConfigFile(location);
            config.read();
            loadActionFromConfig();
        } catch (InvalidConfigFileException ex) {
            // This isn't a valid config file. Maybe it's a properties file?
            Logger.userError(ErrorLevel.MEDIUM, "Unable to parse action file: "
                    + group + "/" + name + ": " + ex.getMessage());
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
        this(group, name, triggers, response, conditions,
                ConditionTree.createConjunction(conditions.size()), newFormat);
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
     * @param conditionTree The condition tree to use
     * @param newFormat The new formatter to use
     */
    public Action(final String group, final String name,
            final ActionType[] triggers, final String[] response,
            final List<ActionCondition> conditions,
            final ConditionTree conditionTree, final String newFormat) {
        super(group, name, triggers, response, conditions, conditionTree, newFormat);

        final String dir = ActionManager.getDirectory() + group + File.separator;
        location = dir + name;

        new File(dir).mkdirs();
        
        ActionManager.processEvent(CoreActionType.ACTION_CREATED, null, this);

        save();
        
        ActionManager.registerAction(this);
    }

    /**
     * Loads this action from the config instance.
     */
    protected void loadActionFromConfig() {
        if (config.isFlatDomain(DOMAIN_TRIGGERS)) {
            if (!loadTriggers(config.getFlatDomain(DOMAIN_TRIGGERS))) {
                return;
            }
        } else {
            error("No trigger specified");
            return;
        }

        if (config.isFlatDomain(DOMAIN_RESPONSE)) {
            response = new String[config.getFlatDomain(DOMAIN_RESPONSE).size()];

            int i = 0;
            for (String line: config.getFlatDomain(DOMAIN_RESPONSE)) {
                response[i++] = line;
            }
        } else {
            error("No response specified");
            return;
        }

        if (config.isFlatDomain(DOMAIN_FORMAT)) {
            newFormat = config.getFlatDomain(DOMAIN_FORMAT).size() == 0 ? "" :
                config.getFlatDomain(DOMAIN_FORMAT).get(0);
        }

        for (int cond = 0; config.isKeyDomain("condition " + cond); cond++) {
            if (!readCondition(config.getKeyDomain("condition " + cond))) {
                return;
            }
        }

        if (config.isFlatDomain(DOMAIN_CONDITIONTREE)
                && config.getFlatDomain(DOMAIN_CONDITIONTREE).size() > 0) {
            conditionTree = ConditionTree.parseString(
                    config.getFlatDomain(DOMAIN_CONDITIONTREE).get(0));

            if (conditionTree == null) {
                error("Unable to parse condition tree");
                return;
            }

            if (conditionTree.getMaximumArgument() >= conditions.size()) {
                error("Condition tree references condition "
                        + conditionTree.getMaximumArgument() + " but there are"
                        + " only " + conditions.size() + " conditions");
                return;
            }
        }

        ActionManager.registerAction(this);

        checkMetaData();
    }

    /**
     * Checks to see if this action contains group meta-data, and adds it to
     * the group as appropriate.
     */
    private void checkMetaData() {
        if (config.isKeyDomain(DOMAIN_METADATA)) {
            final ActionGroup myGroup = ActionManager.getGroup(group);
            final Map<String, String> data = config.getKeyDomain(DOMAIN_METADATA);

            if (data.containsKey("description")) {
                myGroup.setDescription(data.get("description"));
            }

            if (data.containsKey("author")) {
                myGroup.setAuthor(data.get("author"));
            }
            
            if (data.containsKey("version")) {
                try {
                    myGroup.setVersion(Integer.parseInt(data.get("version")));
                } catch (NumberFormatException ex) {
                    // Do nothing
                }
            }
            
            if (data.containsKey("component")) {
                try {
                    myGroup.setComponent(Integer.parseInt(data.get("component")));
                } catch (NumberFormatException ex) {
                    // Do nothing
                }
            }
        }

        for (int i = 0; config.isKeyDomain("setting " + i); i++) {
            final ActionGroup myGroup = ActionManager.getGroup(group);
            final Map<String, String> data = config.getKeyDomain("setting " + i);
            
            if (data.containsKey("type") && data.containsKey("setting")
                    && data.containsKey("title") && data.containsKey("default")
                    && data.containsKey("tooltip")) {
                ActionManager.registerDefault(data.get("setting"), data.get("default"));
                myGroup.getSettings().put(data.get("setting"), new PreferencesSetting(
                        PreferencesType.valueOf(data.get("type")), "actions",
                        data.get("setting"), data.get("title"), data.get("tooltip")));
            }
        }
    }

    /**
     * Loads a list of triggers with the specified names.
     * 
     * @param newTriggers A list of trigger names
     * @return True if all triggers are valid and compatible, false otherwise.
     */
    private boolean loadTriggers(final List<String> newTriggers) {
        triggers = new ActionType[newTriggers.size()];

        for (int i = 0; i < triggers.length; i++) {
            triggers[i] = ActionManager.getActionType(newTriggers.get(i));

            if (triggers[i] == null) {
                error("Invalid trigger specified: " + newTriggers.get(i));
                return false;
            } else if (i != 0 && !triggers[i].getType().equals(triggers[0].getType())) {
                error("Triggers are not compatible");
                return false;
            }
        }

        return true;
    }

    /**
     * Called to save the action.
     */
    public void save() {
        if (!isModified()) {
            return;
        }
        
        final ConfigFile newConfig = new ConfigFile(location);

        final List<String> triggerNames = new ArrayList<String>();
        final List<String> responseLines = new ArrayList<String>();

        for (ActionType trigger : triggers) {
            if (trigger == null) {
                Logger.appError(ErrorLevel.LOW, "ActionType was null",
                        new IllegalArgumentException("Triggers: "
                        + Arrays.toString(triggers)));
                continue;
            }

            triggerNames.add(trigger.toString());
        }

        for (String line : response) {
            responseLines.add(line);
        }

        newConfig.addDomain(DOMAIN_TRIGGERS, triggerNames);
        newConfig.addDomain(DOMAIN_RESPONSE, responseLines);

        if (conditionTree != null) {
            newConfig.addDomain(DOMAIN_CONDITIONTREE, new ArrayList<String>());
            newConfig.getFlatDomain(DOMAIN_CONDITIONTREE).add(conditionTree.toString());
        }

        if (newFormat != null) {
            newConfig.addDomain(DOMAIN_FORMAT, new ArrayList<String>());
            newConfig.getFlatDomain(DOMAIN_FORMAT).add(newFormat);
        }

        int i = 0;
        for (ActionCondition condition : conditions) {
            final Map<String, String> data = new HashMap<String, String>();

            data.put("argument", String.valueOf(condition.getArg()));
            
            if (condition.getArg() == -1) {
                data.put("starget", condition.getStarget());
            } else {
                data.put("component", condition.getComponent().toString());
            }
            
            data.put("comparison", condition.getComparison().toString());
            data.put("target", condition.getTarget());

            newConfig.addDomain("condition " + i, data);
            i++;
        }

        if (config != null) {
            // Preserve any meta-data
            if (config.isKeyDomain(DOMAIN_METADATA)) {
                newConfig.addDomain(DOMAIN_METADATA, config.getKeyDomain(DOMAIN_METADATA));
            }

            for (i = 0; config.isKeyDomain("setting " + i); i++) {
                newConfig.addDomain("setting " + i, config.getKeyDomain("setting " + i));
            }
        }

        try {
            newConfig.write();

            resetModified();
        } catch (IOException ex) {
            Logger.userError(ErrorLevel.HIGH, "I/O error when saving action: "
                    + group + "/" + name + ": " + ex.getMessage());
        }
        ActionManager.processEvent(CoreActionType.ACTION_UPDATED, null, this);
    }

    /**
     * Reads a condition from the specified configuration section.
     * 
     * @param data The relevant section of the action configuration
     * @return True if the condition is valid, false otherwise
     */
    private boolean readCondition(final Map<String,String> data) {
        int arg = 0;
        ActionComponent component = null;
        ActionComparison comparison = null;
        String target = "";
        String starget = null;

        // ------ Read the argument

        try {
            arg = Integer.parseInt(data.get("argument"));
        } catch (NumberFormatException ex) {
            error("Invalid argument number specified: " + data.get("argument"));
            return false;
        }

        if (arg < -1 || arg >= triggers[0].getType().getArity()) {
            error("Invalid argument number specified: " + arg);
            return false;
        }

        // ------ Read the component or the source

        if (arg == -1) {
            starget = data.get("starget");
            
            if (starget == null) {
                error("No starget specified");
                return false;
            }
        } else {
            component = readComponent(data, arg);
            if (component == null) {
                return false;
            }            
        }

        // ------ Read the comparison

        comparison = ActionManager.getActionComparison(data.get("comparison"));
        if (comparison == null) {
            error("Invalid comparison specified: " + data.get("comparison"));
            return false;
        }

        if ((arg != -1 && !comparison.appliesTo().equals(component.getType())) 
            || (arg == -1 && !comparison.appliesTo().equals(String.class))) {
            error("Comparison cannot be applied to specified component: " + data.get("comparison"));
            return false;
        }

        // ------ Read the target

        target = data.get("target");

        if (target == null) {
            error("No target specified for condition");
            return false;
        }

        if (arg == -1) {
            conditions.add(new ActionCondition(starget, comparison, target));
        } else {
            conditions.add(new ActionCondition(arg, component, comparison, target));
        }
        
        return true;
    }

    /**
     * Reads a component from the specified data section for the specified argument.
     * 
     * @param data The relevant section of the action configuration
     * @param arg The argument number that the component should apply to
     * @return The corresponding ActionComponent, or null if the specified
     * component is invalid.
     */
    private ActionComponent readComponent(final Map<String, String> data, final int arg) {
        final String componentName = data.get("component");
        ActionComponent component;

        if (componentName.indexOf('.') == -1) {
            component = ActionManager.getActionComponent(componentName);
        } else {
            try {
                component = new ActionComponentChain(triggers[0].getType().getArgTypes()[arg],
                        componentName);
            } catch (IllegalArgumentException iae) {
                error(iae.getMessage());
                return null;
            }
        }

        if (component == null) {
            error("Unknown component: " + componentName);
            return null;
        }

        if (!component.appliesTo().equals(triggers[0].getType().getArgTypes()[arg])) {
            error("Component cannot be applied to specified arg in condition: " + componentName);
            return null;
        }

        return component;
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

    /** {@inheritDoc} */
    @Override
    public void setName(final String newName) {
        super.setName(newName);

        new File(location).delete();        
        location = ActionManager.getDirectory() + group + File.separator + newName;

        save();
    }

    /** {@inheritDoc} */
    @Override
    public void setGroup(final String newGroup) {
        super.setGroup(newGroup);

        new File(location).delete();

        final String dir = ActionManager.getDirectory() + group + File.separator;
        location = dir + name;

        new File(dir).mkdirs();        

        save();
    }

    /**
     * Deletes this action.
     */
    public void delete() {
        ActionManager.processEvent(CoreActionType.ACTION_DELETED, null, getGroup(), getName());
        new File(location).delete();
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        final String parent = super.toString();

        return parent.substring(0, parent.length() - 1)
                + ",location=" + location + "]";
    }

}
