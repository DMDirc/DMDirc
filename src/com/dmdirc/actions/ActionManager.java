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
import com.dmdirc.Precondition;
import com.dmdirc.ui.messages.WhoisNumericFormatter;
import com.dmdirc.commandline.CommandLineOptionsModule.Directory;
import com.dmdirc.commandline.CommandLineOptionsModule.DirectoryType;
import com.dmdirc.config.ConfigBinding;
import com.dmdirc.events.AppErrorEvent;
import com.dmdirc.events.ClientClosedEvent;
import com.dmdirc.events.DMDircEvent;
import com.dmdirc.events.DisplayableEvent;
import com.dmdirc.events.UserErrorEvent;
import com.dmdirc.interfaces.ActionController;
import com.dmdirc.interfaces.actions.ActionComparison;
import com.dmdirc.interfaces.actions.ActionComponent;
import com.dmdirc.interfaces.actions.ActionType;
import com.dmdirc.interfaces.config.IdentityController;
import com.dmdirc.logger.ErrorLevel;
import com.dmdirc.updater.components.ActionGroupComponent;
import com.dmdirc.updater.manager.UpdateManager;
import com.dmdirc.util.collections.MapList;
import com.dmdirc.util.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.engio.mbassy.listener.Handler;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Manages all actions for the client.
 */
@Singleton
public class ActionManager implements ActionController {

    private static final Logger LOG = LoggerFactory.getLogger(ActionManager.class);
    /** The identity manager to load configuration from. */
    private final IdentityController identityManager;
    /** The factory to use to create actions. */
    private final ActionFactory factory;
    /** Provider of an update manager. */
    private final Provider<UpdateManager> updateManagerProvider;
    /** A list of registered action types. */
    private final Collection<ActionType> types = new ArrayList<>();
    /** A list of registered action components. */
    private final Collection<ActionComponent> components = new ArrayList<>();
    /** A list of registered action comparisons. */
    private final Collection<ActionComparison> comparisons = new ArrayList<>();
    /** A map linking types and a list of actions that're registered for them. */
    private final MapList<ActionType, Action> actions = new MapList<>();
    /** A map linking groups and a list of actions that're in them. */
    private final Map<String, ActionGroup> groups = new HashMap<>();
    /** A map of objects to synchronise on for concurrency groups. */
    private final Map<String, Object> locks = new HashMap<>();
    /** A map of the action type groups to the action types within. */
    private final MapList<String, ActionType> typeGroups = new MapList<>();
    /** The global event bus to monitor. */
    private final DMDircMBassador eventBus;
    /** The directory to load and save actions in. */
    private final String directory;
    /** Indicates whether or not user actions should be killed (not processed). */
    @ConfigBinding(domain = "actions", key = "killswitch")
    private boolean killSwitch;

    /**
     * Creates a new instance of ActionManager.
     *
     * @param identityManager        The IdentityManager to load configuration from.
     * @param factory                The factory to use to create new actions.
     * @param updateManagerProvider  Provider of an update manager, to register components.
     * @param eventBus               The global event bus to monitor.
     * @param directory              The directory to load and save actions in.
     */
    @Inject
    public ActionManager(
            final IdentityController identityManager,
            final ActionFactory factory,
            final Provider<UpdateManager> updateManagerProvider,
            final DMDircMBassador eventBus,
            @Directory(DirectoryType.ACTIONS) final String directory) {
        this.identityManager = identityManager;
        this.factory = factory;
        this.updateManagerProvider = updateManagerProvider;
        this.eventBus = eventBus;
        this.directory = directory;
    }

    /**
     * Initialiases the actions manager.
     *
     * @param colourComparisons The colour comparisons to use.
     */
    // TODO: Refactor to take a list of comparisons/sources.
    public void initialise(final ColourActionComparison colourComparisons) {
        LOG.info("Initialising the actions manager");

        identityManager.getGlobalConfiguration().getBinder().bind(this, ActionManager.class);

        registerTypes(CoreActionType.values());
        registerComparisons(CoreActionComparison.values());
        registerComparisons(colourComparisons.getComparisons());
        registerComponents(CoreActionComponent.values());

        new WhoisNumericFormatter(identityManager.getAddonSettings(), eventBus).register();

        eventBus.subscribe(this);
    }

    @Override
    public void saveAllActions() {
        for (ActionGroup group : groups.values()) {
            for (Action action : group) {
                action.save();
            }
        }
    }

