package com.sentry;

import com.google.inject.Provides;

import java.awt.image.BufferedImage;

import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.InventoryID;
import net.runelite.api.Item;
import net.runelite.api.ItemContainer;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.WidgetLoaded;
import net.runelite.api.widgets.ComponentID;
import net.runelite.api.widgets.InterfaceID;
import net.runelite.api.widgets.Widget;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.info.InfoPanel;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.util.ImageUtil;

@Slf4j
@PluginDescriptor(name = "Bank Searcher")
public class BankSearcherPlugin extends Plugin {
	@Inject
	private Client client;

	@Inject
	private ClientToolbar clientToolbar;

	private BankSearcherPanel bankSearcherPanel;
	private NavigationButton navButton;

	@Inject
	private BankSearcherConfig config;

	private Item[] bankItems;

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
		if (gameStateChanged.getGameState() == GameState.LOGGED_IN) {
			client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "Example says " + config.greeting(), null);
		}
	}

	@Subscribe
	public void onWidgetLoaded(WidgetLoaded widgetLoaded) {
		if (widgetLoaded.getGroupId() == InterfaceID.BANK) {
			Widget bankContainer = client.getWidget(ComponentID.BANK_ITEM_CONTAINER);
			boolean bankIsOpen = bankContainer != null && !bankContainer.isHidden();

			if (bankIsOpen) {
				log.info("BANK IS OPEN");
				this.bankItems = BankSearcher.getBankItems(client);
				for (Item bankItem : this.bankItems) {
					log.info(bankItem.toString());
				}
				bankSearcherPanel.updateItems(this.bankItems);
			}
		}
	}

	@Provides
	BankSearcherConfig provideConfig(ConfigManager configManager) {
		return configManager.getConfig(BankSearcherConfig.class);
	}
}
