/*
 * Copyright (c) 2006-2014 DMDirc Developers
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

import com.google.common.base.Optional;

import java.util.Collection;

/**
 * Model representing a list of aliases in a dialog.
 */
public interface AliasDialogModel {

    /**
     * Gets the aliases in this model.
     *
     * @return Aliases in this model
     */
    Collection<Alias> getAliases();

    /**
     * Returns an alias with the given name from the model.
     *
     * @param name Name of the alias to find
     *
     * @return An option Alias from the model
     */
    Optional<Alias> getAlias(final String name);

    /**
     * Adds an alias to the model.
     *
     * @param name         Name for the new alias
     * @param minArguments Minimum number of arguments for the new alias
     * @param substitution Substitution for the new alias
     */
    void addAlias(String name, int minArguments, String substitution);

    /**
     * Edits the specified alias in the model.
     *
     * @param name         Name of the alias to edit
     * @param minArguments Minimum number of arguments for the new alias
     * @param substitution Substitution for the new alias
     */
    void editAlias(final String name, int minArguments, String substitution);

    /**
     * Renames an existing alias in the model.
     *
     * @param oldName Old name for the alias
     * @param newName New name for the alias
     */
    void renameAlias(final String oldName, final String newName);

    /**
     * Removes the named alias from the model.
     *
     * @param name Name of the alias to remove
     */
    void removeAlias(final String name);

    /**
     * Saves the contents of this model to the alias manager.
     */
    void save();

    /**
     * Sets the selected alias.
     *
     * @param alias Alias to set
     */
    void setSelectedAlias(final Alias alias);

    /**
     * Returns the currently selected alias.
     *
     * @return Optional for the selected alias
     */
    Optional<Alias> getSelectedAlias();

    /**
     * Adds an alias listener, will be notified of changes to the model.
     *
     * @param listener Listener to add
     */
    void addListener(final AliasDialogModelListener listener);

    /**
     * Removes a listener from the model.
     *
     * @param listener Listener to add
     */
    void removeListener(final AliasDialogModelListener listener);

}
