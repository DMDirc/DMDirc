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

import com.dmdirc.logger.ErrorLevel;
import com.dmdirc.logger.Logger;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Allows easy downloading of files from HTTP sites.
 *
 * @author Chris
 */
public final class Downloader {
    
    /** Creates a new instance of Downloader. */
    private Downloader() {
        // Shouldn't be used
    }
    
    /**
     * Retrieves the specified page.
     * 
     * @param url The URL to retrieve
     * @return A list of lines received from the server
     * @throws java.net.MalformedURLException If the URL is malformed
     * @throws java.io.IOException If there's an I/O error while downloading
     */
    public static List<String> getPage(final String url)
            throws MalformedURLException, IOException {
        
        return getPage(url, "");
    }

    /**
     * Retrieves the specified page, sending the specified post data.
     * 
     * @param url The URL to retrieve
     * @param postData The raw POST data to send
     * @return A list of lines received from the server
     * @throws java.net.MalformedURLException If the URL is malformed
     * @throws java.io.IOException If there's an I/O error while downloading
     */    
    public static List<String> getPage(final String url, final String postData)
            throws MalformedURLException, IOException {
        
        final List<String> res = new ArrayList<String>();
        
        final URLConnection urlConn = getConnection(url, postData);

        BufferedReader in = null;

        try {
            in = new BufferedReader(new InputStreamReader(urlConn.getInputStream()));
        
            String line;

            do {
                line = in.readLine();

                if (line != null) {
                    res.add(line);
                }
            } while (line != null);
        } finally {
            StreamUtil.close(in);
        }
        
        return res;
    }
    
    /**
     * Retrieves the specified page, sending the specified post data.
     * 
     * @param url The URL to retrieve
     * @param postData A map of post data that should be sent
     * @return A list of lines received from the server
     * @throws java.net.MalformedURLException If the URL is malformed
     * @throws java.io.IOException If there's an I/O error while downloading
     */    
    public static List<String> getPage(final String url, final Map<String, String> postData)
            throws MalformedURLException, IOException {
        
        final StringBuilder data = new StringBuilder();
        
        try {
            for (Map.Entry<String, String> entry : postData.entrySet()) {
                data.append('&');
                data.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
                data.append('=');
                data.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
            }
        } catch (UnsupportedEncodingException ex) {
            Logger.appError(ErrorLevel.MEDIUM, "URLEncoder doesn't support UTF-8", ex);
        }
        
        return getPage(url, data.length() == 0 ? "" : data.substring(1));
    }
    
    /**
     * Downloads the specified page to disk.
     * 
     * @param url The URL to retrieve
     * @param file The file to save the page to
     * @throws java.io.IOException If there's an I/O error while downloading
     */    
    public static void downloadPage(final String url, final String file)
            throws IOException {    
        downloadPage(url, file, null);
    }
    
    /**
     * Downloads the specified page to disk.
     * 
     * @param url The URL to retrieve
     * @param file The file to save the page to
     * @param listener The progress listener for this download
     * @throws java.io.IOException If there's an I/O error while downloading
     */    
    public static void downloadPage(final String url, final String file,
            final DownloadListener listener) throws IOException {
                
        final URLConnection urlConn = getConnection(url, "");
        final File myFile = new File(file);

        OutputStream output = null;
        InputStream input = null;

        try {
            output = new FileOutputStream(myFile);
            input = urlConn.getInputStream();
            final int length = urlConn.getContentLength();
            int current = 0;

            if (listener != null) {
                listener.setIndeterminate(length == -1);
            }

            final byte[] buffer = new byte[512];
            int count;

            do {
                count = input.read(buffer);

                if (count > 0) {
                    current += count;
                    output.write(buffer, 0, count);

                    if (listener != null && length != -1) {
                        listener.downloadProgress(100 * (float) current / length);
                    }
                }
            } while (count > 0);
        } finally {
            StreamUtil.close(input);
            StreamUtil.close(output);
        }
    }
    
    /**
     * Creates an URL connection for the specified URL and data.
     * 
     * @param url The URL to connect to
     * @param postData The POST data to pass to the URL
     * @return An URLConnection for the specified URL/data
     * @throws java.net.MalformedURLException If the specified URL is malformed
     * @throws java.io.IOException If an I/O exception occurs while connecting
     */
    private static URLConnection getConnection(final String url, final String postData)
            throws MalformedURLException, IOException {
        final URL myUrl = new URL(url);
        final URLConnection urlConn = myUrl.openConnection();
        
        urlConn.setUseCaches(false);
        urlConn.setDoInput(true);
        urlConn.setDoOutput(postData.length() > 0);
        urlConn.setConnectTimeout(10000);
        
        if (postData.length() > 0) {
            urlConn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            
            DataOutputStream out = null;
            
            try {
                out = new DataOutputStream(urlConn.getOutputStream());
                out.writeBytes(postData);
                out.flush();
            } finally {
                StreamUtil.close(out);
            }
        }
        
        return urlConn;
    }
    
}