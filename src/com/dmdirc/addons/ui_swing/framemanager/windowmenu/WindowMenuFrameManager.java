/*
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

package com.dmdirc.addons.ui_swing.framemanager.windowmenu;

import com.dmdirc.FrameContainer;
import com.dmdirc.FrameContainerComparator;
import com.dmdirc.addons.ui_swing.SwingController;
import com.dmdirc.config.IdentityManager;
import com.dmdirc.interfaces.SelectionListener;
import com.dmdirc.ui.IconManager;
import com.dmdirc.ui.WindowManager;
import com.dmdirc.ui.interfaces.Window;
import com.dmdirc.ui.interfaces.FrameListener;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;
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
    /** Comparator. */
    private final FrameContainerComparator comparator =
            new FrameContainerComparator();
    /** Non frame container menu count. */
    private final int itemCount;
    /** Menu items for toggling, closing and minimising. */
    private final JMenuItem toggleStateMenuItem, closeMenuItem, minimiseMenuItem;
    /** Seperator. */
    private final JSeparator separator;
    /** Active window. */
    private Window activeWindow;
    /** Enabled menu items? */
    private final AtomicBoolean enabledMenuItems = new AtomicBoolean(false);
    /** Swing controller. */
    private final SwingController controller;
    /** Window -> menu map. */
    private Map<FrameContainer, FrameContainerMenu> menus =
            new HashMap<FrameContainer, FrameContainerMenu>();
    private Map<FrameContainer, FrameContainerMenuItem> items =
            new HashMap<FrameContainer, FrameContainerMenuItem>();
    private Map<FrameContainer, FrameContainerMenuItem> menuItems =
            new HashMap<FrameContainer, FrameContainerMenuItem>();

    /** 
     * Creates a new instance of WindowMenuFrameManager.
     *
     * @param controller Swing controller
     */
    public WindowMenuFrameManager(final SwingController controller) {
        super();
        this.controller = controller;

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

        new WindowMenuScroller(this, controller.getDomain(), 4);
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
        final FrameContainerMenuItem item = new FrameContainerMenuItem(window,
                this);
        items.put(window, item);
        add(item, getIndex(window, this));
        window.addSelectionListener(this);
    }

    /** {@inheritDoc} */
    @Override
    public void delWindow(final FrameContainer window) {
        if (items.containsKey(window)) {
            remove(items.get(window));
            items.remove(window);
        } else if (menus.containsKey(window)) {
            remove(menus.get(window));
            menus.remove(window);
        }
        window.removeSelectionListener(this);
    }

    /** {@inheritDoc} */
    @Override
    public void addWindow(final FrameContainer parent,
            final FrameContainer window) {
        final FrameContainerMenuItem item = new FrameContainerMenuItem(window,
                this);
        JMenu parentMenu;
        if (!menus.containsKey(parent)) {
            final FrameContainerMenu replacement =
                    new FrameContainerMenu(parent);
            replaceItemWithMenu(getParentMenu(parent), items.get(parent),
                    replacement);
            parentMenu = replacement;
        } else {
            parentMenu = menus.get(parent);
        }
        items.put(window, item);
        parentMenu.add(item, getIndex(window, parentMenu));
        window.addSelectionListener(this);
    }

    /** {@inheritDoc} */
    @Override
    public void delWindow(final FrameContainer parent,
            final FrameContainer window) {
        if (items.containsKey(window)) {
            final JMenu menu = getParentMenu(window);
            menu.remove(items.get(window));
            items.remove(window);
            if (menu.getMenuComponentCount() == 1) {
                System.out.println(getParentMenu(parent));
                replaceMenuWithItem(getParentMenu(parent), menus.get(parent),
                        new FrameContainerMenuItem(parent, this));
            }
        } else if (menus.containsKey(window)) {
            menus.get(parent).remove(menus.get(window));
            menus.remove(window);
        }
        window.removeSelectionListener(this);
    }

    private JMenu getParentMenu(final FrameContainer window) {
        FrameContainer parent = null;
        Window parentWindow = WindowManager.getParent(window.getFrame());
        if (parentWindow != null) {
            parent = parentWindow.getContainer();
        }
        if (parent == null) {
            return this;
        } else {
            return menus.get(parent);
        }
    }

    private void replaceItemWithMenu(final JMenu parentMenu,
            final FrameContainerMenuItem item, final FrameContainerMenu menu) {
        parentMenu.remove(item);
        parentMenu.add(menu, getIndex(menu.getFrame(), parentMenu));
        menu.add(item, getIndex(item.getFrame(), menu));
        items.remove(item.getFrame());
        menus.put(menu.getFrame(), menu);
        menuItems.put(item.getFrame(), item);
    }

    private void replaceMenuWithItem(final JMenu parentMenu,
            final FrameContainerMenu menu, final FrameContainerMenuItem item) {
        parentMenu.remove(menu);
        parentMenu.add(item, getIndex(item.getFrame(), parentMenu));
        menus.remove(menu.getFrame());
        items.put(item.getFrame(), item);
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
        final Map<FrameContainer, SelectionListener> allItems =
                new HashMap<FrameContainer, SelectionListener>();
        synchronized (allItems) {
            allItems.putAll(menus);

            for (SelectionListener menuItem : allItems.values()) {
                menuItem.selectionChanged(window);
            }

            allItems.clear();
            allItems.putAll(items);
            allItems.putAll(menuItems);

            for (SelectionListener menuItem : allItems.values()) {
                menuItem.selectionChanged(window);
            }
        }
    }

    /**
     * Tells a parent its child is selected.
     *
     * @param window parent to inform
     */
    protected void parentSelection(final FrameContainer window) {
        menus.get(window).childSelected();
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
     * @param menu Menu for the node to be inserted in
     *
     * @return index where new node is to be inserted.
     */
    private int getIndex(final FrameContainer newChild, final JMenu menu) {
        final int count = menu == this ? itemCount : 0;
        for (int i = count; i < menu.getMenuComponentCount(); i++) {
            if (!(menu.getMenuComponent(i) instanceof FrameContainerMenuItem)) {
                continue;
            }
            final FrameContainer child =
                    ((FrameContainerMenuItem) menu.getMenuComponent(i)).getFrame();
            if (sortBefore(newChild, child)) {
                return i;
            } else if (!sortAfter(newChild, child) && IdentityManager.
                    getGlobalConfig().getOptionBool("treeview",
                    "sortwindows") && newChild.toString().compareToIgnoreCase(
                    child.toString()) < 0) {
                return i;
            }
        }

        return menu.getMenuComponentCount();
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
