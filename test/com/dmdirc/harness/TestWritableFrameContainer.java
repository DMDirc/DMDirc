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

package com.dmdirc.harness;

import com.dmdirc.WritableFrameContainer;
import com.dmdirc.commandparser.CommandManager;
import com.dmdirc.commandparser.parsers.GlobalCommandParser;
import com.dmdirc.interfaces.CommandController;
import com.dmdirc.interfaces.Connection;
import com.dmdirc.interfaces.config.AggregateConfigProvider;
import com.dmdirc.messages.MessageSinkManager;
import com.dmdirc.ui.input.TabCompleter;
import com.dmdirc.util.URLBuilder;

import java.util.Collections;

import static org.mockito.Mockito.*;

public class TestWritableFrameContainer extends WritableFrameContainer {

    private final int lineLength;

    public TestWritableFrameContainer(final int lineLength,
            final AggregateConfigProvider cm, final CommandManager commandManager,
            final MessageSinkManager messageSinkManager, final URLBuilder urlBuilder) {
        super("raw", "Raw", "(Raw)", cm,
                new GlobalCommandParser(cm, commandManager), messageSinkManager, urlBuilder,
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
    public Connection getConnection() {
        return null;
    }

    @Override
    public TabCompleter getTabCompleter() {
        return new TabCompleter(mock(CommandController.class), getConfigManager());
    }
}
