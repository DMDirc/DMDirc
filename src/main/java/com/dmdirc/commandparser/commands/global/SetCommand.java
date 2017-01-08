/*
 * Copyright (c) 2006-2017 DMDirc Developers
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the
 * Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS
 * OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.dmdirc.commandparser.commands.global;

import com.dmdirc.commandparser.BaseCommandInfo;
import com.dmdirc.commandparser.CommandArguments;
import com.dmdirc.commandparser.CommandInfo;
import com.dmdirc.commandparser.CommandType;
import com.dmdirc.commandparser.commands.BaseCommand;
import com.dmdirc.commandparser.commands.IntelligentCommand;
import com.dmdirc.commandparser.commands.context.ChannelCommandContext;
import com.dmdirc.commandparser.commands.context.CommandContext;
import com.dmdirc.commandparser.commands.flags.CommandFlag;
import com.dmdirc.commandparser.commands.flags.CommandFlagHandler;
import com.dmdirc.commandparser.commands.flags.CommandFlagResult;
import com.dmdirc.interfaces.CommandController;
import com.dmdirc.interfaces.Connection;
import com.dmdirc.interfaces.GroupChat;
import com.dmdirc.interfaces.WindowModel;
import com.dmdirc.config.provider.AggregateConfigProvider;
import com.dmdirc.config.provider.ConfigProvider;
import com.dmdirc.interfaces.config.IdentityController;
import com.dmdirc.interfaces.config.IdentityFactory;
import com.dmdirc.interfaces.config.ReadOnlyConfigProvider;
import com.dmdirc.ui.input.AdditionalTabTargets;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;
import java.util.List;
import java.util.Optional;

/**
 * The set command allows the user to inspect and change global config settings.
 */
public class SetCommand extends BaseCommand implements IntelligentCommand {

    /** A command info object for this command. */
    public static final CommandInfo INFO = new BaseCommandInfo("set",
            "set [--server|--channel] [domain [option [newvalue]]] - inspect or change configuration settings"
            + "\nset [--server|--channel] --append <domain> <option> <data> - appends data to the specified option"
            + "\nset [--server|--channel] --unset <domain> <option> - unsets the specified option",
            CommandType.TYPE_GLOBAL);
    /** The flag to indicate the set command should apply to a server's settings. */
    private final CommandFlag serverFlag = new CommandFlag("server");
    /** The flag to indicate the set command should apply to a channel's settings. */
    private final CommandFlag channelFlag = new CommandFlag("channel");
    /** The flag to indicate that the specified setting should be unset. */
    private final CommandFlag unsetFlag = new CommandFlag("unset", true, 0, 2);
    /** The flag to indicate that the specified setting should be appended to. */
    private final CommandFlag appendFlag = new CommandFlag("append", true, 0, 2);
    /** The command flag handler for this command. */
    private final CommandFlagHandler handler;
    /** The controller to use to set settings. */
    private final IdentityController identityController;
    /** The factory to use to create new identities. */
    private final IdentityFactory identityFactory;

    /**
     * Creates a new instance of Set.
     *
     * @param controller         The controller to use for command information.
     * @param identityController The controller to use to set settings.
     * @param identityFactory    The factory to use to create new identities.
     */
    @Inject
    public SetCommand(
            final CommandController controller,
            final IdentityController identityController,
            final IdentityFactory identityFactory) {
        super(controller);

        this.identityController = identityController;
        this.identityFactory = identityFactory;

        unsetFlag.addDisabled(appendFlag);
        appendFlag.addDisabled(unsetFlag);

        channelFlag.addDisabled(serverFlag);
        serverFlag.addDisabled(channelFlag);

        handler = new CommandFlagHandler(serverFlag, channelFlag, unsetFlag, appendFlag);
    }

