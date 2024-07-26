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

	BankSearcherItemPanel(BankSearcherItem bankItem)
	{
		this.setLayout(new BorderLayout(5, 0));
		this.setToolTipText(bankItem.getName());
		this.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		this.setBorder(new EmptyBorder(5, 5, 5, 0));

		// Icon
		JLabel itemIcon = new JLabel();
		itemIcon.setPreferredSize(ICON_SIZE);
		if (bankItem.getIcon() != null)
		{
			bankItem.getIcon().addTo(itemIcon);
		}

		this.add(itemIcon, BorderLayout.LINE_START);

		// Item details panel
		JPanel rightPanel = new JPanel(new GridLayout(3, 1));
		rightPanel.setBackground(ColorScheme.DARKER_GRAY_COLOR);

		// Item name
		JLabel itemName = new JLabel();
		itemName.setForeground(Color.WHITE);
		itemName.setMaximumSize(new Dimension(0, 0));        // to limit the label's size for
		itemName.setPreferredSize(new Dimension(0, 0));    // items with longer names
		itemName.setText(bankItem.getName());
		rightPanel.add(itemName);

		// GE Price
		JLabel gePrice = new JLabel();
		gePrice.setText(String.valueOf(bankItem.getGePrice()) + " gp");
		gePrice.setForeground(ColorScheme.GRAND_EXCHANGE_PRICE);
		rightPanel.add(gePrice);

		// HA Price
		JLabel haPrice = new JLabel();
		haPrice.setText(String.valueOf(bankItem.getHaPrice()) + " alch");
		haPrice.setForeground(ColorScheme.GRAND_EXCHANGE_ALCH);
		rightPanel.add(haPrice);

		this.add(rightPanel, BorderLayout.CENTER);
	}
}