package com.sentry;

import java.awt.AlphaComposite;
import java.awt.Composite;
import java.awt.CompositeContext;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.ColorModel;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.client.game.ItemManager;
import net.runelite.client.game.ItemVariationMapping;
import net.runelite.client.util.AsyncBufferedImage;

import javax.inject.Inject;
import java.util.*;

@Slf4j
public class BankSearcherService
{
	@Inject
	private Client client;

	@Inject
	private ItemManager itemManager;

	//Constants
	int ITEM_PLACEHOLDER_ID = 14401;

	private Set<Integer> placeholderIconsUpdated = new HashSet<>();

	public List<BankSearcherItem> getBankItems()
	{
		final ItemContainer itemContainer = client.getItemContainer(InventoryID.BANK);
		if (itemContainer == null)
		{
			return null;
		}

		List<BankSearcherItem> bankItems = new ArrayList<>();
		for (Item item : itemContainer.getItems())
		{
			int itemId = item.getId();
			int quantity = item.getQuantity();

			ItemComposition itemComp = this.itemManager.getItemComposition(itemId);
			int placeholderId = itemComp.getPlaceholderId();
			Boolean isPlaceholder = itemComp.getPlaceholderTemplateId() == ITEM_PLACEHOLDER_ID;

			AsyncBufferedImage itemImage;
			if (isPlaceholder)
			{
				quantity = 0;
				itemImage = this.itemManager.getImage(placeholderId, quantity, true);

				if(!placeholderIconsUpdated.contains(placeholderId))
				{
					Graphics2D placeholderIconGraphics = itemImage.createGraphics();
					placeholderIconGraphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC, 0.5f));
					placeholderIconGraphics.drawImage(itemImage, 0, 0, Constants.ITEM_SPRITE_WIDTH, Constants.ITEM_SPRITE_HEIGHT, null);
					placeholderIconGraphics.dispose();

					placeholderIconsUpdated.add(placeholderId);
				}
			}
			else if (quantity > 1 || itemComp.isStackable())
			{
				itemImage = this.itemManager.getImage(itemId, quantity, true);
			}
			else
			{
				itemImage = this.itemManager.getImage(itemId);
			}

			bankItems.add(new BankSearcherItem(itemImage, itemComp.getName(), itemId, quantity, itemManager.getItemPrice(itemId), itemComp.getHaPrice(), isPlaceholder, placeholderId));

			log.info("Name: {}, ItemId: {}, PlaceholderId: {}, PlaceholderTemplateId: {}", itemComp.getName(), itemId, itemComp.getPlaceholderId(), itemComp.getPlaceholderTemplateId());
		}


		return bankItems;
	}

	public List<BankSearcherItem> searchBankItems(String searchText, List<BankSearcherItem> allBankItems)
	{
		List<BankSearcherItem> filteredBankItems = new ArrayList<>();
		String lowerSearchText = searchText.toLowerCase();

		for (BankSearcherItem bankItem : allBankItems)
		{
			if (bankItem.getName().toLowerCase().contains(lowerSearchText))
			{
				filteredBankItems.add(bankItem);
			}
		}

		for (BankSearcherItem bankItem : filteredBankItems)
		{
			log.info(bankItem.toString());
		}

		return filteredBankItems;
	}
}
