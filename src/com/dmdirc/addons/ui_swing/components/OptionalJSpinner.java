/*
 * 
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

package com.dmdirc.addons.ui_swing.components;

import com.dmdirc.util.ListenerList;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import net.miginfocom.swing.MigLayout;

/**
 * Optional JSpinner component.  Composite JSpinner, JCheckbox component.
 */
public class OptionalJSpinner extends JPanel implements ActionListener,
        ChangeListener {

    private static final long serialVersionUID = -2867331420063503447L;
    private final JSpinner spinner;
    private final JCheckBox checkbox;
    private final ListenerList listeners;

    /**
     * Creates a new optional JSpinner with a default number model.
     */
    public OptionalJSpinner() {
        this(new SpinnerNumberModel());
    }

    /**
     * Creates a new optional JSpinner with a default number model.
     *
     * @param enabled Initial selected state
     */
    public OptionalJSpinner(final boolean enabled) {
        this(new SpinnerNumberModel(), enabled);
    }

    /**
     * Creates a new optional SPinner with a default number model.
     *
     * @param model Model to show
     */
    public OptionalJSpinner(final SpinnerModel model) {
        this(model, true);
    }

    /**
     * Creates a new optional SPinner with a default number model.
     *
     * @param model Model to show
     * @param enabled Initial selected state
     */
    public OptionalJSpinner(final SpinnerModel model, final boolean enabled) {
        checkbox = new JCheckBox("", enabled);
        spinner = new JSpinner(model);
        listeners = new ListenerList();

        spinner.setEnabled(enabled);

        spinner.addChangeListener(this);
        checkbox.addActionListener(this);

        setLayout(new MigLayout("fill"));

        add(checkbox, "");
        add(spinner, "growx, pushx");
    }

    /**
     * Returns the state of the button. True if the toggle button is selected,
     * false if it's not.
     *
     * @return true if the toggle button is selected, otherwise false
     */
    public boolean isSelected() {
        return checkbox.isSelected();
    }

    /**
     * Sets the state of the button.
     *
     * @param selected true if the button is selected, otherwise false
     */
    public void setSelected(final boolean selected) {
        checkbox.setSelected(selected);
    }

    /**
     * Returns the current value of the model, typically this value is
     * displayed by the editor. If the user has changed the value displayed
     * by the editor it is possible for the model's value to differ from that
     * of the editor, refer to the class level javadoc for examples of how to
     * deal with this.
     *
     * This method simply delegates to the model. It is equivalent to:
     * 
     * getModel().getValue()
     *
     * @return The current value
     */
    public Object getValue() {
        return spinner.getValue();
    }

    /**
     * Changes current value of the model, typically this value is displayed
     * by the editor. If the SpinnerModel implementation doesn't support
     * the specified value then an IllegalArgumentException is thrown.
     * This method simply delegates to the model. It is equivalent to:
     *
     * getModel().setValue(value)
     *
     * @param value Value to set
     */
    public void setValue(final Object value) {
        spinner.setValue(value);
    }

    /**
     * Returns the SpinnerModel that defines this spinners sequence of values.
     *
     * @return the value of the model property
     */
    public SpinnerModel getModel() {
        return spinner.getModel();
    }

    /**
     * Changes the model that represents the value of this spinner. If the
     * editor property has not been explicitly set, the editor property
     * is (implicitly) set after the "model" PropertyChangeEvent has been
     * fired. The editor property is set to the value returned by createEditor,
     * as in:
     *
     * setEditor(createEditor(model));
     *
     * @param model the new SpinnerModel
     */
    public void setModel(final SpinnerModel model) {
        spinner.setModel(model);
    }

    /**
     * Adds a change listener to this optional spinner.
     *
     * @param listener Listener to add
     */
    public void addChangeListener(final ChangeListener listener) {
        synchronized (listeners) {
            listeners.add(ChangeListener.class, listener);
        }
    }

    /**
     * Removes a change listener from this optional spinner.
     *
     * @param listener Listener to remove
     */
    public void removeChangeListener(final ChangeListener listener) {
        synchronized (listeners) {
            listeners.remove(ChangeListener.class, listener);
        }
    }

    private void fireChangeListener() {
        for (ChangeListener listener : listeners.get(ChangeListener.class)) {
            listener.stateChanged(new ChangeEvent(this));
        }
    }

    /** {@inheritDoc} */
    @Override
    public void stateChanged(final ChangeEvent e) {
        fireChangeListener();
    }

    /**
     * {@inheritDoc}
     *
     * @param e Action event
     */
    @Override
    public void actionPerformed(final ActionEvent e) {
        fireChangeListener();
        spinner.setEnabled(checkbox.isSelected());
    }
}
