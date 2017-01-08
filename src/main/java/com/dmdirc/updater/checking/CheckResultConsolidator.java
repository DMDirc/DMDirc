/*
 * Copyright (c) 2006-2017 DMDirc Developers
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the
 * Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS
 * OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.dmdirc.updater.checking;

import com.dmdirc.updater.UpdateComponent;

import java.util.Collection;
import java.util.Map;

/**
 * A {@link CheckResultConsolidator} defines a strategy for consolidating multiple sets of
 * {@link UpdateCheckResult}s provided by different {@link UpdateCheckStrategy}s.
 */
public interface CheckResultConsolidator {

    /**
     * Consolidates the collection of results into a single mapping. The resulting map should
     * contain one entry for each distinct component contained in any of the providing result sets,
     * mapped to the best {@link UpdateCheckResult} according to this strategy.
     *
     * @param results The results to be consolidated
     *
     * @return A map of {@link UpdateComponent}s to their consolidated result
     */
    Map<UpdateComponent, UpdateCheckResult> consolidate(
            Collection<Map<UpdateComponent, UpdateCheckResult>> results);

}
