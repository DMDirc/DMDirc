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

package com.dmdirc.ui.dialogs.about;

import static com.dmdirc.ui.UIUtilities.SMALL_BORDER;

import java.awt.BorderLayout;
import java.awt.Insets;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

/**
 * License panel.
 */
public class LicensePanel extends JPanel {
    
    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 2;
    
    /** License scroll pane. */
    private JScrollPane scrollPane;
    
    /** Creates a new instance of LicensePanel. */
    public LicensePanel() {
        super();
        
        initComponents();
    }
    
    /** Initialises the components. */
    private void initComponents() {
        final JTextArea license;
        
        license = new JTextArea();
        license.setText("Copyright (c) 2006-2007 The DMDirc team\n\nPermission "
                + "is hereby granted, free of charge, to any person obtaining a"
                + " copy of this software and associated documentation files "
                + "(the \"Software\"), to deal in the Software without "
                + "restriction, including without limitation the rights to use,"
                + " copy, modify, merge, publish, distribute, sublicense, "
                + "and/or sell copies of the Software, and to permit persons "
                + "to whom the Software is furnished to do so, subject to the "
                + "following conditions:\n\nThe above copyright notice and this"
                + " permission notice shall be included in all copies or "
                + "substantial portions of the Software.\n\nTHE SOFTWARE IS "
                + "PROVIDED \"AS IS\", WITHOUT WARRANTY OF ANY KIND, EXPRESS "
                + "OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF "
                + "MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND "
                + "NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT "
                + "HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,"
                + " WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, "
                + "ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR "
                + "THE USE OR OTHER DEALINGS IN THE SOFTWARE.\n");
        license.setEditable(false);
        license.setWrapStyleWord(true);
        license.setLineWrap(true);
        license.setMargin(new Insets(SMALL_BORDER, SMALL_BORDER, SMALL_BORDER,
                SMALL_BORDER));
        
        license.setColumns(40);
        license.setRows(10);
        
        scrollPane = new JScrollPane(license);
        scrollPane.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(SMALL_BORDER, SMALL_BORDER,
                SMALL_BORDER, SMALL_BORDER),
                scrollPane.getBorder()
                ));
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                scrollPane.getVerticalScrollBar().setValue(0);
            }
        }
        );
        
        setLayout(new BorderLayout());
        
        add(scrollPane, BorderLayout.CENTER);
    }   
}