    /**
     * Saves all actions when the client is being closed.
     *
     * @param event The event that was raised.
     */
    @Handler
    public void handleClientClosed(final ClientClosedEvent event) {
        LOG.debug("Client closed - saving all actions");
        saveAllActions();
    }

    @Override
    public void registerSetting(final String name, final String value) {
        LOG.debug("Registering new action setting: {} = {}", name, value);
        identityManager.getAddonSettings().setOption("actions", name, value);
    }

    @Override
    public void addGroup(final ActionGroup group) {
        groups.put(group.getName(), group);
    }

    @Override
    public void registerTypes(final ActionType[] newTypes) {
        for (ActionType type : newTypes) {
            checkNotNull(type);

            if (!types.contains(type)) {
                LOG.debug("Registering action type: {}", type);
                types.add(type);
                typeGroups.add(type.getType().getGroup(), type);
            }
        }
    }

    @Override
    public void registerComponents(final ActionComponent[] comps) {
        for (ActionComponent comp : comps) {
            checkNotNull(comp);

            LOG.debug("Registering action component: {}", comp);
            components.add(comp);
        }
    }

    @Override
    public void registerComparisons(final ActionComparison[] comps) {
        for (ActionComparison comp : comps) {
            checkNotNull(comp);

            LOG.debug("Registering action comparison: {}", comp);
            comparisons.add(comp);
        }
    }

    @Override
    public Map<String, ActionGroup> getGroupsMap() {
        return Collections.unmodifiableMap(groups);
    }

    @Override
    public MapList<String, ActionType> getGroupedTypes() {
        return new MapList<>(typeGroups);
    }

    @Override
    public void loadUserActions() {
        actions.clear();

        groups.values().forEach(ActionGroup::clear);

        final File dir = new File(directory);

        if (!dir.exists()) {
            try {
                dir.mkdirs();
                dir.createNewFile();
            } catch (IOException ex) {
                eventBus.publishAsync(new UserErrorEvent(ErrorLevel.HIGH, null,
                        "I/O error when creating actions directory: " + ex.getMessage(), ""));
            }
        }

        if (dir.listFiles() == null) {
            eventBus.publishAsync(new UserErrorEvent(ErrorLevel.MEDIUM, null,
                    "Unable to load user action files", ""));
        } else {
            for (File file : dir.listFiles()) {
                if (file.isDirectory()) {
                    loadActions(file);
                }
            }
        }

        registerComponents(updateManagerProvider.get());
    }

    /**
     * Creates new ActionGroupComponents for each action group.
     *
     * @param updateManager The update manager to register components with
     */
    private void registerComponents(final UpdateManager updateManager) {
        groups.values().stream()
                .filter(group -> group.getComponent() != -1 && group.getVersion() != null)
                .forEach(group -> updateManager.addComponent(
                        new ActionGroupComponent(this, group)));
    }

    /**
     * Loads action files from a specified group directory.
     *
     * @param dir The directory to scan.
     */
    @Precondition("The specified File is not null and represents a directory")
    private void loadActions(final File dir) {
        checkNotNull(dir);
        checkArgument(dir.isDirectory());

        LOG.debug("Loading actions from directory: {}", dir.getAbsolutePath());

        if (!groups.containsKey(dir.getName())) {
            groups.put(dir.getName(), new ActionGroup(this, dir.getName()));
        }

        for (File file : dir.listFiles()) {
            factory.getAction(dir.getName(), file.getName());
        }
    }

    @Override
    public void addAction(final Action action) {
        checkNotNull(action);

        LOG.debug("Registering action: {}/{} (status: {})", action.getGroup(), action.getName(),
                action.getStatus());

        if (action.getStatus() != ActionStatus.FAILED) {
            for (ActionType trigger : action.getTriggers()) {
                LOG.trace("Action has trigger {}", trigger);
                actions.add(trigger, action);
            }
        }

        getOrCreateGroup(action.getGroup()).add(action);
    }

    @Override
    public ActionGroup getOrCreateGroup(final String name) {
        if (!groups.containsKey(name)) {
            groups.put(name, new ActionGroup(this, name));
        }

        return groups.get(name);
    }

    @Override
    public void removeAction(final Action action) {
        checkNotNull(action);

        actions.removeFromAll(action);
        getOrCreateGroup(action.getGroup()).remove(action);
    }

