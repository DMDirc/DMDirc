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

import com.dmdirc.GlobalWindow.GlobalWindowManager;
import com.dmdirc.commandline.CommandLineParser;
import com.dmdirc.commandparser.CommandManager;
import com.dmdirc.events.ClientOpenedEvent;
import com.dmdirc.events.FeedbackNagEvent;
import com.dmdirc.events.FirstRunEvent;
import com.dmdirc.interfaces.CommandController.CommandDetails;
import com.dmdirc.interfaces.ConnectionManager;
import com.dmdirc.interfaces.Migrator;
import com.dmdirc.interfaces.SystemLifecycleComponent;
import com.dmdirc.interfaces.config.IdentityController;
import com.dmdirc.interfaces.ui.UIController;
import com.dmdirc.logger.DMDircExceptionHandler;
import com.dmdirc.logger.ModeAliasReporter;
import com.dmdirc.logger.ProgramErrorAppender;
import com.dmdirc.plugins.CorePluginExtractor;
import com.dmdirc.plugins.PluginManager;
import com.dmdirc.plugins.Service;
import com.dmdirc.plugins.ServiceManager;
import com.dmdirc.plugins.ServiceProvider;
import com.dmdirc.ui.WarningDialog;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.spi.ContextAware;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

import java.awt.GraphicsEnvironment;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dagger.ObjectGraph;

/**
 * Main class, handles initialisation.
 */
public class Main {

    private final Logger log = LoggerFactory.getLogger(Main.class);
    /** The UI to use for the client. */
    private final Collection<UIController> CONTROLLERS = new HashSet<>();
    /** The identity manager the client will use. */
    private final IdentityController identityManager;
    /** The server manager the client will use. */
    private final ConnectionManager connectionManager;
    /** The command-line parser used for this instance. */
    private final CommandLineParser commandLineParser;
    /** The plugin manager the client will use. */
    private final PluginManager pluginManager;
    /** The extractor to use for core plugins. */
    private final CorePluginExtractor corePluginExtractor;
    /** The command manager to use. */
    private final CommandManager commandManager;
    /** The global window manager to use. */
    private final GlobalWindowManager globalWindowManager;
    /** The set of known lifecycle components. */
    private final Set<SystemLifecycleComponent> lifecycleComponents;
    /** The set of migrators to execute on startup. */
    private final Set<Migrator> migrators;
    /** The event bus to dispatch events on. */
    private final DMDircMBassador eventBus;
    /** The commands to load into the command manager. */
    private final Set<CommandDetails> commands;
    /** Mode alias reporter to use. */
    private final ModeAliasReporter reporter;
    private final ServiceManager serviceManager;

    static {
        // TODO: Can this go in a Dagger module?
        final LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        final ContextAware configurator = new JoranConfigurator();
        configurator.setContext(context);
        context.reset();
    }

    /**
     * Creates a new instance of {@link Main}.
     */
    @Inject
    public Main(
            final IdentityController identityManager,
            final ConnectionManager connectionManager,
            final CommandLineParser commandLineParser,
            final PluginManager pluginManager,
            final CommandManager commandManager,
            final CorePluginExtractor corePluginExtractor,
            final GlobalWindowManager globalWindowManager,
            final Set<SystemLifecycleComponent> lifecycleComponents,
            final Set<Migrator> migrators,
            final DMDircMBassador eventBus,
            final Set<CommandDetails> commands,
            final ModeAliasReporter reporter,
            final ServiceManager serviceManager) {
        this.identityManager = identityManager;
        this.connectionManager = connectionManager;
        this.commandLineParser = commandLineParser;
        this.pluginManager = pluginManager;
        this.corePluginExtractor = corePluginExtractor;
        this.commandManager = commandManager;
        this.globalWindowManager = globalWindowManager;
        this.lifecycleComponents = lifecycleComponents;
        this.migrators = migrators;
        this.eventBus = eventBus;
        this.commands = commands;
        this.reporter = reporter;
        this.serviceManager = serviceManager;
    }

    /**
     * Entry procedure.
     *
     * @param args the command line arguments
     */
    @SuppressWarnings("PMD.AvoidCatchingThrowable")
    public static void main(final String... args) {

        /* TODO: Make this not break reflection from plugins...
        try {
            Policy.setPolicy(new DMDircSecurityPolicy());
            System.setSecurityManager(new SecurityManager());
        } catch (SecurityException ex) {
            System.err.println("Unable to set security policy: " + ex.getMessage());
            ex.printStackTrace();
        }
        */

        try {
            final ClientModule clientModule = new ClientModule();
            final ObjectGraph graph = ObjectGraph.create(clientModule);
            clientModule.setObjectGraph(graph);

            final CommandLineParser parser = graph.get(CommandLineParser.class);
            parser.parse(args);
            graph.get(Main.class).init();
        } catch (Throwable ex) {
            System.err.println("Unable to set security policy: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    /**
     * Initialises the client.
     */
    public void init() {
        Thread.setDefaultUncaughtExceptionHandler(new DMDircExceptionHandler());
        setupLogback();
        migrators.stream().filter(Migrator::needsMigration).forEach(Migrator::migrate);
        commands.forEach(c -> commandManager.registerCommand(c.getCommand(), c.getInfo()));

        loadUIs(serviceManager);

        doFirstRun();

        lifecycleComponents.forEach(SystemLifecycleComponent::startUp);

        pluginManager.doAutoLoad();

        eventBus.publishAsync(new ClientOpenedEvent());
        eventBus.subscribe(reporter);

        commandLineParser.processArguments(connectionManager);

        globalWindowManager.init();
    }

    /**
     * Called when the UI has failed to initialise correctly. This method attempts to extract any
     * and all UI plugins bundled with the client, and requests a restart. If this has already been
     * attempted, it shows an error and exits.
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
            // Try to extract the UIs again in case they changed between versions
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
     * Attempts to find and activate a service which provides a UI that we can use.
     *
     * @param pm The plugin manager to use to load plugins
     */
    protected void loadUIs(final ServiceManager pm) {
        final List<Service> uis = pm.getServicesByType("ui");

        // First try: go for our desired service type
        uis.stream().filter(Service::activate).forEach(service -> {
            final ServiceProvider provider = service.getActiveProvider();

            final Object export = provider.getExportedService("getController").execute();

            if (export != null) {
                CONTROLLERS.add((UIController) export);
            }
        });

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
            eventBus.publish(new FirstRunEvent());

            Executors.newSingleThreadScheduledExecutor(
                    new ThreadFactoryBuilder().setNameFormat("feedback-nag-%d").build()).schedule(
                    () -> eventBus.publishAsync(new FeedbackNagEvent()), 5, TimeUnit.MINUTES);
        }
    }

    private void setupLogback() {
        // TODO: Add a normal logging thing, with or without runtime switching.
        final LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        final ProgramErrorAppender appender = new ProgramErrorAppender();
        appender.setEventBus(eventBus);
        appender.setContext(context);
        appender.setName("Error Logger");
        appender.start();
        final ch.qos.logback.classic.Logger rootLogger = context.getLogger(Logger.ROOT_LOGGER_NAME);
        rootLogger.addAppender(appender);
    }

}
