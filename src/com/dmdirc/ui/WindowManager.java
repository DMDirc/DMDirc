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

package com.dmdirc.ui;

import com.dmdirc.CustomWindow;
import com.dmdirc.Precondition;
import com.dmdirc.logger.ErrorLevel;
import com.dmdirc.logger.Logger;
import com.dmdirc.ui.interfaces.FrameManager;
import com.dmdirc.ui.interfaces.Window;
import com.dmdirc.util.MapList;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

/**
 * The WindowManager maintains a list of all open windows, and their
 * parent/child relations.
 *
 * @author Chris
 */
public class WindowManager {

    /** A list of root windows. */
    private final static List<Window> rootWindows
            = new ArrayList<Window>();

    /** A map of parent windows to their children. */
    private final static MapList<Window, Window> childWindows
            = new MapList<Window, Window>();

    /** A list of frame managers. */
    private final static List<FrameManager> frameManagers
            = new ArrayList<FrameManager>();

    /**
     * Creates a new instance of WindowManager.
     */
    private WindowManager() {
        // Shouldn't be instansiated
    }

    /**
     * Registers a FrameManager with the WindowManager.
     *
     * @param frameManager The frame manager to be registered
     */
    @Precondition({
        "The specified FrameManager is not null",
        "The specified FrameManager has not already been added"
    })
    public static void addFrameManager(final FrameManager frameManager) {
        assert(frameManager != null);
        assert(!frameManagers.contains(frameManager));

        frameManagers.add(frameManager);
    }

    /**
     * Unregisters a FrameManager with the WindowManager.
     *
     * @param frameManager The frame manager to be removed
     */
    @Precondition({
        "The specified FrameManager is not null",
        "The specified FrameManager has already been added and not removed"
    })
    public static void removeFrameManager(final FrameManager frameManager) {
        assert(frameManager != null);
        assert(frameManagers.contains(frameManager));

        frameManagers.remove(frameManager);
    }

    /**
     * Adds a new root window to the Window Manager.
     *
     * @param window The window to be added
     */
    @Precondition({
        "The specified Window is not null",
        "The specified Window has not already been added"
    })
    public static void addWindow(final Window window) {
        assert(window != null);
        assert(!rootWindows.contains(window));

        rootWindows.add(window);
        childWindows.add(window);

        fireAddWindow(window);
    }

    /**
     * Adds a new child window to the Window Manager.
     *
     * @param parent The parent window
     * @param child The child window to be added
     */
    @Precondition({
        "The specified Windows are not null",
        "The parent Window has already been added",
        "The child Window has not already been added"
    })
    public static void addWindow(final Window parent, final Window child) {
        assert(parent != null);
        assert(child != null);
        assert(childWindows.containsKey(parent));
        assert(!childWindows.containsKey(child));

        childWindows.add(parent, child);
        childWindows.add(child);

        fireAddWindow(parent, child);
    }

    /**
     * Removes a window from the Window Manager. If the specified window
     * has child windows, they are recursively removed before the target window.
     *
     * @param window The window to be removed
     */
    @Precondition({
        "The specified Window is not null",
        "The specified Window has already been added and not removed"
    })
    public static void removeWindow(final Window window) {
        assert(window != null);
        assert(childWindows.containsKey(window));

        if (childWindows.get(window) != null && !childWindows.get(window).isEmpty()) {
            for (Window child : new ArrayList<Window>(childWindows.get(window))) {
                removeWindow(child);
            }
        }

        childWindows.remove(window);

        if (rootWindows.contains(window)) {
            rootWindows.remove(window);

            fireDeleteWindow(window);
        } else {
            final Window parent = getParent(window);

            if (parent == null) {
                Logger.appError(ErrorLevel.MEDIUM, "Invalid window removed",
                        new IllegalArgumentException("Tried to remove a" +
                        " non-root window that has no known parent.\nWindow:" +
                        " " + window.getTitle()));
                return;
            } else {
                childWindows.remove(parent, window);

                fireDeleteWindow(parent, window);
            }
        }
    }

    /**
     * Finds and returns a global custom window with the specified name.
     * If a custom window with the specified name isn't found, null is returned.
     * 
     * @param name The name of the custom window to search for
     * @return The specified custom window, or null
     */
    @Precondition("The specified window name is not null")
    public static Window findCustomWindow(final String name) {
        assert(name != null);

        return findCustomWindow(rootWindows, name);
    }

    /**
     * Finds and returns a non-global custom window with the specified name.
     * If a custom window with the specified name isn't found, null is returned.
     * 
     * @param parent The parent whose children should be searched
     * @param name The name of the custom window to search for
     * @return The specified custom window, or null
     */
    @Precondition({
        "The specified window name is not null",
        "The specified parent window is not null",
        "The specified parent window has been added to the Window Manager"
    })
    public static Window findCustomWindow(final Window parent, final String name) {
        assert(parent != null);
        assert(name != null);
        assert(childWindows.containsKey(parent));

        return findCustomWindow(childWindows.get(parent), name);
    }

    /**
     * Finds a custom window with the specified name among the specified list
     * of windows. If the custom window is not found, returns null.
     * 
     * @param windows The list of windows to search
     * @param name The name of the custom window to search for
     * @return The custom window if found, or null otherwise
     */
    private static Window findCustomWindow(final List<Window> windows, final String name) {
        for (Window window : windows) {
            if (window.getContainer() instanceof CustomWindow
                    && ((CustomWindow) window.getContainer()).getName().equals(name)) {
                return window;
            }
        }

        return null;
    }

    /**
     * Retrieves the parent window of the specified window. If the window
     * has no parent (i.e., it is a root window or it has not been added),
     * returns null.
     * 
     * @param window The window whose parent is being sought
     * @return The parent of the specified window, or null if not found
     */
    public static Window getParent(final Window window) {
        for (Entry<Window, List<Window>> entry : childWindows.entrySet()) {
            if (entry.getValue().contains(window)) {
                return entry.getKey();
            }
        }

        return null;
    }

    private static void fireAddWindow(final Window window) {
        for (FrameManager manager : frameManagers) {
            manager.addWindow(window.getContainer());
        }
    }

    private static void fireAddWindow(final Window parent, final Window child) {
        for (FrameManager manager : frameManagers) {
            manager.addWindow(parent.getContainer(), child.getContainer());
        }
    }

    private static void fireDeleteWindow(final Window window) {
        for (FrameManager manager : frameManagers) {
            manager.delWindow(window.getContainer());
        }
    }

    private static void fireDeleteWindow(final Window parent, final Window child) {
        for (FrameManager manager : frameManagers) {
            manager.delWindow(parent.getContainer(), child.getContainer());
        }
    }

}