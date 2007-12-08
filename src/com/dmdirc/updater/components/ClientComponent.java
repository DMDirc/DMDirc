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

package com.dmdirc.updater.components;

import com.dmdirc.Main;
import com.dmdirc.updater.UpdateComponent;

import java.io.File;

/**
 * Represents the client component, which covers the core client resources.
 * 
 * @author chris
 */
public class ClientComponent implements UpdateComponent {

    /** {@inheritDoc} */
    @Override
    public String getName() {
        return "client";
    }

    /** {@inheritDoc} */
    @Override
    public int getVersion() {
        return Main.RELEASE_DATE;
    }

    /** {@inheritDoc} */
    @Override
    public void doInstall(final String path) {
        final File tmpFile = new File(path);
        final File targetFile = new File(tmpFile.getParent() + File.separator + ".DMDirc.jar");
        if (targetFile.exists()) {
            targetFile.delete();
        }
        tmpFile.renameTo(targetFile);
    }

}
