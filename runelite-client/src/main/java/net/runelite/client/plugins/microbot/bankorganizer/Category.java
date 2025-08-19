package net.runelite.client.plugins.microbot.bankorganizer;

enum Category {
    RAIDS(1, "Raids", "🏛️"),
    SLAYER(2, "Slayer", "😈"),
    BARROWS(3, "Barrows", "⚰️"),
    COMBAT(4, "Combat", "⚔️"),
    ARMOUR(5, "Armour", "🛡️"),
    AMMO(6, "Ammo", "🎯"),
    POTIONS(7, "Potions", "🧪"),
    FOOD(8, "Food", "🍖"),
    RUNES(9, "Runes", "🔮"),
    TELEPORTS(10, "Teleports", "🧭"),
    SKILLING(11, "Skilling", "⛏️"),
    FARMING(12, "Farming", "🌱"),
    CLUE(13, "Clues", "🧩"),
    TOOLS(14, "Tools", "🧰"),
    QUEST(15, "Quest", "📜"),
    MISC(16, "Misc", "📦");

    private final int order; private final String display; private final String emoji;
    Category(int order, String display, String emoji) { this.order=order; this.display=display; this.emoji=emoji; }
    public int order() { return order; }
    public String displayName() { return display; }
    public String emoji() { return emoji; }
}
