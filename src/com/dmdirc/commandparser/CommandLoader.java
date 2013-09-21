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

package com.dmdirc.commandparser;

import com.dmdirc.interfaces.LifecycleController;
import com.dmdirc.ServerManager;
import com.dmdirc.actions.wrappers.AliasWrapper;
import com.dmdirc.commandparser.commands.channel.Ban;
import com.dmdirc.commandparser.commands.channel.Cycle;
import com.dmdirc.commandparser.commands.channel.Invite;
import com.dmdirc.commandparser.commands.channel.KickReason;
import com.dmdirc.commandparser.commands.channel.Mode;
import com.dmdirc.commandparser.commands.channel.Names;
import com.dmdirc.commandparser.commands.channel.Part;
import com.dmdirc.commandparser.commands.channel.SetNickColour;
import com.dmdirc.commandparser.commands.channel.ShowTopic;
import com.dmdirc.commandparser.commands.chat.Me;
import com.dmdirc.commandparser.commands.global.AliasCommand;
import com.dmdirc.commandparser.commands.global.AllServers;
import com.dmdirc.commandparser.commands.global.Clear;
import com.dmdirc.commandparser.commands.global.Echo;
import com.dmdirc.commandparser.commands.global.Exit;
import com.dmdirc.commandparser.commands.global.Help;
import com.dmdirc.commandparser.commands.global.Ifplugin;
import com.dmdirc.commandparser.commands.global.LoadPlugin;
import com.dmdirc.commandparser.commands.global.NewServer;
import com.dmdirc.commandparser.commands.global.Notify;
import com.dmdirc.commandparser.commands.global.OpenWindow;
import com.dmdirc.commandparser.commands.global.ReloadActions;
import com.dmdirc.commandparser.commands.global.ReloadIdentities;
import com.dmdirc.commandparser.commands.global.ReloadPlugin;
import com.dmdirc.commandparser.commands.global.SaveConfig;
import com.dmdirc.commandparser.commands.global.Set;
import com.dmdirc.commandparser.commands.global.UnloadPlugin;
import com.dmdirc.commandparser.commands.server.AllChannels;
import com.dmdirc.commandparser.commands.server.Away;
import com.dmdirc.commandparser.commands.server.Back;
import com.dmdirc.commandparser.commands.server.ChangeServer;
import com.dmdirc.commandparser.commands.server.Ctcp;
import com.dmdirc.commandparser.commands.server.Disconnect;
import com.dmdirc.commandparser.commands.server.Ignore;
import com.dmdirc.commandparser.commands.server.JoinChannelCommand;
import com.dmdirc.commandparser.commands.server.Message;
import com.dmdirc.commandparser.commands.server.Nick;
import com.dmdirc.commandparser.commands.server.Notice;
import com.dmdirc.commandparser.commands.server.OpenQuery;
import com.dmdirc.commandparser.commands.server.Raw;
import com.dmdirc.commandparser.commands.server.RawServerCommand;
import com.dmdirc.commandparser.commands.server.Reconnect;
import com.dmdirc.commandparser.commands.server.Umode;
import com.dmdirc.config.IdentityManager;
import com.dmdirc.interfaces.ActionController;
import com.dmdirc.interfaces.CommandController;
import com.dmdirc.plugins.PluginManager;
import com.dmdirc.ui.messages.ColourManager;

import javax.inject.Inject;

import dagger.Lazy;

/**
 * Facilitates loading of core commands into a {@link CommandManager}.
 */
public class CommandLoader {

    /** Controller to give to commands that want to start/stop the app. */
    private final Lazy<LifecycleController> lifecycleController;

    /** Server manager to give to server-related commands. */
    private final Lazy<ServerManager> serverManager;

    /** Plugin manager to give to plugin-dependent commands. */
    private final Lazy<PluginManager> pluginManager;

    /** The identity manager to pass to config-related commands. */
    private final Lazy<IdentityManager> identityManager;

    /** The action controller to pass to action-aware commands. */
    private final Lazy<ActionController> actionController;

    /** The colour manager to pass to colourful commands. */
    private final Lazy<ColourManager> colourManager;

    /** The alias wrapper to pass to alias commands. */
    private final Lazy<AliasWrapper> aliasWrapper;

