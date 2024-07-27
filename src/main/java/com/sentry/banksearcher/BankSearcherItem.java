package com.sentry.banksearcher;

import lombok.Value;
import net.runelite.client.util.AsyncBufferedImage;

@Value
public class BankSearcherItem
{
	private final AsyncBufferedImage icon;
	private final String name;
	private final Integer itemId;
	private final Integer quantity;
	private final Integer gePrice;
	private final Integer haPrice;
	private final Boolean isPlaceholder;
	private final Integer placeholderId;
}
