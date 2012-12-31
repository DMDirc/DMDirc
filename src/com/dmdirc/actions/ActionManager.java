/*
 * Copyright (c) 2006-2013 DMDirc Developers
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

import com.dmdirc.Main;
import com.dmdirc.Precondition;
import com.dmdirc.ServerManager;
import com.dmdirc.actions.internal.WhoisNumericFormatter;
import com.dmdirc.actions.wrappers.AliasWrapper;
import com.dmdirc.actions.wrappers.PerformWrapper;
import com.dmdirc.config.ConfigBinding;
import com.dmdirc.interfaces.ActionController;
import com.dmdirc.interfaces.ActionListener;
import com.dmdirc.interfaces.IdentityController;
import com.dmdirc.interfaces.actions.ActionComparison;
import com.dmdirc.interfaces.actions.ActionComponent;
import com.dmdirc.interfaces.actions.ActionType;
import com.dmdirc.logger.ErrorLevel;
import com.dmdirc.logger.Logger;
import com.dmdirc.updater.UpdateChecker;
import com.dmdirc.updater.components.ActionGroupComponent;
import com.dmdirc.updater.manager.UpdateManager;
import com.dmdirc.util.collections.MapList;
import com.dmdirc.util.resourcemanager.ZipResourceManager;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;

/**
 * Manages all actions for the client.
 */
@Slf4j
public class ActionManager implements ActionController {

    /** The identity manager to load configuration from. */
    private final IdentityController identityManager;

    /** The ActionManager Instance. */
    private static ActionManager me;

    /** The instance of main that owns this Action Manager. */
    private final Main main;

    /** The ServerManager currently in use. */
    private final ServerManager serverManager;

    /** A list of registered action types. */
    private final List<ActionType> types
            = new ArrayList<ActionType>();

    /** A list of registered action components. */
    private final List<ActionComponent> components
            = new ArrayList<ActionComponent>();

    /** A list of registered action comparisons. */
    private final List<ActionComparison> comparisons
            = new ArrayList<ActionComparison>();

    /** A map linking types and a list of actions that're registered for them. */
    private final MapList<ActionType, Action> actions
            = new MapList<ActionType, Action>();

    /** A map linking groups and a list of actions that're in them. */
    private final Map<String, ActionGroup> groups
            = new HashMap<String, ActionGroup>();

    /** A map of objects to synchronise on for concurrency groups. */
    private final Map<String, Object> locks
            = new HashMap<String, Object>();

    /** A map of the action type groups to the action types within. */
    private final MapList<String, ActionType> typeGroups
            = new MapList<String, ActionType>();

    /** The listeners that we have registered. */
    private final MapList<ActionType, ActionListener> listeners
            = new MapList<ActionType, ActionListener>();

    /** Indicates whether or not user actions should be killed (not processed). */
    @ConfigBinding(domain="actions", key="killswitch")
    private boolean killSwitch;

    /**
     * Creates a new instance of ActionManager.
     *
     * @param main Main that created this action manager.
     * @param serverManager The ServerManager in use.
     * @param identityManager The IdentityManager to load configuration from.
     */
    private ActionManager(final Main main, final ServerManager serverManager, final IdentityController identityManager) {
        // TODO: We shouldn't need a main.
        this.main = main;
        this.serverManager = serverManager;
        this.identityManager = identityManager;
        this.identityManager.getGlobalConfiguration().getBinder().bind(this, ActionManager.class);
    }

    /**
     * Get the instance of Main that owns this.
     *
     * @return Instance of main.
     * @Deprecated Global state is bad.
     */
    @Deprecated
    public Main getMain() {
        return main;
    }

    /**
     * Get the server manager.
     *
     * @return ServerManager
     */
    public ServerManager getServerManager() {
        return serverManager;
    }

    /**
     * Create the singleton instance of the Action Manager.
     *
     * @return The singleton ActionManager instance
     */
    @Deprecated
    public static ActionManager initActionManager(final Main main, final ServerManager serverManager, final IdentityController identityManager) {
        me = new ActionManager(main, serverManager, identityManager);
        return me;
    }

    /**
     * Returns a singleton instance of the Action Manager.
     *
     * @return A singleton ActionManager instance
     */
    public static ActionManager getActionManager() {
        return me;
    }

