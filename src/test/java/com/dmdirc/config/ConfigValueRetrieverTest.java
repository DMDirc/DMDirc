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

package com.dmdirc.config;

import com.dmdirc.interfaces.config.ReadOnlyConfigProvider;

import com.google.common.collect.Lists;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ConfigValueRetrieverTest {

    private static final String DOMAIN = "mydomain";
    private static final String KEY = "mykey";

    private static final String FALLBACK_DOMAIN = "otherdomain";
    private static final String FALLBACK_KEY = "otherkey";

    @Mock private ReadOnlyConfigProvider configProvider;
    private ConfigValueRetriever retriever;

    @Before
    public void setUpRetriever() {
        retriever = new ConfigValueRetriever(configProvider);
    }

    @Test
    public void testRetrievesBasicString() {
        when(configProvider.getOptionString(DOMAIN, KEY, true,
                ReadOnlyConfigProvider.PERMISSIVE_VALIDATOR)).thenReturn("value!");

        assertEquals("value!", retriever.getValue(String.class, DOMAIN, KEY));
    }

    @Test
    public void testRetrievesStringWithFallback() {
        when(configProvider.getOptionString(DOMAIN, KEY, true,
                ReadOnlyConfigProvider.PERMISSIVE_VALIDATOR)).thenReturn(null);
        when(configProvider.getOptionString(
                DOMAIN, KEY, true, ReadOnlyConfigProvider.PERMISSIVE_VALIDATOR,
                FALLBACK_DOMAIN, FALLBACK_KEY))
                .thenReturn("value!");

        assertEquals("value!", retriever.getValue(String.class, DOMAIN, KEY, true,
                FALLBACK_DOMAIN, FALLBACK_KEY));
    }

    @Test
    public void testRetrievesStringWithRequiresFalse() {
        when(configProvider.getOptionString(DOMAIN, KEY, false,
                ReadOnlyConfigProvider.PERMISSIVE_VALIDATOR)).thenReturn(null);
        when(configProvider.getOptionString(DOMAIN, KEY, true,
                ReadOnlyConfigProvider.PERMISSIVE_VALIDATOR)).thenReturn("value!");

        assertNull(retriever.getValue(String.class, DOMAIN, KEY, false));
    }

    @Test
    public void testRetrievesStringWithRequiresTrue() {
        when(configProvider.getOptionString(DOMAIN, KEY, false,
                ReadOnlyConfigProvider.PERMISSIVE_VALIDATOR)).thenReturn(null);
        when(configProvider.getOptionString(DOMAIN, KEY, true,
                ReadOnlyConfigProvider.PERMISSIVE_VALIDATOR)).thenReturn("value!");

        assertEquals("value!", retriever.getValue(String.class, DOMAIN, KEY, true));
    }

    @Test
    public void testRetrievesBoolean() {
        when(configProvider.getOptionBool(DOMAIN, KEY)).thenReturn(true);

        assertEquals(true, retriever.getValue(Boolean.class, DOMAIN, KEY));
    }

    @Test
    public void testRetrievesBooleanByPrimitiveType() {
        when(configProvider.getOptionBool(DOMAIN, KEY)).thenReturn(true);

        assertEquals(true, retriever.getValue(Boolean.TYPE, DOMAIN, KEY));
    }

    @Test
    public void testRetrievesChar() {
        when(configProvider.getOptionChar(DOMAIN, KEY)).thenReturn('s');

        assertEquals('s', retriever.getValue(Character.class, DOMAIN, KEY));
    }

    @Test
    public void testRetrievesCharByPrimitiveType() {
        when(configProvider.getOptionChar(DOMAIN, KEY)).thenReturn('s');

        assertEquals('s', retriever.getValue(Character.TYPE, DOMAIN, KEY));
    }

    @Test
    public void testRetrievesInt() {
        when(configProvider.getOptionInt(DOMAIN, KEY)).thenReturn(1337);

        assertEquals(1337, retriever.getValue(Integer.class, DOMAIN, KEY));
    }

    @Test
    public void testRetrievesIntByPrimitiveType() {
        when(configProvider.getOptionInt(DOMAIN, KEY)).thenReturn(1337);

        assertEquals(1337, retriever.getValue(Integer.TYPE, DOMAIN, KEY));
    }

    @Test
    @SuppressWarnings("AssertEqualsBetweenInconvertibleTypes")
    public void testRetrievesList() {
        when(configProvider.getOptionList(DOMAIN, KEY)).thenReturn(
                Lists.newArrayList("test1", "test2"));

        assertEquals(Lists.newArrayList("test1", "test2"),
                retriever.getValue(List.class, DOMAIN, KEY));
    }

}
