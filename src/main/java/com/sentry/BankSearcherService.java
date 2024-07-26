package com.sentry;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.client.game.ItemManager;
import net.runelite.client.util.AsyncBufferedImage;

import javax.inject.Inject;
import java.util.*;

@Slf4j
public class BankSearcherService {
    @Inject
    private Client client;

    @Inject
    private ItemManager itemManager;

    public List<BankSearcherItem> getBankItems() {
        final ItemContainer itemContainer = client.getItemContainer(InventoryID.BANK);
        if(itemContainer == null) return null;

        List<BankSearcherItem> bankItems = new ArrayList<>();
        for(Item item : itemContainer.getItems()) {
            int itemId = item.getId();
            int quantity = item.getQuantity();
            ItemComposition itemComp = this.itemManager.getItemComposition(itemId);
            AsyncBufferedImage itemImage;
            if(quantity > 1 || itemComp.isStackable()) {
                itemImage = this.itemManager.getImage(itemId, quantity, true);
            }
            else {
                itemImage = this.itemManager.getImage(itemId);
            }

            bankItems.add(new BankSearcherItem(itemImage, itemComp.getName(), itemId, quantity, itemManager.getItemPrice(itemId), itemComp.getHaPrice()));
        }

        for(BankSearcherItem bankItem : bankItems) {
            log.info(bankItem.toString());
        }

        return bankItems;
    }

    public List<BankSearcherItem> searchBankItems(String searchText, List<BankSearcherItem> allBankItems) {
        List<BankSearcherItem> filteredBankItems = new ArrayList<>();
        String lowerSearchText = searchText.toLowerCase();

        for(BankSearcherItem bankItem : allBankItems) {
            if(bankItem.getName().toLowerCase().contains(lowerSearchText)) {
                filteredBankItems.add(bankItem);
            }
        }

        for(BankSearcherItem bankItem : filteredBankItems) {
            log.info(bankItem.toString());
        }

        return filteredBankItems;
    }
}
