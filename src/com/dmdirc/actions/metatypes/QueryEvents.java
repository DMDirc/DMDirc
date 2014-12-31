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

package com.dmdirc.actions.metatypes;

import com.dmdirc.Query;
import com.dmdirc.interfaces.actions.ActionMetaType;
import com.dmdirc.parser.interfaces.ClientInfo;

/**
 * Defines query-related events.
 */
public enum QueryEvents implements ActionMetaType {

    /** Query event type. */
    QUERY_EVENT(new String[]{"query"}, Query.class),
    /** Query event with argument. */
    QUERY_EVENT_WITH_ARG(new String[]{"query", "message"}, Query.class, String.class),
    /** Query event with argument and source. */
    QUERY_SOURCED_EVENT_WITH_ARG(new String[]{"query", "source", "message"}, Query.class,
            ClientInfo.class, String.class);
    /** The names of the arguments for this meta type. */
    private final String[] argNames;
    /** The classes of the arguments for this meta type. */
    private final Class<?>[] argTypes;

    /**
     * Creates a new instance of this meta-type.
     *
     * @param argNames The names of the meta-type's arguments
     * @param argTypes The types of the meta-type's arguments
     */
    QueryEvents(final String[] argNames, final Class<?>... argTypes) {
        this.argNames = argNames;
        this.argTypes = argTypes;
    }

    @Override
    public int getArity() {
        return argNames.length;
    }

    @Override
    public Class<?>[] getArgTypes() {
        return argTypes;
    }

    @Override
    public String[] getArgNames() {
        return argNames;
    }

    @Override
    public String getGroup() {
        return "Query Events";
    }

}
