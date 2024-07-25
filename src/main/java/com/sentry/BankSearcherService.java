package com.sentry;

import net.runelite.api.Client;
import net.runelite.api.InventoryID;
import net.runelite.api.Item;
import net.runelite.api.ItemContainer;

import javax.inject.Inject;

public class BankSearcherService {
    @Inject
    private Client client;

    public Item[] getBankItems() {
        final ItemContainer itemContainer = client.getItemContainer(InventoryID.BANK);
        if (itemContainer == null) return null;

        return itemContainer.getItems();
    }

    public Item[] searchBankItems(String searchText) {
        // TO DO: Implement method to search items in bank based on searchText
        return new Item[]{};
    }
}
