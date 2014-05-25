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

package com.dmdirc.commandparser.aliases;

import com.dmdirc.interfaces.ui.AliasDialogModel;
import com.dmdirc.interfaces.ui.AliasDialogModelListener;
import com.dmdirc.util.collections.ListenerList;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;

import javax.inject.Inject;

/**
 * Model representing a list of aliases in a dialog.
 */
public class CoreAliasDialogModel implements AliasDialogModel {

    private final AliasManager aliasManager;
    private final ListenerList listeners;
    private final Map<String, Alias> aliases;
    private Optional<Alias> selectedAlias;

    @Inject
    public CoreAliasDialogModel(final AliasManager aliasManager) {
        this.aliasManager = aliasManager;
        listeners = new ListenerList();
        aliases = new ConcurrentSkipListMap<>();
        for (Alias alias : aliasManager.getAliases()) {
            aliases.put(alias.getName(), alias);
        }
    }

    @Override
    public Collection<Alias> getAliases() {
        return Collections.unmodifiableCollection(aliases.values());
    }

    @Override
    public Optional<Alias> getAlias(final String name) {
        Preconditions.checkNotNull(name);
        return Optional.fromNullable(aliases.get(name));
    }

    @Override
    public void addAlias(final Alias alias) {
        Preconditions.checkNotNull(alias);
        aliases.put(alias.getName(), alias);
        listeners.getCallable(AliasDialogModelListener.class).aliasAdded(alias);
    }

    @Override
    public void editAlias(final String name, final Alias alias) {
        Preconditions.checkNotNull(name);
        Preconditions.checkNotNull(alias);
        aliases.put(name, alias);
        listeners.getCallable(AliasDialogModelListener.class).aliasEdited(name);
    }

    @Override
    public void removeAlias(final String name) {
        Preconditions.checkNotNull(name);
        if (!aliases.containsKey(name)) {
            return;
        }
        final Alias alias = aliases.get(name);
        aliases.remove(name);
        listeners.getCallable(AliasDialogModelListener.class).aliasRemoved(alias);
    }

    @Override
    public void save() {
        for (Alias alias : aliasManager.getAliases()) {
            if (aliases.containsKey(alias.getName())) {
                aliasManager.removeAlias(alias);
            }
        }
        for (Alias alias : aliases.values()) {
            aliasManager.addAlias(alias);
        }
    }

    @Override
    public void setSelectedAlias(final Alias alias) {
        selectedAlias = Optional.fromNullable(alias);
        listeners.getCallable(AliasDialogModelListener.class).aliasSelectionChanged(selectedAlias);
    }

    @Override
    public Optional<Alias> getSelectedAlias() {
        return selectedAlias;
    }

    @Override
    public void addListener(final AliasDialogModelListener listener) {
        Preconditions.checkNotNull(listener);
        listeners.add(AliasDialogModelListener.class, listener);
    }

    @Override
    public void removeListener(final AliasDialogModelListener listener) {
        Preconditions.checkNotNull(listener);
        listeners.remove(AliasDialogModelListener.class, listener);
    }

}
