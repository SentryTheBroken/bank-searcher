package com.sentry.banksearcher.ui;

import com.sentry.banksearcher.BankSearcherItem;
import java.awt.BorderLayout;
import java.awt.Dimension;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import net.runelite.client.ui.ColorScheme;

/**
 * This panel displays an individual item in the compact layout
 */
class BankSearcherItemBoxPanel extends JPanel
{
	private static final Dimension ICON_SIZE = new Dimension(32, 32);

	BankSearcherItemBoxPanel(BankSearcherItem bankItem)
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

		this.add(itemIcon, BorderLayout.CENTER);
	}
}