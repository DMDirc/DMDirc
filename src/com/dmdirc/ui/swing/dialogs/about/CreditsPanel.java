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

package com.dmdirc.ui.swing.dialogs.about;

import com.dmdirc.BrowserLauncher;
import static com.dmdirc.ui.swing.UIUtilities.SMALL_BORDER;

import java.awt.BorderLayout;

import javax.swing.BorderFactory;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkEvent.EventType;
import javax.swing.event.HyperlinkListener;

/**
 * Authors Panel.
 */
public final class CreditsPanel extends JPanel implements HyperlinkListener {
    
    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 1;
    
    /** Creates a new instance of CreditsPanel. */
    public CreditsPanel() {
        super();
        
        initComponents();
    }
    
    /** Initialises the components. */
    private void initComponents() {
        final JEditorPane about = new JEditorPane();
        
        about.setContentType("text/html");
        about.setText("<html>"
                + "<div style='font-family: "
                + UIManager.getFont("TextField.font").getFamily() + "; font-size:"
                + UIManager.getFont("TextField.font").getSize() + "pt;'>"
                + "<h3 style='margin: 3px; padding: 0px 0px 5px 0px;'>Main developers:</h1>"
                + "<ul style='list-style-type: circle; margin-top: 0px;'>"
                + "<li><a href=\"http://www.md87.co.uk\">Chris 'MD87' Smith</a></li>"
                + "<li><a href=\"http://www.greboid.com\">Gregory 'Greboid' Holmes</a></li>"
                + "<li><a href=\"http://home.dataforce.org.uk\">Shane 'Dataforce' Mc Cormack</a></li>"
                + "</ul>"
                + "<h3 style='margin: 3px; padding: 0px 0px 5px 0px;'>Testers:</h1>"
                + "<ul style='list-style-type: circle; margin-top: 0px;'>"
                + "<li><a href=\"http://www.pling.org.uk\">Chris 'laser' Northwood</a></li>"
                + "<li><a href=\"http://www.zipplet.co.uk\">Michael 'Zipplet' Nixon</a></li>"
                + "</ul>"
                + "</div></html>");
        about.setEditable(false);
        about.setHighlighter(null);
        about.setBackground(this.getBackground());
        about.setBorder(BorderFactory.createEmptyBorder(SMALL_BORDER,
                SMALL_BORDER, SMALL_BORDER, SMALL_BORDER));
        about.addHyperlinkListener(this);
        
        setLayout(new BorderLayout());
        
        add(about, BorderLayout.CENTER);
    }
    
    /** {@inheritDoc} */
    public void hyperlinkUpdate(final HyperlinkEvent e) {
        if (e.getEventType() == EventType.ACTIVATED) {
            BrowserLauncher.openURL(e.getURL());
        }
    }
    
}
