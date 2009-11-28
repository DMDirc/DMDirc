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

package com.dmdirc.addons.ui_swing.components.colours;

import com.dmdirc.ui.messages.ColourManager;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;

/**
 * The ColourPickerPanel allows users to pick either an IRC colour or a hex
 * colour visually.
 * @author chris
 */
public final class ColourPickerPanel extends JPanel implements MouseListener,
        MouseMotionListener, MouseWheelListener {

    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 1;
    /** ActionEvent ID for when a hex colour is selected. */
    public static final int ACTION_HEX = 10001;
    /** ActionEvent ID for when an irc colour is selected. */
    public static final int ACTION_IRC = 10002;
    /** The width of each IRC colour patch. */
    private static final int IRC_WIDTH = 9;
    /** The height of each IRC colour patch. */
    private static final int IRC_HEIGHT = 16;
    /** The width of the hex colour patch. */
    private static final int HEX_WIDTH = 125;
    /** The height of the hex colour patch. */
    private static final int HEX_HEIGHT = 125;
    /** The size of borders to use. */
    private static final int BORDER_SIZE = 7;
    /** The size of slider to use. */
    private static final int SLIDER_WIDTH = 10;
    /** The height of the preview area. */
    private static final int PREVIEW_HEIGHT = 20;
    /** Whether to show IRC colours. */
    private final boolean showIrc;
    /** Whether to show hex colours. */
    private final boolean showHex;
    /** The y-coord of the start of the IRC colours block. */
    private int ircOffset;
    /** The y-coord of the start of the hex colours block. */
    private int hexOffset;
    /** The y-coord of the start of the preview block. */
    private int previewOffset;
    /** The saturation to use. */
    private float saturation = (float) 1.0;
    /** The colour to show in the preview window. */
    private Color preview;
    /** Rectangle we use to indicate that only the preview should be drawn. */
    private Rectangle previewRect;
    /** A list of registered actionlisteners. */
    private final List<ActionListener> listeners =
            new ArrayList<ActionListener>();

    /**
     * Creates a new instance of ColourPickerPanel.
     * @param newShowIrc Whether to show IRC colours or not
     * @param newShowHex Whether to show hex colours or not
     */
    public ColourPickerPanel(final boolean newShowIrc,
            final boolean newShowHex) {
        super();

        this.showIrc = newShowIrc;
        this.showHex = newShowHex;

        final int height = 65 + (showIrc ? 30 : 0) + (showHex ? 145 : 0) + (showHex & showIrc
                ? 10 : 0);

        setPreferredSize(new Dimension(160, height));

        addMouseListener(this);
        addMouseMotionListener(this);
        addMouseWheelListener(this);
    }

    /**
     * Creates a new instance of ColourPickerPanel, showing both IRC and Hex
     * colours.
     */
    public ColourPickerPanel() {
        this(true, true);
    }

    /** {@inheritDoc} */
    @Override
    public void paint(final Graphics g) {
        int offset = 20;

        if (previewRect == null || !previewRect.equals(g.getClipBounds())) {
            g.setColor(getBackground());

            g.fillRect(0, 0, getWidth(), getHeight());

            g.setColor(Color.BLACK);

            if (showIrc) {
                g.drawString("IRC Colours", BORDER_SIZE, offset);

                offset += BORDER_SIZE;

                ircOffset = offset;

                for (int i = 0; i < 16; i++) {
                    g.setColor(ColourManager.getColour(i));
                    g.fillRect(i * IRC_WIDTH + BORDER_SIZE, offset, IRC_WIDTH,
                            IRC_HEIGHT);
                    g.setColor(Color.BLACK);
                    g.drawRect(i * IRC_WIDTH + BORDER_SIZE, offset, IRC_WIDTH,
                            IRC_HEIGHT);
                }

                offset += IRC_HEIGHT + 20;
            }

            if (showHex) {
                g.drawString("Hex Colours", BORDER_SIZE, offset);

                offset += BORDER_SIZE;

                hexOffset = offset;

                for (int i = HEX_WIDTH; i > 0; i--) {
                    for (int j = HEX_HEIGHT; j > 0; j--) {
                        g.setColor(new Color(Color.HSBtoRGB((float) i /
                                HEX_WIDTH, saturation, (float) j / HEX_HEIGHT)));
                        g.drawLine(BORDER_SIZE + i, offset + HEX_HEIGHT - j,
                                BORDER_SIZE + i, offset + HEX_HEIGHT - j);
                    }
                }

                g.setColor(Color.BLACK);
                g.drawRect(BORDER_SIZE, offset, HEX_HEIGHT, HEX_WIDTH);

                g.drawRect(BORDER_SIZE * 2 + HEX_WIDTH, offset, 10, HEX_HEIGHT);

                for (int i = 1; i < HEX_HEIGHT; i++) {
                    g.setColor(new Color(Color.HSBtoRGB(0, (float) i /
                            HEX_HEIGHT, 1)));
                    g.drawLine(BORDER_SIZE * 2 + HEX_WIDTH + 1, offset + i,
                            BORDER_SIZE * 2 + HEX_WIDTH + SLIDER_WIDTH - 1,
                            offset + i);
                }

                final Polygon arrow = new Polygon();

                arrow.addPoint(HEX_WIDTH + BORDER_SIZE * 2 + 4, offset +
                        Math.round(saturation * HEX_HEIGHT));
                arrow.addPoint(HEX_WIDTH + BORDER_SIZE * 2 + 13, offset +
                        Math.round(saturation * HEX_HEIGHT) + 5);
                arrow.addPoint(HEX_WIDTH + BORDER_SIZE * 2 + 13, offset +
                        Math.round(saturation * HEX_HEIGHT) - 5);

                g.setColor(Color.BLACK);
                g.fillPolygon(arrow);

                offset += HEX_HEIGHT + 20;
            }

            g.drawString("Preview", BORDER_SIZE, offset);

            offset += BORDER_SIZE;

            previewOffset = offset;

            if (previewRect == null) {
                previewRect = new Rectangle(0, previewOffset, getWidth(),
                        PREVIEW_HEIGHT);
            }
        } else {
            offset = previewOffset;
        }

        g.drawRect(BORDER_SIZE, offset, getWidth() - BORDER_SIZE * 2,
                PREVIEW_HEIGHT);

        if (preview == null) {
            g.setColor(getBackground());
            g.fillRect(BORDER_SIZE + 1, offset + 1,
                    getWidth() - BORDER_SIZE * 2 - 1, PREVIEW_HEIGHT - 1);
            g.setColor(Color.BLACK);
            g.drawLine(BORDER_SIZE, offset, getWidth() - BORDER_SIZE, offset +
                    PREVIEW_HEIGHT);
        } else {
            g.setColor(preview);
            g.fillRect(BORDER_SIZE + 1, offset + 1,
                    getWidth() - BORDER_SIZE * 2 - 1, PREVIEW_HEIGHT - 1);
        }
    }

    /**
     * Retrieves the hex colour beneath the mouse. It is assumed that this
     * method is only called if the mouse is within the hex area.
     * @param e The mouse event that triggered this call
     * @return A colour object representing the colour beneat the mouse
     */
    private Color getHexColour(final MouseEvent e) {
        final int i = e.getX() - BORDER_SIZE;
        final int j = HEX_HEIGHT - (e.getY() - hexOffset);

        return new Color(Color.HSBtoRGB((float) i / HEX_WIDTH, saturation,
                (float) j / HEX_HEIGHT));
    }

    /**
     * Retrieves the irc colour beneath the mouse. It is assumed that this
     * method is only called if the mouse is within the irc colour area.
     * @param e The mouse event that triggered this call
     * @return A colour object representing the colour beneat the mouse
     */
    private Color getIrcColour(final MouseEvent e) {
        final int i = (e.getX() - BORDER_SIZE) / IRC_WIDTH;

        return ColourManager.getColour(i);
    }

    /**
     * Adds an action listener to this object. Action events are generated (and
     * passed to all action listeners) when the user selects a colour. The two
     * IDs used by this object are ACTION_HEX and ACTION_IRC, to indicate a
     * hex colour or an irc colour was selected, respectively.
     * @param listener The action listener to register
     */
    public void addActionListener(final ActionListener listener) {
        listeners.add(listener);
    }

    /**
     * Removes an action listener from this object.
     * @param listener The listener to be removed
     */
    public void removeActionListener(final ActionListener listener) {
        listeners.remove(listener);
    }

    /**
     * Throws a new action event to all listeners.
     * @param id The id of the action
     * @param message The 'message' to use for the event
     */
    private void throwAction(final int id, final String message) {
        final ActionEvent event = new ActionEvent(this, id, message);

        for (ActionListener listener : listeners) {
            listener.actionPerformed(event);
        }
    }

    /**
     * Converts the specified integer (in the range 0-255) into a hex string.
     * @param value The integer to convert
     * @return A char digit hex string representing the specified integer
     */
    private String toHex(final int value) {
        final char[] chars = {
            '0', '1', '2', '3', '4', '5', '6', '7',
            '8', '9', 'A', 'B', 'C', 'D', 'E', 'F',
        };

        return ("" + chars[value / 16]) + chars[value % 16];
    }

    /** 
     * {@inheritDoc}
     * 
     * @param e Mouse event 
     */
    @Override
    public void mouseClicked(final MouseEvent e) {
        if (showIrc && e.getY() > ircOffset && e.getY() < ircOffset + IRC_HEIGHT &&
                e.getX() > BORDER_SIZE && e.getX() < BORDER_SIZE + 16 *
                IRC_WIDTH) {

            final int i = (e.getX() - BORDER_SIZE) / IRC_WIDTH;

            throwAction(ACTION_IRC, "" + i);

        } else if (showHex && e.getY() > hexOffset && e.getY() < hexOffset +
                HEX_HEIGHT) {

            if (e.getX() > BORDER_SIZE && e.getX() < BORDER_SIZE + HEX_WIDTH) {

                final Color color = getHexColour(e);

                throwAction(ACTION_HEX, toHex(color.getRed()) +
                        toHex(color.getGreen()) + toHex(color.getBlue()));

            } else if (e.getX() > BORDER_SIZE * 2 + HEX_WIDTH && e.getX() <
                    BORDER_SIZE * 3 + HEX_WIDTH + SLIDER_WIDTH) {
                saturation = (float) (e.getY() - hexOffset) / 125;
                repaint();
            }
        }
    }

    /** 
     * {@inheritDoc}
     * 
     * @param e Mouse event 
     */
    @Override
    public void mousePressed(final MouseEvent e) {
    // Do nothing
    }

    /** 
     * {@inheritDoc}
     * 
     * @param e Mouse event 
     */
    @Override
    public void mouseReleased(final MouseEvent e) {
    // Do nothing
    }

    /** 
     * {@inheritDoc}
     * 
     * @param e Mouse event 
     */
    @Override
    public void mouseEntered(final MouseEvent e) {
    // Do nothing
    }

    /** 
     * {@inheritDoc}
     * 
     * @param e Mouse event 
     */
    @Override
    public void mouseExited(final MouseEvent e) {
    // Do nothing
    }

    /** 
     * {@inheritDoc}
     * 
     * @param e Mouse event 
     */
    @Override
    public void mouseDragged(final MouseEvent e) {
    // Do nothing
    }

    /** 
     * {@inheritDoc}
     * 
     * @param e Mouse event 
     */
    @Override
    public void mouseMoved(final MouseEvent e) {
        if (showIrc && e.getY() > ircOffset && e.getY() < ircOffset + IRC_HEIGHT &&
                e.getX() > BORDER_SIZE && e.getX() < BORDER_SIZE + 16 *
                IRC_WIDTH) {
            preview = getIrcColour(e);
        } else if (showHex && e.getY() > hexOffset && e.getY() < hexOffset +
                HEX_HEIGHT && e.getX() > BORDER_SIZE && e.getX() < BORDER_SIZE +
                HEX_WIDTH) {
            preview = getHexColour(e);
        } else {
            preview = null;
        }

        repaint(previewRect);
    }

    /** 
     * {@inheritDoc}
     * 
     * @param e Mouse event 
     */
    @Override
    public void mouseWheelMoved(final MouseWheelEvent e) {
        if (showHex) {
            saturation = saturation + (e.getWheelRotation() >= 0 ? 0.02f : -0.02f);

            if (saturation < 0) {
                saturation = 0f;
            }
            if (saturation > 1) {
                saturation = 1f;
            }

            mouseMoved(e);
            repaint();
        }
    }
}
