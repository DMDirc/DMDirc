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

package uk.org.ownage.dmdirc.plugins.plugins.nickcolours;

import java.awt.Color;
import java.util.Map;
import java.util.Properties;

import uk.org.ownage.dmdirc.Channel;
import uk.org.ownage.dmdirc.ChannelClientProperty;
import uk.org.ownage.dmdirc.Config;
import uk.org.ownage.dmdirc.actions.ActionType;
import uk.org.ownage.dmdirc.actions.CoreActionType;
import uk.org.ownage.dmdirc.parser.ChannelClientInfo;
import uk.org.ownage.dmdirc.parser.ChannelInfo;
import uk.org.ownage.dmdirc.plugins.EventPlugin;
import uk.org.ownage.dmdirc.ui.components.PreferencesInterface;
import uk.org.ownage.dmdirc.ui.components.PreferencesPanel;
import uk.org.ownage.dmdirc.ui.messages.ColourManager;

/**
 *
 * @author chris
 */
public class NickColourPlugin implements EventPlugin, PreferencesInterface {
    
    private boolean isActive;
    
    private String[] randColours = new String[] {
        "E90E7F", "8E55E9", "B30E0E", "18B33C",
        "58ADB3", "9E54B3", "B39875", "3176B3"
    };
    
    /** Creates a new instance of NickColourPlugin. */
    public NickColourPlugin() {
        super();
    }
    
    /** {@inheritDoc} */
    public void processEvent(final ActionType type, final StringBuffer format,
            final Object... arguments) {
        if (type.equals(CoreActionType.CHANNEL_GOTNAMES)) {
            final ChannelInfo chanInfo = ((Channel) arguments[0]).getChannelInfo();
            
            for (ChannelClientInfo client : chanInfo.getChannelClients()) {
                colourClient(client);
            }
        } else if (type.equals(CoreActionType.CHANNEL_JOIN)) {
            colourClient((ChannelClientInfo) arguments[1]);
        }
    }
    
    /**
     * Colours the specified client according to the user's config.
     * @param client The client to be coloured
     */
    @SuppressWarnings("unchecked")
    private void colourClient(final ChannelClientInfo client) {
        final Map map = client.getMap();
        
        if (Config.getOptionBool("plugin-NickColour", "userandomcolour")) {
            if (Config.getOptionBool("plugin-NickColour", "useowncolour")) {
                if (client.getClient() == client.getClient().getParser().getMyself()) {
                    map.put(ChannelClientProperty.COLOUR_FOREGROUND, ColourManager.parseColour(Config.getOption("plugin-NickColour", "owncolour")));
                } else {
                    map.put(ChannelClientProperty.COLOUR_FOREGROUND, getColour(client.getNickname()));
                }
            } else {
                map.put(ChannelClientProperty.COLOUR_FOREGROUND, getColour(client.getNickname()));
            }
        }
    }
    
    /**
     * Retrieves a pseudo-random colour for the specified nickname.
     * @param nick The nickname of the client whose colour we're determining
     */
    private Color getColour(final String nick) {
        int count = 0;
        
        for (int i = 0; i < nick.length(); i++) {
            count += nick.charAt(i);
        }
        
        count = count % randColours.length;
        
        return ColourManager.parseColour(randColours[count]);
    }
    
    /** {@inheritDoc} */
    public boolean onLoad() {
        return true;
    }
    
    /** {@inheritDoc} */
    public void onUnload() {
    }
    
    /** {@inheritDoc} */
    public void onActivate() {
        isActive = true;
        
        if (Config.hasOption("plugin-NickColour", "randomcolours")) {
            randColours = Config.getOption("plugin-NickColour", "randomcolours").split("\n");
        }
    }
    
    /** {@inheritDoc} */
    public boolean isActive() {
        return isActive;
    }
    
    /** {@inheritDoc} */
    public void onDeactivate() {
        isActive = false;
    }
    
    /** {@inheritDoc} */
    public boolean isConfigurable() {
        return true;
    }
    
    /** {@inheritDoc} */
    public void showConfig() {
        final PreferencesPanel preferencesPanel = new PreferencesPanel(this, "NickColour Plugin - Config");
        preferencesPanel.addCategory("General", "General configuration for NickColour plugin.");
        
        preferencesPanel.addCheckboxOption("General", "userandomcolour", "Enable Random Colour: ", "Use a pseudo-random colour for each person?", Config.getOptionBool("plugin-NickColour", "userandomcolour"));
        preferencesPanel.addCheckboxOption("General", "useowncolour", "Use own colour: ", "Always use the same colour for own-nick?", Config.getOptionBool("plugin-NickColour", "useowncolour"));
        preferencesPanel.addColourOption("General", "owncolour", "Colour to use for own nick: ", "Use a pseudo-random colour for each person", Config.getOption("plugin-NickColour", "owncolour", "1"), true, true);
        
        preferencesPanel.display();
    }
    
    /**
     * Called when the preferences dialog is closed.
     *
     * @param properties user preferences
     */
    public void configClosed(final Properties properties) {
        Config.setOption("plugin-NickColour", "userandomcolour", properties.getProperty("userandomcolour"));
        Config.setOption("plugin-NickColour", "useowncolour", properties.getProperty("useowncolour"));
        Config.setOption("plugin-NickColour", "owncolour", properties.getProperty("owncolour"));
    }
    
    /** {@inheritDoc} */
    public String getVersion() {
        return "0.0";
    }
    
    /** {@inheritDoc} */
    public String getAuthor() {
        return "Chris <chris@dmdirc.com>";
    }
    
    /** {@inheritDoc} */
    public String getDescription() {
        return "Provides various nick colouring tools";
    }
    
    /** {@inheritDoc} */
    public String toString() {
        return "Nick Colour Plugin";
    }
    
}
