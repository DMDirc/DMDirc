/*
 * Copyright (c) 2006-2015 DMDirc Developers
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

import com.dmdirc.DMDircMBassador;
import com.dmdirc.GlobalWindow;
import com.dmdirc.config.prefs.PreferencesSetting;
import com.dmdirc.config.prefs.PreferencesType;
import com.dmdirc.events.ActionCreatedEvent;
import com.dmdirc.events.ActionDeletedEvent;
import com.dmdirc.events.ActionUpdatedEvent;
import com.dmdirc.events.AppErrorEvent;
import com.dmdirc.events.UserErrorEvent;
import com.dmdirc.interfaces.ActionController;
import com.dmdirc.interfaces.actions.ActionComparison;
import com.dmdirc.interfaces.actions.ActionComponent;
import com.dmdirc.interfaces.actions.ActionType;
import com.dmdirc.interfaces.config.ConfigChangeListener;
import com.dmdirc.interfaces.config.IdentityController;
import com.dmdirc.logger.ErrorLevel;
import com.dmdirc.updater.Version;
import com.dmdirc.util.io.ConfigFile;
import com.dmdirc.util.io.InvalidConfigFileException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Provider;

/**
 * Describes a single action.
 */
public class Action extends ActionModel implements ConfigChangeListener {

    /** The domain name for condition trees. */
    private static final String DOMAIN_CONDITIONTREE = "conditiontree";
    /** The domain name for format changes. */
    private static final String DOMAIN_FORMAT = "format";
    /** The domain name for meta-data. */
    private static final String DOMAIN_METADATA = "metadata";
    /** The domain name for response information. */
    private static final String DOMAIN_RESPONSE = "response";
    /** The domain name for triggers. */
    private static final String DOMAIN_TRIGGERS = "triggers";
    /** The domain name for concurrency. */
    private static final String DOMAIN_CONCURRENCY = "concurrency";
    /** The domain name for misc settings. */
    private static final String DOMAIN_MISC = "misc";
    /** The base directory to save actions in. */
    private final Path actionsDirectory;
    /** The controller to use to read and update settings. */
    private final IdentityController identityController;
    /** The controller to use to retrieve components, comparisons, etc. */
    private final ActionController actionController;
    /** Event bus to post events to. */
    private final DMDircMBassador eventBus;
    /** The config file we're using. */
    protected ConfigFile config;
    /** The location of the file we're reading/saving. */
    private Path location;

    /**
     * Creates a new instance of Action. The group and name specified must be the group and name of
     * a valid action already saved to disk.
     *
     * @param eventBus                    Event bus to post events to
     * @param globalWindowProvider        Provider of global windows for triggering actions.
     * @param substitutorFactory          Factory to use to create action substitutors.
     * @param actionController            The controller that owns this action.
     * @param identityController          The controller to use to retrieve and update settings.
     * @param actionsDirectory            The base directory to store actions in.
     * @param group                       The group the action belongs to
     * @param name                        The name of the action
     */
    public Action(final DMDircMBassador eventBus,
            final Provider<GlobalWindow> globalWindowProvider,
            final ActionSubstitutorFactory substitutorFactory,
            final ActionController actionController, final IdentityController identityController,
            final Path actionsDirectory, final String group, final String name) {
        super(globalWindowProvider, substitutorFactory, group, name);

        this.eventBus = eventBus;
        this.actionController = actionController;
        this.identityController = identityController;
        this.actionsDirectory = actionsDirectory;
        this.location = actionsDirectory.resolve(group).resolve(name);

        try {
            config = new ConfigFile(location);
            config.read();
            loadActionFromConfig();
            actionController.addAction(this);
        } catch (InvalidConfigFileException ex) {
            error(ActionErrorType.FILE, "Unable to parse action file: " + ex.getMessage());
        } catch (IOException ex) {
            error(ActionErrorType.FILE, "I/O error when loading action: " + ex.getMessage());
        }

        identityController.getGlobalConfiguration().addChangeListener("disable_action",
                (group + "/" + name).replace(' ', '.'), this);
        checkDisabled();
    }

