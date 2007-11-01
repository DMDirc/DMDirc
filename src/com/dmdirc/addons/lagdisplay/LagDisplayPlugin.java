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

package com.dmdirc.addons.lagdisplay;

import com.dmdirc.FrameContainer;
import com.dmdirc.Main;
import com.dmdirc.Server;
import com.dmdirc.actions.ActionType;
import com.dmdirc.actions.CoreActionType;
import com.dmdirc.interfaces.ConfigChangeListener;
import com.dmdirc.config.IdentityManager;
import com.dmdirc.plugins.EventPlugin;
import com.dmdirc.plugins.Plugin;
import com.dmdirc.ui.interfaces.Window;

import java.util.Map;
import java.util.WeakHashMap;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;

import net.miginfocom.swing.MigLayout;

/**
 * Displays the current server's lag in the status bar.
 * @author chris
 */
public final class LagDisplayPlugin extends Plugin implements EventPlugin,
        ConfigChangeListener {
    
    /** The panel we use in the status bar. */
    private final JPanel panel = new JPanel();
    
    /** A cache of ping times. */
    private final Map<Server, String> pings = new WeakHashMap<Server, String>();
    
    /** The label we use to show lag. */
    private final JLabel label = new JLabel("Unknown");
    
    /** Whether or not we're using our alternate ping method. */
    private volatile boolean useAlternate = false;
    
    /** Creates a new instance of LagDisplayPlugin. */
    public LagDisplayPlugin() {
        super();
        
        IdentityManager.getGlobalConfig().addChangeListener("plugin-Lagdisplay", this);
        configChanged(null, null);
        
        panel.setBorder(BorderFactory.createEtchedBorder());
        panel.setLayout(new MigLayout("ins 0 rel 0 rel, aligny center"));
        panel.add(label);
    }
    
    /** {@inheritDoc} */
    public boolean onLoad() {
        
        return true;
    }
    
    /** {@inheritDoc} */
    public void onActivate() {
        Main.getUI().getStatusBar().addComponent(panel);
    }
    
    /** {@inheritDoc} */
    public void onDeactivate() {
        Main.getUI().getStatusBar().removeComponent(panel);
    }
    
    /** {@inheritDoc} */
    public String getVersion() {
        return "0.2";
    }
    
    /** {@inheritDoc} */
    public String getAuthor() {
        return "Chris <chris@dmdirc.com>";
    }
    
    /** {@inheritDoc} */
    public String getDescription() {
        return "Displays the server lag in the status bar";
    }
    
    /** {@inheritDoc} */
    public boolean isConfigurable() {
        return false;
    }
    
    /** {@inheritDoc} */
    public void showConfig() {
    }
    
    /** {@inheritDoc}. */
    public String toString() {
        return "Lag Displayer";
    }
    
    /** {@inheritDoc} */
    public void processEvent(final ActionType type, final StringBuffer format, final Object... arguments) {
        if (!useAlternate && type.equals(CoreActionType.SERVER_GOTPING)) {
            final Window active = Main.getUI().getMainWindow().getActiveFrame();
            final String value = formatTime(arguments[1]);
            
            pings.put(((Server) arguments[0]), value);
            
            if (((Server) arguments[0]).ownsFrame(active)) {
                label.setText(value);
            }
        } else if (!useAlternate && type.equals(CoreActionType.SERVER_NOPING)) {
            final Window active = Main.getUI().getMainWindow().getActiveFrame();
            final String value = formatTime(arguments[1]) + "+";
            
            pings.put(((Server) arguments[0]), value);
            
            if (((Server) arguments[0]).ownsFrame(active)) {
                label.setText(value);
            }
        } else if (type.equals(CoreActionType.CLIENT_FRAME_CHANGED)) {
            final FrameContainer source = (FrameContainer) arguments[0];
            if (source.getServer() == null || !pings.containsKey(source.getServer())) {
                label.setText("Unknown");
            } else {
                label.setText(pings.get(source.getServer()));
            }
        }
    }
    
    /**
     * Formats the specified time so it's a nice size to display in the label.
     * @param object An uncast Long representing the time to be formatted
     * @return Formatted time string
     */
    private String formatTime(final Object object) {
        final Long time = (Long) object;
        
        if (time >= 10000) {
            return Math.round(time / 1000.0) + "s";
        } else {
            return time + "ms";
        }
    }

    /** {@inheritDoc} */
    public void configChanged(final String domain, final String key) {
        useAlternate = IdentityManager.getGlobalConfig().getOptionBool("plugin-Lagdisplay",
                "usealternate", false);
    }
}
