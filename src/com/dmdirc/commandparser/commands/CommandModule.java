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

package com.dmdirc.commandparser.commands;

import com.dmdirc.commandparser.CommandInfo;
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
import com.dmdirc.commandparser.commands.global.OpenWindow;
import com.dmdirc.commandparser.commands.global.ReloadIdentities;
import com.dmdirc.commandparser.commands.global.ReloadPlugin;
import com.dmdirc.commandparser.commands.global.SaveConfig;
import com.dmdirc.commandparser.commands.global.SetCommand;
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
import com.dmdirc.interfaces.CommandController.CommandDetails;

import java.util.HashSet;
import java.util.Set;

import dagger.Module;
import dagger.Provides;

/**
 * Provides commands for injection into the command manager.
 */
@Module(library = true, complete = false)
public class CommandModule {

    /**
     * Provides the /me command.
     *
     * @param command The instantiated command.
     *
     * @return The command's details.
     */
    @Provides(type = Provides.Type.SET)
    public CommandDetails getCommand(final Me command) {
        return new SimpleCommandDetails(command, Me.INFO);
    }

    /**
     * Provides the /ban command.
     *
     * @param command The instantiated command.
     *
     * @return The command's details.
     */
    @Provides(type = Provides.Type.SET)
    public CommandDetails getCommand(final Ban command) {
        return new SimpleCommandDetails(command, Ban.INFO);
    }

    /**
     * Provides the /cycle command.
     *
     * @param command The instantiated command.
     *
     * @return The command's details.
     */
    @Provides(type = Provides.Type.SET)
    public CommandDetails getCommand(final Cycle command) {
        return new SimpleCommandDetails(command, Cycle.INFO);
    }

    /**
     * Provides the /invite command.
     *
     * @param command The instantiated command.
     *
     * @return The command's details.
     */
    @Provides(type = Provides.Type.SET)
    public CommandDetails getCommand(final Invite command) {
        return new SimpleCommandDetails(command, Invite.INFO);
    }

    /**
     * Provides the /kick command.
     *
     * @param command The instantiated command.
     *
     * @return The command's details.
     */
    @Provides(type = Provides.Type.SET)
    public CommandDetails getCommand(final KickReason command) {
        return new SimpleCommandDetails(command, KickReason.INFO);
    }

    /**
     * Provides the /mode command.
     *
     * @param command The instantiated command.
     *
     * @return The command's details.
     */
    @Provides(type = Provides.Type.SET)
    public CommandDetails getCommand(final Mode command) {
        return new SimpleCommandDetails(command, Mode.INFO);
    }

    /**
     * Provides the /names command.
     *
     * @param command The instantiated command.
     *
     * @return The command's details.
     */
    @Provides(type = Provides.Type.SET)
    public CommandDetails getCommand(final Names command) {
        return new SimpleCommandDetails(command, Names.INFO);
    }

    /**
     * Provides the /part command.
     *
     * @param command The instantiated command.
     *
     * @return The command's details.
     */
    @Provides(type = Provides.Type.SET)
    public CommandDetails getCommand(final Part command) {
        return new SimpleCommandDetails(command, Part.INFO);
    }

    /**
     * Provides the /setnickcolour command.
     *
     * @param command The instantiated command.
     *
     * @return The command's details.
     */
    @Provides(type = Provides.Type.SET)
    public CommandDetails getCommand(final SetNickColour command) {
        return new SimpleCommandDetails(command, SetNickColour.INFO);
    }

    /**
     * Provides the /topic command.
     *
     * @param command The instantiated command.
     *
     * @return The command's details.
     */
    @Provides(type = Provides.Type.SET)
    public CommandDetails getCommand(final ShowTopic command) {
        return new SimpleCommandDetails(command, ShowTopic.INFO);
    }

    /**
     * Provides the /allchannels command.
     *
     * @param command The instantiated command.
     *
     * @return The command's details.
     */
    @Provides(type = Provides.Type.SET)
    public CommandDetails getCommand(final AllChannels command) {
        return new SimpleCommandDetails(command, AllChannels.INFO);
    }

    /**
     * Provides the /away command.
     *
     * @param command The instantiated command.
     *
     * @return The command's details.
     */
    @Provides(type = Provides.Type.SET)
    public CommandDetails getCommand(final Away command) {
        return new SimpleCommandDetails(command, Away.INFO);
    }

    /**
     * Provides the /back command.
     *
     * @param command The instantiated command.
     *
     * @return The command's details.
     */
    @Provides(type = Provides.Type.SET)
    public CommandDetails getCommand(final Back command) {
        return new SimpleCommandDetails(command, Back.INFO);
    }

