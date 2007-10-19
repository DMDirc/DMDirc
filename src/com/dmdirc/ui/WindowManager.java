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
import com.dmdirc.logger.Logger;
import com.dmdirc.ui.interfaces.FrameManager;
import com.dmdirc.ui.interfaces.Window;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class WindowManager {
    
    private final static List<Window> rootWindows
            = new ArrayList<Window>();
    
    private final static Map<Window, List<Window>> childWindows
            = new HashMap<Window, List<Window>>();
    
    private final static List<FrameManager> frameManagers
            = new ArrayList<FrameManager>();
    
    private WindowManager() {
        // Shouldn't be instansiated
    }
    
    public static void addFrameManager(final FrameManager frameManager) {
        Logger.doAssertion(frameManager != null);
        Logger.doAssertion(!frameManagers.contains(frameManager));
        
        frameManagers.add(frameManager);
    }
    
    public static void removeFrameManager(final FrameManager frameManager) {
        Logger.doAssertion(frameManager != null);
        Logger.doAssertion(frameManagers.contains(frameManager));
        
        frameManagers.remove(frameManager);
    }
    
    public static void addWindow(final Window window) {
        Logger.doAssertion(window != null);
        Logger.doAssertion(!rootWindows.contains(window));
        
        rootWindows.add(window);
        childWindows.put(window, new ArrayList<Window>());
        
        fireAddWindow(window);
    }
    
    public static void addWindow(final Window parent, final Window child) {
        Logger.doAssertion(parent != null, child != null);
        Logger.doAssertion(childWindows.containsKey(parent), !childWindows.containsKey(child));
        
        childWindows.get(parent).add(child);
        childWindows.put(child, new ArrayList<Window>());
        
        fireAddWindow(parent, child);
    }
    
    public static void removeWindow(final Window window) {
        Logger.doAssertion(window != null);
        Logger.doAssertion(childWindows.containsKey(window));
        
        if (!childWindows.get(window).isEmpty()) {
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
            
            childWindows.get(parent).remove(window);
            
            fireDeleteWindow(parent, window);
        }
    }
    
    public static Window findCustomWindow(final String name) {
        Logger.doAssertion(name != null);
        
        return findCustomWindow(rootWindows, name);
    }
    
    public static Window findCustomWindow(final Window parent, final String name) {
        Logger.doAssertion(parent != null, name != null);
        Logger.doAssertion(childWindows.containsKey(parent));
        
        return findCustomWindow(childWindows.get(parent), name);
    }
    
    private static Window findCustomWindow(final List<Window> windows, final String name) {
        for (Window window : windows) {
            if (window.getContainer() instanceof CustomWindow
                    && ((CustomWindow) window.getContainer()).getName().equals(name)) {
                return window;
            }
        }
        
        return null;
    }
    
    private static Window getParent(final Window window) {
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