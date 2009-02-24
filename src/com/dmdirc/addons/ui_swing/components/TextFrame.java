/*
 * Copyright (c) 2006-2009 Chris Smith, Shane Mc Cormack, Gregory Holmes
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

package com.dmdirc.addons.ui_swing.components;

import com.dmdirc.FrameContainer;
import com.dmdirc.Main;
import com.dmdirc.actions.ActionManager;
import com.dmdirc.actions.CoreActionType;
import com.dmdirc.addons.ui_swing.SwingController;
import com.dmdirc.addons.ui_swing.UIUtilities;
import com.dmdirc.addons.ui_swing.actions.ChannelCopyAction;
import com.dmdirc.addons.ui_swing.actions.CommandAction;
import com.dmdirc.addons.ui_swing.actions.HyperlinkCopyAction;
import com.dmdirc.addons.ui_swing.actions.NicknameCopyAction;
import com.dmdirc.addons.ui_swing.actions.SearchAction;
import com.dmdirc.addons.ui_swing.actions.TextPaneCopyAction;
import com.dmdirc.addons.ui_swing.textpane.ClickType;
import com.dmdirc.addons.ui_swing.textpane.Line;
import com.dmdirc.addons.ui_swing.textpane.LineInfo;
import com.dmdirc.addons.ui_swing.textpane.TextPane;
import com.dmdirc.addons.ui_swing.textpane.TextPanePageDownAction;
import com.dmdirc.addons.ui_swing.textpane.TextPanePageUpAction;
import com.dmdirc.commandparser.PopupManager;
import com.dmdirc.commandparser.PopupMenu;
import com.dmdirc.commandparser.PopupMenuItem;
import com.dmdirc.commandparser.PopupType;
import com.dmdirc.commandparser.parsers.GlobalCommandParser;
import com.dmdirc.util.StringTranscoder;
import com.dmdirc.config.ConfigManager;
import com.dmdirc.interfaces.ConfigChangeListener;
import com.dmdirc.interfaces.IconChangeListener;
import com.dmdirc.logger.ErrorLevel;
import com.dmdirc.logger.Logger;
import com.dmdirc.ui.WindowManager;
import com.dmdirc.ui.interfaces.InputWindow;
import com.dmdirc.ui.interfaces.Window;
import com.dmdirc.ui.messages.Formatter;
import com.dmdirc.util.URLHandler;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.UnsupportedCharsetException;
import java.util.Date;

import java.util.LinkedList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JInternalFrame;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;
import javax.swing.plaf.basic.BasicInternalFrameUI;
import javax.swing.plaf.synth.SynthLookAndFeel;

/**
 * Implements a generic (internal) frame.
 */
