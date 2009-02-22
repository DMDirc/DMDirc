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

package com.dmdirc.updater;

import com.dmdirc.config.IdentityManager;
import com.dmdirc.updater.components.ClientComponent;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

public class UpdateTest {

    @BeforeClass
    public static void setUpClass() throws Exception {
        IdentityManager.load();
    }
    
    private final String subject = "outofdate client channel date version url";
    
    private Update update;

    @Before
    public void setUp() throws Exception {
        UpdateChecker.init();
        update = new Update(subject);
    }

    @Test
    public void testGetComponent() {
        assertTrue(update.getComponent() instanceof ClientComponent);
    }

    @Test
    public void testGetRemoteVersion() {
        assertEquals("version", update.getRemoteVersion());
    }

    @Test
    public void testGetUrl() {
        assertEquals("url", update.getUrl());
    }
    
}