    /**
     * Creates a new instance of Action with the specified properties and saves it to disk.
     *
     * @param eventBus                    Event bus to post events to
     * @param globalWindowProvider        Provider of global windows for triggering actions.
     * @param substitutorFactory          Factory to use to create action substitutors.
     * @param actionController            The controller that owns this action.
     * @param identityController          The controller to use to retrieve and update settings.
     * @param actionsDirectory            The base directory to store actions in.
     * @param group                       The group the action belongs to
     * @param name                        The name of the action
     * @param triggers                    The triggers to use
     * @param response                    The response to use
     * @param conditions                  The conditions to use
     * @param conditionTree               The condition tree to use
     * @param newFormat                   The new formatter to use
     */
    public Action(final DMDircMBassador eventBus,
            final Provider<GlobalWindow> globalWindowProvider,
            final ActionSubstitutorFactory substitutorFactory,
            final ActionController actionController, final IdentityController identityController,
            final Path actionsDirectory, final String group, final String name,
            final ActionType[] triggers, final String[] response,
            final List<ActionCondition> conditions, final ConditionTree conditionTree,
            final String newFormat) {
        super(globalWindowProvider, substitutorFactory, group, name,
                triggers, response, conditions, conditionTree, newFormat);

        this.eventBus = eventBus;
        this.actionController = actionController;
        this.identityController = identityController;
        this.actionsDirectory = actionsDirectory;
        this.location = actionsDirectory.resolve(group)
                .resolve(name.replaceAll("[^A-Za-z0-9\\-_]", "_"));

        try {
            Files.createDirectories(location.getParent());
        } catch (IOException ex) {
            //TODO we don't handle the error now, but we should
        }

        save();

        identityController.getGlobalConfiguration().addChangeListener("disable_action",
                (group + "/" + name).replace(' ', '.'), this);
        checkDisabled();

        actionController.addAction(this);
        //TODO This needs to be done somewhere else, remove eventbus when it is.
        eventBus.publishAsync(new ActionCreatedEvent(this));
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
            error(ActionErrorType.TRIGGERS, "No trigger specified");
            return;
        }

        if (config.isFlatDomain(DOMAIN_RESPONSE)) {
            response = new String[config.getFlatDomain(DOMAIN_RESPONSE).size()];

            int i = 0;
            for (String line : config.getFlatDomain(DOMAIN_RESPONSE)) {
                response[i++] = line;
            }
        } else {
            error(ActionErrorType.RESPONSE, "No response specified");
            return;
        }

        if (config.isFlatDomain(DOMAIN_FORMAT)) {
            newFormat = config.getFlatDomain(DOMAIN_FORMAT).isEmpty() ? ""
                    : config.getFlatDomain(DOMAIN_FORMAT).get(0);
        }

        for (int cond = 0; config.isKeyDomain("condition " + cond); cond++) {
            if (!readCondition(config.getKeyDomain("condition " + cond))) {
                return;
            }
        }

        if (config.isFlatDomain(DOMAIN_CONDITIONTREE)
                && !config.getFlatDomain(DOMAIN_CONDITIONTREE).isEmpty()) {
            conditionTree = ConditionTree.parseString(
                    config.getFlatDomain(DOMAIN_CONDITIONTREE).get(0));

            if (conditionTree == null) {
                error(ActionErrorType.CONDITION_TREE, "Unable to parse condition tree");
                return;
            }

            if (conditionTree.getMaximumArgument() >= conditions.size()) {
                error(ActionErrorType.CONDITION_TREE, "Condition tree references condition "
                        + conditionTree.getMaximumArgument() + " but there are"
                        + " only " + conditions.size() + " conditions");
                return;
            }
        }

        if (config.isKeyDomain(DOMAIN_CONCURRENCY)
                && config.getKeyDomain(DOMAIN_CONCURRENCY).containsKey("group")) {
            setConcurrencyGroup(config.getKeyDomain(DOMAIN_CONCURRENCY).get("group"));
        }

        if (config.isKeyDomain(DOMAIN_MISC)
                && config.getKeyDomain(DOMAIN_MISC).containsKey("stopping")) {
            setStopping(Boolean.parseBoolean(config.getKeyDomain(DOMAIN_MISC).get("stopping")));
        }

        if (status == ActionStatus.DISABLED) {
            status = ActionStatus.ACTIVE;
        }

