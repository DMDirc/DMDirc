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

package com.dmdirc.updater;

import com.dmdirc.commandline.CommandLineParser;
import com.dmdirc.interfaces.EventBus;
import com.dmdirc.interfaces.config.IdentityController;
import com.dmdirc.updater.checking.CheckResultConsolidator;
import com.dmdirc.updater.checking.DMDircCheckStrategy;
import com.dmdirc.updater.checking.NaiveConsolidator;
import com.dmdirc.updater.checking.NightlyChecker;
import com.dmdirc.updater.checking.UpdateCheckStrategy;
import com.dmdirc.updater.components.ClientComponent;
import com.dmdirc.updater.components.DefaultsComponent;
import com.dmdirc.updater.components.LauncherComponent;
import com.dmdirc.updater.components.ModeAliasesComponent;
import com.dmdirc.updater.installing.LegacyInstallationStrategy;
import com.dmdirc.updater.installing.UpdateInstallationStrategy;
import com.dmdirc.updater.manager.CachingUpdateManager;
import com.dmdirc.updater.manager.ConfigComponentPolicy;
import com.dmdirc.updater.manager.DMDircUpdateManager;
import com.dmdirc.updater.manager.UpdateComponentPolicy;
import com.dmdirc.updater.manager.UpdateManager;
import com.dmdirc.updater.retrieving.DownloadRetrievalStrategy;
import com.dmdirc.updater.retrieving.UpdateRetrievalStrategy;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * Provides injections for the updater system.
 */
@Module(library = true, complete = false)
public class UpdaterModule {

    /**
     * Provides a client component for the updater component set.
     *
     * @param component The client component to provide.
     *
     * @return The component entry in the set.
     */
    @Provides(type = Provides.Type.SET)
    public UpdateComponent getClientComponent(final ClientComponent component) {
        return component;
    }

    /**
     * Provides a mode aliases component for the updater component set.
     *
     * @param component The mode aliases component to provide.
     *
     * @return The component entry in the set.
     */
    @Provides(type = Provides.Type.SET)
    public UpdateComponent getModeAliasesComponent(final ModeAliasesComponent component) {
        return component;
    }

    /**
     * Provides a defaults component for the updater component set.
     *
     * @param component The defaults component to provide.
     *
     * @return The component entry in the set.
     */
    @Provides(type = Provides.Type.SET)
    public UpdateComponent getDefaultsComponent(final DefaultsComponent component) {
        return component;
    }

    /**
     * Gets an update manager for the client.
     *
     * @param commandLineParser  CLI parser to use to find launcher version.
     * @param updateManager      The underlying update manager.
     * @param identityController The controller to use to read and update settings.
     * @param eventBus           The event bus to post errors to.
     *
     * @return The update manager to use.
     */
    @Provides
    @Singleton
    public UpdateManager getUpdateManager(
            final CommandLineParser commandLineParser,
            final DMDircUpdateManager updateManager,
            final IdentityController identityController,
            final EventBus eventBus) {
        UpdateChecker.init(updateManager, identityController);

        commandLineParser.getLauncherVersion().ifPresent(version ->
                LauncherComponent.setLauncherInfo(updateManager, version));

        return updateManager;
    }

    /**
     * Gets a caching update manager for the client.
     *
     * @param updateManager The underlying update manager.
     *
     * @return The update manager to use.
     */
    @Provides
    @Singleton
    public CachingUpdateManager getCachingUpdateManager(final DMDircUpdateManager updateManager) {
        return updateManager;
    }

    /**
     * Provides a {@link CheckResultConsolidator} that the client should use.
     *
     * @param consolidator The consolidator to provide.
     *
     * @return The consolidator to use in the client.
     */
    @Provides
    public CheckResultConsolidator getConsolidator(final NaiveConsolidator consolidator) {
        return consolidator;
    }

    /**
     * Provides an {@link UpdateComponentPolicy} that the client should use.
     *
     * @param policy The policy to provide.
     *
     * @return The policy to use in the client.
     */
    @Provides
    public UpdateComponentPolicy getUpdatePolicy(final ConfigComponentPolicy policy) {
        return policy;
    }

    /**
     * Provides an {@link UpdateRetrievalStrategy} that the client should use.
     *
     * @param strategy The strategy to provide.
     *
     * @return The strategy to use in the client.
     */
    @Provides(type = Provides.Type.SET)
    public UpdateRetrievalStrategy getRetrievalStrategy(final DownloadRetrievalStrategy strategy) {
        return strategy;
    }

    /**
     * Provides an {@link UpdateInstallationStrategy} that the client should use.
     *
     * @param strategy The strategy to provide.
     *
     * @return The strategy to use in the client.
     */
    @Provides(type = Provides.Type.SET)
    public UpdateInstallationStrategy getInstallStrategy(final LegacyInstallationStrategy strategy) {
        return strategy;
    }

    /**
     * Provides an {@link UpdateCheckStrategy} that the client should use.
     *
     * @param strategy The strategy to provide.
     *
     * @return The strategy to use in the client.
     */
    @Provides(type = Provides.Type.SET)
    public UpdateCheckStrategy getCheckStrategy(final DMDircCheckStrategy strategy) {
        return strategy;
    }

    /**
     * Provides an {@link UpdateCheckStrategy} that the client should use for nightlies.
     *
     * @param strategy The strategy to provide.
     *
     * @return The strategy to use in the client.
     */
    @Provides(type = Provides.Type.SET)
    public UpdateCheckStrategy getCheckStrategy(final NightlyChecker strategy) {
        return strategy;
    }

}
