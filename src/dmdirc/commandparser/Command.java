/*
 * Command.java
 *
 * Created on 22 February 2007, 20:01
 *
 */

package dmdirc.commandparser;

/**
 *
 * @author chris
 */
public class Command {
    
    private String name;
    private int arity;
    private boolean thingy;
    
    /** Creates a new instance of Command */
    public Command() {
    }
    
    public String getSignature() {
        if (thingy) {
            return name;
        } else {
            return name+"/"+arity;
        }
    }
    
}
