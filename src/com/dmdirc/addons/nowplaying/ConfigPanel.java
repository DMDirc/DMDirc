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

package com.dmdirc.addons.nowplaying;

import com.dmdirc.config.prefs.PreferencesInterface;
import com.dmdirc.ui.swing.components.reorderablelist.ReorderableJList;
import static com.dmdirc.ui.swing.UIUtilities.SMALL_BORDER;

import java.awt.BorderLayout;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

/**
 * Now playing plugin config panel.
 */
public class ConfigPanel extends JPanel implements PreferencesInterface {
    
    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 1;
    
    /** Media source order list. */
    private ReorderableJList list;
    
    /** Media sources. */
    private final List<String> sources;
    
    private final NowPlayingPlugin plugin;
    
    /** Creates a new instance of ConfigPanel. */
    public ConfigPanel(final NowPlayingPlugin plugin, final List<String> sources) {
        super();
        
        this.sources = new LinkedList<String>(sources);
        this.plugin = plugin;
        
        initComponents();
    }
    
    /** Initialises the components. */
    private void initComponents() {
        list = new ReorderableJList();
        
        for (String source : sources) {
            list.getModel().addElement(source);
        }
        
        setLayout(new BorderLayout(SMALL_BORDER, SMALL_BORDER));
        
        add(new JLabel("Media source order: "), BorderLayout.PAGE_START);
        add(new JScrollPane(list), BorderLayout.CENTER);
    }
    
    public List<String> getSources() {
        final List<String> newSources = new LinkedList<String>();
        
        final Enumeration<?> values = list.getModel().elements();
        
        while (values.hasMoreElements()) {
            newSources.add((String) values.nextElement());
        }
        
        return newSources;
    }

    /** {@inheritDoc} */
    @Override
    public void save() {
        plugin.saveSettings(getSources());
    }
}
