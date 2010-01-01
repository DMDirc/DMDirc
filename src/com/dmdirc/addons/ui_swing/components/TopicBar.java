/*
 * 
 * Copyright (c) 2006-2010 Chris Smith, Shane Mc Cormack, Gregory Holmes
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

package com.dmdirc.addons.ui_swing.components;

import com.dmdirc.Channel;
import com.dmdirc.addons.ui_swing.SwingController;
import com.dmdirc.addons.ui_swing.UIUtilities;
import com.dmdirc.addons.ui_swing.actions.NoNewlinesPasteAction;
import com.dmdirc.addons.ui_swing.components.frames.ChannelFrame;
import com.dmdirc.config.IdentityManager;
import com.dmdirc.interfaces.ConfigChangeListener;
import com.dmdirc.parser.interfaces.ChannelInfo;
import com.dmdirc.parser.interfaces.Parser;
import com.dmdirc.parser.interfaces.callbacks.ChannelTopicListener;
import com.dmdirc.ui.IconManager;
import com.dmdirc.ui.messages.ColourManager;
import com.dmdirc.ui.messages.Styliser;
import com.dmdirc.util.URLHandler;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.AbstractAction;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.AbstractDocument;
import javax.swing.text.BadLocationException;
import javax.swing.text.BoxView;
import javax.swing.text.ComponentView;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Element;
import javax.swing.text.GlyphView;
import javax.swing.text.IconView;
import javax.swing.text.LabelView;
import javax.swing.text.ParagraphView;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import javax.swing.text.StyledEditorKit;
import javax.swing.text.View;
import javax.swing.text.ViewFactory;

import net.miginfocom.swing.MigLayout;

/**
 * Component to show and edit topics for a channel.
 */
