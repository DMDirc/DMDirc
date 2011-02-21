/*
 * Copyright (c) 2006-2011 Chris Smith, Shane Mc Cormack, Gregory Holmes
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

package com.dmdirc.commandparser.commands.global;

import com.dmdirc.Channel;
import com.dmdirc.FrameContainer;
import com.dmdirc.commandparser.BaseCommandInfo;
import com.dmdirc.commandparser.CommandArguments;
import com.dmdirc.commandparser.CommandInfo;
import com.dmdirc.commandparser.CommandManager;
import com.dmdirc.commandparser.CommandType;
import com.dmdirc.commandparser.commands.Command;
import com.dmdirc.commandparser.commands.IntelligentCommand;
import com.dmdirc.commandparser.commands.context.ChannelCommandContext;
import com.dmdirc.commandparser.commands.context.CommandContext;
import com.dmdirc.commandparser.commands.flags.CommandFlag;
import com.dmdirc.commandparser.commands.flags.CommandFlagHandler;
import com.dmdirc.commandparser.commands.flags.CommandFlagResult;
import com.dmdirc.config.ConfigManager;
import com.dmdirc.config.Identity;
import com.dmdirc.config.IdentityManager;
import com.dmdirc.ui.input.AdditionalTabTargets;

import java.util.List;

/**
 * The set command allows the user to inspect and change global config settings.
 */
public class Set extends Command implements IntelligentCommand {

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

    /**
     * Creates a new instance of Set.
     */
    public Set() {
        super();

        unsetFlag.addDisabled(appendFlag);
        appendFlag.addDisabled(unsetFlag);

        channelFlag.addDisabled(serverFlag);
        serverFlag.addDisabled(channelFlag);

        handler = new CommandFlagHandler(serverFlag, channelFlag, unsetFlag, appendFlag);
    }

