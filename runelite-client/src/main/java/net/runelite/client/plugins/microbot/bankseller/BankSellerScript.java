package net.runelite.client.plugins.microbot.bankseller;

import net.runelite.api.gameval.ItemID;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.Script;
import net.runelite.client.plugins.microbot.util.bank.Rs2Bank;
import net.runelite.client.plugins.microbot.util.grandexchange.Rs2GrandExchange;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Inventory;
import net.runelite.client.plugins.microbot.util.inventory.Rs2ItemModel;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static net.runelite.client.plugins.microbot.util.Global.sleep;
import static net.runelite.client.plugins.microbot.util.Global.sleepUntil;

public class BankSellerScript extends Script {

    public boolean run(BankSellerConfig config) {
        Microbot.enableAutoRunOn = false;
        mainScheduledFuture = scheduledExecutorService.scheduleWithFixedDelay(() -> {
            try {
                if (!Microbot.isLoggedIn() || !super.run() || !isRunning()) {
                    return;
                }

                // If inventory empty attempt to withdraw tradeable items from bank
                if (Rs2Inventory.isEmpty()) {
                    if (!withdrawTradeablesFromBank()) {
                        Microbot.log("No more tradeable items. Stopping Bank Seller.");
                        shutdown();
                    }
                    return;
                }

                // Ensure grand exchange is open
                if (!Rs2GrandExchange.isOpen()) {
                    Rs2GrandExchange.openExchange();
                    sleepUntil(Rs2GrandExchange::isOpen, 5000);
                    return;
                }

                sellInventory();

                if (Rs2Inventory.isEmpty() && !bankHasTradeables()) {
                    Microbot.log("Finished selling all tradeable items.");
                    shutdown();
                }
            } catch (Exception ex) {
                System.out.println(ex.getMessage());
            }
        }, 0, 2000, TimeUnit.MILLISECONDS);
        return true;
    }

    private boolean bankHasTradeables() {
        return Rs2Bank.getAll(item -> item.isTradeable() && item.getId() != ItemID.COINS)
                .findAny()
                .isPresent();
    }

    private boolean withdrawTradeablesFromBank() {
        if (!Rs2Bank.openBank()) {
            return true; // try again later
        }
        Rs2Bank.depositAll();
        sleepUntil(Rs2Inventory::isEmpty, 3000);

        // Ensure items are withdrawn as notes so they stack in the inventory
        Rs2Bank.setWithdrawAsNote();

        List<Rs2ItemModel> tradeables = Rs2Bank.getAll(item -> item.isTradeable() && item.getId() != ItemID.COINS)
                .collect(Collectors.toList());
        if (tradeables.isEmpty()) {
            Rs2Bank.closeBank();
            sleepUntil(() -> !Rs2Bank.isOpen(), 2000);
            return false; // nothing to withdraw
        }

        for (Rs2ItemModel item : tradeables) {
            if (Rs2Inventory.isFull()) {
                break;
            }
            int id = item.getId();
            int before = Rs2Inventory.get(id) != null ? Rs2Inventory.get(id).getQuantity() : 0;
            if (Rs2Bank.withdrawAll(id)) {
                sleepUntil(() -> Rs2Inventory.get(id) != null && Rs2Inventory.get(id).getQuantity() > before, 2400);
                sleep(200, 400);
            }
        }
        Rs2Bank.closeBank();
        sleepUntil(() -> !Rs2Bank.isOpen(), 2000);
        return true;
    }

    private void sellInventory() {
        List<Rs2ItemModel> items = Rs2Inventory.items()
                .filter(item -> item.isTradeable() && item.getId() != ItemID.COINS)
                .collect(Collectors.toList());

        for (Rs2ItemModel item : items) {
            while (Rs2GrandExchange.getAvailableSlot() == null) {
                if (Rs2GrandExchange.hasSoldOffer()) {
                    Rs2GrandExchange.collectAllToBank();
                    sleep(600, 1200);
                } else {
                    return; // wait for slot on next iteration
                }
            }
            int price = Rs2GrandExchange.getSellPrice(item.getId());
            if (price <= 0) {
                price = Rs2GrandExchange.getPrice(item.getId());
            }
            Rs2GrandExchange.sellItem(item.getName(), item.getQuantity(), price);
            sleepUntil(() -> !Rs2GrandExchange.isOfferScreenOpen(), 5000);
            sleep(600, 1200);
        }
    }

    @Override
    public void shutdown() {
        super.shutdown();
    }
}