public class TopicBar extends JComponent implements ActionListener,
        ConfigChangeListener, ChannelTopicListener, HyperlinkListener,
        MouseListener, DocumentListener {

    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 1;
    /** Topic text. */
    private final TextPaneInputField topicText;
    /** Edit button. */
    private final JButton topicEdit;
    /** Cancel button. */
    private final JButton topicCancel;
    /** Associated channel. */
    private Channel channel;
    /** Controller. */
    private SwingController controller;
    /** Empty Attrib set. */
    private SimpleAttributeSet as;
    /** Foreground Colour. */
    private Color foregroundColour;
    /** Background Colour. */
    private Color backgroundColour;
    /** the maximum length allowed for a topic. */
    private int topicLengthMax;
    /** Error icon. */
    private final JLabel errorIcon;

    /**
     * Instantiates a new topic bar.
     *
     * @param channelFrame Parent channel frame
     */
    public TopicBar(final ChannelFrame channelFrame) {
        this.channel = channelFrame.getChannel();
        controller = channelFrame.getController();
        topicText = new TextPaneInputField();
        topicLengthMax = channel.getServer().getParser().getMaxTopicLength();
        errorIcon =
                new JLabel(IconManager.getIconManager().getIcon("input-error"));
        //TODO issue 3251
        //if (channelFrame.getConfigManager().getOptionBool(controller.
        //        getDomain(), "showfulltopic")) {
        //    topicText.setEditorKit(new StyledEditorKit());
        //} else {
        topicText.setEditorKit(new WrapEditorKit());
        //}
        ((DefaultStyledDocument) topicText.getDocument()).setDocumentFilter(
                new NewlinesDocumentFilter());

        topicText.getActionMap().put("paste-from-clipboard",
                new NoNewlinesPasteAction());
        topicEdit = new ImageButton("edit", IconManager.getIconManager().
                getIcon("edit-inactive"), IconManager.getIconManager().
                getIcon("edit"));
        topicCancel = new ImageButton("cancel", IconManager.getIconManager().
                getIcon("close"), IconManager.getIconManager().
                getIcon("close-active"));

        new SwingInputHandler(topicText, channelFrame.getCommandParser(),
                channelFrame).setTypes(false,
                false, true, false);

        topicText.setFocusable(false);
        topicText.setEditable(false);
        topicCancel.setVisible(false);

        final JScrollPane sp = new JScrollPane(topicText);
        sp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        sp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

        setLayout(new MigLayout("fillx, ins 0, hidemode 3"));
        add(sp, "growx, pushx");
        add(errorIcon, "");
        add(topicCancel, "");
        add(topicEdit, "");


        channel.getChannelInfo().getParser().getCallbackManager().addCallback(
                ChannelTopicListener.class, this, channel.getChannelInfo().
                getName());
        topicText.addActionListener(this);
        topicEdit.addActionListener(this);
        topicCancel.addActionListener(this);
        topicText.getInputMap().put(KeyStroke.getKeyStroke("ENTER"),
                "enterButton");
        topicText.getActionMap().put("enterButton", new AbstractAction(
                "enterButton") {

            private static final long serialVersionUID = 1;

            /** {@inheritDoc} */
            @Override
            public void actionPerformed(ActionEvent e) {
                TopicBar.this.actionPerformed(e);
            }
        });
        topicText.getInputMap().put(KeyStroke.getKeyStroke("ESCAPE"),
                "escapeButton");
        topicText.getActionMap().put("escapeButton", new AbstractAction(
                "escapeButton") {

            private static final long serialVersionUID = 1;

            /** {@inheritDoc} */
            @Override
            public void actionPerformed(ActionEvent e) {
                e.setSource(topicCancel);
                TopicBar.this.actionPerformed(e);
            }
        });
        topicText.addHyperlinkListener(this);
        topicText.addMouseListener(this);
        topicText.getDocument().addDocumentListener(this);
        IdentityManager.getGlobalConfig().addChangeListener(
                "ui", "backgroundcolour", this);
        IdentityManager.getGlobalConfig().addChangeListener(
                "ui", "foregroundcolour", this);
        IdentityManager.getGlobalConfig().addChangeListener(
                "ui", "inputbackgroundcolour", this);
        IdentityManager.getGlobalConfig().addChangeListener(
                "ui", "inputforegroundcolour", this);
        IdentityManager.getGlobalConfig().addChangeListener(
                controller.getDomain(), "showfulltopic", this);
        IdentityManager.getGlobalConfig().addChangeListener(
                controller.getDomain(), "hideEmptyTopicBar", this);
        setColours();
    }

    /** {@inheritDoc} */
    @Override
    public void onChannelTopic(final Parser tParser, ChannelInfo cChannel,
            boolean bIsJoinTopic) {
        topicChanged();
    }

    /**
     * Topic has changed, update topic.
     */
    private void topicChanged() {
        topicText.setText("");
        setAttributes();
        ((DefaultStyledDocument) topicText.getDocument()).setCharacterAttributes(
                0, Integer.MAX_VALUE, as, true);
        if (channel.getCurrentTopic() != null) {
            Styliser.addStyledString((StyledDocument) topicText.getDocument(),
                    new String[]{Styliser.CODE_HEXCOLOUR + ColourManager.getHex(
                        foregroundColour) + channel.getCurrentTopic().getTopic(),},
                    as);
        }
        if (channel.getConfigManager().getOptionBool(controller.getDomain(),
                "hideEmptyTopicBar")) {
            setVisible(topicText.getDocument().getLength() != 0);
        }
        topicText.setCaretPosition(0);
        validateTopic();
    }

    /**
     * {@inheritDoc}
     *
     * @param e Action event
     */
    @Override
    public void actionPerformed(final ActionEvent e) {
        if (e.getSource() == topicEdit || e.getSource() == topicText) {
            if (topicText.isEditable()) {
                channel.setTopic(topicText.getText());
                ((ChannelFrame) channel.getFrame()).getInputField().
                        requestFocusInWindow();
                topicChanged();
                topicText.setFocusable(false);
                topicText.setEditable(false);
                topicCancel.setVisible(false);
            } else {
                topicText.setVisible(false);
                topicText.setText("");
                setAttributes();
                ((DefaultStyledDocument) topicText.getDocument()).
                        setCharacterAttributes(
                        0, Integer.MAX_VALUE, as, true);
                if (channel.getCurrentTopic() != null) {
                    topicText.setText(channel.getCurrentTopic().getTopic());
                }
                topicText.setCaretPosition(0);
                topicText.setFocusable(true);
                topicText.setEditable(true);
                topicText.setVisible(true);
                topicText.requestFocusInWindow();
                topicCancel.setVisible(true);
            }
        } else if (e.getSource() == topicCancel) {
            topicText.setFocusable(false);
            topicText.setEditable(false);
            topicCancel.setVisible(false);
            ((ChannelFrame) channel.getFrame()).getInputField().
                    requestFocusInWindow();
            topicChanged();
        }
    }

    /** {@inheritDoc} */
    @Override
    public void hyperlinkUpdate(final HyperlinkEvent e) {
        if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
            URLHandler.getURLHander().launchApp(e.getURL());
        }
    }

    private void setColours() {
        backgroundColour = channel.getConfigManager().getOptionColour(
                "ui", "inputbackgroundcolour", "ui", "backgroundcolour");
        foregroundColour = channel.getConfigManager().getOptionColour(
                "ui", "inputforegroundcolour", "ui", "foregroundcolour");
        setBackground(backgroundColour);
        setForeground(foregroundColour);
        setDisabledTextColour(foregroundColour);
        setCaretColor(foregroundColour);
        setAttributes();
    }

    private void setAttributes() {
        as = new SimpleAttributeSet();
        StyleConstants.setFontFamily(as, topicText.getFont().getFamily());
        StyleConstants.setFontSize(as, topicText.getFont().getSize());
        StyleConstants.setBackground(as, backgroundColour);
        StyleConstants.setForeground(as, foregroundColour);
        StyleConstants.setUnderline(as, false);
        StyleConstants.setBold(as, false);
        StyleConstants.setItalic(as, false);
    }

    /**
     * Sets the caret position in this topic bar.
     *
     * @param position New position
     */
    public void setCaretPosition(final int position) {
        UIUtilities.invokeLater(new Runnable() {

            /** {@inheritDoc} */
            @Override
            public void run() {
                topicText.setCaretPosition(position);
            }
        });
    }

    /**
     * Sets the caret colour to the specified coloour.
     *
     * @param optionColour Colour for the caret
     */
    public void setCaretColor(final Color optionColour) {
        UIUtilities.invokeLater(new Runnable() {

            /** {@inheritDoc} */
            @Override
            public void run() {
                topicText.setCaretColor(optionColour);
            }
        });
    }

    /**
     * Sets the foreground colour to the specified coloour.
     *
     * @param optionColour Colour for the foreground
     */
    @Override
    public void setForeground(final Color optionColour) {
        UIUtilities.invokeLater(new Runnable() {

            /** {@inheritDoc} */
            @Override
            public void run() {
                topicText.setForeground(optionColour);
            }
        });
    }

    /**
     * Sets the disabled text colour to the specified coloour.
     *
     * @param optionColour Colour for the disabled text
     */
    public void setDisabledTextColour(final Color optionColour) {
        UIUtilities.invokeLater(new Runnable() {

            /** {@inheritDoc} */
            @Override
            public void run() {
                topicText.setDisabledTextColor(optionColour);
            }
        });
    }

    /**
     * Sets the background colour to the specified coloour.
     *
     * @param optionColour Colour for the caret
     */
    @Override
    public void setBackground(final Color optionColour) {
        UIUtilities.invokeLater(new Runnable() {

            /** {@inheritDoc} */
            @Override
            public void run() {
                topicText.setBackground(optionColour);
            }
        });
    }

    /** {@inheritDoc} */
    @Override
    public void configChanged(String domain, String key) {
        //TODO issue 3251
        //if ("showfulltopic".equals(key)) {
        //    if (channel.getConfigManager().getOptionBool(controller.getDomain(),
        //            "showfulltopic")) {
        //        topicText.setEditorKit(new StyledEditorKit());
        //    } else {
        //        topicText.setEditorKit(new WrapEditorKit());
        //    }
        //    ((DefaultStyledDocument) topicText.getDocument()).setDocumentFilter(
        //            new NewlinesDocumentFilter());
        //}
        setColours();
        if ("hideEmptyTopicBar".equals(key)) {
            setVisible(true);
            if (channel.getConfigManager().getOptionBool(controller.getDomain(),
                    "hideEmptyTopicBar")) {
                setVisible(topicText.getDocument().getLength() != 0);
            }
        }
    }

    /**
     * Closes this topic bar.
     */
    public void close() {
        channel.getChannelInfo().getParser().getCallbackManager().delCallback(
                ChannelTopicListener.class, this);
    }

    /**
     * Validates the topic text and shows errors as appropriate.
     */
    public void validateTopic() {
        UIUtilities.invokeLater(new Runnable() {

            /** {@inheritDoc} */
            @Override
            public void run() {
                if (topicText.isEditable()) {
                    final int charsLeft = topicLengthMax - topicText.getText().
                            length();
                    if (charsLeft < 0) {
                        errorIcon.setVisible(true);
                        errorIcon.setToolTipText("Topic too long: " + topicText.
                                getText().length() + " of " + topicLengthMax);
                    } else {
                        errorIcon.setVisible(false);
                        errorIcon.setToolTipText(null);
                    }
                } else {
                    errorIcon.setVisible(false);
                }
            }
        });
    }

    /**
     * {@inheritDoc}
     *
     * @param e Mouse event
     */
    @Override
    public void mouseClicked(MouseEvent e) {
        if (e.getClickCount() == 2) {
            topicEdit.doClick();
        }
    }

    /**
     * {@inheritDoc}
     *
     * @param e Mouse event
     */
    @Override
    public void mousePressed(final MouseEvent e) {
        //Ignore
    }

    /**
     * {@inheritDoc}
     *
     * @param e Mouse event
     */
    @Override
    public void mouseReleased(final MouseEvent e) {
        //Ignore
    }

    /**
     * {@inheritDoc}
     *
     * @param e Mouse event
     */
    @Override
    public void mouseEntered(final MouseEvent e) {
        //Ignore
    }

    /**
     * {@inheritDoc}
     *
     * @param e Mouse event
     */
    @Override
    public void mouseExited(final MouseEvent e) {
        //Ignore
    }

    /** {@inheritDoc} */
    @Override
    public void insertUpdate(final DocumentEvent e) {
        validateTopic();
    }

    /** {@inheritDoc} */
    @Override
    public void removeUpdate(final DocumentEvent e) {
        validateTopic();
    }

    /** {@inheritDoc} */
    @Override
    public void changedUpdate(final DocumentEvent e) {
        validateTopic();
    }
}

