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

package com.dmdirc.addons.lagdisplay;

import com.dmdirc.FrameContainer;
import com.dmdirc.Main;
import com.dmdirc.Server;
import com.dmdirc.ServerState;
import com.dmdirc.actions.ActionManager;
import com.dmdirc.actions.interfaces.ActionType;
import com.dmdirc.actions.CoreActionType;
import com.dmdirc.config.ConfigManager;
import com.dmdirc.config.IdentityManager;
import com.dmdirc.config.prefs.PreferencesCategory;
import com.dmdirc.config.prefs.PreferencesManager;
import com.dmdirc.config.prefs.PreferencesSetting;
import com.dmdirc.config.prefs.PreferencesType;
import com.dmdirc.interfaces.ActionListener;
import com.dmdirc.interfaces.ConfigChangeListener;
import com.dmdirc.plugins.Plugin;
import com.dmdirc.ui.interfaces.Window;
import com.dmdirc.util.RollingList;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * Displays the current server's lag in the status bar.
 * @author chris
 */
public final class LagDisplayPlugin extends Plugin implements ActionListener, ConfigChangeListener {
    
    /** The panel we use in the status bar. */
    private final LagDisplayPanel panel = new LagDisplayPanel(this);
    
    /** A cache of ping times. */
    private final Map<Server, String> pings = new WeakHashMap<Server, String>();

    /** Ping history. */
    private final Map<Server, RollingList<Long>> history
            = new HashMap<Server, RollingList<Long>>();

    /** Whether or not to show a graph in the info popup. */
    private boolean showGraph = true;

    /** Whether or not to show labels on that graph. */
    private boolean showLabels = true;

    /** The length of history to keep per-server. */
    private int historySize = 100;
    
    /** Creates a new instance of LagDisplayPlugin. */
    public LagDisplayPlugin() {
        super();
    }
    
    /** {@inheritDoc} */
    @Override
    public void onLoad() {
        Main.getUI().getStatusBar().addComponent(panel);
        IdentityManager.getGlobalConfig().addChangeListener(getDomain(), this);

        readConfig();
        
        ActionManager.addListener(this, CoreActionType.SERVER_GOTPING,
                CoreActionType.SERVER_NOPING, CoreActionType.CLIENT_FRAME_CHANGED,
                CoreActionType.SERVER_DISCONNECTED, CoreActionType.SERVER_PINGSENT,
                CoreActionType.SERVER_NUMERIC);
    }

    /**
     * Reads the plugin's global configuration settings.
     */
    protected void readConfig() {
        final ConfigManager manager = IdentityManager.getGlobalConfig();
        showGraph = manager.getOptionBool(getDomain(), "graph");
        showLabels = manager.getOptionBool(getDomain(), "labels");
        historySize = manager.getOptionInt(getDomain(), "history");
    }

    /**
     * Retrieves the history of the specified server. If there is no history,
     * a new list is added to the history map and returned.
     * 
     * @param server The server whose history is being requested
     * @return The history for the specified server
     */
    protected RollingList<Long> getHistory(final Server server) {
        if (!history.containsKey(server)) {
            history.put(server, new RollingList<Long>(historySize));
        }

        return history.get(server);
    }

    /**
     * Determines if the {@link ServerInfoDialog} should show a graph of the
     * ping time for the current server.
     * 
     * @return True if a graph should be shown, false otherwise
     */
    public boolean shouldShowGraph() {
        return showGraph;
    }

    /**
     * Determines if the {@link PingHistoryPanel} should show labels on selected
     * points.
     *
     * @return True if labels should be shown, false otherwise
     */
    public boolean shouldShowLabels() {
        return showLabels;
    }
    
    /** {@inheritDoc} */
    @Override
    public void onUnload() {
        Main.getUI().getStatusBar().removeComponent(panel);
        IdentityManager.getConfigIdentity().removeListener(this);
        
        ActionManager.removeListener(this);
    }
    
