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

package com.dmdirc.ui.messages;

import com.dmdirc.util.colours.Colour;

import com.google.auto.value.AutoValue;

import java.util.Optional;

/**
 * Describes a format for an event.
 */
@AutoValue
public abstract class EventFormat {

    /** The template to use when rendering the event. */
    public abstract String getTemplate();
    /** The template to use before starting to render the event. */
    public abstract Optional<String> getBeforeTemplate();
    /** The template to use after finishing rendering the event. */
    public abstract Optional<String> getAfterTemplate();

    // TODO: This should probably be a generic set of properties.
    public abstract Optional<Colour> getDefaultForegroundColour();

    public static EventFormat create(
            final String template,
            final Optional<String> beforeTemplate,
            final Optional<String> afterTemplate,
            final Optional<Colour> defaultForegroundColour) {
        return new AutoValue_EventFormat(template, beforeTemplate, afterTemplate,
                defaultForegroundColour);
    }

}
