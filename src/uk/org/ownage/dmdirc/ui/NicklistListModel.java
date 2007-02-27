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
    
    public boolean add(ChannelClientInfo client) {
        boolean returnValue = false;
        boolean sortByMode = true;
        boolean sortByCase = false;
        if (Config.hasOption("ui","sortByMode")) {
            Config.getOption("ui","sortByMode");
        }
        if (Config.hasOption("ui","sortByCase")) {
            Config.getOption("ui","sortByCase");
        }
        
        returnValue = nicknames.add(client);
        
        NicklistComparator comparator = new NicklistComparator(sortByMode, sortByCase);
        Collections.sort(nicknames, comparator);
        
        return returnValue;
    }
    
    public void add(int index, ChannelClientInfo client) {
        nicknames.add(index, client);
    }
    
    public boolean remove(ChannelClientInfo client) {
        return nicknames.remove(client);
    }
    
    public ChannelClientInfo remove(int index) {
        return nicknames.remove(index);
    }
    
}
