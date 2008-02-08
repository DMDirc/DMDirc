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

package com.dmdirc.ui.swing.components;

import javax.swing.JComponent;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import net.miginfocom.swing.MigLayout;

/**
 * Duration editor component.
 */
public class DurationEditor extends JComponent implements ChangeListener {

    private long duration;
    private JSpinner yearsSpinner;
    private JSpinner monthsSpinner;
    private JSpinner daysSpinner;
    private JSpinner hoursSpinner;
    private JSpinner minutesSpinner;
    private JSpinner secondsSpinner;

    

    public DurationEditor() {
        this(0);
    }

    public DurationEditor(final long duration) {
        this.duration = duration;
        
        initComponents();
        addListeners();
        layoutComponents();
    }
    
    private void initComponents() {
        daysSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 31, 1));
        hoursSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 24, 1));
        minutesSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 60, 1));
        secondsSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 60, 1));
    }
    
    private void addListeners() {
        daysSpinner.addChangeListener(this);
        hoursSpinner.addChangeListener(this);
        minutesSpinner.addChangeListener(this);
        secondsSpinner.addChangeListener(this);
    }
    
    private void layoutComponents() {
        setLayout(new MigLayout());
    }

    public void stateChanged(final ChangeEvent e) {
        if (e.getSource() == hoursSpinner) {
            if ((Integer) hoursSpinner.getValue() >= 24) {
                daysSpinner.setVisible(true);
            } else {
                daysSpinner.setVisible(false);
            }
        }
    }
}
