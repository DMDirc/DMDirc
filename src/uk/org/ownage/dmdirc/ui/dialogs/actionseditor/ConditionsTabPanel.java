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

package uk.org.ownage.dmdirc.ui.dialogs.actionseditor;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import uk.org.ownage.dmdirc.actions.Action;

import static uk.org.ownage.dmdirc.ui.UIUtilities.SMALL_BORDER;

/**
 * Conditions tab panel, conditions editing for the actions editor dialog.
 */
public final class ConditionsTabPanel extends JPanel implements ActionListener {
    
    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 1;
    
    /** The action. */
    private Action action;
    
    /** New button panel. */
    private JPanel buttonsPanel;
    /** Comparisons components panel. */
    private JPanel comparisonsPanel;
    
    /** Info blurb. */
    private JTextArea infoLabel;
    /** No conditions label. */
    private JLabel noConditions;
    /** New comparison button. */
    private JButton newComparison;
    
    /** 
     * Creates a new instance of ConditionsTabPanel. 
     *
     * @param action action to be edited
     */
    public ConditionsTabPanel(final Action action) {
        super();
        
        this.action = action;
        
        initComponents();
        addListeners();
        layoutComponents();
    }
    
    /** Initialises the components. */
    private void initComponents() {     
        comparisonsPanel = new JPanel();
        noConditions = new JLabel("No conditions set.");
        newComparison = new JButton("New");
        infoLabel = new JTextArea("This action will only be executed if the " 
                + "following are true:");
        
        infoLabel.setBorder(BorderFactory.createEmptyBorder(SMALL_BORDER, 
                SMALL_BORDER, SMALL_BORDER, SMALL_BORDER));
        
        comparisonsPanel.setBorder(BorderFactory.createEmptyBorder(0, 
                SMALL_BORDER, SMALL_BORDER, SMALL_BORDER));
        
        infoLabel.setEditable(false);
        infoLabel.setWrapStyleWord(true);
        infoLabel.setLineWrap(true);
        infoLabel.setHighlighter(null);
        infoLabel.setBackground(this.getBackground());
        
        initButtonsPanel();
    }
    
    /** Adds listeners to the components. */
    private void addListeners() {
        newComparison.addActionListener(this);
    }
    
    /** Lays out components. */
    private void layoutComponents() {
        layoutComparisonPanel();
        
        this.setLayout(new BorderLayout());
        
        this.add(infoLabel, BorderLayout.PAGE_START);
        this.add(comparisonsPanel, BorderLayout.CENTER);
        this.add(buttonsPanel, BorderLayout.PAGE_END);
    }
    
    /** Lays out the comparisons panel. */
    private void layoutComparisonPanel() {
        if (comparisonsPanel.getComponentCount() == 0) {
            comparisonsPanel.add(noConditions);
        } else {
            //Do some layout of the stuff.
        }
    }
    
    /** Initialises the button panel. */
    private void initButtonsPanel() {        
        buttonsPanel = new JPanel();
        
        buttonsPanel.setBorder(BorderFactory.createEmptyBorder(0, SMALL_BORDER,
                SMALL_BORDER, SMALL_BORDER));
        
        buttonsPanel.setLayout(new BoxLayout(buttonsPanel, BoxLayout.LINE_AXIS));
        
        buttonsPanel.add(Box.createHorizontalGlue());
        buttonsPanel.add(newComparison);
    }

    /** {@inheritDoc}. */
    public void actionPerformed(final ActionEvent event) {
        if (event.getSource() == newComparison) {
            new ConditionEditorDialog(this);
        }
    }
    
}
