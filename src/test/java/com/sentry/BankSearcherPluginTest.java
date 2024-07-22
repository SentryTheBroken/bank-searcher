package com.sentry;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class BankSearcherPluginTest {
	public static void main(String[] args) throws Exception {
		ExternalPluginManager.loadBuiltin(BankSearcherPlugin.class);
		RuneLite.main(args);
	}
}