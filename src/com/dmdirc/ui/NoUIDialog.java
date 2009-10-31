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

package com.dmdirc.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.concurrent.Semaphore;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;

/**
 * Simple Dialog to inform the user there are no UI plugins.
 */
public class NoUIDialog extends JDialog {

    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = -528603916540455179L;
    /** Dialog heading text. */
    public static final String TITLE = "No UIs Found";
    /** Dialog body text. */
    public static final String BODY = "DMDirc cannot find any UI plugins, " +
            "which are required for you to use DMDirc.  You can either " +
            "download a UI plugin or extract one from the jar. DMDirc will " +
            "now exit";

    /** Alternative Dialog heading text. */
    public static final String TITLE2 = "No compatible UIs Found";
    /** Alternative Dialog body text. */
    public static final String BODY2 = "DMDirc did not find any compatible UI plugins, " +
            "which are required for you to use DMDirc. The bundled UI plugins " +
            "have automatically been extracted from the jar, and your UI has been " +
            "reset to the default swing UI. DMDirc will now attempt to restart. " +
            "If you are not using the launcher you will need to restart DMDirc " +
            "manually.";

    /** Another alternative Dialog body text! */
    public static final String BODY3 = "DMDirc did not find any compatible UI plugins, " +
            "which are required for you to use DMDirc.  The bundled UI plugins " +
            "were automatically extracted from the jar, but this did not fix " +
            "the problem. DMDirc is unable to continue and will now exit.";

    /** Create a new NoUIDialog */
    public NoUIDialog() {
        this(TITLE, BODY);
    }

    /**
     * Create a new NoUIDialog
     *
     * @param title Title of dialog
     * @param body Body of dialog
     */
    public NoUIDialog(final String title, final String body) {
        super((JFrame) null, "DMDirc: "+title);
        setResizable(false);
        setLayout(new BorderLayout(5, 5));
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        final JButton button = new JButton("OK");
        button.addActionListener(new ActionListener() {

            /** {@inheritDoc} */
            @Override
            public void actionPerformed(final ActionEvent e) {
                dispose();
            }
        });

        final JTextPane textArea = new JTextPane(new HTMLDocument());
        textArea.setEditorKit(new HTMLEditorKit());
        textArea.setOpaque(false);
        textArea.setEditable(false);
        textArea.setHighlighter(null);
        textArea.setMargin(new Insets(5, 5, 5, 5));

        final HTMLDocument doc = (HTMLDocument) textArea.getDocument();
        final Font font = UIManager.getFont("Label.font");
        doc.getStyleSheet().addRule("body " +
                "{ font-family: " + font.getFamily() + "; " + "font-size: " +
                font.getSize() + "pt; text-align: center; }");
        doc.getStyleSheet().addRule("h1 " +
                "{ font-family: " + font.getFamily() + "; " +
                "font-size: 1.5em; padding: 0; margin: 0}");
        doc.getStyleSheet().addRule("p { text-align: justify; }");

        textArea.setText("<h1>" + title + "</h1><p>" + body + "</p>");

        add(textArea, BorderLayout.CENTER);
        add(button, BorderLayout.SOUTH);

        setPreferredSize(new Dimension(250, 255));

    }

    /**
     * Static method to instantiate and display the dialog.
     */
    public void display() {
        SwingUtilities.invokeLater(new Runnable() {

            /** {@inheritDoc} */
            @Override
            public void run() {
                pack();
                CoreUIUtils.centreWindow(NoUIDialog.this);
                setVisible(true);
            }
        });
    }

    /**
     * Static method to instantiate and display the dialog, blocking until it
     * is closed.
     */
    public void displayBlocking() {
        final Semaphore semaphore = new Semaphore(0);
        SwingUtilities.invokeLater(new Runnable() {

            /** {@inheritDoc} */
            @Override
            public void run() {
                addWindowListener(new WindowAdapter() {

                    @Override
                    public void windowClosed(final WindowEvent e) {
                        semaphore.release();
                    }

                });
                pack();
                CoreUIUtils.centreWindow(NoUIDialog.this);
                setVisible(true);
            }
        });
        semaphore.acquireUninterruptibly();
    }
}
