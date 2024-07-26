package com.sentry;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.util.List;

import javax.inject.Inject;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Item;
import net.runelite.api.ItemComposition;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.game.ItemManager;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.PluginPanel;
import net.runelite.client.ui.components.IconTextField;
import net.runelite.client.ui.components.PluginErrorPanel;
import net.runelite.client.util.AsyncBufferedImage;
import net.runelite.client.util.ImageUtil;

@Slf4j
public class BankSearcherPanel extends PluginPanel
{
	// Injections
	private BankSearcherPlugin bankSearcherPlugin;
	private ItemManager itemManager;
	private ClientThread clientThread;

	// Constants
	private static final String ERROR_PANEL = "ERROR_PANEL";
	private static final String RESULTS_PANEL = "RESULTS_PANEL";

	// Swing variables
	private final GridBagConstraints constraints = new GridBagConstraints();
	private final CardLayout cardLayout = new CardLayout();

	private final JPanel actionsAndSearchPanel = new JPanel();
	private final JPanel actions = new JPanel();
	private final IconTextField searchBar = new IconTextField();

	// The results container, this will hold all the individual ge item panels
	private final JPanel searchItemsPanel = new JPanel();

	// The center panel, this holds either the error panel or the results container
	private final JPanel centerPanel = new JPanel(cardLayout);

	// The error panel, this displays an error message
	private final PluginErrorPanel errorPanel = new PluginErrorPanel();

	@Setter
	private BankSearcherLayoutType layoutType = BankSearcherLayoutType.COMPACT;

	@Getter
	private String searchText = "";

	@Inject
	private BankSearcherPanel(BankSearcherPlugin bankSearcherPlugin, ItemManager itemManager, ClientThread clientThread)
	{
		this.bankSearcherPlugin = bankSearcherPlugin;
		this.itemManager = itemManager;
		this.clientThread = clientThread;

		this.setLayout(new BorderLayout());
		this.setBackground(ColorScheme.DARK_GRAY_COLOR);

		constraints.fill = GridBagConstraints.HORIZONTAL;
		constraints.weightx = 1;
		constraints.gridx = 0;
		constraints.gridy = 0;

		/*  The main container, this holds the search bar and the center panel */
		JPanel container = new JPanel();
		container.setLayout(new BorderLayout(5, 5));
		container.setBorder(new EmptyBorder(10, 10, 10, 10));
		container.setBackground(ColorScheme.DARK_GRAY_COLOR);

		this.buildActionsAndSearchPanel();
		this.buildCenterPanel();

		//container.add(searchBar, BorderLayout.NORTH);
		container.add(this.actionsAndSearchPanel, BorderLayout.NORTH);
		container.add(this.centerPanel, BorderLayout.CENTER);

		this.add(container, BorderLayout.CENTER);
	}

	private void buildActionsAndSearchPanel()
	{
		this.actionsAndSearchPanel.setLayout(new BorderLayout());
		this.actionsAndSearchPanel.setBackground(ColorScheme.PROGRESS_ERROR_COLOR);

		this.actions.setLayout(new FlowLayout(FlowLayout.RIGHT, 0, 0));
		JButton detailButton = new JButton();
		detailButton.setFocusable(false);
		detailButton.setToolTipText("Detail View");
		detailButton.addActionListener(e -> this.handleLayoutButtonClicked(BankSearcherLayoutType.DETAIL));
		final BufferedImage detailIcon = ImageUtil.loadImageResource(getClass(), "DetailIcon.png");
		detailButton.setIcon(new ImageIcon(detailIcon.getScaledInstance(24, 24, Image.SCALE_SMOOTH)));

		JButton compactButton = new JButton();
		compactButton.setFocusable(false);
		compactButton.setToolTipText("Compact View");
		compactButton.addActionListener(e -> this.handleLayoutButtonClicked(BankSearcherLayoutType.COMPACT));
		final BufferedImage compactIcon = ImageUtil.loadImageResource(getClass(), "CompactIcon.png");
		compactButton.setIcon(new ImageIcon(compactIcon.getScaledInstance(24, 24, Image.SCALE_SMOOTH)));

		this.actions.add(detailButton);
		this.actions.add(compactButton);

		this.searchBar.setIcon(IconTextField.Icon.SEARCH);
		this.searchBar.setPreferredSize(new Dimension(100, 30));
		this.searchBar.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		this.searchBar.setHoverBackgroundColor(ColorScheme.DARK_GRAY_HOVER_COLOR);
		this.searchBar.addActionListener(this::handleSearchBarAction);
		this.searchBar.addClearListener(this::handleSearchBarClear);

		this.actionsAndSearchPanel.add(this.actions, BorderLayout.NORTH);
		this.actionsAndSearchPanel.add(this.searchBar, BorderLayout.CENTER);
	}

