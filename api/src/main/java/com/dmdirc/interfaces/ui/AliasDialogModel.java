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

package com.dmdirc.interfaces.ui;

import com.dmdirc.commandparser.aliases.Alias;
import com.dmdirc.util.validators.Validator;

import java.util.Collection;
import java.util.Optional;

/**
 * Model representing a list of aliases in a dialog.
 */
public interface AliasDialogModel {

    /**
     * Loads the model.
     */
    void loadModel();

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
     * @param alias Optional alias to set
     */
    void setSelectedAlias(final Optional<Alias> alias);

    /**
     * Returns the currently selected alias.
     *
     * @return Optional for the selected alias
     */
    Optional<Alias> getSelectedAlias();

    /**
     * Gets the name of the selected alias.
     *
     * @return Selected alias name
     */
    String getSelectedAliasName();

    /**
     * Gets the minimum arguments of the selected alias.
     *
     * @return Selected alias minimum arguments
     */
    int getSelectedAliasMinimumArguments();

    /**
     * Gets the substitution of the selected alias.
     *
     * @return Selected alias substitution
     */
    String getSelectedAliasSubstitution();

    /**
     * Sets the name of the selected alias.
     *
     * @param aliasName New name
     */
    void setSelectedAliasName(final String aliasName);

    /**
     * Sets the minimum arguments of the selected alias.
     *
     * @param minArgs New minimum arguments
     */
    void setSelectedAliasMinimumArguments(final int minArgs);

    /**
     * Sets the substitution of the selected alias.
     *
     * @param substitution New substitution
     */
    void setSelectedAliasSubstitution(final String substitution);

    /**
     * Tests whether the current command is valid.
     *
     * @return true if valid
     */
    boolean isCommandValid();

    /**
     * Tests whether the current minimum arguments is valid.
     *
     * @return true if valid
     */
    boolean isMinimumArgumentsValid();

    /**
     * Tests whether the current substitution is valid.
     *
     * @return true if valid
     */
    boolean isSubstitutionValid();

    /**
     * Tests whether the dialog can be saved.
     *
     * @return true if valid
     */
    boolean isSelectedAliasValid();

    /**
     * Tests whether the dialog is allowed to change the selection.
     *
     * @return true if allowed
     */
    boolean isChangeAliasAllowed();

    /**
     * Gets a validator for changing the command of an existing alias.
     *
     * @return Command name validator
     */
    Validator<String> getCommandValidator();

    /**
     * Gets a validator for the command of a new alias.
     *
     * @return Command name validator
     */
    Validator<String> getNewCommandValidator();

    /**
     * Gets a validator for the minimum arguments of an alias.
     *
     * @return Arguments validator
     */
    Validator<Integer> getMinimumArgumentsValidator();

    /**
     * Gets a validator for the substitution of an alias.
     *
     * @return Substitution validator
     */
    Validator<String> getSubstitutionValidator();

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
