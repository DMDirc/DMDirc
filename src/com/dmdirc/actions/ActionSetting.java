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

package com.dmdirc.actions;

/**
 * Represents a setting required by an Action pack.
 * 
 * @author chris
 */
public class ActionSetting {
    
    /** The possible types of setting used by action packs. */
    public static enum TYPE {
        /** A freeform text setting. */
        TEXT,
        /** A colour setting. */
        COLOUR,
        /** A numerical setting. */
        NUMBER,
        /** A boolean setting. */
        BOOLEAN
    }

    /**
     * Creates a new instance of ActionSetting.
     * 
     * @param type The type of setting.
     * @param setting The name of the setting.
     * @param title The user-friendly name of the setting.
     * @param tooltip The tooltip to show for this setting.
     * @param fallback The default value of this setting.
     */
    public ActionSetting(TYPE type, String setting, String title, String tooltip, String fallback) {
        this.type = type;
        this.setting = setting;
        this.title = title;
        this.tooltip = tooltip;
        this.fallback = fallback;
    }
    
    /** The type of this setting. */
    private TYPE type;
    
    /** The name (key) of this setting. */
    private String setting;
    
    /** The user-friendly name of this setting. */
    private String title;
    
    /** The tooltip to show for this setting. */
    private String tooltip;
    
    /** The default value of this setting. */
    private String fallback;

    /**
     * Retrieves the default value of this setting.
     * 
     * @return This setting's default value.
     */
    public String getFallback() {
        return fallback;
    }

    /**
     * Retrieves the name (key) of this setting.
     * 
     * @return This setting's name
     */    
    public String getSetting() {
        return setting;
    }

    /**
     * Retrieves the user-friendly name of this setting.
     * 
     * @return This setting's user-friendly name.
     */    
    public String getTitle() {
        return title;
    }

    /**
     * Retrieves the tooltip text of this setting.
     * 
     * @return This setting's tooltip text.
     */    
    public String getTooltip() {
        return tooltip;
    }

    /**
     * Retrieves the type of this setting.
     * 
     * @return This setting's type.
     */    
    public TYPE getType() {
        return type;
    }
    
}