	private void buildCenterPanel()
	{
		this.searchItemsPanel.setLayout(new GridBagLayout());
		this.searchItemsPanel.setBackground(ColorScheme.DARK_GRAY_COLOR);

		/* This panel wraps the results panel and guarantees the scrolling behaviour */
		JPanel wrapper = new JPanel(new BorderLayout());
		wrapper.setBackground(ColorScheme.DARK_GRAY_COLOR);
		wrapper.add(this.searchItemsPanel, BorderLayout.NORTH);

		/*  The results wrapper, this scrolling panel wraps the results container */
		JScrollPane resultsWrapper = new JScrollPane(wrapper);
		resultsWrapper.setBackground(ColorScheme.DARK_GRAY_COLOR);
		resultsWrapper.getVerticalScrollBar().setPreferredSize(new Dimension(12, 0));
		resultsWrapper.getVerticalScrollBar().setBorder(new EmptyBorder(0, 5, 0, 0));
		resultsWrapper.setVisible(false);

		/* This panel wraps the error panel and limits its height */
		JPanel errorWrapper = new JPanel(new BorderLayout());
		errorWrapper.setBackground(ColorScheme.DARK_GRAY_COLOR);
		errorWrapper.add(this.errorPanel, BorderLayout.NORTH);

		this.errorPanel.setContent("Bank Searcher", "Here you can search for an item in your bank by it's name. Please visit a bank to see your items.");

		this.centerPanel.add(resultsWrapper, RESULTS_PANEL);
		this.centerPanel.add(errorWrapper, ERROR_PANEL);

		this.cardLayout.show(centerPanel, ERROR_PANEL);
	}

	private void handleLayoutButtonClicked(BankSearcherLayoutType layoutType)
	{
		List<BankSearcherItem> filteredBankItems = this.bankSearcherPlugin.getFilteredBankItems();
		if (this.layoutType != layoutType && filteredBankItems != null && !filteredBankItems.isEmpty())
		{
			log.info("Switching layout type to {}", layoutType);
			this.setLayoutType(layoutType);
			this.showItems(filteredBankItems);
		}
	}

	private void handleSearchBarAction(ActionEvent e)
	{
		List<BankSearcherItem> filteredBankItems = this.bankSearcherPlugin.getFilteredBankItems();
		if (filteredBankItems == null || filteredBankItems.isEmpty()) return;

		this.searchText = e.getActionCommand();
		List<BankSearcherItem> searchedBankItems = this.bankSearcherPlugin.searchBankItems(this.searchText);

		if (searchedBankItems != null && !searchedBankItems.isEmpty())
		{
			this.showItems(searchedBankItems);
		}
		else
		{
			searchBar.setIcon(IconTextField.Icon.ERROR);
			errorPanel.setContent("No results found.", "No items were found with that name, please try again.");
			cardLayout.show(centerPanel, ERROR_PANEL);
			searchBar.setEditable(true);
		}
	}

	private void handleSearchBarClear()
	{
		this.searchText = "";
		this.bankSearcherPlugin.resetFilteredBankItems();
		this.showAllItems();
	}

	public void showAllItems()
	{
		clientThread.invoke(() -> {
			this.showItems(this.bankSearcherPlugin.getAllBankItems());
		});
	}

	public void showItems(List<BankSearcherItem> bankItems)
	{
		this.searchItemsPanel.removeAll();
		this.constraints.gridy = 0;
		this.searchBar.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		this.searchBar.setEditable(false);
		this.searchBar.setIcon(IconTextField.Icon.LOADING);

		if (this.layoutType == BankSearcherLayoutType.COMPACT)
		{
			this.createCompactItemLayout(bankItems);
		}
		else if (this.layoutType == BankSearcherLayoutType.DETAIL)
		{
			this.createDetailItemLayout(bankItems);
		}

		this.searchBar.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		this.searchBar.setEditable(true);
		this.searchBar.setIcon(IconTextField.Icon.SEARCH);
		this.cardLayout.show(centerPanel, RESULTS_PANEL);
		this.searchItemsPanel.revalidate();
		this.searchItemsPanel.repaint();
	}

	private void createDetailItemLayout(List<BankSearcherItem> bankItems)
	{
		this.searchItemsPanel.setLayout(new GridBagLayout());

		int index = 0;
		for (BankSearcherItem bankItem : bankItems)
		{
			BankSearcherItemPanel bankItemPanel = new BankSearcherItemPanel(bankItem);

            /*
            Add the first item directly, wrap the rest with margin. This margin hack is because
            gridbaglayout does not support inter-element margins.
             */
			if (index++ > 0)
			{
				JPanel marginWrapper = new JPanel(new BorderLayout());
				marginWrapper.setBackground(ColorScheme.DARK_GRAY_COLOR);
				marginWrapper.setBorder(new EmptyBorder(5, 0, 0, 0));
				marginWrapper.add(bankItemPanel, BorderLayout.NORTH);
				this.searchItemsPanel.add(marginWrapper, this.constraints);
			}
			else
			{
				this.searchItemsPanel.add(bankItemPanel, this.constraints);
			}

			this.constraints.gridy++;
		}
	}

	private void createCompactItemLayout(List<BankSearcherItem> bankItems)
	{
		this.searchItemsPanel.setLayout(new GridLayout(0, 5, 1, 1));

		int index = 0;
		for (BankSearcherItem bankItem : bankItems)
		{
			BankSearcherItemBoxPanel bankItemPanel = new BankSearcherItemBoxPanel(bankItem);

            /*
            Add the first item directly, wrap the rest with margin. This margin hack is because
            gridbaglayout does not support inter-element margins.
             */
			if (index++ > 4)
			{
				JPanel marginWrapper = new JPanel(new BorderLayout());
				marginWrapper.setBackground(ColorScheme.DARK_GRAY_COLOR);
				marginWrapper.setBorder(new EmptyBorder(0, 0, 0, 0));
				marginWrapper.add(bankItemPanel, BorderLayout.NORTH);
				this.searchItemsPanel.add(marginWrapper, this.constraints);
			}
			else
			{
				this.searchItemsPanel.add(bankItemPanel, this.constraints);
			}

			this.constraints.gridy++;
		}
	}
}
