package net.runelite.client.plugins.microbot.geflipper;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.GrandExchangeOffer;
import net.runelite.api.GrandExchangeOfferState;
import net.runelite.api.ItemComposition;
import net.runelite.api.gameval.ItemID;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.Script;
import net.runelite.client.plugins.microbot.breakhandler.BreakHandlerScript;
import net.runelite.client.plugins.microbot.util.antiban.Rs2Antiban;
import net.runelite.client.plugins.microbot.util.antiban.Rs2AntibanSettings;
import net.runelite.client.plugins.microbot.util.antiban.enums.ActivityIntensity;
import net.runelite.client.plugins.microbot.util.grandexchange.GrandExchangeAction;
import net.runelite.client.plugins.microbot.util.grandexchange.GrandExchangeRequest;
import net.runelite.client.plugins.microbot.util.grandexchange.GrandExchangeSlots;
import net.runelite.client.plugins.microbot.util.grandexchange.Rs2GrandExchange;
import net.runelite.client.plugins.microbot.util.grandexchange.models.WikiPrice;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Inventory;

import java.util.*;
import java.util.concurrent.TimeUnit;

import static net.runelite.client.plugins.microbot.util.Global.sleepUntil;

@Slf4j
public class GeFlipperScript extends Script {
    public static final double VERSION = 1.0;
    private final Map<GrandExchangeSlots, FlipOffer> offers = new HashMap<>();
    private final Map<Integer, Long> cooldown = new HashMap<>();
    private long profit = 0;
    private String latestFlip = "";
    private GeFlipperPanel panel;

    private final int[] F2P_ITEMS = new int[]{ItemID.LOBSTER, ItemID.COAL, ItemID.IRON_ORE, ItemID.SWORD_FISH, ItemID.NATURE_RUNE};
    private final int[] MEMBER_ITEMS = new int[]{ItemID.RAW_SHARK, ItemID.DRAGON_BONES, ItemID.MAHOGANY_PLANK, ItemID.RUNE_BAR};

    private static class FlipOffer {
        int itemId;
        int buyPrice;
        int sellPrice;
        int quantity;
        boolean selling;
        FlipOffer(int itemId, int buyPrice, int sellPrice, int quantity) {
            this.itemId = itemId;
            this.buyPrice = buyPrice;
            this.sellPrice = sellPrice;
            this.quantity = quantity;
            this.selling = false;
        }
    }

    public long getProfit() { return profit; }
    public String getLatestFlip() { return latestFlip; }

    public boolean run(GeFlipperConfig config, GeFlipperPanel panel) {
        this.panel = panel;
        Rs2AntibanSettings.naturalMouse = true;
        Rs2Antiban.setActivityIntensity(ActivityIntensity.MEDIUM);
        mainScheduledFuture = scheduledExecutorService.scheduleWithFixedDelay(() -> {
            try {
                if (!super.run() || !Microbot.isLoggedIn()) return;

                if (BreakHandlerScript.breakIn >= 0 && BreakHandlerScript.breakIn <= 60) {
                    Microbot.status = "Awaiting break";
                    if (Rs2GrandExchange.isOpen()) {
                        Rs2GrandExchange.closeExchange();
                    }
                    return;
                }

                if (!Rs2GrandExchange.isOpen()) {
                    Rs2GrandExchange.openExchange();
                    return;
                }

                handleCompletedOffers();
                fillEmptySlots();

            } catch (Exception e) {
                log.error("GE flipper error {}", e.getMessage(), e);
            }
        }, 0, 3, TimeUnit.SECONDS);
        return true;
    }

    private void handleCompletedOffers() {
        for (GrandExchangeSlots slot : GrandExchangeSlots.values()) {
            FlipOffer fo = offers.get(slot);
            if (fo == null) continue;

            if (!fo.selling && Rs2GrandExchange.hasBoughtOffer(slot)) {
                Rs2GrandExchange.collectOffer(slot, false);
                int bought = Rs2GrandExchange.getItemsBoughtFromOffer(slot);
                fo.quantity = bought;
                ItemComposition ic = Microbot.getRs2ItemManager().getItemDefinition(fo.itemId);
                Microbot.status = "Selling " + ic.getName();
                Rs2GrandExchange.processOffer(GrandExchangeRequest.builder()
                        .slot(slot)
                        .action(GrandExchangeAction.SELL)
                        .itemName(ic.getName())
                        .price(fo.sellPrice)
                        .quantity(fo.quantity)
                        .build());
                fo.selling = true;
                sleepUntil(() -> {
                    GrandExchangeOffer offer = Microbot.getClient().getGrandExchangeOffers()[slot.ordinal()];
                    return offer.getState() == GrandExchangeOfferState.SELLING;
                }, 3000);
            } else if (fo.selling && Rs2GrandExchange.hasSoldOffer(slot)) {
                Rs2GrandExchange.collectOffer(slot, false);
                long gain = (long) (fo.sellPrice - fo.buyPrice) * fo.quantity;
                profit += gain;
                ItemComposition ic = Microbot.getRs2ItemManager().getItemDefinition(fo.itemId);
                latestFlip = ic.getName() + " +" + gain;
                panel.updateLatestFlip(latestFlip);
                panel.updateProfit(profit);
                offers.remove(slot);
                Microbot.status = "Idle";
            }
        }
    }

    private void fillEmptySlots() {
        int coins = Rs2Inventory.count(ItemID.COINS);
        List<Integer> pool = new ArrayList<>();
        for (int id : F2P_ITEMS) pool.add(id);
        if (Microbot.getClient().isMembersWorld()) {
            for (int id : MEMBER_ITEMS) pool.add(id);
        }
        Collections.shuffle(pool);
        for (GrandExchangeSlots slot : GrandExchangeSlots.values()) {
            if (!slotIsEmpty(slot) || offers.containsKey(slot)) continue;
            for (int itemId : pool) {
                if (onCooldown(itemId)) continue;
                WikiPrice price = Rs2GrandExchange.getRealTimePrices(itemId);
                if (price == null) continue;
                int sellPrice = price.getSellPrice();
                if (sellPrice <= 0 || sellPrice > coins) continue;
                int quantity = Math.max(1, Math.min(100, coins / sellPrice));
                ItemComposition ic = Microbot.getRs2ItemManager().getItemDefinition(itemId);
                Microbot.status = "Buying " + ic.getName();
                Rs2GrandExchange.processOffer(GrandExchangeRequest.builder()
                        .slot(slot)
                        .action(GrandExchangeAction.BUY)
                        .itemName(ic.getName())
                        .price(sellPrice)
                        .quantity(quantity)
                        .build());
                offers.put(slot, new FlipOffer(itemId, sellPrice, price.getBuyPrice(), quantity));
                cooldown.put(itemId, System.currentTimeMillis());
                sleepUntil(() -> {
                    GrandExchangeOffer offer = Microbot.getClient().getGrandExchangeOffers()[slot.ordinal()];
                    return offer.getState() == GrandExchangeOfferState.BUYING;
                }, 3000);
                return;
            }
        }
    }

    private boolean slotIsEmpty(GrandExchangeSlots slot) {
        GrandExchangeOffer offer = Microbot.getClient().getGrandExchangeOffers()[slot.ordinal()];
        return offer == null || offer.getState() == GrandExchangeOfferState.EMPTY;
    }

    private boolean onCooldown(int itemId) {
        long now = System.currentTimeMillis();
        return cooldown.containsKey(itemId) && now - cooldown.get(itemId) < TimeUnit.HOURS.toMillis(4);
    }

    @Override
    public void shutdown() {
        super.shutdown();
        offers.clear();
        cooldown.clear();
    }
}
