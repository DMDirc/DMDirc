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

package uk.org.ownage.dmdirc.ui;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.Spring;
import javax.swing.SpringLayout;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;

/**
 * Allows the user to modify global client preferences.
 */
public class PreferencesDialog extends StandardDialog implements ActionListener {
    
    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 2;
    
    /** Size of the large borders in the dialog. */
    private static final int LARGE_BORDER = 10;
    
    /** Size of the small borders in the dialog. */
    private static final int SMALL_BORDER = 5;
    
    /**
     * Creates a new instance of PreferencesDialog.
     * @param parent The frame that owns this dialog
     * @param modal Whether to show modally or not
     */
    public PreferencesDialog(final Frame parent, final boolean modal) {
        super(parent, modal);
        
        initComponents();
    }
    
    /**
     * Initialises GUI components.
     */
    private void initComponents() {
        final JTabbedPane tabbedPane = new JTabbedPane();
        final GridBagConstraints constraints = new GridBagConstraints();
        final JButton button1 = new JButton();
        final JButton button2 = new JButton();
        
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        getContentPane().setLayout(new GridBagLayout());
        setTitle("Preferences");
        setResizable(true);
        
        
        button1.setPreferredSize(new Dimension(100, 25));
        button2.setPreferredSize(new Dimension(100, 25));
        
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.gridwidth = 3;
        constraints.fill = GridBagConstraints.BOTH;
        constraints.weightx = 1.0;
        constraints.weighty = 1.0;
        constraints.insets = new Insets(SMALL_BORDER, SMALL_BORDER,
                SMALL_BORDER, SMALL_BORDER);
        getContentPane().add(tabbedPane, constraints);
        
        constraints.weighty = 0.0;
        constraints.gridx = 0;
        constraints.gridy = 1;
        constraints.gridwidth = 1;
        getContentPane().add(Box.createHorizontalGlue(), constraints);
        
        constraints.weightx = 0.0;
        constraints.insets.set(0, LARGE_BORDER, LARGE_BORDER, LARGE_BORDER);
        constraints.gridx = 1;
        constraints.anchor = GridBagConstraints.EAST;
        constraints.fill = GridBagConstraints.NONE;
        getContentPane().add(button1, constraints);
        
        constraints.gridx = 2;
        getContentPane().add(button2, constraints);
        
        tabbedPane.setBorder(new EmptyBorder(LARGE_BORDER, LARGE_BORDER,
                LARGE_BORDER, LARGE_BORDER));
        
        orderButtons(button1, button2);
        
        initGeneralTab(tabbedPane);
        
        initUITab(tabbedPane);
        
        initInputTab(tabbedPane);
        
        initLoggingTab(tabbedPane);
        
        initIdentitiesTab(tabbedPane);
        
        initListeners();
        
        pack();
    }
    
    /**
     * Initialises the preferences tab.
     *
     * @param tabbedPane parent pane
     */
    private void initGeneralTab(final JTabbedPane tabbedPane) {
        final JPanel generalPanel = new JPanel(new GridBagLayout());
        final GridBagConstraints constraints = new GridBagConstraints();
        final JPanel messagesPanel = new JPanel(new SpringLayout());
        final JPanel commandsPanel = new JPanel(new SpringLayout());
        JLabel label;
        JTextField textField;
        
        generalPanel.setBorder(new EmptyBorder(LARGE_BORDER, LARGE_BORDER,
                LARGE_BORDER, LARGE_BORDER));
        
        tabbedPane.addTab("General", generalPanel);
        
        String[] messageLabels = {"Part message: ", "Quit message: ",
        "Cycle message: ", "Close message: ", };
        int numPairs = messageLabels.length;
        
        for (int i = 0; i < numPairs; i++) {
            label = new JLabel(messageLabels[i], JLabel.TRAILING);
            messagesPanel.add(label);
            textField = new JTextField(10);
            label.setLabelFor(textField);
            messagesPanel.add(textField);
        }
        
        layoutGrid(messagesPanel, numPairs, 2, SMALL_BORDER, SMALL_BORDER,
                LARGE_BORDER, LARGE_BORDER);
        
        constraints.fill = GridBagConstraints.BOTH;
        constraints.weightx = 1.0;
        constraints.weighty = 1.0;
        generalPanel.add(messagesPanel, constraints);
    }
    
