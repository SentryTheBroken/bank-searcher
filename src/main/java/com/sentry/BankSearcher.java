package com.sentry;

import net.runelite.api.Client;
import net.runelite.api.InventoryID;
import net.runelite.api.Item;
import net.runelite.api.ItemContainer;

public class BankSearcher {

    public static Item[] getBankItems(Client client) {
        final ItemContainer itemContainer = client.getItemContainer(InventoryID.BANK);
        if (itemContainer == null) return null;

        return itemContainer.getItems();
    }

}
