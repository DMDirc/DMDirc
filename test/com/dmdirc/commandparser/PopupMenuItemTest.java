/*
 * Copyright (c) 2006-2011 Chris Smith, Shane Mc Cormack, Gregory Holmes
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

package com.dmdirc.commandparser;

import com.dmdirc.config.IdentityManager;

import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

public class PopupMenuItemTest {

    @BeforeClass
    public static void setUpClass() throws Exception {
        IdentityManager.load();
    }

    /**
     * Tests a command which doesn't define an arity gets args substituted
     * for a single target.
     */
    @Test
    public void testUnaryCommandWithSingle() {
        final PopupMenuItem item = new PopupMenuItem(null, 1, "foo %s");

        assertEquals("/foo bar", item.getCommand(new Object[][]{{"bar"}}));
    }

    /**
     * Tests a command which doesn't define an arity gets args substituted
     * for multiple targets.
     */
    @Test
    public void testUnaryCommandWithMultiple() {
        final PopupMenuItem item = new PopupMenuItem(null, 1, "foo %s");

        assertEquals("/foo bar\n/foo b\n/foo 2",
                item.getCommand(new Object[][]{
                    {"bar"}, {"b"}, {"2"},
                }));
    }

    /**
     * Tests a command which defines its arity as 1 gets args substituted
     * for a single target.
     */
    @Test
    public void testExplicitUnaryCommandWithSingle() {
        final PopupMenuItem item = new PopupMenuItem(null, 1, "1:foo %s");

        assertEquals("/foo bar", item.getCommand(new Object[][]{{"bar"}}));
    }

    /**
     * Tests a command which defines its arity as 1 gets args substituted
     * for multiple targets.
     */
    @Test
    public void testExplicitUnaryCommandWithMultiple() {
        final PopupMenuItem item = new PopupMenuItem(null, 1, "1:foo %s");

        assertEquals("/foo bar\n/foo a\n/foo 1",
                item.getCommand(new Object[][]{
                    {"bar"}, {"a"}, {"1"},
                }));
    }

    /**
     * Tests a command which takes multiple arguments.
     */
    @Test
    public void testMultipleCommand() {
        final PopupMenuItem item = new PopupMenuItem(null, 2, "4:foo %s %s %s %s");

        assertEquals("/foo 1 2 a b\n/foo bar baz",
                item.getCommand(new Object[][]{
                    {"1", "2"}, {"a", "b"}, {"bar", "baz"},
                }).trim());
    }

}