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
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.text.BadLocationException;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;

/**
 * Simple Dialog to inform the user there are no UI plugins.
 */
public class NoUIDialog extends JDialog {

    private static final long serialVersionUID = -528603916540455178L;
    private static final String TITLE = "<h1>No UIs Found</h1>";
    private static final String BODY = "DMDirc cannot find any UI plugins, " +
            "a UI plugin is required for you to use DMDirc.  You can either " +
            "download a UI plugin or extract one from the jar. DMDirc will " +
            "now exit";

    private NoUIDialog() {
        super((JFrame) null, "DMDirc: No UIs Found");
        setResizable(false);
        setLayout(new BorderLayout(5, 5));
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        final JButton button = new JButton("OK");
        button.addActionListener(new ActionListener() {

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

        textArea.setText(TITLE + "<p>" + BODY + "</p>");

        add(textArea, BorderLayout.CENTER);
        add(button, BorderLayout.SOUTH);

        setPreferredSize(new Dimension(250, 200));
    }

    /**
     * Static method to instantiate a display the dialog.
     */
    public static void display() {
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                final NoUIDialog me = new NoUIDialog();
                me.pack();
                me.setVisible(true);
            }
        });
    }
}