    /** {@inheritDoc} */
    @Override
    public void processEvent(final ActionType type, final StringBuffer format,
            final Object... arguments) {
        boolean useAlternate = false;

        for (Object obj : arguments) {
            if (obj instanceof FrameContainer && ((FrameContainer) obj).getServer() != null) {
                useAlternate = ((FrameContainer) obj).getServer().getConfigManager()
                        .getOptionBool(getDomain(), "usealternate");
                break;
            }
        }

        if (!useAlternate && type.equals(CoreActionType.SERVER_GOTPING)) {
            final Window active = Main.getUI().getActiveWindow();
            final String value = formatTime(arguments[1]);

            getHistory(((Server) arguments[0])).add((Long) arguments[1]);
            pings.put(((Server) arguments[0]), value);
            
            if (((Server) arguments[0]).ownsFrame(active)) {
                panel.setText(value);
            }

            panel.refreshDialog();
        } else if (!useAlternate && type.equals(CoreActionType.SERVER_NOPING)) {
            final Window active = Main.getUI().getActiveWindow();
            final String value = formatTime(arguments[1]) + "+";
            
            pings.put(((Server) arguments[0]), value);
            
            if (((Server) arguments[0]).ownsFrame(active)) {
                panel.setText(value);
            }

            panel.refreshDialog();
        } else if (type.equals(CoreActionType.SERVER_DISCONNECTED)) {
            final Window active = Main.getUI().getActiveWindow();
            
            if (((Server) arguments[0]).ownsFrame(active)) {
                panel.setText("Not connected");
                pings.remove(arguments[0]);
            }

            panel.refreshDialog();
        } else if (type.equals(CoreActionType.CLIENT_FRAME_CHANGED)) {
            final FrameContainer source = (FrameContainer) arguments[0];
            if (source.getServer() == null) {
                panel.setText("Unknown");
            } else if (source.getServer().getState() != ServerState.CONNECTED) {
                panel.setText("Not connected");
            } else {
                panel.setText(getTime(source.getServer()));
            }

            panel.refreshDialog();
        } else if (useAlternate && type.equals(CoreActionType.SERVER_PINGSENT)) {
            ((Server) arguments[0]).getParser().sendLine("LAGCHECK_" + new Date().getTime());
        } else if (useAlternate && type.equals(CoreActionType.SERVER_NUMERIC)
                && ((Integer) arguments[1]).intValue() == 421
                && ((String[]) arguments[2])[3].startsWith("LAGCHECK_")) {            
            try {
                final long sent = Long.parseLong(((String[]) arguments[2])[3].substring(9));
                final Long duration = Long.valueOf(new Date().getTime() - sent);
                final String value = formatTime(duration);
                final Window active = Main.getUI().getActiveWindow();
                
                pings.put((Server) arguments[0], value);
                getHistory(((Server) arguments[0])).add(duration);
                
                if (((Server) arguments[0]).ownsFrame(active)) {
                    panel.setText(value);
                }                
            } catch (NumberFormatException ex) {
                pings.remove((Server) arguments[0]);
            }

            if (format != null) {
                format.delete(0, format.length());
            }

            panel.refreshDialog();
        }
    }

    /**
     * Retrieves the ping time for the specified server.
     *
     * @param server The server whose ping time is being requested
     * @return A String representation of the current lag, or "Unknown"
     */
    public String getTime(final Server server) {
        return pings.get(server) == null ? "Unknown" : pings.get(server);
    }
    
    /**
     * Formats the specified time so it's a nice size to display in the label.
     * @param object An uncast Long representing the time to be formatted
     * @return Formatted time string
     */
    protected String formatTime(final Object object) {
        final Long time = (Long) object;
        
        if (time >= 10000) {
            return Math.round(time / 1000.0) + "s";
        } else {
            return time + "ms";
        }
    }

    /** {@inheritDoc} */
    @Override
    public void showConfig(final PreferencesManager manager) {
        final PreferencesCategory cat = new PreferencesCategory("Lag display plugin",
                                                                "");
        cat.addSetting(new PreferencesSetting(PreferencesType.BOOLEAN,
                getDomain(), "usealternate",
                "Alternate method", "Use an alternate method of determining "
                + "lag which bypasses bouncers or proxies that may reply?"));
        cat.addSetting(new PreferencesSetting(PreferencesType.BOOLEAN,
                getDomain(), "graph", "Show graph", "Show a graph of ping times " +
                "for the current server in the information popup?"));
        cat.addSetting(new PreferencesSetting(PreferencesType.BOOLEAN,
                getDomain(), "labels", "Show labels", "Show labels on selected " +
                "points on the ping graph?"));
        cat.addSetting(new PreferencesSetting(PreferencesType.INTEGER,
                getDomain(), "history", "Graph points", "Number of data points " +
                "to plot on the graph, if enabled."));
        manager.getCategory("Plugins").addSubCategory(cat);
    }

    /** {@inheritDoc} */
    @Override
    public void configChanged(final String domain, final String key) {
        readConfig();
    }
}
