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

package com.dmdirc.ui.core.errors;

import com.dmdirc.DMDircMBassador;
import com.dmdirc.events.NonFatalProgramErrorEvent;
import com.dmdirc.events.ProgramErrorDeletedEvent;
import com.dmdirc.events.ProgramErrorStatusEvent;
import com.dmdirc.interfaces.ui.ErrorsDialogModel;
import com.dmdirc.interfaces.ui.ErrorsDialogModelListener;
import com.dmdirc.logger.ErrorManager;
import com.dmdirc.logger.ErrorReportStatus;
import com.dmdirc.logger.ProgramError;
import com.dmdirc.util.collections.ListenerList;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import javax.inject.Inject;

import net.engio.mbassy.listener.Handler;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Basic implementation for a {@link ErrorsDialogModel}.
 */
public class CoreErrorsDialogModel implements ErrorsDialogModel {

    private final ListenerList listenerList;
    private final ErrorManager errorManager;
    private final DMDircMBassador eventBus;
    private Optional<DisplayableError> selectedError;
    private final Set<DisplayableError> errors;

    @Inject
    public CoreErrorsDialogModel(final ErrorManager errorManager,
            final DMDircMBassador eventBus) {
        this.listenerList = new ListenerList();
        this.errorManager = errorManager;
        this.eventBus = eventBus;
        errors = new CopyOnWriteArraySet<>();
        selectedError = Optional.empty();
    }

    @Override
    public void load() {
        eventBus.subscribe(this);
        errorManager.getErrors().forEach(e -> errors.add(getDisplayableError(e)));
    }

    @Override
    public void unload() {
        eventBus.unsubscribe(this);
        errors.clear();
    }

    @Override
    public Set<DisplayableError> getErrors() {
        return Collections.unmodifiableSet(errors);
    }

    @Override
    public Optional<DisplayableError> getSelectedError() {
        return selectedError;
    }

    @Override
    public void setSelectedError(final Optional<DisplayableError> selectedError) {
        checkNotNull(selectedError);
        this.selectedError = selectedError;
        listenerList.getCallable(ErrorsDialogModelListener.class).selectedErrorChanged(selectedError);
    }

    @Override
    public void deleteSelectedError() {
        selectedError.map(DisplayableError::getProgramError).ifPresent(errorManager::deleteError);
    }

    @Override
    public void deleteAllErrors() {
        errorManager.deleteAll();
    }

    @Override
    public void sendSelectedError() {
        selectedError.map(DisplayableError::getProgramError).ifPresent(errorManager::sendError);
    }

    @Override
    public boolean isDeletedAllowed() {
        return selectedError.isPresent();
    }

    @Override
    public boolean isDeleteAllAllowed() {
        return !errorManager.getErrors().isEmpty();
    }

    @Override
    public boolean isSendAllowed() {
        final ErrorReportStatus status = selectedError.map(DisplayableError::getReportStatus)
                .orElse(ErrorReportStatus.NOT_APPLICABLE);
        return status == ErrorReportStatus.WAITING || status == ErrorReportStatus.ERROR;
    }

    @Override
    public void addListener(final ErrorsDialogModelListener listener) {
        checkNotNull(listener);
        listenerList.add(ErrorsDialogModelListener.class, listener);
    }

    @Override
    public void removeListener(final ErrorsDialogModelListener listener) {
        checkNotNull(listener);
        listenerList.remove(ErrorsDialogModelListener.class, listener);
    }

    @Handler
    public void handleErrorStatusChanged(final ProgramErrorStatusEvent event) {
        errors.stream().filter(e -> e.getProgramError().equals(event.getError()))
                .findFirst().ifPresent(e -> {
            e.setReportStatus(event.getError().getReportStatus());
            listenerList.getCallable(ErrorsDialogModelListener.class).errorStatusChanged(e);
        });
    }

    @Handler
    public void handleErrorDeleted(final ProgramErrorDeletedEvent event) {
        errors.stream().filter(e -> e.getProgramError().equals(event.getError()))
                .findFirst().ifPresent(e -> {
            errorManager.deleteError(e.getProgramError());
            errors.remove(e);
            listenerList.getCallable(ErrorsDialogModelListener.class).errorDeleted(e);
            setSelectedError(Optional.empty());
        });
    }

    @Handler
    public void handleErrorAdded(final NonFatalProgramErrorEvent event) {
        final Optional<DisplayableError> de = errors.stream().filter(
                e -> e.getProgramError().equals(event.getError())).findFirst();
        if (!de.isPresent()) {
            final DisplayableError error = getDisplayableError(event.getError());
            errors.add(error);
            listenerList.getCallable(ErrorsDialogModelListener.class).errorAdded(error);
        }
    }

    private DisplayableError getDisplayableError(final ProgramError error) {
        final String details;
        if (error.getThrowableAsString().isEmpty()) {
            details = error.getMessage();
        } else {
            details = error.getMessage()
                    + '\n' + error.getThrowableAsString();
        }
        return new DisplayableError(error.getDate(), error.getMessage(), details,
                error.getLevel(), error.getReportStatus(), error);
    }
}
