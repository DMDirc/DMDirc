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
import com.dmdirc.commandparser.GlobalCommand;
import com.dmdirc.commandparser.IntelligentCommand;
import com.dmdirc.plugins.PluginManager;
import com.dmdirc.ui.input.AdditionalTabTargets;
import com.dmdirc.ui.interfaces.InputWindow;

import java.util.Arrays;
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
        if (PluginManager.getPluginManager().addPlugin(args[0])) {
            PluginManager.getPluginManager().getPlugin(args[0]).setActive(true);
            sendLine(origin, isSilent, "commandOutput", "Plugin loaded.");
        } else {
            sendLine(origin, isSilent, "commandError", "Plugin Loading failed");
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
    public boolean isPolyadic() {
        return false;
    }
    
    /** {@inheritDoc} */
    public int getArity() {
        return 1;
    }
    
    /** {@inheritDoc} */
    public String getHelp() {
        return "loadplugin <class> - loads the specified class as a plugin";
    }

    /** {@inheritDoc} */
    public AdditionalTabTargets getSuggestions(int arg, List<String> previousArgs) {
        final AdditionalTabTargets res = new AdditionalTabTargets();
        
        if (arg == 0) {
            res.setIncludeNormal(false);
            res.addAll(Arrays.asList(PluginManager.getPluginManager().getNames()));
        }
        
        return res;
    }
    
}
