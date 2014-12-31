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

package com.dmdirc.ui.input;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

/**
 * The AdditionalTabTargets class is a basic wrapper around an {@link ArrayList} that adds an
 * additional property to determine what types of results to include.
 */
public final class AdditionalTabTargets extends ArrayList<String> {

    /** A version number for this class. */
    private static final long serialVersionUID = 1;
    /** Whether to include normal targets. */
    private final Collection<TabCompletionType> includes =
            new ArrayList<>(Arrays.asList(TabCompletionType.values()));

    /**
     * Determines if the specified type of completion should be used.
     *
     * @param type The type to check for
     *
     * @return True if the specified targets are included, false otherwise
     */
    public boolean shouldInclude(final TabCompletionType type) {
        return includes.contains(type);
    }

    /**
     * Includes the specified target type.
     *
     * @param type The type to be included
     */
    public void include(final TabCompletionType type) {
        if (!includes.contains(type)) {
            includes.add(type);
        }
    }

    /**
     * Excludes the specified target type.
     *
     * @param type The type to be excluded
     */
    public void exclude(final TabCompletionType type) {
        includes.remove(type);
    }

    /**
     * Excludes all types of targets except ADDITIONAL.
     *
     * @return A reference to this object.
     */
    public AdditionalTabTargets excludeAll() {
        includes.clear();
        includes.add(TabCompletionType.ADDITIONAL);

        return this;
    }

}
