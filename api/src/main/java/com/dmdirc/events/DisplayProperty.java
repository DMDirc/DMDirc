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

package com.dmdirc.events;

import com.dmdirc.util.colours.Colour;

/**
 * Describes a property that may be set on a {@link DisplayableEvent} to affect its display.
 */
@SuppressWarnings("UnusedDeclaration") // Generic type used for compile-time validation only
public interface DisplayProperty<T> {

    /** The foreground colour of text relating to the event. */
    DisplayProperty<Colour> FOREGROUND_COLOUR = new DisplayPropertyImpl<>();
    /** The background colour of text relating to the event. */
    DisplayProperty<Colour> BACKGROUND_COLOUR = new DisplayPropertyImpl<>();
    /** Whether to suppress display of the event. */
    DisplayProperty<Boolean> DO_NOT_DISPLAY = new DisplayPropertyImpl<>();
    /** Whether to suppress timestamps for the event. */
    DisplayProperty<Boolean> NO_TIMESTAMPS = new DisplayPropertyImpl<>();

    final class DisplayPropertyImpl<T> implements DisplayProperty<T> {}

}
