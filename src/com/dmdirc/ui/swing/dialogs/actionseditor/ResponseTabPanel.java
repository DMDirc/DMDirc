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

package com.dmdirc.ui.swing.dialogs.actionseditor;

import com.dmdirc.actions.interfaces.ActionType;
import com.dmdirc.config.IdentityManager;
import com.dmdirc.ui.swing.UIUtilities;
import static com.dmdirc.ui.swing.UIUtilities.LARGE_BORDER;
import static com.dmdirc.ui.swing.UIUtilities.SMALL_BORDER;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

/**
 * Response tab panel, response and formatter editing for the actions editor
 * dialog.
 */
public final class ResponseTabPanel extends JPanel implements 
        SubstitutionsPanelListener {
    
    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 1;
    
    /** The parent dialog. */
    private final ActionsEditorDialog owner;
    /** Response field. */
    private JTextArea responses;
    /** Formatters combobox. */
    private JComboBox formatter;
    /** Formatter scrollpane. */
    private JScrollPane scrollPane;
    /** Substitutions panel. */
    private SubstitutionsPanel subsPanel;
    
    /**
     * Creates a new instance of ResponseTabPanel.
     *
     * @param owner Parent dialog
     */
    public ResponseTabPanel(final ActionsEditorDialog owner) {
        super();
        
        this.owner = owner;
        
        this.setOpaque(UIUtilities.getTabbedPaneOpaque());
        initComponents();
        layoutComponents();
    }
    
    /** Initialises the components. */
    private void initComponents() {
        subsPanel = new SubstitutionsPanel(owner.getTrigger());
        responses = new JTextArea();
        formatter = new JComboBox(new DefaultComboBoxModel());
        scrollPane = new JScrollPane(responses);
        
        subsPanel.addSubstitutionsPanelListener(this);
        
        scrollPane.setBorder(BorderFactory.createCompoundBorder(
                scrollPane.getBorder(), BorderFactory.createEmptyBorder(
                SMALL_BORDER, 0,SMALL_BORDER, 0)));
        
        responses.setRows(3);
        formatter.setPreferredSize(
                new Dimension(100, formatter.getFont().getSize()
                + LARGE_BORDER));
        
        ((DefaultComboBoxModel) formatter.getModel()).addElement("No change");
        ((DefaultComboBoxModel) formatter.getModel()).addElement("No response");
        
        final Set<String> formatters
                = IdentityManager.getGlobalConfig().getOptions("formatter").keySet();
        
        for (String format : formatters) {
            ((DefaultComboBoxModel) formatter.getModel()).addElement(format);
        }
        
        if (owner.getAction() == null) {
            return;
        }
        
        final StringBuilder sb = new StringBuilder();
        for (String response : owner.getAction().getResponse()) {
            sb.append(response).append('\n');
        }
        
        if (sb.length() > 0) {
            responses.setText(sb.substring(0, sb.length() -1));
        }
        
        if (owner.getAction().getNewFormat() == null) {
            formatter.setSelectedIndex(0);
        } else if (owner.getAction().getNewFormat().isEmpty()) {
            formatter.setSelectedIndex(1);
        } else {
            formatter.setSelectedItem(owner.getAction().getNewFormat());
        }
    }
    
    /** Lays out components. */
    private void layoutComponents() {
        final GridBagConstraints constraints = new GridBagConstraints();
        
        this.setBorder(BorderFactory.createEmptyBorder(SMALL_BORDER,
                SMALL_BORDER, SMALL_BORDER, SMALL_BORDER));
        this.setLayout(new GridBagLayout());
        
        constraints.fill = GridBagConstraints.BOTH;
        constraints.weightx = 1.0;
        constraints.weighty = 0.0;
        constraints.gridwidth = 4;
        constraints.gridx = 0;
        constraints.gridy = 0;
        add(new JLabel("Execute the following commands: "), constraints);
        
        constraints.gridwidth = 1;
        constraints.gridheight = 2;
        constraints.gridx = 4;
        constraints.insets = new Insets(0, SMALL_BORDER, SMALL_BORDER, 0);
        add(subsPanel, constraints);
        
        constraints.insets = new Insets(0, 0, 0, 0);
        constraints.gridheight = 1;
        constraints.gridwidth = 3;
        constraints.gridx = 0;
        constraints.weighty = 1.0;
        constraints.gridy = 1;
        add(scrollPane, constraints);
        
        constraints.weighty = 0.0;
        constraints.weightx = 0.0;
        constraints.gridwidth = 1;
        constraints.gridy = 2;
        add(new JLabel("Formatter: "), constraints);
        
        constraints.weightx = 1.0;
        constraints.gridx = 1;
        constraints.gridwidth = 4;
        add(formatter, constraints);
    }
    
    /**
     * Returns the responses for this panel.
     *
     * @return Responses
     */
    public String getResponses() {
        return responses.getText();
    }
    
    /**
     * Returns the name for this panel.
     *
     * @return Action name
     */
    public String getFormatter() {
        String format = (String) formatter.getSelectedItem();
        if ("No response".equals(format)) {
            format = "";
        }
        if ("No change".equals(format)) {
            format = null;
        }
        return format;
    }

    /** {@inheritDoc} */
    @Override
    public void substitutionInsert(final ActionSubstitution substitution) {
            responses.replaceSelection(substitution.toString());
    }
    
    /**
     * Sets the Actiontype of the substitutions panel.
     *
     * @param type ActionType for the panel
     */
    public void setTrigger(final ActionType type) {
        subsPanel.setType(type);
    }
    
}
