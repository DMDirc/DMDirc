/*
 * Copyright (c) 2006-2009 Chris Smith, Shane Mc Cormack, Gregory Holmes
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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
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
public class ConfigFile extends TextFile {

    /** A list of domains in this config file. */
    private final List<String> domains = new ArrayList<String>();

    /** The values associated with each flat domain. */
    private final MapList<String, String> flatdomains = new MapList<String, String>();

    /** The key/value sets associated with each key domain. */
    private final Map<String, Map<String, String>> keydomains
            = new HashMap<String, Map<String, String>>();
    
    /** Whether or not we should automatically create domains. */
    private boolean automake;

    /**
     * Creates a new read-only Config File from the specified input stream.
     * 
     * @param is The input stream to read
     */
    public ConfigFile(final InputStream is) {
        super(is, Charset.forName("UTF-8"));
    }

    /**
     * Creates a new Config File from the specified file.
     * 
     * @param file The file to read/write
     */
    public ConfigFile(final File file) {
        super(file, Charset.forName("UTF-8"));
    }

    /**
     * Creates a new Config File from the specified file.
     * 
     * @param filename The name of the file to read/write
     */
    public ConfigFile(final String filename) {
        this(new File(filename));
    }

    /**
     * Sets the "automake" value of this config file. If automake is set to
     * true, any calls to getKeyDomain will automatically create the domain
     * if it did not previously exist.
     * 
     * @param automake The new value of the automake setting of this file
     */
    public void setAutomake(final boolean automake) {
        this.automake = automake;
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
        
        keydomains.clear();
        flatdomains.clear();
        domains.clear();
        
        readLines();

        for (String line : getLines()) {
            String tline = line;
            
            while (!tline.isEmpty() && (tline.charAt(0) == '\t' || 
                    tline.charAt(0) == ' ')) {
                tline = tline.substring(1);
            }

            if (tline.indexOf('#') == 0 || tline.isEmpty()) {
                continue;
            } else if (
                    (tline.endsWith(":") && !tline.endsWith("\\:"))
                    && findEquals(tline) == -1) {
                domain = unescape(tline.substring(0, tline.length() - 1));

                domains.add(domain);

                keydomain = keydomains.containsKey(domain)
                        || flatdomains.containsValue("keysections", domain);
                
                if (keydomain && !keydomains.containsKey(domain)) {
                    keydomains.put(domain, new HashMap<String, String>());
                } else if (!keydomain && !flatdomains.containsKey(domain)) {
                    flatdomains.add(domain);
                }
            } else if (domain != null && keydomain
                    && (offset = findEquals(tline)) != -1) {
                final String key = unescape(tline.substring(0, offset));
                final String value = unescape(tline.substring(offset + 1));

                keydomains.get(domain).put(key, value);
            } else if (domain != null && !keydomain) {
                flatdomains.add(domain, unescape(tline));
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
        if (!isWritable()) {
            throw new UnsupportedOperationException("Cannot write to a file "
                    + "that isn't writable");
        }
        
        final List<String> lines = new ArrayList<String>();

        lines.add("# This is a DMDirc configuration file.");
        lines.add("# Written on: " + new GregorianCalendar().getTime().toString());

        writeMeta(lines);

        for (String domain : domains) {
            if ("keysections".equals(domain)) {
                continue;
            }

            lines.add("");

            lines.add(escape(domain) + ':');

            if (flatdomains.containsKey(domain)) {
                for (String entry : flatdomains.get(domain)) {
                    lines.add("  " + escape(entry));
                }
            } else {
                for (Map.Entry<String, String> entry : keydomains.get(domain).entrySet()) {
                    lines.add("  " + escape(entry.getKey()) + "="
                            + escape(entry.getValue()));
                }
            }
        }

        writeLines(lines);
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
     * Retrieves all the key domains for this config file.
     * 
     * @return This config file's key domains
     */
    public Map<String, Map<String, String>> getKeyDomains() {
        return keydomains;
    }
    
    /**
     * Retrieves the key/values of the specified key domain.
     * 
     * @param domain The domain to be retrieved
     * @return A map of keys to values in the specified domain
     */
    public Map<String, String> getKeyDomain(final String domain) {
        if (automake && !isKeyDomain(domain)) {
            domains.add(domain);
            keydomains.put(domain, new HashMap<String, String>());
        }
        
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
    
    /**
     * Unescapes any escaped characters in the specified input string.
     * 
     * @param input The string to unescape
     * @return The string with all escape chars (\) resolved
     */
    protected static String unescape(final String input) {
        boolean escaped = false;
        final StringBuilder temp = new StringBuilder();

        for (int i = 0; i < input.length(); i++) {
            final char ch = input.charAt(i);

            if (escaped) {
                if (ch == 'n') {
                    temp.append('\n');
                } else if (ch == 'r') {
                    temp.append('\r');
                } else {
                    temp.append(ch);
                }
                
                escaped = false;
            } else if (ch == '\\') {
                escaped = true;
            } else {
                temp.append(ch);
            }
        }
        
        return temp.toString();
    }
    
    /**
     * Escapes the specified input string by prefixing all occurances of
     * \, \n, \r, =, # and : with backslashes.
     * 
     * @param input The string to be escaped
     * @return A backslash-armoured version of the string
     */
    protected static String escape(final String input) {
        return input.replaceAll("\\\\", "\\\\\\\\").replaceAll("\n", "\\\\n")
                .replaceAll("\r", "\\\\r").replaceAll("=", "\\\\=")
                .replaceAll(":", "\\\\:").replaceAll("#", "\\\\#");
    }
    
    /**
     * Finds the first non-escaped instance of '=' in the specified string.
     * 
     * @param input The string to be searched
     * @return The offset of the first non-escaped instance of '=', or -1.
     */
    protected static int findEquals(final String input) {
        boolean escaped = false;
        
        for (int i = 0; i < input.length(); i++) {
            if (escaped) {
                escaped = false;
            } else if (input.charAt(i) == '\\') {
                escaped = true;
            } else if (input.charAt(i) == '=') {
                return i;
            }
        }
        
        return -1;
    }
}