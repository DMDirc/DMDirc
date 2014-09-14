/*
 * Copyright (c) 2006-2014 DMDirc Developers
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

package com.dmdirc.ui.messages;

import com.dmdirc.DMDircMBassador;
import com.dmdirc.interfaces.config.AggregateConfigProvider;
import com.dmdirc.interfaces.config.ConfigChangeListener;
import com.dmdirc.logger.ErrorManager;
import com.dmdirc.logger.Logger;
import com.dmdirc.util.colours.Colour;
import com.dmdirc.util.colours.ColourUtils;
import com.dmdirc.util.validators.ColourValidator;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ColourManagerTest {

    @Mock private ErrorManager errorManager;
    @Mock private AggregateConfigProvider configManager;
    @Captor private ArgumentCaptor<ConfigChangeListener> configListener;
    @Mock private DMDircMBassador eventBus;
    private ColourManager manager;

    @Before
    public void setup() {
        Logger.setErrorManager(errorManager);

        manager = new ColourManager(configManager, eventBus);
        verify(configManager).addChangeListener(anyString(), configListener.capture());
    }

    @Test
    public void testGetColourInt() {
        int spec = 4;

        Colour expResult = Colour.RED;
        Colour result = manager.getColourFromIrcCode(spec);
        assertEquals(expResult, result);
    }

    @Test
    public void testGetColourOOB() {
        Colour result = manager.getColourFromIrcCode(20);
        assertEquals(Colour.WHITE, result);
    }

    @Test
    public void testGetColourHexInvalid() {
        String spec = "FFZZFF";

        Colour result = manager.getColourFromHex(spec);
        assertEquals(Colour.WHITE, result);
    }

    @Test
    public void testGetColourHex() {
        String spec = "FFFFFF";

        Colour expResult = Colour.WHITE;
        Colour result = manager.getColourFromHex(spec);
        assertEquals(expResult, result);
    }

    @Test
    public void testParseColourNull() {
        Colour fallback = Colour.RED;
        Colour result = manager.getColourFromString(null, fallback);

        assertEquals(fallback, result);
    }

    @Test
    public void testParseColourInvalidNumber() {
        Colour fallback = Colour.RED;
        Colour result = manager.getColourFromString("zz", fallback);

        assertEquals(fallback, result);
    }

    @Test
    public void testParseColourOOBNumber() {
        Colour fallback = Colour.RED;
        Colour result = manager.getColourFromString("20", fallback);

        assertEquals(fallback, result);
    }

    @Test
    public void testParseColourShortNumber() {
        Colour fallback = Colour.RED;
        Colour result = manager.getColourFromString("1234", fallback);

        assertEquals(fallback, result);
    }

    @Test
    public void testColourCache() {
        Colour result1 = manager.getColourFromString("ff0f0f", Colour.WHITE);
        Colour result2 = manager.getColourFromString("ff0f0f", Colour.WHITE);
        Colour result3 = manager.getColourFromHex("ff0f0f");

        assertSame(result1, result2);
        assertSame(result2, result3);
    }

    @Test
    public void testCustomColours() {
        when(configManager.hasOptionString(eq("colour"), eq("4"), any(ColourValidator.class))).thenReturn(true);
        when(configManager.getOptionString("colour", "4")).thenReturn("00FF00");
        configListener.getValue().configChanged("colour", "4");
        assertEquals("00ff00", ColourUtils.getHex(manager.getColourFromIrcCode(4)).toLowerCase());
    }

    @Test
    public void testCustomColours2() {
        when(configManager.hasOptionString(eq("colour"), eq("4"), any(ColourValidator.class))).thenReturn(true);
        when(configManager.getOptionString("colour", "4")).thenReturn("00FF00");
        configListener.getValue().configChanged("colour", "4");

        when(configManager.hasOptionString(eq("colour"), eq("4"), any(ColourValidator.class))).thenReturn(false);
        configListener.getValue().configChanged("colour", "4");
        assertEquals("ff0000", ColourUtils.getHex(manager.getColourFromIrcCode(4)).toLowerCase());
    }
}
