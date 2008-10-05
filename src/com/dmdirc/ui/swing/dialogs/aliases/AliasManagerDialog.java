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

package com.dmdirc.ui.swing.dialogs.aliases;

import com.dmdirc.ui.swing.components.renderers.ArrayCellRenderer;
import com.dmdirc.ui.swing.components.renderers.ActionConditionCellRenderer;
import com.dmdirc.actions.wrappers.Alias;
import com.dmdirc.actions.Action;
import com.dmdirc.actions.ActionCondition;
import com.dmdirc.actions.ActionManager;
import com.dmdirc.actions.CoreActionComparison;
import com.dmdirc.actions.wrappers.AliasWrapper;
import com.dmdirc.config.prefs.PreferencesInterface;
import com.dmdirc.ui.swing.components.PackingTable;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableCellRenderer;

import net.miginfocom.layout.PlatformDefaults;
import net.miginfocom.swing.MigLayout;

/**
 * Alias manager dialog.
 */
public final class AliasManagerDialog extends JPanel implements
        ActionListener, ListSelectionListener, PreferencesInterface {

    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 3;
    /** Table scrollpane. */
    private JScrollPane scrollPane;
    /** Error table. */
    private JTable table;
    /** Table model. */
    private AliasTableModel tableModel;
    /** Error detail panel. */
    private AliasPanel aliasDetails;
    /** Add/edit button. */
    private JButton addButton;
    /** Delete button. */
    private JButton deleteButton;
    /** Selected row. */
    private int selectedRow;
    /** Substitutions panel. */
    private AliasSubstitutionsPanel subsPanel;
    /** Show/Hide subsitution button. */
    private JButton showSubs;

    /** Creates a new instance of ErrorListDialog. */
    public AliasManagerDialog() {
        super();

        selectedRow = -1;

        initComponents();
        layoutComponents();
        initListeners();
    }

    /** Initialises the components. */
    private void initComponents() {
        final TableCellRenderer arrayRenderer = new ArrayCellRenderer();
        final TableCellRenderer conditionRenderer =
                new ActionConditionCellRenderer();

        addButton = new JButton("Add");
        deleteButton = new JButton("Delete");

        deleteButton.setEnabled(false);

        scrollPane = new JScrollPane();

        tableModel = new AliasTableModel(getTableData());
        table = new PackingTable(tableModel, false, scrollPane, false) {

            private static final long serialVersionUID = 1;

            @Override
            public TableCellRenderer getCellRenderer(final int row,
                    final int column) {
                switch (column) {
                    case 1:
                        return conditionRenderer;
                    case 2:
                        return arrayRenderer;
                    default:
                        return super.getCellRenderer(row, column);
                }
            }
        };

        table.setAutoCreateRowSorter(true);
        table.setAutoCreateColumnsFromModel(true);
        table.setColumnSelectionAllowed(false);
        table.setCellSelectionEnabled(false);
        table.setDragEnabled(false);
        table.setFillsViewportHeight(false);
        table.setRowSelectionAllowed(true);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getRowSorter().toggleSortOrder(0);

        table.getTableHeader().setReorderingAllowed(false);

        table.setPreferredScrollableViewportSize(new Dimension(600, 150));

        scrollPane.setViewportView(table);

        aliasDetails = new AliasPanel();
        subsPanel = new AliasSubstitutionsPanel();
        subsPanel.setVisible(false);
        showSubs = new JButton("Show Substitutions");
    }

    /**
     * Updates the table data.
     */
    public void updateTableData() {
        tableModel.setAliases(getTableData());
    }

    /**
     * Gets the table data fromt he alias wrapper.
     *
     * @return Alias list
     */
    private List<Alias> getTableData() {
        final List<Alias> aliases = new ArrayList<Alias>();

        for (Action loopAction : AliasWrapper.getAliasWrapper()) {
            final List<ActionCondition> arguments = loopAction.getConditions();

            ActionCondition argument;

            argument = arguments.get(0);

            if (argument.getComparison() != CoreActionComparison.STRING_EQUALS) {
                argument = arguments.get(1);
            }

            aliases.add(new Alias(argument.getTarget(),
                    arguments, loopAction.getResponse()));
        }

        return aliases;
    }

    /** Initialises the listeners. */
    private void initListeners() {
        table.getSelectionModel().addListSelectionListener(this);
        addButton.addActionListener(this);
        deleteButton.addActionListener(this);
        showSubs.addActionListener(this);
    }

    /** Lays out the components. */
    private void layoutComponents() {
        setLayout(new MigLayout("fill, ins 0, pack"));
        final JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, true);
        final JPanel panel = new JPanel();

        panel.setLayout(new MigLayout("ins 0, fill, hidemode 3, pack"));

        panel.add(aliasDetails, "span 6, wrap, grow, pushy");
        panel.add(subsPanel, "spanx, wrap, grow");
        panel.add(showSubs, "split 7, sgx button, left");
        panel.add(addButton, "skip 2, sgx button, gap unrel");
        panel.add(deleteButton, "sgx button");

        splitPane.setTopComponent(scrollPane);
        splitPane.setBottomComponent(panel);

        splitPane.setDividerSize((int) PlatformDefaults.getPanelInsets(0).getValue());
        
        add(splitPane, "grow, push");
    }

    /** {@inheritDoc}. */
    @Override
    public void valueChanged(final ListSelectionEvent e) {
        if (!e.getValueIsAdjusting()) {

            if (selectedRow > -1 && selectedRow < tableModel.getRowCount() && aliasDetails.getAlias() == tableModel.getAlias(
                    table.getRowSorter().convertRowIndexToModel(selectedRow))) {
                updateAlias();
            }

            if (table.getSelectedRow() > -1) {
                aliasDetails.setAlias(tableModel.getAlias(table.getRowSorter().
                        convertRowIndexToModel(table.getSelectedRow())));
                deleteButton.setEnabled(true);
            } else {
                aliasDetails.clear();
                deleteButton.setEnabled(false);
            }

            selectedRow = table.getSelectedRow();
        }
    }

    /** Updates the selected alias with the edited details. */
    private void updateAlias() {
        final Alias alias = tableModel.getAlias(table.getRowSorter().
                convertRowIndexToModel(selectedRow));

        alias.update(aliasDetails.getNewAlias());

        tableModel.fireTableRowsUpdated(tableModel.indexOf(alias),
                tableModel.indexOf(alias));
    }

    /** 
     * {@inheritDoc}
     * 
     * @param e Action event
     */
    @Override
    public void actionPerformed(final ActionEvent e) {
        if (e.getSource() == deleteButton) {
            delete();
        } else if (e.getSource() == addButton) {
            add();
        } else if (e.getSource() == showSubs) {
            if (subsPanel.isVisible()) {
                subsPanel.setVisible(false);
                showSubs.setText("Show Substitutions");
            } else {
                subsPanel.setVisible(true);
                showSubs.setText("Hide Substitutions");
            }
        }
    }

    /** Adds an alias. */
    private void add() {
        final Alias alias = new Alias("");
        tableModel.addRow(alias);
        final int newRow = table.getRowSorter().
                convertRowIndexToView(tableModel.indexOf(alias));
        table.getSelectionModel().setSelectionInterval(newRow, newRow);
        aliasDetails.focusCommand();
    }

    /** Deletes an alias. */
    private void delete() {
        if (table.getSelectedRow() != -1) {
            tableModel.removeRow(table.getRowSorter().
                    convertRowIndexToModel(table.getSelectedRow()));
        }
    }

    /** Saves the aliases. */
    @Override
    public void save() {
        if (table.getSelectedRow() != -1) {
                updateAlias();
            }
        if (checkForDuplicates()) {
                JOptionPane.showMessageDialog(this,
                        "There are duplicate aliases in the table, these need " +
                        "to be removed before saving", "Duplicates",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }
        final List<Action> actions =
                AliasWrapper.getAliasWrapper().getActions();
        final List<Alias> aliases = tableModel.getAliases();

        final List<Alias> newAliases = new ArrayList<Alias>();
        final List<Alias> modifiedAliases = new ArrayList<Alias>();

        for (Alias alias : aliases) {
            final Action action = getAction(alias);

            if (action == null) {
                newAliases.add(alias);
            } else {
                if (!action.getName().equals(alias.getName()) || !action.getConditions().
                        equals(alias.getArguments()) ||
                        !Arrays.equals(action.getResponse(), alias.getResponse())) {
                    modifiedAliases.add(alias);
                }
                actions.remove(action);
            }
        }

        for (Action action : actions) {
            action.delete();
        }

        saveNewAliases(newAliases);

        saveModifiedAliases(modifiedAliases);

        ActionManager.loadActions();
    }

    /**
     * Saves new aliases.
     *
     * @param aliases List of new aliases to save
     */
    private void saveNewAliases(final List<Alias> aliases) {
        for (Alias alias : aliases) {
            alias.createAction().save();
        }
    }

    /**
     * Saves modified aliases.
     *
     * @param aliases List of modified aliases to save
     */
    private void saveModifiedAliases(final List<Alias> aliases) {
        for (Alias alias : aliases) {
            final Action action = getAction(alias);
            if (action != null) {
                action.setName(alias.getName());
                action.setConditions(alias.getArguments());
                action.setResponse(alias.getResponse());
                action.save();
            }
        }
    }

    /**
     * Returns the action corresponding to the specified alias.
     *
     * @param alias Alias to check
     *
     * @return Corresponding action or null if none found
     */
    private Action getAction(final Alias alias) {
        final List<Action> actions =
                AliasWrapper.getAliasWrapper().getActions();
        Action action = null;

        for (Action loopAction : actions) {
            if (loopAction.getName().equals(alias.getName()) && loopAction.getConditions().
                    equals(alias.getArguments())) {
                action = loopAction;
                break;
            }
        }

        return action;
    }

    /**
     * Checks if ths alias matches another alias.
     *
     * @return true iif there are duplicate matches
     */
    private boolean checkForDuplicates() {
        final List<Alias> aliases = tableModel.getAliases();

        for (Alias alias : aliases) {
            int matches = 0;

            for (Alias loopAlias : aliases) {
                if (loopAlias.matches(alias)) {
                    matches++;
                }
            }

            if (matches > 1) {
                return true;
            }
        }

        return false;
    }
}
