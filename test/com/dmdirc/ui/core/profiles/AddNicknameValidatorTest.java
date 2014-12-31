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

package com.dmdirc.ui.core.profiles;

import com.dmdirc.interfaces.ui.ProfilesDialogModel;
import com.dmdirc.util.validators.Validator;

import com.google.common.collect.Lists;

import java.util.List;
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
public class AddNicknameValidatorTest {

    @Mock private ProfilesDialogModel model;
    @Mock private MutableProfile profile;

    @Before
    public void setupModel() {
        final List<String> nicknames = Lists.newArrayList("nickname1", "nickname2", "nickname3");
        when(model.getSelectedProfile())
                .thenReturn(Optional.ofNullable(profile));
        when(model.getSelectedProfileNicknames())
                .thenReturn(Optional.ofNullable(nicknames));
        when(model.getSelectedProfileSelectedNickname())
                .thenReturn(Optional.ofNullable("nickname2"));
    }

    @Test
    public void testDuplicateName() {
        final Validator<String> instance = new AddNicknameValidator(model);
        assertTrue("testDuplicateName", instance.validate("nickname1").isFailure());
    }

    @Test
    public void testNonDuplicateName() {
        final Validator<String> instance = new AddNicknameValidator(model);
        assertFalse("testNonDuplicateName", instance.validate("nickname4").isFailure());
    }

    @Test
    public void testSelectedName() {
        final Validator<String> instance = new AddNicknameValidator(model);
        assertTrue("testSelectedName", instance.validate("nickname2").isFailure());
    }

}
