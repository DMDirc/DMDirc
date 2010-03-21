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

package com.dmdirc.ui;

import com.dmdirc.CustomWindow;
import com.dmdirc.FrameContainer;
import com.dmdirc.Precondition;
import com.dmdirc.Server;
import com.dmdirc.actions.ActionManager;
import com.dmdirc.actions.CoreActionType;
import com.dmdirc.interfaces.SelectionListener;
import com.dmdirc.logger.ErrorLevel;
import com.dmdirc.logger.Logger;
import com.dmdirc.ui.interfaces.FrameListener;
import com.dmdirc.ui.interfaces.Window;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * The WindowManager maintains a list of all open windows, and their
 * parent/child relations.
 *
 * @author Chris
 */
public class WindowManager {

    /** A list of root windows. */
    private final static List<FrameContainer<?>> rootWindows
            = new CopyOnWriteArrayList<FrameContainer<?>>();
    
    /** A list of frame listeners. */
    private final static List<FrameListener> frameListeners
            = new ArrayList<FrameListener>();
    
    /** A list of selection listeners. */
    private final static List<SelectionListener> selListeners
            = new ArrayList<SelectionListener>();
    
    /** Our selection listener proxy. */
    private static final SelectionListener selectionListener
            = new WMSelectionListener();

    /** Active window. */
    private static FrameContainer<?> activeWindow;

    /**
     * Creates a new instance of WindowManager.
     */
    private WindowManager() {
        // Shouldn't be instansiated
    }

    /**
     * Registers a FrameListener with the WindowManager.
     *
     * @param frameListener The frame listener to be registered
     */
    @Precondition({
        "The specified FrameListener is not null",
        "The specified FrameListener has not already been added"
    })
    public static void addFrameListener(final FrameListener frameListener) {
        Logger.assertTrue(frameListener != null);
        Logger.assertTrue(!frameListeners.contains(frameListener));

        frameListeners.add(frameListener);
    }

    /**
     * Unregisters a FrameListener with the WindowManager.
     *
     * @param frameListener The frame listener to be removed
     */
    @Precondition({
        "The specified FrameListener is not null",
        "The specified FrameListener has already been added and not removed"
    })
    public static void removeFrameListener(final FrameListener frameListener) {
        Logger.assertTrue(frameListener != null);
        Logger.assertTrue(frameListeners.contains(frameListener));

        frameListeners.remove(frameListener);
    }
    
    /**
     * Registers the specified SelectionListener with the WindowManager.
     * 
     * @param listener The listener to be added
     */
    public static void addSelectionListener(final SelectionListener listener) {
        selListeners.add(listener);
    }
    
    /**
     * Unregisters the specified SelectionListener with the WindowManager.
     * 
     * @param listener The listener to be removed
     */
    public static void removeSelectionListener(final SelectionListener listener) {
        selListeners.remove(listener);
    }

    /**
     * Adds a new root window to the Window Manager.
     *
     * @param window The window to be added
     * @since 0.6.4
     */
    @Precondition({
        "The specified Window is not null",
        "The specified Window has not already been added"
    })
    public static void addWindow(final FrameContainer<?> window) {
        addWindow(window, true);
    }

    /**
     * Adds a new root window to the Window Manager.
     *
     * @param window The window to be added
     * @param focus Should this window become focused
     * @since 0.6.4
     */
    @Precondition({
        "The specified Window is not null",
        "The specified Window has not already been added"
    })
    public static void addWindow(final FrameContainer<?> window, final boolean focus) {
        Logger.assertTrue(window != null);
        Logger.assertTrue(!rootWindows.contains(window));

        rootWindows.add(window);

        window.addSelectionListener(selectionListener);

        fireAddWindow(window, focus);
    }

    /**
     * Adds a new root window to the Window Manager.
     *
     * @param window The window to be added
     * @deprecated Use {@link #addWindow(com.dmdirc.FrameContainer)}
     */
    @Deprecated
    public static void addWindow(final Window window) {
        Logger.assertTrue(window != null);

        addWindow(window.getContainer());
    }

    /**
     * Adds a new child window to the Window Manager.
     *
     * @param parent The parent window
     * @param child The child window to be added
     * @deprecated Use {@link #addWindow(com.dmdirc.FrameContainer, com.dmdirc.FrameContainer)}
     */
    @Deprecated
    public static void addWindow(final Window parent, final Window child) {
        Logger.assertTrue(parent != null);
        Logger.assertTrue(child != null);

        addWindow(parent.getContainer(), child.getContainer());
    }