    /** {@inheritDoc} */
    @Override
    public void initialise() {
        log.info("Initialising the actions manager");

        registerTypes(CoreActionType.values());
        registerComparisons(CoreActionComparison.values());
        registerComponents(CoreActionComponent.values());

        addGroup(AliasWrapper.getAliasWrapper());
        addGroup(PerformWrapper.getPerformWrapper());

        new WhoisNumericFormatter(identityManager.getGlobalAddonIdentity()).register();

        // Register a listener for the closing event, so we can save actions
        registerListener(new ActionListener() {
            /** {@inheritDoc} */
            @Override
            public void processEvent(final ActionType type, final StringBuffer format,
                    final Object... arguments) {
                saveAllActions();
            }
        }, CoreActionType.CLIENT_CLOSED);
    }

    /** {@inheritDoc} */
    @Override
    public void saveAllActions() {
        for (ActionGroup group : groups.values()) {
            for (Action action : group) {
                action.save();
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public void registerSetting(final String name, final String value) {
        log.debug("Registering new action setting: {} = {}", name, value);
        identityManager.getGlobalAddonIdentity().setOption("actions", name, value);
    }

    /** {@inheritDoc} */
    @Override
    public void addGroup(final ActionGroup group) {
        groups.put(group.getName(), group);
    }

    /** {@inheritDoc} */
    @Override
    public void registerTypes(final ActionType[] newTypes) {
        for (ActionType type : newTypes) {
            Logger.assertTrue(type != null);

            if (!types.contains(type)) {
                log.debug("Registering action type: {}", type);
                types.add(type);
                typeGroups.add(type.getType().getGroup(), type);
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public void registerComponents(final ActionComponent[] comps) {
        for (ActionComponent comp : comps) {
            Logger.assertTrue(comp != null);

            log.debug("Registering action component: {}", comp);
            components.add(comp);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void registerComparisons(final ActionComparison[] comps) {
        for (ActionComparison comp : comps) {
            Logger.assertTrue(comp != null);

            log.debug("Registering action comparison: {}", comp);
            comparisons.add(comp);
        }
    }

    /** {@inheritDoc} */
    @Override
    public Map<String, ActionGroup> getGroupsMap() {
        return Collections.unmodifiableMap(groups);
    }

    /** {@inheritDoc} */
    @Override
    public MapList<String, ActionType> getGroupedTypes() {
        return new MapList<String, ActionType>(typeGroups);
    }

    /** {@inheritDoc} */
    @Override
    public void loadUserActions() {
        actions.clear();

        for (ActionGroup group : groups.values()) {
            group.clear();
        }

        final File dir = new File(getDirectory());

        if (!dir.exists()) {
            try {
                dir.mkdirs();
                dir.createNewFile();
            } catch (IOException ex) {
                Logger.userError(ErrorLevel.HIGH, "I/O error when creating actions directory: "
                        + ex.getMessage());
            }
        }

        if (dir.listFiles() == null) {
            Logger.userError(ErrorLevel.MEDIUM, "Unable to load user action files");
        } else {
            for (File file : dir.listFiles()) {
                if (file.isDirectory()) {
                    loadActions(file);
                }
            }
        }

        registerComponents(UpdateChecker.getManager());
    }

    /**
     * Creates new ActionGroupComponents for each action group.
     *
     * @param updateManager The update manager to register components with
     */
    private void registerComponents(final UpdateManager updateManager) {
        for (ActionGroup group : groups.values()) {
            if (group.getComponent() != -1 && group.getVersion() != null) {
                updateManager.addComponent(new ActionGroupComponent(group));
            }
        }
    }

    /**
     * Loads action files from a specified group directory.
     *
     * @param dir The directory to scan.
     */
    @Precondition("The specified File is not null and represents a directory")
    private void loadActions(final File dir) {
        Logger.assertTrue(dir != null);
        Logger.assertTrue(dir.isDirectory());

        log.debug("Loading actions from directory: {}", dir.getAbsolutePath());

        if (!groups.containsKey(dir.getName())) {
            groups.put(dir.getName(), new ActionGroup(dir.getName()));
        }

        for (File file : dir.listFiles()) {
            new Action(dir.getName(), file.getName());
        }
    }

    /** {@inheritDoc} */
    @Override
    public void addAction(final Action action) {
        Logger.assertTrue(action != null);

        log.debug("Registering action: {}/{} (status: {})",
                new Object[] { action.getGroup(), action.getName(), action.getStatus() });

        if (action.getStatus() != ActionStatus.FAILED) {
            for (ActionType trigger : action.getTriggers()) {
                log.trace("Action has trigger {}", trigger);
                actions.add(trigger, action);
            }
        }

        getOrCreateGroup(action.getGroup()).add(action);
    }

    /** {@inheritDoc} */
    @Override
    public ActionGroup getOrCreateGroup(final String name) {
        if (!groups.containsKey(name)) {
            groups.put(name, new ActionGroup(name));
        }

        return groups.get(name);
    }

    /** {@inheritDoc} */
    @Override
    public void removeAction(final Action action) {
        Logger.assertTrue(action != null);

        actions.removeFromAll(action);
        getOrCreateGroup(action.getGroup()).remove(action);
    }

    /** {@inheritDoc} */
    @Override
    public void reregisterAction(final Action action) {
        removeAction(action);
        addAction(action);
    }

    /** {@inheritDoc} */
    @Override
    public boolean triggerEvent(final ActionType type,
            final StringBuffer format, final Object ... arguments) {
        Logger.assertTrue(type != null);
        Logger.assertTrue(type.getType() != null);
        Logger.assertTrue(type.getType().getArity() == arguments.length);

        log.trace("Calling listeners for event of type {}", type);

        boolean res = false;

        if (listeners.containsKey(type)) {
            for (ActionListener listener
                    : new ArrayList<ActionListener>(listeners.get(type))) {
                try {
                    listener.processEvent(type, format, arguments);
                } catch (Exception e) {
                    Logger.appError(ErrorLevel.MEDIUM, "Error processing action: "
                            + e.getMessage(), e);
                }
            }
        }

        if (!killSwitch) {
            res |= triggerActions(type, format, arguments);
        }

        return !res;
    }

    /**
     * Triggers actions that respond to the specified type.
     *
     * @param type The type of the event to process
     * @param format The format of the message that's going to be displayed for
     * the event. Actions may change this format.
     * @param arguments The arguments for the event
     * @return True if the event should be skipped, or false if it can continue
     */
    @Precondition("The specified ActionType is not null")
    private boolean triggerActions(final ActionType type,
            final StringBuffer format, final Object ... arguments) {
        Logger.assertTrue(type != null);

        boolean res = false;

        log.trace("Executing actions for event of type {}", type);

        if (actions.containsKey(type)) {
            for (Action action : new ArrayList<Action>(actions.get(type))) {
                try {
                    if (action.getConcurrencyGroup() == null) {
                        res |= action.trigger(getServerManager(), format, arguments);
                    } else {
                        synchronized (locks) {
                            if (!locks.containsKey(action.getConcurrencyGroup())) {
                                locks.put(action.getConcurrencyGroup(), new Object());
                            }
                        }

                        synchronized (locks.get(action.getConcurrencyGroup())) {
                            res |= action.trigger(getServerManager(), format, arguments);
                        }
                    }
                } catch (LinkageError e) {
                    Logger.appError(ErrorLevel.MEDIUM, "Error processing action: "
                            + e.getMessage(), e);
                } catch (Exception e) {
                    Logger.appError(ErrorLevel.MEDIUM, "Error processing action: "
                            + e.getMessage(), e);
                }
            }
        }

        return res;
    }

    /**
     * Returns the directory that should be used to store actions.
     *
     * @return The directory that should be used to store actions
     */
    public String getDirectory() {
        return this.identityManager.getConfigDir() + "actions" + System.getProperty("file.separator");
    }

    /** {@inheritDoc} */
    @Override
    public ActionGroup createGroup(final String group) {
        Logger.assertTrue(group != null);
        Logger.assertTrue(!group.isEmpty());
        Logger.assertTrue(!groups.containsKey(group));

        final File file = new File(getDirectory() + group);
        if (file.isDirectory() || file.mkdir()) {
            final ActionGroup actionGroup = new ActionGroup(group);
            groups.put(group, actionGroup);
            return actionGroup;
        } else {
            throw new IllegalArgumentException("Unable to create action group directory"
                    + "\n\nDir: " + getDirectory() + group);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void deleteGroup(final String group) {
        Logger.assertTrue(group != null);
        Logger.assertTrue(!group.isEmpty());
        Logger.assertTrue(groups.containsKey(group));

        for (Action action : groups.get(group).getActions()) {
            removeAction(action);
        }

        final File dir = new File(getDirectory() + group);

        if (dir.isDirectory()) {
            for (File file : dir.listFiles()) {
                if (!file.delete()) {
                    Logger.userError(ErrorLevel.MEDIUM, "Unable to remove file: "
                            + file.getAbsolutePath());
                    return;
                }
            }
        }

        if (!dir.delete()) {
            Logger.userError(ErrorLevel.MEDIUM, "Unable to remove directory: "
                    + dir.getAbsolutePath());
            return;
        }

        groups.remove(group);
    }

    /** {@inheritDoc} */
    @Override
    public void changeGroupName(final String oldName, final String newName) {
        Logger.assertTrue(oldName != null);
        Logger.assertTrue(!oldName.isEmpty());
        Logger.assertTrue(newName != null);
        Logger.assertTrue(!newName.isEmpty());
        Logger.assertTrue(groups.containsKey(oldName));
        Logger.assertTrue(!groups.containsKey(newName));
        Logger.assertTrue(!newName.equals(oldName));

        createGroup(newName);

        for (Action action : groups.get(oldName).getActions()) {
            action.setGroup(newName);
            getOrCreateGroup(oldName).remove(action);
            getOrCreateGroup(newName).add(action);
        }

        deleteGroup(oldName);
    }

    /** {@inheritDoc} */
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

    /** {@inheritDoc} */
    @Override
    public List<ActionType> findCompatibleTypes(final ActionType type) {
        Logger.assertTrue(type != null);

        final List<ActionType> res = new ArrayList<ActionType>();
        for (ActionType target : types) {
            if (!target.equals(type) && target.getType().equals(type.getType())) {
                res.add(target);
            }
        }

        return res;
    }

    /** {@inheritDoc} */
    @Override
    public List<ActionComponent> findCompatibleComponents(final Class<?> target) {
        Logger.assertTrue(target != null);

        final List<ActionComponent> res = new ArrayList<ActionComponent>();
        for (ActionComponent subject : components) {
            if (subject.appliesTo().equals(target)) {
                res.add(subject);
            }
        }

        return res;
    }

    /** {@inheritDoc} */
    @Override
    public List<ActionComparison> findCompatibleComparisons(final Class<?> target) {
        Logger.assertTrue(target != null);

        final List<ActionComparison> res = new ArrayList<ActionComparison>();
        for (ActionComparison subject : comparisons) {
            if (subject.appliesTo().equals(target)) {
                res.add(subject);
            }
        }

        return res;
    }

    /** {@inheritDoc} */
    @Override
    public List<ActionType> getAllTypes() {
        return Collections.unmodifiableList(types);
    }

    /** {@inheritDoc} */
    @Override
    public List<ActionComparison> getAllComparisons() {
        return Collections.unmodifiableList(comparisons);
    }

    /** {@inheritDoc} */
    @Override
    public ActionComponent getComponent(final String type) {
        Logger.assertTrue(type != null);
        Logger.assertTrue(!type.isEmpty());

        for (ActionComponent target : components) {
            if (target.name().equals(type)) {
                return target;
            }
        }

        return null;
    }

    /** {@inheritDoc} */
    @Override
    public ActionComparison getComparison(final String type) {
        Logger.assertTrue(type != null);
        Logger.assertTrue(!type.isEmpty());

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
     * @throws IOException If the zip cannot be extracted
     */
    public static void installActionPack(final String path) throws IOException {
        final ZipResourceManager ziprm = ZipResourceManager.getInstance(path);

        ziprm.extractResources("", getActionManager().getDirectory());

        getActionManager().loadUserActions();

        new File(path).delete();
    }

    /** {@inheritDoc} */
    @Override
    public void registerListener(final ActionListener listener, final ActionType ... types) {
        for (ActionType type : types) {
            listeners.add(type, listener);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void unregisterListener(final ActionListener listener, final ActionType ... types) {
        for (ActionType type : types) {
            listeners.remove(type, listener);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void unregisterListener(final ActionListener listener) {
        listeners.removeFromAll(listener);
    }
}
