/*
 * Copyright (c) 2006-2015 DMDirc Developers
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
import com.dmdirc.DMDircMBassador;
import com.dmdirc.Precondition;
import com.dmdirc.events.FrameClosingEvent;
import com.dmdirc.interfaces.WindowModel;
import com.dmdirc.interfaces.ui.FrameListener;
import com.dmdirc.util.collections.ListenerList;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;

import javax.inject.Inject;
import javax.inject.Singleton;

import net.engio.mbassy.listener.Handler;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * The WindowManager maintains a list of all open windows, and their parent/child relations.
 */
@Singleton
public class WindowManager {

    /** A list of root windows. */
    private final Collection<WindowModel> rootWindows = new CopyOnWriteArrayList<>();
    /** Mapping of windows to their parents. */
    private final Map<WindowModel, WindowModel> parents = new HashMap<>();
    /** Mapping of parents to their children. */
    private final Multimap<WindowModel, WindowModel> children = ArrayListMultimap.create();
    /** Mapping of IDs to windows. */
    private final Map<String, WindowModel> windowsById = new HashMap<>();
    /** A list of frame listeners. */
    private final ListenerList listeners = new ListenerList();
    /** Counter to use for ID assignments. */
    private final AtomicLong nextId = new AtomicLong(0L);

    /**
     * Creates a new instance of {@link WindowManager}.
     */
    @Inject
    public WindowManager(final DMDircMBassador eventBus) {
        eventBus.subscribe(this);
    }

    /**
     * Registers a FrameListener with the WindowManager.
     *
     * @param frameListener The frame listener to be registered
     */
    @Precondition("The specified FrameListener is not null")
    public void addListener(final FrameListener frameListener) {
        checkNotNull(frameListener);

        listeners.add(FrameListener.class, frameListener);
    }

    /**
     * Registers a FrameListener with the WindowManager, and then calls the relevant methods on it
     * for all existing windows.
     *
     * @param frameListener The frame listener to be registered
     *
     * @since 0.6.6
     */
    @Precondition("The specified FrameListener is not null")
    public void addListenerAndSync(final FrameListener frameListener) {
        addListener(frameListener);

        for (WindowModel root : rootWindows) {
            frameListener.addWindow(root, true);

            for (WindowModel child : getChildren(root)) {
                fireAddWindow(frameListener, root, child);
            }
        }
    }

    /**
     * Recursively fires the addWindow callback for the specified windows and listener.
     *
     * @param listener The listener to be fired
     * @param parent   The parent window
     * @param child    The new child window that was added
     *
     */
    private void fireAddWindow(final FrameListener listener,
            final WindowModel parent, final WindowModel child) {
        listener.addWindow(parent, child, true);

        for (WindowModel grandchild : getChildren(child)) {
            fireAddWindow(listener, child, grandchild);
        }
    }

    /**
     * Unregisters a FrameListener with the WindowManager.
     *
     * @param frameListener The frame listener to be removed
     */
    public void removeListener(final FrameListener frameListener) {
        listeners.remove(FrameListener.class, frameListener);
    }

    /**
     * Gets the parent of the specified window, if there is one.
     *
     * @param window The window to find the parent of.
     * @return The window's parent, if one exists.
     */
    public Optional<WindowModel> getParent(final WindowModel window) {
        return Optional.ofNullable(parents.get(window));
    }

    /**
     * Gets the collection of children belonging to the specified window.
     *
     * @param window The window to find the children on.
     * @return A (possibly empty) collection of children of the given window.
     */
    public Collection<WindowModel> getChildren(final WindowModel window) {
        return Collections.unmodifiableCollection(children.get(window));
    }

    /**
     * Adds a new root window to the Window Manager.
     *
     * @param window The window to be added
     *
     * @since 0.6.4
     */
    @Precondition({
        "The specified Window is not null",
        "The specified Window has not already been added"
    })
    public void addWindow(final WindowModel window) {
        addWindow(window, true);
    }

    /**
     * Adds a new root window to the Window Manager.
     *
     * @param window The window to be added
     * @param focus  Should this window become focused
     *
     * @since 0.6.4
     */
    @Precondition({
        "The specified Window is not null",
        "The specified Window has not already been added"
    })
    public void addWindow(final WindowModel window, final boolean focus) {
        checkNotNull(window);
        checkArgument(!rootWindows.contains(window));

        rootWindows.add(window);
        assignId(window);
        fireAddWindow(window, focus);
    }

    /**
     * Adds a new child window to the Window Manager.
     *
     * @param parent The parent window
     * @param child  The child window to be added
     *
     * @since 0.6.4
     */
    @Precondition("The specified Windows are not null")
    public void addWindow(final WindowModel parent, final WindowModel child) {
        addWindow(parent, child, true);
    }

    /**
     * Adds a new child window to the Window Manager.
     *
     * @param parent The parent window
     * @param child  The child window to be added
     * @param focus  Should this window become focused
     *
     * @since 0.6.4
     */
    @Precondition({
        "The specified containers are not null",
        "The specified parent is in the window hierarchy already",
        "The specified child is NOT in the window hierarchy already"
    })
    public void addWindow(final WindowModel parent, final WindowModel child, final boolean focus) {
        checkNotNull(parent);
        checkArgument(isInHierarchy(parent));
        checkNotNull(child);

        parents.put(child, parent);
        children.put(parent, child);
        assignId(child);
        fireAddWindow(parent, child, focus);
    }

