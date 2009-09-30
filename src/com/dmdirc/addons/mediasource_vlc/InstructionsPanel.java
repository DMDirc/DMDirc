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

package com.dmdirc.addons.mediasource_vlc;

import com.dmdirc.config.prefs.PreferencesInterface;
import com.dmdirc.addons.ui_swing.components.text.TextLabel;

import javax.swing.JPanel;

import net.miginfocom.swing.MigLayout;

/**
 * Shows installation instructions for the VLC media source.
 * 
 * @author chris
 */
class InstructionsPanel extends JPanel implements PreferencesInterface {
    
    private static final long serialVersionUID = 1;

    public InstructionsPanel() {
        setLayout(new MigLayout());
        final TextLabel instructions = new TextLabel("<html><p>"                
                + "The VLC media source requires that VLC's web interface is" +
                " enabled. To do this, follow the steps below:</p>"
                + "<ol style='margin-left: 20px; padding-left: 0px;'>" +
                "<li>Open VLC's preferences dialog (found in the 'Tools' menu)" +
                "<li>Set the 'Show settings' option to 'All'" +
                "<li>Expand the 'Interface' category by clicking on the plus sign next to it" +
                "<li>Select the 'Main interfaces' category" +
                "<li>Check the box next to 'HTTP remote control interface'" +
                "<li>Expand the 'Main interfaces' category" +
                "<li>Select the 'HTTP' category" +
                "<li>In the 'Host address' field, enter 'localhost:8082'" +
                "<li>In the 'Source directory' field enter the path to VLC's" +
                " http directory<ul style='margin-left: 5px; padding-left: 0px;" +
                "list-style-type: none;'>" +
                "<li style='padding-bottom: 5px'>For Linux users this may be /usr/share/vlc/http/" +
                "<li>For Windows users this will be under the main VLC directory, e.g. " +
                "C:\\Program Files\\VLC\\http</ul><li>Click 'Save'<li>Restart VLC</ol></html>");
        add(instructions);
    }

    /** {@inheritDpc} */
    @Override
    public void save() {
        // Do nothing
    }

}
