package com.dmdirc;

import com.dmdirc.actions.ActionManager;
import com.dmdirc.commandparser.CommandManager;
import com.dmdirc.config.IdentityManager;
import com.dmdirc.config.InvalidIdentityFileException;
import com.dmdirc.plugins.PluginManager;

/**
 * Main subclass to init things needed for testing.
 */
public class TestMain extends Main {
    private static Main instance;

    public TestMain() { }

    /** {@inheritDoc} */
    @Override
    public void init(final String[] args) {
        // TODO: Tests probably shouldn't rely on a config dir... Who knows
        //       what the user has done with their config.
        IdentityManager.getIdentityManager().loadVersionIdentity();
        try {
            IdentityManager.getIdentityManager().initialise(getConfigDir());
        } catch (InvalidIdentityFileException ex) {
            // If a bad config dir exists, try to continue anyway, maybe the
            // test doesn't need it.
            // DON'T do anything to the user's configuration... (so no calls
            // to handleInvalidConfigFile(); here)
        }
        serverManager = new ServerManager(this);
        ActionManager.initActionManager(this, serverManager, IdentityManager.getIdentityManager());
        pluginManager = new PluginManager(IdentityManager.getIdentityManager(), this);
        pluginManager.refreshPlugins();
        CommandManager.initCommandManager(IdentityManager.getIdentityManager(), this);

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
            instance = new TestMain();
            instance.init(new String[0]);
        }
        return instance;
    }
}