/*
 * Copyright (c) 2006-2009 Chris Smith, Shane Mc Cormack, Gregory Holmes
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

import com.dmdirc.commandparser.CommandArguments;
import com.dmdirc.commandparser.CommandManager;
import com.dmdirc.commandparser.commands.GlobalCommand;
import com.dmdirc.commandparser.commands.IntelligentCommand;
import com.dmdirc.config.ConfigManager;
import com.dmdirc.config.Identity;
import com.dmdirc.config.IdentityManager;
import com.dmdirc.ui.input.AdditionalTabTargets;
import com.dmdirc.ui.interfaces.InputWindow;

import java.util.List;

/**
 * The set command allows the user to inspect and change global config settings.
 *
 * @author chris
 */
public final class Set extends GlobalCommand implements IntelligentCommand {
    
    /**
     * Creates a new instance of Set.
     */
    public Set() {
        super();
        
        CommandManager.registerCommand(this);
    }
    
    /** {@inheritDoc} */
    @Override
    public void execute(final InputWindow origin, final boolean isSilent,
            final CommandArguments args) {
        int i = 0;
        
        Identity identity = IdentityManager.getConfigIdentity();
        ConfigManager manager = IdentityManager.getGlobalConfig();
        
        if (args.getArguments().length > 0
                && "--server".equalsIgnoreCase(args.getArguments()[0]) && origin != null
                && origin.getContainer().getServer() != null) {
            i = 1;
            identity = origin.getContainer().getServer().getServerIdentity();
            manager = origin.getContainer().getServer().getConfigManager();
        }
        
        switch (args.getArguments().length - i) {
        case 0:
            doDomainList(origin, isSilent, manager);
            break;
        case 1:
            doOptionsList(origin, isSilent, manager, args.getArguments()[i]);
            break;
        case 2:
            doShowOption(origin, isSilent, manager, args.getArguments()[i],
                    args.getArguments()[1 + i]);
            break;
        default:
            if (args.getArguments()[i].equalsIgnoreCase("--unset")) {
                doUnsetOption(origin, isSilent, identity, args.getArguments()[1 + i],
                        args.getArguments()[2 + i]);
            } else if (args.getArguments()[i].equalsIgnoreCase("--append")
                    && args.getArguments().length > 3 + i) {
                doAppendOption(origin, isSilent, identity, manager, 
                        args.getArguments()[1 + i], args.getArguments()[2 + i],
                        args.getArgumentsAsString(3 + i));
            } else {
                doSetOption(origin, isSilent, identity, args.getArguments()[i],
                        args.getArguments()[1 + i], args.getArgumentsAsString(2 + i));
            }
        }
    }
    
    /**
     * Shows the user a list of valid domains.
     *
     * @param origin The window the command was issued from
     * @param isSilent Whether or not the command is being silenced or not
     * @param manager The config manager to use to retrieve data
     */
    private void doDomainList(final InputWindow origin, final boolean isSilent,
            final ConfigManager manager) {
        final StringBuffer output = new StringBuffer(67);
        
        output.append("Valid domains (use ");
        output.append(CommandManager.getCommandChar());
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
    private void doOptionsList(final InputWindow origin,
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
    private void doShowOption(final InputWindow origin,
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
    private void doSetOption(final InputWindow origin,
            final boolean isSilent, final Identity identity,
            final String domain, final String option, final String newvalue) {
        identity.setOption(domain, option, newvalue);
        
        sendLine(origin, isSilent, FORMAT_OUTPUT, domain + "." + option +
                " has been set to: " + newvalue);
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
    private void doAppendOption(final InputWindow origin,
            final boolean isSilent, final Identity identity, final ConfigManager manager,
            final String domain,final String option, final String data) {
        doSetOption(origin, isSilent, identity, domain, option,
                (manager.hasOptionString(domain, option) ?
                    manager.getOption(domain, option) : "") + data);
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
    private void doUnsetOption(final InputWindow origin,
            final boolean isSilent, final Identity identity, final String domain,
            final String option) {
        identity.unsetOption(domain, option);
        
        sendLine(origin, isSilent, FORMAT_OUTPUT, domain + "." + option + " has been unset.");
    }
    
    /** {@inheritDoc} */
    @Override
    public String getName() {
        return "set";
    }
    
    /** {@inheritDoc} */
    @Override
    public boolean showInHelp() {
        return true;
    }
    
    /** {@inheritDoc} */
    @Override
    public String getHelp() {
        return "set [--server] [domain [option [newvalue]]] - inspect or change configuration settings"
                + "\nset [--server] --append <domain> <option> <data> - appends data to the specified option"
                + "\nset [--server] --unset <domain> <option> - unsets the specified option";
    }
    
    /** {@inheritDoc} */
    @Override
    public AdditionalTabTargets getSuggestions(final int arg, final List<String> previousArgs) {
        final AdditionalTabTargets res = new AdditionalTabTargets();
        
        if (arg == 0) {
            res.addAll(IdentityManager.getGlobalConfig().getDomains());
            res.add("--unset");
            res.add("--append");
            res.add("--server");
            res.excludeAll();
        } else if (arg == 1 && previousArgs.size() >= 1) {
            if (previousArgs.get(0).equalsIgnoreCase("--unset")
                    || previousArgs.get(0).equalsIgnoreCase("--append")
                    || previousArgs.get(0).equalsIgnoreCase("--server")) {
                res.addAll(IdentityManager.getGlobalConfig().getDomains());
            } else {
                res.addAll(IdentityManager.getGlobalConfig().getOptions(previousArgs.get(0)).keySet());
            }
            res.excludeAll();
        } else if (arg == 2 && (previousArgs.get(0).equalsIgnoreCase("--unset")
                || previousArgs.get(0).equalsIgnoreCase("--append")
                || previousArgs.get(0).equalsIgnoreCase("--server"))) {
            res.addAll(IdentityManager.getGlobalConfig().getOptions(previousArgs.get(1)).keySet());
            res.excludeAll();
        }
        
        return res;
    }
    
}
