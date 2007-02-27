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
        
        NicklistComparator<ChannelClientInfo> comparator = new NicklistComparator<ChannelClientInfo>(sortByMode, sortByCase);
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
