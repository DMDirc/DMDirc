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

package com.dmdirc.ui.core.aliases;

import com.dmdirc.commandparser.aliases.Alias;
import com.dmdirc.commandparser.aliases.AliasFactory;
import com.dmdirc.commandparser.aliases.AliasManager;
import com.dmdirc.commandparser.validators.CommandNameValidator;
import com.dmdirc.interfaces.CommandController;
import com.dmdirc.interfaces.ui.AliasDialogModel;
import com.dmdirc.interfaces.ui.AliasDialogModelListener;
import com.dmdirc.util.collections.ListenerList;
import com.dmdirc.util.validators.FileNameValidator;
import com.dmdirc.util.validators.IntegerValidator;
import com.dmdirc.util.validators.NotEmptyValidator;
import com.dmdirc.util.validators.Validator;
import com.dmdirc.util.validators.ValidatorChain;

import com.google.common.collect.Iterables;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentSkipListMap;

import javax.inject.Inject;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

public class CoreAliasDialogModel implements AliasDialogModel {

    private final CommandController commandController;
    private final AliasManager aliasManager;
    private final AliasFactory factory;
    private final ListenerList listeners;
    private final Map<String, Alias> aliases;
    private Optional<Alias> selectedAlias;
    private String name;
    private int minArgs;
    private String substitution;

    @Inject
    public CoreAliasDialogModel(final AliasManager aliasManager, final AliasFactory factory,
            final CommandController commandController) {
        this.commandController = commandController;
        this.aliasManager = aliasManager;
        this.factory = factory;
        listeners = new ListenerList();
        aliases = new ConcurrentSkipListMap<>();
        selectedAlias = Optional.empty();
    }

    @Override
    public void loadModel() {
        for (Alias alias : aliasManager.getAliases()) {
            aliases.put(alias.getName(), alias);
            listeners.getCallable(AliasDialogModelListener.class).aliasAdded(alias);
        }
        setSelectedAlias(Optional.ofNullable(Iterables.getFirst(aliasManager.getAliases(), null)));
    }

    @Override
    public Collection<Alias> getAliases() {
        return Collections.unmodifiableCollection(aliases.values());
    }

    @Override
    public Optional<Alias> getAlias(final String name) {
        checkNotNull(name, "Name cannot be null");
        return Optional.ofNullable(aliases.get(name));
    }

    @Override
    public void addAlias(final String name, final int minArguments, final String substitution) {
        checkNotNull(name, "Name cannot be null");
        checkArgument(!aliases.containsKey(name), "Name cannot already exist");
        final Alias alias = factory.createAlias(name, minArguments, substitution);
        aliases.put(name, alias);
        listeners.getCallable(AliasDialogModelListener.class).aliasAdded(alias);
    }

    @Override
    public void editAlias(final String name, final int minArguments, final String substitution) {
        editAlias(name, minArguments, substitution, false);
    }

    private void editAlias(final String name, final int minArguments, final String substitution,
            final boolean selection) {
        checkNotNull(name, "Name cannot be null");
        checkArgument(aliases.containsKey(name), "Name must already exist");
        final Alias newAlias = factory.createAlias(name, minArguments, substitution);
        final Alias oldAlias = aliases.put(name, newAlias);
        listeners.getCallable(AliasDialogModelListener.class).aliasEdited(oldAlias, newAlias);
        if (!selection) {
            setSelectedAlias(Optional.ofNullable(newAlias));
        }
    }

    @Override
    public void renameAlias(final String oldName, final String newName) {
        renameAlias(oldName, newName, false);
    }

    private void renameAlias(final String oldName, final String newName, final boolean selection) {
        checkNotNull(oldName, "Oldname cannot be null");
        checkNotNull(newName, "Newname cannot be null");
        checkArgument(aliases.containsKey(oldName), "Old name must exist");
        checkArgument(!aliases.containsKey(newName), "New name must not exist");
        final Alias alias = aliases.get(oldName);
        final Alias newAlias = factory.createAlias(newName, alias.getMinArguments(),
                alias.getSubstitution());
        final Alias oldAlias = aliases.remove(oldName);
        aliases.put(newName, newAlias);
        listeners.getCallable(AliasDialogModelListener.class).aliasRenamed(oldAlias, newAlias);
        if (!selection) {
            setSelectedAlias(Optional.ofNullable(aliases.get(newName)));
        }
    }

