package net.runelite.client.plugins.microbot.bankorganizer;

import net.runelite.client.config.*;

@ConfigGroup("bankorganizer")
public interface BankOrganizerConfig extends Config {
    // Presets section
    @ConfigSection(
            name = "Presets",
            description = "Built-in category sets",
            position = 0
    )
    String sectionPresets = "sectionPresets";

    @ConfigItem(keyName = "useDefaultPreset", name = "Use Default Preset", description = "Create standard tabs incl. Raids/Slayer/Barrows", position = 1, section = sectionPresets)
    default boolean useDefaultPreset() { return true; }

    // Combination Tags section
    @ConfigSection(
            name = "Combination Tags",
            description = "Derived tags across categories",
            position = 10
    )
    String sectionCombos = "sectionCombos";

    @ConfigItem(keyName = "enableCombos", name = "Enable built-in combos", description = "Enable all preset combo toggles below", position = 11, section = sectionCombos)
    default boolean enableCombos() { return true; }

    // Built-in combos
    @ConfigItem(keyName = "comboRaidsAll", name = "Combo: Raids Gear (All)", description = "Union of raids gear/armour/weapons/consumables", position = 12, section = sectionCombos)
    default boolean comboRaidsAll() { return true; }
    @ConfigItem(keyName = "comboRaidsTOA", name = "Combo: Raids — TOA", description = "TOA-leaning items (fang/ward/lightbearer/masori)", position = 13, section = sectionCombos)
    default boolean comboRaidsTOA() { return true; }
    @ConfigItem(keyName = "comboRaidsCOX", name = "Combo: Raids — COX", description = "COX-leaning items (tbow/DHCB/ancestral)", position = 14, section = sectionCombos)
    default boolean comboRaidsCOX() { return true; }
    @ConfigItem(keyName = "comboRaidsTOB", name = "Combo: Raids — TOB", description = "TOB-leaning items (scythe/avernic/sang)", position = 15, section = sectionCombos)
    default boolean comboRaidsTOB() { return false; }

    @ConfigItem(keyName = "comboSlayer", name = "Combo: Slayer Tasks", description = "Slayer essentials (mask/helm, cannon, ammo, food, pots, tp)", position = 16, section = sectionCombos)
    default boolean comboSlayer() { return true; }
    @ConfigItem(keyName = "comboPvP", name = "Combo: PvP / PK", description = "Common PK loadouts (combat, armour, ammo, tp, food, pots)", position = 17, section = sectionCombos)
    default boolean comboPvP() { return false; }
    @ConfigItem(keyName = "comboSkilling", name = "Combo: Skilling Sets", description = "Tools + common mats for multi-skill sessions", position = 18, section = sectionCombos)
    default boolean comboSkilling() { return false; }
    @ConfigItem(keyName = "comboClue", name = "Combo: Clue Prep", description = "Clue items (teleports, runes, spade, stash)", position = 19, section = sectionCombos)
    default boolean comboClue() { return false; }

    // Custom keyword combos
    @ConfigItem(keyName = "customCombo1Name", name = "Custom combo 1 — name", description = "Name for custom combo tag", position = 20, section = sectionCombos)
    default String customCombo1Name() { return ""; }
    @ConfigItem(keyName = "customCombo1Keywords", name = "Custom combo 1 — keywords", description = "Comma terms to include (name contains)", position = 21, section = sectionCombos)
    default String customCombo1Keywords() { return ""; }
    @ConfigItem(keyName = "customCombo2Name", name = "Custom combo 2 — name", description = "Name for custom combo tag", position = 22, section = sectionCombos)
    default String customCombo2Name() { return ""; }
    @ConfigItem(keyName = "customCombo2Keywords", name = "Custom combo 2 — keywords", description = "Comma terms to include (name contains)", position = 23, section = sectionCombos)
    default String customCombo2Keywords() { return ""; }

    // Preset Packs
    enum PresetPack { NONE, RAIDS, SLAYER, IRONMAN, SKILLING, BARROWS, CLUES, ALL }

    @ConfigSection(
            name = "Preset Packs",
            description = "Apply curated rule sets",
            position = 24
    )
    String sectionPacks = "sectionPacks";

    @ConfigItem(keyName = "selectedPack", name = "Select pack", description = "Choose a preset pack to apply or load", position = 25, section = sectionPacks)
    default PresetPack selectedPack() { return PresetPack.RAIDS; }

    // Rule‑Editor JSON
    @ConfigSection(
            name = "Rule‑Editor (JSON)",
            description = "Define unlimited combos",
            position = 30
    )
    String sectionRuleEditor = "sectionRuleEditor";

