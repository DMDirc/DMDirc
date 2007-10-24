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

    private final List<String> domains = new ArrayList<String>();

    private final MapList<String, String> flatdomains = new MapList<String, String>();

    private final Map<String, Map<String, String>> keydomains
            = new HashMap<String, Map<String, String>>();

    private final TextFile file;

    public ConfigFile(final String file) throws FileNotFoundException, IOException {
        this.file = new TextFile(file);

        read();
    }

    public void read() throws FileNotFoundException, IOException {
        String domain = null;
        boolean keydomain = false;
        int offset;

        for (String line : file.getLines()) {
            final String tline = line.trim();

            if (tline.indexOf('#') == 0) {
                continue;
            } else if (tline.endsWith(":") && tline.indexOf('=') == -1) {
                domain = tline.substring(1, tline.length() - 1);

                domains.add(domain);

                keydomain = keydomains.containsKey(domain)
                        || flatdomains.containsValue("keysections", domain);
            } else if (domain != null && keydomain && (offset = tline.indexOf('=')) != -1) {
                final String key = tline.substring(0, offset);
                final String value = tline.substring(offset + 1);

                keydomains.get(domain).put(key, value);
            } else if (domain != null && !keydomain) {
                flatdomains.add(domain, tline);
            }
        }
    }

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
    
    public Map<String, String> getKeyDomain(final String domain) {
        return keydomains.get(domain);
    }
    
    public List<String> getListDomain(final String domain) {
        return flatdomains.get(domain);
    }
    
    public boolean hasDomain(final String domain) {
        return keydomains.containsKey(domain) || flatdomains.containsKey(domain);
    }
}