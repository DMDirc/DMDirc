/*
 * Copyright (c) 2006-2008 Chris Smith, Shane Mc Cormack, Gregory Holmes
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

package com.dmdirc.ui.input.tabstyles;

import com.dmdirc.ui.input.AdditionalTabTargets;
import com.dmdirc.ui.input.TabCompleterResult;

import java.awt.Toolkit;

public class BashStyle extends TabCompletionStyle {
    
    /** The name of this style. */
    private static final String NAME = "bash";
    
    /** The last position the user tab-completed at. */
    private int lastPosition = -1;
    
    /** The number of times the user has tab-completed the same position. */
    private int tabCount = 0;
    
    /** The last word that was tab completed. */
    private String lastWord = "";
    
    /** {@inheritDoc} */
    public String getName() {
        return NAME;
    }
    
    /** {@inheritDoc} */
    public TabCompletionResult getResult(final String original, final int start,
            final int end, final AdditionalTabTargets additional) {
        final String word = original.substring(start, end);
        final TabCompleterResult res = tabCompleter.complete(word, additional);
        
        if (start == lastPosition && word.equals(lastWord)) {
            tabCount++;
        } else {
            lastPosition = start;
            lastWord = word;
            tabCount = 1;
        }
        
        if (res.getResultCount() == 0) {
            Toolkit.getDefaultToolkit().beep();
            
            return null;
        } else if (res.getResultCount() == 1) {
            // One result, just replace it
            
            final String result = res.getResults().get(0);
            
            return new TabCompletionResult(
                    original.substring(0, start) + result + original.substring(end),
                    start + result.length());
        } else {
            // Multiple results
            
            final String sub = res.getBestSubstring();
            if (sub.equalsIgnoreCase(word) && tabCount >= 2) {
                window.addLine("tabCompletion", res.toString());
                
                return null;
            } else {
                return new TabCompletionResult(
                        original.substring(0, start) + sub + original.substring(end),
                        start + sub.length());
            }
        }
    }
    
}