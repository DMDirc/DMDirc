/*
 * Copyright (c) 2006-2008 Chris Smith, Shane Mc Cormack, Gregory Holmes
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

package com.dmdirc.addons.dcc.actions;

import com.dmdirc.actions.interfaces.ActionMetaType;
import com.dmdirc.actions.interfaces.ActionType;

/**
 * DCC actions.
 * 
 * @author chris
 */
public enum DccActions implements ActionType {
    
    /** Chat request. */
    DCC_CHAT_REQUEST(DccEvents.DCC_CHAT_REQUEST, "Chat request"),
    /** File offered. */
    DCC_FILE_OFFERED(DccEvents.DCC_FILE_REQUEST, "File offered");
    
    /** The type of this action. */
    private final ActionMetaType type;
    
    /** The name of this action. */
    private final String name;
    
    /**
     * Constructs a new core action.
     * @param type The type of this action
     * @param name The name of this action
     */
    DccActions(final ActionMetaType type, final String name) {
        this.type = type;
        this.name = name;
    }
    
    /** {@inheritDoc} */
    @Override
    public ActionMetaType getType() {
        return type;
    }
    
    /** {@inheritDoc} */
    @Override
    public String getName() {
        return name;
    }
}
