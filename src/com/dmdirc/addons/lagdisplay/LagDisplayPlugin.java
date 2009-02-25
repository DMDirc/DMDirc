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
import com.dmdirc.config.prefs.PreferencesCategory;
import com.dmdirc.config.prefs.PreferencesManager;
import com.dmdirc.config.prefs.PreferencesSetting;
import com.dmdirc.config.prefs.PreferencesType;
import com.dmdirc.interfaces.ActionListener;
import com.dmdirc.plugins.Plugin;
import com.dmdirc.ui.interfaces.Window;
import com.dmdirc.addons.ui_swing.UIUtilities;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Date;
import java.util.Map;
import java.util.WeakHashMap;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.border.EtchedBorder;

import net.miginfocom.swing.MigLayout;

/**
 * Displays the current server's lag in the status bar.
 * @author chris
 */
public final class LagDisplayPlugin extends Plugin implements ActionListener,
        MouseListener {
    
    /** The panel we use in the status bar. */
    private final JPanel panel = new JPanel();
    
    /** A cache of ping times. */
    private final Map<Server, String> pings = new WeakHashMap<Server, String>();
    
    /** The label we use to show lag. */
    private final JLabel label = new JLabel("Unknown");

    /** The dialog we're using to show extra info. */
    private ServerInfoDialog dialog;
    
    /** Creates a new instance of LagDisplayPlugin. */
    public LagDisplayPlugin() {
        super();
        
        panel.setBorder(BorderFactory.createEtchedBorder());
        panel.setLayout(new MigLayout("ins 0 rel 0 rel, aligny center"));
        panel.add(label);
    }
    
    /** {@inheritDoc} */
    @Override
    public void onLoad() {
        Main.getUI().getStatusBar().addComponent(panel);
        
        ActionManager.addListener(this, CoreActionType.SERVER_GOTPING,
                CoreActionType.SERVER_NOPING, CoreActionType.CLIENT_FRAME_CHANGED,
                CoreActionType.SERVER_DISCONNECTED, CoreActionType.SERVER_PINGSENT,
                CoreActionType.SERVER_NUMERIC);

        panel.addMouseListener(this);
    }
    
    /** {@inheritDoc} */
    @Override
    public void onUnload() {
        Main.getUI().getStatusBar().removeComponent(panel);
        
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
            
            pings.put(((Server) arguments[0]), value);
            
            if (((Server) arguments[0]).ownsFrame(active)) {
                label.setText(value);
            }

            refreshDialog();
        } else if (!useAlternate && type.equals(CoreActionType.SERVER_NOPING)) {
            final Window active = Main.getUI().getActiveWindow();
            final String value = formatTime(arguments[1]) + "+";
            
            pings.put(((Server) arguments[0]), value);
            
            if (((Server) arguments[0]).ownsFrame(active)) {
                label.setText(value);
            }

            refreshDialog();
        } else if (type.equals(CoreActionType.SERVER_DISCONNECTED)) {
            final Window active = Main.getUI().getActiveWindow();
            
            if (((Server) arguments[0]).ownsFrame(active)) {
                label.setText("Not connected");
                pings.remove(arguments[0]);
            }

            refreshDialog();
        } else if (type.equals(CoreActionType.CLIENT_FRAME_CHANGED)) {
            final FrameContainer source = (FrameContainer) arguments[0];
            if (source.getServer() == null) {
                label.setText("Unknown");
            } else if (source.getServer().getState() != ServerState.CONNECTED) {
                label.setText("Not connected");
            } else {
                label.setText(getTime(source.getServer()));
            }

            refreshDialog();
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
                
                if (((Server) arguments[0]).ownsFrame(active)) {
                    label.setText(value);
                }                
            } catch (NumberFormatException ex) {
                pings.remove((Server) arguments[0]);
            }

            if (format != null) {
                format.delete(0, format.length());
            }

            refreshDialog();
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
    private String formatTime(final Object object) {
        final Long time = (Long) object;
        
        if (time >= 10000) {
            return Math.round(time / 1000.0) + "s";
        } else {
            return time + "ms";
        }
    }

    /** {@inheritDoc} */
    @Override
    public void mouseClicked(final MouseEvent e) {
        // Don't care
    }

    /** {@inheritDoc} */
    @Override
    public void mousePressed(final MouseEvent e) {
        // Don't care
    }

    /** {@inheritDoc} */
    @Override
    public void mouseReleased(final MouseEvent e) {
        // Don't care
    }

    /** {@inheritDoc} */
    @Override
    public void mouseEntered(final MouseEvent e) {
        panel.setBackground(UIManager.getColor("ToolTip.background"));
        panel.setForeground(UIManager.getColor("ToolTip.foreground"));
        panel.setBorder(new ToplessEtchedBorder());

        openDialog();
    }

    /** {@inheritDoc} */
    @Override
    public void mouseExited(final MouseEvent e) {
        panel.setBackground(null);
        panel.setForeground(null);
        panel.setBorder(new EtchedBorder());

        closeDialog();
    }

    /** {@inheritDoc} */
    @Override
    public void showConfig(final PreferencesManager manager) {
        final PreferencesCategory cat = new PreferencesCategory("Lag display plugin",
                                                                "");
        cat.addSetting(new PreferencesSetting(PreferencesType.BOOLEAN,
                getDomain(), "usealternate",
                "Alternate method", "Use an alternate method of determining "
                + "lag which bypasses bouncers or proxies that may reply."));
        manager.getCategory("Plugins").addSubCategory(cat);
    }

    /**
     * Closes and reopens the dialog to update information and border positions.
     */
    protected void refreshDialog() {
        UIUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                synchronized (ServerInfoDialog.class) {
                    if (dialog != null) {
                        closeDialog();
                        openDialog();
                    }
                }
            }
        });
    }

    /**
     * Opens the information dialog.
     */
    protected void openDialog() {
        synchronized (ServerInfoDialog.class) {
            dialog = new ServerInfoDialog(this, panel);
            dialog.setVisible(true);
        }
    }

    /**
     * Closes the information dialog.
     */
    protected void closeDialog() {
        synchronized (ServerInfoDialog.class) {
            if (dialog != null) {
                dialog.setVisible(false);
                dialog.dispose();
                dialog = null;
            }
        }
    }

    /**
     * An {@link EtchedBorder} with no top.
     */
    private static class ToplessEtchedBorder extends EtchedBorder {

        /**
         * A version number for this class. It should be changed whenever the class
         * structure is changed (or anything else that would prevent serialized
         * objects being unserialized with the new class).
         */
        private static final long serialVersionUID = 1;

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width,
                                int height) {
            int w = width;
            int h = height;

            g.translate(x, y);

            g.setColor(etchType == LOWERED? getShadowColor(c) : getHighlightColor(c));
            g.drawLine(0, h-2, w, h-2);
            g.drawLine(0, 0, 0, h-1);
            g.drawLine(w-2, 0, w-2, h-1);

            g.setColor(Color.WHITE);
            g.drawLine(0, h-1, w, h-1);
            g.drawLine(w-1, 0, w-1, h-1);

            g.translate(-x, -y);
        }

    }
}
