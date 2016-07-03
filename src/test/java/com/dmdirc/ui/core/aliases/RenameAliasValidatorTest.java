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

package com.dmdirc.ui.core.aliases;

import com.dmdirc.commandparser.aliases.Alias;
import com.dmdirc.interfaces.ui.AliasDialogModel;

import com.google.common.collect.Lists;

import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class RenameAliasValidatorTest {

    @Mock private AliasDialogModel model;
    @Mock private Alias alias1;
    @Mock private Alias alias2;

    @Before
    public void setupModel() {
        when(alias1.getName()).thenReturn("alias1");
        when(alias2.getName()).thenReturn("alias2");
        when(model.getAliases()).thenReturn(Lists.newArrayList(alias1, alias2));
    }

    @Test
    public void testRenameSelected() {
        when(model.getSelectedAlias()).thenReturn(Optional.ofNullable(alias1));
        when(model.getSelectedAliasName()).thenReturn("alias1");
        final RenameAliasValidator instance = new RenameAliasValidator(model);
        assertFalse(instance.validate("alias1").isFailure());
        assertTrue(instance.validate("alias2").isFailure());
        assertFalse(instance.validate("test").isFailure());

    }

    @Test
    public void testRenameNonSelected() {
        when(model.getSelectedAlias()).thenReturn(Optional.<Alias>empty());
        when(model.getSelectedAliasName()).thenReturn(null);
        final RenameAliasValidator instance = new RenameAliasValidator(model);
        assertTrue(instance.validate("alias1").isFailure());
        assertTrue(instance.validate("alias2").isFailure());
        assertFalse(instance.validate("test").isFailure());
    }

}