    @ConfigItem(keyName = "ruleEditorJson", name = "Rules JSON", description = "Array of combo definitions. You can edit.", position = 31, section = sectionRuleEditor)
    default String ruleEditorJson() { return PresetPacks.DEFAULT_RULES_JSON; }

    // Base settings
    @ConfigItem(keyName = "prefixTabs", name = "Prefix tabs with order number", description = "Adds 01_, 02_, ... so tags sort predictably", position = 40)
    default boolean prefixTabs() { return true; }
    @ConfigItem(keyName = "decorate", name = "Decorate tab names", description = "Adds small symbols like ⚔️, ⛏️, 🧪, 🍖", position = 41)
    default boolean decorate() { return true; }
    @ConfigItem(keyName = "autoArrange", name = "Auto arrange (drag)", description = "Physically reorders bank by simulated drags (Microbot). Turn OFF if you only want tags.", position = 42)
    default boolean autoArrange() { return false; }
    @ConfigItem(keyName = "arrangeBy", name = "Arrange order", description = "Order within a category", position = 43)
    default ArrangeOrder arrangeOrder() { return ArrangeOrder.ALPHA; }
    @ConfigItem(keyName = "includePlaceholders", name = "Include placeholders", description = "Also tag placeholders in the bank", position = 44)
    default boolean includePlaceholders() { return true; }
    @ConfigItem(keyName = "dryRun", name = "Dry run", description = "Log actions without modifying anything", position = 45)
    default boolean dryRun() { return false; }
    @ConfigItem(keyName = "layoutFilePath", name = "Export/Import file path", description = "Path for JSON layout file", position = 46)
    default String layoutFilePath() { return "bank_layout.json"; }

    // Category keyword extenders
    @ConfigItem(keyName = "extraKeywordsRaids", name = "Extra keywords — Raids", description = "Comma-separated extra terms to classify as Raids", position = 50)
    default String extraKeywordsRaids() { return ""; }
    @ConfigItem(keyName = "extraKeywordsSlayer", name = "Extra keywords — Slayer", description = "Comma-separated extra terms to classify as Slayer", position = 51)
    default String extraKeywordsSlayer() { return ""; }
    @ConfigItem(keyName = "extraKeywordsBarrows", name = "Extra keywords — Barrows", description = "Comma-separated extra terms to classify as Barrows", position = 52)
    default String extraKeywordsBarrows() { return ""; }

    enum ArrangeOrder { ALPHA, GE_PRICE_DESC, HA_VALUE_DESC, WEAPON_ARMOUR_FIRST }

    // Physical arrange scope
    enum ArrangeScope { WHOLE_BANK, CURRENT_VIEW_ONLY, NATIVE_TABS }

    // Humanization / safety
    @ConfigItem(keyName = "dragMinMs", name = "Min drag delay (ms)", description = "Minimum delay between drags", position = 60)
    default int dragMinMs() { return 80; }
    @ConfigItem(keyName = "dragMaxMs", name = "Max drag delay (ms)", description = "Maximum delay between drags", position = 61)
    default int dragMaxMs() { return 160; }
    @ConfigItem(keyName = "arrangeScope", name = "Arrange scope", description = "Where to physically reorder", position = 62)
    default ArrangeScope arrangeScope() { return ArrangeScope.CURRENT_VIEW_ONLY; }

    // Multi‑tab Arrange section
    @ConfigSection(
            name = "Multi‑tab Arrange",
            description = "Physically reorder multiple native tabs in one run",
            position = 63
    )
    String sectionMultiTab = "sectionMultiTab";

    @ConfigItem(keyName = "tabsToArrange", name = "Tabs to arrange", description = "Comma/Range list, e.g. 1-4,6,8 (native tabs). 0 = main.", position = 64, section = sectionMultiTab)
    default String tabsToArrange() { return "1-8"; }

    @ConfigItem(keyName = "includeMainTab", name = "Include main tab (0)", description = "Whether to include the main (All) tab when multi‑arranging", position = 65, section = sectionMultiTab)
    default boolean includeMainTab() { return false; }

    @ConfigItem(keyName = "tabSwitchMinMs", name = "Min tab switch delay (ms)", description = "Delay between switching tabs (min)", position = 66, section = sectionMultiTab)
    default int tabSwitchMinMs() { return 120; }

    @ConfigItem(keyName = "tabSwitchMaxMs", name = "Max tab switch delay (ms)", description = "Delay between switching tabs (max)", position = 67, section = sectionMultiTab)
    default int tabSwitchMaxMs() { return 240; }
}
