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
import com.dmdirc.actions.CoreActionType;
import com.dmdirc.commandline.CommandLineParser;
import com.dmdirc.commandparser.CommandManager;
import com.dmdirc.config.IdentityManager;
import com.dmdirc.interfaces.CommandController.CommandDetails;
import com.dmdirc.interfaces.ui.UIController;
import com.dmdirc.logger.DMDircExceptionHandler;
import com.dmdirc.logger.ErrorLevel;
import com.dmdirc.logger.Logger;
import com.dmdirc.messages.MessageSinkManager;
import com.dmdirc.plugins.PluginManager;
import com.dmdirc.plugins.Service;
import com.dmdirc.plugins.ServiceProvider;
import com.dmdirc.ui.WarningDialog;
import com.dmdirc.ui.themes.ThemeManager;
import com.dmdirc.util.URLBuilder;

import java.awt.GraphicsEnvironment;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import javax.inject.Inject;

import dagger.ObjectGraph;

/**
 * Main class, handles initialisation.
 */
public class Main {

    /** Feedback nag delay. */
    private final int FEEDBACK_DELAY = 30 * 60 * 1000;

    /** The UI to use for the client. */
    private final Collection<UIController> CONTROLLERS = new HashSet<>();

    /** The identity manager the client will use. */
    private final IdentityManager identityManager;

    /** The server manager the client will use. */
    private final ServerManager serverManager;

    /** The action manager the client will use. */
    private final ActionManager actionManager;

    /** The command-line parser used for this instance. */
    private final CommandLineParser commandLineParser;

    /** The plugin manager the client will use. */
    private final PluginManager pluginManager;

    /** The extractor to use for core plugins. */
    private final CorePluginExtractor corePluginExtractor;

    /** The command manager to use. */
    private final CommandManager commandManager;

    /** The commands to load into the command manager. */
    private final Set<CommandDetails> commands;

    /**
     * Creates a new instance of {@link Main}.
     *
     * @param identityManager The identity manager the client will use.
     * @param serverManager The server manager the client will use.
     * @param actionManager The action manager the client will use.
     * @param commandLineParser The command-line parser used for this instance.
     * @param pluginManager The plugin manager the client will use.
     * @param commandManager The command manager the client will use.
     * @param messageSinkManager Unused for now - TODO: remove me when it's injected somewhere sensible.
     * @param themeManager Unused for now - TODO: remove me when it's injected somewhere sensible.
     * @param aliasWrapper Alias wrapper to provide to the actions manager.
     * @param corePluginExtractor Extractor to use for core plugins.
     * @param urlBuilder URL builder to use as a singleton.
     * @param commands The commands to be loaded into the command manager.
     */
    @Inject
    public Main(
            final IdentityManager identityManager,
            final ServerManager serverManager,
            final ActionManager actionManager,
            final CommandLineParser commandLineParser,
            final PluginManager pluginManager,
            final CommandManager commandManager,
            final MessageSinkManager messageSinkManager,
            final ThemeManager themeManager,
            final CorePluginExtractor corePluginExtractor,
            final URLBuilder urlBuilder,
            final Set<CommandDetails> commands) {
        this.identityManager = identityManager;
        this.serverManager = serverManager;
        this.actionManager = actionManager;
        this.commandLineParser = commandLineParser;
        this.pluginManager = pluginManager;
        this.corePluginExtractor = corePluginExtractor;
        this.commandManager = commandManager;
        this.commands = commands;
        URLBuilder.setInstance(urlBuilder);
    }

    /**
     * Entry procedure.
     *
     * @param args the command line arguments
     */
    @SuppressWarnings("PMD.AvoidCatchingThrowable")
    public static void main(final String[] args) {
        Thread.setDefaultUncaughtExceptionHandler(new DMDircExceptionHandler());

        try {
            final ObjectGraph graph = ObjectGraph.create(new ClientModule());
            final CommandLineParser parser = graph.get(CommandLineParser.class);
            parser.parse(args);
            graph.get(Main.class).init();
        } catch (Throwable ex) {
            Logger.appError(ErrorLevel.FATAL, "Exception while initialising",
                    ex);
        }
    }

