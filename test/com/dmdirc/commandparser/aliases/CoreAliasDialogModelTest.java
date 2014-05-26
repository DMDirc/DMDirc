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

import com.dmdirc.commandparser.CommandType;
import com.dmdirc.interfaces.ui.AliasDialogModelListener;

import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CoreAliasDialogModelTest {

    @Mock private AliasManager aliasModel;
    @Mock private AliasDialogModelListener listener;
    private final Alias alias1 = new Alias(CommandType.TYPE_GLOBAL, "alias1", 0, "");
    private final Alias alias2Edited = new Alias(CommandType.TYPE_GLOBAL, "alias1", 1, "");
    private final Alias alias2 = new Alias(CommandType.TYPE_GLOBAL, "alias2", 0, "");
    private Set<Alias> aliases;

    @Before
    public void setupAliases() {
        aliases = new HashSet<>();
    }

    @Test
    public void testgetAliases() {
        aliases.add(alias1);
        aliases.add(alias2);
        when(aliasModel.getAliases()).thenReturn(aliases);
        final CoreAliasDialogModel model = new CoreAliasDialogModel(aliasModel);
        assertEquals(model.getAliases().size(), 2);
    }

    @Test
    public void testGetAliasExists() {
        aliases.add(alias1);
        aliases.add(alias2);
        when(aliasModel.getAliases()).thenReturn(aliases);
        final CoreAliasDialogModel model = new CoreAliasDialogModel(aliasModel);
        assertEquals(model.getAlias("alias1").get(), alias1);
    }

    @Test
    public void testGetAliasNotExists() {
        aliases.add(alias1);
        aliases.add(alias2);
        when(aliasModel.getAliases()).thenReturn(aliases);
        final CoreAliasDialogModel model = new CoreAliasDialogModel(aliasModel);
        assertFalse(model.getAlias("alias3").isPresent());
    }

    @Test
    public void testAddAlias() {
        when(aliasModel.getAliases()).thenReturn(aliases);
        final CoreAliasDialogModel model = new CoreAliasDialogModel(aliasModel);
        assertEquals(model.getAliases().size(), 0);
        model.addAlias(alias1.getName(), alias1.getMinArguments(), alias1.getSubstitution());
        assertEquals(model.getAliases().size(), 1);
    }

    @Test
    public void testAddAliasListener() {
        when(aliasModel.getAliases()).thenReturn(aliases);
        final CoreAliasDialogModel model = new CoreAliasDialogModel(aliasModel);
        model.addListener(listener);
        model.addAlias(alias1.getName(), alias1.getMinArguments(), alias1.getSubstitution());
        verify(listener).aliasAdded(alias1);
    }

    @Test
    public void testEditAlias() {
        aliases.add(alias1);
        when(aliasModel.getAliases()).thenReturn(aliases);
        final CoreAliasDialogModel model = new CoreAliasDialogModel(aliasModel);
        assertEquals(model.getAliases().size(), 1);
        assertEquals(model.getAlias("alias1").get().getMinArguments(), 0);
        model.editAlias("alias1", alias2Edited.getMinArguments(), alias2Edited.getSubstitution());
        assertEquals(model.getAlias("alias1").get().getMinArguments(), 1);
    }

    @Test
    public void testEditAliasListener() {
        aliases.add(alias1);
        when(aliasModel.getAliases()).thenReturn(aliases);
        final CoreAliasDialogModel model = new CoreAliasDialogModel(aliasModel);
        model.addListener(listener);
        model.editAlias("alias1", alias2Edited.getMinArguments(), alias2Edited.getSubstitution());
        verify(listener).aliasEdited("alias1");
    }

    @Test
    public void testRenameAlias() {
        aliases.add(alias1);
        when(aliasModel.getAliases()).thenReturn(aliases);
        final CoreAliasDialogModel model = new CoreAliasDialogModel(aliasModel);
        assertEquals(model.getAliases().size(), 1);
        assertEquals(model.getAlias("alias1").get(), alias1);
        model.renameAlias("alias1", "alias2");
        assertEquals(model.getAlias("alias2").get(), alias2);
    }

    @Test
    public void testRenameAliasListener() {
        aliases.add(alias1);
        when(aliasModel.getAliases()).thenReturn(aliases);
        final CoreAliasDialogModel model = new CoreAliasDialogModel(aliasModel);
        model.addListener(listener);
        assertEquals(model.getAliases().size(), 1);
        assertEquals(model.getAlias("alias1").get(), alias1);
        model.renameAlias("alias1", "alias2");
        verify(listener).aliasRenamed("alias1", "alias2");
    }

    @Test
    public void testRemoveAlias() {
        aliases.add(alias1);
        aliases.add(alias2);
        when(aliasModel.getAliases()).thenReturn(aliases);
        final CoreAliasDialogModel model = new CoreAliasDialogModel(aliasModel);
        assertEquals(model.getAliases().size(), 2);
        model.removeAlias("alias1");
        assertEquals(model.getAliases().size(), 1);
        assertFalse(model.getAlias("alias1").isPresent());
    }

    @Test
    public void testRemoveAliasListener() {
        aliases.add(alias1);
        aliases.add(alias2);
        when(aliasModel.getAliases()).thenReturn(aliases);
        final CoreAliasDialogModel model = new CoreAliasDialogModel(aliasModel);
        model.addListener(listener);
        model.removeAlias("alias1");
        verify(listener).aliasRemoved(alias1);
    }

    @Test
    public void testSelectedAliasInitiallyNull() {
        aliases.add(alias1);
        aliases.add(alias2);
        when(aliasModel.getAliases()).thenReturn(aliases);
        final CoreAliasDialogModel model = new CoreAliasDialogModel(aliasModel);
        assertEquals(model.getAliases().size(), 2);
        assertFalse(model.getSelectedAlias().isPresent());
    }

    @Test
    public void testSelectedAliasNotNull() {
        aliases.add(alias1);
        aliases.add(alias2);
        when(aliasModel.getAliases()).thenReturn(aliases);
        final CoreAliasDialogModel model = new CoreAliasDialogModel(aliasModel);
        assertEquals(model.getAliases().size(), 2);
        assertFalse(model.getSelectedAlias().isPresent());
        model.setSelectedAlias(alias2);
        assertEquals(model.getSelectedAlias().get(), alias2);
    }

    @Test
    public void testSelectedAliasToNull() {
        aliases.add(alias1);
        aliases.add(alias2);
        when(aliasModel.getAliases()).thenReturn(aliases);
        final CoreAliasDialogModel model = new CoreAliasDialogModel(aliasModel);
        assertEquals(model.getAliases().size(), 2);
        assertFalse(model.getSelectedAlias().isPresent());
        model.setSelectedAlias(alias2);
        assertEquals(model.getSelectedAlias().get(), alias2);
        model.setSelectedAlias(null);
        assertFalse(model.getSelectedAlias().isPresent());
    }

    @Test
    public void testRemoveListener() {
        when(aliasModel.getAliases()).thenReturn(aliases);
        final CoreAliasDialogModel model = new CoreAliasDialogModel(aliasModel);
        model.addListener(listener);
        model.addAlias(alias1.getName(), alias1.getMinArguments(), alias1.getSubstitution());
        model.removeListener(listener);
        model.addAlias(alias2.getName(), alias2.getMinArguments(), alias2.getSubstitution());
        verify(listener).aliasAdded(alias1);
        verify(listener, never()).aliasAdded(alias2);
    }

}