    /**
     * Recursively determines if the specified target is in the known hierarchy of containers. That
     * is, whether or not the specified target or any of its parents are root windows.
     *
     * @since 0.6.4
     * @param target The container to be tested
     *
     * @return True if the target is in the hierarchy, false otherwise
     */
    protected boolean isInHierarchy(final WindowModel target) {
        return rootWindows.contains(target) || parents.containsKey(target);
    }

    /**
     * Removes a window from the Window Manager. If the specified window has child windows, they are
     * recursively removed before the target window. If the window hasn't previously been added, the
     * request to remove it is ignored.
     *
     * @param window The window to be removed
     *
     * @since 0.6.4
     */
    @Precondition({
        "The specified window is not null",
        "The specified window is in the window hierarchy"
    })
    public void removeWindow(final WindowModel window) {
        checkNotNull(window);
        checkArgument(isInHierarchy(window));

        children.get(window).forEach(WindowModel::close);
        children.removeAll(window);
        windowsById.remove(window.getId());

        if (rootWindows.contains(window)) {
            fireDeleteWindow(window);
            rootWindows.remove(window);
        } else {
            final WindowModel parent = parents.get(window);
            fireDeleteWindow(parent, window);
            parents.remove(parent);
        }
    }

    /**
     * Finds and returns a global custom window with the specified name. If a custom window with the
     * specified name isn't found, null is returned.
     *
     * @param name The name of the custom window to search for
     *
     * @return The specified custom window, or null
     */
    @Precondition("The specified window name is not null")
    public WindowModel findCustomWindow(final String name) {
        checkNotNull(name);

        return findCustomWindow(rootWindows, name);
    }

    /**
     * Finds and returns a non-global custom window with the specified name. If a custom window with
     * the specified name isn't found, null is returned.
     *
     * @param parent The parent whose children should be searched
     * @param name   The name of the custom window to search for
     *
     * @return The specified custom window, or null
     */
    @Precondition({
        "The specified window name is not null",
        "The specified parent window is not null",
        "The specified parent window has been added to the Window Manager"
    })
    public WindowModel findCustomWindow(final WindowModel parent, final String name) {
        checkNotNull(parent);
        checkNotNull(name);

        return findCustomWindow(getChildren(parent), name);
    }

    /**
     * Finds a custom window with the specified name among the specified list of windows. If the
     * custom window is not found, returns null.
     *
     * @param windows The list of windows to search
     * @param name    The name of the custom window to search for
     *
     * @return The custom window if found, or null otherwise
     */
    private WindowModel findCustomWindow(final Iterable<WindowModel> windows, final String name) {
        for (WindowModel window : windows) {
            if (window instanceof CustomWindow && window.getName().equals(name)) {
                return window;
            }
        }

        return null;
    }

    /**
     * Retrieves all known root (parent-less) windows.
     *
     * @since 0.6.4
     * @return A collection of all known root windows.
     */
    public Collection<WindowModel> getRootWindows() {
        return Collections.unmodifiableCollection(rootWindows);
    }

    /**
     * Retrieves the window with the specified ID.
     *
     * @param id The ID of the window to retrieve
     * @return The window with the given ID, if it exists.
     */
    public Optional<WindowModel> getWindowById(final String id) {
        return Optional.ofNullable(windowsById.get(id));
    }

    /**
     * Assigns a unique ID to the given window.
     *
     * @param window The window to assign an ID to.
     */
    private void assignId(final WindowModel window) {
        final String id = "WINDOW/" + nextId.getAndIncrement();
        window.setId(id);
        windowsById.put(id, window);
    }

    /**
     * Fires the addWindow(Window) callback.
     *
     * @param window The window that was added
     * @param focus  Should this window become focused
     */
    private void fireAddWindow(final WindowModel window, final boolean focus) {
        for (FrameListener listener : listeners.get(FrameListener.class)) {
            listener.addWindow(window, focus);
        }
    }

    /**
     * Fires the addWindow(Window, Window) callback.
     *
     * @param parent The parent window
     * @param child  The new child window that was added
     * @param focus  Should this window become focused
     *
     */
    private void fireAddWindow(final WindowModel parent,
            final WindowModel child, final boolean focus) {
        for (FrameListener listener : listeners.get(FrameListener.class)) {
            listener.addWindow(parent, child, focus);
        }
    }

    /**
     * Fires the delWindow(Window) callback.
     *
     * @param window The window that was removed
     */
    private void fireDeleteWindow(final WindowModel window) {
        for (FrameListener listener : listeners.get(FrameListener.class)) {
            listener.delWindow(window);
        }
    }

    /**
     * Fires the delWindow(Window, Window) callback.
     *
     * @param parent The parent window
     * @param child  The child window that was removed
     */
    private void fireDeleteWindow(final WindowModel parent, final WindowModel child) {
        for (FrameListener listener : listeners.get(FrameListener.class)) {
            listener.delWindow(parent, child);
        }
    }

    @Handler
    public void frameClosing(final FrameClosingEvent event) {
        removeWindow(event.getSource());
    }

}
