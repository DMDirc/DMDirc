/*
 * Copyright (c) 2006-2017 DMDirc Developers
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

package com.dmdirc.commandparser.commands;

import com.dmdirc.interfaces.WindowModel;
import com.dmdirc.ui.input.AdditionalTabTargets;

import java.util.List;

/**
 * Intelligent commands implement a method that provides a list of possible options for them, for
 * use (for example) by table completers.
 */
public interface IntelligentCommand {

    /**
     * Returns a list of suggestions for the specified argument, given the specified context.
     *
     * @param arg     The argument that is being completed
     * @param context The context in which suggestions are being sought
     *
     * @return A list of suggestions for the argument
     *
     * @since 0.6.4
     */
    AdditionalTabTargets getSuggestions(int arg, IntelligentCommandContext context);

    /**
     * Describes the context of an intelligent tab completion request.
     *
     * @since 0.6.4
     */
    @SuppressWarnings("PMD.UnusedPrivateField")
    class IntelligentCommandContext {

        /** The window the command is being entered in. */
        private final WindowModel window;
        /** The previously supplied arguments, if any. */
        private final List<String> previousArgs;
        /** The partially typed word, if any. */
        private final String partial;

        public IntelligentCommandContext(final WindowModel window,
                final List<String> previousArgs, final String partial) {
            this.window = window;
            this.previousArgs = previousArgs;
            this.partial = partial;
        }

        public WindowModel getWindow() {
            return window;
        }

        public List<String> getPreviousArgs() {
            return previousArgs;
        }

        public String getPartial() {
            return partial;
        }

    }

}