    @Override
    public void removeAlias(final String name) {
        checkNotNull(name, "Name cannot be null");
        if (!aliases.containsKey(name)) {
            return;
        }
        final Alias alias = aliases.get(name);
        aliases.remove(name);
        if (getSelectedAlias().isPresent() && getSelectedAlias().get().equals(alias)) {
            setSelectedAlias(Optional.empty());
        }
        listeners.getCallable(AliasDialogModelListener.class).aliasRemoved(alias);
    }

    @Override
    public void save() {
        setSelectedAlias(Optional.empty());
        aliasManager.getAliases().forEach(aliasManager::removeAlias);
        aliases.values().forEach(aliasManager::addAlias);
    }

    @Override
    public void setSelectedAlias(final Optional<Alias> alias) {
        if (alias.equals(selectedAlias)) {
            return;
        }
        if (selectedAlias.isPresent()) {
            if (selectedAlias.get().getMinArguments() != minArgs
                    || !selectedAlias.get().getSubstitution().equals(substitution)) {
                editAlias(selectedAlias.get().getName(), minArgs, substitution, true);
            }
            if (!selectedAlias.get().getName().equals(name)) {
                renameAlias(selectedAlias.get().getName(), name, true);
            }
        }
        selectedAlias = alias;

        name = alias.map(Alias::getName).orElse(null);
        minArgs = alias.map(Alias::getMinArguments).orElse(-1);
        substitution = alias.map(Alias::getSubstitution).orElse(null);

        listeners.getCallable(AliasDialogModelListener.class).aliasSelectionChanged(selectedAlias);
    }

    @Override
    public Optional<Alias> getSelectedAlias() {
        return selectedAlias;
    }

    @Override
    public String getSelectedAliasName() {
        return name;
    }

    @Override
    public int getSelectedAliasMinimumArguments() {
        return minArgs;
    }

    @Override
    public String getSelectedAliasSubstitution() {
        return substitution;
    }

    @Override
    public void setSelectedAliasName(final String aliasName) {
        this.name = aliasName;
        listeners.getCallable(AliasDialogModelListener.class).selectedAliasEdited(name, minArgs,
                substitution);
    }

    @Override
    public void setSelectedAliasMinimumArguments(final int minArgs) {
        this.minArgs = minArgs;
        listeners.getCallable(AliasDialogModelListener.class).selectedAliasEdited(name, minArgs,
                substitution);
    }

    @Override
    public void setSelectedAliasSubstitution(final String substitution) {
        this.substitution = substitution;
        listeners.getCallable(AliasDialogModelListener.class).selectedAliasEdited(name, minArgs,
                substitution);
    }

    @Override
    public boolean isCommandValid() {
        return selectedAlias.isPresent() && !getCommandValidator().validate(name).isFailure();
    }

    @Override
    public boolean isMinimumArgumentsValid() {
        return selectedAlias.isPresent() && !getMinimumArgumentsValidator().validate(minArgs).
                isFailure();
    }

    @Override
    public boolean isSubstitutionValid() {
        return selectedAlias.isPresent() && !getSubstitutionValidator().validate(substitution).
                isFailure();
    }

    @Override
    public boolean isSelectedAliasValid() {
        return !selectedAlias.isPresent() ||
                isCommandValid() && isMinimumArgumentsValid() && isSubstitutionValid();
    }

    @Override
    public boolean isChangeAliasAllowed() {
        return !selectedAlias.isPresent() ||
                isCommandValid() && isMinimumArgumentsValid() && isSubstitutionValid();
    }

    @Override
    public Validator<String> getCommandValidator() {
        return ValidatorChain.<String>builder()
                .addValidator(new CommandNameValidator(commandController.getCommandChar()))
                .addValidator(new FileNameValidator())
                .addValidator(new RenameAliasValidator(this))
                .build();
    }

    @Override
    public Validator<String> getNewCommandValidator() {
        return ValidatorChain.<String>builder()
                .addValidator(new CommandNameValidator(commandController.getCommandChar()))
                .addValidator(new FileNameValidator())
                .addValidator(new NewAliasValidator(this))
                .build();
    }

    @Override
    public Validator<Integer> getMinimumArgumentsValidator() {
        return new IntegerValidator(0, Integer.MAX_VALUE);
    }

    @Override
    public Validator<String> getSubstitutionValidator() {
        return new NotEmptyValidator();
    }

    @Override
    public void addListener(final AliasDialogModelListener listener) {
        checkNotNull(listener, "Listener must not be null");
        listeners.add(AliasDialogModelListener.class, listener);
    }

    @Override
    public void removeListener(final AliasDialogModelListener listener) {
        checkNotNull(listener, "Listener must not be null");
        listeners.remove(AliasDialogModelListener.class, listener);
    }

}
