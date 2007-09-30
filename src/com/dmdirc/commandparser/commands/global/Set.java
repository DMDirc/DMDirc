/*
 * Copyright (c) 2006-2007 Chris Smith, Shane Mc Cormack, Gregory Holmes
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

import com.dmdirc.commandparser.CommandManager;
import com.dmdirc.commandparser.commands.GlobalCommand;
import com.dmdirc.commandparser.commands.IntelligentCommand;
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
    public void execute(final InputWindow origin, final boolean isSilent,
            final String... args) {
        switch (args.length) {
        case 0:
            doDomainList(origin, isSilent);
            break;
        case 1:
            doOptionsList(origin, isSilent, args[0]);
            break;
        case 2:
            doShowOption(origin, isSilent, args[0], args[1]);
            break;
        default:
            if (args[0].equalsIgnoreCase("--unset")) {
                doUnsetOption(origin, isSilent, args[1], args[2]);
            } else {
                doSetOption(origin, isSilent, args[0], args[1], implodeArgs(2, args));
            }
        }
    }
    
    /**
     * Shows the user a list of valid domains.
     *
     * @param origin The window the command was issued from
     * @param isSilent Whether or not the command is being silenced or not
     */
    private void doDomainList(final InputWindow origin, final boolean isSilent) {
        final StringBuffer output = new StringBuffer(67);
        
        output.append("Valid domains (use ");
        output.append(CommandManager.getCommandChar());
        output.append("set <domain> to see options within a domain): ");
        
        for (String domain : IdentityManager.getGlobalConfig().getDomains()) {
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
     * @param domain The domain to be inspected
     */
    private void doOptionsList(final InputWindow origin,
            final boolean isSilent, final String domain) {
        final StringBuffer output = new StringBuffer(24);
        
        output.append("Options in domain '");
        output.append(domain);
        output.append("': ");
        
        boolean found = false;
        
        for (String option : IdentityManager.getGlobalConfig().getOptions(domain)) {
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
     * @param domain The domain of the option
     * @param option The name of the option
     */
    private void doShowOption(final InputWindow origin,
            final boolean isSilent, final String domain, final String option) {
        if (IdentityManager.getGlobalConfig().hasOption(domain, option)) {
            sendLine(origin, isSilent, FORMAT_OUTPUT, "The current value of " + domain + "." + option
                    + " is: " + IdentityManager.getGlobalConfig().getOption(domain, option));
        } else {
            sendLine(origin, isSilent, FORMAT_ERROR, "Option not found: " + domain + "." + option);
        }
    }
    
    /**
     * Sets the value of the specified option.
     *
     * @param origin The window the command was issued from
     * @param isSilent Whether or not the command is being silenced or not
     * @param domain The domain of the option
     * @param option The name of the option
     * @param newvalue The value the option should be set to
     */
    private void doSetOption(final InputWindow origin,
            final boolean isSilent, final String domain, final String option,
            final String newvalue) {
        IdentityManager.getConfigIdentity().setOption(domain, option, newvalue);
        
        sendLine(origin, isSilent, FORMAT_OUTPUT, domain + "." + option + " has been set to: " + newvalue);
    }
    
    /**
     * Unsets the specified option.
     *
     * @param origin The window the command was issued from
     * @param isSilent Whether or not the command is being silenced or not
     * @param domain The domain of the option
     * @param option The name of the option
     */
    private void doUnsetOption(final InputWindow origin,
            final boolean isSilent, final String domain, final String option) {
        IdentityManager.getConfigIdentity().unsetOption(domain, option);
        
        sendLine(origin, isSilent, FORMAT_OUTPUT, domain + "." + option + " has been unset.");
    }
    
    /** {@inheritDoc} */
    public String getName() {
        return "set";
    }
    
    /** {@inheritDoc} */
    public boolean showInHelp() {
        return true;
    }
    
    /** {@inheritDoc} */
    public boolean isPolyadic() {
        return true;
    }
    
    /** {@inheritDoc} */
    public int getArity() {
        return 0;
    }
    
    /** {@inheritDoc} */
    public String getHelp() {
        return "set [domain [option [newvalue]]] - inspect or change configuration settings"
                + "\nset --unset <domain> <option> - unset the specified option";
    }
    
    /** {@inheritDoc} */
    public AdditionalTabTargets getSuggestions(final int arg, final List<String> previousArgs) {
        final AdditionalTabTargets res = new AdditionalTabTargets();
        
        if (arg == 0) {
            res.addAll(IdentityManager.getGlobalConfig().getDomains());
            res.add("--unset");
            res.setIncludeNormal(false);
        } else if (arg == 1 && previousArgs.size() >= 1) {
            if (previousArgs.get(0).equalsIgnoreCase("--unset")) {
                res.addAll(IdentityManager.getGlobalConfig().getDomains());
            } else {
                res.addAll(IdentityManager.getGlobalConfig().getOptions(previousArgs.get(0)));
            }
            res.setIncludeNormal(false);
        } else if (arg == 2 && previousArgs.get(0).equalsIgnoreCase("--unset")) {
            res.addAll(IdentityManager.getGlobalConfig().getOptions(previousArgs.get(1)));
            res.setIncludeNormal(false);
        }
        
        return res;
    }
    
}
