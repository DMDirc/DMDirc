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
import com.dmdirc.commandparser.GlobalCommand;
import com.dmdirc.commandparser.GlobalCommandParser;
import com.dmdirc.commandparser.IntelligentCommand;
import com.dmdirc.plugins.Plugin;
import com.dmdirc.plugins.PluginManager;
import com.dmdirc.ui.input.AdditionalTabTargets;
import com.dmdirc.ui.interfaces.InputWindow;

import java.util.Arrays;
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
    public void execute(final InputWindow origin, final boolean isSilent,
            final String... args) {
        if (args.length <= 1) {
            sendLine(origin, isSilent, "commandUsage", Config.getCommandChar(),
                    "ifplugin", "<[!]plugin> <command>");
            return;
        }
        
        final boolean negative = args[0].charAt(0) == '!';
        
        final String pname = args[0].substring(negative ? 1 : 0);
        
        final Plugin plugin = PluginManager.getPluginManager().getPlugin(pname);
        
        boolean result = true;
        
        if (plugin == null || !plugin.isActive()) {
            result = false;
        }
        
        if (result != negative) {
            if (origin == null) {
                GlobalCommandParser.getGlobalCommandParser().parseCommand(null, implodeArgs(1, args));
            } else {
                origin.getCommandParser().parseCommand(origin, implodeArgs(1, args));
            }
        }
    }
    
    /** {@inheritDoc}. */
    public String getName() {
        return "ifplugin";
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
        return "ifplugin <[!]plugin> <command> - executes a command if the specified plugin is/isn't loaded";
    }

    /** {@inheritDoc} */
    public AdditionalTabTargets getSuggestions(final int arg, final List<String> previousArgs) {
        final AdditionalTabTargets res = new AdditionalTabTargets();
        
        if (arg == 0) {
            res.setIncludeNormal(false);

            for (Plugin possPlugin : PluginManager.getPluginManager().getPossiblePlugins()) {
                res.add(possPlugin.getClass().getCanonicalName());
                res.add("!" + possPlugin.getClass().getCanonicalName());
            }            
        }
        
        return res;
    }    
    
}
