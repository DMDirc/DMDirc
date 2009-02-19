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

package com.dmdirc.addons.urlcatcher;

import com.dmdirc.actions.ActionManager;
import com.dmdirc.actions.interfaces.ActionType;
import com.dmdirc.actions.CoreActionType;
import com.dmdirc.commandparser.CommandManager;
import com.dmdirc.interfaces.ActionListener;
import com.dmdirc.plugins.Plugin;
import com.dmdirc.ui.messages.Styliser;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author chris
 */
public class UrlCatcherPlugin extends Plugin implements ActionListener {
    
    private final Map<String, Integer> urls = new HashMap<String, Integer>();
    
    private final UrlListCommand command = new UrlListCommand(this);

    /** {@inheritDoc} */
    @Override
    public void onLoad() {
        ActionManager.addListener(this, CoreActionType.CLIENT_LINE_ADDED);
        CommandManager.registerCommand(command);
    }

    /** {@inheritDoc} */
    @Override    
    public void onUnload() {
        ActionManager.removeListener(this);
        CommandManager.unregisterCommand(command);
    }

    /** {@inheritDoc} */
    @Override    
    public void processEvent(final ActionType type, final StringBuffer format,
            final Object... arguments) {
        final String message = Styliser.doLinks((String) arguments[1]);
        
        if (message.indexOf(Styliser.CODE_HYPERLINK) > -1) {
            final String[] parts = message.split("" + Styliser.CODE_HYPERLINK);
            
            for (int i = 1; i < parts.length; i += 2) {
                addURL(parts[i]);
            }
        }
    }
    
    /**
     * Adds an URL to the list of tracked URLs, or increases its counter by one.
     * 
     * @param url The URL to be added
     */
    private void addURL(final String url) {
        if (urls.containsKey(url)) {
            urls.put(url, urls.get(url) + 1);
        } else {
            urls.put(url, 1);
        }
    }
    
    /**
     * Retrieves the URLs that have been recorded.
     * 
     * @return A map of URLs to the number of times they've been seen.
     */
    public Map<String, Integer> getURLS() {
        return urls;
    }

}
