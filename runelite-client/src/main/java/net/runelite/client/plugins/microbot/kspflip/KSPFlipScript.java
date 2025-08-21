package net.runelite.client.plugins.microbot.kspflip;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ItemID;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.Script;
import net.runelite.client.plugins.microbot.breakhandler.BreakHandlerScript;
import net.runelite.client.plugins.microbot.util.bank.Rs2Bank;
import net.runelite.client.plugins.microbot.util.grandexchange.Rs2GrandExchange;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Inventory;
import net.runelite.client.plugins.microbot.util.math.Rs2Random;

@Slf4j
public class KSPFlipScript extends Script
{
    private Instant startTime;
    private long totalProfit = 0;
    private String status = "Idle";

    private final KSPFlipPanel panel;
    private final KSPFlipOverlay overlay;

    private final List<Integer> itemPool = new ArrayList<>();
    private final Map<Integer, Long> lastFlipped = new ConcurrentHashMap<>();

    public KSPFlipScript(KSPFlipPanel panel, KSPFlipOverlay overlay)
    {
        this.panel = panel;
        this.overlay = overlay;
    }

    public boolean run()
    {
        Microbot.enableAutoRunOn = true;
        startTime = Instant.now();
        mainScheduledFuture = scheduledExecutorService.scheduleWithFixedDelay(() -> {
            try {
                if (!super.run()) {
                    return;
                }

                if (BreakHandlerScript.breakIn > 0 && BreakHandlerScript.breakIn < 60) {
                    status = "Preparing for break";
                    Rs2GrandExchange.closeExchange();
                    sleep(2000);
                    return;
                }

                if (itemPool.isEmpty() || Rs2Random.betweenInclusive(0, 100) < 5) {
                    refreshItemPool();
                }

                if (!Rs2Bank.isOpen() && Rs2Inventory.hasItem(ItemID.COINS_995)) {
                    flipLoop();
                }

                overlay.updateOverlay(getProfit(), getStatus(), getRuntime());
            } catch (Exception e) {
                log.error("Error in flip loop", e);
                status = "Error - retrying";
            }
        }, 0, 600, TimeUnit.MILLISECONDS);
        return true;
    }

    private void refreshItemPool()
    {
        itemPool.clear();
        status = "Refreshing items";

        for (int itemId = 0; itemId < 30000; itemId++)
        {
            long buy = Rs2GrandExchange.getBuyPrice(itemId);
            long sell = Rs2GrandExchange.getSellPrice(itemId);
            long vol = Rs2GrandExchange.getVolume(itemId);

            if (buy <= 0 || sell <= 0)
            {
                continue;
            }
            if (sell < buy)
            {
                continue;
            }
            if (vol < 5000)
            {
                continue;
            }
            if (isTradeLimitReached(itemId))
            {
                continue;
            }

            itemPool.add(itemId);
        }

        status = "Items found: " + itemPool.size();
    }

    private void flipLoop()
    {
        if (itemPool.isEmpty())
        {
            status = "No items found";
            return;
        }

        for (int itemId : itemPool)
        {
            if (Rs2GrandExchange.getAvailableSlot() == null)
            {
                break;
            }

            status = "Flipping " + itemId;
            long buyPrice = Rs2GrandExchange.getSellPrice(itemId);

            int quantity = Math.min(getAffordableQuantity(buyPrice), 1000);
            if (quantity <= 0)
            {
                continue;
            }

            Rs2GrandExchange.buyItem(itemId, quantity, buyPrice);
            sleepUntil(() -> Rs2GrandExchange.findSlotForItem(itemId, false) != null, 3000);
            lastFlipped.put(itemId, System.currentTimeMillis());
        }

        if (Rs2GrandExchange.hasFinishedBuyingOffers() || Rs2GrandExchange.hasFinishedSellingOffers())
        {
            collectProfit();
        }
    }

    private int getAffordableQuantity(long price)
    {
        int coins = Rs2Inventory.count(ItemID.COINS_995);
        return (int) (coins / price);
    }

    private void collectProfit()
    {
        int coinsBefore = Rs2Inventory.count(ItemID.COINS_995);
        Rs2GrandExchange.collectAllToInventory();
        int coinsAfter = Rs2Inventory.count(ItemID.COINS_995);
        long profit = coinsAfter - coinsBefore;
        if (profit > 0)
        {
            totalProfit += profit;
            panel.updateLastFlip(profit);
        }
    }

    private boolean isTradeLimitReached(int itemId)
    {
        if (!lastFlipped.containsKey(itemId))
        {
            return false;
        }
        long last = lastFlipped.get(itemId);
        return System.currentTimeMillis() - last < Duration.ofHours(4).toMillis();
    }

    public void shutdown()
    {
        super.shutdown();
    }

    public String getStatus()
    {
        return status;
    }

    public long getProfit()
    {
        return totalProfit;
    }

    public String getRuntime()
    {
        Duration d = Duration.between(startTime, Instant.now());
        return String.format("%02d:%02d:%02d", d.toHours(), d.toMinutesPart(), d.toSecondsPart());
    }
}
