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
import com.dmdirc.plugins.Plugin;
import com.dmdirc.plugins.PluginManager;
import com.dmdirc.ui.InputWindow;

/**
 * Allows the user to reload a plugin.
 * @author chris
 */
public final class ReloadPlugin extends GlobalCommand {
    
    /**
     * Creates a new instance of ReloadPlugin.
     */
    public ReloadPlugin() {
        super();
        
        CommandManager.registerCommand(this);
    }
    
    /** {@inheritDoc} */
    public void execute(final InputWindow origin, final boolean isSilent,
            final String... args) {
        Plugin plugin = PluginManager.getPluginManager().getPlugin(args[0]);
        if (plugin == null) {
            sendLine(origin, isSilent, "commandError", "Plugin Reloading failed - Plugin not loaded");
        } else {
            final boolean isActive = plugin.isActive();
            plugin = null;
            if (PluginManager.getPluginManager().reloadPlugin(args[0])) {
                sendLine(origin, isSilent, "commandOutput", "Plugin Reloaded.");
                PluginManager.getPluginManager().getPlugin(args[0]).setActive(isActive);
            } else {
                sendLine(origin, isSilent, "commandError", "Plugin Reloading failed");
            }
            
        }
    }
    
    
    /** {@inheritDoc}. */
    public String getName() {
        return "reloadplugin";
    }
    
    /** {@inheritDoc}. */
    public boolean showInHelp() {
        return true;
    }
    
    /** {@inheritDoc}. */
    public boolean isPolyadic() {
        return false;
    }
    
    /** {@inheritDoc}. */
    public int getArity() {
        return 1;
    }
    
    /** {@inheritDoc}. */
    public String getHelp() {
        return "Reloadplugin <class> - Reloads the specified plugin";
    }
    
}
