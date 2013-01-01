/*
 * Copyright (c) 2006-2013 DMDirc Developers
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

import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import static org.junit.Assert.*;

@RunWith(Parameterized.class)
public class ConditionTreeEvaluatorTest {

    private final Boolean[] data;

    public ConditionTreeEvaluatorTest(Boolean[] data) {
        this.data = data;
    }

    @Test
    public void testEvaluate() {
        final String target = "((0&1&2)|3)&(!4)";
        final ConditionTree tree = ConditionTree.parseString(target);
        assertNotNull(tree);

        final boolean expected =
                    ((data[0] && data[1] && data[2]) || data[3])
                    && !data[4];
        final boolean actual = tree.evaluate(new boolean[]{data[0], data[1],
                data[2], data[3], data[4]});

        assertEquals(expected, actual);
    }

    @Parameterized.Parameters
    public static List<Boolean[][]> getData() {
        return Arrays.asList(new Boolean[][][]{
            {{true, true, true, true, true}},
            {{true, true, true, true, false}},
            {{true, true, true, false, true}},
            {{true, true, true, false, false}},
            {{true, true, false, true, true}},
            {{true, true, false, true, false}},
            {{true, true, false, false, true}},
            {{true, true, false, false, false}},
            {{true, false, true, true, true}},
            {{true, false, true, true, false}},
            {{true, false, true, false, true}},
            {{true, false, true, false, false}},
            {{true, false, false, true, true}},
            {{true, false, false, true, false}},
            {{true, false, false, false, true}},
            {{true, false, false, false, false}},
            {{false, true, true, true, true}},
            {{false, true, true, true, false}},
            {{false, true, true, false, true}},
            {{false, true, true, false, false}},
            {{false, true, false, true, true}},
            {{false, true, false, true, false}},
            {{false, true, false, false, true}},
            {{false, true, false, false, false}},
            {{false, false, true, true, true}},
            {{false, false, true, true, false}},
            {{false, false, true, false, true}},
            {{false, false, true, false, false}},
            {{false, false, false, true, true}},
            {{false, false, false, true, false}},
            {{false, false, false, false, true}},
            {{false, false, false, false, false}},
        });
    }
}
