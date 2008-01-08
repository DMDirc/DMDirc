/*
 * Copyright (c) 2006-2008 Chris Smith, Shane Mc Cormack, Gregory Holmes
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

package com.dmdirc.addons.nickcolours;

import com.dmdirc.Channel;
import com.dmdirc.ChannelClientProperty;
import com.dmdirc.Main;
import com.dmdirc.actions.ActionManager;
import com.dmdirc.actions.interfaces.ActionType;
import com.dmdirc.actions.CoreActionType;
import com.dmdirc.config.IdentityManager;
import com.dmdirc.interfaces.ActionListener;
import com.dmdirc.parser.ChannelClientInfo;
import com.dmdirc.parser.ChannelInfo;
import com.dmdirc.parser.ClientInfo;
import com.dmdirc.plugins.Plugin;
import com.dmdirc.ui.interfaces.PreferencesInterface;
import com.dmdirc.ui.interfaces.PreferencesPanel;
import com.dmdirc.ui.messages.ColourManager;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Provides various features related to nickname colouring.
 *
 * @author chris
 */
public final class NickColourPlugin extends Plugin implements ActionListener, PreferencesInterface {
    
    /** The config domain to use for this plugin. */
    private static final String DOMAIN = "plugin-NickColour";
    
    /** "Random" colours to use to colour nicknames. */
    private String[] randColours = new String[] {
        "E90E7F", "8E55E9", "B30E0E", "18B33C",
        "58ADB3", "9E54B3", "B39875", "3176B3",
    };
    
    /** The nick colour panel we use for config. */
    private NickColourPanel nickpanel;
    
    /** Creates a new instance of NickColourPlugin. */
    public NickColourPlugin() {
        super();
    }
    
    /** {@inheritDoc} */
    public void processEvent(final ActionType type, final StringBuffer format,
            final Object... arguments) {
        if (type.equals(CoreActionType.CHANNEL_GOTNAMES)) {
            final ChannelInfo chanInfo = ((Channel) arguments[0]).getChannelInfo();
            final String network = ((Channel) arguments[0]).getServer().getNetwork();
            
            for (ChannelClientInfo client : chanInfo.getChannelClients()) {
                colourClient(network, client);
            }
        } else if (type.equals(CoreActionType.CHANNEL_JOIN)) {
            final String network = ((Channel) arguments[0]).getServer().getNetwork();
            
            colourClient(network, (ChannelClientInfo) arguments[1]);
        }
    }
    
    /**
     * Colours the specified client according to the user's config.
     *
     * @param network The network to use for the colouring
     * @param client The client to be coloured
     */
    private void colourClient(final String network, final ChannelClientInfo client) {
        final Map map = client.getMap();
        final ClientInfo myself = client.getClient().getParser().getMyself();
        final String nickOption1 = "color:"
                + client.getClient().getParser().toLowerCase(network + ":" + client.getNickname());
        final String nickOption2 = "color:"
                + client.getClient().getParser().toLowerCase("*:" + client.getNickname());
        
        if (IdentityManager.getGlobalConfig().getOptionBool(DOMAIN, "useowncolour", false)
                && client.getClient().equals(myself)) {
            final Color color = ColourManager.parseColour(
                    IdentityManager.getGlobalConfig().getOption(DOMAIN, "owncolour"));
            putColour(map, color, color);
        }  else if (IdentityManager.getGlobalConfig().getOptionBool(DOMAIN, "userandomcolour", false)) {
            putColour(map, getColour(client.getNickname()), getColour(client.getNickname()));
        }
        
        String[] parts = null;
                
        if (IdentityManager.getGlobalConfig().hasOption(DOMAIN, nickOption1)) {
            parts = getParts(nickOption1);
        } else if (IdentityManager.getGlobalConfig().hasOption(DOMAIN, nickOption2)) {
            parts = getParts(nickOption2);
        }
        
        if (parts != null) {
            Color textColor = null;
            Color nickColor = null;
            
            if (parts[0] != null) {
                textColor = ColourManager.parseColour(parts[0], null);
            }
            if (parts[1] != null) {
                nickColor = ColourManager.parseColour(parts[1], null);
            }
            
            putColour(map, textColor, nickColor);
        }
    }
    
    /**
     * Puts the specified colour into the given map. The keys are determined
     * by config settings.
     *
     * @param map The map to use
     * @param colour The colour to be inserted
     */
    @SuppressWarnings("unchecked")
    private void putColour(final Map map, final Color textColour, final Color nickColour) {
        if (IdentityManager.getGlobalConfig().getOptionBool(DOMAIN, "settext", false) && textColour != null) {
            map.put(ChannelClientProperty.TEXT_FOREGROUND, textColour);
        }
        
        if (IdentityManager.getGlobalConfig().getOptionBool(DOMAIN, "setnicklist", false) && nickColour != null) {
            map.put(ChannelClientProperty.NICKLIST_FOREGROUND, nickColour);
        }
    }
    
    /**
     * Retrieves a pseudo-random colour for the specified nickname.
     *
     * @param nick The nickname of the client whose colour we're determining
     * @return Colour of the specified nickname
     */
    private Color getColour(final String nick) {
        int count = 0;
        
        for (int i = 0; i < nick.length(); i++) {
            count += nick.charAt(i);
        }
        
        count = count % randColours.length;
        
        return ColourManager.parseColour(randColours[count]);
    }
    
