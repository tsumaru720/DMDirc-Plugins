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

package com.dmdirc.addons.ui_swing.dialogs.about;

import com.dmdirc.plugins.PluginInfo;
import java.awt.Color;

import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;

/**
 * Simple renderer to show licence info in the tree.
 */
public class LicenceRenderer extends DefaultTreeCellRenderer {

    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 1;

    /** {@inheritDoc} */
    @Override
    public Component getTreeCellRendererComponent(final JTree tree,
            final Object value, final boolean sel, final boolean expanded,
            final boolean leaf, final int row, final boolean hasFocus) {
        final JLabel label = (JLabel) super.getTreeCellRendererComponent(tree,
                value, sel, expanded, leaf, row, hasFocus);
        label.setIcon(null);
        label.setOpaque(false);
        if (((DefaultMutableTreeNode) value).getUserObject()
                instanceof PluginInfo) {
            setText(((PluginInfo) ((DefaultMutableTreeNode) value)
                    .getUserObject()).getNiceName());
        }
        return label;
    }

    /**
     * {@inheritDoc}
     *
     * @return Background colour for non selected nodes
     */
    @Override
    public Color getBackgroundNonSelectionColor() {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public Color getBackground() {
        return null;
    }
}
