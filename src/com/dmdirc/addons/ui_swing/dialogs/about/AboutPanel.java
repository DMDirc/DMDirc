/*
 * Copyright (c) 2006-2010 Chris Smith, Shane Mc Cormack, Gregory Holmes
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
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkEvent.EventType;
import javax.swing.event.HyperlinkListener;

import net.miginfocom.swing.MigLayout;

/**
 * About DMDirc panel.
 */
public final class AboutPanel extends JPanel implements HyperlinkListener {
    
    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 1;
    
    /** Creates a new instance of AboutPanel. */
    public AboutPanel() {
        super();
        
        this.setOpaque(UIUtilities.getTabbedPaneOpaque());
        initComponents();
    }
    
    /** Initialises the components. */
    private void initComponents() {
        final HTMLLabel about = new HTMLLabel("<html><center>"
                + "<h1 style=\"margin-bottom: 0px;\">DMDirc</h1>"
                + "<span style=\"font-style: italic;\">The intelligent IRC client.</span>"
                + "<p>Easy to use, cross-platform IRC client.</p>"
                + "<p><a href=\"http://www.dmdirc.com\">www.dmdirc.com</a></p>"
                + "</center></html>");
        about.addHyperlinkListener(this);
        
        setLayout(new MigLayout("ins rel, fill"));
        
        add(about, "align center");
    }
    
    /** {@inheritDoc} */
    @Override
    public void hyperlinkUpdate(final HyperlinkEvent e) {
        if (e.getEventType() == EventType.ACTIVATED) {
            URLHandler.getURLHander().launchApp(e.getURL());
        }
    }
    
}
