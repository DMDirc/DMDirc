/*
 * Copyright (c) 2006-2012 DMDirc Developers
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
import com.dmdirc.interfaces.CommandController;
import com.dmdirc.interfaces.ServerFactory;

/**
 * Facilitates loading of core commands into a {@link CommandManager}.
 */
public class CommandLoader {

    /**
     * Server factory to pass to commands.
     */
    private ServerFactory serverFactory;

    /**
     * Creates a new command loader.
     *
     * @param serverFactory Server factory to pass to commands
     */
    public CommandLoader(final ServerFactory serverFactory) {
        this.serverFactory = serverFactory;
    }

    /**
     * Loads all known core commands into the given manager.
     *
     * @param manager The manager to add commands to
     */
    public void loadCommands(final CommandController manager) {
        // Chat commands
        manager.registerCommand(new Me(manager), Me.INFO);

        // Channel commands
        manager.registerCommand(new Ban(manager), Ban.INFO);
        manager.registerCommand(new Cycle(manager), Cycle.INFO);
        manager.registerCommand(new Invite(manager), Invite.INFO);
        manager.registerCommand(new KickReason(manager), KickReason.INFO);
        manager.registerCommand(new Mode(manager), Mode.INFO);
        manager.registerCommand(new Names(manager), Names.INFO);
        manager.registerCommand(new Part(manager), Part.INFO);
        manager.registerCommand(new SetNickColour(manager), SetNickColour.INFO);
        manager.registerCommand(new ShowTopic(manager), ShowTopic.INFO);

        // Server commands
        manager.registerCommand(new AllChannels(manager), AllChannels.INFO);
        manager.registerCommand(new Away(manager), Away.INFO);
        manager.registerCommand(new Back(manager), Back.INFO);
        manager.registerCommand(new ChangeServer(manager), ChangeServer.INFO);
        manager.registerCommand(new Ctcp(manager), Ctcp.INFO);
        manager.registerCommand(new Disconnect(manager), Disconnect.INFO);
        manager.registerCommand(new Ignore(manager), Ignore.INFO);
        manager.registerCommand(new JoinChannelCommand(manager), JoinChannelCommand.INFO);
        manager.registerCommand(new Message(manager), Message.INFO);
        manager.registerCommand(new Nick(manager), Nick.INFO);
        manager.registerCommand(new Notice(manager), Notice.INFO);
        manager.registerCommand(new OpenQuery(manager), OpenQuery.INFO);
        manager.registerCommand(new Raw(manager), Raw.INFO);
        manager.registerCommand(new Reconnect(manager), Reconnect.INFO);
        manager.registerCommand(new Umode(manager), Umode.INFO);

        manager.registerCommand(new RawServerCommand(manager, "lusers"));
        manager.registerCommand(new RawServerCommand(manager, "map"));
        manager.registerCommand(new RawServerCommand(manager, "motd"));
        manager.registerCommand(new RawServerCommand(manager, "oper"));
        manager.registerCommand(new RawServerCommand(manager, "whois"));
        manager.registerCommand(new RawServerCommand(manager, "who"));

        // Query commands

        // Global commands
        manager.registerCommand(new AliasCommand(manager), AliasCommand.INFO);
        manager.registerCommand(new AllServers(manager), AllServers.INFO);
        manager.registerCommand(new Clear(manager), Clear.INFO);
        manager.registerCommand(new Echo(manager), Echo.INFO);
        manager.registerCommand(new Exit(manager), Exit.INFO);
        manager.registerCommand(new Help(manager), Help.INFO);
        manager.registerCommand(new Ifplugin(manager), Ifplugin.INFO);
        manager.registerCommand(new NewServer(manager, serverFactory), NewServer.INFO);
        manager.registerCommand(new Notify(manager), Notify.INFO);
        manager.registerCommand(new LoadPlugin(manager), LoadPlugin.INFO);
        manager.registerCommand(new UnloadPlugin(manager), UnloadPlugin.INFO);
        manager.registerCommand(new OpenWindow(manager), OpenWindow.INFO);
        manager.registerCommand(new ReloadActions(manager), ReloadActions.INFO);
        manager.registerCommand(new ReloadIdentities(manager), ReloadIdentities.INFO);
        manager.registerCommand(new ReloadPlugin(manager), ReloadPlugin.INFO);
        manager.registerCommand(new SaveConfig(manager), SaveConfig.INFO);
        manager.registerCommand(new Set(manager), Set.INFO);
    }

}