    /**
     * Initialises the UI tab.
     *
     * @param tabbedPane parent pane
     */
    private void initUITab(final JTabbedPane tabbedPane) {
        final JPanel uiPanel = new JPanel();
        final GridBagConstraints constraints = new GridBagConstraints();
        final String[] windowOptions 
                = new String[] {"All", "Active", "Server", };
        JPanel panel;
        JLabel label;
        JTextField textField;
        JCheckBox checkBox;
        JComboBox comboBox;
        
        tabbedPane.addTab("GUI", uiPanel);
        
        uiPanel.setBorder(new EmptyBorder(LARGE_BORDER, LARGE_BORDER,
                LARGE_BORDER, LARGE_BORDER));
        
        panel = new JPanel(new SpringLayout());
        label = new JLabel("Show version: ", JLabel.TRAILING);
        panel.add(label);
        checkBox = new JCheckBox();
        label.setLabelFor(checkBox);
        panel.add(checkBox);
        
        label = new JLabel("Input buffer size: ", JLabel.TRAILING);
        panel.add(label);
        textField = new JTextField(3);
        label.setLabelFor(textField);
        panel.add(textField);
        
        label = new JLabel("Auto-maximise windows: ", JLabel.TRAILING);
        panel.add(label);
        checkBox = new JCheckBox();
        label.setLabelFor(checkBox);
        panel.add(checkBox);
        
        label = new JLabel("Window background colour: ", JLabel.TRAILING);
        panel.add(label);
        textField = new JTextField(3);
        label.setLabelFor(textField);
        panel.add(textField);
        
        label = new JLabel("Window foreground colour: ", JLabel.TRAILING);
        panel.add(label);
        textField = new JTextField(3);
        label.setLabelFor(textField);
        panel.add(textField);
        
        label = new JLabel("Nicklist sort by mode: ", JLabel.TRAILING);
        panel.add(label);
        checkBox = new JCheckBox();
        label.setLabelFor(checkBox);
        panel.add(checkBox);
        
        label = new JLabel("Nicklist sort by case: ", JLabel.TRAILING);
        panel.add(label);
        checkBox = new JCheckBox();
        label.setLabelFor(checkBox);
        panel.add(checkBox);
        
        label = new JLabel("Treeview rollover enabled: ", JLabel.TRAILING);
        panel.add(label);
        checkBox = new JCheckBox();
        label.setLabelFor(checkBox);
        panel.add(checkBox);
        
        label = new JLabel("Treeview rollover colour: ", JLabel.TRAILING);
        panel.add(label);
        textField = new JTextField(3);
        label.setLabelFor(textField);
        panel.add(textField);
        
        label = new JLabel("Treeview sort windows: ", JLabel.TRAILING);
        panel.add(label);
        checkBox = new JCheckBox();
        label.setLabelFor(checkBox);
        panel.add(checkBox);
        
        label = new JLabel("Treeview sort servers: ", JLabel.TRAILING);
        panel.add(label);
        checkBox = new JCheckBox();
        label.setLabelFor(checkBox);
        panel.add(checkBox);
        
        label = new JLabel("Split user modes: ", JLabel.TRAILING);
        panel.add(label);
        checkBox = new JCheckBox();
        label.setLabelFor(checkBox);
        panel.add(checkBox);
        
        label = new JLabel("Socket closed: ", JLabel.TRAILING);
        panel.add(label);
        comboBox = new JComboBox(windowOptions);
        label.setLabelFor(comboBox);
        panel.add(comboBox);
        
        label = new JLabel("Private notice: ", JLabel.TRAILING);
        panel.add(label);
        comboBox = new JComboBox(windowOptions);
        label.setLabelFor(comboBox);
        panel.add(comboBox);;
        
        label = new JLabel("CTCP request: ", JLabel.TRAILING);
        panel.add(label);
        comboBox = new JComboBox(windowOptions);
        label.setLabelFor(comboBox);
        panel.add(comboBox);
        
        label = new JLabel("CTCP reply: ", JLabel.TRAILING);
        panel.add(label);
        comboBox = new JComboBox(windowOptions);
        label.setLabelFor(comboBox);
        panel.add(comboBox);
        
        layoutGrid(panel, 16, 2, SMALL_BORDER, SMALL_BORDER,
                LARGE_BORDER, LARGE_BORDER);
        
        constraints.fill = GridBagConstraints.BOTH;
        constraints.weightx = 1.0;
        constraints.weighty = 1.0;
        uiPanel.add(panel, constraints);
    }
    
    private void initInputTab(final JTabbedPane tabbedPane) {
        final JPanel inputPanel = new JPanel();
        final GridBagConstraints constraints = new GridBagConstraints();
        JPanel panel;
        JLabel label;
        JTextField textField;
        JCheckBox checkBox;
        
        tabbedPane.addTab("Input", inputPanel);
        
        inputPanel.setBorder(new EmptyBorder(LARGE_BORDER, LARGE_BORDER,
                LARGE_BORDER, LARGE_BORDER));
        
        panel = new JPanel(new SpringLayout());
        label = new JLabel("Command Character: ", JLabel.TRAILING);
        panel.add(label);
        textField = new JTextField(10);
        label.setLabelFor(textField);
        panel.add(textField);
        
        label = new JLabel("Case-sensitive tab completion: ", JLabel.TRAILING);
        panel.add(label);
        checkBox = new JCheckBox();
        label.setLabelFor(checkBox);
        panel.add(checkBox);
        
        layoutGrid(panel, 2, 2, SMALL_BORDER, SMALL_BORDER,
                LARGE_BORDER, LARGE_BORDER);
        
        constraints.fill = GridBagConstraints.BOTH;
        constraints.weightx = 1.0;
        constraints.weighty = 1.0;
        inputPanel.add(panel, constraints);
    }
    
