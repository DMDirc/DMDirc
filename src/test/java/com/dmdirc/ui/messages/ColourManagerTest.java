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

package com.dmdirc.ui.messages;

import com.dmdirc.DMDircMBassador;
import com.dmdirc.interfaces.config.AggregateConfigProvider;
import com.dmdirc.interfaces.config.ConfigChangeListener;
import com.dmdirc.logger.ErrorManager;
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ColourManagerTest {

    @Mock private ErrorManager errorManager;
    @Mock private AggregateConfigProvider configManager;
    @Captor private ArgumentCaptor<ConfigChangeListener> configListener;
    @Mock private DMDircMBassador eventBus;
    private ColourManager manager;

    @Before
    public void setup() {
        manager = new ColourManager(configManager);
        verify(configManager).addChangeListener(anyString(), configListener.capture());
    }

    @Test
    public void testGetColourInt() {
        final int spec = 4;

        final Colour expResult = Colour.RED;
        final Colour result = manager.getColourFromIrcCode(spec);
        assertEquals(expResult, result);
    }

    @Test
    public void testGetColourOOB() {
        final Colour result = manager.getColourFromIrcCode(20);
        assertEquals(Colour.WHITE, result);
    }

    @Test
    public void testGetColourHexInvalid() {
        final String spec = "FFZZFF";

        final Colour result = manager.getColourFromHex(spec);
        assertEquals(Colour.WHITE, result);
    }

    @Test
    public void testGetColourHex() {
        final String spec = "FFFFFF";

        final Colour expResult = Colour.WHITE;
        final Colour result = manager.getColourFromHex(spec);
        assertEquals(expResult, result);
    }

    @Test
    public void testParseColourNull() {
        final Colour fallback = Colour.RED;
        final Colour result = manager.getColourFromString(null, fallback);

        assertEquals(fallback, result);
    }

    @Test
    public void testParseColourInvalidNumber() {
        final Colour fallback = Colour.RED;
        final Colour result = manager.getColourFromString("zz", fallback);

        assertEquals(fallback, result);
    }

    @Test
    public void testParseColourOOBNumber() {
        final Colour fallback = Colour.RED;
        final Colour result = manager.getColourFromString("20", fallback);

        assertEquals(fallback, result);
    }

    @Test
    public void testParseColourShortNumber() {
        final Colour fallback = Colour.RED;
        final Colour result = manager.getColourFromString("1234", fallback);

        assertEquals(fallback, result);
    }

    @Test
    public void testColourCache() {
        final Colour result1 = manager.getColourFromString("ff0f0f", Colour.WHITE);
        final Colour result2 = manager.getColourFromString("ff0f0f", Colour.WHITE);
        final Colour result3 = manager.getColourFromHex("ff0f0f");

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
