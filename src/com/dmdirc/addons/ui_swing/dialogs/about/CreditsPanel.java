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

import com.dmdirc.addons.ui_swing.components.text.HTMLLabel;
import com.dmdirc.addons.ui_swing.UIUtilities;
import com.dmdirc.util.URLHandler;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkEvent.EventType;
import javax.swing.event.HyperlinkListener;

import net.miginfocom.swing.MigLayout;

/**
 * Authors Panel.
 */
public final class CreditsPanel extends JPanel implements HyperlinkListener {
    
    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 2;

    /** HTML label we're using. */
    private HTMLLabel about;
    
    /** Creates a new instance of CreditsPanel. */
    public CreditsPanel() {
        super();
        
        this.setOpaque(UIUtilities.getTabbedPaneOpaque());
        initComponents();
    }

    /** Shows some alternate content. */
    public void showEE() {
        about.setText("<html><center><br><br><br>"
                + "<img src=\"http://www.dmdirc.com/res/about.png\"></html>");
    }
    
    /** Initialises the components. */
    private void initComponents() {
        about = new HTMLLabel("<html>"
                + "<h3 style='margin: 3px; padding: 0px 0px 5px 0px;'>Main developers:</h1>"
                + "<ul style='list-style-type: circle; margin-top: 0px;'>"
                + "<li><a href=\"http://www.md87.co.uk\">Chris 'MD87' Smith</a></li>"
                + "<li><a href=\"http://www.greboid.com\">Gregory 'Greboid' Holmes</a></li>"
                + "<li><a href=\"http://home.dataforce.org.uk\">Shane 'Dataforce' Mc Cormack</a></li>"
                + "</ul>"
                + "<h3 style='margin: 3px; padding: 0px 0px 5px 0px;'>Additional developers:</h1>"
                + "<ul style='list-style-type: circle; margin-top: 0px;'>"
                + "<li><a href=\"http://www.rivernile.org.uk\">Niall 'Rivernile' Scott</a></li>"
                + "<li><a href=\"http://www.zipplet.co.uk\">Michael 'Zipplet' Nixon</a></li>"
                + "</ul>"
                + "<h3 style='margin: 3px; padding: 0px 0px 5px 0px;'>Testers:</h1>"
                + "<ul style='list-style-type: circle; margin-top: 0px;'>"
                + "<li><a href=\"http://www.pling.org.uk\">Chris 'laser' Northwood</a></li>"
                + "<li><a href=\"http://simonmott.co.uk/\">Simon 'Demented-Idiot' Mott</a></li>"
                + "</ul>"
                + "</html>");
        about.addHyperlinkListener(this);
        
        setLayout(new MigLayout("ins rel, fill"));
        final JScrollPane scrollPane = new JScrollPane(about);
        scrollPane.setOpaque(UIUtilities.getTabbedPaneOpaque());
        scrollPane.getViewport().setOpaque(UIUtilities.getTabbedPaneOpaque());
        add(scrollPane, "grow, push");
    }
    
    /** {@inheritDoc} */
    @Override
    public void hyperlinkUpdate(final HyperlinkEvent e) {
        if (e.getEventType() == EventType.ACTIVATED) {
            URLHandler.getURLHander().launchApp(e.getURL());
        }
    }
    
}
