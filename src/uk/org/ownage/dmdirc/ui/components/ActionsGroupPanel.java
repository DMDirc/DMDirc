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

package uk.org.ownage.dmdirc.ui.components;

import java.awt.BorderLayout;
import java.awt.Color;
import java.util.List;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.LineBorder;
import uk.org.ownage.dmdirc.actions.Action;
import uk.org.ownage.dmdirc.ui.dialogs.ActionsManagerDialog;
import static uk.org.ownage.dmdirc.ui.UIUtilities.SMALL_BORDER;

/**
 * The actions group panel is the control displayed within the tabbed control
 * of the actions manager dialog. It shows the user all actions belonging to
 * a particular group.
 * @author chris
 */
public class ActionsGroupPanel extends JPanel {
    
    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 1;
    
    /** The column headers we use. */
    private static final String[] headers = {"Name", "Trigger", "Response"};
    
    /** The dialog that owns this group panel. */
    private final ActionsManagerDialog parent;
    
    /** The actions that belong in this pane. */
    private List<Action> actions;
    
    /** The JTable we use to display data. */
    private JTable table;
    
    /** Creates a new instance of ActionsGroupPanel. */
    public ActionsGroupPanel(final ActionsManagerDialog parent,
            final List<Action> actions) {
        super();
        
        this.parent = parent;
        this.actions = actions;
        
        initComponents();
    }
    
    /** Initialises the components for this panel. */
    private void initComponents() {
        Object[][] data = new Object[actions.size()][3];
        
        int i = 0;
        for (Action action : actions) {
            data[i][0] = action.getName();
            data[i][1] = action.getTrigger()[0];
            data[i][2] = "Response";
            i++;
        }
        
        table = new JTable(data, headers) {
            private static final long serialVersionUID = 1;
            
            public boolean isCellEditable(final int x, final int y) {
                return false;
            }
        };
        
        table.setCellSelectionEnabled(false);
        table.setColumnSelectionAllowed(false);
        table.setRowSelectionAllowed(true);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setAutoCreateRowSorter(true);
        
        final JScrollPane pane = new JScrollPane(table);
        
        pane.setBorder(new CompoundBorder(
                new EmptyBorder(SMALL_BORDER, SMALL_BORDER, SMALL_BORDER, SMALL_BORDER),
                new EtchedBorder()
                ));
        
        setLayout(new BorderLayout());
        add(pane);
    }
    
}
