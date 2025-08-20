package net.runelite.client.plugins.microbot.grandexchangeflipper;

import java.time.Duration;
import java.time.Instant;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.GrandExchangeOffer;
import net.runelite.api.ItemComposition;
import net.runelite.api.ItemID;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.Script;
import net.runelite.client.plugins.microbot.breakhandler.BreakHandlerScript;
import net.runelite.client.plugins.microbot.util.grandexchange.GrandExchangeSlots;
import net.runelite.client.plugins.microbot.util.grandexchange.Rs2GrandExchange;
import net.runelite.client.plugins.microbot.util.grandexchange.models.WikiPrice;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Inventory;
import net.runelite.client.plugins.microbot.util.player.Rs2Player;
import net.runelite.client.plugins.microbot.util.antiban.Rs2AntibanSettings;
import net.runelite.client.plugins.microbot.util.antiban.Rs2Antiban;
import net.runelite.client.plugins.microbot.util.antiban.enums.ActivityIntensity;

@Slf4j
public class GrandExchangeFlipperScript extends Script
{
    public static final double VERSION = 1.0;

    private final List<Integer> f2pItems = new ArrayList<>();
    private final List<Integer> memberItems = new ArrayList<>();

    private final Map<Integer, Instant> cooldown = new HashMap<>();
    private final Map<Integer, Integer> slotItem = new HashMap<>();
    private final Map<Integer, Integer> slotBuyPrice = new HashMap<>();
    private final Map<Integer, Integer> slotSellPrice = new HashMap<>();

    private long profit = 0;
    private long startTime;
    private GrandExchangeFlipperPanel panel;
    private String status = "Idle";

    private void setStatus(String status)
    {
        this.status = status;
        Microbot.status = status;
    }

    private void buildItemLists()
    {
        if (!f2pItems.isEmpty() || !memberItems.isEmpty())
        {
            return;
        }

        for (Field field : ItemID.class.getDeclaredFields())
        {
            if (field.getType() != int.class)
            {
                continue;
            }
            try
            {
                int id = field.getInt(null);
                ItemComposition composition = Microbot.getItemManager().getItemComposition(id);
                if (composition == null || !composition.isTradeable())
                {
                    continue;
                }
                if (composition.isMembers())
                {
                    memberItems.add(id);
                }
                else
                {
                    f2pItems.add(id);
                }
            }
            catch (IllegalAccessException ignored)
            {
            }
        }
    }

    public boolean run(GrandExchangeFlipperPlugin plugin, GrandExchangeFlipperPanel panel)
    {
        this.panel = panel;
        startTime = System.currentTimeMillis();
        Microbot.getClientThread().invoke(this::buildItemLists);
        Rs2AntibanSettings.naturalMouse = true;
        Rs2Antiban.setActivityIntensity(ActivityIntensity.VERY_LOW);
        setStatus("Starting");
        mainScheduledFuture = scheduledExecutorService.scheduleWithFixedDelay(() ->
        {
            try
            {
                if (!Microbot.isLoggedIn())
                {
                    setStatus("Logged out");
                    return;
                }
                if (BreakHandlerScript.breakIn > 0 && BreakHandlerScript.breakIn <= 60)
                {
                    setStatus("Preparing for break");
                    Rs2GrandExchange.closeExchange();
                    return;
                }
                if (Rs2GrandExchange.hasFinishedBuyingOffers() || Rs2GrandExchange.hasFinishedSellingOffers())
                {
                    setStatus("Collecting offers");
                    Rs2GrandExchange.collectAllToInventory();
                }
                if (!Rs2GrandExchange.isOpen())
                {
                    setStatus("Opening GE");
                    Rs2GrandExchange.openExchange();
                    return;
                }

                int maxSlots = Rs2Player.isMember() ? 8 : 3;
                setStatus("Managing offers");
                for (int i = 0; i < maxSlots; i++)
                {
                    GrandExchangeOffer offer = Microbot.getClient().getGrandExchangeOffers()[i];
                    if (offer == null)
                    {
                        continue;
                    }
                    switch (offer.getState())
                    {
                        case EMPTY:
                            handleEmptySlot(i);
                            break;
                        case BOUGHT:
                            handleBoughtSlot(i);
                            break;
                        case SOLD:
                            handleSoldSlot(i);
                            break;
                        default:
                            break;
                    }
                }
            }
            catch (Exception ex)
            {
                log.error("Error in GE flipper", ex);
            }
        }, 0, 1500, TimeUnit.MILLISECONDS);
        return true;
    }

