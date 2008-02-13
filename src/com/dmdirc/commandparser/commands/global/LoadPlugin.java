/*
 * Copyright (c) 2006-2008 Chris Smith, Shane Mc Cormack, Gregory Holmes
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
import com.dmdirc.plugins.PluginInfo;
import com.dmdirc.plugins.PluginManager;
import com.dmdirc.ui.input.AdditionalTabTargets;
import com.dmdirc.ui.interfaces.InputWindow;

import java.util.List;

/**
 * Allows the user to load a plugin.
 * @author chris
 */
public final class LoadPlugin extends GlobalCommand implements IntelligentCommand {
    
    /**
     * Creates a new instance of LoadPlugin.
     */
    public LoadPlugin() {
        super();
        
        CommandManager.registerCommand(this);
    }
    
    /** {@inheritDoc} */
    public void execute(final InputWindow origin, final boolean isSilent,
            final String... args) {
        if (args.length == 0) {
            showUsage(origin, isSilent, "loadplugin", "<plugin>");
            return;
        }
        
        if (PluginManager.getPluginManager().addPlugin(args[0])) {
            PluginManager.getPluginManager().getPluginInfo(args[0]).loadPlugin();
            sendLine(origin, isSilent, FORMAT_OUTPUT, "Plugin loaded.");
        } else {
            PluginInfo plugin = PluginManager.getPluginManager().getPluginInfo(args[0]);
            if (plugin == null) {
                sendLine(origin, isSilent, FORMAT_ERROR, "Plugin Loading failed");
            } else {
                if (!plugin.isLoaded()) {
                    PluginManager.getPluginManager().getPluginInfo(args[0]).loadPlugin();
                    sendLine(origin, isSilent, FORMAT_OUTPUT, "Plugin loaded.");
                } else {
                    sendLine(origin, isSilent, FORMAT_OUTPUT, "Plugin already loaded.");
                }
            }
        }
    }
    
    
    /** {@inheritDoc} */
    public String getName() {
        return "loadplugin";
    }
    
    /** {@inheritDoc} */
    public boolean showInHelp() {
        return true;
    }
    
    /** {@inheritDoc} */
    public String getHelp() {
        return "loadplugin <plugin> - loads the specified class as a plugin";
    }
    
    /** {@inheritDoc} */
    public AdditionalTabTargets getSuggestions(final int arg, final List<String> previousArgs) {
        final AdditionalTabTargets res = new AdditionalTabTargets();
        
        if (arg == 0) {
            res.excludeAll();
            
            for (PluginInfo possPlugin : PluginManager.getPluginManager().getPossiblePluginInfos(false)) {
                res.add(possPlugin.getFilename());
            }
        }
        
        return res;
    }
    
}