    /**
     * Creates a new instance of {@link CommandLoader}.
     *
     * @param lifecycleController Controller to give to commands that want to start/stop the app.
     * @param serverManager The server manager to pass to server-related commands.
     * @param pluginManager The plugin manager to pass to plugin-dependent commands.
     * @param identityManager The identity manager to pass to config-related commands.
     * @param colourManager The colour manager to give to colourful commands.
     * @param aliasWrapper The alias wrapper to pass to alias commands.
     */
    @Inject
    public CommandLoader(
            final Lazy<LifecycleController> lifecycleController,
            final Lazy<ServerManager> serverManager,
            final Lazy<PluginManager> pluginManager,
            final Lazy<IdentityManager> identityManager,
            final Lazy<ActionController> actionController,
            final Lazy<ColourManager> colourManager,
            final Lazy<AliasWrapper> aliasWrapper) {
        this.lifecycleController = lifecycleController;
        this.serverManager = serverManager;
        this.pluginManager = pluginManager;
        this.identityManager = identityManager;
        this.actionController = actionController;
        this.colourManager = colourManager;
        this.aliasWrapper = aliasWrapper;
    }

    /**
     * Loads all known core commands into the given manager.
     *
     * @param manager The manager to add commands to
     */
    public void loadCommands(final CommandController manager) {
        // Chat commands
        manager.registerCommand(new Me(), Me.INFO);

        // Channel commands
        manager.registerCommand(new Ban(), Ban.INFO);
        manager.registerCommand(new Cycle(), Cycle.INFO);
        manager.registerCommand(new Invite(), Invite.INFO);
        manager.registerCommand(new KickReason(), KickReason.INFO);
        manager.registerCommand(new Mode(), Mode.INFO);
        manager.registerCommand(new Names(), Names.INFO);
        manager.registerCommand(new Part(), Part.INFO);
        manager.registerCommand(new SetNickColour(colourManager.get()), SetNickColour.INFO);
        manager.registerCommand(new ShowTopic(), ShowTopic.INFO);

        // Server commands
        manager.registerCommand(new AllChannels(), AllChannels.INFO);
        manager.registerCommand(new Away(), Away.INFO);
        manager.registerCommand(new Back(), Back.INFO);
        manager.registerCommand(new ChangeServer(), ChangeServer.INFO);
        manager.registerCommand(new Ctcp(), Ctcp.INFO);
        manager.registerCommand(new Disconnect(), Disconnect.INFO);
        manager.registerCommand(new Ignore(), Ignore.INFO);
        manager.registerCommand(new JoinChannelCommand(actionController.get()), JoinChannelCommand.INFO);
        manager.registerCommand(new Message(), Message.INFO);
        manager.registerCommand(new Nick(), Nick.INFO);
        manager.registerCommand(new Notice(), Notice.INFO);
        manager.registerCommand(new OpenQuery(), OpenQuery.INFO);
        manager.registerCommand(new Raw(), Raw.INFO);
        manager.registerCommand(new Reconnect(), Reconnect.INFO);
        manager.registerCommand(new Umode(), Umode.INFO);

        manager.registerCommand(new RawServerCommand("lusers"));
        manager.registerCommand(new RawServerCommand("map"));
        manager.registerCommand(new RawServerCommand("motd"));
        manager.registerCommand(new RawServerCommand("oper"));
        manager.registerCommand(new RawServerCommand("whois"));
        manager.registerCommand(new RawServerCommand("who"));

        // Query commands

        // Global commands
        manager.registerCommand(new AliasCommand(aliasWrapper.get()), AliasCommand.INFO);
        manager.registerCommand(new AllServers(serverManager.get()), AllServers.INFO);
        manager.registerCommand(new Clear(), Clear.INFO);
        manager.registerCommand(new Echo(), Echo.INFO);
        manager.registerCommand(new Exit(lifecycleController.get()), Exit.INFO);
        manager.registerCommand(new Help(), Help.INFO);
        manager.registerCommand(new Ifplugin(pluginManager.get()), Ifplugin.INFO);
        manager.registerCommand(new NewServer(serverManager.get(), pluginManager.get(), identityManager.get()), NewServer.INFO);
        manager.registerCommand(new Notify(colourManager.get()), Notify.INFO);
        manager.registerCommand(new LoadPlugin(pluginManager.get()), LoadPlugin.INFO);
        manager.registerCommand(new UnloadPlugin(pluginManager.get()), UnloadPlugin.INFO);
        manager.registerCommand(new OpenWindow(), OpenWindow.INFO);
        manager.registerCommand(new ReloadActions(), ReloadActions.INFO);
        manager.registerCommand(new ReloadIdentities(identityManager.get()), ReloadIdentities.INFO);
        manager.registerCommand(new ReloadPlugin(pluginManager.get()), ReloadPlugin.INFO);
        manager.registerCommand(new SaveConfig(identityManager.get()), SaveConfig.INFO);
        manager.registerCommand(new Set(identityManager.get(), identityManager.get()), Set.INFO);
    }

}
