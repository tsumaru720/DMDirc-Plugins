/*
 * Copyright (c) 2006-2011 Chris Smith, Shane Mc Cormack, Gregory Holmes
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

package com.dmdirc.addons.ui_swing.components.text;

import java.awt.Color;
import java.awt.Font;
import java.awt.Insets;

import javax.swing.JTextPane;
import javax.swing.UIManager;
import javax.swing.plaf.basic.BasicTextPaneUI;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.StyleSheet;

/**
 * Dynamic text label.
 */
public class TextLabel extends JTextPane {

    /**
     * A version number for this class. It should be changed whenever the
     * class structure is changed (or anything else that would prevent
     * serialized objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 1;
    /** Simple attribute set. */
    private final SimpleAttributeSet sas = new SimpleAttributeSet();

    /**
     * Creates a new instance of TextLabel.
     */
    public TextLabel() {
        this(null, true);
    }

    /**
     * Creates a new instance of TextLabel.
     *
     * @param justified Justify the text?
     */
    public TextLabel(final boolean justified) {
        this(null, justified);
    }

    /**
     * Creates a new instance of TextLabel.
     *
     * @param text Text to display
     */
    public TextLabel(final String text) {
        this(text, true);
    }

    /**
     * Creates a new instance of TextLabel.
     *
     * @param text Text to display
     * @param justified Justify the text?
     */
    public TextLabel(final String text, final boolean justified) {
        super(new DefaultStyledDocument());
        setEditorKit(new DMDircHTMLEditorKit());
        setUI(new BasicTextPaneUI());

        final StyleSheet styleSheet = ((HTMLDocument) getDocument()).
                getStyleSheet();
        final Font font = UIManager.getFont("Label.font");
        final Color colour = UIManager.getColor("Label.foreground");
        styleSheet.addRule("body "
                + "{ font-family: " + font.getFamily() + "; "
                + "font-size: " + font.getSize() + "pt; "
                + "color: rgb(" + colour.getRed() + ", " + colour.getGreen()
                + ", " + colour.getBlue() + "); }");

        setOpaque(false);
        setEditable(false);
        setHighlighter(null);
        setMargin(new Insets(0, 0, 0, 0));

        if (justified) {
            StyleConstants.setAlignment(sas, StyleConstants.ALIGN_JUSTIFIED);
        }

        setText(text);
    }

    /** {@inheritDoc} */
    @Override
    public final StyledDocument getDocument() {
        return (StyledDocument) super.getDocument();
    }

    /** {@inheritDoc} */
    @Override
    public final void setText(final String t) {
        super.setText(t);
        if (t != null && !t.isEmpty()) {
            getDocument().setParagraphAttributes(0, t.length(), sas, false);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void setForeground(final Color colour) {
        if (sas == null) {
            return;
        }
        if (colour != null) {
            StyleConstants.setForeground(sas, colour);
            getDocument().setParagraphAttributes(0, getDocument().getLength(),
                    sas, false);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void setFont(final Font font) {
        super.setFont(font);
        if (sas == null) {
            return;
        }
        if (font != null) {
            StyleConstants.setFontFamily(sas, font.getFamily());
            StyleConstants.setFontSize(sas, font.getSize());
            StyleConstants.setBold(sas, font.isBold());
            StyleConstants.setItalic(sas, font.isItalic());
            getDocument().setParagraphAttributes(0, getDocument().getLength(),
                    sas, false);
        }
    }

    /**
     * Sets the alignment of the text in this label.
     *
     * @param alignment One of the following values
     *                  <ul>
     *                     <li>StyleConstants.ALIGN_CENTER
     *                     <li>StyleConstants.ALIGN_JUSTIFIED
     *                     <li>StyleConstants.ALIGN_LEFT
     *                     <li>StyleConstants.ALIGN_RIGHT
     *                  </ul>
     */
    public void setAlignment(final int alignment) {
        StyleConstants.setAlignment(sas, alignment);
        getDocument().setParagraphAttributes(0, getDocument().getLength(),
                    sas, false);
    }
}
