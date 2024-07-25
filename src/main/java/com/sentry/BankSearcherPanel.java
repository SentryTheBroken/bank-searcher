package com.sentry;

import java.awt.*;
import java.util.concurrent.ScheduledExecutorService;

import javax.inject.Inject;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Item;
import net.runelite.api.ItemComposition;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.RuneLiteConfig;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.game.ItemManager;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.PluginPanel;
import net.runelite.client.ui.components.IconTextField;
import net.runelite.client.ui.components.PluginErrorPanel;
import net.runelite.client.util.AsyncBufferedImage;

@Slf4j
public class BankSearcherPanel extends PluginPanel {
    // Injections
    private BankSearcherPlugin bankSearcherPlugin;
    private ItemManager itemManager;

    // Constants
    private static final String ERROR_PANEL = "ERROR_PANEL";
    private static final String RESULTS_PANEL = "RESULTS_PANEL";

    // Swing variables
    private final GridBagConstraints constraints = new GridBagConstraints();
    private final CardLayout cardLayout = new CardLayout();

    private final JPanel actionsAndSearchPanel = new JPanel();
    private final IconTextField searchBar = new IconTextField();

    // The results container, this will hold all the individual ge item panels
    private final JPanel searchItemsPanel = new JPanel();

    // The center panel, this holds either the error panel or the results container
    private final JPanel centerPanel = new JPanel(cardLayout);

    // The error panel, this displays an error message
    private final PluginErrorPanel errorPanel = new PluginErrorPanel();

    @Inject
    private BankSearcherPanel(BankSearcherPlugin bankSearcherPlugin, ItemManager itemManager) {
        this.bankSearcherPlugin = bankSearcherPlugin;
        this.itemManager = itemManager;

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

    private void buildActionsAndSearchPanel() {
        this.actionsAndSearchPanel.setLayout(new BorderLayout());
        this.actionsAndSearchPanel.setBackground(ColorScheme.PROGRESS_ERROR_COLOR);

        this.searchBar.setIcon(IconTextField.Icon.SEARCH);
        this.searchBar.setPreferredSize(new Dimension(100, 30));
        this.searchBar.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        this.searchBar.setHoverBackgroundColor(ColorScheme.DARK_GRAY_HOVER_COLOR);
        //this.searchBar.addActionListener(e -> executor.execute(() -> priceLookup(false)));
        this.searchBar.addClearListener(this::updateSearch);

        this.actionsAndSearchPanel.add(searchBar);
    }

    private void buildCenterPanel() {
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

        this.errorPanel.setContent("Bank Searcher", "Here you can search for an item in your bank by its name.");

        this.centerPanel.add(resultsWrapper, RESULTS_PANEL);
        this.centerPanel.add(errorWrapper, ERROR_PANEL);

        this.cardLayout.show(centerPanel, ERROR_PANEL);
    }

    public void updateItems(Item[] bankItems) {
        this.searchItemsPanel.removeAll();
        this.constraints.gridy = 0;
        this.searchBar.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        this.searchBar.setEditable(false);
        this.searchBar.setIcon(IconTextField.Icon.LOADING);

        this.createCompactItemLayout(bankItems);

        this.searchBar.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        this.searchBar.setEditable(true);
        this.searchBar.setIcon(IconTextField.Icon.SEARCH);
        this.cardLayout.show(centerPanel, RESULTS_PANEL);
    }

    private boolean updateSearch() {
        return true;
    }

    private void createDetailItemLayout(Item[] bankItems) {
        this.searchItemsPanel.setLayout(new GridBagLayout());

        int index = 0;
        for(Item bankItem : bankItems) {
            int itemId = bankItem.getId();
            int quantity = bankItem.getQuantity();
            ItemComposition itemComp = this.itemManager.getItemComposition(itemId);
            AsyncBufferedImage itemImage;
            if(quantity > 1 || itemComp.isStackable()) {
                itemImage = this.itemManager.getImage(itemId, quantity, true);
            }
            else {
                itemImage = this.itemManager.getImage(itemId);
            }

            BankSearcherItemPanel bankItemPanel = new BankSearcherItemPanel(itemImage, itemComp.getName(), itemId, quantity);

            /*
            Add the first item directly, wrap the rest with margin. This margin hack is because
            gridbaglayout does not support inter-element margins.
             */
            if(index++ > 0) {
                JPanel marginWrapper = new JPanel(new BorderLayout());
                marginWrapper.setBackground(ColorScheme.DARK_GRAY_COLOR);
                marginWrapper.setBorder(new EmptyBorder(5, 0, 0, 0));
                marginWrapper.add(bankItemPanel, BorderLayout.NORTH);
                this.searchItemsPanel.add(marginWrapper, this.constraints);
            }
            else {
                this.searchItemsPanel.add(bankItemPanel, this.constraints);
            }

            this.constraints.gridy++;
        }
    }

    private void createCompactItemLayout(Item[] bankItems) {
        this.searchItemsPanel.setLayout(new GridLayout(0, 5, 1, 1));

        int index = 0;
        for(Item bankItem : bankItems) {
            int itemId = bankItem.getId();
            int quantity = bankItem.getQuantity();
            ItemComposition itemComp = this.itemManager.getItemComposition(itemId);
            AsyncBufferedImage itemImage;
            if(quantity > 1 || itemComp.isStackable()) {
                itemImage = this.itemManager.getImage(itemId, quantity, true);
            }
            else {
                itemImage = this.itemManager.getImage(itemId);
            }

            BankSearcherItemBoxPanel bankItemPanel = new BankSearcherItemBoxPanel(itemImage, itemComp.getName(), itemId, quantity);

            /*
            Add the first item directly, wrap the rest with margin. This margin hack is because
            gridbaglayout does not support inter-element margins.
             */
            if(index++ > 4) {
                JPanel marginWrapper = new JPanel(new BorderLayout());
                marginWrapper.setBackground(ColorScheme.DARK_GRAY_COLOR);
                marginWrapper.setBorder(new EmptyBorder(0, 0, 0, 0));
                marginWrapper.add(bankItemPanel, BorderLayout.NORTH);
                this.searchItemsPanel.add(marginWrapper, this.constraints);
            }
            else {
                this.searchItemsPanel.add(bankItemPanel, this.constraints);
            }

            this.constraints.gridy++;
        }
    }
}
