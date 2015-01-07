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
import com.dmdirc.events.ProgramErrorDeletedEvent;
import com.dmdirc.events.ProgramErrorEvent;
import com.dmdirc.events.ProgramErrorStatusEvent;
import com.dmdirc.interfaces.ui.ErrorsDialogModel;
import com.dmdirc.interfaces.ui.ErrorsDialogModelListener;
import com.dmdirc.logger.ErrorManager;
import com.dmdirc.logger.ProgramError;
import com.dmdirc.util.collections.ListenerList;

import java.util.List;
import java.util.Optional;

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
    private Optional<ProgramError> selectedError;

    @Inject
    public CoreErrorsDialogModel(final ErrorManager errorManager,
            final DMDircMBassador eventBus) {
        this.listenerList = new ListenerList();
        this.errorManager = errorManager;
        this.eventBus = eventBus;
        selectedError = Optional.empty();
    }

    @Override
    public void load() {
        eventBus.subscribe(this);
    }

    @Override
    public void unload() {
        eventBus.unsubscribe(this);
    }

    @Override
    public List<ProgramError> getErrors() {
        return errorManager.getErrors();
    }

    @Override
    public Optional<ProgramError> getSelectedError() {
        return selectedError;
    }

    @Override
    public void setSelectedError(final Optional<ProgramError> selectedError) {
        checkNotNull(selectedError);
        this.selectedError = selectedError;
        listenerList.getCallable(ErrorsDialogModelListener.class).selectedErrorChanged(selectedError);
    }

    @Override
    public void deleteSelectedError() {
        selectedError.ifPresent(errorManager::deleteError);
    }

    @Override
    public void deleteAllErrors() {
        errorManager.deleteAll();
    }

    @Override
    public void sendSelectedError() {
        selectedError.ifPresent(errorManager::sendError);
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
        return selectedError.isPresent();
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
        listenerList.getCallable(ErrorsDialogModelListener.class).errorStatusChanged(event.getError());
    }

    @Handler
    public void handleErrorDeleted(final ProgramErrorDeletedEvent event) {
        listenerList.getCallable(ErrorsDialogModelListener.class).errorDeleted(event.getError());
    }

    @Handler
    public void handleErrorAdded(final ProgramErrorEvent event) {
        listenerList.getCallable(ErrorsDialogModelListener.class).errorAdded(event.getError());
    }
}
