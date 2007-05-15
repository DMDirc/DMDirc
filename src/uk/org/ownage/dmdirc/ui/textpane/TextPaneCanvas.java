package uk.org.ownage.dmdirc.ui.textpane;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.font.FontRenderContext;
import java.awt.font.LineBreakMeasurer;
import java.awt.font.TextHitInfo;
import java.awt.font.TextLayout;
import java.text.AttributedCharacterIterator;
import java.util.LinkedHashMap;
import java.util.Map;


/** Canvas object to draw text. */
class TextPaneCanvas extends Canvas implements MouseListener, MouseMotionListener {
    
    /**
     * A version number for this class. It should be changed whenever the
     * class structure is changed (or anything else that would prevent
     * serialized objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 2;
    
    /** Font render context to be used for the text in this pane. */
    private final FontRenderContext defaultFRC = new FontRenderContext(null, false, false);
    
    /** IRCDocument. */
    private IRCDocument ircDocument;
    
    /** line break measurer, used for line wrapping. */
    private LineBreakMeasurer lineMeasurer;
    
    /** start character of a paragraph. */
    private int paragraphStart;
    /** end character of a paragraph. */
    private int paragraphEnd;
    /** position of the scrollbar. */
    private int scrollBarPosition;
    /** parent textpane. */
    private TextPane textPane;
    
    /**
     * Creates a new text pane canvas.
     * @param parent parent text pane for the canvas
     */
    public TextPaneCanvas(final TextPane parent,
            final IRCDocument ircDocument) {
        this.ircDocument = ircDocument;
        scrollBarPosition = 0;
        textPane = parent;
        this.addMouseListener(this);
        this.addMouseMotionListener(this);
    }
    
    /**
     * Paints the text onto the canvas.
     * @param g graphics object to draw onto
     */
    public void paint(final Graphics g) {
        final Graphics2D graphics2D = (Graphics2D) g;
        
        final float formatWidth = getWidth();
        final float formatHeight = getHeight();
        
        float drawPosY = formatHeight;
        
        int startLine = scrollBarPosition;
        if (startLine >= ircDocument.getNumLines()) {
            startLine = ircDocument.getNumLines() - 1;
        }
        if (startLine <= 0) {
            startLine = 0;
        }
        
        for (int i = startLine; i >= 0; i--) {
            final AttributedCharacterIterator iterator = ircDocument.getLine(i).getIterator();
            paragraphStart = iterator.getBeginIndex();
            paragraphEnd = iterator.getEndIndex();
            lineMeasurer = new LineBreakMeasurer(iterator, defaultFRC);
            lineMeasurer.setPosition(paragraphStart);
            
            while (lineMeasurer.getPosition() < paragraphEnd) {
                
                final TextLayout layout = lineMeasurer.nextLayout(formatWidth);
                drawPosY -= layout.getDescent() + layout.getLeading();
                
                float drawPosX;
                if (layout.isLeftToRight()) {
                    drawPosX = 0;
                } else {
                    drawPosX = formatWidth - layout.getAdvance();
                }
                
                drawPosY -= layout.getAscent();
                
                if (drawPosY + layout.getAscent() >= 0 || (drawPosY + layout.getDescent() + layout.getLeading()) <= formatHeight) {
                    layout.draw(graphics2D, drawPosX, drawPosY + layout.getAscent());
                }
            }
            if (drawPosY <= 0) {
                break;
            }
        }
    }
    
    /**
     * Repaints the canvas offscreen.
     * @param g graphics object to draw onto
     */
    public void update(final Graphics g) {
        final Image offScreen = this.createImage(getWidth(), getHeight());
        final Graphics graphics = offScreen.getGraphics();
        
        graphics.clearRect(0, 0, this.getWidth(), this.getHeight());
        
        paint(graphics);
        
        g.drawImage(offScreen, 0, 0, this);
    }
    
    /**
     * sets the position of the scroll bar, and repaints if required.
     * @param position scroll bar position
     */
    public void setScrollBarPosition(final int position) {
        if (scrollBarPosition != position) {
            scrollBarPosition = position;
            this.repaint();
        }
    }
    
    /** {@inheritDoc}. */
    public void mouseClicked(final MouseEvent e) {
        //Ignore
    }
    
    /** {@inheritDoc}. */
    public void mousePressed(final MouseEvent e) {
    }
    
    /** {@inheritDoc}. */
    public void mouseReleased(final MouseEvent e) {
    }
    
    /** {@inheritDoc}. */
    public void mouseEntered(final MouseEvent e) {
        //Ignore
    }
    
    /** {@inheritDoc}. */
    public void mouseExited(final MouseEvent e) {
        //Ignore
    }
    
    /** {@inheritDoc}. */
    public void mouseDragged(final MouseEvent e) {
    }
    
    /** {@inheritDoc}. */
    public void mouseMoved(final MouseEvent e) {
        //Ignore
    }
}