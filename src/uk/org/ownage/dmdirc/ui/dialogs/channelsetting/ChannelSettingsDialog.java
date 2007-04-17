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

package uk.org.ownage.dmdirc.ui.dialogs.channelsetting;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;

import uk.org.ownage.dmdirc.Channel;
import uk.org.ownage.dmdirc.parser.ChannelInfo;
import uk.org.ownage.dmdirc.parser.ChannelListModeItem;
import uk.org.ownage.dmdirc.parser.IRCParser;
import uk.org.ownage.dmdirc.ui.*;
import uk.org.ownage.dmdirc.ui.components.ParamModePanel;
import uk.org.ownage.dmdirc.ui.components.StandardDialog;

import static uk.org.ownage.dmdirc.ui.UIUtilities.*;

/**
 * Allows the user to modify channel settings (modes, topics, etc).
 * @author chris
 */
public class ChannelSettingsDialog extends StandardDialog
        implements ActionListener, KeyListener {
    
    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 5;
    
    /**
     * The channel object that this dialog belongs to.
     */
    private final Channel channel;
    
    /**
     * The checkboxes used for boolean modes.
     */
    private Map<String, JCheckBox> modeCheckBoxes;
    
    /**
     * The ParamModePanels used for parameter-requiring modes.
     */
    private Map<String, ParamModePanel> modeInputs;
    
    /**
     * Combox box used to switch between list modes.
     */
    private JComboBox listModesMenu;
    
    /**
     * Arraylist of jpanels containing the listmodes.
     */
    private List<JPanel> listModesPanels;
    
    /**
     * JPanel used to show listmodespanels in.
     */
    private JPanel listModesPanel;
    
    /**
     * add and remove list mode buttons.
     */
    private JButton addListModeButton, removeListModeButton;
    
    /**
     * list modes available on this server.
     */
    private char[] listModesArray;
    
    /**
     * the maximum length allowed for a topic.
     */
    private final int topicLengthMax;
    
    /**
     * label showing the number of characters left in a topic.
     */
    private JLabel topicLengthLabel;
    
    /**
     * Topic text entry text area.
     */
    private JTextArea topicText;
    
    /** Client settings panel */
    private ChannelSettingsPane channelSettingsPane;
    
    /**
     * Creates a new instance of ChannelSettingsDialog.
     * @param newChannel The channel object that we're editing settings for
     */
    public ChannelSettingsDialog(final Channel newChannel) {
        super(MainFrame.getMainFrame(), false);
        this.channel = newChannel;
        final Map<String, String> iSupport =
                channel.getServer().getParser().get005();
        if (iSupport.containsKey("TOPICLEN")) {
            topicLengthMax = Integer.parseInt(iSupport.get("TOPICLEN"));
        } else {
            topicLengthMax = 250;
        }
        
        initComponents();
        initListeners();
    }
    
    /** Initialises the main UI components. */
    private void initComponents() {
        final GridBagConstraints constraints = new GridBagConstraints();
        final JTabbedPane tabbedPane = new JTabbedPane();
        
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        getContentPane().setLayout(new GridBagLayout());
        setTitle("Channel settings for " + channel);
        setResizable(false);
        
        final JButton button1 = new JButton();
        final JButton button2 = new JButton();
        
        orderButtons(button1, button2);
        
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.gridwidth = 3;
        constraints.weightx = 1.0;
        constraints.weighty = 1.0;
        constraints.fill = GridBagConstraints.BOTH;
        constraints.insets = new Insets(LARGE_BORDER, LARGE_BORDER,
                LARGE_BORDER, LARGE_BORDER);
        getContentPane().add(tabbedPane, constraints);
        
        constraints.weighty = 0.0;
        constraints.gridx = 0;
        constraints.gridy = 1;
        constraints.gridwidth = 1;
        getContentPane().add(Box.createHorizontalGlue(), constraints);
        
        constraints.weightx = 0.0;
        constraints.insets.set(0, LARGE_BORDER, LARGE_BORDER, LARGE_BORDER);
        constraints.gridx = 1;
        constraints.gridy = 1;
        constraints.anchor = GridBagConstraints.EAST;
        constraints.fill = GridBagConstraints.NONE;
        getContentPane().add(getLeftButton(), constraints);
        
        constraints.gridx = 2;
        getContentPane().add(getRightButton(), constraints);
        
        initIrcTab(tabbedPane);
        
        initListModesTab(tabbedPane);
        
        initSettingsTab(tabbedPane);
        
        pack();
    }
    
    /**
     * Initialises the IRC Settings tab.
     * @param tabbedPane The pane to add the IRC Settings tab to
     */
    private void initIrcTab(final JTabbedPane tabbedPane) {
        final JPanel settingsPanel = new JPanel(new GridBagLayout());
        
        tabbedPane.addTab("IRC Settings", settingsPanel);
        
        settingsPanel.setBorder(new EmptyBorder(LARGE_BORDER, LARGE_BORDER,
                LARGE_BORDER, LARGE_BORDER));
        
        initModesPanel(settingsPanel);
        initTopicsPanel(settingsPanel);
    }
    
    /**
     * Initialises the IRC Settings tab.
     * @param tabbedPane The pane to add the IRC Settings tab to
     */
    private void initListModesTab(final JTabbedPane tabbedPane) {
        final JPanel listModesMainPanel = new JPanel(new GridBagLayout());
        
        tabbedPane.addTab("List Modes", listModesMainPanel);
        
        listModesMainPanel.setBorder(new EmptyBorder(LARGE_BORDER, LARGE_BORDER,
                LARGE_BORDER, LARGE_BORDER));
        
        initListModesPanel(listModesMainPanel);
    }
    
    /**
     * Initialises the channel Settings (identities) tab.
     * @param tabbedPane The pane to add the IRC Settings tab to
     */
    private void initSettingsTab(final JTabbedPane tabbedPane) {
        final JPanel settingsPanel = new JPanel(new BorderLayout());
        
        tabbedPane.addTab("Client Settings", settingsPanel);
        
        settingsPanel.setBorder(new EmptyBorder(LARGE_BORDER, LARGE_BORDER,
                LARGE_BORDER, LARGE_BORDER));
        
        initSettingsPanel(settingsPanel);
    }
    
    /**
     * Initialises the modes panel.
     * @param parent The panel to add the modes panel to
     */
    private void initModesPanel(final JPanel parent) {
        final GridBagConstraints constraints = new GridBagConstraints();
        final JPanel modesPanel = new JPanel(new GridBagLayout());
        final IRCParser parser = channel.getServer().getParser();
        final String booleanModes = parser.getBoolChanModes();
        final String ourBooleanModes = channel.getChannelInfo().getModeStr();
        final String paramModes = parser.getSetOnlyChanModes()
        + parser.getSetUnsetChanModes();
        
        constraints.fill = GridBagConstraints.BOTH;
        constraints.anchor = GridBagConstraints.NORTH;
        constraints.weightx = 1.0;
        constraints.weighty = 1.0;
        
        modesPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                new EtchedBorder(), "Channel Modes"),
                new EmptyBorder(LARGE_BORDER, LARGE_BORDER, LARGE_BORDER,
                LARGE_BORDER)));
        parent.add(modesPanel, constraints);
        
        modeCheckBoxes = new Hashtable<String, JCheckBox>();
        
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.anchor = GridBagConstraints.WEST;
        constraints.weightx = 1.0;
        constraints.weighty = 1.0;
        
        // Lay out all the boolean mode checkboxes
        for (int i = 0; i < booleanModes.length(); i++) {
            final String mode = booleanModes.substring(i, i + 1);
            final char modeChar = mode.toCharArray()[0];
            final boolean state = ourBooleanModes.split(" ")[0]
                    .contains(mode.subSequence(0, 1));
            String text = "Mode " + mode;
            
            if (channel.getConfigManager().hasOption("server", "mode" + mode)) {
                text = channel.getConfigManager().getOption("server", "mode" + mode);
            }
            
            final JCheckBox checkBox = new JCheckBox(text, state);
            checkBox.setBorder(new EmptyBorder(5, 0, 0, 10));
            modesPanel.add(checkBox, constraints);
            
            constraints.gridx++;
            if (constraints.gridx == 2) {
                constraints.gridy++;
                constraints.gridx = 0;
            }
            
            if (!channel.getServer().getParser().isUserSettable(modeChar)) {
                checkBox.setEnabled(false);
            }
            modeCheckBoxes.put(mode, checkBox);
        }
        
        // Lay out all the parameter-requiring modes
        modeInputs = new Hashtable<String, ParamModePanel>();
        
        constraints.gridwidth = 2;
        
        if (constraints.gridx != 0) {
            constraints.gridy++;
            constraints.gridx = 0;
        }
        
        for (int i = 0; i < paramModes.length(); i++) {
            final String mode = paramModes.substring(i, i + 1);
            final String value = channel.getChannelInfo()
            .getModeParam(mode.charAt(0));
            final boolean state = ourBooleanModes.split(" ")[0]
                    .contains(mode.subSequence(0, 1));
            
            final ParamModePanel panel = new ParamModePanel(mode, state, value,
                    channel.getConfigManager());
            panel.setBorder(new EmptyBorder(5, 0, 0, 0));
            modesPanel.add(panel, constraints);
            
            modeInputs.put(mode, panel);
            
            constraints.gridy++;
        }
        
    }
    
    /**
     * Initialises the topic panel.
     * @param parent The panel to add the topics panel to
     */
    private void initTopicsPanel(final JPanel parent) {
        final GridBagConstraints constraints = new GridBagConstraints();
        final JPanel topicsPanel = new JPanel(new GridBagLayout());
        final JLabel topicWho = new JLabel();
        final String topic = channel.getChannelInfo().getTopic();
        final JScrollPane scrollPane;
        topicLengthLabel = new JLabel();
        topicText = new JTextArea(100, 4);
        
        constraints.fill = GridBagConstraints.BOTH;
        constraints.anchor = GridBagConstraints.NORTH;
        constraints.gridy = 1;
        constraints.weightx = 1.0;
        
        topicsPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                new EtchedBorder(), "Channel Topic"),
                new EmptyBorder(LARGE_BORDER, LARGE_BORDER, LARGE_BORDER,
                LARGE_BORDER)));
        parent.add(topicsPanel, constraints);
        
        constraints.gridy = 2;
        topicText.setText(channel.getChannelInfo().getTopic());
        topicText.setLineWrap(true);
        topicText.addKeyListener(this);
        topicText.setWrapStyleWord(true);
        scrollPane = new JScrollPane(topicText);
        scrollPane.setMinimumSize(new Dimension(100,
                getFont().getSize() * 4));
        scrollPane.setPreferredSize(new Dimension(100,
                getFont().getSize() * 4));
        topicsPanel.add(scrollPane, constraints);
        
        constraints.gridy = 3;
        topicLengthLabel.setText(topicLengthMax - topicText.getText().length()
        + " of " + topicLengthMax + " available");
        topicsPanel.add(topicLengthLabel, constraints);
        
        constraints.fill = GridBagConstraints.BOTH;
        constraints.anchor = GridBagConstraints.LINE_START;
        constraints.gridy = 4;
        topicWho.setSize(30, 0);
        topicWho.setBorder(new EmptyBorder(SMALL_BORDER, 0, 0, 0));
        if ("".equals(topic)) {
            topicWho.setText("No topic set.");
        } else {
            topicWho.setText("<html>Set by "
                    + channel.getChannelInfo().getTopicUser() + "<br> on "
                    + new Date(1000 * channel.getChannelInfo().getTopicTime())
                    + "</html>");
        }
        topicsPanel.add(topicWho, constraints);
    }
    
    /**
     * Initialises the list modes panel.
     * @param parent The panel to add the list modes panel to
     */
    private void initListModesPanel(final JPanel parent) {
        //TODO use a cardlayout
        final GridBagConstraints constraints = new GridBagConstraints();
        listModesArray = channel.getServer().getParser()
        .getListChanModes().toCharArray();
        final ChannelInfo channelInfo = channel.getChannelInfo();
        final ArrayList<String> listModesList = new ArrayList<String>();
        
        
        JList list;
        DefaultListModel listModel;
        JPanel panel;
        ArrayList<ChannelListModeItem> listItems;
        
        
        for (char mode : listModesArray) {
            String modeText = mode + " list";
            if (channel.getConfigManager().hasOption("server", "mode" + mode)) {
                modeText = channel.getConfigManager().getOption("server", "mode" + mode) + " list";
            }
            listModesList.add(modeText);
        }
        
        addListModeButton = new JButton("Add");
        removeListModeButton = new JButton("Remove");
        listModesPanels = new ArrayList<JPanel>();
        listModesPanel = new JPanel(new BorderLayout());
        listModesMenu = new JComboBox(listModesList.toArray());
        
        for (char mode : listModesArray) {
            panel = new JPanel(new BorderLayout());
            
            listModel = new DefaultListModel();
            list = new JList(listModel);
            list.setBorder(new EtchedBorder());
            
            listItems = channelInfo.getListModeParam(mode);
            
            for (ChannelListModeItem listItem : listItems) {
                listModel.addElement(listItem);
            }
            
            panel.add(list, BorderLayout.CENTER);
            panel.setBorder(new EmptyBorder(SMALL_BORDER, 0, SMALL_BORDER, 0));
            
            listModesPanels.add(panel);
        }
        
        listModesPanel.add(listModesPanels.get(0), BorderLayout.CENTER);
        listModesPanel.setPreferredSize(parent.getPreferredSize());
        
        constraints.weightx = 1.0;
        constraints.weighty = 0.0;
        constraints.fill = GridBagConstraints.BOTH;
        constraints.gridx = 1;
        constraints.gridy = 1;
        constraints.gridwidth = 2;
        parent.add(listModesMenu, constraints);
        constraints.weightx = 1.0;
        constraints.weighty = 1.0;
        constraints.gridy = 2;
        parent.add(listModesPanel, constraints);
        constraints.weightx = 0.5;
        constraints.weighty = 0;
        constraints.gridx = 1;
        constraints.gridy = 3;
        constraints.gridwidth = 1;
        parent.add(addListModeButton, constraints);
        constraints.gridx = 2;
        parent.add(removeListModeButton, constraints);
    }
    
    /**
     * Initialises the channel settings.
     * @param parent The panel to add the channel settings panel to
     */
    private void initSettingsPanel(final JPanel parent) {
        channelSettingsPane = new ChannelSettingsPane(parent, channel);
        parent.add(channelSettingsPane, BorderLayout.CENTER);
    }
    
    /** Initialises listeners for this dialog. */
    private void initListeners() {
        getOkButton().addActionListener(this);
        getCancelButton().addActionListener(this);
        addListModeButton.addActionListener(this);
        removeListModeButton.addActionListener(this);
        listModesMenu.addActionListener(this);
    }
    
    /**
     * Called whenever the user clicks on one of the two buttons.
     * @param actionEvent Event generated by this action
     */
    public final void actionPerformed(final ActionEvent actionEvent) {
        if (getOkButton().equals(actionEvent.getSource())) {
            setChangedBooleanModes();
            setChangedTopic();
            setVisible(false);
        } else if (getCancelButton().equals(actionEvent.getSource())) {
            setVisible(false);
        } else if (listModesMenu.equals(actionEvent.getSource())) {
            listModesPanel.removeAll();
            listModesPanel.add(listModesPanels.get(listModesMenu
                    .getSelectedIndex()), BorderLayout.CENTER);
        } else if (addListModeButton.equals(actionEvent.getSource())) {
            final int selectedIndex = listModesMenu.getSelectedIndex();
            String modeText = "" + listModesArray[selectedIndex];
            String modeMask;
            if (channel.getConfigManager().hasOption("server", "mode"
                    + listModesArray[selectedIndex])) {
                modeText = channel.getConfigManager().getOption("server", "mode"
                        + listModesArray[selectedIndex]);
            }
            modeMask =  JOptionPane.showInputDialog(listModesPanel,
                    "Please enter the hostmask for the new " + modeText);
            if (modeMask != null && (!modeMask.equals("")
            || modeMask.length() > 0)) {
                channel.getChannelInfo().alterMode(true,
                        listModesArray[selectedIndex],
                        modeMask);
                channel.getChannelInfo().sendModes();
            }
        } else if (removeListModeButton.equals(actionEvent.getSource())) {
            final int selectedIndex = listModesMenu.getSelectedIndex();
            final JList list = (JList) listModesPanels.get(selectedIndex)
            .getComponent(0);
            for (Object mode : list.getSelectedValues()) {
                ((DefaultListModel) list.getModel()).removeElement(mode);
                channel.getChannelInfo().alterMode(false,
                        listModesArray[selectedIndex],
                        mode.toString());
                
            }
            channel.getChannelInfo().sendModes();
        }
    }
    
    
    /**
     * processes the topic and changes it if necessary.
     */
    private void setChangedTopic() {
        if (!channel.getChannelInfo().getTopic().equals(topicText.getText())) {
            channel.getServer().getParser().sendLine("TOPIC "
                    + channel.getChannelInfo().getName() + "" + " :"
                    + topicText.getText());
        }
    }
    
    /**
     * Processes the channel settings dialog and constructs a mode string for
     * changed modes, then sends this to the server.
     */
    private void setChangedBooleanModes() {
        boolean changed = false;
        final IRCParser parser = channel.getServer().getParser();
        final String booleanModes = parser.getBoolChanModes();
        final String ourBooleanModes = channel.getChannelInfo().getModeStr();
        final String paramModes = parser.getSetOnlyChanModes()
        + parser.getSetUnsetChanModes();
        
        for (int i = 0; i < booleanModes.length(); i++) {
            final String mode = booleanModes.substring(i, i + 1);
            final boolean state = ourBooleanModes.split(" ")[0]
                    .contains(mode.subSequence(0, 1));
            
            if (state != modeCheckBoxes.get(mode).isSelected()) {
                changed = true;
                channel.getChannelInfo().alterMode(
                        modeCheckBoxes.get(mode).isSelected(),
                        mode.toCharArray()[0],
                        "");
            }
        }
        
        for (int i = 0; i < paramModes.length(); i++) {
            final String mode = paramModes.substring(i, i + 1);
            final String value = channel.getChannelInfo()
            .getModeParam(mode.charAt(0));
            final boolean state = ourBooleanModes.split(" ")[0]
                    .contains(mode.subSequence(0, 1));
            final ParamModePanel paramModePanel = modeInputs.get(mode);
            
            if (state != paramModePanel.getState()
            || !value.equals(paramModePanel.getValue())) {
                changed = true;
                if (paramModePanel.getValue().contains(" ")) {
                    channel.getChannelInfo().alterMode(
                            paramModePanel.getState(), mode.toCharArray()[0],
                            paramModePanel.getValue().substring(0,
                            paramModePanel.getValue().indexOf(" ")));
                } else {
                    channel.getChannelInfo().alterMode(
                            paramModePanel.getState(), mode.toCharArray()[0],
                            paramModePanel.getValue());
                }
            }
        }
        if (changed) {
            channel.getChannelInfo().sendModes();
        }
    }
    
    /**
     * Handles key typed events in the dialog.
     *
     * @param keyEvent Key typed KeyEvent
     */
    public final void keyTyped(final KeyEvent keyEvent) {
        if (topicText.getText().length() >= topicLengthMax
                && (keyEvent.getKeyCode() != KeyEvent.VK_BACK_SPACE
                && keyEvent.getKeyCode() != KeyEvent.VK_DELETE)) {
            keyEvent.consume();
        }
        topicLengthLabel.setText(topicLengthMax - topicText.getText().length()
        + " of " + topicLengthMax + " available");
    }
    
    /**
     * Handles key pressed events in the dialog.
     *
     * @param keyEvent Key typed KeyEvent
     */
    public final void keyPressed(final KeyEvent keyEvent) {
        if (keyEvent.getKeyCode() == KeyEvent.VK_ENTER && keyEvent.getSource() == topicText) {
            keyEvent.consume();
            setChangedBooleanModes();
            setChangedTopic();
            setVisible(false);
        }
    }
    
    /** {@inheritDoc}. */
    public final void keyReleased(final KeyEvent keyEvent) {
        //ignore, unused.
    }
    
}
