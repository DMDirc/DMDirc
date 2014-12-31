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

import com.google.common.collect.Sets;
import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.util.Optional;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import static junit.framework.TestCase.assertFalse;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class YamlAutoCommandStoreTest {

    private FileSystem fs;
    private YamlAutoCommandStore ycs;
    private AutoCommand command;
    private AutoCommand command1;
    private AutoCommand command2;
    private AutoCommand command3;
    private AutoCommand command4;

    @Before
    public void setup() throws IOException {
        fs = Jimfs.newFileSystem(Configuration.unix());
        Files.copy(getClass().getResource("readtest.yml").openStream(),
                fs.getPath("readtest.yml"));
        command = AutoCommand.create(Optional.ofNullable("server"),
                Optional.ofNullable("network"),
                Optional.ofNullable("profile"),
                "command");
        command1 = AutoCommand.create(Optional.ofNullable("server1"),
                Optional.ofNullable("network1"),
                Optional.ofNullable("profile1"), "command1");
        command2 = AutoCommand.create(Optional.ofNullable("server2"),
                Optional.ofNullable("network2"),
                Optional.<String>empty(),
                "command2");
        command3 = AutoCommand.create(Optional.<String>empty(),
                Optional.ofNullable("network3"), Optional.<String>empty(),
                "command3");
        command4 = AutoCommand.create(Optional.<String>empty(),
                Optional.<String>empty(),
                Optional.<String>empty(),
                "command4");
    }

    @Test
    public void testReadAutoCommands() {
        ycs = new YamlAutoCommandStore(fs.getPath("readtest.yml"));
        final Set<AutoCommand> commands = ycs.readAutoCommands();
        assertTrue("Command 1 not present", commands.contains(command1));
        assertTrue("Command 2 not present", commands.contains(command2));
        assertTrue("Command 3 not present", commands.contains(command3));
        assertTrue("Command 4 not present", commands.contains(command4));
    }

    @Test
    public void testWriteAutoCommands() throws IOException {
        ycs = new YamlAutoCommandStore(fs.getPath("store.yml"));
        assertEquals(0, ycs.readAutoCommands().size());
        assertFalse(Files.exists(fs.getPath("store.yml")));
        ycs.writeAutoCommands(Sets.newHashSet(command));
        final Set<AutoCommand> commands = ycs.readAutoCommands();
        assertTrue("Command not present", commands.contains(command));
        assertTrue(Files.exists(fs.getPath("store.yml")));
    }

}