    @Override
    public void execute(@Nonnull final WindowModel origin,
            final CommandArguments args, final CommandContext context) {
        @Nullable final CommandFlagResult res = handler.process(origin, args);

        if (res == null) {
            return;
        }

        ConfigProvider identity = identityController.getUserSettings();
        AggregateConfigProvider manager = identityController.getGlobalConfiguration();
        final Optional<Connection> connection = origin.getConnection();

        if (res.hasFlag(serverFlag)) {
            if (!connection.isPresent()) {
                showError(origin, args.isSilent(), "Cannot use --server in this context");
                return;
            }

            identity = connection.get().getServerIdentity();
            manager = connection.get().getWindowModel().getConfigManager();
        }

        if (res.hasFlag(channelFlag)) {
            if (!(context instanceof ChannelCommandContext)) {
                showError(origin, args.isSilent(),"Cannot use --channel in this context");
                return;
            }

            final GroupChat groupChat = ((ChannelCommandContext) context).getGroupChat();
            identity = identityFactory.createChannelConfig(connection.get().getNetwork(),
                    groupChat.getName());
            manager = groupChat.getWindowModel().getConfigManager();
        }

        if (res.hasFlag(unsetFlag)) {
            final String[] arguments = res.getArguments(unsetFlag);
            doUnsetOption(origin, args.isSilent(), identity, arguments[0], arguments[1]);
            return;
        }

        if (res.hasFlag(appendFlag)) {
            final String[] arguments = res.getArguments(appendFlag);
            doAppendOption(origin, args.isSilent(), identity, manager,
                    arguments[0], arguments[1], res.getArgumentsAsString());
            return;
        }

        final String[] arguments = res.getArguments();

        switch (arguments.length) {
            case 0:
                doDomainList(origin, args.isSilent(), manager);
                break;
            case 1:
                doOptionsList(origin, args.isSilent(), manager, arguments[0]);
                break;
            case 2:
                doShowOption(origin, args.isSilent(), manager, arguments[0],
                        arguments[1]);
                break;
            default:
                doSetOption(origin, args.isSilent(), identity, arguments[0],
                        arguments[1], res.getArgumentsAsString(2));
                break;
        }
    }

    /**
     * Shows the user a list of valid domains.
     *
     * @param origin   The window the command was issued from
     * @param isSilent Whether or not the command is being silenced or not
     * @param manager  The config manager to use to retrieve data
     */
    private void doDomainList(final WindowModel origin, final boolean isSilent,
            final AggregateConfigProvider manager) {
        final StringBuilder output = new StringBuilder(67);

        output.append("Valid domains (use ");
        output.append(getController().getCommandChar());
        output.append("set <domain> to see options within a domain): ");

        for (String domain : manager.getDomains()) {
            output.append(domain);
            output.append(", ");
        }

        showOutput(origin, isSilent, output.substring(0, output.length() - 2));
    }

    /**
     * Shows the user a list of valid options within a domain.
     *
     * @param origin   The window the command was issued from
     * @param isSilent Whether or not the command is being silenced or not
     * @param manager  The config manager to use to retrieve data
     * @param domain   The domain to be inspected
     */
    private void doOptionsList(final WindowModel origin,
            final boolean isSilent, final ReadOnlyConfigProvider manager, final String domain) {
        final StringBuilder output = new StringBuilder(24);

        output.append("Options in domain '");
        output.append(domain);
        output.append("': ");

        boolean found = false;

        for (String option : manager.getOptions(domain).keySet()) {
            output.append(option);
            output.append(", ");
            found = true;
        }

        if (found) {
            showOutput(origin, isSilent, output.substring(0, output.length() - 2));
        } else {
            showError(origin, isSilent, "There are no options in the domain '" + domain + "'.");
        }
    }