    /**
     * Reads the nick colour data from the config.
     *
     * @return A multi-dimensional array of nick colour info.
     */
    public Object[][] getData() {
        final List<Object[]> data = new ArrayList<Object[]>();
        
        for (String key : IdentityManager.getGlobalConfig().getOptions(DOMAIN)) {
            if (key.startsWith("color:")) {
                final String network = key.substring(6, key.indexOf(':', 6));
                final String user = key.substring(1 + key.indexOf(':', 6));
                final String[] parts = getParts(key);
                
                
                data.add(new Object[]{network, user, parts[0], parts[1]});
            }
        }
        
        final Object[][] res = new Object[data.size()][4];
        
        int i = 0;
        for (Object[] row : data) {
            res[i] = row;
            
            i++;
        }
        
        return res;
    }
    
    /**
     * Retrieves the config option with the specified key, and returns an
     * array of the colours that should be used for it.
     * 
     * @param key The config key to look up
     * @return The colours specified by the given key
     */
    private String[] getParts(final String key) {
        String[] parts = IdentityManager.getGlobalConfig().getOption(DOMAIN, key).split(":");
        
        if (parts.length == 0) {
            parts = new String[]{null, null};
        } else if (parts.length == 1) {
            parts = new String[]{parts[0], null};
        }
        
        return parts;
    }
    
    /** {@inheritDoc} */
    public void onLoad() {
        if (IdentityManager.getGlobalConfig().hasOption(DOMAIN, "randomcolours")) {
            randColours =(String[]) IdentityManager.getGlobalConfig().getOptionList(DOMAIN, "randomcolours").toArray();
        }
        
        ActionManager.addListener(this, CoreActionType.CHANNEL_GOTNAMES,
                CoreActionType.CHANNEL_JOIN);
    }
    
    /** {@inheritDoc} */
    public void onUnload() {
        ActionManager.removeListener(this);
    }
    
    /** {@inheritDoc} */
    public boolean isConfigurable() {
        return true;
    }
    
    /** {@inheritDoc} */
    public void showConfig() {
        final PreferencesPanel preferencesPanel = Main.getUI().getPreferencesPanel(this, "NickColour Plugin - Config");
        preferencesPanel.addCategory("General", "General configuration for NickColour plugin.");
        preferencesPanel.addCategory("Nick colours", "Set colours for specific nicknames.");
        
        preferencesPanel.addCheckboxOption("General", "showintext",
                "Show colours in text area: ", "Colour nicknames in main text area?",
                IdentityManager.getGlobalConfig().getOptionBool("ui", "shownickcoloursintext", false));
        preferencesPanel.addCheckboxOption("General", "showinlist",
                "Show colours in nick list: ", "Colour nicknames in channel user lists?",
                IdentityManager.getGlobalConfig().getOptionBool("ui", "shownickcoloursinnicklist", false));
        
        preferencesPanel.addCheckboxOption("General", "settext",
                "Set colours in text area: ", "Should the plugin set the text colour of nicks?",
                IdentityManager.getGlobalConfig().getOptionBool(DOMAIN, "settext", false));
        preferencesPanel.addCheckboxOption("General", "setnicklist",
                "Set colours in nick list: ", "Should the plugin set the nicklist colour of nicks?",
                IdentityManager.getGlobalConfig().getOptionBool(DOMAIN, "setnicklist", false));
        
        preferencesPanel.addCheckboxOption("General", "userandomcolour",
                "Enable Random Colour: ", "Use a pseudo-random colour for each person?",
                IdentityManager.getGlobalConfig().getOptionBool(DOMAIN, "userandomcolour", false));
        preferencesPanel.addCheckboxOption("General", "useowncolour",
                "Use colour for own nick: ", "Always use the same colour for own nick?",
                IdentityManager.getGlobalConfig().getOptionBool(DOMAIN, "useowncolour", false));
        preferencesPanel.addColourOption("General", "owncolour",
                "Colour to use for own nick: ", "Colour used for own nick",
                IdentityManager.getGlobalConfig().getOption(DOMAIN, "owncolour", "1"), true, true);

        nickpanel = new NickColourPanel(this);
        preferencesPanel.replaceOptionPanel("Nick colours", nickpanel);
        
        preferencesPanel.display();
        
    }
    
    /**
     * Called when the preferences dialog is closed.
     *
     * @param properties user preferences
     */
    public void configClosed(final Properties properties) {
        IdentityManager.getConfigIdentity().setOption("ui", "shownickcoloursintext", properties.getProperty("showintext"));
        IdentityManager.getConfigIdentity().setOption("ui", "shownickcoloursinnicklist", properties.getProperty("showinlist"));
        IdentityManager.getConfigIdentity().setOption(DOMAIN, "userandomcolour", properties.getProperty("userandomcolour"));
        IdentityManager.getConfigIdentity().setOption(DOMAIN, "useowncolour", properties.getProperty("useowncolour"));
        IdentityManager.getConfigIdentity().setOption(DOMAIN, "owncolour", properties.getProperty("owncolour"));
        
        IdentityManager.getConfigIdentity().setOption(DOMAIN, "settext", properties.getProperty("settext"));
        IdentityManager.getConfigIdentity().setOption(DOMAIN, "setnicklist", properties.getProperty("setnicklist"));
        
        // Remove all old config entries
        for (Object[] parts : getData()) {
            IdentityManager.getConfigIdentity().unsetOption(DOMAIN, "color:" + parts[0] + ":" + parts[1]);
        }
        
        // And write the new ones
        for (Object[] row : nickpanel.getData()) {
            IdentityManager.getConfigIdentity().setOption(DOMAIN, "color:" + row[0] + ":" + row[1], row[2] + ":" + row[3]);
        }
    }
    
    /** {@inheritDoc} */
    public void configCancelled() {
        // Do nothing
    }
    
}
