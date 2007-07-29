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

package com.dmdirc.actions.wrappers;

import com.dmdirc.actions.Action;
import com.dmdirc.actions.ActionManager;
import com.dmdirc.actions.CoreActionType;
import com.dmdirc.logger.ErrorLevel;
import com.dmdirc.logger.Logger;

/**
 * An action wrapper for performs.
 *
 * @author Chris
 */
public class PerformWrapper extends ActionWrapper {
    
    private static PerformWrapper me;
    
    /**
     * Creates a new instance of PerformWrapper.
     */
    private PerformWrapper() {
        super();
    }
    
    /**
     * Retrieves a singleton instance of this perform wrapper.
     *
     * @return A singleton instance of PerformWrapper
     */
    public static synchronized PerformWrapper getPerformWrapper() {
        if (me == null) {
            me = new PerformWrapper();
            ActionManager.registerWrapper(me);
        }
        
        return me;
    }
    
    /** {@inheritDoc} */
    public String getGroupName() {
        return "performs";
    }
    
    /** {@inheritDoc} */
    @Override
    public void registerAction(final Action action) {
        if (action.getTriggers().length == 1 && action.getTriggers()[0] == CoreActionType.SERVER_CONNECTED) {
            super.registerAction(action);
        } else {
            Logger.userError(ErrorLevel.MEDIUM, "Invalid perform action: " + action.getName());
        }
    }
    
}
