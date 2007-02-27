/*
 * NicklistComparator.java
 *
 * Created on 27 February 2007, 11:47
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package uk.org.ownage.dmdirc.ui;

import java.util.Comparator;

/**
 *
 * @author greboid
 */
public class NicklistComparator<ChannelClientInfo> implements Comparator {
    boolean sortByMode = true;
    boolean sortByCase = false;
    
    /** Creates a new instance of NicklistComparator */
    public NicklistComparator(boolean sortByMode, boolean sortByCase) {
        this.sortByMode = sortByMode;
        this.sortByCase = sortByCase;
    }
    
    public int compare(Object o1, Object o2) 
    throws ClassCastException {
        if (o1 instanceof uk.org.ownage.dmdirc.parser.ChannelClientInfo
                || o2 instanceof uk.org.ownage.dmdirc.parser.ChannelClientInfo) {
            throw new ClassCastException("Unsupported types");
        }
        return -1;
    }
}
