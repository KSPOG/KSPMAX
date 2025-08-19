package net.runelite.client.plugins.microbot.bankorganizer;

final class PresetPacks {
    private PresetPacks() {}

    // Default prefilled Rule‑Editor JSON (master list)
    static final String DEFAULT_RULES_JSON = """
[
  {"name":"TOA — Mage (Shadow/Support)","includeCategories":["RAIDS","COMBAT","ARMOUR","RUNES","POTIONS","FOOD","TELEPORTS"],"includeKeywords":["tumeken","shadow","virtus","ely","lightbearer","elidinis","ward","ancient","sceptre","blood rune","soul rune"]},
  {"name":"TOA — Ranged (Masori/Fang swap)","includeCategories":["RAIDS","COMBAT","ARMOUR","AMMO","POTIONS","FOOD","TELEPORTS"],"includeKeywords":["masori","zaryte","bowfa","crystal","lightbearer","venator","keris","fang","elidinis"]},
  {"name":"TOA — Melee (Fang/BGS)","includeCategories":["RAIDS","COMBAT","ARMOUR","POTIONS","FOOD","TELEPORTS"],"includeKeywords":["fang","keris","bandos godsword","justiciar","primordial","torture","inquisitor"]},
  {"name":"COX — Ranged (Tbow/DHCB)","includeCategories":["RAIDS","COMBAT","ARMOUR","AMMO","POTIONS","FOOD","TELEPORTS"],"includeKeywords":["twisted","tbow","dragon hunter crossbow","crystal helm","crystal body","crystal legs","anguish","pegs"]},
  {"name":"COX — Mage (Ancestral/Kodai)","includeCategories":["RAIDS","COMBAT","ARMOUR","RUNES","POTIONS","FOOD","TELEPORTS"],"includeKeywords":["ancestral","kodai","trident","harmonised","occult","tormented","blood rune","death rune"]},
  {"name":"COX — Melee (DWH/Claw/BGS)","includeCategories":["RAIDS","COMBAT","ARMOUR","POTIONS","FOOD","TELEPORTS"],"includeKeywords":["dragon warhammer","claws","bandos godsword","bandos","primordial","torture","defender"]},
  {"name":"TOB — Melee (Scythe/Avernic)","includeCategories":["RAIDS","COMBAT","ARMOUR","POTIONS","FOOD","TELEPORTS"],"includeKeywords":["scythe","ghrazi","avernic","justiciar","torture","ferocious","primordial"]},
  {"name":"TOB — Mage (Sang/Arceuus)","includeCategories":["RAIDS","COMBAT","ARMOUR","RUNES","POTIONS","FOOD","TELEPORTS"],"includeKeywords":["sanguinesti","occult","tormented","ahrim","ancient","blood rune","death rune"]},
  {"name":"Slayer — Demons (Arclight)","includeCategories":["SLAYER","COMBAT","ARMOUR","AMMO","POTIONS","FOOD","TELEPORTS"],"includeKeywords":["arclight","black mask","slayer helm","demon","holy water"]},
  {"name":"Slayer — Dragons (Anti-fire)","includeCategories":["SLAYER","COMBAT","ARMOUR","AMMO","POTIONS","FOOD","TELEPORTS"],"includeKeywords":["anti-dragon","antifire","dragonfire shield","dragon hunter lance","extended antifire","ranging potion"]},
  {"name":"Slayer — Undead (Salve)","includeCategories":["SLAYER","COMBAT","ARMOUR","AMMO","POTIONS","FOOD","TELEPORTS"],"includeKeywords":["salve","ivandis","undead","crumble undead","ensouled"]},
  {"name":"Clue Prep — Universal","includeCategories":["CLUE","TELEPORTS","RUNES","TOOLS","FOOD","POTIONS"],"includeKeywords":["spade","sextant","chart","watch","stash","chronicle","games necklace","dueling ring","skills necklace"]},
  {"name":"Clue — Easy","includeCategories":["CLUE","TELEPORTS","RUNES","TOOLS"],"includeKeywords":["chronicle","necklace of passage","games necklace","falador","varrock","camelot","ardougne"]},
  {"name":"Clue — Medium","includeCategories":["CLUE","TELEPORTS","RUNES","TOOLS"],"includeKeywords":["digsite","slayer ring","ecto","khazard","fairy","miscellania","kandarin"]},
  {"name":"Clue — Hard/Elite","includeCategories":["CLUE","TELEPORTS","RUNES","TOOLS"],"includeKeywords":["wilderness","gwd","ancient shard","xeric","karamja gloves","royal seed pod","ecto"]},
  {"name":"Barrows — Sets","includeCategories":["BARROWS","ARMOUR","COMBAT","POTIONS","FOOD"],"includeKeywords":["ahrim","dharok","guthan","karil","torag","verac","karils","dharoks"]},
  {"name":"Skilling — Woodcut/Fletch","includeCategories":["SKILLING","TOOLS"],"includeKeywords":["axe","knife","logs","bowstring","feather"]},
  {"name":"Skilling — Fish/Cook","includeCategories":["SKILLING","TOOLS","FOOD","POTIONS"],"includeKeywords":["harpoon","net","cage","feather","karambwan","raw","cooking gauntlets"]},
  {"name":"Ironman — Early Progress","includeCategories":["COMBAT","ARMOUR","TOOLS","TELEPORTS","FOOD","POTIONS"],"includeKeywords":["graceful","ardougne cloak","mythical","accumulator","glory","combat bracelet","rune"]},
  {"name":"Ironman — Midgame Bossing","includeCategories":["COMBAT","ARMOUR","AMMO","POTIONS","FOOD","TELEPORTS"],"includeKeywords":["trident","whip","serp","blowpipe","void","salve","slayer helm","bandos","obby"]},
  {"name":"Ironman — Late Bossing","includeCategories":["COMBAT","ARMOUR","AMMO","POTIONS","FOOD","TELEPORTS","RAIDS"],"includeKeywords":["tbow","scythe","sanguinesti","kodai","ancestral","justiciar","zaryte","lightbearer"]}
]
""";

