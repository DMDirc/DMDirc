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

package com.dmdirc.ui.core.profiles;

import com.dmdirc.actions.wrappers.Profile;
import com.dmdirc.interfaces.ui.ProfilesDialogModel;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class NewProfileNameValidatorTest {

    @Mock private ProfilesDialogModel model;
    @Mock private Profile profile1;
    @Mock private Profile profile2;
    @Mock private Profile profile3;
    private List<Profile> profiles;

    @Before
    public void setupModel() {
        profiles = Lists.newArrayList(profile1, profile2, profile3);
        when(profile1.getName()).thenReturn("profile1");
        when(profile2.getName()).thenReturn("profile2");
        when(profile3.getName()).thenReturn("profile3");
        when(model.getProfileList()).thenReturn(profiles);
        when(model.getSelectedProfile()).thenReturn(Optional.fromNullable(profile2));
    }

    @Test
    public void testDuplicateName() {
        final NewProfileNameValidator instance
                = new NewProfileNameValidator(model);
        assertTrue("testDuplicateName", instance.validate("profile1").isFailure());
    }

    @Test
    public void testNonDuplicateName() {
        final NewProfileNameValidator instance
                = new NewProfileNameValidator(model);
        assertFalse("testNonDuplicateName", instance.validate("profile4").isFailure());
    }

    @Test
    public void testSelectedName() {
        final NewProfileNameValidator instance
                = new NewProfileNameValidator(model);
        assertTrue("testSelectedName", instance.validate("profile2").isFailure());
    }

}
