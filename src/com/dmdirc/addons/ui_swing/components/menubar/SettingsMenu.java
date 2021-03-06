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

package com.dmdirc.addons.ui_swing.components.menubar;

import com.dmdirc.addons.ui_swing.Apple;
import com.dmdirc.addons.ui_swing.MainFrame;
import com.dmdirc.addons.ui_swing.SwingController;
import com.dmdirc.addons.ui_swing.dialogs.actionsmanager.ActionsManagerDialog;
import com.dmdirc.addons.ui_swing.dialogs.aliases.AliasManagerDialog;
import com.dmdirc.addons.ui_swing.dialogs.prefs.SwingPreferencesDialog;
import com.dmdirc.addons.ui_swing.dialogs.profiles.ProfileManagerDialog;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

/**
 * A menu to add settings related commands to the menu bar.
 */
public class SettingsMenu extends JMenu implements ActionListener {

    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 1;
    /** Swing controller. */
    private final SwingController controller;
    /** Main frame. */
    private final MainFrame mainFrame;

    public SettingsMenu(final SwingController controller,
            final MainFrame mainFrame) {
        super("Settings");
        this.controller = controller;
        this.mainFrame = mainFrame;
        setMnemonic('e');
        initSettingsMenu();
    }

    /**
     * Initialises the settings menu.
     */
    private void initSettingsMenu() {
        JMenuItem menuItem;

        if (!Apple.isAppleUI()) {
            menuItem = new JMenuItem();
            menuItem.setText("Preferences");
            menuItem.setMnemonic('p');
            menuItem.setActionCommand("Preferences");
            menuItem.addActionListener(this);
            add(menuItem);
        }

        menuItem = new JMenuItem();
        menuItem.setMnemonic('m');
        menuItem.setText("Profile Manager");
        menuItem.setActionCommand("Profile");
        menuItem.addActionListener(this);
        add(menuItem);

        menuItem = new JMenuItem();
        menuItem.setMnemonic('a');
        menuItem.setText("Actions Manager");
        menuItem.setActionCommand("Actions");
        menuItem.addActionListener(this);
        add(menuItem);

        menuItem = new JMenuItem();
        menuItem.setMnemonic('l');
        menuItem.setText("Alias Manager");
        menuItem.setActionCommand("Aliases");
        menuItem.addActionListener(this);
        add(menuItem);
    }

    /** {@inheritDoc} */
    @Override
    public void actionPerformed(final ActionEvent e) {
        if ("Preferences".equals(e.getActionCommand())) {
            SwingPreferencesDialog.showSwingPreferencesDialog(controller);
        } else if (e.getActionCommand().equals("Profile")) {
            ProfileManagerDialog.showProfileManagerDialog(mainFrame);
        }  else if (e.getActionCommand().equals("Actions")) {
            ActionsManagerDialog.showActionsManagerDialog(mainFrame,
                    controller);
        } else if (e.getActionCommand().equals("Aliases")) {
            AliasManagerDialog.showAliasManagerDialog(mainFrame);
        }
    }
}
