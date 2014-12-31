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
import com.dmdirc.commandline.CommandLineOptionsModule.Directory;
import com.dmdirc.commandline.CommandLineOptionsModule.DirectoryType;
import com.dmdirc.interfaces.ActionController;
import com.dmdirc.interfaces.actions.ActionType;
import com.dmdirc.interfaces.config.IdentityController;

import java.nio.file.Path;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

/**
 * Factory for creating {@link Action}s.
 */
@Singleton
public class ActionFactory {

    /** The controller that will own actions. */
    private final Provider<ActionController> actionController;
    /** The controller to use to retrieve and update settings. */
    private final Provider<IdentityController> identityController;
    /** The global window to use for actions without windows. */
    private final Provider<GlobalWindow> globalWindowProvider;
    /** The factory to use to create substitutors. */
    private final ActionSubstitutorFactory substitutorFactory;
    /** The base directory to store actions in. */
    private final Path actionsDirectory;
    /** Event bus to post events on. */
    private final DMDircMBassador eventBus;

    /**
     * Creates a new instance of {@link ActionFactory}.
     *
     * @param eventBus                    The event bus to post events on.
     * @param actionController            The controller that will own actions.
     * @param identityController          The controller to use to retrieve and update settings.
     * @param globalWindowProvider        The global window to use for actions without windows.
     * @param substitutorFactory          The factory to use to create substitutors.
     * @param actionsDirectory            The base directory to store actions in.
     */
    @Inject
    public ActionFactory(final DMDircMBassador eventBus, final Provider<ActionController> actionController,
            final Provider<IdentityController> identityController,
            final Provider<GlobalWindow> globalWindowProvider,
            final ActionSubstitutorFactory substitutorFactory,
            @Directory(DirectoryType.ACTIONS) final Path actionsDirectory) {
        this.eventBus = eventBus;
        this.actionController = actionController;
        this.identityController = identityController;
        this.globalWindowProvider = globalWindowProvider;
        this.substitutorFactory = substitutorFactory;
        this.actionsDirectory = actionsDirectory;
    }

    /**
     * Creates a new instance of Action. The group and name specified must be the group and name of
     * a valid action already saved to disk.
     *
     * @param group The group the action belongs to
     * @param name  The name of the action
     *
     * @return A relevant action.
     */
    public Action getAction(final String group, final String name) {
        return new Action(
                eventBus, globalWindowProvider,
                substitutorFactory,
                actionController.get(),
                identityController.get(),
                actionsDirectory,
                group,
                name);
    }

    /**
     * Creates a new instance of Action with the specified properties and saves it to disk.
     *
     * @param group         The group the action belongs to
     * @param name          The name of the action
     * @param triggers      The triggers to use
     * @param response      The response to use
     * @param conditions    The conditions to use
     * @param conditionTree The condition tree to use
     * @param newFormat     The new formatter to use
     *
     * @return A relevant action.
     */
    public Action getAction(final String group, final String name,
            final ActionType[] triggers, final String[] response,
            final List<ActionCondition> conditions,
            final ConditionTree conditionTree, final String newFormat) {
        return new Action(
                eventBus, globalWindowProvider,
                substitutorFactory,
                actionController.get(),
                identityController.get(),
                actionsDirectory,
                group,
                name,
                triggers,
                response,
                conditions,
                conditionTree,
                newFormat);
    }

}
