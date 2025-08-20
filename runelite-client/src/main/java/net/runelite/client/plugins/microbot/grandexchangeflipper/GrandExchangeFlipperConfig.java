package net.runelite.client.plugins.microbot.grandexchangeflipper;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("geflipper")
public interface GrandExchangeFlipperConfig extends Config
{
    @ConfigItem(
            keyName = "priceSource",
            name = "Price source",
            description = "Select price data source",
            position = 0
    )
    default PriceSource priceSource()
    {
            return PriceSource.DEFAULT;
    }

    enum PriceSource
    {
            DEFAULT,
            OSRS_WIKI
    }
}