    /**
     * Adds a new child window to the Window Manager.
     *
     * @param parent The parent window
     * @param child The child window to be added
     * @since 0.6.4
     */
    @Precondition({
        "The specified Windows are not null"
    })
    public static void addWindow(final FrameContainer<?> parent,
            final FrameContainer<?> child) {
        addWindow(parent, child, true);
    }

    /**
     * Adds a new child window to the Window Manager.
     *
     * @param parent The parent window
     * @param child The child window to be added
     * @param focus Should this window become focused
     * @since 0.6.4
     */
    @Precondition({
        "The specified Windows are not null"
    })
    public static void addWindow(final FrameContainer<?> parent,
            final FrameContainer<?> child, final boolean focus) {
        Logger.assertTrue(parent != null);
        Logger.assertTrue(child != null);

        parent.addChild(child);

        child.addSelectionListener(selectionListener);

        fireAddWindow(parent, child, focus);
    }

    /**
     * Removes a window from the Window Manager. If the specified window
     * has child windows, they are recursively removed before the target window.
     * If the window hasn't previously been added, the request to remove it is
     * ignored.
     * <p>
     * This method will not block, but may instead create a new thread in order
     * to wait for child windows to be terminated. {@link FrameContainer}s
     * calling this method should persist resource references until after the
     * {@link FrameContainer#windowClosed()} method is called, to ensure
     * correct state when the window deleted listeners are fired at a later
     * time. See the documentation at {@link FrameContainer#windowClosing()} for
     * further explanation.
     *
     * @see FrameContainer#windowClosing()
     * @param window The window to be removed
     * @deprecated Use {@link #removeWindow(com.dmdirc.FrameContainer)}
     */
    @Deprecated
    public static void removeWindow(final Window window) {
        Logger.assertTrue(window != null);
        removeWindow(window.getContainer());
    }

    /**
     * Removes a window from the Window Manager. If the specified window
     * has child windows, they are recursively removed before the target window.
     * If the window hasn't previously been added, the request to remove it is
     * ignored.
     * <p>
     * This method will not block, but may instead create a new thread in order
     * to wait for child windows to be terminated. {@link FrameContainer}s
     * calling this method should persist resource references until after the
     * {@link FrameContainer#windowClosed()} method is called, to ensure
     * correct state when the window deleted listeners are fired at a later
     * time. See the documentation at {@link FrameContainer#windowClosing()} for
     * further explanation.
     *
     * @see FrameContainer#windowClosing()
     * @param window The window to be removed
     * @since 0.6.4
     */
    @Precondition("The specified Window is not null")
    public static void removeWindow(final FrameContainer<?> window) {
        removeWindow(window, false);
    }

    /**
     * Removes a window from the Window Manager. If the specified window
     * has child windows, they are recursively removed before the target window.
     * If the window hasn't previously been added, the reques to remove it is
     * ignored.
     *
     * @param window The window to be removed
     * @param canWait Whether or not this method can wait for child windows
     * to be closed. If canWait is false, a new thread is created.
     * @since 0.6.3
     */
    @Precondition("The specified Window is not null")
    public static void removeWindow(final FrameContainer<?> window, final boolean canWait) {
        Logger.assertTrue(window != null);

        if (!window.getChildren().isEmpty()) {
            if (!canWait) {
                new Thread(new Runnable() {
                    /** {@inheritDoc} */
                    @Override
                    public void run() {
                        removeWindow(window, true);
                    }
                }, "WindowManager removeWindow thread").start();
                return;
            }

            for (FrameContainer<?> child : window.getChildren()) {
                child.close();
            }

            while (!window.getChildren().isEmpty()) {
                Thread.yield();
            }
        }

        if (rootWindows.contains(window)) {
            fireDeleteWindow(window);
            rootWindows.remove(window);

            if (rootWindows.isEmpty()) {
                // Last root window has been removed, show that the selected
                // window is now null
                fireSelectionChanged(null);
            }
        } else {
            final FrameContainer<?> parent = window.getParent();
            fireDeleteWindow(parent, window);

            if (parent == null) {
                Logger.appError(ErrorLevel.MEDIUM, "Invalid window removed",
                        new IllegalArgumentException("Tried to remove a" +
                        " non-root window that has no known parent.\nWindow:" +
                        " " + window.getName()));
                return;
            } else {
                parent.removeChild(window);
            }
        }
        
        window.removeSelectionListener(selectionListener);
        window.windowClosed();
    }

