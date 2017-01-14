/*
 * Copyright (c) 2006-2017 DMDirc Developers
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

package com.dmdirc;

import com.dmdirc.parser.interfaces.Parser;

import java.util.Optional;

import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class ServerStatusTest {

    @Test(expected=IllegalArgumentException.class)
    public void testIllegalTransition() {
        final Server server = mock(Server.class);
        when(server.getParser()).thenReturn(Optional.empty());
        final ServerStatus status = new ServerStatus(server, mock(Object.class));
        status.transition(ServerState.CONNECTED);
    }

    @Test
    public void testGetParserIdSame() {
        final ServerStatus status = new ServerStatus(mock(Server.class), mock(Object.class));
        final Parser parser = mock(Parser.class);

        assertEquals(status.getParserID(parser), status.getParserID(parser));
    }

    @Test
    public void testGetParserIdDifferent() {
        final ServerStatus status = new ServerStatus(mock(Server.class), mock(Object.class));
        final Parser parser1 = mock(Parser.class);
        final Parser parser2 = mock(Parser.class);

        assertTrue(status.getParserID(parser1) != status.getParserID(parser2));
    }

    @Test
    public void testGetParserIdNull() {
        final ServerStatus status = new ServerStatus(mock(Server.class), mock(Object.class));

        assertEquals(0, status.getParserID(null));
    }

}
