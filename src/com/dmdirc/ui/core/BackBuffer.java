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

package com.dmdirc.ui.core;

import com.dmdirc.DMDircMBassador;
import com.dmdirc.FrameContainer;
import com.dmdirc.ui.messages.ColourManagerFactory;
import com.dmdirc.ui.messages.IRCDocument;
import com.dmdirc.ui.messages.Styliser;

/**
 * Models the history of a window in the client.
 */
public class BackBuffer {

    private final IRCDocument document;
    private final Styliser styliser;

    public BackBuffer(final FrameContainer owner, final ColourManagerFactory colourManagerFactory,
            final DMDircMBassador eventBus) {
        this.styliser = new Styliser(
                owner.getOptionalConnection().orElse(null),
                owner.getConfigManager(),
                colourManagerFactory.getColourManager(owner.getConfigManager()),
                owner.getEventBus());
        this.document = new IRCDocument(owner.getConfigManager(), styliser, eventBus);
    }

    public IRCDocument getDocument() {
        return document;
    }

    public Styliser getStyliser() {
        return styliser;
    }

}
