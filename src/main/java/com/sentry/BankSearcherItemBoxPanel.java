package com.sentry;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.util.AsyncBufferedImage;
import net.runelite.client.util.QuantityFormatter;

/**
 * This panel displays an individual item result in the
 * Grand Exchange search plugin.
 */
class BankSearcherItemBoxPanel extends JPanel
{
    private static final Dimension ICON_SIZE = new Dimension(32, 32);

    BankSearcherItemBoxPanel(AsyncBufferedImage icon, String name, int itemID, int quantity)
    {
        BorderLayout layout = new BorderLayout();
        layout.setHgap(5);
        setLayout(layout);
        setToolTipText(name);
        setBackground(ColorScheme.DARKER_GRAY_COLOR);

        Color background = getBackground();
        List<JPanel> panels = new ArrayList<>();
        panels.add(this);

        setBorder(new EmptyBorder(5, 5, 5, 0));

        // Icon
        JLabel itemIcon = new JLabel();
        itemIcon.setPreferredSize(ICON_SIZE);
        if (icon != null)
        {
            icon.addTo(itemIcon);
        }
        add(itemIcon, BorderLayout.CENTER);
    }
}