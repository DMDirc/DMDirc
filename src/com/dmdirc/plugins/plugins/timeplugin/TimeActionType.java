/*
 * Copyright (c) 2006-2007 Chris Smith, Shane Mc Cormack, Gregory Holmes
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

package uk.org.ownage.dmdirc.plugins.plugins.timeplugin;

import uk.org.ownage.dmdirc.actions.ActionMetaType;
import uk.org.ownage.dmdirc.actions.ActionType;

/**
 * Lists the actions that the time plugin will raise.
 * @author chris
 */
public enum TimeActionType implements ActionType {
    
    TIME_MINUTE("Every minute"),
    TIME_HOUR("Every hour"),
    TIME_DAY("Every day");
    
    private final String name;
    
    /**
     * Creates a new instance of a TimeActionType.
     * @param name The name of the action type
     */
    TimeActionType(final String name) {
        this.name = name;
    }
    
    /** {@inheritDoc} */
    public ActionMetaType getType() {
        return TimeActionMetaType.TIME_TIME;
    }
    
    /** {@inheritDoc} */
    public String getName() {
        return name;
    }
    
}