    static String forPack(BankOrganizerConfig.PresetPack pack) {
        switch (pack) {
            case RAIDS:
                return join(
                        rule("TOA — Mage (Shadow/Support)"),
                        rule("TOA — Ranged (Masori/Fang swap)"),
                        rule("TOA — Melee (Fang/BGS)"),
                        rule("COX — Ranged (Tbow/DHCB)"),
                        rule("COX — Mage (Ancestral/Kodai)"),
                        rule("COX — Melee (DWH/Claw/BGS)"),
                        rule("TOB — Melee (Scythe/Avernic)"),
                        rule("TOB — Mage (Sang/Arceuus)")
                );
            case SLAYER:
                return join(
                        rule("Slayer — Demons (Arclight)"),
                        rule("Slayer — Dragons (Anti-fire)"),
                        rule("Slayer — Undead (Salve)")
                );
            case IRONMAN:
                return join(
                        rule("Ironman — Early Progress"),
                        rule("Ironman — Midgame Bossing"),
                        rule("Ironman — Late Bossing")
                );
            case SKILLING:
                return join(
                        rule("Skilling — Woodcut/Fletch"),
                        rule("Skilling — Fish/Cook")
                );
            case BARROWS:
                return join(rule("Barrows — Sets"));
            case CLUES:
                return join(
                        rule("Clue Prep — Universal"),
                        rule("Clue — Easy"),
                        rule("Clue — Medium"),
                        rule("Clue — Hard/Elite")
                );
            case ALL:
                return DEFAULT_RULES_JSON;
            case NONE:
            default:
                return "[]";
        }
    }

    private static String rule(String name) {
        String src = DEFAULT_RULES_JSON;
        int i = src.indexOf("\"name\":\""+name);
        if (i < 0) return "{}";
        int start = src.lastIndexOf('{', i);
        int end = src.indexOf('}', i);
        return src.substring(start, end+1);
    }

    private static String join(String... objs) {
        StringBuilder sb = new StringBuilder("[");
        boolean first = true;
        for (String o : objs) {
            if (o == null || o.isEmpty() || o.equals("{}")) continue;
            if (!first) sb.append(','); first = false; sb.append(o);
        }
        return sb.append(']').toString();
    }
}