/**
 * @author Stanislav Lapitsky
 * @version 1.0
 */
class WrapEditorKit extends StyledEditorKit {

    private static final long serialVersionUID = 1;
    private ViewFactory defaultFactory = new WrapColumnFactory();

    /** {@inheritDoc} */
    @Override
    public ViewFactory getViewFactory() {
        return defaultFactory;
    }
}

/**
 * @author Stanislav Lapitsky
 * @version 1.0
 */
class WrapColumnFactory implements ViewFactory {

    /** {@inheritDoc} */
    @Override
    public View create(final Element elem) {
        String kind = elem.getName();
        if (kind != null) {
            if (kind.equals(AbstractDocument.ContentElementName)) {
                return new WrapLabelView(elem);
            } else if (kind.equals(AbstractDocument.ParagraphElementName)) {
                return new NoWrapParagraphView(elem);
            } else if (kind.equals(AbstractDocument.SectionElementName)) {
                return new BoxView(elem, View.Y_AXIS);
            } else if (kind.equals(StyleConstants.ComponentElementName)) {
                return new ComponentView(elem);
            } else if (kind.equals(StyleConstants.IconElementName)) {
                return new IconView(elem);
            }
        }

        // default to text display
        return new LabelView(elem);
    }
}

/**
 * @author Stanislav Lapitsky
 * @version 1.0
 */
