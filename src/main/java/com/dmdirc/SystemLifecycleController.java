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

package com.dmdirc;

import com.dmdirc.ClientModule.GlobalConfig;
import com.dmdirc.events.ClientClosedEvent;
import com.dmdirc.interfaces.ConnectionManager;
import com.dmdirc.interfaces.EventBus;
import com.dmdirc.interfaces.LifecycleController;
import com.dmdirc.interfaces.SystemLifecycleComponent;
import com.dmdirc.interfaces.config.AggregateConfigProvider;
import com.dmdirc.interfaces.config.IdentityController;

import java.util.HashSet;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Simple {@link LifecycleController} implementation that calls {@link System#exit(int)}.
 */
@Singleton
public class SystemLifecycleController implements LifecycleController {

    /** Controller to retrieve settings from. */
    private final AggregateConfigProvider configProvider;
    /** Components to shut down when the client quits. */
    private final Set<SystemLifecycleComponent> lifecycleComponents;
    /** Manager to use to disconnect servers. */
    private final ConnectionManager connectionManager;
    /** The event bus to raise client closed events on. */
    private final EventBus eventBus;
    /** The identity controller to save when quitting. */
    private final IdentityController identityController;

    @Inject
    public SystemLifecycleController(
            @GlobalConfig final AggregateConfigProvider configProvider,
            final Set<SystemLifecycleComponent> lifecycleComponents,
            final ConnectionManager connectionManager,
            final EventBus eventBus,
            final IdentityController identityController) {
        this.configProvider = configProvider;
        this.lifecycleComponents = new HashSet<>(lifecycleComponents);
        this.connectionManager = connectionManager;
        this.eventBus = eventBus;
        this.identityController = identityController;
    }

    @Override
    public void quit() {
        quit(0);
    }

    @Override
    public void quit(final int exitCode) {
        quit(configProvider.getOption("general", "closemessage"), exitCode);
    }

    @Override
    public void quit(final String reason) {
        quit(reason, 0);
    }

    @Override
    public void quit(final String reason, final int exitCode) {
        lifecycleComponents.forEach(SystemLifecycleComponent::shutDown);

        // TODO: Make all of these into lifecycle components
        eventBus.publish(new ClientClosedEvent());
        identityController.saveAll();
        connectionManager.disconnectAll(reason);

        System.exit(exitCode);
    }

}
