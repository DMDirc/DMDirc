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

package com.dmdirc.ui.core.autocommands;

import com.dmdirc.commandparser.auto.AutoCommand;
import com.dmdirc.commandparser.auto.AutoCommandManager;
import com.dmdirc.commandparser.auto.AutoCommandType;
import com.dmdirc.interfaces.ui.GlobalAutoCommandsDialogModel;
import com.dmdirc.util.validators.Validator;

import java.util.Optional;

import javax.inject.Inject;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

/**
 * Basic implementation of an {@link GlobalAutoCommandsDialogModel}.
 */
public class CoreGlobalAutoCommandsDialogModel implements GlobalAutoCommandsDialogModel {

    private final AutoCommandManager autoCommandManager;
    private final ResponseValidator responseValidator;
    private boolean loaded;
    private Optional<AutoCommand> globalAutoCommand;
    private MutableAutoCommand autoCommand;

    @Inject
    public CoreGlobalAutoCommandsDialogModel(final AutoCommandManager autoCommandManager,
            final ResponseValidator responseValidator) {
        this.autoCommandManager = autoCommandManager;
        this.responseValidator = responseValidator;
        autoCommand = getAutoCommand(Optional.empty());
    }

    @Override
    public void load() {
        loaded = true;
        globalAutoCommand = autoCommandManager.getGlobalAutoCommand();
        autoCommand = getAutoCommand(globalAutoCommand);
    }

    @Override
    public String getResponse() {
        checkState(loaded);
        return autoCommand.getResponse();
    }

    @Override
    public void setResponse(final String response) {
        checkState(loaded);
        checkNotNull(response);
        autoCommand.setResponse(response);
    }

    @Override
    public Validator<String> getResponseValidator() {
        checkState(loaded);
        return responseValidator;
    }

    @Override
    public boolean isResponseValid() {
        checkState(loaded);
        return !getResponseValidator().validate(autoCommand.getResponse()).isFailure();
    }

    @Override
    public boolean isSaveAllowed() {
        checkState(loaded);
        return isResponseValid();
    }

    @Override
    public void save() {
        checkState(loaded);
        checkState(isSaveAllowed());
        if (globalAutoCommand.isPresent() && !autoCommand.getResponse().isEmpty()) {
            autoCommandManager.replaceAutoCommand(globalAutoCommand.get(), getAutoCommand(autoCommand));
        } else if (globalAutoCommand.isPresent()) {
            autoCommandManager.removeAutoCommand(getAutoCommand(autoCommand));
        } else {
            autoCommandManager.addAutoCommand(getAutoCommand(autoCommand));
        }
    }

    private MutableAutoCommand getAutoCommand(final Optional<AutoCommand> command) {
        if (command.isPresent()) {
            final AutoCommand c = command.get();
            return new MutableAutoCommand(c.getServer(),
                    c.getNetwork(), c.getProfile(), c.getResponse(), c.getType());
        } else {
            return new MutableAutoCommand(Optional.empty(), Optional.empty(), Optional.empty(), "",
                    AutoCommandType.GLOBAL);
        }
    }

    private AutoCommand getAutoCommand(final MutableAutoCommand command) {
        return AutoCommand.create(command.getServer(), command.getNetwork(), command.getProfile(),
                command.getResponse());
    }
}
