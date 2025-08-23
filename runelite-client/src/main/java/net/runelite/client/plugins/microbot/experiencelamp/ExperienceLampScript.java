package net.runelite.client.plugins.microbot.experiencelamp;

import static java.util.Map.entry;

import java.util.Map;
import java.util.concurrent.TimeUnit;
import net.runelite.api.Skill;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.Script;
import net.runelite.client.plugins.microbot.globval.WidgetIndices.GenieLampWindow;
import net.runelite.client.plugins.microbot.util.Global;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Inventory;
import net.runelite.client.plugins.microbot.util.widget.Rs2Widget;

public class ExperienceLampScript extends Script
{
    private static final Map<Skill, Integer> SKILL_WIDGETS = Map.ofEntries(
        entry(Skill.ATTACK, GenieLampWindow.ATTACK_DYNAMIC_CONTAINER),
        entry(Skill.STRENGTH, GenieLampWindow.STRENGHT_DYNAMIC_CONTAINER),
        entry(Skill.RANGED, GenieLampWindow.RANGED_DYNAMIC_CONTAINER),
        entry(Skill.MAGIC, GenieLampWindow.MAGIC_DYNAMIC_CONTAINER),
        entry(Skill.DEFENCE, GenieLampWindow.DEFENSE_DYNAMIC_CONTAINER),
        entry(Skill.HITPOINTS, GenieLampWindow.HITPOINTS_DYNAMIC_CONTAINER),
        entry(Skill.PRAYER, GenieLampWindow.PRAYER_DYNAMIC_CONTAINER),
        entry(Skill.AGILITY, GenieLampWindow.AGILITY_DYNAMIC_CONTAINER),
        entry(Skill.HERBLORE, GenieLampWindow.HERBOLORE_DYNAMIC_CONTAINER),
        entry(Skill.THIEVING, GenieLampWindow.THIEVING_DYNAMIC_CONTAINER),
        entry(Skill.CRAFTING, GenieLampWindow.CRAFTING_DYNAMIC_CONTAINER),
        entry(Skill.RUNECRAFT, GenieLampWindow.RUNECRAFTING_DYNAMIC_CONTAINER),
        entry(Skill.SLAYER, GenieLampWindow.SLAYER_DYNAMIC_CONTAINER),
        entry(Skill.FARMING, GenieLampWindow.FARMING_DYNAMIC_CONTAINER),
        entry(Skill.MINING, GenieLampWindow.MINING_DYNAMIC_CONTAINER),
        entry(Skill.SMITHING, GenieLampWindow.SMITHING_DYNAMIC_CONTAINER),
        entry(Skill.FISHING, GenieLampWindow.FISHING_DYNAMIC_CONTAINER),
        entry(Skill.COOKING, GenieLampWindow.COOKING_DYNAMIC_CONTAINER),
        entry(Skill.FIREMAKING, GenieLampWindow.FIREMAKING_DYNAMIC_CONTAINER),
        entry(Skill.WOODCUTTING, GenieLampWindow.WOODCUTTING_DYNAMIC_CONTAINER),
        entry(Skill.FLETCHING, GenieLampWindow.FLETCHING_DYNAMIC_CONTAINER),
        entry(Skill.CONSTRUCTION, GenieLampWindow.CONSTRUCTION_DYNAMIC_CONTAINER),
        entry(Skill.HUNTER, GenieLampWindow.HUNTER_DYNAMIC_CONTAINER)
    );

    public boolean run(ExperienceLampConfig config)
    {
        Microbot.enableAutoRunOn = false;
        mainScheduledFuture = scheduledExecutorService.scheduleWithFixedDelay(() ->
        {
            try
            {
                if (!Microbot.isLoggedIn() || !super.run())
                {
                    return;
                }

                if (Rs2Widget.isWidgetVisible(GenieLampWindow.GROUP_INDEX, GenieLampWindow.PARENT_CONTAINER))
                {
                    int child = SKILL_WIDGETS.getOrDefault(config.skill(), GenieLampWindow.ATTACK_DYNAMIC_CONTAINER);
                    Rs2Widget.clickWidget(GenieLampWindow.GROUP_INDEX, child);
                    Global.sleep(200, 400);
                    Rs2Widget.clickWidget(GenieLampWindow.GROUP_INDEX, GenieLampWindow.CONFIRM_DYNAMIC_CONTAINER);
                    Global.sleep(600, 800);
                    return;
                }

                Rs2Inventory.interact(item -> item.getName().toLowerCase().contains("lamp"), "Rub");
                Global.sleep(600, 1000);
            }
            catch (Exception e)
            {
                System.out.println(e.getMessage());
            }
        }, 0, 600, TimeUnit.MILLISECONDS);
        return true;
    }

    @Override
    public void shutdown()
    {
        super.shutdown();
    }
}
