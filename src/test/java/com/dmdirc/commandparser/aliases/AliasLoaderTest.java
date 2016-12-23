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

import com.google.common.collect.Sets;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.ArgumentMatchers.anySetOf;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AliasLoaderTest {

    @Mock private AliasManager aliasManager;
    @Mock private AliasStore aliasStore;
    @Mock private Alias alias1;
    @Mock private Alias alias2;
    @Mock private Alias alias3;

    private Set<Alias> aliases;
    private AliasLoader loader;

    @Before
    public void setup() {
        loader = new AliasLoader(aliasManager, aliasStore);
        aliases = Sets.newHashSet(alias1, alias2, alias3);
    }

    @Test
    public void testLoadPreservesDirtyState() {
        when(aliasManager.isDirty()).thenReturn(true);
        when(aliasStore.readAliases()).thenReturn(aliases);
        loader.load();
        verify(aliasManager).setDirty(true);
    }

    @Test
    public void testLoadAddsEachAlias() {
        when(aliasStore.readAliases()).thenReturn(aliases);
        loader.load();
        verify(aliasManager).addAlias(alias1);
        verify(aliasManager).addAlias(alias2);
        verify(aliasManager).addAlias(alias3);
    }

    @Test
    public void testSaveDoesNotWriteIfManagerNotDirty() {
        when(aliasManager.isDirty()).thenReturn(false);
        loader.save();
        verify(aliasStore, never()).writeAliases(anySetOf(Alias.class));
    }

    @Test
    public void testSaveSetsDirtyToFalse() {
        when(aliasManager.isDirty()).thenReturn(true);
        when(aliasManager.getAliases()).thenReturn(aliases);
        loader.save();
        verify(aliasManager).setDirty(false);
    }

    @Test
    public void testSaveSavesAliases() {
        when(aliasManager.isDirty()).thenReturn(true);
        when(aliasManager.getAliases()).thenReturn(aliases);
        loader.save();
        verify(aliasStore).writeAliases(aliases);
    }

}