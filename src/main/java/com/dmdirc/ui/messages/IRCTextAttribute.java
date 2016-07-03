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

package com.dmdirc.ui.messages;

import java.io.InvalidObjectException;
import java.text.AttributedCharacterIterator.Attribute;
import java.util.HashMap;
import java.util.Map;

/**
 * Defines attribute keys that can be used to identify text attributes. These keys are used in
 * AttributedCharacterIterator and AttributedString.
 */
public final class IRCTextAttribute extends Attribute {

    /** A version number for this class. */
    private static final long serialVersionUID = 1;
    /** table of all instances in this class, used by readResolve. */
    private static final Map<String, IRCTextAttribute> INSTANCE_MAP = new HashMap<>(1);

    /**
     * Constructs an Attribute with the given name.
     *
     * @param name name for the attribute
     */
    protected IRCTextAttribute(final String name) {
        super(name);
        if (getClass() == IRCTextAttribute.class) {
            INSTANCE_MAP.put(name, this);
        }
    }

    /**
     * Resolves instances being deserialized to the predefined constants.
     *
     * @return IRCTextAttribute instance
     *
     * @throws InvalidObjectException when the class being deserialized is not an instance of
     *                                IRCTextAttribute
     */
    @Override
    protected Object readResolve() throws InvalidObjectException {
        if (getClass() != IRCTextAttribute.class) {
            throw new InvalidObjectException("subclass didn't correctly implement readResolve");
        }

        final IRCTextAttribute instance = INSTANCE_MAP.get(getName());
        if (instance == null) {
            throw new InvalidObjectException("unknown attribute name");
        } else {
            return instance;
        }
    }
    /** Hyperlink attribute. */
    public static final IRCTextAttribute HYPERLINK = new IRCTextAttribute("hyperlink");
    /** Nickname attribute. */
    public static final IRCTextAttribute NICKNAME = new IRCTextAttribute("nickname");
    /** Channel attribute. */
    public static final IRCTextAttribute CHANNEL = new IRCTextAttribute("channel");
    /** Smiley attribute. */
    public static final IRCTextAttribute SMILEY = new IRCTextAttribute("smiley");
    /** Tooltip attribute. */
    public static final IRCTextAttribute TOOLTIP = new IRCTextAttribute("tooltip");

}
