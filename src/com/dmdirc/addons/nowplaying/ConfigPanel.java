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

package com.dmdirc.addons.nowplaying;

import com.dmdirc.config.IdentityManager;
import com.dmdirc.config.prefs.PreferencesInterface;
import com.dmdirc.addons.ui_swing.components.text.TextLabel;
import com.dmdirc.addons.ui_swing.components.reorderablelist.ReorderableJList;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import net.miginfocom.swing.MigLayout;

/**
 * Now playing plugin config panel.
 */
public class ConfigPanel extends JPanel implements PreferencesInterface,
        KeyListener {

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
    /** The plugin that owns this panel. */
    private final NowPlayingPlugin plugin;
    /** Text field for our setting. */
    private JTextField textfield;
    /** Panel that the preview is in. */
    private JPanel previewPanel;
    /** Label for previews. */
    private TextLabel preview;
    /** Update timer. */
    private Timer updateTimer;

    /**
     * Creates a new instance of ConfigPanel.
     *
     * @param plugin The plugin that owns this panel
     * @param sources A list of sources to be used in the panel
     */
    public ConfigPanel(final NowPlayingPlugin plugin, final List<String> sources) {
        super();

        if (sources == null) {
            this.sources = new LinkedList<String>();
        } else {
            this.sources = new LinkedList<String>(sources);
        }
        this.plugin = plugin;

        initComponents();
    }

    /**
     * Initialises the components.
     */
    private void initComponents() {
        list = new ReorderableJList();

        for (String source : sources) {
            list.getModel().addElement(source);
        }

        textfield = new JTextField(IdentityManager.getGlobalConfig().getOption(
                plugin.getDomain(), "format"));
        textfield.addKeyListener(this);
        preview = new TextLabel("Preview:\n");

        setLayout(new MigLayout("fillx, ins 0"));

        JPanel panel = new JPanel();

        panel.setBorder(BorderFactory.createTitledBorder(UIManager.getBorder(
                "TitledBorder.border"), "Source order"));
        panel.setLayout(new MigLayout("fillx, ins 5"));

        panel.add(new JLabel("Drag and drop items to reorder"), "wrap");
        panel.add(new JScrollPane(list), "growx");

        add(panel, "growx, wrap");

        panel = new JPanel();

        panel.setBorder(BorderFactory.createTitledBorder(UIManager.getBorder(
                "TitledBorder.border"), "Output format"));
        panel.setLayout(new MigLayout("fillx, ins 5"));

        panel.add(textfield, "span, growx, wrap");
        panel.add(preview, "span, grow, wrap, gaptop 10");
        add(panel, "growx, wrap");

        previewPanel = panel;

        add(new NowPlayingSubsitutionPanel(Arrays.asList(new String[]{"app",
                    "title", "artist", "album", "bitrate", "format", "length",
                    "time",
                    "state"})), "growx");
        schedulePreviewUpdate();
    }

    /**
     * Updates the preview text.
     */
    private void updatePreview() {
        updateTimer.cancel();

        MediaSource source = plugin.getBestSource();

        if (source == null) {
            source = new DummyMediaSource();
        }

        preview.setText("Preview:\n" + plugin.doSubstitution(textfield.getText(),
                source));
        preview.repaint();

        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                previewPanel.revalidate();
                revalidate();
            }
        });
    }

    /**
     * Retrieves the (new) source order from this config panel.
     *
     * @return An ordered list of sources
     */
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
        IdentityManager.getConfigIdentity().setOption(plugin.getDomain(),
                "format", textfield.getText());
    }

    /**
     * {@inheritDoc}
     *
     * @param e Key event action
     */
    @Override
    public void keyTyped(final KeyEvent e) {
        // Do nothing
    }

    /**
     * {@inheritDoc}
     *
     * @param e Key event action
     */
    @Override
    public void keyPressed(final KeyEvent e) {
        // Do nothing
    }

    /**
     * {@inheritDoc}
     *
     * @param e Key event action
     */
    @Override
    public void keyReleased(final KeyEvent e) {
        schedulePreviewUpdate();
    }

    /**
     * Schedules an update to the preview text.
     */
    private void schedulePreviewUpdate() {
        if (updateTimer != null) {
            updateTimer.cancel();
        }

        updateTimer = new Timer("Nowplaying config timer");
        updateTimer.schedule(new TimerTask() {

            /** {@inheritDoc} */
            @Override
            public void run() {
                updatePreview();
            }
        }, 500);
    }

    /**
     * A dummy media source for use in previews.
     */
    private class DummyMediaSource implements MediaSource {

        /** {@inheritDoc} */
        @Override
        public MediaSourceState getState() {
            return MediaSourceState.PLAYING;
        }

        /** {@inheritDoc} */
        @Override
        public String getAppName() {
            return "MyProgram";
        }

        /** {@inheritDoc} */
        @Override
        public String getArtist() {
            return "The Artist";
        }

        /** {@inheritDoc} */
        @Override
        public String getTitle() {
            return "Song about nothing";
        }

        /** {@inheritDoc} */
        @Override
        public String getAlbum() {
            return "Album 45";
        }

        /** {@inheritDoc} */
        @Override
        public String getLength() {
            return "3:45";
        }

        /** {@inheritDoc} */
        @Override
        public String getTime() {
            return "1:20";
        }

        /** {@inheritDoc} */
        @Override
        public String getFormat() {
            return "flac";
        }

        /** {@inheritDoc} */
        @Override
        public String getBitrate() {
            return "128";
        }
    }

}