        checkMetaData();
    }

    /**
     * Checks to see if this action contains group meta-data, and adds it to the group as
     * appropriate.
     */
    private void checkMetaData() {
        if (config.isKeyDomain(DOMAIN_METADATA)) {
            final ActionGroup myGroup = actionController.getOrCreateGroup(group);
            final Map<String, String> data = config.getKeyDomain(DOMAIN_METADATA);

            if (data.containsKey("description")) {
                myGroup.setDescription(data.get("description"));
            }

            if (data.containsKey("author")) {
                myGroup.setAuthor(data.get("author"));
            }

            if (data.containsKey("version")) {
                myGroup.setVersion(new Version(data.get("version")));
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
            final ActionGroup myGroup = actionController.getOrCreateGroup(group);
            final Map<String, String> data = config.getKeyDomain("setting " + i);

            if (data.containsKey("type") && data.containsKey("setting")
                    && data.containsKey("title") && data.containsKey("default")
                    && data.containsKey("tooltip")) {
                actionController.registerSetting(data.get("setting"), data.get("default"));
                myGroup.getSettings().put(data.get("setting"), new PreferencesSetting(
                        PreferencesType.valueOf(data.get("type")), "actions",
                        data.get("setting"), data.get("title"), data.get("tooltip"),
                        identityController.getGlobalConfiguration(),
                        identityController.getUserSettings()));
            }
        }
    }

    /**
     * Loads a list of triggers with the specified names.
     *
     * @param newTriggers A list of trigger names
     *
     * @return True if all triggers are valid and compatible, false otherwise.
     */
    private boolean loadTriggers(final List<String> newTriggers) {
        triggers = new ActionType[newTriggers.size()];

        for (int i = 0; i < triggers.length; i++) {
            triggers[i] = actionController.getType(newTriggers.get(i));

            if (triggers[i] == null) {
                error(ActionErrorType.TRIGGERS, "Invalid trigger specified: " + newTriggers.get(i));
                return false;
            } else if (i != 0 && !triggers[i].getType().equals(triggers[0].getType())) {
                error(ActionErrorType.TRIGGERS, "Triggers are not compatible");
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

        final List<String> triggerNames = new ArrayList<>();
        final List<String> responseLines = new ArrayList<>();
        responseLines.addAll(Arrays.asList(response));

        for (ActionType trigger : triggers) {
            if (trigger == null) {
                eventBus.publish(new AppErrorEvent(ErrorLevel.LOW,
                        new IllegalArgumentException("Triggers: "+ Arrays.toString(triggers)),
                        "ActionType was null", ""));
                continue;
            }

            triggerNames.add(trigger.toString());
        }

        newConfig.addDomain(DOMAIN_TRIGGERS, triggerNames);
        newConfig.addDomain(DOMAIN_RESPONSE, responseLines);

        if (conditionTree != null) {
            newConfig.addDomain(DOMAIN_CONDITIONTREE, new ArrayList<>());
            newConfig.getFlatDomain(DOMAIN_CONDITIONTREE).add(conditionTree.toString());
        }

        if (newFormat != null) {
            newConfig.addDomain(DOMAIN_FORMAT, new ArrayList<>());
            newConfig.getFlatDomain(DOMAIN_FORMAT).add(newFormat);
        }

        if (concurrencyGroup != null) {
            newConfig.addDomain(DOMAIN_CONCURRENCY, new HashMap<>());
            newConfig.getKeyDomain(DOMAIN_CONCURRENCY).put("group", concurrencyGroup);
        }

        if (stop) {
            newConfig.addDomain(DOMAIN_MISC, new HashMap<>());
            newConfig.getKeyDomain(DOMAIN_MISC).put("stopping", "true");
        }

        int i = 0;
        for (ActionCondition condition : conditions) {
            final Map<String, String> data = new HashMap<>();

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
            eventBus.publish(new UserErrorEvent(ErrorLevel.HIGH, ex,
                    "I/O error when saving action: " + group + '/' + name + ": " + ex.getMessage(),
                    ""));
        }

        eventBus.publishAsync(new ActionUpdatedEvent(this));
    }

    /**
     * Reads a condition from the specified configuration section.
     *
     * @param data The relevant section of the action configuration
     *
     * @return True if the condition is valid, false otherwise
     */
    private boolean readCondition(final Map<String, String> data) {
        final int arg;
        ActionComponent component = null;
        final ActionComparison comparison;
        final String target;
        String starget = null;

        // ------ Read the argument
        try {
            arg = Integer.parseInt(data.get("argument"));
        } catch (NumberFormatException ex) {
            error(ActionErrorType.CONDITIONS,
                    "Invalid argument number specified: " + data.get("argument"));
            return false;
        }

        if (arg < -1 || arg >= triggers[0].getType().getArity()) {
            error(ActionErrorType.CONDITIONS, "Invalid argument number specified: " + arg);
            return false;
        }

        // ------ Read the component or the source
        if (arg == -1) {
            starget = data.get("starget");

            if (starget == null) {
                error(ActionErrorType.CONDITIONS, "No starget specified");
                return false;
            }
        } else {
            component = readComponent(data, arg);
            if (component == null) {
                return false;
            }
        }

        // ------ Read the comparison
        comparison = actionController.getComparison(data.get("comparison"));
        if (comparison == null) {
            error(ActionErrorType.CONDITIONS, "Invalid comparison specified: "
                    + data.get("comparison"));
            return false;
        }

        if ((arg != -1 && !comparison.appliesTo().equals(component.getType()))
                || (arg == -1 && !comparison.appliesTo().equals(String.class))) {
            error(ActionErrorType.CONDITIONS,
                    "Comparison cannot be applied to specified component: " + data.get("comparison"));
            return false;
        }

        // ------ Read the target
        target = data.get("target");

        if (target == null) {
            error(ActionErrorType.CONDITIONS, "No target specified for condition");
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
     * @param arg  The argument number that the component should apply to
     *
     * @return The corresponding ActionComponent, or null if the specified component is invalid.
     */
    private ActionComponent readComponent(final Map<String, String> data, final int arg) {
        final String componentName = data.get("component");
        final ActionComponent component;

        if (componentName.indexOf('.') == -1) {
            component = actionController.getComponent(componentName);
        } else {
            try {
                component = new ActionComponentChain(triggers[0].getType().getArgTypes()[arg],
                        componentName, actionController);
            } catch (IllegalArgumentException iae) {
                error(ActionErrorType.CONDITIONS, iae.getMessage());
                return null;
            }
        }

        if (component == null) {
            error(ActionErrorType.CONDITIONS, "Unknown component: " + componentName);
            return null;
        }

        if (!component.appliesTo().equals(triggers[0].getType().getArgTypes()[arg])) {
            error(ActionErrorType.CONDITIONS,
                    "Component cannot be applied to specified arg in condition: " + componentName);
            return null;
        }

        return component;
    }

    /**
     * Raises a trivial error, informing the user of the problem.
     *
     * @param type    The type of error that occurred.
     * @param message The message to be raised
     */
    private void error(final ActionErrorType type, final String message) {
        this.error = message;
        this.errorType = type;
        this.status = ActionStatus.FAILED;

        eventBus.publish(new UserErrorEvent(ErrorLevel.LOW,
                new IllegalStateException("Error when parsing action: " + group + '/' + name + ": "
                        + message),
                "Error when parsing action" + group + '/' + name + ": " + message, ""));
    }

    @Override
    public void setName(final String newName) {
        super.setName(newName);

        try {
            Files.delete(location);
        } catch (IOException ex) {
            //TODO we don't handle the error now, but we should
        }
        location = actionsDirectory.resolve(group).resolve(newName);

        save();
    }

    @Override
    public void setGroup(final String newGroup) {
        super.setGroup(newGroup);

        try {
            Files.delete(location);
        } catch (IOException ex) {
            //TODO we don't handle the error now, but we should
        }

        location = actionsDirectory.resolve(group).resolve(name);

        try {
            Files.createDirectories(location);
        } catch (IOException ex) {
            //TODO we don't handle the error now, but we should
        }


        save();
    }

    /**
     * Deletes this action.
     */
    public void delete() {
        eventBus.publishAsync(new ActionDeletedEvent(actionController.getOrCreateGroup(getGroup()), this));
        try {
            Files.delete(location);
        } catch (IOException ex) {
            //TODO we don't handle the error now, but we should
        }
    }

    @Override
    public String toString() {
        final String parent = super.toString();

        return parent.substring(0, parent.length() - 1) + ",location=" + location + "]";
    }

    @Override
    public void configChanged(final String domain, final String key) {
        checkDisabled();
    }

    /**
     * Checks if this action is disabled or not.
     *
     * @since 0.6.3
     */
    protected void checkDisabled() {
        final String key = (group + "/" + name).replace(' ', '.');
        final boolean disabled = identityController.getGlobalConfiguration().hasOptionBool(
                "disable_action", key)
                && identityController.getGlobalConfiguration().getOptionBool("disable_action", key);

        if (disabled && status == ActionStatus.ACTIVE) {
            status = ActionStatus.DISABLED;
        } else if (!disabled && status == ActionStatus.DISABLED) {
            status = ActionStatus.ACTIVE;
        }
    }

    /**
     * Sets whether this action is enabled or not.
     *
     * @param enabled true to enable, false to disable
     */
    public void setEnabled(final boolean enabled) {
        if (enabled) {
            identityController.getUserSettings().unsetOption("disable_action",
                    (group + "/" + name).replace(' ', '.'));
        } else {
            identityController.getUserSettings().setOption("disable_action",
                    (group + "/" + name).replace(' ', '.'), true);
        }
    }

}
