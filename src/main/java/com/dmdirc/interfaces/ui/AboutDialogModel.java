/*
 * Copyright (c) 2006-2017 DMDirc Developers
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the
 * Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS
 * OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.dmdirc.interfaces.ui;

import com.dmdirc.ui.core.about.Developer;
import com.dmdirc.ui.core.about.InfoItem;
import com.dmdirc.ui.core.about.LicensedComponent;

import java.util.List;

/**
 * Model representing information required to show an about dialog.
 */
public interface AboutDialogModel {

    /**
     * Initialises the model.  Should be called before the other methods are used.
     */
    void load();

    /**
     * Returns the HTML about text for the client.
     *
     * @return HTML to display
     */
    String getAbout();

    /**
     * Returns the list of main developers in the client.
     *
     * @return Developer list
     */
    List<Developer> getMainDevelopers();

    /**
     * Returns the list of other developers in the client.
     *
     * @return Developer list
     */
    List<Developer> getOtherDevelopers();

    /**
     * Returns a list of information items to show to the user.
     *
     * @return List of information items
     */
    List<InfoItem> getInfo();

    /**
     * Returns a list of licensed components in the client.
     *
     * @return List of licensed components
     */
    List<LicensedComponent> getLicensedComponents();
}
