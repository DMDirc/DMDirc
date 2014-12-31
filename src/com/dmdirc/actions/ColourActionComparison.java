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

import com.dmdirc.ClientModule;
import com.dmdirc.interfaces.actions.ActionComparison;
import com.dmdirc.ui.messages.ColourManager;
import com.dmdirc.util.colours.Colour;

import javax.inject.Inject;
import javax.inject.Provider;

/**
 * Core action comparisons dependent on a colour manager.
 */
public class ColourActionComparison {

    /** Instance of the equals comparison. */
    private final Equals equalsComparison;
    /** Instance of the not equals comparison. */
    private final NotEquals notEqualsComparison;
    /** Colour manager to use to parse colours. */
    private final Provider<ColourManager> colourManager;

    /**
     * Creates a new instance of {@link ColourActionComparison}.
     *
     * @param colourManager The colour manager to use.
     */
    @Inject
    public ColourActionComparison(@ClientModule.GlobalConfig final Provider<ColourManager> colourManager) {
        this.equalsComparison = new Equals();
        this.notEqualsComparison = new NotEquals();
        this.colourManager = colourManager;
    }

    /**
     * Gets the available colour comparisons.
     *
     * @return The available comparisons.
     */
    public ActionComparison[] getComparisons() {
        return new ActionComparison[]{equalsComparison, notEqualsComparison};
    }

    /** Checks if the colour is the same as another colour. */
    private class Equals implements ActionComparison {

        @Override
        public boolean test(final Object arg1, final Object arg2) {
            return arg1.equals(colourManager.get().getColourFromString((String) arg2, null));
        }

        @Override
        public Class<?> appliesTo() {
            return Colour.class;
        }

        @Override
        public String getName() {
            return "equals";
        }

        @Override
        public String name() {
            return "COLOUR_EQUALS";
        }

    }

    /** Checks if the colour is not the same as another colour. */
    private class NotEquals implements ActionComparison {

        @Override
        public boolean test(final Object arg1, final Object arg2) {
            return !equalsComparison.test(arg1, arg2);
        }

        @Override
        public Class<?> appliesTo() {
            return Colour.class;
        }

        @Override
        public String getName() {
            return "does not equal";
        }

        @Override
        public String name() {
            return "COLOUR_NEQUALS";
        }

    }

}