public abstract class TextFrame extends JInternalFrame implements Window,
        PropertyChangeListener, InternalFrameListener,
        MouseListener, KeyListener, ConfigChangeListener {

    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 5;
    /** The channel object that owns this frame. */
    protected final FrameContainer parent;
    /** Frame output pane. */
    private TextPane textPane;
    /** search bar. */
    private SwingSearchBar searchBar;
    /** String transcoder. */
    private StringTranscoder transcoder;
    /** Frame buffer size. */
    private int frameBufferSize;
    /** Quick copy? */
    private boolean quickCopy;
    /** Are we closing? */
    private boolean closing = false;
    /** Input window for popup commands. */
    private Window inputWindow;

    /** Click types. */
    public enum MouseClickType {

        /** Clicked. */
        CLICKED,
        /** Released. */
        RELEASED,
        /** Pressed. */
        PRESSED,
    }

    /**
     * Creates a new instance of Frame.
     *
     * @param owner FrameContainer owning this frame.
     */
    public TextFrame(final FrameContainer owner) {
        super();
        final ConfigManager config = owner.getConfigManager();
        final Boolean pref = config.getOptionBool("ui", "maximisewindows");
        frameBufferSize = config.getOptionInt("ui", "frameBufferSize");
        quickCopy = config.getOptionBool("ui", "quickCopy");
        parent = owner;

        setFrameIcon(owner.getIcon());
        owner.addIconChangeListener(new IconChangeListener() {

            /** {@inheritDoc} */
            @Override
            public void iconChanged(final Window window, final Icon icon) {
                setFrameIcon(icon);
            }
        });

        try {
            transcoder = new StringTranscoder(Charset.forName(
                    config.getOption("channel", "encoding")));
        } catch (UnsupportedCharsetException ex) {
            transcoder = new StringTranscoder(Charset.forName("UTF-8"));
        } catch (IllegalCharsetNameException ex) {
            transcoder = new StringTranscoder(Charset.forName("UTF-8"));
        } catch (IllegalArgumentException ex) {
            transcoder = new StringTranscoder(Charset.forName("UTF-8"));
        }

        inputWindow = this;
        while (!(inputWindow instanceof InputWindow) && inputWindow != null) {
            inputWindow = WindowManager.getParent(inputWindow);
        }

        initComponents();
        setMaximizable(true);
        setClosable(true);
        setResizable(true);
        setIconifiable(true);
        setFocusable(true);
        setPreferredSize(new Dimension(SwingController.getMainFrame().getWidth() /
                2, SwingController.getMainFrame().getHeight() / 3));
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        addPropertyChangeListener("maximum", this);
        addPropertyChangeListener("UI", this);
        addInternalFrameListener(this);

        getTextPane().setBackground(config.getOptionColour("ui",
                "backgroundcolour"));
        getTextPane().setForeground(config.getOptionColour("ui",
                "foregroundcolour"));

        config.addChangeListener("ui", "foregroundcolour", this);
        config.addChangeListener("ui", "backgroundcolour", this);
        config.addChangeListener("ui", "quickCopy", this);
        config.addChangeListener("ui", "frameBufferSize", this);

        if (pref || Main.getUI().getMainWindow().getMaximised()) {
            hideTitlebar();
        }
    }

    /** {@inheritDoc} */
    @Override
    public void setTitle(final String title) {
        SwingUtilities.invokeLater(new Runnable() {

            /** {@inheritDoc} */
            @Override
            public void run() {
                TextFrame.super.setTitle(title);
            }
        });
    }

    /** {@inheritDoc} */
    @Override
    public void open() {
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                setVisible(true);
            }
        });
    }

    /** {@inheritDoc} */
    @Override
    public final void addLine(final String line, final boolean timestamp) {
        final String encodedLine = transcoder.decode(line);
        SwingUtilities.invokeLater(new Runnable() {

            /** {@inheritDoc} */
            @Override
            public void run() {
                final List<Line> lines = new LinkedList<Line>();
                for (final String myLine : encodedLine.split("\n")) {
                    if (timestamp) {
                        lines.add(new Line(new String[]{
                            Formatter.formatMessage(getConfigManager(),
                            "timestamp", new Date()), myLine,
                        }, getConfigManager()));
                    } else {
                        lines.add(new Line(new String[]{myLine,}, 
                                getConfigManager()));
                    }

                    new LoggingSwingWorker() {

                        /** {@inheritDoc} */
                        @Override
                        protected Object doInBackground() throws Exception {
                            ActionManager.processEvent(CoreActionType.CLIENT_LINE_ADDED,
                            null, getContainer(), myLine);
                            return null;
                        }
                    }.execute();
                }
                
                textPane.getDocument().addText(lines);

                if (frameBufferSize > 0) {
                    textPane.trim(frameBufferSize);
                }
            }
        });
    }

    /** {@inheritDoc} */
    @Override
    public final void addLine(final String messageType,
            final Object... args) {
        if (!messageType.isEmpty()) {
            addLine(Formatter.formatMessage(getConfigManager(), messageType,
                    args), true);
        }

    }

    /** {@inheritDoc} */
    @Override
    public final void addLine(final StringBuffer messageType,
            final Object... args) {
        if (messageType != null) {
            addLine(messageType.toString(), args);
        }

    }

    /** {@inheritDoc} */
    @Override
    public final void clear() {
        SwingUtilities.invokeLater(new Runnable() {

            /** {@inheritDoc} */
            @Override
            public void run() {
                getTextPane().clear();
            }
        });
    }

    /**
     * Initialises the components for this frame.
     */
    private void initComponents() {
        setTextPane(new TextPane(getContainer()));

        getTextPane().addMouseListener(this);
        getTextPane().addKeyListener(this);

        searchBar =
                new SwingSearchBar(this);
        searchBar.setVisible(false);
        searchBar.addKeyListener(this);

        getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).
                put(KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_UP, 0),
                "pageUpAction");

        getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).
                put(KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_DOWN, 0),
                "pageDownAction");

        getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).
                put(KeyStroke.getKeyStroke(KeyEvent.VK_F3, 0), "searchAction");

        getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).
                put(KeyStroke.getKeyStroke(KeyEvent.VK_F,
                UIUtilities.getCtrlDownMask()), "searchAction");

        getActionMap().put("pageUpAction",
                new TextPanePageUpAction(getTextPane()));
        getActionMap().put("pageDownAction",
                new TextPanePageDownAction(getTextPane()));
        getActionMap().put("searchAction", new SearchAction(searchBar));
    }

    /**
     * Removes and reinserts the border of an internal frame on maximising.
     * {@inheritDoc}
     * 
     * @param event Property change event
     */
    @Override
    public final void propertyChange(final PropertyChangeEvent event) {
        if ("maximum".equals(event.getPropertyName())) {
            if (event.getNewValue().equals(Boolean.TRUE)) {
                hideTitlebar();
                Main.getUI().getMainWindow().setMaximised(true);
            } else {
                showTitlebar();

                Main.getUI().getMainWindow().setMaximised(false);
                Main.getUI().getMainWindow().setActiveFrame(this);
            }

        } else if ("UI".equals(event.getPropertyName()) && isMaximum()) {
            hideTitlebar();
        }

    }

    /** Hides the titlebar for this frame. */
    private void hideTitlebar() {
        setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        ((BasicInternalFrameUI) getUI()).setNorthPane(null);
    }

    /** Shows the titlebar for this frame. */
    private void showTitlebar() {
        final Class<?> c;
        Object temp = null;
        Constructor<?> constructor;

        final String componentUI = (String) UIManager.get("InternalFrameUI");

        if ("javax.swing.plaf.synth.SynthLookAndFeel".equals(componentUI)) {
            temp = SynthLookAndFeel.createUI(this);
        } else {
            try {
                c = getClass().getClassLoader().loadClass(componentUI);
                constructor =
                        c.getConstructor(new Class[]{javax.swing.JInternalFrame.class});
                temp =
                        constructor.newInstance(new Object[]{this});
            } catch (ClassNotFoundException ex) {
                Logger.appError(ErrorLevel.MEDIUM, "Unable to readd titlebar",
                        ex);
            } catch (NoSuchMethodException ex) {
                Logger.appError(ErrorLevel.MEDIUM, "Unable to readd titlebar",
                        ex);
            } catch (InstantiationException ex) {
                Logger.appError(ErrorLevel.MEDIUM, "Unable to readd titlebar",
                        ex);
            } catch (IllegalAccessException ex) {
                Logger.appError(ErrorLevel.MEDIUM, "Unable to readd titlebar",
                        ex);
            } catch (InvocationTargetException ex) {
                Logger.appError(ErrorLevel.MEDIUM, "Unable to readd titlebar",
                        ex);
            }

        }

        setBorder(UIManager.getBorder("InternalFrame.border"));
        if (temp == null) {
            temp = new BasicInternalFrameUI(this);
        }

        this.setUI((BasicInternalFrameUI) temp);
    }

    /**
     * Not needed for this class. {@inheritDoc}
     * 
     * @param event Internal frame event
     */
    @Override
    public void internalFrameOpened(final InternalFrameEvent event) {
        new LoggingSwingWorker() {

            /** {@inheritDoc} */
            @Override
            protected Object doInBackground() throws Exception {
                parent.windowOpened();
                return null;
            }
        }.execute();
    }

    /**
     * Not needed for this class. {@inheritDoc}
     * 
     * @param event Internal frame event
     */
    @Override
    public void internalFrameClosing(final InternalFrameEvent event) {
        new LoggingSwingWorker() {

            /** {@inheritDoc} */
            @Override
            protected Object doInBackground() throws Exception {
                parent.windowClosing();
                return null;
            }
        }.execute();
    }

    /**
     * Not needed for this class. {@inheritDoc}
     * 
     * @param event Internal frame event
     */
    @Override
    public void internalFrameClosed(final InternalFrameEvent event) {
        new LoggingSwingWorker() {

            /** {@inheritDoc} */
            @Override
            protected Object doInBackground() throws Exception {
                parent.windowClosed();
                return null;
            }
        }.execute();
    }

    /**
     * Makes the internal frame invisible. {@inheritDoc}
     * 
     * @param event Internal frame event
     */
    @Override
    public void internalFrameIconified(final InternalFrameEvent event) {
        event.getInternalFrame().setVisible(false);
    }

    /**
     * Not needed for this class. {@inheritDoc}
     * 
     * @param event Internal frame event
     */
    @Override
    public void internalFrameDeiconified(final InternalFrameEvent event) {
    //Ignore.
    }

    /**
     * Activates the input field on frame focus. {@inheritDoc}
     * 
     * @param event Internal frame event
     */
    @Override
    public void internalFrameActivated(final InternalFrameEvent event) {
        new LoggingSwingWorker() {

            /** {@inheritDoc} */
            @Override
            protected Object doInBackground() throws Exception {
                parent.windowActivated();
                return null;
            }
        }.execute();
    }

    /**
     * Not needed for this class. {@inheritDoc}
     * 
     * @param event Internal frame event
     */
    @Override
    public void internalFrameDeactivated(final InternalFrameEvent event) {
        new LoggingSwingWorker() {

            /** {@inheritDoc} */
            @Override
            protected Object doInBackground() throws Exception {
                parent.windowDeactivated();
                return null;
            }
        }.execute();
    }

    /** {@inheritDoc} */
    @Override
    public FrameContainer getContainer() {
        return parent;
    }

    /** {@inheritDoc} */
    @Override
    public ConfigManager getConfigManager() {
        return getContainer().getConfigManager();
    }

    /**
     * Returns the text pane for this frame.
     *
     * @return Text pane for this frame
     */
    public final TextPane getTextPane() {
        return textPane;
    }

    /**
     * Returns the transcoder for this frame.
     *
     * @return String transcoder for this frame
     */
    @Override
    public StringTranscoder getTranscoder() {
        return transcoder;
    }

    /** {@inheritDoc} */
    @Override
    public final String getName() {
        if (parent == null) {
            return "";
        }

        return parent.toString();
    }

    /**
     * Sets the frames text pane.
     *
     * @param newTextPane new text pane to use
     */
    protected final void setTextPane(final TextPane newTextPane) {
        this.textPane = newTextPane;
    }

    /**
     * {@inheritDoc}
     * 
     * @param mouseEvent Mouse event
     */
    @Override
    public void mouseClicked(final MouseEvent mouseEvent) {
        if (mouseEvent.getSource() == getTextPane()) {
            processMouseClickEvent(mouseEvent, MouseClickType.CLICKED);
        }

    }

    /**
     * {@inheritDoc}
     * 
     * @param mouseEvent Mouse event
     */
    @Override
    public void mousePressed(final MouseEvent mouseEvent) {
        processMouseClickEvent(mouseEvent, MouseClickType.PRESSED);
    }

    /**
     * {@inheritDoc}
     * 
     * @param mouseEvent Mouse event
     */
    @Override
    public void mouseReleased(final MouseEvent mouseEvent) {
        if (quickCopy && mouseEvent.getSource() == getTextPane()) {
            getTextPane().copy();
            getTextPane().clearSelection();
        }

        processMouseClickEvent(mouseEvent, MouseClickType.RELEASED);
    }

    /**
     * {@inheritDoc}
     * 
     * @param mouseEvent Mouse event
     */
    @Override
    public void mouseEntered(final MouseEvent mouseEvent) {
        //Ignore.
    }

    /**
     * {@inheritDoc}
     * 
     * @param mouseEvent Mouse event
     */
    @Override
    public void mouseExited(final MouseEvent mouseEvent) {
        //Ignore.
    }

    /**
     * Processes every mouse button event to check for a popup trigger.
     *
     * @param e mouse event
     * @param type 
     */
    public void processMouseClickEvent(final MouseEvent e,
            final MouseClickType type) {
        final Point point = getTextPane().getMousePosition();
        if (e.getSource() == getTextPane() && point != null) {
            final LineInfo lineInfo = getTextPane().getClickPosition(point);
            final ClickType clickType = getTextPane().getClickType(lineInfo);
            final String attribute = (String) getTextPane().
                    getAttributeValueAtPoint(lineInfo);
            if (e.isPopupTrigger()) {
                showPopupMenuInternal(clickType, point, attribute);
            } else {
                if (type == MouseClickType.CLICKED) {
                    switch (clickType) {
                        case CHANNEL:
                            parent.getServer().join(attribute);
                            break;
                        case HYPERLINK:
                            URLHandler.getURLHander().launchApp(attribute);
                            break;
                        case NICKNAME:
                            if (getContainer().getServer().hasQuery(attribute)) {
                                getContainer().getServer().getQuery(attribute).
                                        activateFrame();
                            } else {
                                getContainer().getServer().addQuery(attribute);
                                getContainer().getServer().getQuery(attribute).
                                        show();
                            }

                            break;
                        default:
                            break;
                    }

                }
            }
        }
        super.processMouseEvent(e);
    }

    /**
     * What popup type should be used for popup menus for nicknames
     * 
     * @return Appropriate popuptype for this frame
     */
    public abstract PopupType getNicknamePopupType();

    /**
     * What popup type should be used for popup menus for channels
     * 
     * @return Appropriate popuptype for this frame
     */
    public abstract PopupType getChannelPopupType();

    /**
     * What popup type should be used for popup menus for hyperlinks
     * 
     * @return Appropriate popuptype for this frame
     */
    public abstract PopupType getHyperlinkPopupType();

    /**
     * What popup type should be used for popup menus for normal clicks
     * 
     * @return Appropriate popuptype for this frame
     */
    public abstract PopupType getNormalPopupType();

    /**
     * A method called to add custom popup items.
     * 
     * @param popupMenu Popup menu to add popup items to
     */
    public abstract void addCustomPopupItems(final JPopupMenu popupMenu);

    /**
     * Shows a popup menu at the specified point for the specified click type
     * 
     * @param type ClickType Click type
     * @param point Point Point of the click
     * @param argument Word under the click
     */
    private void showPopupMenuInternal(final ClickType type,
            final Point point,
            final String argument) {
        final JPopupMenu popupMenu;

        switch (type) {
            case CHANNEL:
                popupMenu = getPopupMenu(getChannelPopupType(), argument);
                popupMenu.add(new ChannelCopyAction(argument));
                if (popupMenu.getComponentCount() > 1) {
                    popupMenu.addSeparator();
                }

                break;
            case HYPERLINK:
                popupMenu = getPopupMenu(getHyperlinkPopupType(), argument);
                popupMenu.add(new HyperlinkCopyAction(argument));
                if (popupMenu.getComponentCount() > 1) {
                    popupMenu.addSeparator();
                }

                break;
            case NICKNAME:
                popupMenu = getPopupMenu(getNicknamePopupType(), argument);
                if (popupMenu.getComponentCount() > 0) {
                    popupMenu.addSeparator();
                }

                popupMenu.add(new NicknameCopyAction(argument));
                break;
            default:
                popupMenu = getPopupMenu(null, argument);
                break;
        }

        popupMenu.add(new TextPaneCopyAction(getTextPane()));

        addCustomPopupItems(popupMenu);

        popupMenu.show(this, (int) point.getX(), (int) point.getY());
    }

    /**
     * Shows a popup menu at the specified point for the specified click type
     * 
     * @param type ClickType Click type
     * @param point Point Point of the click
     * @param argument Word under the click
     */
    public void showPopupMenu(final ClickType type,
            final Point point,
            final String argument) {
        final JPopupMenu popupMenu;

        switch (type) {
            case CHANNEL:
                popupMenu = getPopupMenu(getChannelPopupType(), argument);
                popupMenu.add(new ChannelCopyAction(argument));
                if (popupMenu.getComponentCount() > 1) {
                    popupMenu.addSeparator();
                }

                break;
            case HYPERLINK:
                popupMenu = getPopupMenu(getHyperlinkPopupType(), argument);
                popupMenu.add(new HyperlinkCopyAction(argument));
                if (popupMenu.getComponentCount() > 1) {
                    popupMenu.addSeparator();
                }

                break;
            case NICKNAME:
                popupMenu = getPopupMenu(getNicknamePopupType(), argument);
                if (popupMenu.getComponentCount() > 0) {
                    popupMenu.addSeparator();
                }

                popupMenu.add(new NicknameCopyAction(argument));
                break;
            default:
                popupMenu = getPopupMenu(null, argument);
                break;
        }

        popupMenu.show(this, (int) point.getX(), (int) point.getY());
    }

    /**
     * Builds a popup menu of a specified type
     * 
     * @param type type of menu to build
     * @param arguments Arguments for the command
     * 
     * @return PopupMenu
     */
    public JPopupMenu getPopupMenu(
            final PopupType type,
            final Object... arguments) {
        JPopupMenu popupMenu = new JPopupMenu();

        if (type != null) {
            popupMenu = (JPopupMenu) populatePopupMenu(popupMenu,
                    PopupManager.getMenu(type, getConfigManager()),
                    arguments);
        }

        return popupMenu;
    }

    /**
     * Populates the specified popupmenu
     * 
     * @param menu Menu component
     * @param popup Popup to get info from
     * @param arguments Arguments for the command
     * 
     * @return Populated popup
     */
    private JComponent populatePopupMenu(final JComponent menu,
            final PopupMenu popup,
            final Object... arguments) {
        for (PopupMenuItem menuItem : popup.getItems()) {
            if (menuItem.isDivider()) {
                menu.add(new JSeparator());
            } else if (menuItem.isSubMenu()) {
                menu.add(populatePopupMenu(new JMenu(menuItem.getName()),
                        menuItem.getSubMenu(), arguments));
            } else {
                menu.add(new JMenuItem(new CommandAction(inputWindow == null ? GlobalCommandParser.getGlobalCommandParser()
                        : ((InputWindow) inputWindow).getCommandParser(),
                        (InputWindow) inputWindow, menuItem.getName(),
                        menuItem.getCommand(arguments))));
            }

        }
        return menu;
    }

    /** 
     * {@inheritDoc}
     * 
     * @param event Key event
     */
    @Override
    public void keyTyped(final KeyEvent event) {
    //Ignore.
    }

    /** 
     * {@inheritDoc}
     * 
     * @param event Key event
     */
    @Override
    public void keyPressed(final KeyEvent event) {
        if (!quickCopy && (event.getModifiers() & UIUtilities.getCtrlMask()) != 0 &&
                event.getKeyCode() == KeyEvent.VK_C) {
            getTextPane().copy();
        }

    }

    /** 
     * {@inheritDoc}
     * 
     * @param event Key event
     */
    @Override
    public void keyReleased(final KeyEvent event) {
    //Ignore.
    }

    /**
     * Gets the search bar.
     *
     * @return the frames search bar
     */
    public final SwingSearchBar getSearchBar() {
        return searchBar;
    }

    /** Closes this frame. */
    @Override
    public void close() {
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                if (closing) {
                    return;
                }

                closing = true;

                try {
                    setClosed(true);
                } catch (PropertyVetoException ex) {
                    Logger.userError(ErrorLevel.LOW, "Unable to close frame");
                }

            }
        });
    }

    /** Minimises the frame. */
    public void minimise() {
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                try {
                    setIcon(true);
                } catch (PropertyVetoException ex) {
                    Logger.userError(ErrorLevel.LOW, "Unable to minimise frame");
                }

            }
        });
    }
    
    /** Maximises the frame. */
    public void maximise() {
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                try {
                    setIcon(false);
                    setMaximum(true);
                    setVisible(true);
                } catch (PropertyVetoException ex) {
                    Logger.userError(ErrorLevel.LOW, "Unable to minimise frame");
                }

            }
        });
    }
    
    /** restores the frame. */
    public void restore() {
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                try {
                    setIcon(false);
                    setMaximum(false);
                    setVisible(true);
                } catch (PropertyVetoException ex) {
                    Logger.userError(ErrorLevel.LOW, "Unable to minimise frame");
                }

            }
        });
    }

    /** {@inheritDoc} */
    @Override
    public void configChanged(final String domain,
            final String key) {
        if (getConfigManager() == null) {
            return;
        }

        if ("ui".equals(domain)) {
            if ("foregroundcolour".equals(key) && getTextPane() != null) {
                getTextPane().setForeground(getConfigManager().
                        getOptionColour("ui", "foregroundcolour"));
            } else if ("backgroundcolour".equals(key) && getTextPane() != null) {
                getTextPane().setBackground(getConfigManager().
                        getOptionColour("ui", "backgroundcolour"));
            } else if ("frameBufferSize".equals(key)) {
                frameBufferSize = getContainer().getConfigManager().
                        getOptionInt("ui", "frameBufferSize");
            } else if ("quickCopy".equals(key)) {
                quickCopy = getContainer().getConfigManager().
                        getOptionBool("ui", "quickCopy");
            }
        }
    }
}
