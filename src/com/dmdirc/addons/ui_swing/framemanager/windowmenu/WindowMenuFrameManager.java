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
package com.dmdirc.addons.ui_swing.framemanager.windowmenu;

import com.dmdirc.FrameContainer;
import com.dmdirc.FrameContainerComparator;
import com.dmdirc.addons.ui_swing.UIUtilities;
import com.dmdirc.config.IdentityManager;
import com.dmdirc.interfaces.SelectionListener;
import com.dmdirc.ui.IconManager;
import com.dmdirc.ui.WindowManager;
import com.dmdirc.ui.interfaces.Window;
import com.dmdirc.ui.interfaces.FrameListener;
import com.dmdirc.addons.ui_swing.components.MenuScroller;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;

/**
 * Manages the window menu window list.
 */
public final class WindowMenuFrameManager extends JMenu implements
        FrameListener, ActionListener, SelectionListener, MenuListener {

    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 1;
    /** Menu item list. */
    private final Map<FrameContainer, FrameContainerMenuItem> menuItemMap;
    /** Comparator. */
    private final FrameContainerComparator comparator =
            new FrameContainerComparator();
    /** Non frame container menu count. */
    private final int itemCount;
    /** Menu items for toggling, closing and minimising. */
    private final JMenuItem toggleStateMenuItem,  closeMenuItem,  minimiseMenuItem;
    /** Seperator. */
    private final JSeparator separator;
    /** Active window. */
    private Window activeWindow;
    /** Enabled menu items? */
    private final AtomicBoolean enabledMenuItems = new AtomicBoolean(false);

    /** 
     * Creates a new instance of WindowMenuFrameManager.
     */
    public WindowMenuFrameManager() {
        super();

        menuItemMap =
                new TreeMap<FrameContainer, FrameContainerMenuItem>(comparator);

        setText("Window");
        setMnemonic('w');
        WindowManager.addFrameListener(this);
        addMenuListener(this);

        minimiseMenuItem = new JMenuItem(IconManager.getIconManager().getIcon(
                "minimise"));
        minimiseMenuItem.setMnemonic('n');
        minimiseMenuItem.setText("Minimise");
        minimiseMenuItem.setActionCommand("Minimise");
        minimiseMenuItem.addActionListener(this);
        add(minimiseMenuItem);

        toggleStateMenuItem = new JMenuItem(IconManager.getIconManager().getIcon(
                "maximise"));
        toggleStateMenuItem.setMnemonic('m');
        toggleStateMenuItem.setText("Maximise");
        toggleStateMenuItem.setActionCommand("ToggleState");
        toggleStateMenuItem.addActionListener(this);
        add(toggleStateMenuItem);

        closeMenuItem = new JMenuItem(IconManager.getIconManager().getIcon(
                "close"));
        closeMenuItem.setMnemonic('c');
        closeMenuItem.setText("Close");
        closeMenuItem.setActionCommand("Close");
        closeMenuItem.addActionListener(this);
        add(closeMenuItem);
        
        separator = new JPopupMenu.Separator();
        add(separator);

        itemCount = getMenuComponentCount();
        checkToggleState();

        new MenuScroller(this, 20, 250, 4, 0).setShowSeperators(false);
    }

    /**
     * Checks the number of components in the menu and enables menus items 
     * appropriately.
     */
    private void checkMenuItems() {
        enabledMenuItems.set((getMenuComponentCount() > itemCount));
        separator.setVisible(enabledMenuItems.get());
        closeMenuItem.setEnabled(enabledMenuItems.get());
        toggleStateMenuItem.setEnabled(enabledMenuItems.get());
        minimiseMenuItem.setEnabled(enabledMenuItems.get());
    }

    /** {@inheritDoc} */
    @Override
    public void addWindow(final FrameContainer window) {
        addFrameContainer(window);
    }

    /** {@inheritDoc} */
    @Override
    public void delWindow(final FrameContainer window) {
        removeFramecontainer(window);
    }

    /** {@inheritDoc} */
    @Override
    public void addWindow(final FrameContainer parent,
            final FrameContainer window) {
        addFrameContainer(window);
    }

    /** {@inheritDoc} */
    @Override
    public void delWindow(final FrameContainer parent,
            final FrameContainer window) {
        removeFramecontainer(window);
    }

    /**
     * Adds a frame container to the list.
     *
     * @param window Window to add to the list
     */
    private void addFrameContainer(final FrameContainer window) {
        UIUtilities.invokeLater(new Runnable() {

            /** {@inheritDoc} */
            @Override
            public void run() {
                final FrameContainerMenuItem mi =
                        new FrameContainerMenuItem(window);
                synchronized (menuItemMap) {
                    if (isShowing()) {
                        setSelected(false);
                        setPopupMenuVisible(false);
                    }
                    menuItemMap.put(window, mi);
                    window.addSelectionListener(WindowMenuFrameManager.this);
                    add(mi, getIndex(window));
                    checkMenuItems();
                }
            }
        });
    }

    /**
     * Removes a frame container from the list.
     *
     * @param window Window to remove from list
     */
    private void removeFramecontainer(final FrameContainer window) {
        UIUtilities.invokeLater(new Runnable() {

            /** {@inheritDoc} */
            @Override
            public void run() {
                synchronized (menuItemMap) {
                    if (isShowing()) {
                        setSelected(false);
                        setPopupMenuVisible(false);
                    }
                    final FrameContainerMenuItem mi = menuItemMap.get(window);
                    if (mi != null) {
                        remove(mi);
                        menuItemMap.remove(window);
                        window.removeSelectionListener(
                                WindowMenuFrameManager.this);
                    }
                    checkMenuItems();
                }
            }
        });
    }

    /** 
     * {@inheritDoc}
     * 
     * @param e Action event
     */
    @Override
    public void actionPerformed(final ActionEvent e) {
        if (enabledMenuItems.get()) {
            if (e.getActionCommand().equals("ToggleState")) {
                activeWindow.toggleMaximise();
            } else if (e.getActionCommand().equals("Minimise")) {
                activeWindow.minimise();
            } else if (e.getActionCommand().equals("Close")) {
                activeWindow.close();
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public void selectionChanged(final Window window) {
        activeWindow = window;
        final Map<FrameContainer, FrameContainerMenuItem> newMap =
                new TreeMap<FrameContainer, FrameContainerMenuItem>(
                comparator);
        synchronized (menuItemMap) {
            newMap.putAll(menuItemMap);
        }

        for (FrameContainerMenuItem menuItem : newMap.values()) {
            menuItem.selectionChanged(window);
        }
    }

    /**
     * Checks and sets the state of the toggle menu item.
     */
    private void checkToggleState() {
        checkMenuItems();
        if (activeWindow != null) {
            toggleStateMenuItem.setEnabled(true);
            closeMenuItem.setEnabled(true);
            minimiseMenuItem.setEnabled(true);

            if (activeWindow.isMaximum()) {
                toggleStateMenuItem.setText("Restore");
                toggleStateMenuItem.setIcon(IconManager.getIconManager().getIcon(
                        "restore"));
                toggleStateMenuItem.setMnemonic('r');
            } else {
                toggleStateMenuItem.setText("Maximise");
                toggleStateMenuItem.setIcon(IconManager.getIconManager().getIcon(
                        "maximise"));
                toggleStateMenuItem.setMnemonic('m');
            }
        }
    }

    /**
     * Compares the new child with the existing children or parent to decide
     * where it needs to be inserted.
     *
     * @param newChild new node to be inserted.
     *
     * @return index where new node is to be inserted.
     */
    private int getIndex(final FrameContainer newChild) {
        for (int i = itemCount; i < getMenuComponentCount(); i++) {
            if (!(getMenuComponent(i) instanceof FrameContainerMenuItem)) {
                continue;
            }
            final FrameContainer child =
                    ((FrameContainerMenuItem) getMenuComponent(i)).getFrame();
            if (sortBefore(newChild, child)) {
                return i;
            } else if (!sortAfter(newChild, child) &&
                    IdentityManager.getGlobalConfig().getOptionBool("treeview",
                    "sortwindows") && newChild.toString().compareToIgnoreCase(
                    child.toString()) < 0) {
                return i;
            }
        }

        return getMenuComponentCount();
    }

    /**
     * Compares the types of the specified nodes' objects to see if the new
     * node should be sorted before the other.
     *
     * @param newChild The new child to be tested
     * @param child The existing child that it's being tested against
     *
     * @return True iff newChild should be sorted before child
     */
    private boolean sortBefore(final FrameContainer newChild,
            final FrameContainer child) {

        return comparator.compare(newChild, child) <= -1;
    }

    /**
     * Compares the types of the specified nodes' objects to see if the new
     * node should be sorted after the other.
     *
     * @param newChild The new child to be tested
     * @param child The existing child that it's being tested against
     *
     * @return True iff newChild should be sorted before child
     */
    private boolean sortAfter(final FrameContainer newChild,
            final FrameContainer child) {

        return comparator.compare(newChild, child) >= 1;
    }

    /** {@inheritDoc} */
    @Override
    public void menuSelected(final MenuEvent e) {
        checkToggleState();
    }

    /** {@inheritDoc} */
    @Override
    public void menuDeselected(final MenuEvent e) {
        //Ignore
    }

    /** {@inheritDoc} */
    @Override
    public void menuCanceled(final MenuEvent e) {
        //Ignore
    }
}
