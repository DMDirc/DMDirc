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

package com.dmdirc.addons.lagdisplay;

import com.dmdirc.Server;
import com.dmdirc.Main;
import com.dmdirc.ServerManager;
import com.dmdirc.ServerState;
import com.dmdirc.ui.swing.MainFrame;
import com.dmdirc.ui.swing.SwingController;
import com.dmdirc.ui.swing.components.StandardDialog;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Point;
import java.util.List;

import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.border.EtchedBorder;

import net.miginfocom.swing.MigLayout;

/**
 * Shows information about all connected servers.
 *
 * @author chris
 */
public class ServerInfoDialog extends StandardDialog {

    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 1;

    /** The parent JPanel. */
    private final JPanel parent;

    /**
     * Creates a new ServerInfoDialog.
     *
     * @param ldp The {@link LagDisplayPlugin} we're using for info
     * @param parent The {@link JPanel} to use for positioning
     */
    public ServerInfoDialog(final LagDisplayPlugin ldp, final JPanel parent) {
        super((MainFrame) Main.getUI().getMainWindow(), false);

        this.parent = parent;

        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        setTitle("Server info");

        final JPanel panel = new JPanel();

        panel.setLayout(new MigLayout("ins 3 5 7 5, gap 10 5"));
        
        final List<Server> servers = ServerManager.getServerManager().getServers();

        if (servers.isEmpty()) {
            panel.add(new JLabel("No open servers."));
        } else {
            for (Server server : servers) {
                panel.add(new JLabel(server.getName()));
                panel.add(new JLabel(server.getState() == ServerState.CONNECTED ? server.getNetwork() : "---", JLabel.CENTER), "grow");
                panel.add(new JLabel(server.getState() == ServerState.CONNECTED ? ldp.getTime(server) : "---", JLabel.RIGHT), "grow, wrap");
            }
        }

        panel.setBackground(UIManager.getColor("ToolTip.background"));
        panel.setForeground(UIManager.getColor("ToolTip.foreground"));

        add(panel);

        setUndecorated(true);
        setFocusableWindowState(false);
        setFocusable(false);
        setResizable(false);

        pack();

        final Point point = parent.getLocationOnScreen();
        point.translate(parent.getWidth() / 2 - this.getWidth() / 2, - this.getHeight());
        final int maxX = SwingController.getMainFrame().getLocationOnScreen().x
                + SwingController.getMainFrame().getWidth() - 10 - getWidth();
        point.x = Math.min(maxX, point.x);
        setLocation(point);

        panel.setBorder(new GappedEtchedBorder());
    }

    /**
     * An {@link EtchedBorder} that leaves a gap in the bottom where the
     * lag display panel is.
     */
    private class GappedEtchedBorder extends EtchedBorder {

        /**
         * A version number for this class. It should be changed whenever the class
         * structure is changed (or anything else that would prevent serialized
         * objects being unserialized with the new class).
         */
        private static final long serialVersionUID = 1;

        /** {@inheritDoc} */
        @Override
        public void paintBorder(final Component c, final Graphics g,
                final int x, final int y, final int width, final int height) {
            int w = width;
            int h = height;

            g.translate(x, y);

            g.setColor(etchType == LOWERED? getShadowColor(c) : getHighlightColor(c));
            g.drawLine(0, 0, w-1, 0);
            g.drawLine(0, h-1, parent.getLocationOnScreen().x - getLocationOnScreen().x, h-1);
            g.drawLine(parent.getWidth() + parent.getLocationOnScreen().x - getLocationOnScreen().x - 2, h-1, w-1, h-1);
            g.drawLine(0, 0, 0, h-1);
            g.drawLine(w-1, 0, w-1, h-1);

            g.translate(-x, -y);
        }

    }

}
