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

package com.dmdirc.addons.relaybot;

import com.dmdirc.config.IdentityManager;
import com.dmdirc.config.prefs.PreferencesInterface;
import com.dmdirc.plugins.Plugin;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;

import net.miginfocom.swing.MigLayout;

/**
 * Panel used for the relay bot channel and nickname settings in the plugin's
 * config dialog.
 */
public class RelayChannelPanel extends JPanel implements ActionListener,
        PreferencesInterface, ListSelectionListener {

    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 1;
    /** The table used for displaying the options. */
    private final JTable table;
    /** The plugin we're associated with. */
    private final transient RelayBotPlugin plugin;
    /** The table headings. */
    private static final String[] HEADERS = {"Channel", "Nickname", };
    /** Delete button. */
    private final JButton deleteButton;

    /**
     * Creates a new instance of NickColourPanel.
     *
     * @param controller The UI controller that owns this panel
     * @param plugin The plugin that owns this panel
     */
    public RelayChannelPanel(final Plugin controller,
            final RelayBotPlugin plugin) {
        super();

        this.plugin = plugin;

        final Object[][] data = plugin.getData();

        table = new JTable(new DefaultTableModel(data, HEADERS));

        final JScrollPane scrollPane = new JScrollPane(table);

        table.getSelectionModel().addListSelectionListener(this);
        table.setFillsViewportHeight(true);
        int height = 100;
        try {
            final Method getPrefsDialog = controller.getClass()
                    .getDeclaredMethod("getPrefsDialog", (Class<?>) null);
            final Object prefsDialog = getPrefsDialog.invoke(controller,
                    (Class<?>) null);
            final Method getPanelHeight = prefsDialog.getClass()
                    .getDeclaredMethod("getPanelHeight", (Class<?>) null);
            final Object panelHeight = getPanelHeight.invoke(prefsDialog,
                    (Class<?>) null);
            height = (Integer) panelHeight;
        } catch (IllegalAccessException ex) {
            height = 100;
        } catch (IllegalArgumentException ex) {
            height = 100;
        } catch (InvocationTargetException ex) {
            height = 100;
        } catch (NoSuchMethodException ex) {
            height = 100;
        } catch (SecurityException ex) {
            height = 100;
        }
        setLayout(new MigLayout("ins 0, fillx, hmax " + height));
        add(scrollPane, "grow, push, wrap, spanx");

        final JButton addButton = new JButton("Add");
        addButton.addActionListener(this);
        add(addButton, "sg button, growx, pushx");

        deleteButton = new JButton("Delete");
        deleteButton.addActionListener(this);
        add(deleteButton, "sg button, growx, pushx");

        deleteButton.setEnabled(false);
    }

    /**
     * {@inheritDoc}
     *
     * @param e Action event
     */
    @Override
    public void actionPerformed(final ActionEvent e) {
        final DefaultTableModel model = ((DefaultTableModel) table.getModel());

        if (e.getActionCommand().equals("Add")) {
            addRow("#changeme", "changeme");
        } else if (e.getActionCommand().equals("Delete")) {
            final int row = table.getSelectedRow();
            if (table.isEditing()) {
                table.getCellEditor().stopCellEditing();
            }
            if (row > -1) {
                model.removeRow(row);
            }
        }
    }

    /**
     * Removes a row from the table.
     *
     * @param row The row to be removed
     */
    public void removeRow(final int row) {
        ((DefaultTableModel) table.getModel()).removeRow(row);
    }

    /**
     * Adds a row to the table.
     *
     * @param channel The channel setting
     * @param nickname The nickname setting
     */
    public void addRow(final String channel, final String nickname) {
        final DefaultTableModel model = ((DefaultTableModel) table.getModel());
        model.addRow(new Object[]{channel, nickname, });
    }

    /**
     * Retrieves the current data in use by this panel.
     *
     * @return This panel's current data.
     */
    public List<String[]> getData() {
        final List<String[]> res = new ArrayList<String[]>();
        final DefaultTableModel model = ((DefaultTableModel) table.getModel());

        for (int i = 0; i < model.getRowCount(); i++) {
            res.add(new String[]{(String) model.getValueAt(i, 0),
            (String) model.getValueAt(i, 1), });
        }

        return res;
    }

    /** {@inheritDoc} */
    @Override
    public void save() {
        // Remove all old config entries
        for (String[] parts : plugin.getData()) {
           IdentityManager.getConfigIdentity().unsetOption(plugin.getDomain(),
                   parts[0]);
        }

        // And write the new ones
        for (String[] row : getData()) {
            IdentityManager.getConfigIdentity().setOption(plugin.getDomain(),
                    row[0], row[1]);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void valueChanged(final ListSelectionEvent e) {
        deleteButton.setEnabled(table.getSelectedRow() > -1
                && table.getModel().getRowCount() > 0);
    }
}
