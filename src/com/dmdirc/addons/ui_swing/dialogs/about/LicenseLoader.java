/*
 * 
 * Copyright (c) 2006-2008 Chris Smith, Shane Mc Cormack, Gregory Holmes
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

package com.dmdirc.addons.ui_swing.dialogs.about;

import com.dmdirc.addons.ui_swing.components.GenericListModel;
import com.dmdirc.addons.ui_swing.components.LoggingSwingWorker;
import com.dmdirc.logger.ErrorLevel;
import com.dmdirc.logger.Logger;
import com.dmdirc.util.resourcemanager.ResourceManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

/**
 * Background loader of licenses into a list.
 */
public class LicenseLoader extends LoggingSwingWorker<Void, Void> {

    /** Model to load licenses into. */
    private GenericListModel<License> model;
    /** Void reference. */
    private Void Void;

    /**
     * Instantiates a new license loader.
     *
     * @param model Model to load licenses into
     */
    public LicenseLoader(final GenericListModel<License> model) {
        this.model = model;
    }

    /**
     * {@inheritDoc}
     *
     * @throws Exception on any exception in the background task
     */
    @Override
    protected Void doInBackground() throws Exception {
        final ResourceManager rm = ResourceManager.getResourceManager();
        if (rm == null) {
            Logger.userError(ErrorLevel.LOW, "Unable to load licenses, " +
                    "no resource manager");
        } else {
            final Map<String, InputStream> licenses =
                    new TreeMap<String, InputStream>(rm.
                    getResourcesStartingWithAsInputStreams(
                    "com/dmdirc/licenses/"));
            for (Entry<String, InputStream> entry : licenses.entrySet()) {
                final String licenseString = entry.getKey().substring(entry.
                        getKey().
                        lastIndexOf('/') + 1);
                if (licenseString.length() > 1) {
                    final String licenseStringParts[] = licenseString.split(
                            " - ");
                    final License license = new License(licenseStringParts[1],
                            licenseStringParts[0], "<html><h1>" +
                            licenseStringParts[1] + "</h1><p>" + readInputStream(
                            entry.getValue()).replaceAll("\n", "<br>") +
                            "</p></html>");
                    model.add(license);
                }
            }
        }

        return Void;
    }

    /** {@inheritDoc} */
    @Override
    protected void done() {
        super.done();
    }

    /**
     * Converts an input stream into a string.
     *
     * @param stream Stream to convert
     *
     * @return Contents of the input stream
     */
    private String readInputStream(final InputStream stream) {
        String line;
        final BufferedReader input =
                new BufferedReader(new InputStreamReader(stream));
        final StringBuilder text = new StringBuilder();

        try {
            line = input.readLine();
            while (line != null) {
                text.append(line).append("\n");
                line = input.readLine();
            }
        } catch (IOException ex) {
            //Ignore
        }

        return text.toString();
    }
}
