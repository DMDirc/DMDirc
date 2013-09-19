package com.dmdirc;

import com.dmdirc.actions.ActionManager;
import com.dmdirc.commandparser.CommandManager;
import com.dmdirc.config.IdentityManager;
import com.dmdirc.config.InvalidIdentityFileException;
import com.dmdirc.plugins.PluginManager;

import static org.mockito.Mockito.*;

/**
 * Main subclass to init things needed for testing.
 */
public class TestMain extends Main {

    private static Main instance;

    private final IdentityManager identityManager;
    private final ServerManager serverManager;
    private final ActionManager actionManager;

    public TestMain(final IdentityManager identityManager,
            final ServerManager serverManager,
            final ActionManager actionManager) {
        super(identityManager, serverManager, actionManager);
        this.identityManager = identityManager;
        this.serverManager = serverManager;
        this.actionManager = actionManager;
    }

    /** {@inheritDoc} */
    @Override
    public void init(final String[] args) {
        try {
            IdentityManager.getIdentityManager().initialise(getConfigDir());
        } catch (InvalidIdentityFileException ex) {
            // If a bad config dir exists, try to continue anyway, maybe the
            // test doesn't need it.
            // DON'T do anything to the user's configuration... (so no calls
            // to handleInvalidConfigFile(); here)
        }

        final String fs = System.getProperty("file.separator");
        final String pluginDirectory = getConfigDir() + "plugins" + fs;
        pluginManager = new PluginManager(IdentityManager.getIdentityManager(), pluginDirectory);
        pluginManager.refreshPlugins();
        CommandManager.initCommandManager(IdentityManager.getIdentityManager(), serverManager);

        ActionManager.getActionManager().initialise();
    }

    /**
     * Singleton method for convenience so that we don't need to init a billion
     * TestMain instances to run tests.
     *
     * Separate instances of TestMain are available, but probably pointless as
     * long as IdentityManager, PluginManager and ConfigManater are still
     * singletons.
     *
     * @return A Singleton instance of TestMain
     */
    public static Main getTestMain() {
        if (instance == null) {
            // TODO: Tests probably shouldn't rely on a config dir... Who knows
            //       what the user has done with their config.
            final IdentityManager identityManager = new IdentityManager();
            IdentityManager.setIdentityManager(identityManager);
            IdentityManager.getIdentityManager().loadVersionIdentity();

            final ServerManager serverManager = mock(ServerManager.class);

            final ActionManager actionManager = new ActionManager(serverManager, identityManager);
            ActionManager.setActionManager(actionManager);

            instance = new TestMain(identityManager, serverManager, actionManager);
            instance.init(new String[0]);
        }
        return instance;
    }
}
