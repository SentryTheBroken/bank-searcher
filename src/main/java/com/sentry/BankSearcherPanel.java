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

    private static final String ERROR_PANEL = "ERROR_PANEL";
    private static final String RESULTS_PANEL = "RESULTS_PANEL";
    private static final int MAX_SEARCH_ITEMS = 100;

    private final GridBagConstraints constraints = new GridBagConstraints();
    private final CardLayout cardLayout = new CardLayout();

    private final ClientThread clientThread;
    private final ItemManager itemManager;
    private final ScheduledExecutorService executor;
    private final RuneLiteConfig runeLiteConfig;
    private final BankSearcherPlugin bankSearcherPlugin;

    private final IconTextField searchBar = new IconTextField();

    /*  The results container, this will hold all the individual ge item panels */
    private final JPanel searchItemsPanel = new JPanel();

    /*  The center panel, this holds either the error panel or the results container */
    private final JPanel centerPanel = new JPanel(cardLayout);

    /*  The error panel, this displays an error message */
    private final PluginErrorPanel errorPanel = new PluginErrorPanel();

    @Inject
    private BankSearcherPanel(ClientThread clientThread, ItemManager itemManager,
                              ScheduledExecutorService executor, RuneLiteConfig runeLiteConfig, BankSearcherPlugin bankSearcherPlugin)
    {
        this.clientThread = clientThread;
        this.itemManager = itemManager;
        this.executor = executor;
        this.runeLiteConfig = runeLiteConfig;
        this.bankSearcherPlugin = bankSearcherPlugin;

        setLayout(new BorderLayout());
        setBackground(ColorScheme.DARK_GRAY_COLOR);

        /*  The main container, this holds the search bar and the center panel */
        JPanel container = new JPanel();
        container.setLayout(new BorderLayout(5, 5));
        container.setBorder(new EmptyBorder(10, 10, 10, 10));
        container.setBackground(ColorScheme.DARK_GRAY_COLOR);

        searchBar.setIcon(IconTextField.Icon.SEARCH);
        searchBar.setPreferredSize(new Dimension(100, 30));
        searchBar.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        searchBar.setHoverBackgroundColor(ColorScheme.DARK_GRAY_HOVER_COLOR);
        //searchBar.addActionListener(e -> executor.execute(() -> priceLookup(false)));
        searchBar.addClearListener(this::updateSearch);

        searchItemsPanel.setLayout(new GridBagLayout());
        searchItemsPanel.setBackground(ColorScheme.DARK_GRAY_COLOR);

        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.weightx = 1;
        constraints.gridx = 0;
        constraints.gridy = 0;

        /* This panel wraps the results panel and guarantees the scrolling behaviour */
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(ColorScheme.DARK_GRAY_COLOR);
        wrapper.add(searchItemsPanel, BorderLayout.NORTH);

        /*  The results wrapper, this scrolling panel wraps the results container */
        JScrollPane resultsWrapper = new JScrollPane(wrapper);
        resultsWrapper.setBackground(ColorScheme.DARK_GRAY_COLOR);
        resultsWrapper.getVerticalScrollBar().setPreferredSize(new Dimension(12, 0));
        resultsWrapper.getVerticalScrollBar().setBorder(new EmptyBorder(0, 5, 0, 0));
        resultsWrapper.setVisible(false);

        /* This panel wraps the error panel and limits its height */
        JPanel errorWrapper = new JPanel(new BorderLayout());
        errorWrapper.setBackground(ColorScheme.DARK_GRAY_COLOR);
        errorWrapper.add(errorPanel, BorderLayout.NORTH);

        errorPanel.setContent("Bank Searcher",
                "Here you can search for an item in your bank by its name.");

        centerPanel.add(resultsWrapper, RESULTS_PANEL);
        centerPanel.add(errorWrapper, ERROR_PANEL);

        cardLayout.show(centerPanel, ERROR_PANEL);

        container.add(searchBar, BorderLayout.NORTH);
        container.add(centerPanel, BorderLayout.CENTER);

        add(container, BorderLayout.CENTER);
    }

    public void updateItems(Item[] bankItems) {
        log.info("UPDATING ITEMS");
        log.info("BANK IS OPEN");
        searchItemsPanel.removeAll();
        searchBar.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        searchBar.setEditable(false);
        searchBar.setIcon(IconTextField.Icon.LOADING);

        this.createDetailItemLayout(bankItems);

        searchBar.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        searchBar.setEditable(true);
        searchBar.setIcon(IconTextField.Icon.SEARCH);
        cardLayout.show(centerPanel, RESULTS_PANEL);
    }

    private boolean updateSearch() {
        return true;
    }

    private void createDetailItemLayout(Item[] bankItems) {
        searchItemsPanel.setLayout(new GridBagLayout());

        int index = 0;
        for(Item bankItem : bankItems) {
            int itemId = bankItem.getId();
            int quantity = bankItem.getQuantity();
            ItemComposition itemComp = itemManager.getItemComposition(itemId);
            AsyncBufferedImage itemImage;
            if(quantity > 1 || itemComp.isStackable()) {
                itemImage = itemManager.getImage(itemId, quantity, true);
            }
            else {
                itemImage = itemManager.getImage(itemId);
            }

            BankSearcherItemPanel bankItemPanel = new BankSearcherItemPanel(itemImage, itemComp.getName(), itemId, quantity);

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
                searchItemsPanel.add(marginWrapper, constraints);
            }
            else
            {
                searchItemsPanel.add(bankItemPanel, constraints);
            }

            constraints.gridy++;
        }
    }

    private void createCompactItemLayout(Item[] bankItems) {
        searchItemsPanel.setLayout(new GridLayout(0, 5, 1, 1));

        int index = 0;
        for(Item bankItem : bankItems) {
            int itemId = bankItem.getId();
            int quantity = bankItem.getQuantity();
            ItemComposition itemComp = itemManager.getItemComposition(itemId);
            AsyncBufferedImage itemImage;
            if(quantity > 1 || itemComp.isStackable()) {
                itemImage = itemManager.getImage(itemId, quantity, true);
            }
            else {
                itemImage = itemManager.getImage(itemId);
            }

            BankSearcherItemBoxPanel bankItemPanel = new BankSearcherItemBoxPanel(itemImage, itemComp.getName(), itemId, quantity);

            /*
            Add the first item directly, wrap the rest with margin. This margin hack is because
            gridbaglayout does not support inter-element margins.
             */
            if (index++ > 4) {
                JPanel marginWrapper = new JPanel(new BorderLayout());
                marginWrapper.setBackground(ColorScheme.DARK_GRAY_COLOR);
                marginWrapper.setBorder(new EmptyBorder(0, 0, 0, 0));
                marginWrapper.add(bankItemPanel, BorderLayout.NORTH);
                searchItemsPanel.add(marginWrapper, constraints);
            }
            else
            {
                searchItemsPanel.add(bankItemPanel, constraints);
            }

            constraints.gridy++;
        }
    }
}
