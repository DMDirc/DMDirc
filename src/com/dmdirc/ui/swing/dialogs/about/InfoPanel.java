/*
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

package com.dmdirc.ui.swing.dialogs.about;

import com.dmdirc.Main;

import java.util.Locale;

import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

import net.miginfocom.swing.MigLayout;

/** Info panel. */
public final class InfoPanel extends JPanel {

    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 1;

    /** Creates a new instance of InfoPanel. */
    public InfoPanel() {
        super();

        initComponents();
    }

    /**
     * Returns the DMDirc version info
     * 
     * @return DMDirc version string
     */
    private String getDMDircVersion() {
        return Main.VERSION + " (" + Main.SVN_REVISION + "; " +
                Main.UPDATE_CHANNEL + ")";
    }

    /**
     * Returns the systems java version
     * 
     * @return Java version string
     */
    private String getJavaVersion() {
        return System.getProperty("java.vm.name", "unknown") + " " +
                System.getProperty("java.vm.version", "unknown") + 
                " [" + System.getProperty("java.vm.vendor", "uknown") + "]";
    }

    /**
     * Returns the systems OS version
     * 
     * @return OS version string
     */
    private String getOSVersion() {
        return System.getProperty("os.name", "unknown") + " " +
                System.getProperty("os.version", "unknown") + " " +
                System.getProperty("os.arch", "unknown") + "; " +
                System.getProperty("file.encoding", "unknown") + "; " + Locale.getDefault().
                toString();
    }

    /** Initialises the components. */
    private void initComponents() {
        final JScrollPane scrollPane = new JScrollPane();
        final JEditorPane infoPane = new JEditorPane("text/html", "<html>" +
                "<b>DMDirc version: </b>" + getDMDircVersion() + "<br>" +
                "<b>Profile directory: </b>" + Main.getConfigDir() + "<br>" +
                "<b>Java version: </b>" + getJavaVersion() + "<br>" +
                "<b>OS Version: </b>" + getOSVersion() + "<br>" +
                "</html>");
        infoPane.setEditable(false);
        scrollPane.setViewportView(infoPane);

        SwingUtilities.invokeLater(new Runnable() {

            /** {@inheritDoc} */
            @Override
            public void run() {
                scrollPane.getVerticalScrollBar().setValue(0);
            }
        });

        setLayout(new MigLayout("ins rel, fill"));
        add(scrollPane, "grow, wrap");
    }
}
