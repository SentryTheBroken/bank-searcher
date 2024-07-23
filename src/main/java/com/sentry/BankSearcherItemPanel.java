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
class BankSearcherItemPanel extends JPanel
{
    private static final Dimension ICON_SIZE = new Dimension(32, 32);

    BankSearcherItemPanel(AsyncBufferedImage icon, String name, int itemID, int quantity)
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
        add(itemIcon, BorderLayout.LINE_START);

        // Item details panel
        JPanel rightPanel = new JPanel(new GridLayout(3, 1));
        panels.add(rightPanel);
        rightPanel.setBackground(background);

        // Item name
        JLabel itemName = new JLabel();
        itemName.setForeground(Color.WHITE);
        itemName.setMaximumSize(new Dimension(0, 0));        // to limit the label's size for
        itemName.setPreferredSize(new Dimension(0, 0));    // items with longer names
        itemName.setText(name);
        rightPanel.add(itemName);

        // Quantity
        JLabel quantityLabel = new JLabel();
        quantityLabel.setText("Qty: " + String.valueOf(quantity));
        quantityLabel.setForeground(ColorScheme.GRAND_EXCHANGE_PRICE);
        rightPanel.add(quantityLabel);

        add(rightPanel, BorderLayout.CENTER);
    }
}