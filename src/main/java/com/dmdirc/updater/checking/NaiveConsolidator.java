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
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

/**
 * A simple {@link CheckResultConsolidator} that returns the first check result for each component
 * that has an update available.
 */
public class NaiveConsolidator implements CheckResultConsolidator {

    /**
     * Creates a new instance of {@link NaiveConsolidator}.
     */
    @Inject
    public NaiveConsolidator() {
    }

    @Override
    public Map<UpdateComponent, UpdateCheckResult> consolidate(
            final Collection<Map<UpdateComponent, UpdateCheckResult>> results) {
        final Map<UpdateComponent, UpdateCheckResult> res = new HashMap<>();

        for (Map<UpdateComponent, UpdateCheckResult> set : results) {
            set.entrySet().stream()
                    .filter(entry -> !res.containsKey(entry.getKey()) ||
                            !res.get(entry.getKey()).isUpdateAvailable())
                    .forEach(entry -> res.put(entry.getKey(), entry.getValue()));
        }

        return res;
    }

}
