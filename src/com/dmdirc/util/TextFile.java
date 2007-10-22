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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Allows reading and writing to a plain text file via a list of lines.
 * 
 * @author chris
 */
public class TextFile {
    
    private final File file;
    
    public TextFile(final String filename) {
        file = new File(filename);
    }
    
    public List<String> getLines() throws FileNotFoundException, IOException {
        final BufferedReader reader = new BufferedReader(new FileReader(file));
        final List<String> res = new ArrayList<String>();
        
        String line;
        
        while ((line = reader.readLine()) != null) {
            res.add(line);
        }
        
        reader.close();
        
        return res;
    }
    
    public void writeLines(final List<String> lines) throws IOException {
        final BufferedWriter writer = new BufferedWriter(new FileWriter(file));
        
        for (String line : lines) {
            writer.write(line);
            writer.newLine();
        }
    }

}
