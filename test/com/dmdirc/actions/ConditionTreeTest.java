/*
 * Copyright (c) 2006-2009 Chris Smith, Shane Mc Cormack, Gregory Holmes
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
    public void testParseString() {
        String[][] testCases = {
            {"1", "1"},
            {"50", "50"},
            {"!50", "!50"},
            {"1&1", "(1&1)"},
            {"1&!1", "(1&!1)"},
            {"!1&1", "(!1&1)"},
            {"(1)", "1"},
            {"((((1))))", "1"},
            {"(1&1)&1", "((1&1)&1)"},
            {"1&2&3", "((1&2)&3)"},
            {"(1&2&3)|4", "(((1&2)&3)|4)"},
            {"!(1|!1)", "!(1|!1)"},
            {"!!1", "!!1"},
            {"1 & !(2 | (3 & !4))", "(1&!(2|(3&!4)))"},
            {"", ""},
            {"((1|((2&!3)&(!4))|6)&7)|!8", "((((1|((2&!3)&!4))|6)&7)|!8)"},
        };
        
        for (String[] testCase : testCases) {
            System.out.println("\nInput: " + testCase[0]);
            
            final ConditionTree res = ConditionTree.parseString(testCase[0]);
            assertNotNull(res);
            
            System.out.println("  Expected: " + testCase[1]);
            System.out.println("  Result: " + res);
            
            assertEquals(testCase[1], res.toString());
        }
    }
    
    @Test
    public void testEvaluate() {
        System.out.println();
        
        final String target = "((0&1&2)|3)&(!4)";
        final ConditionTree tree = ConditionTree.parseString(target);
        assertNotNull(tree);
        
        boolean[][] testCases = {
            {true, true, true, true, true},
            {true, true, true, true, false},
            {true, true, true, false, true},
            {true, true, true, false, false},
            {true, true, false, true, true},
            {true, true, false, true, false},
            {true, true, false, false, true},
            {true, true, false, false, false},   
            {true, false, true, true, true},
            {true, false, true, true, false},
            {true, false, true, false, true},
            {true, false, true, false, false},            
            {true, false, false, true, true},
            {true, false, false, true, false},
            {true, false, false, false, true},
            {true, false, false, false, false}, 
            {false, true, true, true, true},
            {false, true, true, true, false},
            {false, true, true, false, true},
            {false, true, true, false, false},
            {false, true, false, true, true},
            {false, true, false, true, false},
            {false, true, false, false, true},
            {false, true, false, false, false},   
            {false, false, true, true, true},
            {false, false, true, true, false},
            {false, false, true, false, true},
            {false, false, true, false, false},            
            {false, false, false, true, true},
            {false, false, false, true, false},
            {false, false, false, false, true},
            {false, false, false, false, false},            
        };
        
        //final String target = "((0&1&2)|3)&(!4)";
        for (boolean[] testCase : testCases) {
            final boolean expected = 
                    ((testCase[0] && testCase[1] && testCase[2]) || testCase[3])
                    && !testCase[4];
            final boolean actual = tree.evaluate(testCase);
            
            assertEquals(expected, actual);
        }
    }
    
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
    public void testNoopEvaluation() {
        final ConditionTree tree = ConditionTree.parseString("");
        
        assertTrue(tree.evaluate(new boolean[]{true, false, true}));
    }   
    
    @Test
    public void testBracketedUnary() {
        final ConditionTree tree = ConditionTree.parseString("(+)");
        
        assertNull(tree);
    }    
    
}
