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

package com.dmdirc.util;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Reads and writes a standard DMDirc config file.
 *
 * @author chris
 */
public class ConfigFile {

    /** A list of domains in this config file. */
    private final List<String> domains = new ArrayList<String>();

    /** The values associated with each flat domain. */
    private final MapList<String, String> flatdomains = new MapList<String, String>();

    /** The key/value sets associated with each key domain. */
    private final Map<String, Map<String, String>> keydomains
            = new HashMap<String, Map<String, String>>();

    /** The text file we're reading. */
    private final TextFile file;

    /**
     * Creates a new instance of ConfigFile.
     * 
     * @param file The path of the file to be loaded
     */
    public ConfigFile(final String file) {
        this.file = new TextFile(file);
    }

    /**
     * Reads the data from the file.
     * 
     * @throws FileNotFoundException if the file is not found
     * @throws IOException if an i/o exception occured when reading
     * @throws InvalidConfigFileException if the config file isn't valid
     */
    public void read() throws FileNotFoundException, IOException, InvalidConfigFileException {
        String domain = null;
        boolean keydomain = false;
        int offset;

        for (String line : file.getLines()) {
            final String tline = line.trim();

            if (tline.indexOf('#') == 0 || tline.isEmpty()) {
                continue;
            } else if (tline.endsWith(":") && tline.indexOf('=') == -1) {
                domain = tline.substring(0, tline.length() - 1);

                domains.add(domain);

                keydomain = keydomains.containsKey(domain)
                        || flatdomains.containsValue("keysections", domain);
                
                if (keydomain && !keydomains.containsKey(domain)) {
                    keydomains.put(domain, new HashMap<String, String>());
                } else if (!keydomain && !flatdomains.containsKey(domain)) {
                    flatdomains.add(domain);
                }
            } else if (domain != null && keydomain && (offset = tline.indexOf('=')) != -1) {
                final String key = tline.substring(0, offset);
                final String value = tline.substring(offset + 1);

                keydomains.get(domain).put(key, value);
            } else if (domain != null && !keydomain) {
                flatdomains.add(domain, tline);
            } else {
                throw new InvalidConfigFileException("Unknown or unexpected" +
                        " line encountered: " + tline);
            }
        }
    }

    /**
     * Writes the contents of this ConfigFile to disk.
     * 
     * @throws IOException if the write operation fails
     */
    public void write() throws IOException {
        final List<String> lines = new ArrayList<String>();

        lines.add("# This is a DMDirc configuration file.");
        lines.add("# Written on: " + new GregorianCalendar().getTime().toString());

        writeMeta(lines);

        for (String domain : domains) {
            if ("keysections".equals(domain)) {
                continue;
            }

            lines.add("");

            lines.add(domain + ':');

            if (flatdomains.containsKey(domain)) {
                for (String entry : flatdomains.get(domain)) {
                    lines.add("  " + entry);
                }
            } else if (keydomains.containsKey(domain)) {
                for (Map.Entry<String, String> entry : keydomains.get(domain).entrySet()) {
                    lines.add("  " + entry.getKey() + "=" + entry.getValue());
                }
            }
        }

        file.writeLines(lines);
    }

    /**
     * Appends the meta-data (keysections) to the specified list of lines.
     * 
     * @param lines The set of lines to be appended to
     */
    private void writeMeta(final List<String> lines) {
        lines.add("");
        lines.add("# This section indicates which sections below take key/value");
        lines.add("# pairs, rather than a simple list. It should be placed above");
        lines.add("# any sections that take key/values.");
        lines.add("keysections:");

        for (String domain : domains) {
            if ("keysections".equals(domain)) {
                continue;
            } else if (keydomains.containsKey(domain)) {
                lines.add("  " + domain);
            }
        }
    }
    
    /**
     * Retrieves the key/values of the specified key domain.
     * 
     * @param domain The domain to be retrieved
     * @return A map of keys to values in the specified domain
     */
    public Map<String, String> getKeyDomain(final String domain) {
        return keydomains.get(domain);
    }
    
    /**
     * Retrieves the content of the specified flat domain.
     * 
     * @param domain The domain to be retrieved
     * @return A list of lines in the specified domain
     */
    public List<String> getFlatDomain(final String domain) {
        return flatdomains.get(domain);
    }
    
    /**
     * Determines if this config file has the specified domain.
     * 
     * @param domain The domain to check for
     * @return True if the domain is known, false otherwise
     */
    public boolean hasDomain(final String domain) {
        return keydomains.containsKey(domain) || flatdomains.containsKey(domain);
    }

    /**
     * Determines if this config file has the specified domain, and the domain
     * is a key domain.
     * 
     * @param domain The domain to check for
     * @return True if the domain is known and keyed, false otherwise
     */
    public boolean isKeyDomain(final String domain) {
        return keydomains.containsKey(domain);
    }

    /**
     * Determines if this config file has the specified domain, and the domain
     * is a flat domain.
     * 
     * @param domain The domain to check for
     * @return True if the domain is known and flat, false otherwise
     */
    public boolean isFlatDomain(final String domain) {
        return flatdomains.containsKey(domain);
    }
    
    /**
     * Adds a new flat domain to this config file.
     * 
     * @param name The name of the domain to be added
     * @param data The content of the domain
     */
    public void addDomain(final String name, final List<String> data) {
        domains.add(name);
        flatdomains.add(name, data);
    }

    /**
     * Adds a new key domain to this config file.
     * 
     * @param name The name of the domain to be added
     * @param data The content of the domain
     */    
    public void addDomain(final String name, final Map<String, String> data) {
        domains.add(name);
        keydomains.put(name, data);
    }
}