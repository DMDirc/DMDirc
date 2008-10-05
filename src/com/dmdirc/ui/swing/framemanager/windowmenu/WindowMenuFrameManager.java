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
import com.dmdirc.FrameContainerComparator;
import com.dmdirc.GlobalWindow;
import com.dmdirc.Main;
import com.dmdirc.config.IdentityManager;
import com.dmdirc.interfaces.SelectionListener;
import com.dmdirc.logger.ErrorLevel;
import com.dmdirc.logger.Logger;
import com.dmdirc.ui.WindowManager;
import com.dmdirc.ui.interfaces.FrameManager;
import com.dmdirc.ui.interfaces.Window;
import com.dmdirc.ui.swing.UIUtilities;
import com.dmdirc.ui.swing.components.TextFrame;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyVetoException;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
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
    /** Parent menu list. */
    private final Map<FrameContainer, FrameContainerMenu> parentItemMap;
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

    /** 
     * Creates a new instance of WindowMenuFrameManager.
     */
    public WindowMenuFrameManager() {
        super();

        parentItemMap =
                new HashMap<FrameContainer, FrameContainerMenu>();
        menuItemMap =
                new HashMap<FrameContainer, FrameContainerMenuItem>();

        setText("Window");
        setMnemonic('w');
        WindowManager.addFrameManager(this);
        addMenuListener(this);

        toggleStateMenuItem = new JMenuItem();
        toggleStateMenuItem.setMnemonic('m');
        toggleStateMenuItem.setText("Maximise");
        toggleStateMenuItem.setActionCommand("ToggleState");
        toggleStateMenuItem.addActionListener(this);
        add(toggleStateMenuItem);

        minimiseMenuItem = new JMenuItem();
        minimiseMenuItem.setMnemonic('n');
        minimiseMenuItem.setText("Minimise");
        minimiseMenuItem.setActionCommand("Minimise");
        minimiseMenuItem.addActionListener(this);
        add(minimiseMenuItem);

        closeMenuItem = new JMenuItem();
        closeMenuItem.setMnemonic('c');
        closeMenuItem.setText("Close");
        closeMenuItem.setActionCommand("Close");
        closeMenuItem.addActionListener(this);
        add(closeMenuItem);

        checkToggleState();

        separator = new JPopupMenu.Separator();
        separator.setVisible(false);
        add(separator);

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
    public void addWindow(final FrameContainer window) {
        UIUtilities.invokeLater(new Runnable() {

            /** {@inheritDoc} */
            @Override
            public void run() {
                if (getMenuComponentCount() == itemCount) {
                    separator.setVisible(true);
                }

                final FrameContainerMenu pmi = new FrameContainerMenu(window);
                final FrameContainerMenuItem mi =
                        new FrameContainerMenuItem(window);
                synchronized (menuItemMap) {
                    if (isShowing()) {
                        setSelected(false);
                        setPopupMenuVisible(false);
                    }
                    parentItemMap.put(window, pmi);
                    menuItemMap.put(window, mi);
                    window.addSelectionListener(WindowMenuFrameManager.this);
                    if (IdentityManager.getGlobalConfig().
                            getOptionBool("treeview", "sortservers", true)) {
                        add(pmi, getIndex(WindowMenuFrameManager.this, window));
                    } else {
                        if (window instanceof GlobalWindow) {
                            add(pmi, itemCount);
                            pmi.add(mi);
                        } else {
                            add(pmi);
                            pmi.add(mi);
                        }
                    }
                }
            }
        });
    }

    /** {@inheritDoc} */
    @Override
    public void delWindow(final FrameContainer window) {
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
                        window.removeSelectionListener(WindowMenuFrameManager.this);
                    }
                }

                if (getMenuComponentCount() == itemCount) {
                    separator.setVisible(false);
                }
            }
        });
    }

    /** {@inheritDoc} */
    @Override
    public void addWindow(final FrameContainer parent,
            final FrameContainer window) {
        UIUtilities.invokeLater(new Runnable() {

            /** {@inheritDoc} */
            @Override
            public void run() {
                if (getMenuComponentCount() == itemCount) {
                    separator.setVisible(true);
                }

                final JMenu pmi = parentItemMap.get(parent);
                final FrameContainerMenuItem mi =
                        new FrameContainerMenuItem(window);
                synchronized (menuItemMap) {
                    if (isShowing()) {
                        setSelected(false);
                        setPopupMenuVisible(false);
                    }
                    menuItemMap.put(window, mi);
                    window.addSelectionListener(WindowMenuFrameManager.this);
                    pmi.add(mi, getIndex(pmi, window));
                }
            }
        });
    }

    /** {@inheritDoc} */
    @Override
    public void delWindow(final FrameContainer parent,
            final FrameContainer window) {
        UIUtilities.invokeLater(new Runnable() {

            /** {@inheritDoc} */
            @Override
            public void run() {
                synchronized (menuItemMap) {
                    if (isShowing()) {
                        setSelected(false);
                        setPopupMenuVisible(false);
                    }
                    final JMenu pmi = parentItemMap.get(parent);
                    final FrameContainerMenuItem mi = menuItemMap.get(window);
                    if (mi != null) {
                        pmi.remove(mi);
                        menuItemMap.remove(window);
                        window.removeSelectionListener(WindowMenuFrameManager.this);
                    }
                }

                if (getMenuComponentCount() == itemCount) {
                    separator.setVisible(false);
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
        if ("ToggleState".equals(e.getActionCommand())) {
            final Window window = Main.getUI().getActiveWindow();
            if (window != null) {
                try {
                    if (window.isMaximum()) {
                        ((TextFrame) Main.getUI().getActiveWindow()).restore();
                    } else {
                        ((TextFrame) Main.getUI().getActiveWindow()).maximise();
                    }
                    window.setMaximum(!window.isMaximum());
                } catch (PropertyVetoException ex) {
                    Logger.userError(ErrorLevel.LOW, "Unable to maximise window");
                }
            }
        } else if (e.getActionCommand().equals("Minimise")) {
            ((TextFrame) Main.getUI().getActiveWindow()).minimise();
        } else if (e.getActionCommand().equals("Close")) {
            ((TextFrame) Main.getUI().getActiveWindow()).close();
        }
    }

    /** {@inheritDoc} */
    @Override
    public void selectionChanged(final Window window) {
        final Map<FrameContainer, FrameContainerMenuItem> newMap =
                new HashMap<FrameContainer, FrameContainerMenuItem>();
        synchronized (menuItemMap) {
            newMap.putAll(menuItemMap);
        }

        for (FrameContainerMenuItem menuItem : newMap.values()) {
            menuItem.selectionChanged(window);
            final FrameContainerMenu pmi = parentItemMap.get(menuItem.getFrame());
            if (pmi != null) {
                pmi.selectionChanged(window.getContainer().getServer().getFrame());
            }
        }
    }

    /**
     * Checks and sets the state of the toggle menu item.
     */
    private void checkToggleState() {
        if (Main.getUI().getActiveWindow() == null) {
            toggleStateMenuItem.setEnabled(false);
            closeMenuItem.setEnabled(false);
            minimiseMenuItem.setEnabled(false);
        } else {
            toggleStateMenuItem.setEnabled(true);
            closeMenuItem.setEnabled(true);
            minimiseMenuItem.setEnabled(true);

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
     * @param menu Menu to check sort order
     * @param newChild new node to be inserted.
     *
     * @return index where new node is to be inserted.
     */
    private int getIndex(final JMenu menu, final FrameContainer newChild) {
        final int startCount;
        if (menu == this) {
            startCount = itemCount;
        } else {
            startCount = 1;
        }
        for (int i = startCount; i < menu.getMenuComponentCount(); i++) {
            if (!(menu.getMenuComponent(i) instanceof FrameContainerMenuItem) &&
                    !(menu.getMenuComponent(i) instanceof FrameContainerMenu)) {
                continue;
            }
            final FrameContainer child;
            if (menu.getMenuComponent(i) instanceof FrameContainerMenu) {
                child =
                        ((FrameContainerMenu) menu.getMenuComponent(i)).getFrame();
            } else {
                child =
                        ((FrameContainerMenuItem) menu.getMenuComponent(i)).getFrame();
            }
            if (sortBefore(newChild, child)) {
                return i;
            } else if (!sortAfter(newChild, child) && IdentityManager.getGlobalConfig().
                    getOptionBool("treeview", "sortwindows", false) && newChild.toString().
                    compareToIgnoreCase(
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
