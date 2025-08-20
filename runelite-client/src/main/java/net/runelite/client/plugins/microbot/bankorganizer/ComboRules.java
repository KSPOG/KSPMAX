package net.runelite.client.plugins.microbot.bankorganizer;

import net.runelite.client.game.ItemManager;
import java.util.*;

final class ComboRules {
    private ComboRules() {}

    static Map<String, Set<Integer>> buildBuiltInCombos(Map<Category, List<OrganizerService.ItemStack>> base,
                                                        ItemManager itemManager,
                                                        BankOrganizerConfig cfg) {
        Map<String, Set<Integer>> out = new LinkedHashMap<>();
        if (!cfg.enableCombos()) return out;

        if (cfg.comboRaidsAll()) out.put(tag("Raids Gear (All)", "🏛️"), union(base,
                Category.RAIDS, Category.COMBAT, Category.ARMOUR, Category.RUNES, Category.TELEPORTS, Category.FOOD, Category.POTIONS));
        if (cfg.comboRaidsTOA()) out.put(tag("Raids — TOA", "🏜️"), filterByName(union(base,
                Category.RAIDS, Category.COMBAT, Category.ARMOUR, Category.RUNES, Category.TELEPORTS, Category.FOOD, Category.POTIONS), itemManager,
                "fang","masori","elidinis","ward","lightbearer","akh"));
        if (cfg.comboRaidsCOX()) out.put(tag("Raids — COX", "⛰️"), filterByName(union(base,
                Category.RAIDS, Category.COMBAT, Category.ARMOUR, Category.RUNES, Category.TELEPORTS, Category.FOOD, Category.POTIONS), itemManager,
                "twisted","tbow","ancestral","kodai","dinh","olm"));
        if (cfg.comboRaidsTOB()) out.put(tag("Raids — TOB", "🕍"), filterByName(union(base,
                Category.RAIDS, Category.COMBAT, Category.ARMOUR, Category.RUNES, Category.TELEPORTS, Category.FOOD, Category.POTIONS), itemManager,
                "sanguinesti","scythe","aver","verzik","justiciar"));

        if (cfg.comboSlayer()) out.put(tag("Slayer Tasks", "😈"), union(base,
                Category.SLAYER, Category.COMBAT, Category.ARMOUR, Category.AMMO, Category.FOOD, Category.POTIONS, Category.TELEPORTS, Category.TOOLS));
        if (cfg.comboPvP()) out.put(tag("PvP / PK", "🗡️"), union(base,
                Category.COMBAT, Category.ARMOUR, Category.AMMO, Category.TELEPORTS, Category.FOOD, Category.POTIONS));
        if (cfg.comboSkilling()) out.put(tag("Skilling Sets", "🧰"), union(base,
                Category.SKILLING, Category.TOOLS, Category.FARMING, Category.TELEPORTS, Category.FOOD, Category.POTIONS));
        if (cfg.comboClue()) out.put(tag("Clue Prep", "🧩"), union(base,
                Category.CLUE, Category.TELEPORTS, Category.RUNES, Category.TOOLS, Category.FOOD));

        addCustomKeyword(out, itemManager, cfg.customCombo1Name(), cfg.customCombo1Keywords());
        addCustomKeyword(out, itemManager, cfg.customCombo2Name(), cfg.customCombo2Keywords());
        return out;
    }

    static Map<String, Set<Integer>> buildRuleEditorCombos(Map<Category, List<OrganizerService.ItemStack>> base,
                                                           ItemManager itemManager,
                                                           String json) {
        Map<String, Set<Integer>> out = new LinkedHashMap<>();
        if (json == null || json.trim().isEmpty()) return out;
        java.util.List<java.util.Map<String, Object>> rules = TinyJson.parseArrayOfObjects(json.trim());
        for (java.util.Map<String,Object> rule : rules) {
            String name = TinyJson.asString(rule.get("name"));
            if (name == null || name.isEmpty()) continue;
            java.util.List<String> catNames = TinyJson.asStringList(rule.get("includeCategories"));
            java.util.List<String> kw = TinyJson.asStringList(rule.get("includeKeywords"));
            Set<Integer> ids = new LinkedHashSet<>();
            if (catNames != null && !catNames.isEmpty()) {
                java.util.List<Category> cats = new java.util.ArrayList<>();
                for (String cn : catNames) { try { cats.add(Category.valueOf(cn.trim().toUpperCase())); } catch (Exception ignored) {} }
                ids.addAll(union(base, cats.toArray(new Category[0])));
            }
            if (kw != null && !kw.isEmpty()) ids = filterByName(ids, itemManager, kw.toArray(new String[0]));
            out.put("🧩 " + name, ids);
        }
        return out;
    }

    static Set<Integer> union(Map<Category, List<OrganizerService.ItemStack>> base, Category... cats) {
        Set<Integer> out = new LinkedHashSet<>();
        for (Category c : cats) {
            java.util.List<OrganizerService.ItemStack> list = base.get(c);
            if (list == null) continue;
            for (OrganizerService.ItemStack s : list) out.add(s.id);
        }
        return out;
    }

    private static String tag(String base, String emoji) { return emoji + " " + base; }

    private static void addCustomKeyword(Map<String, Set<Integer>> out, ItemManager itemManager, String name, String csv) {
        if (name == null || name.trim().isEmpty()) return;
        Set<Integer> set = filterByName(allIds(itemManager), itemManager, csv);
        out.put("🧩 " + name.trim(), set);
    }

    private static Set<Integer> allIds(ItemManager itemManager) {
        Set<Integer> set = new LinkedHashSet<>();
        final int limit = 30_000; // approximate max item ID; RuneLite API lacks a count method
        for (int id = 0; id < limit; id++) {
            try {
                itemManager.getItemComposition(id);
                set.add(id);
            }
            catch (Exception ignored) {
            }
        }
        return set;
    }

    static Set<Integer> filterByName(Set<Integer> ids, ItemManager itemManager, String... needles) {
        if (needles == null || needles.length == 0) return ids;
        Set<Integer> out = new LinkedHashSet<>();
        java.util.List<String> nlist = new java.util.ArrayList<>();
        if (needles.length == 1 && needles[0] != null && needles[0].contains(",")) {
            for (String part : needles[0].split(",")) if (!part.trim().isEmpty()) nlist.add(part.trim().toLowerCase());
        } else {
            for (String s : needles) if (s != null && !s.trim().isEmpty()) nlist.add(s.trim().toLowerCase());
        }
        for (int id : ids) {
            try { String n = itemManager.getItemComposition(id).getName().toLowerCase(); for (String k : nlist) { if (n.contains(k)) { out.add(id); break; } } } catch (Exception ignored) {}
        }
        return out;
    }
}
