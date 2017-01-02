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

package com.dmdirc;

import com.dmdirc.util.io.FileUtils;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

/**
 * OSGi bundle activator for the client. Eventually this shouldn't need to exist, as everything will be split into
 * its own bundle.
 */
public class ClientActivator implements BundleActivator {

    @Override
    public void start(final BundleContext context) throws Exception {
        System.out.println(context.getBundle().getLocation());
        System.out.println(FileUtils.getApplicationPath(ClientActivator.class));
        Main.main("-d", "etc/profile");
    }

    @Override
    public void stop(final BundleContext context) throws Exception {
        // Uh... *SHRUG*
    }

}