class NoWrapParagraphView extends ParagraphView {

    /**
     * Creates a new no wrap paragraph view.
     *
     * @param elem Element to view
     */
    public NoWrapParagraphView(final Element elem) {
        super(elem);
    }

    /** {@inheritDoc} */
    @Override
    public void layout(final int width, final int height) {
        super.layout(Short.MAX_VALUE, height);
    }

    /** {@inheritDoc} */
    @Override
    public float getMinimumSpan(final int axis) {
        return super.getPreferredSpan(axis);
    }
}

/**
 * @author Stanislav Lapitsky
 * @version 1.0
 */
class WrapLabelView extends LabelView {

    /**
     * Creates a new wrap label view.
     *
     * @param elem Element to view
     */
    public WrapLabelView(final Element elem) {
        super(elem);
    }

    /** {@inheritDoc} */
    @Override
    public int getBreakWeight(final int axis, final float pos, final float len) {
        if (axis == View.X_AXIS) {
            checkPainter();
            int p0 = getStartOffset();
            int p1 = getGlyphPainter().getBoundedPosition(this, p0, pos, len);
            if (p1 == p0) {
                // can't even fit a single character
                return View.BadBreakWeight;
            }
            try {
                //if the view contains line break char return forced break
                if (getDocument().getText(p0, p1 - p0).indexOf("\r") >= 0) {
                    return View.ForcedBreakWeight;
                }
            } catch (BadLocationException ex) {
                //should never happen
            }
        }
        return super.getBreakWeight(axis, pos, len);
    }

    /** {@inheritDoc} */
    @Override
    public View breakView(final int axis, final int p0, final float pos,
            final float len) {
        if (axis == View.X_AXIS) {
            checkPainter();
            int p1 = getGlyphPainter().getBoundedPosition(this, p0, pos, len);
            try {
                //if the view contains line break char break the view
                int index = getDocument().getText(p0, p1 - p0).indexOf("\r");
                if (index >= 0) {
                    GlyphView v = (GlyphView) createFragment(p0, p0 + index + 1);
                    return v;
                }
            } catch (BadLocationException ex) {
                //should never happen
            }
        }
        return super.breakView(axis, p0, pos, len);
    }
}
       