    private void handleEmptySlot(int slot)
    {
        setStatus("Placing buy offer");
        List<Integer> pool = new ArrayList<>(f2pItems);
        if (Rs2Player.isMember())
        {
            pool.addAll(memberItems);
        }
        Collections.shuffle(pool);
        for (int itemId : pool)
        {
            Instant last = cooldown.get(itemId);
            if (last != null && Instant.now().isBefore(last.plus(Duration.ofHours(4))))
            {
                continue;
            }
            WikiPrice price = Rs2GrandExchange.getRealTimePrices(itemId);
            if (price == null)
            {
                int guide = Microbot.getItemManager().getItemPrice(itemId);
                if (guide <= 0)
                {
                    continue;
                }
                price = new WikiPrice(guide, guide, 0);
            }
            int coins = Rs2Inventory.itemQuantity(ItemID.COINS);
            int quantity = Math.min(coins / price.sellPrice, 100);
            if (quantity <= 0)
            {
                continue;
            }
            ItemComposition composition = Microbot.getClientThread().runOnClientThreadOptional(() ->
                Microbot.getItemManager().getItemComposition(itemId)).orElse(null);
            if (composition == null)
            {
                continue;
            }
            String name = composition.getName();
            if (Rs2GrandExchange.buyItem(name, price.sellPrice, quantity))
            {
                slotItem.put(slot, itemId);
                slotBuyPrice.put(slot, price.sellPrice);
                slotSellPrice.put(slot, price.buyPrice);
                cooldown.put(itemId, Instant.now());
                break;
            }
        }
    }

    private void handleBoughtSlot(int slot)
    {
        setStatus("Selling items");
        Integer itemId = slotItem.get(slot);
        if (itemId == null)
        {
            return;
        }
        int quantity = Rs2GrandExchange.collectOfferAndGetQuantity(GrandExchangeSlots.values()[slot], false, itemId);
        if (quantity <= 0)
        {
            return;
        }
        ItemComposition composition = Microbot.getClientThread().runOnClientThreadOptional(() ->
            Microbot.getItemManager().getItemComposition(itemId)).orElse(null);
        if (composition == null)
        {
            return;
        }
        String name = composition.getName();
        int sellPrice = slotSellPrice.getOrDefault(slot, 0);
        Rs2GrandExchange.sellItem(name, quantity, sellPrice);
    }

    private void handleSoldSlot(int slot)
    {
        setStatus("Collecting profits");
        Integer itemId = slotItem.get(slot);
        if (itemId == null)
        {
            return;
        }
        int quantity = Rs2GrandExchange.collectOfferAndGetQuantity(GrandExchangeSlots.values()[slot], false, itemId);
        if (quantity <= 0)
        {
            return;
        }
        long itemProfit = (long) (slotSellPrice.get(slot) - slotBuyPrice.get(slot)) * quantity;
        profit += itemProfit;
        ItemComposition composition = Microbot.getClientThread().runOnClientThreadOptional(() ->
            Microbot.getItemManager().getItemComposition(itemId)).orElse(null);
        if (composition != null)
        {
            String name = composition.getName();
            if (panel != null)
            {
                panel.updateLatestFlip(name, itemProfit, profit);
            }
        }
        slotItem.remove(slot);
        slotBuyPrice.remove(slot);
        slotSellPrice.remove(slot);
    }

    public long getProfit()
    {
        return profit;
    }

    public Duration getRuntime()
    {
        if (startTime == 0)
        {
            return Duration.ZERO;
        }
        return Duration.ofMillis(System.currentTimeMillis() - startTime);
    }

    public String getStatus()
    {
        return status;
    }
}
