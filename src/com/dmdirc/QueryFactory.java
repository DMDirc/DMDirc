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

package com.dmdirc;

import com.dmdirc.interfaces.CommandController;
import com.dmdirc.messages.MessageSinkManager;
import com.dmdirc.ui.input.TabCompleterFactory;
import com.dmdirc.ui.messages.ColourManagerFactory;
import com.dmdirc.util.URLBuilder;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Factory for {@link Query}s.
 */
@Singleton
public class QueryFactory {

    private final TabCompleterFactory tabCompleterFactory;
    private final CommandController commandController;
    private final MessageSinkManager messageSinkManager;
    private final URLBuilder urlBuilder;
    private final ColourManagerFactory colourManagerFactory;

    @Inject
    public QueryFactory(final TabCompleterFactory tabCompleterFactory,
            final CommandController commandController, final MessageSinkManager messageSinkManager,
            final URLBuilder urlBuilder, final ColourManagerFactory colourManagerFactory) {
        this.tabCompleterFactory = tabCompleterFactory;
        this.commandController = commandController;
        this.messageSinkManager = messageSinkManager;
        this.urlBuilder = urlBuilder;
        this.colourManagerFactory = colourManagerFactory;
    }

    public Query getQuery(final Server server, final String host) {
        return new Query(server, host, tabCompleterFactory, commandController,
                messageSinkManager, urlBuilder, colourManagerFactory);
    }

}
