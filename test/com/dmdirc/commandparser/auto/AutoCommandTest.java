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

package com.dmdirc.commandparser.auto;

import java.util.Optional;

import org.junit.Before;
import org.junit.Test;

import static junit.framework.TestCase.assertEquals;

public class AutoCommandTest {

    private AutoCommand command;

    @Before
    public void setUp() throws Exception {
        command = AutoCommand.create(Optional.empty(), Optional.empty(), Optional.empty(), "");
    }

    @Test
    public void testGetServer_Empty() throws Exception {
        command = AutoCommand.create(Optional.empty(), Optional.empty(), Optional.empty(), "");
        assertEquals(Optional.<String>empty(), command.getServer());
    }

    @Test
    public void testGetServer_NotEmpty() throws Exception {
        command = AutoCommand.create(Optional.of("server"), Optional.empty(), Optional.empty(), "");
        assertEquals(Optional.of("server"), command.getServer());
    }

    @Test
    public void testGetNetwork_Empty() throws Exception {
        command = AutoCommand.create(Optional.empty(), Optional.empty(), Optional.empty(), "");
        assertEquals(Optional.<String>empty(), command.getNetwork());
    }

    @Test
    public void testGetNetwork_NotEmpty() throws Exception {
        command = AutoCommand.create(Optional.empty(), Optional.of("network"), Optional.empty(), "");
        assertEquals(Optional.of("network"), command.getNetwork());
    }

    @Test
    public void testGetProfile_Empty() throws Exception {
        command = AutoCommand.create(Optional.empty(), Optional.empty(), Optional.empty(), "");
        assertEquals(Optional.<String>empty(), command.getProfile());
    }

    @Test
    public void testGetProfile_NotEmpty() throws Exception {
        command = AutoCommand.create(Optional.empty(), Optional.empty(), Optional.of("profile"), "");
        assertEquals(Optional.of("profile"), command.getProfile());
    }

    @Test
    public void testGetResponse_Empty() throws Exception {
        command = AutoCommand.create(Optional.empty(), Optional.empty(), Optional.empty(), "");
        assertEquals("", command.getResponse());
    }

    @Test
    public void testGetResponse_NotEmpty() throws Exception {
        command = AutoCommand.create(Optional.empty(), Optional.empty(), Optional.empty(), "response");
        assertEquals("response", command.getResponse());
    }

    @Test
    public void testGetType_Server() throws Exception {
        command = AutoCommand.create(Optional.of("server"), Optional.empty(), Optional.empty(),
                "response");
        assertEquals(AutoCommandType.SERVER, command.getType());
    }

    @Test
    public void testGetType_Network() throws Exception {
        command = AutoCommand.create(Optional.empty(), Optional.of("network"), Optional.empty(),
                "response");
        assertEquals(AutoCommandType.NETWORK, command.getType());
    }

    @Test
    public void testGetType_Global() throws Exception {
        command = AutoCommand.create(Optional.empty(), Optional.empty(), Optional.of("profile"),
                "response");
        assertEquals(AutoCommandType.GLOBAL, command.getType());
    }

    @Test
    public void testGetType_Unknown() throws Exception {
        command = AutoCommand.create(Optional.of("server"), Optional.of("network"), Optional.empty(),
                "response");
        assertEquals(AutoCommandType.UNKNOWN, command.getType());
    }
}