package net.runelite.client.plugins.microbot.bankorganizer;

import net.runelite.api.ItemComposition;
import net.runelite.client.util.Text;
import java.util.*;

final class CategoryRules {
    private CategoryRules() {}

    static final List<Category> DEFAULT_ORDER = Arrays.asList(
            Category.RAIDS, Category.SLAYER, Category.BARROWS,
            Category.COMBAT, Category.ARMOUR, Category.AMMO,
            Category.POTIONS, Category.FOOD, Category.RUNES, Category.TELEPORTS,
            Category.SKILLING, Category.FARMING, Category.CLUE, Category.TOOLS,
            Category.QUEST, Category.MISC
    );

    static Category classify(ItemComposition ic) {
        String n = Text.standardize(ic.getName());
        if (isBarrows(n)) return Category.BARROWS;
        if (isSlayer(n)) return Category.SLAYER;
        if (isRaids(n)) return Category.RAIDS;

        if (nameAny(n, "potion","brew","restore","super","overload","antidote","antipoison","stamina","prayer potion","energy potion"))
            return Category.POTIONS;
        if (nameAny(n, "shark","anglerfish","karambwan","lobster","trout","salmon","cake","pizza","wine","meat","stew"))
            return Category.FOOD;
        if (nameAny(n, "rune","runes","astral","law rune","nature rune","blood rune","soul rune"))
            return Category.RUNES;
        if (nameAny(n, "teleport","tablet","tabs","chronicle","xeric","games necklace","dueling ring","necklace of passage","skills necklace","combat bracelet"))
            return Category.TELEPORTS;
        if (nameAny(n, "arrow","bolt","javelin","throwing","dart","ammo","cannonball"))
            return Category.AMMO;
        if (nameAny(n, "helm","coif","hat","mask","body","plate","legs","skirt","gloves","boots","shield","cape","defender","ring","amulet"))
            return Category.ARMOUR;
        if (nameAny(n, "sword","2h","scimitar","mace","spear","halberd","maul","bow","staff","knife","whip","godsword","claws"))
            return Category.COMBAT;
        if (nameAny(n, "spade","hammer","chisel","axe","pickaxe","harpoon","net","tinderbox","small fishing","butterfly net","impling jar","pestle","mortar"))
            return Category.TOOLS;
        if (nameAny(n, "seed","sapling","compost","watering can","rake","seed dibber","secateurs","plant cure","ultracompost"))
            return Category.FARMING;
        if (nameAny(n, "clue","casket","totem","torn clue","clue scroll"))
            return Category.CLUE;
        if (nameAny(n, "quest","sigil","key","orb","notes","device","journal","talisman","enchantment scroll","god book"))
            return Category.QUEST;
        if (isSkilling(ic)) return Category.SKILLING;
        return Category.MISC;
    }

    static boolean nameAny(String name, String... needles) { for (String s : needles) if (name.contains(s)) return true; return false; }

    private static boolean isSkilling(ItemComposition ic) {
        String n = ic.getName().toLowerCase();
        return n.contains("ore") || n.contains("bar") || n.contains("log") || n.contains("herb") || n.contains("raw ") || n.contains("leather") || n.contains("gem") || n.contains("plank");
    }

    private static boolean isRaids(String n) {
        return nameAny(n, "twisted bow","kodai","ancestral","dragon hunter crossbow","elder maul","dexterous prayer scroll","arcane prayer scroll","olmlet","twisted","olm","xeric's","xerics","masori","fang","lightbearer","ward of elidinis","sanguinesti","scythe","verzik");
    }
    private static boolean isSlayer(String n) { return nameAny(n, "slayer","black mask","slayer helm","slayer helmet","brimstone key","konar","cannon","cannon base","cannon stand","cannon barrels","cannon furnace","slayer ring","broad bolt","broad arrows"); }
    private static boolean isBarrows(String n) { return nameAny(n, "ahrim","dharok","guthan","karil","torag","verac","barrows","amulet of the damned"); }
}
