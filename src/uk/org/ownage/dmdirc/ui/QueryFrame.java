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

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.ScrollPaneConstants;

import uk.org.ownage.dmdirc.Query;
import uk.org.ownage.dmdirc.Server;
import uk.org.ownage.dmdirc.commandparser.CommandParser;
import uk.org.ownage.dmdirc.commandparser.QueryCommandParser;
import uk.org.ownage.dmdirc.identities.ConfigManager;
import uk.org.ownage.dmdirc.ui.components.Frame;
import uk.org.ownage.dmdirc.ui.input.InputHandler;

/**
 * The QueryFrame is the MDI window that shows query messages to the user.
 */
public final class QueryFrame extends Frame {
    
    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 7;
    
    /** max length a line can be. */
    private final int maxLineLength;
    
    /** This channel's command parser. */
    private final QueryCommandParser commandParser;
    
    /** This frame's parent. */
    private final Query parent;
    
    /**
     * Creates a new QueryFrame.
     * @param owner Parent Frame container
     */
    public QueryFrame(final Query owner) {
        super(owner);
        
        parent = owner;
        
        maxLineLength = this.getServer().getParser().getMaxLength("PRIVMSG", this.getFrameParent().toString());
        
        initComponents();
        
        commandParser = new QueryCommandParser(((Query) getFrameParent()).
                getServer(), (Query) getFrameParent());
        
        setInputHandler(new InputHandler(getInputField(), commandParser, this));
    }
    
    /**
     * Retrieves the config manager for this command window.
     * @return This window's config manager
     */
    public ConfigManager getConfigManager() {
        return parent.getServer().getConfigManager();
    }
    
    /**
     * Retrieves the command Parser for this command window.
     * @return This window's command Parser
     */
    public CommandParser getCommandParser() {
        return commandParser;
    }
    
    /**
     * Retrieves the server associated with this command window.
     * @return This window's associated server instance
     */
    public Server getServer() {
        return parent.getServer();
    }
    
    /**
     * Initialises components in this frame.
     */
    private void initComponents() {
        final GridBagConstraints constraints = new GridBagConstraints();
        
        setTitle("Query Frame");
        
        getScrollPane().setVerticalScrollBarPolicy(
                ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        getTextPane().setEditable(false);
        getScrollPane().setViewportView(getTextPane());
        
        getContentPane().setLayout(new GridBagLayout());
        constraints.weightx = 1.0;
        constraints.weighty = 1.0;
        constraints.fill = GridBagConstraints.BOTH;
        constraints.insets = new Insets(0, 0, 5, 5);
        getContentPane().add(getScrollPane(), constraints);
        
        
        constraints.weighty = 0.0;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.gridy = 1;
        getContentPane().add(getSearchBar(), constraints);
        
        constraints.gridy = 2;
        getContentPane().add(getInputPanel(), constraints);
        
        pack();
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
}
