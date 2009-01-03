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

package com.dmdirc.ui.swing.dialogs.about;

import com.dmdirc.Main;
import com.dmdirc.ui.core.util.Info;
import com.dmdirc.ui.swing.SwingController;
import com.dmdirc.ui.swing.UIUtilities;


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

        this.setOpaque(UIUtilities.getTabbedPaneOpaque());
        initComponents();
    }

    /** Initialises the components. */
    private void initComponents() {
        final JScrollPane scrollPane = new JScrollPane();
        final JEditorPane infoPane = new JEditorPane("text/html", "<html>" +
                "<b>DMDirc version: </b>" + Info.getDMDircVersion() + "<br>" +
                "<b>Profile directory: </b>" + Main.getConfigDir() + "<br>" +
                "<b>Java version: </b>" + Info.getJavaVersion() + "<br>" +
                "<b>OS Version: </b>" + Info.getOSVersion() + "<br>" +
                "<b>Look and Feel: </b>" + SwingController.getLookAndFeel() + "<br>" +
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
