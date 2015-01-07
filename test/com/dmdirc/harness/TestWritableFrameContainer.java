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

package com.dmdirc.harness;

import com.dmdirc.DMDircMBassador;
import com.dmdirc.FrameContainer;
import com.dmdirc.commandparser.CommandManager;
import com.dmdirc.commandparser.parsers.GlobalCommandParser;
import com.dmdirc.interfaces.CommandController;
import com.dmdirc.interfaces.Connection;
import com.dmdirc.interfaces.config.AggregateConfigProvider;
import com.dmdirc.ui.input.TabCompleter;
import com.dmdirc.ui.messages.BackBufferFactory;
import com.dmdirc.ui.messages.sink.MessageSinkManager;

import java.util.Collections;
import java.util.Optional;

import static org.mockito.Mockito.mock;

public class TestWritableFrameContainer extends FrameContainer {

    private final int lineLength;

    public TestWritableFrameContainer(final int lineLength,
            final AggregateConfigProvider cm, final CommandManager commandManager,
            final MessageSinkManager messageSinkManager,
            final DMDircMBassador eventBus,
            final BackBufferFactory backBufferFactory) {
        super(null, "raw", "Raw", "(Raw)", cm, backBufferFactory,
                new GlobalCommandParser(cm, commandManager, eventBus),
                new TabCompleter(mock(CommandController.class), cm),
                messageSinkManager,
                eventBus,
                Collections.<String>emptySet());

        this.lineLength = lineLength;
    }

    @Override
    public void sendLine(final String line) {
        // Do nothing
    }

    @Override
    public int getMaxLineLength() {
        return lineLength;
    }

    @Override
    public Optional<Connection> getConnection() {
        return Optional.empty();
    }

}
