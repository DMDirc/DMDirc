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
import com.dmdirc.commandparser.parsers.GlobalCommandParser;
import com.dmdirc.commandparser.commands.IntelligentCommand;
import com.dmdirc.plugins.PluginInfo;
import com.dmdirc.plugins.PluginManager;
import com.dmdirc.ui.input.AdditionalTabTargets;
import com.dmdirc.ui.input.TabCompleter;
import com.dmdirc.ui.interfaces.InputWindow;

import java.util.List;

/**
 * The if plugin command allows the user to execute commands based on whether
 * or not a plugin is loaded.
 *
 * @author chris
 */
public final class Ifplugin extends GlobalCommand implements IntelligentCommand {
    
    /**
     * Creates a new instance of Ifplugin.
     */
    public Ifplugin() {
        super();
        
        CommandManager.registerCommand(this);
    }
    
    /** {@inheritDoc} */
    @Override
    public void execute(final InputWindow origin, final boolean isSilent,
            final CommandArguments args) {
        if (args.getArguments().length <= 1) {
            showUsage(origin, isSilent, "ifplugin", "<[!]plugin> <command>");
            return;
        }
        
        final boolean negative = args.getArguments()[0].charAt(0) == '!';
        
        final String pname = args.getArguments()[0].substring(negative ? 1 : 0);
        
        final PluginInfo pluginInfo = PluginManager.getPluginManager().getPluginInfoByName(pname);
        
        boolean result = true;
        
        if (pluginInfo == null || !pluginInfo.isLoaded()) {
            result = false;
        }
        
        if (result != negative) {
            if (origin == null) {
                GlobalCommandParser.getGlobalCommandParser().parseCommand(null,
                        args.getArgumentsAsString(1));
            } else {
                origin.getCommandParser().parseCommand(origin, args.getArgumentsAsString(1));
            }
        }
    }
    
    /** {@inheritDoc} */
    @Override
    public String getName() {
        return "ifplugin";
    }
    
    /** {@inheritDoc} */
    @Override
    public boolean showInHelp() {
        return true;
    }
    
    /** {@inheritDoc} */
    @Override
    public String getHelp() {
        return "ifplugin <[!]plugin> <command> - executes a command if the " +
                "specified plugin is/isn't loaded";
    }

    /** {@inheritDoc} */
    @Override
    public AdditionalTabTargets getSuggestions(final int arg, final List<String> previousArgs) {
        AdditionalTabTargets res;
        
        if (arg == 0) {
            res = new AdditionalTabTargets().excludeAll();

            for (PluginInfo possPlugin
                    : PluginManager.getPluginManager().getPluginInfos()) {
                res.add(possPlugin.getName());
                res.add("!" + possPlugin.getName());
            }            
        } else {
            res = TabCompleter.getIntelligentResults(arg, previousArgs, 1);
        }
        
        return res;
    }    
    
}
