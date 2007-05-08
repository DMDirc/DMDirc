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

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.plaf.basic.BasicSplitPaneDivider;
import javax.swing.plaf.basic.BasicSplitPaneUI;

import uk.org.ownage.dmdirc.Channel;
import uk.org.ownage.dmdirc.Config;
import uk.org.ownage.dmdirc.FrameContainer;
import uk.org.ownage.dmdirc.Server;
import uk.org.ownage.dmdirc.commandparser.ChannelCommand;
import uk.org.ownage.dmdirc.commandparser.ChannelCommandParser;
import uk.org.ownage.dmdirc.commandparser.Command;
import uk.org.ownage.dmdirc.commandparser.CommandManager;
import uk.org.ownage.dmdirc.commandparser.CommandParser;
import uk.org.ownage.dmdirc.commandparser.ServerCommand;
import uk.org.ownage.dmdirc.identities.ConfigManager;
import uk.org.ownage.dmdirc.parser.ChannelClientInfo;
import uk.org.ownage.dmdirc.ui.components.Frame;
import uk.org.ownage.dmdirc.ui.dialogs.channelsetting.ChannelSettingsDialog;
import uk.org.ownage.dmdirc.ui.input.InputHandler;

import static uk.org.ownage.dmdirc.ui.UIUtilities.SMALL_BORDER;

/**
 * The channel frame is the GUI component that represents a channel to the user.
 */
