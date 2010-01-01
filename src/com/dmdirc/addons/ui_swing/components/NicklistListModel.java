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

package com.dmdirc.addons.ui_swing.components;

import com.dmdirc.config.ConfigManager;
import com.dmdirc.interfaces.ConfigChangeListener;
import com.dmdirc.parser.interfaces.ChannelClientInfo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.swing.AbstractListModel;

/** Stores and provides means to modify nicklist data for a channel. */
public final class NicklistListModel extends AbstractListModel implements
        ConfigChangeListener {

    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 1;
    /** stores the nicknames to be shown in this list. */
    private final List<ChannelClientInfo> nicknames;
    /** Config manager. */
    private ConfigManager config;
    /** Sort by mode? */
    private boolean sortByMode;
    /** Sort by case? */
    private boolean sortByCase;

    /**
     * Creates a new empty model.
     *
     * @param config Config manager
     */
    public NicklistListModel(final ConfigManager config) {
        this(config, Collections.synchronizedList(new ArrayList<ChannelClientInfo>()));
    }

    /**
     * Creates a new model and initiliases it with the data provided.
     *
     * @param config Config manager
     * @param newNicknames list of nicknames used for initialisation
     */
    public NicklistListModel(final ConfigManager config,
            final List<ChannelClientInfo> newNicknames) {
        super();

        this.config = config;
        
        sortByMode = config.getOptionBool("nicklist", "sortByMode");
        sortByCase = config.getOptionBool("nicklist", "sortByCase");
        config.addChangeListener("nicklist", "sortByMode", this);
        config.addChangeListener("nicklist", "sortByCase", this);
        nicknames = Collections.synchronizedList(newNicknames);
        
        sort();
    }

    /**
     * Returns the size of the current nicklist.
     * @return nicklist size
     */
    @Override
    public int getSize() {
        return nicknames.size();
    }

    /**
     * Returns the element at the specified place in the nicklist.
     * 
     * @param index index of nick required
     * 
     * @return nicklist entry requested
     */
    @Override
    public ChannelClientInfo getElementAt(final int index) {
        return nicknames.get(index);
    }

    /**
     * Sorts the nicklist based on settings in the Config.
     */
    public void sort() {
        synchronized (nicknames) {
            Collections.sort(nicknames,
                    new NicklistComparator(sortByMode, sortByCase));
        }
        rerender();
    }

    /**
     * Replaces the entire nicklist with the arraylist specified.
     * 
     * @param clients replacement nicklist
     * 
     * @return boolean success
     */
    public boolean replace(final Collection<ChannelClientInfo> clients) {
        boolean returnValue = false;

        nicknames.clear();
        returnValue = nicknames.addAll(clients);
        sort();

        return returnValue;
    }

    /**
     * Adds the specified client to the nicklist.
     * 
     * @param client client to add to the nicklist
     * 
     * @return boolean success
     */
    public boolean add(final ChannelClientInfo client) {
        boolean returnValue = false;

        returnValue = nicknames.add(client);
        sort();

        return returnValue;
    }

    /**
     * Removes the specified client from the nicklist.
     * 
     * @param client client to remove
     * 
     * @return boolean success
     */
    public boolean remove(final ChannelClientInfo client) {
        boolean returnValue;
        
        returnValue = nicknames.remove(client);
        rerender();
        
        return returnValue;
    }

    /**
     * Removes the specified index from the nicklist.
     * 
     * @param index index to remove
     * 
     * @return ChannelClientInfo client removed
     */
    public ChannelClientInfo remove(final int index) {
        ChannelClientInfo returnValue;
        
        returnValue = nicknames.remove(index);
        rerender();
        
        return returnValue;
    }

    /** 
     * Fires the model changed event forcing the model to re-render. 
     */
    public void rerender() {
        fireContentsChanged(this, 0, nicknames.size());
    }

    /** {@inheritDoc} */
    @Override
    public void configChanged(String domain, String key) {
        if ("sortByMode".equals(key)) {
            sortByMode = config.getOptionBool("nicklist", "sortByMode");
        } else if ("sortByCase".equals(key)) {
            sortByCase = config.getOptionBool("nicklist", "sortByCase");
        }
        
        sort();
    }
}