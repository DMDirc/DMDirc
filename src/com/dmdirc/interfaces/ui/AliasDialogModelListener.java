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

package com.dmdirc.interfaces.ui;

import com.dmdirc.commandparser.aliases.Alias;

import java.util.Optional;

/**
 * Listener for various events in an alias dialog model.
 */
public interface AliasDialogModelListener {

    /**
     * An alias has been added.
     *
     * @param alias Alias that has been added
     */
    void aliasAdded(Alias alias);

    /**
     * An alias has been removed.
     *
     * @param alias Alias that was removed.
     */
    void aliasRemoved(Alias alias);

    /**
     * An alias has been edited.
     *
     * @param oldAlias Old alias
     * @param newAlias New alias
     */
    void aliasEdited(Alias oldAlias, Alias newAlias);

    /**
     * An alias has been renamed.
     *
     * @param oldAlias Old alias
     * @param newAlias New alias
     */
    void aliasRenamed(Alias oldAlias, Alias newAlias);

    /**
     * An alias selection has been changed.
     *
     * @param alias Optional alias that has been selected
     */
    void aliasSelectionChanged(Optional<Alias> alias);

    /**
     * The selected alias has been edited.
     *
     * @param name         New name
     * @param minArguments New minimum arguments
     * @param substitution New substitution
     */
    void selectedAliasEdited(final String name, final int minArguments, final String substitution);

}
