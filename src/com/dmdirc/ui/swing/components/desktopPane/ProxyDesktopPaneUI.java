/*
 * 
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

package com.dmdirc.ui.swing.components.desktopPane;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JDesktopPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.plaf.DesktopPaneUI;

/**
 * Proxy desktop pane ui, proxies and adds custom key bindings.
 */
public class ProxyDesktopPaneUI extends DesktopPaneUI {

    private DesktopPaneUI ui;
    private DMDircDesktopPane desktopPane;

    /**
     * Creates a new proxying desktop pane ui.
     *
     * @param ui UI to proxy to
     * @param desktopPane desktop pane to use
     */
    public ProxyDesktopPaneUI(final DesktopPaneUI ui,
                              final DMDircDesktopPane desktopPane) {
        this.ui = ui;
        this.desktopPane = desktopPane;
    }

    /** @inheritDoc} */
    @Override
    public void installUI(final JComponent c) {
        ui.installUI(c);

        InputMap inputMap =
                 SwingUtilities.getUIInputMap(desktopPane,
                                              JDesktopPane.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        inputMap.put(KeyStroke.getKeyStroke("ctrl shift pressed TAB"),
                     "selectPreviousFrame");

        SwingUtilities.getUIActionMap(desktopPane).put("selectNextFrame", new AbstractAction("selectNextFrame") {

            private static final long serialVersionUID = 1;

            /** {@inheritDoc} */
            @Override
            public void actionPerformed(final ActionEvent evt) {
                desktopPane.scrollDown();
            }
        });

        SwingUtilities.getUIActionMap(desktopPane).
                put("selectPreviousFrame",
                    new AbstractAction("selectPreviousFrame") {

            private static final long serialVersionUID = 1;

            /** {@inheritDoc} */
            @Override
            public void actionPerformed(final ActionEvent evt) {
                desktopPane.scrollUp();
            }
        });
    }

    /** @inheritDoc} */
    @Override
    public void uninstallUI(final JComponent c) {
        ui.uninstallUI(c);
    }
}
