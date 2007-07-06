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

package com.dmdirc.ui.dialogs.channelsetting;

import com.dmdirc.Channel;
import com.dmdirc.parser.ChannelListModeItem;
import static com.dmdirc.ui.UIUtilities.SMALL_BORDER;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

/**
 * List modes panel.
 */
public final class ChannelListModesPane extends JPanel implements
        ActionListener, ListSelectionListener  {
    
    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 1;
    
    /** Channel. */
    private final Channel channel;
    
    /** Combox box used to switch between list modes. */
    private final JComboBox listModesMenu;
    
    /** Arraylist of jpanels containing the listmodes. */
    private final List<JPanel> listModesPanels;
    
    /** JPanel used to show listmodespanels in. */
    private final JScrollPane listModesPanel;
    
    /** Add list mode button. */
    private final JButton addListModeButton;
    
    /** Remove list mode button. */
    private final JButton removeListModeButton;
    
    /** list modes available on this server. */
    private final char[] listModesArray;
    
    /**
     * Creates a new instance of ChannelListModePane.
     *
     * @param channel Parent channel
     */
    public ChannelListModesPane(final Channel channel) {
        super();
        
        this.channel = channel;
        
        listModesPanel = new JScrollPane();
        listModesPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(SMALL_BORDER, 0,
                SMALL_BORDER, 0),
                listModesPanel.getBorder()));
        listModesPanels = new ArrayList<JPanel>();
        listModesArray =
                channel.getServer().getParser().getListChanModes().toCharArray();
        listModesMenu =
                new JComboBox(new DefaultComboBoxModel());
        addListModeButton = new JButton("Add");
        removeListModeButton = new JButton("Remove");
        removeListModeButton.setEnabled(false);
        
        initListModesPanel();
        initListeners();
        
        setVisible(true);
    }
    
    /** Updates the panel. */
    public void update() {
        final boolean visible = listModesPanel.isVisible();
        
        if (visible) {
            listModesPanel.setVisible(false);
        }
        
        for (int i = 0; i < listModesArray.length;
        i++) {
            final char mode = listModesArray[i];
            final ArrayList<ChannelListModeItem> listItems =
                    channel.getChannelInfo().getListModeParam(mode);
            final DefaultListModel model =
                    (DefaultListModel) ((JList) listModesPanels.get(i).getComponent(0)).getModel();
            
            model.removeAllElements();
            for (ChannelListModeItem listItem : listItems) {
                model.addElement(listItem);
            }
        }
        
        if (visible) {
            listModesPanel.setVisible(true);
        }
    }
    
    /** Updates the list mode menu. */
    private void updateMenu() {
        final DefaultComboBoxModel model =
                (DefaultComboBoxModel) listModesMenu.getModel();
        for (char mode : listModesArray) {
            String modeText = mode + " list";
            if (channel.getConfigManager().
                    getOptionBool("server", "friendlymodes")
                    && channel.getConfigManager().hasOption("server", "mode" + mode)) {
                modeText =
                        channel.getConfigManager().
                        getOption("server", "mode" + mode) + " list";
            }
            model.addElement(modeText);
            
            final JPanel panel =
                    new JPanel(new BorderLayout());
            final JList list = new JList(new DefaultListModel());
            list.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
            list.addListSelectionListener(this);
            panel.add(list, BorderLayout.CENTER);
            listModesPanels.add(panel);
        }
        listModesPanel.setViewportView(listModesPanels.get(0));
        listModesPanel.setVisible(true);
    }
    
    /** Initialises the list modes panel. */
    private void initListModesPanel() {
        final GridBagConstraints constraints = new GridBagConstraints();
        
        updateMenu();
        
        setLayout(new GridBagLayout());
        setBorder(BorderFactory.createEmptyBorder(SMALL_BORDER, SMALL_BORDER,
                SMALL_BORDER, SMALL_BORDER));
        
        constraints.weightx = 1.0;
        constraints.weighty = 0.0;
        constraints.fill = GridBagConstraints.BOTH;
        constraints.gridx = 1;
        constraints.gridy = 1;
        constraints.gridwidth = 2;
        add(listModesMenu, constraints);
        
        constraints.gridy = 2;
        constraints.weighty = 1.0;
        add(listModesPanel, constraints);
        
        constraints.weightx = 0.5;
        constraints.weighty = 0.0;
        constraints.gridy = 3;
        constraints.gridwidth = 1;
        add(addListModeButton, constraints);
        
        constraints.gridx = 2;
        constraints.insets = new Insets(0, SMALL_BORDER, 0, 0);
        add(removeListModeButton, constraints);
        
        update();
    }
    
    /** Initialises listeners for this dialog. */
    private void initListeners() {
        addListModeButton.addActionListener(this);
        removeListModeButton.addActionListener(this);
        listModesMenu.addActionListener(this);
    }
    
    /** Adds a list mode. */
    private void addListMode() {
        final int selectedIndex = listModesMenu.getSelectedIndex();
        String modeText = "" + listModesArray[selectedIndex];
        String modeMask;
        if (channel.getConfigManager().
                hasOption("server", "mode" + listModesArray[selectedIndex])) {
            modeText =
                    channel.getConfigManager().
                    getOption("server", "mode" + listModesArray[selectedIndex]);
        }
        modeMask =
                JOptionPane.showInputDialog(listModesPanel,
                "Please enter the hostmask for the new " + modeText);
        if (modeMask != null && (!modeMask.equals("") || modeMask.length() > 0)) {
            channel.getChannelInfo().
                    alterMode(true, listModesArray[selectedIndex], modeMask);
            channel.getChannelInfo().sendModes();
        }
        final DefaultListModel model =
                (DefaultListModel) ((JList) listModesPanels.get(selectedIndex).getComponent(0)).getModel();
        model.addElement(new ChannelListModeItem(modeMask, "",
                System.currentTimeMillis() / 1000));
    }
    
    /** Removes a list mode. */
    private void removeListMode() {
        final int selectedIndex = listModesMenu.getSelectedIndex();
        final JList list =
                (JList) listModesPanels.get(selectedIndex).getComponent(0);
        for (Object mode : list.getSelectedValues()) {
            ((DefaultListModel) list.getModel()).removeElement(mode);
            channel.getChannelInfo().
                    alterMode(false, listModesArray[selectedIndex],
                    mode.toString());
        }
        channel.getChannelInfo().sendModes();
    }
    
    /** {@inheritDoc} */
    public void actionPerformed(final ActionEvent event) {
        if (listModesMenu.equals(event.getSource())) {
            listModesPanel.setVisible(false);
            listModesPanel.setViewportView(listModesPanels.get(listModesMenu.getSelectedIndex()));
            listModesPanel.setVisible(true);
        } else if (addListModeButton.equals(event.getSource())) {
            addListMode();
        } else if (removeListModeButton.equals(event.getSource())) {
            removeListMode();
        }
    }

    /** {@inheritDoc} */
    public void valueChanged(final ListSelectionEvent event) {
        if (!event.getValueIsAdjusting()) {
            final int selected = ((JList) event.getSource()).getSelectedIndex();
            if (selected == -1) {
                removeListModeButton.setEnabled(false);
            } else {
                removeListModeButton.setEnabled(true);
            }
        }
    }
}