    /**
     * Finds and returns a global custom window with the specified name.
     * If a custom window with the specified name isn't found, null is returned.
     * 
     * @param name The name of the custom window to search for
     * @return The specified custom window, or null
     */
    @Precondition("The specified window name is not null")
    public static FrameContainer<?> findCustomWindow(final String name) {
        Logger.assertTrue(name != null);

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
    public static FrameContainer<?> findCustomWindow(final FrameContainer<?> parent,
            final String name) {
        Logger.assertTrue(parent != null);
        Logger.assertTrue(name != null);

        return findCustomWindow(parent.getChildren(), name);
    }

    /**
     * Finds a custom window with the specified name among the specified list
     * of windows. If the custom window is not found, returns null.
     * 
     * @param windows The list of windows to search
     * @param name The name of the custom window to search for
     * @return The custom window if found, or null otherwise
     */
    private static FrameContainer<?> findCustomWindow(final Collection<FrameContainer<?>> windows,
            final String name) {
        for (FrameContainer<?> window : windows) {
            if (window instanceof CustomWindow
                    && ((CustomWindow) window).getName().equals(name)) {
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
     * @deprecated Call {@link FrameContainer#getParent()} directly
     */
    @Deprecated
    public static FrameContainer<?> getParent(final FrameContainer<?> window) {
        return window.getParent();
    }
    
    /**
     * Retrieves all known root (parent-less) windows.
     * 
     * @since 0.6.4
     * @return A collection of all known root windows.
     */
    public static Collection<FrameContainer<?>> getRootWindows() {
        return rootWindows;
    }

    /**
     * Returns the current focused window.
     * 
     * @return Focused window or null
     * @since 0.6.3
     */
    public static FrameContainer<?> getActiveWindow() {
        return activeWindow;
    }

    /**
     * Returns the server associated with the currently focused window.
     *
     * @return The currently active server or null
     * @since 0.6.3
     */
    public static Server getActiveServer() {
        return activeWindow == null ? null : getActiveWindow().getServer();
    }
    
    /**
     * Retrieves all children of the specified window.
     * 
     * @since 0.6.4
     * @param window The window whose children are being requested
     * @return A collection of all known child windows.
     * @deprecated Call {@link FrameContainer#getChildren()} directly
     */
    @Deprecated
    public static Collection<FrameContainer<?>> getChildren(final FrameContainer<?> window) {
        return window.getChildren();
    }
    
    /**
     * Fires the addWindow(Window) callback.
     * 
     * @param window The window that was added
     * @param focus Should this window become focused
     */
    private static void fireAddWindow(final FrameContainer<?> window, final boolean focus) {
        for (FrameListener listener : frameListeners) {
            listener.addWindow(window, focus);
        }
    }

    /**
     * Fires the addWindow(Window, Window) callback.
     * 
     * @param parent The parent window
     * @param child The new child window that was added
     * @param focus Should this window become focused
     *
     */
    private static void fireAddWindow(final FrameContainer<?> parent,
            final FrameContainer<?> child, final boolean focus) {
        for (FrameListener listener : frameListeners) {
            listener.addWindow(parent, child, focus);
        }
    }

    /**
     * Fires the delWindow(Window) callback.
     * 
     * @param window The window that was removed
     */
    private static void fireDeleteWindow(final FrameContainer<?> window) {
        for (FrameListener listener : frameListeners) {
            listener.delWindow(window);
        }
    }

    /**
     * Fires the delWindow(Window, Window) callback.
     * 
     * @param parent The parent window
     * @param child The child window that was removed
     */
    private static void fireDeleteWindow(final FrameContainer<?> parent,
            final FrameContainer<?> child) {
        for (FrameListener listener : frameListeners) {
            listener.delWindow(parent, child);
        }
    }

    /**
     * Fires the selectionChanged(Window) callback, and triggers the
     * CLIENT_FRAME_CHANGED action.
     *
     * @param window The window that is now focused (or null)
     */
    private static void fireSelectionChanged(final FrameContainer<?> window) {
        for (SelectionListener listener : selListeners) {
            listener.selectionChanged(window);
        }

        ActionManager.processEvent(CoreActionType.CLIENT_FRAME_CHANGED, null, window);
    }
        
    /**
     * Proxy for selection events.
     */
    private static class WMSelectionListener implements SelectionListener {

        /** {@inheritDoc} */
        @Override
        public void selectionChanged(final FrameContainer<?> window) {
            activeWindow = window;

            fireSelectionChanged(window);
        }
        
    }

}