    /**
     * Initialises the client.
     *
     * @param args The command line arguments
     */
    public void init() {
        for (CommandDetails command : commands) {
            commandManager.registerCommand(command.getCommand(), command.getInfo());
        }

        loadUIs(pluginManager);

        doFirstRun();

        actionManager.initialise();
        pluginManager.doAutoLoad();
        actionManager.loadUserActions();
        actionManager.triggerEvent(CoreActionType.CLIENT_OPENED, null);

        commandLineParser.processArguments(serverManager);

        GlobalWindow.init();

        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            /** {@inheritDoc} */
            @Override
            public void run() {
                actionManager.triggerEvent(CoreActionType.CLIENT_CLOSED, null);
                serverManager.disconnectAll("Unexpected shutdown");
                identityManager.saveAll();
            }
        }, "Shutdown thread"));
    }

    /**
     * Called when the UI has failed to initialise correctly. This method
     * attempts to extract any and all UI plugins bundled with the client, and
     * requests a restart. If this has already been attempted, it shows an error
     * and exits.
     */
    private void handleMissingUI() {
        // Check to see if we have already tried this
        if (identityManager.getGlobalConfiguration().hasOptionBool("debug", "uiFixAttempted")) {
            System.out.println("DMDirc is unable to load any compatible UI plugins.");
            if (!GraphicsEnvironment.isHeadless()) {
                new WarningDialog(WarningDialog.NO_COMPAT_UIS_TITLE,
                        WarningDialog.NO_RECOV_UIS).displayBlocking();
            }
            identityManager.getUserSettings().unsetOption("debug", "uiFixAttempted");
            System.exit(1);
        } else {
            // Try to extract the UIs again incase they changed between versions
            // and the user didn't update the UI plugin.
            corePluginExtractor.extractCorePlugins("ui_");

            System.out.println("DMDirc has updated the UI plugins and needs to restart.");

            if (!GraphicsEnvironment.isHeadless()) {
                new WarningDialog(WarningDialog.NO_COMPAT_UIS_TITLE,
                        WarningDialog.NO_COMPAT_UIS_BODY).displayBlocking();
            }

            // Allow the rebooted DMDirc to know that we have attempted restarting.
            identityManager.getUserSettings().setOption("debug", "uiFixAttempted", "true");
            // Tell the launcher to restart!
            System.exit(42);
        }
    }

    /**
     * Attempts to find and activate a service which provides a UI that we
     * can use.
     *
     * @param pm The plugin manager to use to load plugins
     */
    protected void loadUIs(final PluginManager pm) {
        final List<Service> uis = pm.getServicesByType("ui");

        // First try: go for our desired service type
        for (Service service : uis) {
            if (service.activate()) {
                final ServiceProvider provider = service.getActiveProvider();

                final Object export = provider.getExportedService("getController").execute();

                if (export != null) {
                    CONTROLLERS.add((UIController) export);
                }
            }
        }

        if (CONTROLLERS.isEmpty()) {
            handleMissingUI();
        } else {
            // The fix worked!
            if (identityManager.getGlobalConfiguration().hasOptionBool("debug", "uiFixAttempted")) {
                identityManager.getUserSettings().unsetOption("debug", "uiFixAttempted");
            }
        }
    }

    /**
     * Executes the first run or migration wizards as required.
     */
    private void doFirstRun() {
        if (identityManager.getGlobalConfiguration().getOptionBool("general", "firstRun")) {
            identityManager.getUserSettings().setOption("general", "firstRun", "false");
            for (UIController controller : CONTROLLERS) {
                controller.showFirstRunWizard();
            }

            new Timer().schedule(new TimerTask() {

                /** {@inheritDoc} */
                @Override
                public void run() {
                    for (UIController controller : CONTROLLERS) {
                        controller.showFeedbackNag();
                    }
                }
            }, FEEDBACK_DELAY);
        }
    }

}