    /**
     * Shows the user the current value of one option.
     *
     * @param origin   The window the command was issued from
     * @param isSilent Whether or not the command is being silenced or not
     * @param manager  The config manager to use to retrieve data
     * @param domain   The domain of the option
     * @param option   The name of the option
     */
    private void doShowOption(final WindowModel origin,
            final boolean isSilent, final ReadOnlyConfigProvider manager,
            final String domain, final String option) {
        if (manager.hasOptionString(domain, option)) {
            showOutput(origin, isSilent, "The current value of "
                    + domain + '.' + option + " is: " + manager.getOption(domain, option));
        } else {
            showError(origin, isSilent, "Option not found: " + domain + '.' + option);
        }
    }

    /**
     * Sets the value of the specified option.
     *
     * @param origin   The window the command was issued from
     * @param isSilent Whether or not the command is being silenced or not
     * @param identity The identity to use to set data
     * @param domain   The domain of the option
     * @param option   The name of the option
     * @param newvalue The value the option should be set to
     */
    private void doSetOption(final WindowModel origin,
            final boolean isSilent, final ConfigProvider identity,
            final String domain, final String option, final String newvalue) {
        identity.setOption(domain, option, newvalue);

        showOutput(origin, isSilent, domain + '.' + option + " has been set to: " + newvalue);
    }

    /**
     * Appends data to the specified option.
     *
     * @param origin   The window the command was issued from
     * @param isSilent Whether or not the command is being silenced or not
     * @param identity The identity to use to set data
     * @param manager  The config manager to use to retrieve data
     * @param domain   The domain of the option
     * @param option   The name of the option
     * @param data     The data to be appended
     */
    private void doAppendOption(final WindowModel origin,
            final boolean isSilent, final ConfigProvider identity,
            final ReadOnlyConfigProvider manager,
            final String domain, final String option, final String data) {
        doSetOption(origin, isSilent, identity, domain, option,
                (manager.hasOptionString(domain, option)
                ? manager.getOption(domain, option) : "") + data);
    }

    /**
     * Unsets the specified option.
     *
     * @param origin   The window the command was issued from
     * @param isSilent Whether or not the command is being silenced or not
     * @param identity The identity to use to set data
     * @param domain   The domain of the option
     * @param option   The name of the option
     */
    private void doUnsetOption(final WindowModel origin,
            final boolean isSilent, final ConfigProvider identity, final String domain,
            final String option) {
        identity.unsetOption(domain, option);

        showOutput(origin, isSilent, domain + '.' + option + " has been unset.");
    }

    @Override
    public AdditionalTabTargets getSuggestions(final int arg,
            final IntelligentCommandContext context) {
        final List<String> previousArgs = context.getPreviousArgs();
        final AdditionalTabTargets res = new AdditionalTabTargets();

        if (arg == 0) {
            res.addAll(context.getWindow().getConfigManager()
                    .getDomains());
            res.add("--unset");
            res.add("--append");
            res.add("--server");
            res.add("--channel");
            res.excludeAll();
        } else if (arg == 1 && !previousArgs.isEmpty()) {
            if ("--unset".equalsIgnoreCase(previousArgs.get(0))
                    || "--append".equalsIgnoreCase(previousArgs.get(0))
                    || "--server".equalsIgnoreCase(previousArgs.get(0))
                    || "--channel".equalsIgnoreCase(previousArgs.get(0))) {
                res.addAll(context.getWindow().getConfigManager()
                        .getDomains());
            } else {
                res.addAll(context.getWindow().getConfigManager()
                        .getOptions(previousArgs.get(0)).keySet());
            }
            res.excludeAll();
        } else if (arg == 2 && ("--unset".equalsIgnoreCase(previousArgs.get(0))
                || "--append".equalsIgnoreCase(previousArgs.get(0))
                || "--server".equalsIgnoreCase(previousArgs.get(0))
                || "--channel".equalsIgnoreCase(previousArgs.get(0)))) {
            res.addAll(context.getWindow().getConfigManager()
                    .getOptions(previousArgs.get(1)).keySet());
            res.excludeAll();
        }

        return res;
    }

}
