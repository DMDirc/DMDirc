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

import com.dmdirc.Config;
import com.dmdirc.commandparser.CommandManager;
import com.dmdirc.commandparser.CommandWindow;
import com.dmdirc.commandparser.GlobalCommand;

/**
 * The set command allows the user to inspect and change global config settings.
 * @author chris
 */
public final class Set extends GlobalCommand {
    
    /**
     * Creates a new instance of Set.
     */
    public Set() {
        super();
        
        CommandManager.registerCommand(this);
    }
    
    /** {@inheritDoc} */
    public void execute(final CommandWindow origin, final boolean isSilent,
            final String... args) {
        switch (args.length) {
            case 0:
                doDomainList(origin);
                break;
            case 1:
                doOptionsList(origin, args[0]);
                break;
            case 2:
                doShowOption(origin, args[0], args[1]);
                break;
            default:
                doSetOption(origin, args[0], args[1], implodeArgs(2, args));
        }
    }
    
    /**
     * Shows the user a list of valid domains.
     * @param origin The window the command was issued from
     */
    private void doDomainList(final CommandWindow origin) {
        final StringBuffer output = new StringBuffer(67);
        
        output.append("Valid domains (use ");
        output.append(Config.getCommandChar());
        output.append("set <domain> to see options within a domain): ");
        
        for (String domain : Config.getDomains()) {
            output.append(domain);
            output.append(", ");
        }
        
        origin.addLine("commandOutput", output.substring(0, output.length() - 2));
    }
    
    /**
     * Shows the user a list of valid options within a domain.
     * @param origin The window the command was issued from
     * @param domain The domain to be inspected
     */
    private void doOptionsList(final CommandWindow origin, final String domain) {
        final StringBuffer output = new StringBuffer(24);
        
        output.append("Options in domain '");
        output.append(domain);
        output.append("': ");
        
        boolean found = false;
        
        for (String option : Config.getOptions(domain)) {
            output.append(option);
            output.append(", ");
            found = true;
        }
        
        if (found) {
            origin.addLine("commandOutput", output.substring(0, output.length() - 2));
        } else {
            origin.addLine("commandError", "There are no options in the domain '" + domain + "'.");
        }
    }
    
    /**
     * Shows the user the current value of one option.
     * @param origin The window the command was issued from
     * @param domain The domain of the option
     * @param option The name of the option
     */
    private void doShowOption(final CommandWindow origin, final String domain,
            final String option) {
        if (Config.hasOption(domain, option)) {
            origin.addLine("commandOutput", "The current value of " + domain + "." + option
                    + " is: " + Config.getOption(domain, option));
        } else {
            origin.addLine("commandError", "Option not found: " + domain + "." + option);
        }
    }
    
    /**
     * Sets the value of the specified option.
     * @param origin The window the command was issued from
     * @param domain The domain of the option
     * @param option The name of the option
     * @param newvalue The value the option should be set to
     */
    private void doSetOption(final CommandWindow origin, final String domain,
            final String option, final String newvalue) {
        Config.setOption(domain, option, newvalue);
        
        origin.addLine("commandOutput", domain + "." + option + " has been set to: " + newvalue);
    }
    
    /** {@inheritDoc}. */
    public String getName() {
        return "set";
    }
    
    /** {@inheritDoc}. */
    public boolean showInHelp() {
        return true;
    }
    
    /** {@inheritDoc}. */
    public boolean isPolyadic() {
        return true;
    }
    
    /** {@inheritDoc}. */
    public int getArity() {
        return 0;
    }
    
    /** {@inheritDoc}. */
    public String getHelp() {
        return "set [domain [option [newvalue]]] - inspect or change configuration settings";
    }
    
}