public final class ChannelFrame extends Frame implements MouseListener,
        ActionListener {
    
    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 7;
    
    /** max length a line can be. */
    private final int maxLineLength;
    
    /** The nick list model used for this channel's nickname list. */
    private NicklistListModel nicklistModel;
    
    /** This channel's command parser. */
    private final ChannelCommandParser commandParser;
    
    /** Nick list. */
    private JList nickList;
    
    /** split pane. */
    private JSplitPane splitPane;
    
    /** popup menu item. */
    private JMenuItem settingsMI;
    
    /** The channel object that owns this frame. */
    private final Channel parent;
    
    /** nicklist popup menu. */
    private JPopupMenu nicklistPopup;
    
    /** Command map. */
    private Map<String, Command> commands;
    
    /**
     * Creates a new instance of ChannelFrame. Sets up callbacks and handlers,
     * and default options for the form.
     * @param owner The Channel object that owns this frame
     */
    public ChannelFrame(final Channel owner) {
        super(owner);
        
        parent = owner;
        
        commands = new HashMap<String, Command>();
        
        maxLineLength = this.getServer().getParser().getMaxLength("PRIVMSG", this.getFrameParent().toString());
        
        initComponents();
        
        nickList.setBackground(owner.getConfigManager().getOptionColour("ui", "nicklistbackgroundcolour",
                owner.getConfigManager().getOptionColour("ui", "backgroundcolour", Color.WHITE)));
        nickList.setForeground(owner.getConfigManager().getOptionColour("ui", "nicklistforegroundcolour",
                owner.getConfigManager().getOptionColour("ui", "foregroundcolour", Color.BLACK)));
        
        commandParser = new ChannelCommandParser(((Channel) getFrameParent()).
                getServer(), (Channel) getFrameParent());
        
        setInputHandler(new InputHandler(getInputField(), commandParser, this));
    }
    
    /**
     * Retrieves the command Parser for this command window.
     * @return This window's command Parser
     */
    public CommandParser getCommandParser() {
        return commandParser;
    }
    
    /**
     * Updates the list of clients on this channel.
     * @param newNames The new list of clients
     */
    public void updateNames(final List<ChannelClientInfo> newNames) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                nicklistModel.replace(newNames);
            }
        });
    }
    
    /**
     * Has the nick list update, to take into account mode changes.
     */
    public void updateNames() {
        nicklistModel.sort();
    }
    
    /**
     * Adds a client to this channels' nicklist.
     * @param newName the new client to be added
     */
    public void addName(final ChannelClientInfo newName) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                nicklistModel.add(newName);
            }
        });
    }
    
    /**
     * Removes a client from this channels' nicklist.
     * @param name the client to be deleted
     */
    public void removeName(final ChannelClientInfo name) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                nicklistModel.remove(name);
            }
        });
    }
    
    /**
     * Retrieves the config manager for this command window.
     * @return This window's config manager
     */
    public ConfigManager getConfigManager() {
        return parent.getConfigManager();
    }
    
    /**
     * Retrieves the server associated with this command window.
     * @return This window's associated server instance
     */
    public Server getServer() {
        return parent.getServer();
    }
    
    /** {@inheritDoc} */
    public FrameContainer getContainer() {
        return parent;
    }
    
    /**
     * Retrieves this channel frame's nicklist component.
     * @return This channel's nicklist
     */
    public JList getNickList() {
        return nickList;
    }
    
    /**
     * Initialises the compoents in this frame.
     */
    private void initComponents() {
        settingsMI = new JMenuItem("Settings");
        settingsMI.addActionListener(this);
        getPopup().addSeparator();
        getPopup().add(settingsMI);
        
        nicklistPopup = new JPopupMenu();
        popuplateNicklistPopup();
        
        
        final GridBagConstraints constraints = new GridBagConstraints();
        splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        
        final JScrollPane nickScrollPane = new JScrollPane();
        nickList = new JList();
        nickList.setCellRenderer(new NicklistRenderer(parent.getConfigManager()));
        nickList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        
        nickList.addMouseListener(this);
        
        splitPane.setBorder(null);
        final BasicSplitPaneDivider divider =
                ((BasicSplitPaneUI) splitPane.getUI()).getDivider();
        if (divider != null) {
            divider.setBorder(null);
        }
        
        getScrollPane().setVerticalScrollBarPolicy(
                ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        getTextPane().setEditable(false);
        getScrollPane().setViewportView(getTextPane());
        
        nicklistModel = new NicklistListModel();
        
        nickList.setModel(nicklistModel);
        nickScrollPane.setViewportView(nickList);
        
        getContentPane().setLayout(new GridBagLayout());
        constraints.weightx = 1.0;
        constraints.weighty = 1.0;
        constraints.fill = GridBagConstraints.BOTH;
        constraints.insets = new Insets(0, 0, 0, 0);
        getContentPane().add(splitPane, constraints);
        
        constraints.weighty = 0.0;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.gridy = 1;
        getContentPane().add(getSearchBar(), constraints);
        
        constraints.gridy = 2;
        constraints.insets = new Insets(SMALL_BORDER, 0, 0, 0);
        getContentPane().add(getInputPanel(), constraints);
        
        splitPane.setLeftComponent(getScrollPane());
        splitPane.setRightComponent(nickScrollPane);
        
        splitPane.setResizeWeight(1.0);
        if (Config.getOptionBool("ui", "maximisewindows")) {
            splitPane.setDividerLocation(MainFrame.getMainFrame().getWidth() - 325);
        } else {
            splitPane.setDividerLocation(MainFrame.getMainFrame().getWidth() / 2 - 150);
        }
        splitPane.setDividerSize(5);
        splitPane.setContinuousLayout(true);
        
        pack();
    }
    
    /**
     * {@inheritDoc}.
     */
    public void actionPerformed(final ActionEvent actionEvent) {
        if (commands.containsKey(actionEvent.getActionCommand())) {
            Command command = commands.get(actionEvent.getActionCommand());
            for (Object nickname : nickList.getSelectedValues()) {
                if (command instanceof ChannelCommand) {
                    ((ChannelCommand) commands.get(actionEvent.getActionCommand())).
                            execute(this, this.getServer(),
                            (Channel)this.getContainer(), ((ChannelClientInfo) nickname).getNickname());
                } else if (command instanceof ServerCommand) {
                    ((ServerCommand) commands.get(actionEvent.getActionCommand())).
                            execute(this, this.getServer(), ((ChannelClientInfo) nickname).getNickname());
                }
            }
        }
        super.actionPerformed(actionEvent);
        if (actionEvent.getSource() == settingsMI) {
            new ChannelSettingsDialog((Channel) getFrameParent()).setVisible(true);
        }
    }
    
    /** Popuplates the nicklist popup. */
    private void popuplateNicklistPopup() {
        nicklistPopup.removeAll();
        commands.clear();
        
        List<Command> commandList = CommandManager.getNicklistCommands();
        for (Command command : commandList) {
            commands.put(command.getName(), command);
            JMenuItem mi = new JMenuItem(command.getName().substring(0, 1).
                    toUpperCase(Locale.getDefault()) + command.getName().substring(1));
            mi.setActionCommand(command.getName());
            mi.addActionListener(this);
            nicklistPopup.add(mi);
        }
    }
    
    /**
     * Returns the splitpane.
     * @return nicklist JSplitPane
     */
    public JSplitPane getSplitPane() {
        return splitPane;
    }
    
    /** {@inheritDoc}. */
    public void sendLine(final String line) {
        this.parent.sendLine(line);
        this.getInputHandler().addToBuffer(line);
    }
    
    /** {@inheritDoc}. */
    public int getMaxLineLength() {
        return maxLineLength;
    }
    
    /** {@inheritDoc}. */
    public void actionEvent(final ActionEvent actionEvent) {
        System.out.println(actionEvent.getActionCommand());
    }
    
    /**
     * Checks for url's, channels and nicknames. {@inheritDoc}
     */
    public void mouseClicked(final MouseEvent mouseEvent) {
        processMouseEvent(mouseEvent);
    }
    
    /**
     * Not needed for this class. {@inheritDoc}
     */
    public void mousePressed(final MouseEvent mouseEvent) {
        processMouseEvent(mouseEvent);
    }
    
    /**
     * Not needed for this class. {@inheritDoc}
     */
    public void mouseReleased(final MouseEvent mouseEvent) {
        processMouseEvent(mouseEvent);
    }
    
    /**
     * Not needed for this class. {@inheritDoc}
     */
    public void mouseEntered(final MouseEvent mouseEvent) {
        //Ignore.
    }
    
    /**
     * Not needed for this class. {@inheritDoc}
     */
    public void mouseExited(final MouseEvent mouseEvent) {
        //Ignore.
    }
    
    /**
     * Processes every mouse button event to check for a popup trigger.
     * @param e mouse event
     */
    public void processMouseEvent(final MouseEvent e) {
        if (e.getSource() == nickList && nickList.getMousePosition() != null) {
            boolean showMenu = false;
            for (int i = 0; i < nickList.getModel().getSize(); i++) {
                if (nickList.getCellBounds(i, i).contains(nickList.getMousePosition())
                && nickList.isSelectedIndex(i)) {
                    showMenu = true;
                    break;
                }
            }
            if (!showMenu) {
                for (int i = 0; i < nickList.getModel().getSize(); i++) {
                    if (nickList.getCellBounds(i, i).contains(nickList.getMousePosition())) {
                        nickList.setSelectedIndex(i);
                        showMenu = true;
                        break;
                    }
                }
            }
            if (showMenu) {
                if (e.isPopupTrigger()) {
                    final Point point = getMousePosition();
                    popuplateNicklistPopup();
                    nicklistPopup.show(this, (int) point.getX(), (int) point.getY());
                }
            } else {
                nickList.clearSelection();
            }
        } else {
            super.processMouseEvent(e);
        }
    }
}
