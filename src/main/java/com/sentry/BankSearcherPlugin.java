package com.sentry;

import com.google.inject.Provides;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.Item;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.WidgetClosed;
import net.runelite.api.events.WidgetLoaded;
import net.runelite.api.widgets.ComponentID;
import net.runelite.api.widgets.InterfaceID;
import net.runelite.api.widgets.Widget;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.util.ImageUtil;

@Slf4j
@PluginDescriptor(name = "Bank Searcher")
public class BankSearcherPlugin extends Plugin {
	//Injections
	@Inject
	private Client client;

	@Inject
	private ClientToolbar clientToolbar;

	@Inject
	private BankSearcherConfig config;

	@Inject
	private BankSearcherService bankSearcherService;

	private BankSearcherPanel bankSearcherPanel;
	private NavigationButton navButton;

	@Getter
	private List<BankSearcherItem> allBankItems = new ArrayList<>();

	@Getter
	private List<BankSearcherItem> filteredBankItems = new ArrayList<>();

	@Override
	protected void startUp() throws Exception {
		bankSearcherPanel = injector.getInstance(BankSearcherPanel.class);
		
		final BufferedImage icon = ImageUtil.loadImageResource(getClass(), "BankSearcher.png");

		navButton = NavigationButton.builder()
			.tooltip("Bank Searcher")
			.icon(icon)
			.priority(100)
			.panel(bankSearcherPanel)
			.build();

		clientToolbar.addNavigation(navButton);
		log.info("BankSearcher started!");
	}

	@Override
	protected void shutDown() throws Exception {
		clientToolbar.removeNavigation(navButton);
		bankSearcherPanel = null;
		log.info("BankSearcher stopped!");
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged gameStateChanged) {
		if(gameStateChanged.getGameState() == GameState.LOGGED_IN) {
			// TO DO: Get List of items cached for currently logged in character
		}
	}

	@Subscribe
	public void onWidgetLoaded(WidgetLoaded widgetLoaded) {
		if(widgetLoaded.getGroupId() == InterfaceID.BANK) {
			Widget bankContainer = this.client.getWidget(ComponentID.BANK_ITEM_CONTAINER);
			boolean bankIsOpen = bankContainer != null && !bankContainer.isHidden();

			if (bankIsOpen) {
				log.info("BANK IS OPEN");
				this.allBankItems = this.bankSearcherService.getBankItems();
				this.bankSearcherPanel.showItems(this.allBankItems);
			}
		}
	}

	@Subscribe
	public void onWidgetClosed(WidgetClosed widgetClosed) {
		if(widgetClosed.getGroupId() == InterfaceID.BANK) {
			Widget bankContainer = this.client.getWidget(ComponentID.BANK_ITEM_CONTAINER);
			boolean bankIsOpen = bankContainer != null && !bankContainer.isHidden();

			if(bankIsOpen) {
				log.info("BANK IS CLOSING");
				this.allBankItems = this.bankSearcherService.getBankItems();
				this.bankSearcherPanel.showItems(this.allBankItems);
			}
		}
	}

	@Provides
	BankSearcherConfig provideConfig(ConfigManager configManager) {
		return configManager.getConfig(BankSearcherConfig.class);
	}

	public List<BankSearcherItem> searchBankItems(String searchText) {
		log.info("Search Bank Items for Keyword: {}", searchText);
		this.filteredBankItems = this.bankSearcherService.searchBankItems(searchText, this.allBankItems);
		return this.filteredBankItems;
	}

	public void resetFilteredBankItems() {
		this.filteredBankItems = this.allBankItems;
	}

	private void loadBankItemsLocally() {
		// TO DO: Implement method to load all bank items to a local file
		// based on logged in character which will be saved on shut down.
	}

	private void storeBankItemsLocally() {
		// TO DO: Implement method to save all bank items to a local file
		// based on logged in character which will be loaded on start up.
	}
}
