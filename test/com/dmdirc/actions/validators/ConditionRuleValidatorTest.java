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

package com.dmdirc.actions.validators;

import com.dmdirc.util.validators.ValidationResponse;

import org.junit.Test;

import static org.junit.Assert.*;

public class ConditionRuleValidatorTest {

    @Test
    public void testIllegal() {
        final ValidationResponse res = new ConditionRuleValidator(10).validate("|||");
        assertTrue(res.isFailure());
    }

    @Test
    public void testTooMany() {
        final ValidationResponse res = new ConditionRuleValidator(1).validate("0|1|2");
        assertTrue(res.isFailure());
    }

    @Test
    public void testGood() {
        final ValidationResponse res = new ConditionRuleValidator(3).validate("0|1|2");
        assertFalse(res.isFailure());
    }

    @Test
    public void testChangeArgs() {
        final ConditionRuleValidator validator = new ConditionRuleValidator(1);
        assertTrue(validator.validate("1|0").isFailure());
        validator.setArgs(2);
        assertFalse(validator.validate("1|0").isFailure());
    }

}
