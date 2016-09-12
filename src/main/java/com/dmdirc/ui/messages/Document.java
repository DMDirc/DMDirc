package com.dmdirc.ui.messages;

import com.dmdirc.events.DisplayPropertyMap;
import java.time.LocalDateTime;

/**
 * Models the content of a window, as a series of lines.
 */
public interface Document {

    /**
     * Returns the number of lines in this document.
     *
     * @return Number of lines
     */
    int getNumLines();

    /**
     * Returns the Line at the specified number.
     *
     * @param lineNumber Line number to retrieve
     *
     * @return Line at the specified number or null
     */
    Line getLine(int lineNumber);

    /**
     * Adds the stylised string to the canvas.
     *
     * @param timestamp The timestamp to show along with the text.
     * @param displayPropertyMap The display properties to use
     * @param text stylised string to add to the document
     */
    void addText(LocalDateTime timestamp, DisplayPropertyMap displayPropertyMap,
        String text);

    /**
     * Trims the document to the specified number of lines.
     *
     * @param numLines Number of lines to trim the document to
     */
    void trim(int numLines);

    /** Clears all lines from the document. */
    void clear();

    /**
     * Adds a DocumentListener to the listener list.
     *
     * @param listener Listener to add
     */
    void addIRCDocumentListener(DocumentListener listener);

    /**
     * Removes a DocumentListener from the listener list.
     *
     * @param listener Listener to remove
     */
    void removeIRCDocumentListener(DocumentListener listener);

    /**
     * Returns the line height of the specified.
     *
     * @param line Line
     *
     * @return Line height
     */
    int getLineHeight(int line);

}