    @Override
    public void reregisterAction(final Action action) {
        removeAction(action);
        addAction(action);
    }

    @Override
    public boolean triggerEvent(final ActionType type,
            final StringBuffer format, final Object... arguments) {
        checkNotNull(type);
        checkNotNull(type.getType());
        checkArgument(type.getType().getArity() == arguments.length);

        LOG.trace("Calling listeners for event of type {}", type);

        return killSwitch || !triggerActions(type, format, arguments);
    }

    /**
     * Triggers actions that respond to the specified type.
     *
     * @param type      The type of the event to process
     * @param format    The format of the message that's going to be displayed for the event.
     *                  Actions may change this format.
     * @param arguments The arguments for the event
     *
     * @return True if the event should be skipped, or false if it can continue
     */
    @Precondition("The specified ActionType is not null")
    private boolean triggerActions(final ActionType type,
            final StringBuffer format, final Object... arguments) {
        checkNotNull(type);

        LOG.trace("Executing actions for event of type {}", type);

        boolean res = false;
        if (actions.containsKey(type)) {
            for (Action action : new ArrayList<>(actions.get(type))) {
                try {
                    if (action.getConcurrencyGroup() == null) {
                        res |= action.trigger(format, arguments);
                    } else {
                        synchronized (locks) {
                            if (!locks.containsKey(action.getConcurrencyGroup())) {
                                locks.put(action.getConcurrencyGroup(), new Object());
                            }
                        }

                        synchronized (locks.get(action.getConcurrencyGroup())) {
                            res |= action.trigger(format, arguments);
                        }
                    }
                } catch (LinkageError | Exception e) {
                    eventBus.publishAsync(new AppErrorEvent(ErrorLevel.MEDIUM, e,
                            "Error processing action: " + e.getMessage(), ""));
                }
            }
        }

        return res;
    }

    /**
     * Processes an event from the event bus.
     *
     * @param event The event that was raised.
     */
    @Handler
    public void processEvent(final DMDircEvent event) {
        final ActionType type = getType(getLegacyActionTypeName(event));
        if (type == null) {
            LOG.warn("Unable to locate legacy type for event {}", event.getClass().getName());
            return;
        }

        final Class<?>[] argTypes = type.getType().getArgTypes();
        final Object[] arguments = getLegacyArguments(event, argTypes);

        if (event instanceof DisplayableEvent) {
            final DisplayableEvent displayable = (DisplayableEvent) event;
            final StringBuffer buffer = new StringBuffer(displayable.getDisplayFormat());
            triggerEvent(type, buffer, arguments);
            displayable.setDisplayFormat(buffer.toString());
        } else {
            triggerEvent(type, null, arguments);
        }
    }

    /**
     * Gets the name of the legacy {@link ActionType} to which the given event corresponds.
     *
     * @param event The event to obtain the name of.
     *
     * @return The legacy action type name.
     */
    private static String getLegacyActionTypeName(final DMDircEvent event) {
        return event.getClass().getSimpleName()
                .replaceAll("Event$", "")
                .replaceAll("(.)([A-Z])", "$1_$2")
                .toUpperCase();
    }

    /**
     * Attempts to obtain a legacy arguments array from the given event. Arguments will be matched
     * based on their expected classes. Where an event provides multiple getters of the same type,
     * the one closest in index to the argument index will be used.
     *
     * @param event    The event to get legacy arguments for.
     * @param argTypes The type of arguments expected for the action type.
     *
     * @return An array of objects containing the legacy arguments.
     */
    private static Object[] getLegacyArguments(final DMDircEvent event, final Class<?>[] argTypes) {
        final Object[] arguments = new Object[argTypes.length];
        final Method[] methods = event.getClass().getMethods();

        for (int i = 0; i < argTypes.length; i++) {
            final Class<?> target = argTypes[i];
            Method best = null;
            int bestDistance = Integer.MAX_VALUE;

            for (int j = 0; j < methods.length; j++) {
                final Method method = methods[j];
                if (method.getParameterTypes().length == 0
                        && method.getName().startsWith("get")
                        && !"getDisplayFormat".equals(method.getName())
                        && method.getReturnType().equals(target)
                        && Math.abs(j - i) < bestDistance) {
                    bestDistance = Math.abs(j - i);
                    best = method;
                }
            }

            if (best == null) {
                LOG.error("Unable to find method on event {} to satisfy argument #{} of class {}",
                        event.getClass().getName(), i, target.getName());
                arguments[i] = null;
            } else {
                try {
                    arguments[i] = best.invoke(event);
                } catch (ReflectiveOperationException ex) {
                    LOG.error("Unable to invoke method {} on {} to get action argument",
                            best.getName(), event.getClass().getName(), ex);
                }
            }
        }

        return arguments;
    }

