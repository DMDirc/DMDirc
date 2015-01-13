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

import com.dmdirc.DMDircMBassador;
import com.dmdirc.interfaces.config.AggregateConfigProvider;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ConfigBinderTest {

    @Mock private AggregateConfigProvider configProvider;
    @Mock private DMDircMBassador eventBus;
    private ConfigBinder binder;

    @Before
    public void setup() {
        binder = new ConfigBinder(configProvider, eventBus);
    }

    @Test
    public void testAppliesStringSetting() {
        class StringTest {
            @ConfigBinding(domain = "test", key="foo")
            public String field;

            public String bar;

            @ConfigBinding(domain = "test", key="bar")
            public void method(final String boo) {
                this.bar = boo;
            }
        }

        final StringTest test = new StringTest();
        when(configProvider.getOptionString(eq("test"), eq("foo"), anyBoolean(), any()))
                .thenReturn("test123");
        when(configProvider.getOptionString(eq("test"), eq("bar"), anyBoolean(), any()))
                .thenReturn("test456");
        binder.bind(test, StringTest.class);

        assertEquals("test123", test.field);
        assertEquals("test456", test.bar);
    }

    @Test
    public void testAppliesStringSettingWithDefaultDomnain() {
        class StringTest {
            @ConfigBinding(key="foo")
            public String field;

            public String bar;

            @ConfigBinding(key="bar")
            public void method(final String boo) {
                this.bar = boo;
            }
        }

        final StringTest test = new StringTest();
        when(configProvider.getOptionString(eq("test"), eq("foo"), anyBoolean(), any()))
                .thenReturn("test123");
        when(configProvider.getOptionString(eq("test"), eq("bar"), anyBoolean(), any()))
                .thenReturn("test456");
        binder.withDefaultDomain("test").bind(test, StringTest.class);

        assertEquals("test123", test.field);
        assertEquals("test456", test.bar);
    }

}