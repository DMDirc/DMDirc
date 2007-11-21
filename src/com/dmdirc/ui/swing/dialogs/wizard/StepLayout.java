/*
 * Copyright (c) 2006-2007 Chris Smith, Shane Mc Cormack, Gregory Holmes
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

package com.dmdirc.ui.swing.dialogs.wizard;

import java.awt.CardLayout;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Vector;

/**
 * Adjusted Card layout.
 */
public class StepLayout extends CardLayout {

    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 2;
    /** Cards vector. */
    Vector elements = null;

    /**
     * Instantiates a new step layout.
     */
    public StepLayout() {
        this(0, 0);
    }

    /**
     * Instantiates a new step layout with the specified gaps.
     * 
     * @param hgap Horizontal gap
     * @param vgap Vertical gap
     */
    public StepLayout(final int hgap, final int vgap) {
        super(hgap, vgap);
        elements = new Vector<Object>();
        try {
            Field f = CardLayout.class.getDeclaredField("vector");
            f.setAccessible(true);
            f.set(this, elements);
        } catch (IllegalArgumentException ex) {
            //Ignore, wont happen
        } catch (IllegalAccessException ex) {
            //Ignore, wont happen
        } catch (NoSuchFieldException ex) {
            //Ignore, wont happen
        } catch (SecurityException ex) {
            //Ignore, wont happen
        }
    }

    /**
     * Returns the number of steps in the layout.
     * 
     * @return number of steps >= 0
     */
    public int size() {
        return elements.size();
    }

    /**
     * Checks if the layout is empty
     * 
     * @return true iif the layout has no steps
     */
    public boolean isEmpty() {
        return elements.isEmpty();
    }

    /**
     * Returns the specified step from the layout.
     * 
     * @param index Step to retrieve
     * 
     * @return Step
     */
    public Step getStep(final int index) {
        try {
            Field f = elements.get(index).getClass().getDeclaredField("comp");
            f.setAccessible(true);

            return (Step) f.get(elements.get(index));
        } catch (IllegalArgumentException ex) {
            //Ignore, wont happen
        } catch (IllegalAccessException ex) {
            //Ignore, wont happen
        } catch (NoSuchFieldException ex) {
            //Ignore, wont happen
        } catch (SecurityException ex) {
            //Ignore, wont happen
        }
        return null;
    }

    /**
     * Returns the step list.
     * 
     * @return List of steps
     */
    public List getSteps() {
        return elements;
    }
}