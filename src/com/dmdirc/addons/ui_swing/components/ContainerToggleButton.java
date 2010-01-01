/*
 * Copyright (c) 2006-2010 Chris Smith, Shane Mc Cormack, Gregory Holmes
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

package com.dmdirc.addons.ui_swing.components;

import javax.swing.Icon;
import javax.swing.JToggleButton;

/**
 * JButton that holds an object.
 * 
 * @param <T> Holds an object of this type
 */
public class ContainerToggleButton<T> extends JToggleButton {

    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 1;
    /** Contents. */
    private T contents;

    /**
     * Instantiates a new container button.
     * 
     * @param contents Contents
     * @param string Title
     * @param icon Icon
     */
    public ContainerToggleButton(final T contents, final String string,
            final Icon icon) {
        super(string, icon);
    }

    /**
     * Instantiates a new container button.
     * 
     * @param contents Contents
     * @param string Title
     */
    public ContainerToggleButton(final T contents, final String string) {
        super(string);
    }

    /**
     * Instantiates a new container button.
     * 
     * @param contents Contents
     * @param icon Icon
     */
    public ContainerToggleButton(final T contents, final Icon icon) {
        super(icon);
    }

    /**
     * Instantiates a new container button.
     * 
     * @param contents Contents
     */
    public ContainerToggleButton(final T contents) {
        super();
    }

    /**
     * Instantiates a new container button.
     */
    public ContainerToggleButton() {
        super();
    }

    /**
     * Gets the button's contents
     * 
     * @return Button contents
     */
    public T getContents() {
        return contents;
    }

    /**
     * Sets the button's contents
     * 
     * @param contents New contents
     */
    public void setValue(final T contents) {
        this.contents = contents;
    }
}
