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

import com.dmdirc.ui.core.autocommands.MutableAutoCommand;

import java.util.Collection;
import java.util.Optional;

/**
 * Listener for the various events in an {@link AutoCommandsModel}.
 */
public interface AutoCommandsModelListener {

    /**
     * Called when the selected command is changed.
     *
     * @param command New command
     */
    void selectedCommandChanged(Optional<MutableAutoCommand> command);

    /**
     * Called when the list of commands is replaced.
     *
     * @param commands New commands
     */
    void setAutoCommands(Collection<MutableAutoCommand> commands);

    /**
     * Called when a command in the model is changed.
     *
     * @param command Command that has changed
     */
    void commandEdited(MutableAutoCommand command);

    /**
     * Called when a single command is added to the model.
     *
     * @param command Command that has been added
     */
    void commandAdded(MutableAutoCommand command);

    /**
     * Called when a single command has been removed from the model.
     *
     * @param command Command that has been removed
     */
    void commandRemoved(MutableAutoCommand command);

}
