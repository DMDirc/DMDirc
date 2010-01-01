/*
 * 
 * Copyright (c) 2006-2010 Chris Smith, Shane Mc Cormack, Gregory Holmes
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

package com.dmdirc.addons.ui_swing.components;

import com.dmdirc.addons.ui_swing.MainFrame;
import com.dmdirc.addons.ui_swing.UIUtilities;
import com.dmdirc.addons.ui_swing.components.frames.TextFrame;
import com.dmdirc.FrameContainer;
import com.dmdirc.config.ConfigManager;
import com.dmdirc.config.IdentityManager;
import com.dmdirc.interfaces.ConfigChangeListener;
import com.dmdirc.interfaces.SelectionListener;
import com.dmdirc.ui.IconManager;
import com.dmdirc.ui.WindowManager;
import com.dmdirc.ui.interfaces.Window;
import com.dmdirc.ui.interfaces.FrameListener;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JInternalFrame;
import javax.swing.JPanel;

import net.miginfocom.swing.MigLayout;

/**
 * Provides an MDI style bar for restore/minimise/close.
 */
public class MDIBar extends JPanel implements FrameListener, SelectionListener,
        PropertyChangeListener, ActionListener, ConfigChangeListener {

    private static final long serialVersionUID = -8028057596226636245L;
    private static final int ICON_SIZE = 12;
    private NoFocusButton closeButton;
    private NoFocusButton minimiseButton;
    private NoFocusButton restoreButton;
    private MainFrame mainFrame;
    private ConfigManager config;
    private String visibility;
    /** Active frame. */
    private Window activeFrame;

    /**
     * Instantiates a new MDI bar.
     *
     * @param mainFrame Main frame instance
     */
    public MDIBar(final MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        this.config = IdentityManager.getGlobalConfig();
        visibility = config.getOption("ui", "mdiBarVisibility");

        closeButton = new NoFocusButton(IconManager.getIconManager().
                getScaledIcon("close-12", ICON_SIZE, ICON_SIZE));
        minimiseButton = new NoFocusButton(IconManager.getIconManager().
                getScaledIcon("minimise-12", ICON_SIZE, ICON_SIZE));
        restoreButton = new NoFocusButton(IconManager.getIconManager().
                getScaledIcon("maximise-12", ICON_SIZE, ICON_SIZE));

        setOpaque(false);
        setLayout(new MigLayout("hmax 17, ins 1 0 0 0, fill"));
        add(minimiseButton, "w 17!, h 17!, right");
        add(restoreButton, "w 17!, h 17!, right");
        add(closeButton, "w 17!, h 17!, right");


        WindowManager.addFrameListener(this);
        WindowManager.addSelectionListener(this);
        closeButton.addActionListener(this);
        minimiseButton.addActionListener(this);
        restoreButton.addActionListener(this);
        config.addChangeListener("ui", "mdiBarVisibility", this);

        check();
    }

    /** {@inheritDoc} */
    @Override
    public void setEnabled(final boolean enabled) {
        closeButton.setEnabled(enabled);
        minimiseButton.setEnabled(enabled);
        restoreButton.setEnabled(enabled);
    }

    private void check() {
        UIUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                boolean show = true;
                if (mainFrame == null) {
                    show = false;
                    return;
                } else if ("alwaysShow".equalsIgnoreCase(visibility)) {
                    show = mainFrame.getDesktopPane().getAllFrames().length > 0;
                } else if ("neverShow".equalsIgnoreCase(visibility)) {
                    show = false;
                } else if ("showWhenMaximised".equalsIgnoreCase(visibility)) {
                    show = mainFrame.getMaximised();
                }
                setVisible(show);
                setEnabled(mainFrame.getDesktopPane().getAllFrames().length > 0);
            }
        });
    }

    /** {@inheritDoc} */
    @Override
    public void addWindow(final FrameContainer window) {
        if (window.getFrame() instanceof JInternalFrame) {
            ((JInternalFrame) window.getFrame()).addPropertyChangeListener(
                    "maximum", this);
        }
        check();
    }

    /** {@inheritDoc} */
    @Override
    public void delWindow(final FrameContainer window) {
        if (window.getFrame() instanceof JInternalFrame) {
            ((JInternalFrame) window.getFrame()).removePropertyChangeListener(
                    this);
        }
        check();
    }

    /** {@inheritDoc} */
    @Override
    public void addWindow(final FrameContainer parent,
                          final FrameContainer window) {
        addWindow(window);
    }

    /** {@inheritDoc} */
    @Override
    public void delWindow(final FrameContainer parent,
                          final FrameContainer window) {
        delWindow(window);
    }

    /** {@inheritDoc} */
    @Override
    public void propertyChange(final PropertyChangeEvent evt) {
        if ((Boolean) evt.getNewValue()) {
            restoreButton.setIcon(IconManager.getIconManager().getScaledIcon(
                    "restore-12", ICON_SIZE, ICON_SIZE));
        } else {
            restoreButton.setIcon(IconManager.getIconManager().getScaledIcon(
                    "maximise-12", ICON_SIZE, ICON_SIZE));
        }
        check();
    }

    /**
     * {@inheritDoc}
     *
     * @param e Action event
     */
    @Override
    public void actionPerformed(final ActionEvent e) {
        if (activeFrame == null) {
            return;
        }
        if (closeButton.equals(e.getSource())) {
            activeFrame.close();
        } else if (minimiseButton.equals(e.getSource())) {
            ((TextFrame) activeFrame).minimise();
        } else if (restoreButton.equals(e.getSource())) {
            activeFrame.toggleMaximise();
        }
    }

    /** {@inheritDoc} */
    @Override
    public void configChanged(final String domain, final String key) {
        visibility = config.getOption("ui", "mdiBarVisibility");
        check();
    }

    @Override
    public void selectionChanged(Window window) {
        activeFrame = window;
    }
}
