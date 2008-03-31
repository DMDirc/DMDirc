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
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Locale;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

import net.miginfocom.swing.MigLayout;

/**
 * Info panel.
 */
public final class InfoPanel extends JPanel implements ActionListener {
    
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
     * Returns the systems java version
     * 
     * @return Java version string
     */
    private String getJavaVersion() {
        return System.getProperty("java.vm.name", "unknown") + " " 
                + System.getProperty("java.vm.version", "unknown") + " ["
                + System.getProperty("java.vm.vendor", "uknown") + "]";
    }

    /**
     * Returns the systems OS version
     * 
     * @return OS version string
     */
    private String getOSVersion() {
        return System.getProperty("os.name", "unknown") + " " 
                + System.getProperty("os.version", "unknown") + " "
                + System.getProperty("os.arch", "unknown") + "; "
                + System.getProperty("file.encoding", "unknown") + "; "
                + Locale.getDefault().toString();
    }
    
    /** Initialises the components. */
    private void initComponents() {
        final JButton copy;
        final JPanel info;
        final JScrollPane scrollPane;
        
        copy = new JButton("Copy to clipboard");
        copy.addActionListener(this);
        
        info = new JPanel(new MigLayout("fillx, wrap 2"));
        info.add(new JLabel("DMDirc version: "));
        info.add(new JLabel(Main.VERSION + " (" + Main.SVN_REVISION + "; "
                + Main.UPDATE_CHANNEL + ")"), "growx, pushx");
        info.add(new JLabel("Profile directory: "));
        info.add(new JLabel(Main.getConfigDir()), "growx, pushx");
        info.add(new JLabel("Java version: "));
        info.add(new JLabel(getJavaVersion()), "growx, pushx");
        info.add(new JLabel("OS Version: "));
        info.add(new JLabel(getOSVersion()), "growx, pushx");
        
        scrollPane = new JScrollPane(info);
        SwingUtilities.invokeLater(new Runnable() {
            /** {@inheritDoc} */
            @Override
            public void run() {
                scrollPane.getVerticalScrollBar().setValue(0);
            }
        }
        );
        
        setLayout(new MigLayout("ins rel, fill"));
        add(scrollPane, "grow, wrap");
        add(copy, "right");
    }

    /** 
     * {@inheritDoc}
     * 
     * @param e action event
     */
    @Override
    public void actionPerformed(final ActionEvent e) {
        final StringBuilder b = new StringBuilder();
        b.append("DMDirc version: ");
        b.append(Main.VERSION);
        b.append(System.getProperty("line.separator", "\n"));
        b.append("Profile directory: ");
        b.append(Main.getConfigDir());
        b.append(System.getProperty("line.separator", "\n"));
        b.append("Java version: ");
        b.append(getJavaVersion());
        b.append(System.getProperty("line.separator", "\n"));
        b.append("OS Version: ");
        b.append(getOSVersion());
        
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(
                new StringSelection(b.toString()), null);
    }
}
