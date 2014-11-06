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

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;

import javax.annotation.Nonnull;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

public class AutoCommandsModel {

    private final AutoCommandManager manager;
    private final AutoCommandType type;
    private final Set<AutoCommand> originalCommands;
    private final Set<MutableAutoCommand> commands;
    private Optional<MutableAutoCommand> selectedCommand;

    public AutoCommandsModel(final AutoCommandManager manager, final AutoCommandType type) {
        this.manager = manager;
        this.type = type;
        originalCommands = Sets.newHashSet();
        commands = Sets.newHashSet();
    }

    public void loadModel() {
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

    public Set<MutableAutoCommand> getAutoCommands() {
        return Collections.unmodifiableSet(commands);
    }

    public void setAutoCommands(final Collection<MutableAutoCommand> commands) {
        this.commands.clear();
        this.commands.addAll(commands);
    }

    public Optional<MutableAutoCommand> getSelectedCommand() {
        return selectedCommand;
    }

    public void setSelectedCommand(final Optional<MutableAutoCommand> selectedCommand) {
        selectedCommand.ifPresent(s -> checkArgument(commands.contains(s)));
        this.selectedCommand = selectedCommand;
    }

    public Optional<String> getSelectedCommandServer() {
        if (selectedCommand.isPresent()) {
            return selectedCommand.get().getServer();
        }
        return Optional.empty();
    }

    public Optional<String> getSelectedCommandNetwork() {
        if (selectedCommand.isPresent()) {
            return selectedCommand.get().getNetwork();
        }
        return Optional.empty();
    }

    public Optional<String> getSelectedCommandProfile() {
        if (selectedCommand.isPresent()) {
            return selectedCommand.get().getProfile();
        }
        return Optional.empty();
    }

    public String getSelectedCommandResponse() {
        if (selectedCommand.isPresent()) {
            return selectedCommand.get().getResponse();
        }
        return "";
    }

    public void setSelectedCommandServer(final Optional<String> server) {
        selectedCommand.ifPresent(s -> s.setServer(server));
    }

    public void setSelectedCommandNetwork(final Optional<String> network) {
        selectedCommand.ifPresent(s -> s.setNetwork(network));
    }

    public void setSelectedCommandProfile(final Optional<String> profile) {
        selectedCommand.ifPresent(s -> s.setProfile(profile));
    }

    public void setSelectedCommandResponse(@Nonnull final String response) {
        checkNotNull(response);
        selectedCommand.ifPresent(s -> s.setResponse(response));
    }

    public void addCommand(final MutableAutoCommand command) {
        commands.add(command);
    }

    public void removeCommand(final MutableAutoCommand command) {
        commands.remove(command);
    }

    public void save() {
        final Collection<AutoCommand> removedCommands = Lists.newArrayList(originalCommands);
        originalCommands.forEach(ac -> commands.forEach(mac -> {
            if (mac.equalsAutoCommand(ac)) {
                removedCommands.remove(ac);
            }
        }));
        originalCommands.forEach(manager::removeAutoCommand);
        commands.forEach(c -> {
            if (!originalCommands.contains(c.getAutoCommand())) {
                manager.addAutoCommand(c.getAutoCommand());
            }
        });
    }
}
