/*
 * Copyright (c) 2006-2010 Chris Smith, Shane Mc Cormack, Gregory Holmes
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

package com.dmdirc.addons.ui_swing.dialogs.channelsetting;

import com.dmdirc.addons.ui_swing.components.renderers.ListModeCellRenderer;
import com.dmdirc.addons.ui_swing.components.renderers.ExtendedListModeCellRenderer;
import com.dmdirc.addons.ui_swing.UIUtilities;
import com.dmdirc.Channel;
import com.dmdirc.config.IdentityManager;
import com.dmdirc.interfaces.ConfigChangeListener;
import com.dmdirc.parser.common.ChannelListModeItem;
import com.dmdirc.util.MapList;

import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListCellRenderer;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import net.miginfocom.swing.MigLayout;

/** List modes panel. */
public final class ChannelListModesPane extends JPanel implements ActionListener,
        ListSelectionListener, ConfigChangeListener {

    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 5;
    /** Channel. */
    private final Channel channel;
    /** Combox box used to switch between list modes. */
    private final JComboBox listModesMenu;
    /** Arraylist of jpanels containing the listmodes. */
    private final List<JList> listModesPanels;
    /** JPanel used to show listmodespanels in. */
    private final JScrollPane listModesPanel;
    /** Add list mode button. */
    private final JButton addListModeButton;
    /** Remove list mode button. */
    private final JButton removeListModeButton;
    /** list modes available on this server. */
    private final char[] listModesArray;
    /** Modes on creation. */
    private final MapList<Character, ChannelListModeItem> existingListItems;
    /** Mode count label. */
    private final JLabel modeCount;
    /** Cell renderer. */
    private ListCellRenderer renderer;
    /** Extended info toggle. */
    private final JCheckBox toggle;

    /**
     * Creates a new instance of ChannelListModePane.
     *
     * @param channel Parent channel
     */
    public ChannelListModesPane(final Channel channel) {
        super();

        this.setOpaque(UIUtilities.getTabbedPaneOpaque());
        this.channel = channel;

        if (IdentityManager.getGlobalConfig().getOptionBool("general",
                "extendedListModes")) {
            renderer = new ExtendedListModeCellRenderer();
        } else {
            renderer = new ListModeCellRenderer();
        }
        listModesPanel = new JScrollPane();
        listModesPanels = new ArrayList<JList>();
        listModesArray =
                channel.getServer().getParser().getListChannelModes().toCharArray();
        existingListItems =
                new MapList<Character, ChannelListModeItem>();
        listModesMenu =
                new JComboBox(new DefaultComboBoxModel());
        addListModeButton = new JButton("Add");
        removeListModeButton = new JButton("Remove");
        removeListModeButton.setEnabled(false);
        modeCount = new JLabel();
        toggle = new JCheckBox("Show extended information",
                IdentityManager.getGlobalConfig().getOptionBool("general",
                "extendedListModes"));
        toggle.setOpaque(UIUtilities.getTabbedPaneOpaque());
        toggle.setMargin(new Insets(0, 0, 0, 0));
        toggle.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

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

        existingListItems.clear();

        for (int i = 0; i < listModesArray.length;
                i++) {
            final char mode = listModesArray[i];
            existingListItems.add(mode,
                    new ArrayList<ChannelListModeItem>(channel.getChannelInfo().
                    getListMode(mode)));
            final Collection<ChannelListModeItem> listItems =
                    channel.getChannelInfo().getListMode(mode);
            final DefaultListModel model =
                    (DefaultListModel) listModesPanels.get(i).getModel();

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
        if (listModesArray.length == 0) {
            listModesMenu.setEnabled(false);
            addListModeButton.setEnabled(false);
            return;
        } else {
            listModesMenu.setEnabled(true);
            addListModeButton.setEnabled(true);
        }

        final DefaultComboBoxModel model =
                (DefaultComboBoxModel) listModesMenu.getModel();
        for (char mode : listModesArray) {
            String modeText = mode + " list";
            if (channel.getConfigManager().hasOptionString("server", "mode" + mode)) {
                modeText = channel.getConfigManager().getOption("server",
                        "mode" + mode) + " list [+"+mode+"]";
            }
            model.addElement(modeText);

            final JList list =
                    new JList(new DefaultListModel());
            list.setCellRenderer(renderer);
            list.setVisibleRowCount(8);
            list.addListSelectionListener(this);

            listModesPanels.add(list);
        }
        if (listModesPanels.isEmpty()) {
            listModesPanel.setViewportView(new JPanel());
        } else {
            listModesPanel.setViewportView(listModesPanels.get(0));
        }
        updateModeCount();
        listModesPanel.setVisible(true);
    }

    /** Initialises the list modes panel. */
    private void initListModesPanel() {
        updateMenu();

        setLayout(new MigLayout("fill, wrap 1"));

        add(listModesMenu, "growx, pushx");
        add(listModesPanel, "grow, push");
        add(modeCount, "split 2, growx, pushx");
        add(toggle, "alignx center");
        add(addListModeButton, "split 2, growx, pushx");
        add(removeListModeButton, "growx, pushx");

        update();
        updateModeCount();
    }

    /** Initialises listeners for this dialog. */
    private void initListeners() {
        addListModeButton.addActionListener(this);
        removeListModeButton.addActionListener(this);
        listModesMenu.addActionListener(this);
        toggle.addActionListener(this);
        IdentityManager.getGlobalConfig().addChangeListener("general",
                "extendedListModes", this);

    }

    /** Sends the list modes to the server. */
    public void save() {
        final Map<ChannelListModeItem, Character> currentModes =
                new HashMap<ChannelListModeItem, Character>();
        final Map<ChannelListModeItem, Character> newModes =
                new HashMap<ChannelListModeItem, Character>();

        for (int i = 0; i < listModesArray.length;
                i++) {
            final char mode = listModesArray[i];
            final Enumeration<?> values =
                    ((DefaultListModel) listModesPanels.get(i).getModel()).elements();
            final List<ChannelListModeItem> listItems =
                    existingListItems.get(mode);

            for (ChannelListModeItem listItem : listItems) {
                currentModes.put(listItem, mode);
            }

            while (values.hasMoreElements()) {
                final ChannelListModeItem value =
                        (ChannelListModeItem) values.nextElement();
                newModes.put(value, mode);
            }
        }

        for (Entry<ChannelListModeItem, Character> entry : newModes.entrySet()) {
            if (currentModes.containsKey(entry.getKey())) {
                currentModes.remove(entry.getKey());
            } else {
                channel.getChannelInfo().
                        alterMode(true, entry.getValue(),
                        entry.getKey().getItem());
            }
        }

        for (Entry<ChannelListModeItem, Character> entry : currentModes.entrySet()) {
            channel.getChannelInfo().
                    alterMode(false, entry.getValue(), entry.getKey().getItem());
        }

        channel.getChannelInfo().flushModes();

        IdentityManager.getConfigIdentity().setOption("general",
                "extendedListModes", toggle.isSelected());
    }

    /** Adds a list mode. */
    private void addListMode() {
        final int selectedIndex = listModesMenu.getSelectedIndex();
        String modeText = String.valueOf(listModesArray[selectedIndex]);
        String modeMask;
        if (channel.getConfigManager().hasOptionString("server", "mode"
                + listModesArray[selectedIndex])) {
            modeText = channel.getConfigManager().
                    getOption("server", "mode" + listModesArray[selectedIndex]);
        }
        modeMask = JOptionPane.showInputDialog(listModesPanel,
                "Please enter the hostmask for the new " + modeText);
        if (modeMask != null && (!modeMask.isEmpty() || !modeMask.isEmpty())) {
            final DefaultListModel model =
                    (DefaultListModel) listModesPanels.get(selectedIndex).
                    getModel();
            model.addElement(new ChannelListModeItem(modeMask, "",
                    System.currentTimeMillis() / 1000));
        }
        updateModeCount();
    }

    /** Removes a list mode. */
    private void removeListMode() {
        final int selectedIndex = listModesMenu.getSelectedIndex();
        final JList list = listModesPanels.get(selectedIndex);
        for (Object mode : list.getSelectedValues()) {
            ((DefaultListModel) list.getModel()).removeElement(mode);
        }
        updateModeCount();
    }

    /** 
     * {@inheritDoc}
     * 
     * @param event Action event
     */
    @Override
    public void actionPerformed(final ActionEvent event) {
        if (listModesMenu.equals(event.getSource())) {
            final int selectedIndex = listModesMenu.getSelectedIndex();
            listModesPanel.setVisible(false);
            listModesPanel.setViewportView(listModesPanels.get(selectedIndex));
            listModesPanel.setVisible(true);
            updateModeCount();
        } else if (addListModeButton.equals(event.getSource())) {
            addListMode();
        } else if (removeListModeButton.equals(event.getSource())) {
            removeListMode();
        } else if (toggle.equals(event.getSource())) {
            if (toggle.isSelected()) {
                renderer = new ExtendedListModeCellRenderer();
            } else {
                renderer = new ListModeCellRenderer();
            }
            for (JList list : listModesPanels) {
                list.setCellRenderer(renderer);
            }
        }
    }

    /** 
     * {@inheritDoc}
     * 
     * @param event List selection event
     */
    @Override
    public void valueChanged(final ListSelectionEvent event) {
        if (!event.getValueIsAdjusting()) {
            final int selected =
                    ((JList) event.getSource()).getSelectedIndex();
            if (selected == -1) {
                removeListModeButton.setEnabled(false);
            } else {
                removeListModeButton.setEnabled(true);
            }
        }
    }

    /** Updates the mode count label. */
    private void updateModeCount() {
        if (listModesPanels.isEmpty()) {
            modeCount.setText(null);
            return;
        }
        
        final int selected = listModesMenu.getSelectedIndex();
        final int current = listModesPanels.get(selected).getModel().getSize();
        final int maxModes = channel.getServer().getParser().
                getMaxListModes(listModesArray[selected]);
        
        if (maxModes == -1) {
            modeCount.setText(current + " mode" + (current == 1 ? "" : "s") + " set");
        } else {
            modeCount.setText(current + " mode" + (current == 1 ? "" : "s")
                    + " set (maximum of " + maxModes + ")");
        }
    }

    /** {@inheritDoc} */
    @Override
    public void configChanged(final String domain, final String key) {
        if (IdentityManager.getGlobalConfig().getOptionBool("general",
                "extendedListModes")) {
            renderer = new ListModeCellRenderer();
        } else {
            renderer = new ExtendedListModeCellRenderer();
        }
        for (JList list : listModesPanels) {
            list.setCellRenderer(renderer);
        }
    }
}
