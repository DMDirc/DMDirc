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

package uk.org.ownage.dmdirc.plugins.plugins.lagdisplay;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.EtchedBorder;

import uk.org.ownage.dmdirc.Server;
import uk.org.ownage.dmdirc.actions.ActionType;
import uk.org.ownage.dmdirc.actions.CoreActionType;
import uk.org.ownage.dmdirc.plugins.EventPlugin;
import uk.org.ownage.dmdirc.ui.MainFrame;

/**
 * Displays the current server's lag in the status bar.
 * @author chris
 */
public final class LagDisplayPlugin implements EventPlugin {
    
    /** Is this plugin active? */
    private boolean isActive = false;
    
    /** The panel we use in the status bar. */
    private final JPanel panel = new JPanel();
    
    /** The label we use to show lag. */
    private final JLabel label = new JLabel("Unknown");
    
    /** Creates a new instance of LagDisplayPlugin. */
    public LagDisplayPlugin() {
        super();
        
        panel.setLayout(new BorderLayout());
        panel.setPreferredSize(new Dimension(70, 25));
        panel.setBorder(new EtchedBorder());
        
        label.setHorizontalAlignment(SwingConstants.CENTER);
        
        panel.add(label);
    }
    
    /** {@inheritDoc} */
    public boolean onLoad() {
        
        return true;
    }
    
    /** {@inheritDoc} */
    public void onUnload() {
    }
    
    /** {@inheritDoc} */
    public void onActivate() {
        isActive = true;
        
        MainFrame.getMainFrame().getStatusBar().addComponent(panel);
    }
    
    /** {@inheritDoc} */
    public boolean isActive() {
        return isActive;
    }
    
    /** {@inheritDoc} */
    public void onDeactivate() {
        isActive = false;
        
        MainFrame.getMainFrame().getStatusBar().removeComponent(panel);
    }
    
    /** {@inheritDoc} */
    public int getVersion() {
        return 0;
    }
    
    /** {@inheritDoc} */
    public String getAuthor() {
        return "Chris 'MD87' Smith - chris@dmdirc.com";
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
    
    /** {@inheritDoc} */
    public void processEvent(final ActionType type, final Object... arguments) {
        if (type.equals(CoreActionType.SERVER_GOTPING)) {
            final JInternalFrame active = MainFrame.getMainFrame().getActiveFrame();
            if (((Server) arguments[0]).ownsFrame(active)) {
                label.setText(((Long) arguments[1]).toString() + "ms");
            }
        } else if (type.equals(CoreActionType.SERVER_NOPING)) {
            final JInternalFrame active = MainFrame.getMainFrame().getActiveFrame();
            if (((Server) arguments[0]).ownsFrame(active)) {
                label.setText(((Long) arguments[1]).toString() + "ms+");
            }
        }
    }
    
}