    /**
     * Provides the /server command.
     *
     * @param command The instantiated command.
     *
     * @return The command's details.
     */
    @Provides(type = Provides.Type.SET)
    public CommandDetails getCommand(final ChangeServer command) {
        return new SimpleCommandDetails(command, ChangeServer.INFO);
    }

    /**
     * Provides the /ctcp command.
     *
     * @param command The instantiated command.
     *
     * @return The command's details.
     */
    @Provides(type = Provides.Type.SET)
    public CommandDetails getCommand(final Ctcp command) {
        return new SimpleCommandDetails(command, Ctcp.INFO);
    }

    /**
     * Provides the /disconnect command.
     *
     * @param command The instantiated command.
     *
     * @return The command's details.
     */
    @Provides(type = Provides.Type.SET)
    public CommandDetails getCommand(final Disconnect command) {
        return new SimpleCommandDetails(command, Disconnect.INFO);
    }

    /**
     * Provides the /ignore command.
     *
     * @param command The instantiated command.
     *
     * @return The command's details.
     */
    @Provides(type = Provides.Type.SET)
    public CommandDetails getCommand(final Ignore command) {
        return new SimpleCommandDetails(command, Ignore.INFO);
    }

    /**
     * Provides the /join command.
     *
     * @param command The instantiated command.
     *
     * @return The command's details.
     */
    @Provides(type = Provides.Type.SET)
    public CommandDetails getCommand(final JoinChannelCommand command) {
        return new SimpleCommandDetails(command, JoinChannelCommand.INFO);
    }

    /**
     * Provides the /msg command.
     *
     * @param command The instantiated command.
     *
     * @return The command's details.
     */
    @Provides(type = Provides.Type.SET)
    public CommandDetails getCommand(final Message command) {
        return new SimpleCommandDetails(command, Message.INFO);
    }

    /**
     * Provides the /nick command.
     *
     * @param command The instantiated command.
     *
     * @return The command's details.
     */
    @Provides(type = Provides.Type.SET)
    public CommandDetails getCommand(final Nick command) {
        return new SimpleCommandDetails(command, Nick.INFO);
    }

    /**
     * Provides the /notice command.
     *
     * @param command The instantiated command.
     *
     * @return The command's details.
     */
    @Provides(type = Provides.Type.SET)
    public CommandDetails getCommand(final Notice command) {
        return new SimpleCommandDetails(command, Notice.INFO);
    }

    /**
     * Provides the /query command.
     *
     * @param command The instantiated command.
     *
     * @return The command's details.
     */
    @Provides(type = Provides.Type.SET)
    public CommandDetails getCommand(final OpenQuery command) {
        return new SimpleCommandDetails(command, OpenQuery.INFO);
    }

    /**
     * Provides the /raw command.
     *
     * @param command The instantiated command.
     *
     * @return The command's details.
     */
    @Provides(type = Provides.Type.SET)
    public CommandDetails getCommand(final Raw command) {
        return new SimpleCommandDetails(command, Raw.INFO);
    }

    /**
     * Provides the /reconnect command.
     *
     * @param command The instantiated command.
     *
     * @return The command's details.
     */
    @Provides(type = Provides.Type.SET)
    public CommandDetails getCommand(final Reconnect command) {
        return new SimpleCommandDetails(command, Reconnect.INFO);
    }

    /**
     * Provides the /umode command.
     *
     * @param command The instantiated command.
     *
     * @return The command's details.
     */
    @Provides(type = Provides.Type.SET)
    public CommandDetails getCommand(final Umode command) {
        return new SimpleCommandDetails(command, Umode.INFO);
    }

    /**
     * Provides a set of raw commands.
     *
     * @param controller The controller to use for command info.
     *
     * @return A set of the commands' details.
     */
    @Provides(type = Provides.Type.SET_VALUES)
    public Set<CommandDetails> getRawCommands(final CommandController controller) {
        final Set<CommandDetails> results = new HashSet<>();
        for (String name : new String[]{"lusers", "map", "motd", "oper", "whois", "who"}) {
            final RawServerCommand rawCommand = new RawServerCommand(controller, name);
            results.add(new SimpleCommandDetails(rawCommand, rawCommand));
        }
        return results;
    }

    /**
     * Provides the /alias command.
     *
     * @param command The instantiated command.
     *
     * @return The command's details.
     */
    @Provides(type = Provides.Type.SET)
    public CommandDetails getCommand(final AliasCommand command) {
        return new SimpleCommandDetails(command, AliasCommand.INFO);
    }

