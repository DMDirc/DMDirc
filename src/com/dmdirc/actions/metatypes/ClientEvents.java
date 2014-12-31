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

import com.dmdirc.FrameContainer;
import com.dmdirc.commandparser.PopupMenu;
import com.dmdirc.commandparser.PopupType;
import com.dmdirc.config.prefs.PreferencesCategory;
import com.dmdirc.config.prefs.PreferencesDialogModel;
import com.dmdirc.interfaces.actions.ActionMetaType;
import com.dmdirc.interfaces.config.AggregateConfigProvider;

import javax.swing.KeyStroke;
import javax.swing.text.StyledDocument;

/**
 * Defines client-wide events.
 */
public enum ClientEvents implements ActionMetaType {

    /** Client event type. */
    CLIENT_EVENT(new String[]{}),
    /** Client event type, with a frame argument. */
    WINDOW_EVENT(new String[]{"window"}, FrameContainer.class),
    /** Client event with frame and message. */
    WINDOW_EVENT_WITH_MESSAGE(new String[]{"window", "message"}, FrameContainer.class, String.class),
    /** A popup-related event. */
    POPUP_EVENT(new String[]{"popup type", "popup", "configuration provider"}, PopupType.class,
            PopupMenu.class, AggregateConfigProvider.class),
    /** Client event type, with a key argument. */
    CLIENT_EVENT_WITH_KEY(new String[]{"key event"}, KeyStroke.class),
    /** Client event with an origin and
     * editable buffer. */
    CLIENT_EVENT_WITH_BUFFER(new String[]{"origin", "buffer"}, FrameContainer.class,
            StringBuffer.class),
    /** Client event with preferences manager. */
    CLIENT_EVENT_WITH_PREFS(new String[]{"preferences manager"}, PreferencesDialogModel.class),
    /** Client event with preferences category. */
    CLIENT_EVENT_WITH_PREFS_CAT(new String[]{"preferences category", "is server"},
            PreferencesCategory.class, Boolean.class),
    /** Client event with a styled doc. */
    CLIENT_EVENT_WITH_STYLE(new String[]{"styled document", "start offset", "length"},
            StyledDocument.class, Integer.class, Integer.class),
    /** Unknown command event type. */
    UNKNOWN_COMMAND(new String[]{"source", "command", "arguments"}, FrameContainer.class,
            String.class, String[].class);
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
    ClientEvents(final String[] argNames, final Class<?>... argTypes) {
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
        return "General Events";
    }

}