    @Override
    public ActionGroup createGroup(final String group) {
        checkNotNull(group);
        checkArgument(!group.isEmpty());
        checkArgument(!groups.containsKey(group));

        final File file = new File(directory + group);
        if (file.isDirectory() || file.mkdir()) {
            final ActionGroup actionGroup = new ActionGroup(this, group);
            groups.put(group, actionGroup);
            return actionGroup;
        } else {
            throw new IllegalArgumentException("Unable to create action group directory"
                    + "\n\nDir: " + directory + group);
        }
    }

    @Override
    public void deleteGroup(final String group) {
        checkNotNull(group);
        checkArgument(!group.isEmpty());
        checkArgument(groups.containsKey(group));

        groups.get(group).getActions().forEach(this::removeAction);

        final File dir = new File(directory + group);

        if (dir.isDirectory()) {
            for (File file : dir.listFiles()) {
                if (!file.delete()) {
                    eventBus.publishAsync(new UserErrorEvent(ErrorLevel.MEDIUM, null,
                            "Unable to remove file: " + file.getAbsolutePath(), ""));
                    return;
                }
            }
        }

        if (!dir.delete()) {
            eventBus.publishAsync(new UserErrorEvent(ErrorLevel.MEDIUM, null,
                    "Unable to remove directory: " + dir.getAbsolutePath(), ""));
            return;
        }

        groups.remove(group);
    }

    @Override
    public void changeGroupName(final String oldName, final String newName) {
        checkNotNull(oldName);
        checkArgument(!oldName.isEmpty());
        checkNotNull(newName);
        checkArgument(!newName.isEmpty());
        checkArgument(groups.containsKey(oldName));
        checkArgument(!groups.containsKey(newName));
        checkArgument(!newName.equals(oldName));

        createGroup(newName);

        for (Action action : groups.get(oldName).getActions()) {
            action.setGroup(newName);
            getOrCreateGroup(oldName).remove(action);
            getOrCreateGroup(newName).add(action);
        }

        deleteGroup(oldName);
    }

    @Override
    public ActionType getType(final String type) {
        if (type == null || type.isEmpty()) {
            return null;
        }

        for (ActionType target : types) {
            if (target.name().equals(type)) {
                return target;
            }
        }

        return null;
    }

    @Override
    public List<ActionType> findCompatibleTypes(final ActionType type) {
        checkNotNull(type);

        final List<ActionType> res = types.stream()
                .filter(target -> !target.equals(type) && target.getType().equals(type.getType()))
                .collect(Collectors.toList());

        return res;
    }

    @Override
    public List<ActionComponent> findCompatibleComponents(final Class<?> target) {
        checkNotNull(target);

        final List<ActionComponent> res =
                components.stream().filter(subject -> subject.appliesTo().equals(target))
                        .collect(Collectors.toList());

        return res;
    }

    @Override
    public List<ActionComparison> findCompatibleComparisons(final Class<?> target) {
        checkNotNull(target);

        final List<ActionComparison> res =
                comparisons.stream().filter(subject -> subject.appliesTo().equals(target))
                        .collect(Collectors.toList());

        return res;
    }

    @Override
    public ActionComponent getComponent(final String type) {
        checkNotNull(type);
        checkArgument(!type.isEmpty());

        for (ActionComponent target : components) {
            if (target.name().equals(type)) {
                return target;
            }
        }

        return null;
    }

    @Override
    public ActionComparison getComparison(final String type) {
        checkNotNull(type);
        checkArgument(!type.isEmpty());

        for (ActionComparison target : comparisons) {
            if (target.name().equals(type)) {
                return target;
            }
        }

        return null;
    }

    /**
     * Installs an action pack located at the specified path.
     *
     * @param path The full path of the action pack .zip.
     *
     * @throws IOException If the zip cannot be extracted
     */
    public void installActionPack(final Path path) throws IOException {
        FileUtils.copyRecursively(path, Paths.get(directory));
        loadUserActions();
        Files.delete(path);
    }

}
