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

package com.dmdirc.actions.interfaces;

/**
 * The ActionComponent interface describes the methods that action components
 * are required to implement. Components are objects that allow the user to
 * pull a specific piece of information from a certain type of argument.
 * @author chris
 */
public interface ActionComponent {
    
    /**
     * Retrieves the component of the specified argument that this enum
     * represents.
     * @param argument The object to retrieve the component from
     * @return The relevant component of the object
     */    
    Object get(Object argument);
    
    /**
     * Retrieves the type of class that this component applies to.
     * @return The Class that this component can be applied to
     */
    Class appliesTo();
    
    /**
     * Retrieves the type of this component.
     * @return The Class of this component.
     */
    Class getType();
    
    /**
     * Retrives a friendly name for this component.
     * @return This component's name
     */
    String getName();
    
}
