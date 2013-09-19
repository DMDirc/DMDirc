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

package com.dmdirc;

import com.dmdirc.actions.ActionManager;
import com.dmdirc.commandline.CommandLineOptionsModule;
import com.dmdirc.config.IdentityManager;
import com.dmdirc.interfaces.ActionController;
import com.dmdirc.interfaces.IdentityController;
import com.dmdirc.interfaces.LifecycleController;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * Provides dependencies for the client.
 */
@Module(injects = Main.class, includes = CommandLineOptionsModule.class)
public class ClientModule {

    /**
     * Provides an identity manager for the client.
     *
     * @return An unitialised {@link IdentityManager}.
     */
    @Provides
    @Singleton
    public IdentityManager getIdentityManager() {
        final IdentityManager identityManager = new IdentityManager();
        IdentityManager.setIdentityManager(identityManager);
        identityManager.loadVersionIdentity();
        return identityManager;
    }

    /**
     * Provides an identity controller.
     *
     * @param manager The identity manager to use as a controller.
     * @return An identity controller to use.
     */
    @Provides
    public IdentityController getIdentityController(final IdentityManager manager) {
        return manager;
    }

    /**
     * Provides a parser factory.
     *
     * @return A parser factory for use in the client.
     */
    @Provides
    public ParserFactory getParserFactory() {
        return new ParserFactory(Main.mainInstance.getPluginManager());
    }

    /**
     * Provides an action manager.
     *
     * @param serverManager The server manager to use to iterate servers.
     * @param identityController The identity controller to use to look up settings.
     * @return An unitialised action manager.
     */
    @Provides
    @Singleton
    public ActionManager getActionManager(final ServerManager serverManager, final IdentityController identityController) {
        final ActionManager actionManager = new ActionManager(serverManager, identityController);
        ActionManager.setActionManager(actionManager);
        return actionManager;
    }

    /**
     * Provides an action controller.
     *
     * @param actionManager The action manager to use as a controller.
     * @return An action controller to use.
     */
    @Provides
    public ActionController getActionController(final ActionManager actionManager) {
        return actionManager;
    }

    /**
     * Provides a lifecycle controller.
     *
     * @param controller The concrete implementation to use.
     * @return The lifecycle controller the app should use.
     */
    @Provides
    public LifecycleController getLifecycleController(final SystemLifecycleController controller) {
        return controller;
    }

}
