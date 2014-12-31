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

package com.dmdirc.ui;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.concurrent.Semaphore;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;

/**
 * Simple Dialog to inform the user there are no UI plugins.
 */
public class WarningDialog extends JDialog {

    /** Dialog heading text. */
    public static final String NO_UIS_TITLE = "No UIs Found";
    /** Dialog body text. */
    public static final String NO_UIS_BODY = "DMDirc cannot find any UI "
            + "plugins, which are required for you to use DMDirc.  You can "
            + "either download a UI plugin or extract one from the jar. DMDirc "
            + "will now exit";
    /** Alternative Dialog heading text. */
    public static final String NO_COMPAT_UIS_TITLE = "No compatible UIs Found";
    /** Alternative Dialog body text. */
    public static final String NO_COMPAT_UIS_BODY = "DMDirc did not find any "
            + "compatible UI plugins, which are required for you to use "
            + "DMDirc. The bundled UI plugins have automatically been "
            + "extracted from the jar, and your UI has been reset to the "
            + "default swing UI. DMDirc will now attempt to restart. If you "
            + "are not using the launcher you will need to restart DMDirc "
            + "manually.";
    /** Another alternative Dialog body text! */
    public static final String NO_RECOV_UIS = "DMDirc did not find any "
            + "compatible UI plugins, which are required for you to use DMDirc."
            + "  The bundled UI plugins were automatically extracted from the "
            + "jar, but this did not fix the problem. DMDirc is unable to "
            + "continue and will now exit.";
    /** A version number for this class. */
    private static final long serialVersionUID = -528603916540455179L;

    /** Create a new NoUIDialog. */
    public WarningDialog() {
        this(NO_UIS_TITLE, NO_UIS_BODY);
    }

    /**
     * Create a new NoUIDialog.
     *
     * @param title Title of dialog
     * @param body  Body of dialog
     */
    public WarningDialog(final String title, final String body) {
        super((Window) null);
        setTitle("DMDirc: " + title);
        setIconImage(Toolkit.getDefaultToolkit().getImage(
                Thread.currentThread().getContextClassLoader()
                .getResource("com/dmdirc/res/logo.png")));
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setResizable(false);

        final JPanel panel = new JPanel(new BorderLayout(5, 5));

        final JButton button = new JButton("OK");
        button.addActionListener(e -> dispose());

        final JTextPane textArea = new JTextPane(new HTMLDocument());
        textArea.setEditorKit(new HTMLEditorKit());
        textArea.setOpaque(false);
        textArea.setEditable(false);
        textArea.setHighlighter(null);
        textArea.setMargin(new Insets(5, 5, 5, 5));

        final HTMLDocument doc = (HTMLDocument) textArea.getDocument();
        final Font font = UIManager.getFont("Label.font");
        doc.getStyleSheet().addRule("body "
                + "{ font-family: " + font.getFamily() + "; " + "font-size: "
                + font.getSize() + "pt; text-align: center; }");
        doc.getStyleSheet().addRule("h1 "
                + "{ font-family: " + font.getFamily() + "; "
                + "font-size: 1.5em; padding: 0; margin: 0}");
        doc.getStyleSheet().addRule("p { text-align: justify; }");

        textArea.setText("<h1>" + title + "</h1><p>" + body + "</p>");

        panel.add(textArea, BorderLayout.CENTER);
        panel.add(button, BorderLayout.SOUTH);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(5, 5, 5, 5),
                panel.getBorder()));
        add(panel);
    }

    /**
     * Static method to instantiate and display the dialog.
     */
    public void display() {
        SwingUtilities.invokeLater(() -> {
            setSize(400, 400);
            CoreUIUtils.centreWindow(this);
            setVisible(true);
        });
    }

    /**
     * Static method to instantiate and display the dialog, blocking until it is closed.
     */
    public void displayBlocking() {
        final Semaphore semaphore = new Semaphore(0);
        SwingUtilities.invokeLater(() -> {
            addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosed(final WindowEvent e) {
                    semaphore.release();
                }
            });
        });
        display();
        semaphore.acquireUninterruptibly();
    }

}
