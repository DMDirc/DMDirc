/*
 * Copyright (c) 2006-2014 DMDirc Developers
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

package com.dmdirc.plugins;

import com.dmdirc.ClientModule.GlobalConfig;
import com.dmdirc.CorePluginExtractor;
import com.dmdirc.ServerManager;
import com.dmdirc.actions.ActionFactory;
import com.dmdirc.actions.ActionManager;
import com.dmdirc.actions.ActionSubstitutorFactory;
import com.dmdirc.actions.wrappers.AliasWrapper;
import com.dmdirc.actions.wrappers.PerformWrapper;
import com.dmdirc.commandparser.CommandManager;
import com.dmdirc.config.prefs.PreferencesManager;
import com.dmdirc.interfaces.LifecycleController;
import com.dmdirc.interfaces.config.IdentityController;
import com.dmdirc.messages.MessageSinkManager;
import com.dmdirc.ui.IconManager;
import com.dmdirc.ui.WindowManager;
import com.dmdirc.ui.core.components.StatusBarManager;
import com.dmdirc.ui.messages.ColourManager;
import com.dmdirc.ui.themes.ThemeManager;
import com.dmdirc.util.SimpleInjector;
import com.dmdirc.util.URLBuilder;

import com.google.common.eventbus.EventBus;

import javax.inject.Inject;

/**
 * Utility class that can initialise a {@link SimpleInjector} for use by plugins.
 *
 * Eventually this should be replaced by using the same DI framework for plugins as for the client.
 */
public class PluginInjectorInitialiser {

    private final ActionManager actionManager;
    private final ActionFactory actionFactory;
    private final AliasWrapper aliasWrapper;
    private final PluginManager pluginManager;
    private final IdentityController identityController;
    private final ServerManager serverManager;
    private final ThemeManager themeManager;
    private final CommandManager commandManager;
    private final MessageSinkManager messageSinkManager;
    private final WindowManager windowManager;
    private final StatusBarManager statusBarManager;
    private final PreferencesManager preferencesManager;
    private final PerformWrapper performWrapper;
    private final LifecycleController lifecycleController;
    private final CorePluginExtractor corePluginExtractor;
    private final URLBuilder urlBuilder;
    private final ColourManager colourManager;
    private final ActionSubstitutorFactory actionSubstitutorFactory;
    private final EventBus eventBus;
    private final IconManager iconManager;

    @Inject
    public PluginInjectorInitialiser(final ActionManager actionManager,
            final ActionFactory actionFactory,
            final AliasWrapper aliasWrapper,
            final PluginManager pluginManager,
            final IdentityController identityController,
            final ServerManager serverManager,
            final ThemeManager themeManager,
            final CommandManager commandManager,
            final MessageSinkManager messageSinkManager,
            final WindowManager windowManager,
            final StatusBarManager statusBarManager,
            final PreferencesManager preferencesManager,
            final PerformWrapper performWrapper,
            final LifecycleController lifecycleController,
            final CorePluginExtractor corePluginExtractor,
            final URLBuilder urlBuilder,
            final ColourManager colourManager,
            final ActionSubstitutorFactory actionSubstitutorFactory,
            final EventBus eventBus,
            @GlobalConfig final IconManager iconManager) {
        this.actionManager = actionManager;
        this.actionFactory = actionFactory;
        this.aliasWrapper = aliasWrapper;
        this.pluginManager = pluginManager;
        this.identityController = identityController;
        this.serverManager = serverManager;
        this.themeManager = themeManager;
        this.commandManager = commandManager;
        this.messageSinkManager = messageSinkManager;
        this.windowManager = windowManager;
        this.statusBarManager = statusBarManager;
        this.preferencesManager = preferencesManager;
        this.performWrapper = performWrapper;
        this.lifecycleController = lifecycleController;
        this.corePluginExtractor = corePluginExtractor;
        this.urlBuilder = urlBuilder;
        this.colourManager = colourManager;
        this.actionSubstitutorFactory = actionSubstitutorFactory;
        this.eventBus = eventBus;
        this.iconManager = iconManager;
    }

    /**
     * Initialises the given injector with all of the known "global" managers.
     *
     * @param injector The injector to be initialised
     */
    public void initialise(final SimpleInjector injector) {
        injector.addParameter(actionManager);
        injector.addParameter(PluginManager.class, pluginManager);
        injector.addParameter(identityController);
        injector.addParameter(ServerManager.class, serverManager);
        injector.addParameter(commandManager);
        injector.addParameter(MessageSinkManager.class, messageSinkManager);
        injector.addParameter(WindowManager.class, windowManager);
        injector.addParameter(statusBarManager);
        injector.addParameter(PreferencesManager.class, preferencesManager);
        injector.addParameter(PerformWrapper.class, performWrapper);
        injector.addParameter(AliasWrapper.class, aliasWrapper);
        injector.addParameter(LifecycleController.class, lifecycleController);
        injector.addParameter(corePluginExtractor);
        injector.addParameter(actionFactory);
        injector.addParameter(themeManager);
        injector.addParameter(urlBuilder);
        injector.addParameter(colourManager);
        injector.addParameter(actionSubstitutorFactory);
        injector.addParameter(eventBus);
        injector.addParameter(iconManager);
    }

}
