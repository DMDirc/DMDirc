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

package com.dmdirc.addons.logging;

import com.dmdirc.FrameContainer;
import com.dmdirc.IconManager;
import com.dmdirc.Main;
import com.dmdirc.Server;
import com.dmdirc.config.ConfigManager;
import com.dmdirc.config.IdentityManager;
import com.dmdirc.ui.WindowManager;
import com.dmdirc.ui.interfaces.Window;

import java.util.Stack;

/**
 * Displays an extended history of a window.
 *
 * @author Chris
 */
public class HistoryWindow extends FrameContainer {
    
    /** The title of our window. */
    private final String title;
       
    /** The window we're using. */
    private Window myWindow;
    
    /** Our parent window. */
    private final Window parent;
    
    /**
     * Creates a new HistoryWindow.
     *
     * @param title The title of the window
     * @param reader The reader to use to get the history
     * @param parent The window this history window was opened from
     */
    public HistoryWindow(final String title, final ReverseFileReader reader, final Window parent) {
        super();
        
        this.title = title;
        this.parent = parent;
        
        icon = IconManager.getIconManager().getIcon("raw");
        
        myWindow = Main.getUI().getWindow(this);
        
        WindowManager.addWindow(parent, myWindow);
        
        myWindow.setTitle(title);
        myWindow.setFrameIcon(icon);
        myWindow.setVisible(true);
        
        final Stack<String> lines = reader.getLines(
                IdentityManager.getGlobalConfig().getOptionInt("plugin-Logging", "history.lines", 50000));
        while (lines.size() > 0) {
            myWindow.addLine(lines.pop(), false);
        }
    }
    
    /** {@inheritDoc} */
    public Window getFrame() {
        return myWindow;
    }
    
    /** {@inheritDoc} */
    public String toString() {
        return title;
    }
    
    /** {@inheritDoc} */
    public void close() {
        myWindow.setVisible(false);
        Main.getUI().getMainWindow().delChild(myWindow);
        WindowManager.removeWindow(myWindow);
        myWindow.close();
        myWindow = null;
    }
    
    /** {@inheritDoc} */
    public Server getServer() {
        return parent.getContainer().getServer();
    }
    
    /** {@inheritDoc} */
    @Override
    public ConfigManager getConfigManager() {
        return parent.getConfigManager();
    }    
}
