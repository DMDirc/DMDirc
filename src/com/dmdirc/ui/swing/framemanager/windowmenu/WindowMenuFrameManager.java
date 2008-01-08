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

package com.dmdirc.ui.swing.framemanager.windowmenu;

import com.dmdirc.FrameContainer;
import com.dmdirc.ui.interfaces.FrameManager;
import com.dmdirc.FrameContainerComparator;

import com.dmdirc.GlobalWindow;
import com.dmdirc.Main;
import com.dmdirc.config.IdentityManager;
import com.dmdirc.interfaces.SelectionListener;
import com.dmdirc.logger.ErrorLevel;
import com.dmdirc.logger.Logger;
import com.dmdirc.ui.WindowManager;
import com.dmdirc.ui.interfaces.Window;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyVetoException;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;

/**
 * Manages the window menu window list.
 */
public final class WindowMenuFrameManager extends JMenu implements FrameManager,
        ActionListener, SelectionListener, MenuListener {

    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 1;
    /** Menu item list. */
    private final Map<FrameContainer, FrameContainerMenuItem> menuItemMap;
    /** Comparator. */
    private FrameContainerComparator comparator =
            new FrameContainerComparator();
    /** Non frame container menu count. */
    private final int itemCount;
    /** Toggle state menu item. */
    private JMenuItem toggleStateMenuItem;

    /** 
     * Creates a new instance of WindowMenuFrameManager.
     */
    public WindowMenuFrameManager() {
        menuItemMap =
                new TreeMap<FrameContainer, FrameContainerMenuItem>(comparator);

        setText("Window");
        setMnemonic('w');
        WindowManager.addFrameManager(this);
        addMenuListener(this);
        
        toggleStateMenuItem = new JMenuItem();
        toggleStateMenuItem.setMnemonic('t');
        toggleStateMenuItem.setText("ToggleState");
        toggleStateMenuItem.setActionCommand("ToggleState");
        toggleStateMenuItem.addActionListener(this);
        
        checkToggleState();

        add(toggleStateMenuItem);

        JMenuItem menuItem = new JMenuItem();
        menuItem.setMnemonic('n');
        menuItem.setText("Minimise");
        menuItem.setActionCommand("Minimise");
        menuItem.addActionListener(this);
        add(menuItem);

        menuItem = new JMenuItem();
        menuItem.setMnemonic('c');
        menuItem.setText("Close");
        menuItem.setActionCommand("Close");
        menuItem.addActionListener(this);
        add(menuItem);

        addSeparator();

        itemCount = getMenuComponentCount();
    }

    /** {@inheritDoc} */
    @Override
    public void setParent(final JComponent parent) {
    //Ignore
    }

    /** {@inheritDoc} */
    @Override
    public boolean canPositionVertically() {
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public boolean canPositionHorizontally() {
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public void setSelected(final FrameContainer source) {
    //Ignore
    }

    /** {@inheritDoc} */
    @Override
    public void showNotification(final FrameContainer source,
            final Color colour) {
    //Ignore
    }

    /** {@inheritDoc} */
    @Override
    public void clearNotification(final FrameContainer source) {
    //Ignore
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

    /** {@inheritDoc} */
    @Override
    public void iconUpdated(final FrameContainer window) {
    //Ignore
    }

    /**
     * Adds a frame container to the list.
     *
     * @param window Window to add to the list
     */
    private void addFrameContainer(final FrameContainer window) {
        final FrameContainerMenuItem mi = new FrameContainerMenuItem(window);
        synchronized (menuItemMap) {
            if (isShowing()) {
                setSelected(false);
                setPopupMenuVisible(false);
            }
            menuItemMap.put(window, mi);
            window.addSelectionListener(this);
            add(mi, getIndex(window));
        }
    }

    /**
     * Removes a frame container from the list.
     *
     * @param window Window to remove from list
     */
    private void removeFramecontainer(final FrameContainer window) {
        synchronized (menuItemMap) {
            if (isShowing()) {
                setSelected(false);
                setPopupMenuVisible(false);
            }
            final FrameContainerMenuItem mi = menuItemMap.get(window);
            if (mi != null) {
                remove(mi);
                menuItemMap.remove(window);
                window.removeSelectionListener(this);
            }
        }
    }

    /** 
     * {@inheritDoc}
     * 
     * @param e Action event
     */
    @Override
    public void actionPerformed(final ActionEvent e) {
        if ("ToggleState".equals(e.getActionCommand())) {
            final Window window = Main.getUI().getActiveWindow();
            if (window != null) {
                try {
                    window.setMaximum(!window.isMaximum());
                } catch (PropertyVetoException ex) {
                    Logger.userError(ErrorLevel.LOW, "Unable to maximise window");
                }
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public void selectionChanged(final Window window) {
        final Map<FrameContainer, FrameContainerMenuItem> newMap =
                new TreeMap<FrameContainer, FrameContainerMenuItem>(comparator);
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
        if (Main.getUI().getActiveWindow() == null) {
            toggleStateMenuItem.setEnabled(false);
        } else {
            toggleStateMenuItem.setEnabled(true);
            if (Main.getUI().getActiveWindow().isMaximum()) {
                toggleStateMenuItem.setText("Restore");
                toggleStateMenuItem.setMnemonic('r');
            } else {
                toggleStateMenuItem.setText("Maximise");
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
        if (newChild instanceof GlobalWindow) {
            return 0;
        }

        for (int i = itemCount; i < getMenuComponentCount(); i++) {
            if (!(getMenuComponent(i) instanceof FrameContainerMenuItem)) {
                continue;
            }
            final FrameContainer child =
                    ((FrameContainerMenuItem) getMenuComponent(i)).getFrame();
            if (sortBefore(newChild, child)) {
                return i;
            } else if (!sortAfter(newChild, child) && IdentityManager.getGlobalConfig().
                    getOptionBool("treeview", "sortwindows", false) && newChild.toString().
                    compareToIgnoreCase(
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
