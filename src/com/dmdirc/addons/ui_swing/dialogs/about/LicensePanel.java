/*
 * Copyright (c) 2006-2009 Chris Smith, Shane Mc Cormack, Gregory Holmes
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

import com.dmdirc.addons.ui_swing.UIUtilities;
import com.dmdirc.util.resourcemanager.ResourceManager;

import java.awt.Font;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;

import net.miginfocom.swing.MigLayout;

/**
 * License panel.
 */
public final class LicensePanel extends JPanel {

    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 3;
    /** License scroll pane. */
    private JScrollPane scrollPane;

    /** Creates a new instance of LicensePanel. */
    public LicensePanel() {
        super();

        this.setOpaque(UIUtilities.getTabbedPaneOpaque());
        initComponents();
    }

    /** Initialises the components. */
    private void initComponents() {
        final JEditorPane license;
        final ResourceManager rm = ResourceManager.getResourceManager();

        license = new JEditorPane();
        license.setEditorKit(new HTMLEditorKit());
        final Font font = UIManager.getFont("Label.font");
        ((HTMLDocument) license.getDocument()).getStyleSheet().addRule("body " +
                "{ font-family: " + font.getFamily() + "; " + "font-size: " +
                font.getSize() + "pt; }");

        final StringBuilder licenseText = new StringBuilder();
        licenseText.append("<html>");

        if (rm == null) {
            licenseText.append("Error loading licenses.");
        } else {
            final Map<String, InputStream> licenses =
                    new TreeMap<String, InputStream>(rm.
                    getResourcesStartingWithAsInputStreams(
                    "com/dmdirc/licenses/"));
            licenseText.append("Below are the licenses used in various " +
                    "components of DMDirc: <br><ul>");
            for (Entry<String, InputStream> entry : licenses.entrySet()) {
                final String licenseString = entry.getKey().substring(entry.
                        getKey().
                        lastIndexOf('/') + 1);
                if (licenseString.length() > 1) {
                    licenseText.append("<li>");
                    licenseText.append(licenseString);
                    licenseText.append("</li>");
                }
            }
            licenseText.append("</ul>");
            for (Entry<String, InputStream> entry : licenses.entrySet()) {
                final String licenseString = entry.getKey().substring(entry.
                        getKey().
                        lastIndexOf('/') + 1);
                if (licenseString.length() > 1) {
                    licenseText.append("<h1>");
                    licenseText.append(licenseString.substring(0,
                            licenseString.lastIndexOf(" - ")));
                    licenseText.append("</h1>");
                    licenseText.append(
                            readInputStream(entry.getValue()).replaceAll("\n",
                            "<br>"));
                }
            }
        }
        licenseText.append("</html>");
        license.setText(licenseText.toString());
        license.setEditable(false);

        scrollPane = new JScrollPane(license);
        SwingUtilities.invokeLater(new Runnable() {

            /** {@inheritDoc} */
            @Override
            public void run() {
                scrollPane.getVerticalScrollBar().setValue(0);
            }
        });

        setLayout(new MigLayout("ins rel, fill"));
        add(scrollPane, "grow, push");
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
                text.append(line);
                text.append("<br>");
                line = input.readLine();
            }
        } catch (IOException ex) {
            //Ignore
        }

        return text.toString();

    }
}
