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

import java.util.ArrayList;
import java.util.Collections;
import javax.swing.AbstractListModel;
import uk.org.ownage.dmdirc.Config;
import uk.org.ownage.dmdirc.parser.ChannelClientInfo;

public class NicklistListModel extends AbstractListModel {
    public static final long serialVersionUID = 1l;
    ArrayList<ChannelClientInfo> nicknames;
    
    public NicklistListModel() {
        nicknames = new ArrayList<ChannelClientInfo>();
    }
    
    public NicklistListModel(ArrayList<ChannelClientInfo> nicknames) {
        this.nicknames = nicknames;
    }
    
    public int getSize() {
        return nicknames.size();
    }
    
    public ChannelClientInfo getElementAt(int index) {
        return nicknames.get(index);
    }
    
    public void sort() {
        boolean sortByMode = true;
        boolean sortByCase = false;
        if (Config.hasOption("ui","sortByMode")) {
            Config.getOption("ui","sortByMode");
        }
        if (Config.hasOption("ui","sortByCase")) {
            Config.getOption("ui","sortByCase");
        }
        NicklistComparator comparator = new NicklistComparator(sortByMode, sortByCase);
        Collections.sort(nicknames, comparator);
    }
    
    public boolean add(ArrayList<ChannelClientInfo> clients) {
        boolean returnValue = false;
        
        nicknames.clear();
        returnValue = nicknames.addAll(clients);
        
        this.sort();
        this.fireIntervalAdded(this, 0, nicknames.size());

        return returnValue;
    }
    
    public boolean add(ChannelClientInfo client) {
        boolean returnValue = false;
        
        returnValue = nicknames.add(client);
        
        this.sort();
        this.fireIntervalAdded(this, 0, nicknames.size());

        return returnValue;
    }
    
    public void add(int index, ChannelClientInfo client) {
        nicknames.add(index, client);
        this.sort();
        this.fireIntervalAdded(this, 0, nicknames.size());
    }
    
    public boolean remove(ChannelClientInfo client) {
        boolean returnValue;
        returnValue = nicknames.remove(client);
        this.fireIntervalRemoved(this, 0, nicknames.size());
        return returnValue;
    }
    
    public ChannelClientInfo remove(int index) {
        ChannelClientInfo returnValue;
        returnValue = nicknames.remove(index);
        this.fireIntervalRemoved(this, 0, nicknames.size());
        return returnValue;
    }
    
}
