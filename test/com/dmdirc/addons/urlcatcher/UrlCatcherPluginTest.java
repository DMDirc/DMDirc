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

package com.dmdirc.addons.urlcatcher;

import com.dmdirc.actions.CoreActionType;
import com.dmdirc.config.IdentityManager;
import com.dmdirc.config.InvalidIdentityFileException;
import org.junit.Test;
import static org.junit.Assert.*;

public class UrlCatcherPluginTest {

    @Test
    public void testURLCounting() throws InvalidIdentityFileException {
        IdentityManager.load();
        
        final UrlCatcherPlugin plugin = new UrlCatcherPlugin();

        plugin.processEvent(CoreActionType.CHANNEL_MESSAGE, null,
                null, "This is a message - http://www.google.com/ foo");
        plugin.processEvent(CoreActionType.CHANNEL_MESSAGE, null,
                null, "This is a message - http://www.google.com/ foo");
        plugin.processEvent(CoreActionType.CHANNEL_MESSAGE, null,
                null, "This is a message - http://www.google.com/ foo");
        
        assertEquals(1, plugin.getURLS().size());
        assertEquals(3, (int) plugin.getURLS().get("http://www.google.com/"));
    }
    
    @Test
    public void testURLCatching() throws InvalidIdentityFileException {
        IdentityManager.load();
        
        final UrlCatcherPlugin plugin = new UrlCatcherPlugin();

        plugin.processEvent(CoreActionType.CHANNEL_MESSAGE, null,
                null, "http://www.google.com/ www.example.com foo://bar.baz");
        plugin.processEvent(CoreActionType.CHANNEL_MESSAGE, null,
                null, "No URLs here, no sir!");        
        
        assertEquals(3, plugin.getURLS().size());
        assertTrue(plugin.getURLS().containsKey("http://www.google.com/"));
        assertTrue(plugin.getURLS().containsKey("www.example.com"));
        assertTrue(plugin.getURLS().containsKey("foo://bar.baz"));
    }    

}