    /**
     * Provides the /allservers command.
     *
     * @param command The instantiated command.
     *
     * @return The command's details.
     */
    @Provides(type = Provides.Type.SET)
    public CommandDetails getCommand(final AllServers command) {
        return new SimpleCommandDetails(command, AllServers.INFO);
    }

    /**
     * Provides the /clear command.
     *
     * @param command The instantiated command.
     *
     * @return The command's details.
     */
    @Provides(type = Provides.Type.SET)
    public CommandDetails getCommand(final Clear command) {
        return new SimpleCommandDetails(command, Clear.INFO);
    }

    /**
     * Provides the /echo command.
     *
     * @param command The instantiated command.
     *
     * @return The command's details.
     */
    @Provides(type = Provides.Type.SET)
    public CommandDetails getCommand(final Echo command) {
        return new SimpleCommandDetails(command, Echo.INFO);
    }

    /**
     * Provides the /exit command.
     *
     * @param command The instantiated command.
     *
     * @return The command's details.
     */
    @Provides(type = Provides.Type.SET)
    public CommandDetails getCommand(final Exit command) {
        return new SimpleCommandDetails(command, Exit.INFO);
    }

    /**
     * Provides the /Help command.
     *
     * @param command The instantiated command.
     *
     * @return The command's details.
     */
    @Provides(type = Provides.Type.SET)
    public CommandDetails getCommand(final Help command) {
        return new SimpleCommandDetails(command, Help.INFO);
    }

    /**
     * Provides the /ifplugin command.
     *
     * @param command The instantiated command.
     *
     * @return The command's details.
     */
    @Provides(type = Provides.Type.SET)
    public CommandDetails getCommand(final Ifplugin command) {
        return new SimpleCommandDetails(command, Ifplugin.INFO);
    }

    /**
     * Provides the /newserver command.
     *
     * @param command The instantiated command.
     *
     * @return The command's details.
     */
    @Provides(type = Provides.Type.SET)
    public CommandDetails getCommand(final NewServer command) {
        return new SimpleCommandDetails(command, NewServer.INFO);
    }

    /**
     * Provides the /loadplugin command.
     *
     * @param command The instantiated command.
     *
     * @return The command's details.
     */
    @Provides(type = Provides.Type.SET)
    public CommandDetails getCommand(final LoadPlugin command) {
        return new SimpleCommandDetails(command, LoadPlugin.INFO);
    }

    /**
     * Provides the /unloadplugin command.
     *
     * @param command The instantiated command.
     *
     * @return The command's details.
     */
    @Provides(type = Provides.Type.SET)
    public CommandDetails getCommand(final UnloadPlugin command) {
        return new SimpleCommandDetails(command, UnloadPlugin.INFO);
    }

    /**
     * Provides the /openwindow command.
     *
     * @param command The instantiated command.
     *
     * @return The command's details.
     */
    @Provides(type = Provides.Type.SET)
    public CommandDetails getCommand(final OpenWindow command) {
        return new SimpleCommandDetails(command, OpenWindow.INFO);
    }

    /**
     * Provides the /reloadidentities command.
     *
     * @param command The instantiated command.
     *
     * @return The command's details.
     */
    @Provides(type = Provides.Type.SET)
    public CommandDetails getCommand(final ReloadIdentities command) {
        return new SimpleCommandDetails(command, ReloadIdentities.INFO);
    }

    /**
     * Provides the /reloadplugin command.
     *
     * @param command The instantiated command.
     *
     * @return The command's details.
     */
    @Provides(type = Provides.Type.SET)
    public CommandDetails getCommand(final ReloadPlugin command) {
        return new SimpleCommandDetails(command, ReloadPlugin.INFO);
    }

    /**
     * Provides the /saveconfig command.
     *
     * @param command The instantiated command.
     *
     * @return The command's details.
     */
    @Provides(type = Provides.Type.SET)
    public CommandDetails getCommand(final SaveConfig command) {
        return new SimpleCommandDetails(command, SaveConfig.INFO);
    }

    /**
     * Provides the /set command.
     *
     * @param command The instantiated command.
     *
     * @return The command's details.
     */
    @Provides(type = Provides.Type.SET)
    public CommandDetails getCommand(final SetCommand command) {
        return new SimpleCommandDetails(command, SetCommand.INFO);
    }

    /**
     * Simple implementation of {@link CommandDetails}.
     */
    private static class SimpleCommandDetails implements CommandDetails {

        public SimpleCommandDetails(final Command command, final CommandInfo info) {
            this.command = command;
            this.info = info;
        }

        @Override
        public Command getCommand() {
            return command;
        }

        @Override
        public CommandInfo getInfo() {
            return info;
        }
        /** The command. */
        private final Command command;
        /** The command's info. */
        private final CommandInfo info;

    }

}
