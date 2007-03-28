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

package uk.org.ownage.dmdirc;

import java.util.Date;
import javax.swing.Icon;

import uk.org.ownage.dmdirc.ui.interfaces.StatusErrorNotifier;

/**
 *
 */
public class Error {
    
    /** Error icon. */
    private Icon icon;
    
    /** Error Message. */
    private StatusErrorNotifier notifier;
    
    /** Time. */
    private Date date;
    
    /** Creates a new instance of Error. */
    public Error(Icon newIcon, StatusErrorNotifier newNotifier) {
        this.icon = newIcon;
        this.notifier = newNotifier;
        date = new Date(System.currentTimeMillis());
    }
    
    /**
     * Returns this errors icon.
     * @return error icon
     */
    public Icon getIcon() {
        return this.icon;
    }
    
    /**
     * Returns this errors notifier.
     * @return error notifier
     */
    public StatusErrorNotifier getNotifier() {
        return this.notifier;
    }
    
    /**
     * Returns this errors time.
     * @return error time
     */
    public Date getDate() {
        return this.date;
    }
    
}
