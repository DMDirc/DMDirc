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

import java.util.Arrays;
import java.util.Hashtable;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.WindowConstants;
import javax.swing.border.BevelBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import org.jdesktop.layout.GroupLayout;
import uk.org.ownage.dmdirc.Channel;
import uk.org.ownage.dmdirc.Config;
import uk.org.ownage.dmdirc.ui.components.ParamModePanel;

/**
 * Allows the user to modify channel settings (modes, topics, etc)
 * @author chris
 */
public class ChannelSettingsDialog extends StandardDialog
        implements ActionListener {
    
    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 1;
    
    private Channel channel;
    
    private JTabbedPane tabbedPane;
    private JPanel settingsPanel;
    private JPanel identitiesPanel;
    private JButton button1;
    private JButton button2;
    private JPanel modesPanel;
    private Hashtable<String, JCheckBox> modeCheckBoxes;
    private Hashtable<String, ParamModePanel> modeInputs;
    
    /**
     * Creates a new instance of ChannelSettingsDialog
     * @param channel The channel object that we're editing settings for
     */
    public ChannelSettingsDialog(Channel channel) {
        super(MainFrame.getMainFrame(), false);
        
        this.channel = channel;
        
        initComponents();
        initListeners();
    }
    
    /** Initialises GUI components */
    private void initComponents() {
        // --- Set up the main interface
        
        GridBagConstraints constraints = new GridBagConstraints();
        
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setResizable(false);
        
        setTitle("Channel settings for "+channel);
        
        getContentPane().setLayout(new GridBagLayout());
        
        settingsPanel = new JPanel(new GridBagLayout());
        //settingsPanel.setPreferredSize(new Dimension(400,400));
        
        identitiesPanel = new JPanel(new GridBagLayout());
        //identitiesPanel.setPreferredSize(new Dimension(400,400));
        
        tabbedPane = new JTabbedPane();
        tabbedPane.addTab("IRC Settings", settingsPanel);
        tabbedPane.addTab("Client Settings", identitiesPanel);
        
        button1 = new JButton();
        button1.setPreferredSize(new Dimension(100,25));
        button2 = new JButton();
        button2.setPreferredSize(new Dimension(100,25));
        
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.gridwidth = 3;
        constraints.fill = GridBagConstraints.BOTH;
        constraints.insets = new Insets(10, 10, 10, 10);
        getContentPane().add(tabbedPane, constraints);
        
        constraints.insets.set(0, 10, 10, 10);
        constraints.gridx = 1;
        constraints.gridy = 1;
        constraints.gridwidth = 1;
        constraints.anchor = GridBagConstraints.EAST;
        constraints.fill = GridBagConstraints.NONE;
        getContentPane().add(button1, constraints);
        
        constraints.gridx = 2;
        getContentPane().add(button2, constraints);
        
        orderButtons(button1, button2);
        
        // --- Set up the channel settings page
               
        settingsPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.BOTH;
        constraints.anchor = GridBagConstraints.NORTH;
        modesPanel = new JPanel(new GridBagLayout());
        modesPanel.setBorder(new TitledBorder(new EtchedBorder(),"Channel Modes"));
        //modesPanel.setPreferredSize(new Dimension(380, 200));
        settingsPanel.add(modesPanel, constraints);
        
        // TODO: Get these from the server!
        String booleanModes = "imnpstrDdcCNu";
        String ourBooleanModes = channel.getChannelInfo().getModeStr();
        String paramModes = "lk";
        String listModes = "b";
        
        char[] booleanModesTemp = booleanModes.toCharArray();
        Arrays.sort(booleanModesTemp);
        booleanModes = new String(booleanModesTemp);
        
        modeCheckBoxes = new Hashtable<String, JCheckBox>();
        
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.anchor = GridBagConstraints.WEST;
        
        // Lay out all the boolean mode checkboxes
        for (int i = 0; i < booleanModes.length(); i++) {
            String mode = booleanModes.substring(i, i+1);
            String text = "Mode "+mode;
            boolean state = ourBooleanModes.split(" ")[0].contains(mode.subSequence(0, 1));
            
            if (Config.hasOption("server","mode"+mode)) {
                text = Config.getOption("server","mode"+mode);
            }
            
            JCheckBox checkBox = new JCheckBox(text, state);
            checkBox.setBorder(new EmptyBorder(0,10,0,10));
            modesPanel.add(checkBox, constraints);
            
            constraints.gridx++;
            if (constraints.gridx == 2) {
                constraints.gridy++;
                constraints.gridx = 0;
            }
            
            modeCheckBoxes.put(mode, checkBox);
        }
        
        // Lay out all the parameter-requiring modes
        modeInputs = new Hashtable<String, ParamModePanel>();
        
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.gridwidth = 2;
        
        if (constraints.gridx != 0) {
            constraints.gridy++;
            constraints.gridx = 0;
        }
        
        for (int i = 0; i < paramModes.length(); i++) {
            String mode = paramModes.substring(i, i+1);
            String value = channel.getChannelInfo().getModeParam(mode.charAt(0));
            boolean state = ourBooleanModes.split(" ")[0].contains(mode.subSequence(0, 1));
            
            ParamModePanel panel = new ParamModePanel(mode, state, value);
            modesPanel.add(panel, constraints);
            
            modeInputs.put(mode, panel);
            
            constraints.gridy++;
        }
        
        pack();
    }
    
    /** Initialises listeners for this dialog */
    private void initListeners() {
        button1.addActionListener(this);
        button2.addActionListener(this);
    }

    public void actionPerformed(ActionEvent actionEvent) {
        if (getOkButton().equals(actionEvent.getSource())) {
            // TODO: Apply settings
            setVisible(false);
        } else if (getCancelButton().equals(actionEvent.getSource())) {
            setVisible(false);
        }
    }
    
}
