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

package com.dmdirc.ui.core.autocommands;

import com.dmdirc.commandparser.auto.AutoCommand;
import com.dmdirc.commandparser.auto.AutoCommandManager;
import com.dmdirc.interfaces.ui.AutoCommandsModel;
import com.dmdirc.interfaces.ui.AutoCommandsModelListener;
import com.dmdirc.util.collections.ListenerList;

import com.google.common.collect.Sets;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

public class CoreAutoCommandsModel implements AutoCommandsModel {

    private final AutoCommandManager manager;
    private final Set<AutoCommand> originalCommands;
    private final Set<MutableAutoCommand> commands;
    private AutoCommandType type;
    private boolean loaded;
    private Optional<MutableAutoCommand> selectedCommand;
    private ListenerList listeners;

    @Inject
    public CoreAutoCommandsModel(final AutoCommandManager manager) {
        this.manager = manager;
        originalCommands = Sets.newHashSet();
        commands = Sets.newHashSet();
    }

    @Override
    public void addListener(@Nonnull final AutoCommandsModelListener listener) {
        checkNotNull(listener);
        listeners.add(AutoCommandsModelListener.class, listener);
    }

    @Override
    public void removeListener(@Nonnull final AutoCommandsModelListener listener) {
        checkNotNull(listener);
        listeners.remove(AutoCommandsModelListener.class, listener);
    }

    @Override
    public void setType(@Nonnull final AutoCommandType type) {
        checkNotNull(type);
        checkState(this.type == null);
        this.type = type;
    }

    @Override
    public void loadModel() {
        checkNotNull(type);
        checkState(!loaded);
        loaded = true;
        listeners = new ListenerList();
        originalCommands.clear();
        commands.clear();
        selectedCommand = Optional.empty();
        switch(type) {
            case ALL:
                manager.getAutoCommands().stream().map(c -> {
                    originalCommands.add(c);
                    return new MutableAutoCommand(c);
                }).forEach(commands::add);
                break;
            case CONNECTION:
                manager.getConnectionAutoCommands().stream().map(c -> {
                    originalCommands.add(c);
                    return new MutableAutoCommand(c);
                }).forEach(commands::add);
                break;
            case GLOBAL:
                manager.getGlobalAutoCommands().stream().map(c -> {
                    originalCommands.add(c);
                    return new MutableAutoCommand(c);
                }).forEach(commands::add);
                break;
        }
    }

    @Override
    @Nonnull
    public Collection<MutableAutoCommand> getAutoCommands() {
        return Collections.unmodifiableCollection(commands);
    }

    @Override
    public void setAutoCommands(@Nonnull final Collection<MutableAutoCommand> commands) {
        checkNotNull(commands);
        this.commands.clear();
        this.commands.addAll(commands);
    }

    @Override
    @Nonnull
    public Optional<MutableAutoCommand> getSelectedCommand() {
        return selectedCommand;
    }

    @Override
    public void setSelectedCommand(@Nonnull final Optional<MutableAutoCommand> selectedCommand) {
        checkNotNull(selectedCommand);
        selectedCommand.ifPresent(s -> checkArgument(commands.contains(s)));
        if (!this.selectedCommand.equals(selectedCommand)) {
            this.selectedCommand = selectedCommand;
            listeners.getCallable(AutoCommandsModelListener.class)
                    .selectedCommandChanged(selectedCommand);
        }
    }

    @Override
    @Nonnull
    public Optional<String> getSelectedCommandServer() {
        if (selectedCommand.isPresent()) {
            return selectedCommand.get().getServer();
        }
        return Optional.empty();
    }

    @Override
    @Nonnull
    public Optional<String> getSelectedCommandNetwork() {
        if (selectedCommand.isPresent()) {
            return selectedCommand.get().getNetwork();
        }
        return Optional.empty();
    }

    @Override
    @Nonnull
    public Optional<String> getSelectedCommandProfile() {
        if (selectedCommand.isPresent()) {
            return selectedCommand.get().getProfile();
        }
        return Optional.empty();
    }

    @Override
    @Nonnull
    public String getSelectedCommandResponse() {
        if (selectedCommand.isPresent()) {
            return selectedCommand.get().getResponse();
        }
        return "";
    }

    @Override
    public void setSelectedCommandServer(@Nonnull final Optional<String> server) {
        checkNotNull(server);
        selectedCommand.ifPresent(s -> s.setServer(server));
    }

    @Override
    public void setSelectedCommandNetwork(@Nonnull final Optional<String> network) {
        checkNotNull(network);
        selectedCommand.ifPresent(s -> s.setNetwork(network));
    }

    @Override
    public void setSelectedCommandProfile(@Nonnull final Optional<String> profile) {
        checkNotNull(profile);
        selectedCommand.ifPresent(s -> s.setProfile(profile));
    }

    @Override
    public void setSelectedCommandResponse(@Nonnull final String response) {
        checkNotNull(response);
        checkArgument(!response.isEmpty());
        selectedCommand.ifPresent(s -> s.setResponse(response));
    }

    @Override
    public void addCommand(@Nonnull final MutableAutoCommand command) {
        checkNotNull(command);
        commands.add(command);
    }

    @Override
    public void removeCommand(@Nonnull final MutableAutoCommand command) {
        checkNotNull(command);
        commands.remove(command);
    }

    @Override
    public void save() {
        originalCommands.forEach(manager::removeAutoCommand);
        commands.stream().map(MutableAutoCommand::getAutoCommand).forEach(manager::addAutoCommand);
    }
}
