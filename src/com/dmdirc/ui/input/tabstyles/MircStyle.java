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

import com.dmdirc.Channel;
import com.dmdirc.ui.input.AdditionalTabTargets;
import com.dmdirc.ui.input.TabCompleterResult;

import java.awt.Toolkit;
import java.util.List;

public class MircStyle extends TabCompletionStyle {
    
    /** The name of this style. */
    private static final String NAME = "mirc";
    
    /** The last set of results we retrieved. */
    private List<String> lastResult;
    
    /** The last word that was tab completed. */
    private String lastWord;
    
    /** {@inheritDoc} */
    public String getName() {
        return NAME;
    }
    
    /** {@inheritDoc} */
    public TabCompletionResult getResult(final String original, final int start,
            final int end, final AdditionalTabTargets additional) {
        
        final String word = original.substring(start, end);
        String target = "";
        
        if (word.equals(lastWord)) {
            // We're continuing to tab through
            target = lastResult.get((lastResult.indexOf(lastWord) + 1) % lastResult.size());
        } else {
            // New tab target
            final TabCompleterResult res = tabCompleter.complete(word, additional);
            
            if (res.getResultCount() == 0) {
                Toolkit.getDefaultToolkit().beep();
                return null;
            } else {
                if (word.length() > 0 && window.getContainer() instanceof Channel
                        && ((Channel) window.getContainer()).getChannelInfo().getName().startsWith(word)) {
                    target = ((Channel) window.getContainer()).getChannelInfo().getName();
                } else {
                    target = res.getResults().get(0);
                }
                lastResult = res.getResults();
            }
        }
        
        lastWord = target;
        
        return new TabCompletionResult(
                original.substring(0, start) + target + original.substring(end),
                start + target.length());
    }
    
}