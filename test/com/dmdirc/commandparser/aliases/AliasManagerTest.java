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

package com.dmdirc.commandparser.aliases;

import com.dmdirc.commandparser.CommandType;
import com.dmdirc.commandparser.commands.Command;
import com.dmdirc.interfaces.CommandController;

import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class AliasManagerTest {

    @Mock private CommandController controller;
    private AliasManager manager;
    private Alias alias1;
    private Alias alias2;

    @Before
    public void setUp() {
        manager = new AliasManager(controller);
        alias1 = new Alias(CommandType.TYPE_CHAT, "alias", 1, "exit");
        alias2 = new Alias(CommandType.TYPE_CHAT, "alias", 1, "exit");
    }

    @Test
    public void testAddedAliasReturnedByGetters() {
        manager.addAlias(alias1);

        final Set<String> names = manager.getAliasNames();
        assertEquals(1, names.size());
        assertTrue(names.contains(alias1.getName()));

        final Set<Alias> aliases = manager.getAliases();
        assertEquals(1, aliases.size());
        assertTrue(aliases.contains(alias1));
    }

    @Test
    public void testAddedAliasRegisteredWithController() {
        manager.addAlias(alias1);

        verify(controller).registerCommand(any(Command.class), eq(alias1));
    }

    @Test
    public void testAddingAliasWithSameNameReplacesExisting() {
        manager.addAlias(alias1);
        manager.addAlias(alias2);

        final Set<String> names = manager.getAliasNames();
        assertEquals(1, names.size());
        assertTrue(names.contains(alias1.getName()));

        final Set<Alias> aliases = manager.getAliases();
        assertEquals(1, aliases.size());
        assertTrue(aliases.contains(alias2));
    }

    @Test
    public void testRemovedAliasNotReturnedByGetters() {
        manager.addAlias(alias1);
        manager.removeAlias(alias1);

        final Set<String> names = manager.getAliasNames();
        assertEquals(0, names.size());

        final Set<Alias> aliases = manager.getAliases();
        assertEquals(0, aliases.size());
    }

    @Test
    public void testRemovedAliasByNameNotReturnedByGetters() {
        manager.addAlias(alias1);
        manager.removeAlias("alias");

        final Set<String> names = manager.getAliasNames();
        assertEquals(0, names.size());

        final Set<Alias> aliases = manager.getAliases();
        assertEquals(0, aliases.size());
    }

    @Test
    public void testRemovedAliasByCopyNotReturnedByGetters() {
        manager.addAlias(alias1);
        manager.removeAlias(alias2);

        final Set<String> names = manager.getAliasNames();
        assertEquals(0, names.size());

        final Set<Alias> aliases = manager.getAliases();
        assertEquals(0, aliases.size());
    }

    @Test
    public void testRemovedAliasUnregisteredWithController() {
        manager.addAlias(alias1);
        manager.removeAlias(alias2);

        verify(controller).unregisterCommand(alias1);
    }

}
