package net.runelite.client.plugins.microbot.experiencelamp;

import net.runelite.api.Skill;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("experiencelamp")
public interface ExperienceLampConfig extends Config
{
    @ConfigItem(
        keyName = "skill",
        name = "Skill",
        description = "Skill to train with lamps"
    )
    default Skill skill()
    {
        return Skill.ATTACK;
    }
}
