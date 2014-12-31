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

package com.dmdirc.actions;

import org.junit.Test;

import static org.junit.Assert.*;

public class ConditionTreeTest {

    @Test
    public void testGetNumArgs() {
        final String target = "((0&1&2)|3)&(!4)";
        final ConditionTree tree = ConditionTree.parseString(target);
        assertNotNull(tree);

        assertEquals(4, tree.getMaximumArgument());

        final String target2 = "";
        assertEquals(0, ConditionTree.parseString(target2).getMaximumArgument());
    }

    @Test
    public void testCreateConjunction() {
        final String expected = "(((0&1)&2)&3)";
        final ConditionTree tree = ConditionTree.createConjunction(4);

        assertNotNull(tree);
        assertEquals(expected, tree.toString());
    }

    @Test
    public void testCreateDisjunction() {
        final String expected = "(((0|1)|2)|3)";
        final ConditionTree tree = ConditionTree.createDisjunction(4);

        assertNotNull(tree);
        assertEquals(expected, tree.toString());
    }

    @Test
    public void testMismatchedBrackets() {
        final ConditionTree tree = ConditionTree.parseString("(0");

        assertNull(tree);
    }

    @Test
    public void testMismatchedBrackets2() {
        final ConditionTree tree = ConditionTree.parseString("0)");

        assertNull(tree);
    }

    @Test
    public void testMissingUnaryArg() {
        final ConditionTree tree = ConditionTree.parseString("!");

        assertNull(tree);
    }

    @Test
    public void testGarbageUnaryArg() {
        final ConditionTree tree = ConditionTree.parseString("!xy");

        assertNull(tree);
    }

    @Test
    public void testMissingBinaryArg() {
        final ConditionTree tree = ConditionTree.parseString("0|");

        assertNull(tree);
    }

    @Test
    public void testMissingBinaryArg2() {
        final ConditionTree tree = ConditionTree.parseString("0|!");

        assertNull(tree);
    }

    @Test
    public void testNonExistantOp() {
        final ConditionTree tree = ConditionTree.parseString("0/1");

        assertNull(tree);
    }

    @Test
    public void testHugeNumber() {
        final ConditionTree tree = ConditionTree.parseString("9999999999999999");

        assertNull(tree);
    }

    @Test
    public void testNoopEvaluation() {
        final ConditionTree tree = ConditionTree.parseString("");

        assertTrue(tree.evaluate(new boolean[]{true, false, true}));
    }

    @Test
    public void testBracketedUnary() {
        final ConditionTree tree = ConditionTree.parseString("(+)");

        assertNull(tree);
    }

    @Test
    public void testEquals() {
        final ConditionTree tree1 = ConditionTree.parseString("(1&3)");
        final ConditionTree tree2 = ConditionTree.parseString("((1&(3)))");

        assertTrue(tree1.equals(tree2));
        assertTrue(tree2.equals(tree1));
        assertEquals(tree1.hashCode(), tree2.hashCode());
    }

    @Test
    public void testNotEquals() {
        final ConditionTree tree1 = ConditionTree.parseString("(1&3)");
        final ConditionTree tree2 = ConditionTree.parseString("((1&(2)))");

        assertFalse(tree1.equals(tree2));
        assertFalse(tree2.equals(tree1));
    }

}