    /**
     * Initialises the logging tab.
     *
     * @param tabbedPane parent pane
     */
    private void initLoggingTab(final JTabbedPane tabbedPane) {
        final JPanel loggingPanel = new JPanel(new GridBagLayout());
        final GridBagConstraints constraints = new GridBagConstraints();
        JPanel panel;
        JLabel label;
        JTextField textField;
        JCheckBox checkBox;
        
        tabbedPane.addTab("Logging", loggingPanel);
        
        loggingPanel.setBorder(new EmptyBorder(LARGE_BORDER, LARGE_BORDER,
                LARGE_BORDER, LARGE_BORDER));
        
        panel = new JPanel(new SpringLayout());
        label = new JLabel("Date format: ", JLabel.TRAILING);
        panel.add(label);
        textField = new JTextField(25);
        label.setLabelFor(textField);
        panel.add(textField);
        
        String[] messageLabels = {"Program logs: ", "Debug logging: ", "Sys.err Debug logging: ", };
        int numPairs = messageLabels.length;
        
        for (int i = 0; i < numPairs; i++) {
            label = new JLabel(messageLabels[i], JLabel.TRAILING);
            panel.add(label);
            checkBox = new JCheckBox();
            label.setLabelFor(checkBox);
            panel.add(checkBox);
        }
        
        layoutGrid(panel, numPairs + 1, 2, SMALL_BORDER, SMALL_BORDER,
                LARGE_BORDER, LARGE_BORDER);
        
        constraints.fill = GridBagConstraints.BOTH;
        constraints.weightx = 1.0;
        constraints.weighty = 1.0;
        loggingPanel.add(panel, constraints);
    }
    
    /**
     * Initialises the identities tab.
     *
     * @param tabbedPane parent pane
     */
    private void initIdentitiesTab(final JTabbedPane tabbedPane) {
        final JPanel loggingPanel = new JPanel(new GridBagLayout());
        
        tabbedPane.addTab("Identities", loggingPanel);
        
        loggingPanel.setBorder(new EmptyBorder(LARGE_BORDER, LARGE_BORDER,
                LARGE_BORDER, LARGE_BORDER));
    }
    
    /**
     * Initialises listeners for this dialog.
     */
    private void initListeners() {
        getOkButton().addActionListener(this);
        getCancelButton().addActionListener(this);
    }
    
    public void actionPerformed(ActionEvent actionEvent) {
        if (getOkButton().equals(actionEvent.getSource())) {
            //TODO apply settings
            setVisible(false);
        } else if (getCancelButton().equals(actionEvent.getSource())) {
            setVisible(false);
        }
    }
    
    /**
     * Aligns the first <code>rows</code> * <code>cols</code>
     * components of <code>parent</code> in
     * a grid. Each component in a column is as wide as the maximum
     * preferred width of the components in that column;
     * height is similarly determined for each row.
     * The parent is made just big enough to fit them all.
     *
     * @param rows number of rows
     * @param cols number of columns
     * @param initialX x location to start the grid at
     * @param initialY y location to start the grid at
     * @param xPad x padding between cells
     * @param yPad y padding between cells
     */
    public final void layoutGrid(Container parent, int rows, int columns,
            int initialXPadding, int initialYPadding,
            int xPadding, int yPadding) {
        final SpringLayout layout = (SpringLayout)parent.getLayout();
        
        Spring x = Spring.constant(initialXPadding);
        Spring y = Spring.constant(initialYPadding);
        
        for (int c = 0; c < columns; c++) {
            Spring width = Spring.constant(0);
            for (int r = 0; r < rows; r++) {
                width = Spring.max(width,
                        getConstraintsForCell(r, c, parent, columns).
                        getWidth());
            }
            for (int r = 0; r < rows; r++) {
                SpringLayout.Constraints constraints =
                        getConstraintsForCell(r, c, parent, columns);
                constraints.setX(x);
                constraints.setWidth(width);
            }
            x = Spring.sum(x, Spring.sum(width, Spring.constant(xPadding)));
        }
        
        for (int r = 0; r < rows; r++) {
            Spring height = Spring.constant(0);
            for (int c = 0; c < columns; c++) {
                height = Spring.max(height,
                        getConstraintsForCell(r, c, parent, columns).
                        getHeight());
            }
            for (int c = 0; c < columns; c++) {
                SpringLayout.Constraints constraints =
                        getConstraintsForCell(r, c, parent, columns);
                constraints.setY(y);
                constraints.setHeight(height);
            }
            y = Spring.sum(y, Spring.sum(height, Spring.constant(yPadding)));
        }
        
        SpringLayout.Constraints pCons = layout.getConstraints(parent);
        pCons.setConstraint(SpringLayout.SOUTH, y);
        pCons.setConstraint(SpringLayout.EAST, x);
    }
    
    /**
     * Returns the constraints for a specific cell
     *
     * @param row Row of cell
     * @param col Column of cell
     * @param parent parent container
     * @param cols number of columns
     */
    private final SpringLayout.Constraints getConstraintsForCell(
            int row, int col, Container parent, int columns) {
        SpringLayout layout = (SpringLayout) parent.getLayout();
        Component c = parent.getComponent(row * columns + col);
        return layout.getConstraints(c);
    }
}
