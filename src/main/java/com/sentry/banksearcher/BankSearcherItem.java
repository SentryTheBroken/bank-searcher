package com.sentry.banksearcher;

import lombok.Value;
import net.runelite.client.util.AsyncBufferedImage;

@Value
public class BankSearcherItem
{
	AsyncBufferedImage icon;
	String name;
	Integer itemId;
	Integer quantity;
	Integer gePrice;
	Integer haPrice;
	Boolean isPlaceholder;
	Integer placeholderId;
}
