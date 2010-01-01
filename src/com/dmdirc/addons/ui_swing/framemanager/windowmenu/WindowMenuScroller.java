/*
 * 
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

package com.dmdirc.addons.ui_swing.framemanager.windowmenu;

import com.dmdirc.addons.ui_swing.UIUtilities;
import com.dmdirc.addons.ui_swing.components.MenuScroller;
import com.dmdirc.config.IdentityManager;
import com.dmdirc.interfaces.ConfigChangeListener;

import javax.swing.JMenu;

/**
 * Window menu scroller.
 */
public class WindowMenuScroller implements ConfigChangeListener {

    /** Menu scroller. */
    private MenuScroller scroller;
    /** Config domain. */
    private String configDomain;
    /** Menu to scroll. */
    private JMenu menu;
    /** Fixed menu item count. */
    private int fixedCount;

    /**
     * Creates a new menu scroller for the window menu.
     *
     * @param menu Menu to create scroller for
     * @param configDomain Domain to check config settings in
     * @param fixedCount Number of fixed items in the menu
     */
    public WindowMenuScroller(final JMenu menu, final String configDomain,
            final int fixedCount) {
        this.menu = menu;
        this.configDomain = configDomain;
        this.fixedCount = fixedCount;
        this.scroller = new MenuScroller(menu,
                IdentityManager.getGlobalConfig().getOptionInt(configDomain,
                "windowMenuItems"),
                IdentityManager.getGlobalConfig().getOptionInt(configDomain,
                "windowMenuScrollInterval"), fixedCount, 0);
        scroller.setShowSeperators(false);

        IdentityManager.getGlobalConfig().addChangeListener(configDomain,
                "windowMenuItems", this);
        IdentityManager.getGlobalConfig().addChangeListener(configDomain,
                "windowMenuScrollInterval", this);
    }

    /** {@inheritDoc} */
    @Override
    public void configChanged(final String domain, final String key) {
        UIUtilities.invokeLater(new Runnable() {

            /** {@inheritDoc} */
            @Override
            public void run() {
                scroller.dispose();
                scroller = new MenuScroller(menu,
                        IdentityManager.getGlobalConfig().getOptionInt(
                        configDomain, "windowMenuItems"),
                        IdentityManager.getGlobalConfig().getOptionInt(
                        configDomain, "windowMenuScrollInterval"), fixedCount,
                        0);
                scroller.setShowSeperators(false);
            }
        });
    }
}
