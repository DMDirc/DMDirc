/*
 * Copyright (c) 2006-2013 DMDirc Developers
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

package com.dmdirc.config.prefs;

import com.dmdirc.actions.CoreActionType;
import com.dmdirc.config.ConfigManager;
import com.dmdirc.interfaces.ActionController;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.Mockito.*;

public class PreferencesManagerTest {

    private ActionController actionController;
    private ConfigManager configManager;
    private PreferencesManager manager;

    @Before
    public void setup() {
        this.actionController = mock(ActionController.class);
        this.configManager = mock(ConfigManager.class);
        this.manager = new PreferencesManager(actionController);
    }

    @Test
    public void testGettingChannelPrefsRaisesAction() {
        this.manager.getChannelSettings(this.configManager, null);

        verify(this.actionController).triggerEvent(
                eq(CoreActionType.CLIENT_PREFS_REQUESTED),
                (StringBuffer) isNull(),
                any(PreferencesCategory.class),
                eq(Boolean.FALSE));
    }

    @Test
    public void testGettingServerPrefsRaisesAction() {
        this.manager.getServerSettings(this.configManager, null);

        verify(this.actionController).triggerEvent(
                eq(CoreActionType.CLIENT_PREFS_REQUESTED),
                (StringBuffer) isNull(),
                any(PreferencesCategory.class),
                eq(Boolean.TRUE));
    }

}