    /** {@inheritDoc} */
    @Override
    public void execute(final FrameContainer origin,
            final CommandArguments args, final CommandContext context) {
        final CommandFlagResult res = handler.process(origin, args);

        if (res == null) {
            return;
        }

        Identity identity = IdentityManager.getConfigIdentity();
        ConfigManager manager = IdentityManager.getGlobalConfig();

        if (res.hasFlag(serverFlag)) {
            if (origin.getServer() == null) {
                sendLine(origin, args.isSilent(), FORMAT_ERROR,
                        "Cannot use --server in this context");
                return;
            }

            identity = origin.getServer().getServerIdentity();
            manager = origin.getServer().getConfigManager();
        }

        if (res.hasFlag(channelFlag)) {
            if (!(context instanceof ChannelCommandContext)) {
                sendLine(origin, args.isSilent(), FORMAT_ERROR,
                        "Cannot use --channel in this context");
                return;
            }

            final Channel channel = ((ChannelCommandContext) context).getChannel();
            identity = IdentityManager.getChannelConfig(origin.getServer().getNetwork(),
                    channel.getName());
            manager = channel.getConfigManager();
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
     * @param origin The window the command was issued from
     * @param isSilent Whether or not the command is being silenced or not
     * @param manager The config manager to use to retrieve data
     */
    private void doDomainList(final FrameContainer origin, final boolean isSilent,
            final ConfigManager manager) {
        final StringBuffer output = new StringBuffer(67);

        output.append("Valid domains (use ");
        output.append(CommandManager.getCommandManager().getCommandChar());
        output.append("set <domain> to see options within a domain): ");

        for (String domain : manager.getDomains()) {
            output.append(domain);
            output.append(", ");
        }

        sendLine(origin, isSilent, FORMAT_OUTPUT, output.substring(0, output.length() - 2));
    }

    /**
     * Shows the user a list of valid options within a domain.
     *
     * @param origin The window the command was issued from
     * @param isSilent Whether or not the command is being silenced or not
     * @param manager The config manager to use to retrieve data
     * @param domain The domain to be inspected
     */
    private void doOptionsList(final FrameContainer origin,
            final boolean isSilent, final ConfigManager manager, final String domain) {
        final StringBuffer output = new StringBuffer(24);

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
            sendLine(origin, isSilent, FORMAT_OUTPUT, output.substring(0, output.length() - 2));
        } else {
            sendLine(origin, isSilent, FORMAT_ERROR,
                    "There are no options in the domain '" + domain + "'.");
        }
    }

    /**
     * Shows the user the current value of one option.
     *
     * @param origin The window the command was issued from
     * @param isSilent Whether or not the command is being silenced or not
     * @param manager The config manager to use to retrieve data
     * @param domain The domain of the option
     * @param option The name of the option
     */
    private void doShowOption(final FrameContainer origin,
            final boolean isSilent, final ConfigManager manager,
            final String domain, final String option) {
        if (manager.hasOptionString(domain, option)) {
            sendLine(origin, isSilent, FORMAT_OUTPUT, "The current value of "
                    + domain + "." + option + " is: " + manager.getOption(domain, option));
        } else {
            sendLine(origin, isSilent, FORMAT_ERROR, "Option not found: " + domain + "." + option);
        }
    }

    /**
     * Sets the value of the specified option.
     *
     * @param origin The window the command was issued from
     * @param isSilent Whether or not the command is being silenced or not
     * @param identity The identity to use to set data
     * @param domain The domain of the option
     * @param option The name of the option
     * @param newvalue The value the option should be set to
     */
    private void doSetOption(final FrameContainer origin,
            final boolean isSilent, final Identity identity,
            final String domain, final String option, final String newvalue) {
        identity.setOption(domain, option, newvalue);

        sendLine(origin, isSilent, FORMAT_OUTPUT, domain + "." + option
                + " has been set to: " + newvalue);
    }

    /**
     * Appends data to the specified option.
     *
     * @param origin The window the command was issued from
     * @param isSilent Whether or not the command is being silenced or not
     * @param identity The identity to use to set data
     * @param manager The config manager to use to retrieve data
     * @param domain The domain of the option
     * @param option The name of the option
     * @param data The data to be appended
     */
    private void doAppendOption(final FrameContainer origin,
            final boolean isSilent, final Identity identity, final ConfigManager manager,
            final String domain, final String option, final String data) {
        doSetOption(origin, isSilent, identity, domain, option,
                (manager.hasOptionString(domain, option)
                ? manager.getOption(domain, option) : "") + data);
    }

    /**
     * Unsets the specified option.
     *
     * @param origin The window the command was issued from
     * @param isSilent Whether or not the command is being silenced or not
     * @param identity The identity to use to set data
     * @param domain The domain of the option
     * @param option The name of the option
     */
    private void doUnsetOption(final FrameContainer origin,
            final boolean isSilent, final Identity identity, final String domain,
            final String option) {
        identity.unsetOption(domain, option);

        sendLine(origin, isSilent, FORMAT_OUTPUT, domain + "." + option + " has been unset.");
    }

    /** {@inheritDoc} */
    @Override
    public AdditionalTabTargets getSuggestions(final int arg,
            final IntelligentCommandContext context) {
        final List<String> previousArgs = context.getPreviousArgs();
        final AdditionalTabTargets res = new AdditionalTabTargets();

        if (arg == 0) {
            res.addAll(context.getWindow().getContainer().getConfigManager()
                    .getDomains());
            res.add("--unset");
            res.add("--append");
            res.add("--server");
            res.add("--channel");
            res.excludeAll();
        } else if (arg == 1 && previousArgs.size() >= 1) {
            if (previousArgs.get(0).equalsIgnoreCase("--unset")
                    || previousArgs.get(0).equalsIgnoreCase("--append")
                    || previousArgs.get(0).equalsIgnoreCase("--server")
                    || previousArgs.get(0).equalsIgnoreCase("--channel")) {
                res.addAll(context.getWindow().getContainer().getConfigManager()
                        .getDomains());
            } else {
                res.addAll(context.getWindow().getContainer().getConfigManager()
                        .getOptions(previousArgs.get(0)).keySet());
            }
            res.excludeAll();
        } else if (arg == 2 && (previousArgs.get(0).equalsIgnoreCase("--unset")
                || previousArgs.get(0).equalsIgnoreCase("--append")
                || previousArgs.get(0).equalsIgnoreCase("--server")
                || previousArgs.get(0).equalsIgnoreCase("--channel"))) {
            res.addAll(context.getWindow().getContainer().getConfigManager()
                    .getOptions(previousArgs.get(1)).keySet());
            res.excludeAll();
        }

        return res;
    }

}
