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

package com.dmdirc.interfaces.actions;

/**
 * Encapsulates the methods that all action types are required to implement.
 */
public interface ActionMetaType {

    /**
     * Retrieves the arity of this type.
     *
     * @return The arity of this action type
     */
    int getArity();

    /**
     * Retrieves the type of arguments that actions of this type should expect.
     *
     * @return The type of arguments expected
     */
    Class<?>[] getArgTypes();

    /**
     * Retrieves the names of the arguments that actions of this type should expect.
     *
     * @return The names of the expected arguments
     */
    String[] getArgNames();

    /**
     * Retrieves the name of a logical group for this meta-type to be put into.
     *
     * @return The meta-type's group name
     */
    String getGroup();

}
