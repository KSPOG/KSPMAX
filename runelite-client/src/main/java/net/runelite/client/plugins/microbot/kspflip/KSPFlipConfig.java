package net.runelite.client.plugins.microbot.kspflip;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("kspflip")
public interface KSPFlipConfig extends Config
{
    @ConfigItem(
        keyName = "guide",
        name = "Guide",
        description = "How to use the KSP Flipper plugin"
    )
    default String guide()
    {
        return "Start with coins in your inventory at the Grand Exchange.";
    }
}
