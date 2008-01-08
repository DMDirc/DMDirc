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

package com.dmdirc.addons.nowplaying;

import com.dmdirc.Main;
import com.dmdirc.actions.ActionManager;
import com.dmdirc.actions.interfaces.ActionType;
import com.dmdirc.actions.CoreActionType;
import com.dmdirc.addons.nowplaying.MediaSource;
import com.dmdirc.addons.nowplaying.MediaSourceManager;
import com.dmdirc.commandparser.CommandManager;
import com.dmdirc.config.IdentityManager;
import com.dmdirc.interfaces.ActionListener;
import com.dmdirc.plugins.Plugin;
import com.dmdirc.plugins.PluginInfo;
import com.dmdirc.plugins.PluginManager;
import com.dmdirc.ui.interfaces.PreferencesInterface;
import com.dmdirc.ui.interfaces.PreferencesPanel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

public class NowPlayingPlugin extends Plugin implements ActionListener,
        PreferencesInterface  {
    
    /** Config domain. */
    private static final String DOMAIN = "plugin-nowplaying";
    
    /** The sources that we know of. */
    private final List<MediaSource> sources = new ArrayList<MediaSource>();
    
    /** The now playing command we're registering. */
    private NowPlayingCommand command;
    
    /** Config panel. */
    private ConfigPanel configPanel;
    
    /** The user's preferred order for source usage. */
    private List<String> order;
    
    public NowPlayingPlugin() {
        super();
    }
    
    /** {@inheritDoc} */
    public void onLoad() {
        sources.clear();
        
        loadSettings();
        
        ActionManager.addListener(this, CoreActionType.PLUGIN_LOADED,
                CoreActionType.PLUGIN_UNLOADED);
        
        for (PluginInfo target : PluginManager.getPluginManager().getPluginInfos()) {
            if (target.isLoaded()) {
                addPlugin(target);
            }
        }
        
        command = new NowPlayingCommand(this);
    }
    
    /** {@inheritDoc} */
    public void onUnload() {
        sources.clear();
        
        ActionManager.removeListener(this);
        
        CommandManager.unregisterCommand(command);
    }
    
    /** {@inheritDoc} */
    public boolean isConfigurable() {
        return true;
    }
    
    /** {@inheritDoc} */
    public void showConfig() {
        final PreferencesPanel preferencesPanel = Main.getUI().getPreferencesPanel(this, "Now playing Plugin - Config");
        
        configPanel = new ConfigPanel(order);
        
        preferencesPanel.addCategory("General", "General options for the plugin.");
        
        preferencesPanel.replaceOptionPanel("General", configPanel);
        
        preferencesPanel.display();
    }
    
    /** {@inheritDoc} */
    public void configClosed(final Properties properties) {
        order = configPanel.getSources();
        
        saveSettings();
    }
    
    /** {@inheritDoc} */
    public void configCancelled() {
        //Ignore
    }
    
    /** Saves the plugins settings. */
    private void saveSettings() {
        IdentityManager.getConfigIdentity().setOption(DOMAIN, "sourceOrder", order);
    }
    
    /** Loads the plugins settings. */
    private void loadSettings() {
        if (IdentityManager.getGlobalConfig().hasOption(DOMAIN, "sourceOrder")) {
            order = IdentityManager.getGlobalConfig().getOptionList(DOMAIN, "sourceOrder");
        } else {
            order = new ArrayList<String>();
        }
    }
    
    /** {@inheritDoc} */
    public void processEvent(final ActionType type, final StringBuffer format,
            final Object... arguments) {
        if (type == CoreActionType.PLUGIN_LOADED) {
            addPlugin((PluginInfo) arguments[0]);
        } else if (type == CoreActionType.PLUGIN_UNLOADED) {
            removePlugin((PluginInfo) arguments[0]);
        }
    }
    
    /**
     * Checks to see if a plugin implements one of the Media Source interfaces
     * and if it does, adds the source(s) to our list.
     *
     * @param target The plugin to be tested
     */
    private void addPlugin(final PluginInfo target) {
        final Plugin targetPlugin = target.getPlugin();
        if (targetPlugin instanceof MediaSource) {
            sources.add((MediaSource) targetPlugin);
            addSourceToOrder((MediaSource) targetPlugin);
        }
        
        if (targetPlugin instanceof MediaSourceManager) {
            sources.addAll(((MediaSourceManager) targetPlugin).getSources());
            
            for (MediaSource source : ((MediaSourceManager) targetPlugin).getSources()) {
                addSourceToOrder(source);
            }
        }
    }
    
    /**
     * Checks to see if the specified media source needs to be added to our
     * order list, and adds it if neccessary.
     *
     * @param source The media source to be tested
     */
    private void addSourceToOrder(final MediaSource source) {
        if (!order.contains(source.getAppName())) {
            order.add(source.getAppName());
        }
    }
    
    /**
     * Checks to see if a plugin implements one of the Media Source interfaces
     * and if it does, removes the source(s) from our list.
     *
     * @param target The plugin to be tested
     */
    private void removePlugin(final PluginInfo target) {
        final Plugin targetPlugin = target.getPlugin();
        if (targetPlugin instanceof MediaSource) {
            sources.remove(targetPlugin);
        }
        
        if (targetPlugin instanceof MediaSourceManager) {
            sources.removeAll(((MediaSourceManager) targetPlugin).getSources());
        }
    }
    
    /**
     * Determines if there are any valid sources (paused or not).
     *
     * @return True if there are running sources, false otherwise
     */
    public boolean hasRunningSource() {
        for (final MediaSource source : sources) {
            if (source.isRunning()) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Retrieves the "best" source to use for displaying media information.
     * The best source is defined as the earliest in the list that is running
     * and not paused, or, if no such source exists, the earliest in the list
     * that is running and paused. If neither condition is satisified returns
     * null.
     *
     * @return The best source to use for media info
     */
    public MediaSource getBestSource() {
        MediaSource paused = null;
        
        Collections.sort(sources, new MediaSourceComparator(order));
        
        for (final MediaSource source : sources) {
            if (source.isRunning()) {
                if (source.isPlaying()) {
                    return source;
                } else if (paused == null) {
                    paused = source;
                }
            }
        }
        
        return paused;
    }
    
    /**
     * Retrieves a source based on its name.
     *
     * @param name The name to search for
     * @return The source with the specified name or null if none were found.
     */
    public MediaSource getSource(final String name) {
        for (final MediaSource source : sources) {
            if (source.getAppName().equalsIgnoreCase(name)) {
                return source;
            }
        }
        
        return null;
    }
    
    /**
     * Retrieves all the sources registered with this plugin.
     *
     * @return All known media sources
     */
    public List<MediaSource> getSources() {
        return new ArrayList<MediaSource>(sources);
    }
}