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

package com.dmdirc.ui.swing.dialogs.actioneditor;

import com.dmdirc.actions.ActionCondition;
import com.dmdirc.actions.CoreActionComparison;
import com.dmdirc.actions.CoreActionComponent;
import com.dmdirc.actions.CoreActionType;
import com.dmdirc.ui.swing.UIUtilities;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import net.miginfocom.swing.MigLayout;

/**
 * Action conditioneditor panel.
 */
public class ActionConditionEditorPanel extends JPanel implements ActionListener, DocumentListener {

    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 1;
    /** Argument. */
    private JComboBox argument;
    /** Component. */
    private JComboBox component;
    /** Comparison. */
    private JComboBox comparison;
    /** Target. */
    private JTextField target;

    /** Instantiates the panel. */
    public ActionConditionEditorPanel() {
        super();
        
        initComponents();
        addListeners();
        layoutComponents();
    }

    /** Initialises the components. */
    private void initComponents() {
        argument = new JComboBox(new DefaultComboBoxModel());
        argument.putClientProperty("JComboBox.isTableCellEditor", Boolean.TRUE);
        component = new JComboBox(new DefaultComboBoxModel());
        component.putClientProperty("JComboBox.isTableCellEditor", Boolean.TRUE);
        comparison = new JComboBox(new DefaultComboBoxModel());
        comparison.putClientProperty("JComboBox.isTableCellEditor", Boolean.TRUE);
        target = new JTextField();
    }

    /** Adds the listeners. */
    private void addListeners() {
        argument.addActionListener(this);
        component.addActionListener(this);
        comparison.addActionListener(this);
        target.getDocument().addDocumentListener(this);
    }

    /** Lays out the components. */
    private void layoutComponents() {
        setLayout(new MigLayout("wrap 2"));
        
        add(new JLabel("Argument:"), "align right");
        add(argument, "growx");
        add(new JLabel("Component:"), "align right");
        add(component, "growx");
        add(new JLabel("Comparison:"), "align right");
        add(comparison, "growx");
        add(new JLabel("Target:"), "align right");
        add(target, "growx");
    }

    /** 
     * {@inheritDoc} 
     * 
     * @param e Action event
     */
    @Override
    public void actionPerformed(final ActionEvent e) {
        //Ignore
    }

    /** {@inheritDoc} */
    @Override
    public void insertUpdate(final DocumentEvent e) {
        //Ignore
    }

    /** {@inheritDoc} */
    @Override
    public void removeUpdate(final DocumentEvent e) {
        //Ignore
    }

    /** {@inheritDoc} */
    @Override
    public void changedUpdate(final DocumentEvent e) {
        //Ignore
    }
    
    /**
     * This is a test method.
     * 
     * @param args CLI Params
     * 
     * @throws java.lang.InterruptedException so i can pause the thread.
     */
    public static void main(final String[] args) throws InterruptedException {
        UIUtilities.initUISettings();
        final List<ActionConditionDisplayPanel> conditions =
                new ArrayList<ActionConditionDisplayPanel>();
        conditions.add(new ActionConditionDisplayPanel(new ActionCondition(0,
                CoreActionComponent.USER_NAME,
                CoreActionComparison.STRING_EQUALS, "greboid"),
                CoreActionType.CHANNEL_JOIN));
        conditions.add(new ActionConditionDisplayPanel(new ActionCondition(0,
                CoreActionComponent.USER_NAME,
                CoreActionComparison.STRING_EQUALS, "greboid1"),
                CoreActionType.CHANNEL_JOIN));

        final JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        final ActionConditionsListPanel panel = new ActionConditionsListPanel(CoreActionType.CHANNEL_JOIN,
                conditions);
        frame.add(panel);

        frame.pack();
        frame.setVisible(true);

        Thread.sleep(1000);

        panel.addCondition(new ActionCondition(-1, null, null, null));
    }
}
