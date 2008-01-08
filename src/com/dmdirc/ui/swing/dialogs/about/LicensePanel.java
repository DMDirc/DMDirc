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

package com.dmdirc.ui.swing.dialogs.about;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

import net.miginfocom.swing.MigLayout;

/**
 * License panel.
 */
public final class LicensePanel extends JPanel {
    
    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 3;
    
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
        license.setText("Most of DMDirc is licensed under the MIT license, " 
                + "however some portions (MigLayout) are licensed under the " 
                + "BSD license, see below for details of both.\n\nMIT License."
                + "\nCopyright (c) 2006-2008 The DMDirc team\n\nPermission "
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
                + "THE USE OR OTHER DEALINGS IN THE SOFTWARE.\n\n\n"
                + "BSD License.\n"
                + "Copyright (c) 2004, Mikael Grev, MiG InfoCom AB. (miglayout " 
                + "(at) miginfocom (dot) com)\nAll rights reserved.\n\n"
                + "Redistribution and use in source and binary forms, with or " 
                + "without modification, are permitted provided that the " 
                + "following conditions are met:\nRedistributions of source " 
                + "code must retain the above copyright notice, this list of " 
                + "conditions and the following disclaimer.\nRedistributions " 
                + "in binary form must reproduce the above copyright notice, " 
                + "this list of conditions and the following disclaimer in the " 
                + "documentation and/or other materials provided with the " 
                + "distribution.\nNeither the name of the MiG InfoCom AB nor " 
                + "the names of its contributors may be used to endorse or " 
                + "promote products derived from this software without specific"
                + "prior written permission.\n\nTHIS SOFTWARE IS PROVIDED BY " 
                + "THE COPYRIGHT HOLDERS AND CONTRIBUTORS \"AS IS\" AND ANY " 
                + "EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED " 
                + "TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS " 
                + "FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL " 
                + "THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY " 
                + "DIRECT INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR " 
                + "CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, " 
                + "PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, " 
                + "DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED " 
                + "AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT " 
                + "LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) " 
                + "ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN " 
                + "IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.\n");
        license.setEditable(false);
        license.setWrapStyleWord(true);
        license.setLineWrap(true);
        license.setColumns(40);
        license.setRows(10);
        
        scrollPane = new JScrollPane(license);
        SwingUtilities.invokeLater(new Runnable() {
            /** {@inheritDoc} */
            @Override
            public void run() {
                scrollPane.getVerticalScrollBar().setValue(0);
            }
        }
        );
        
        setLayout(new MigLayout("ins rel, fill"));
        add(scrollPane, "grow");
    }   
}
