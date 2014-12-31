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

package com.dmdirc.interfaces.ui;

import com.dmdirc.config.profiles.Profile;

import java.util.List;
import java.util.Optional;

/**
 * Listener for various events in an new server dialog model.
 */
public interface NewServerDialogModelListener {

    /**
     * Called when the selected profile changes.
     *
     * @param oldProfile Old profile
     * @param newProfile New profile
     */
    void selectedProfileChanged(Optional<Profile> oldProfile,
            Optional<Profile> newProfile);

    /**
     * Called when the profile list is changed.
     *
     * @param profiles New profile list
     */
    void profileListChanged(List<Profile> profiles);

    /**
     * Called when the details of the server are changed.
     *
     * @param hostname      Server hostname
     * @param port          Server port
     * @param password      Server password
     * @param ssl           Should we connect over SSL
     * @param saveAsDefault Should we save these settings as the default
     */
    void serverDetailsChanged(Optional<String> hostname, Optional<Integer> port,
            Optional<String> password, boolean ssl, boolean saveAsDefault);

}
