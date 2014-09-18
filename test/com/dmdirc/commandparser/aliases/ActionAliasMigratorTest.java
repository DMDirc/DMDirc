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

package com.dmdirc.commandparser.aliases;

import com.dmdirc.DMDircMBassador;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class ActionAliasMigratorTest {

    @Mock private AliasFactory aliasFactory;
    @Mock private AliasManager aliasManager;
    @Mock private DMDircMBassador eventBus;

    private FileSystem fs;
    private ActionAliasMigrator migrator1;
    private ActionAliasMigrator migrator2;
    private ActionAliasMigrator migrator3;

    @Before
    public void setup() throws IOException {
        fs = Jimfs.newFileSystem(Configuration.unix());

        Files.createDirectories(fs.getPath("test1/aliases"));
        Files.createDirectories(fs.getPath("test2/other-stuff/aliases"));
        Files.createDirectories(fs.getPath("test3/aliases"));

        Files.copy(getClass().getResource("op-greater-0").openStream(),
                fs.getPath("test1/aliases/op"));
        Files.copy(getClass().getResource("unset-equals-2").openStream(),
                fs.getPath("test1/aliases/unset"));
        Files.copy(getClass().getResource("no-trigger").openStream(),
                fs.getPath("test3/aliases/bad"));

        migrator1 = new ActionAliasMigrator(fs.getPath("test1"), aliasFactory,
                aliasManager, eventBus);
        migrator2 = new ActionAliasMigrator(fs.getPath("test2"), aliasFactory,
                aliasManager, eventBus);
        migrator3 = new ActionAliasMigrator(fs.getPath("test3"), aliasFactory,
                aliasManager, eventBus);
    }

    @Test
    public void testNeedMigration() {
        assertTrue(migrator1.needsMigration());
        assertFalse(migrator2.needsMigration());
    }

    @Test
    public void testDeletesFilesAfterMigration() {
        migrator1.migrate();
        assertFalse(Files.exists(fs.getPath("test1/aliases/unset")));
        assertFalse(Files.exists(fs.getPath("test1/aliases/op")));
        assertFalse(Files.exists(fs.getPath("test1/aliases")));
        assertTrue(Files.exists(fs.getPath("test1")));
    }

    @Test
    public void testLeavesOtherFilesDuringMigration() {
        migrator2.migrate();
        assertTrue(Files.exists(fs.getPath("test2/other-stuff/aliases")));
    }

    @Test
    public void testMigrationCreatesAliases() {
        migrator1.migrate();
        verify(aliasFactory).createAlias("op", 1, "/mode ooooooooooooooo $1-");
        verify(aliasFactory).createAlias("unset", 2, "/set --unset $1-");
    }

    @Test
    public void testBadActionsLeftOnDisk() {
        migrator3.migrate();
        assertTrue(Files.exists(fs.getPath("test3/aliases/bad")));
    }

}