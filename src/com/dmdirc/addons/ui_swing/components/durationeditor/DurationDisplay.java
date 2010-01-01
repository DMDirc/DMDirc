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

package com.dmdirc.addons.ui_swing.components.durationeditor;

import com.dmdirc.ui.messages.Formatter;
import com.dmdirc.addons.ui_swing.UIUtilities;
import com.dmdirc.util.ListenerList;

import java.awt.Insets;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import net.miginfocom.swing.MigLayout;

/**
 * Duration display and edit component.
 */
public class DurationDisplay extends JPanel implements ActionListener,
        DurationListener {

    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 1;
    /** Current duration. */
    private int duration;
    /** Duration label. */
    private JLabel durationLabel;
    /** Edit button. */
    private JButton button;
    /** Listener list. */
    private final ListenerList listeners;
    /** Parent window. */
    private Window window;

    /**
     * Initialises a new duration display of 0 milliseconds.
     */
    public DurationDisplay() {
        this(0);
    }
    
    /**
     * Instantiates a new duration display.
     * 
     * @param window Parent window.
     * 
     * @since 0.6
     */
    public DurationDisplay(final Window window) {
        this(window, 0);
    }
    
    /**
     * Instantiates a new duration display.
     * 
     * @param duration Starting duration
     */
    public DurationDisplay(final long duration) {
        this(null, duration);
    }

    /**
     * Initialises a new duration display showing the specified millisecond duration.
     * 
     * @param window Parent window.
     * @param duration Duration to display in milliseconds
     * 
     * @since 0.6
     */
    public DurationDisplay(final Window window, final long duration) {
        this.window = window;
        this.duration = Long.valueOf(duration / 1000).intValue();
        listeners = new ListenerList();

        initComponents();
        addListeners();
        layoutComponents();
    }

    /**
     * Initliases and lays out the components.
     */
    private void initComponents() {
        button = new JButton("Edit");
        durationLabel = new JLabel();
        if (duration == 0) {
            durationLabel.setText("0 Seconds");
        } else {
            durationLabel.setText(Formatter.formatDuration(duration));
        }

        if (UIUtilities.isWindowsUI()) {
            button.setMargin(new Insets(2, 4, 2, 4));
        } else {
            button.setMargin(new Insets(0, 2, 0, 2));
        }
    }

    /**
     * Adds listeners to the components.
     */
    private void addListeners() {
        button.addActionListener(this);
    }

    /**
     * Lays out the components.
     */
    private void layoutComponents() {
        setLayout(new MigLayout("ins 0, fill"));

        add(durationLabel, "growx, pushx");
        add(button, "");
    }

    /** 
     * {@inheritDoc}
     * 
     * @param e Action event
     */
    @Override
    public void actionPerformed(final ActionEvent e) {
        DurationEditor editor = new DurationEditor(window, duration);
        editor.display(this);
        editor.addDurationListener(this);
    }

    /** 
     * {@inheritDoc}
     */
    @Override
    public void durationUpdated(final int newDuration) {
        duration = newDuration;
        if (duration == 0) {
            durationLabel.setText("0 Seconds");
        } else {
            durationLabel.setText(Formatter.formatDuration(duration));
        }
        fireDurationListener(newDuration * 1000);
    }

    /**
     * Returns the duration of this display in milliseconds.
     * 
     * @return Displayed duration in milliseconds
     */
    public long getDuration() {
        return duration * 1000;
    }

    /**
     * Adds a DurationListener to the listener list.
     *
     * @param listener Listener to add
     */
    public void addDurationListener(final DurationListener listener) {
        synchronized (listeners) {
            if (listener == null) {
                return;
            }
            listeners.add(DurationListener.class, listener);
        }
    }

    /**
     * Removes a DurationListener from the listener list.
     *
     * @param listener Listener to remove
     */
    public void removeDurationListener(final DurationListener listener) {
        listeners.remove(DurationListener.class, listener);
    }

    /**
     * Fires the duration updated method on all listeners.
     * 
     * @param newDuration New duration
     */
    protected void fireDurationListener(final int newDuration) {
        for (DurationListener listener : listeners.get(DurationListener.class)) {
            listener.durationUpdated(newDuration);
        }
    }
    
    /**
     * Sets the Parent window.
     * 
     * @param window Parent window
     */
    public void setWindow(final Window window) {
        this.window = window;
    }
}
