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

package com.dmdirc.addons.addonbrowser;

import com.dmdirc.addons.ui_swing.components.text.TextLabel;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.ListCellRenderer;
import net.miginfocom.swing.MigLayout;

/**
 *
 */
public class AddonInfoListCellRenderer extends JPanel implements ListCellRenderer {

    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 1;
    
    /** {@inheritDoc} */
    @Override
    public Component getListCellRendererComponent(JList list, Object value,
            int index, boolean isSelected, boolean cellHasFocus) {
        final AddonInfo info = (AddonInfo) value;
        removeAll();
        setLayout(new MigLayout("fillx, ins 0"));
        setBackground(index % 2 == 1 ? new Color(0xEE, 0xEE, 0xFF) : Color.WHITE);
        
        JLabel title = new JLabel(info.getTitle());
        title.setFont(title.getFont().deriveFont(16f).deriveFont(Font.BOLD));
        add(title, "wmin 165, wmax 165, gaptop 5, gapleft 5");
        
        title = new JLabel(info.getScreenshot());
        title.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        add(title, "wmin 150, wmax 150, height 150::, wrap, spany 4, gaptop 5, gapright 5");

        title = new JLabel(info.getType().toString() + ", rated "
                + info.getRating() + "/10");
        add(title, "gapleft 5, wrap");
        
        TextLabel label = new TextLabel(info.getDescription());
        add(label, "wmax 100%-170, hmax 150, growy, wrap, gapleft 5, gapbottom 5, pushy");
        
        final JButton button = new JButton("Install");
        button.addActionListener(new InstallListener(info));
        final boolean installed = info.isInstalled();
        
        if (installed || !info.isDownloadable()) {
            button.setEnabled(false);
        }
        
        add(button, "gapleft 5, split");
        
        if (installed || !info.isDownloadable()) {
            title = new JLabel(installed ? "Already installed" : "No download available");
            title.setForeground(Color.GRAY);
            add(title);
        }
        
        add(new JSeparator(), "newline, span, growx, pushx, gaptop 5");
        
        return this;
    }
}
