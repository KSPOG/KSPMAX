package net.runelite.client.plugins.microbot.bankorganizer;

import net.runelite.api.*;
import java.util.*;

final class MicrobotBankMover {
    private MicrobotBankMover() {}

    static void reorderBank(Client client, java.util.List<OrganizerService.ItemStack> desired, java.util.function.Consumer<String> log, int minDelayMs, int maxDelayMs) {
        final Widget bankContainer = client.getWidget(WidgetInfo.BANK_ITEM_CONTAINER);
        if (bankContainer == null || bankContainer.getDynamicChildren() == null) { log.accept("Bank widget not found"); return; }
        ItemContainer bank = client.getItemContainer(InventoryID.BANK);
        if (bank == null) { log.accept("Bank not open"); return; }
        Item[] items = bank.getItems();
        Random rng = new Random();
        int n = Math.min(items.length, desired.size());
        for (int targetSlot = 0; targetSlot < n; targetSlot++) {
            int wantId = desired.get(targetSlot).id;
            if (slotHas(items, targetSlot, wantId)) continue;
            int fromSlot = findSlot(items, wantId, targetSlot+1);
            if (fromSlot == -1) continue;
            ensureSlotVisible(client, bankContainer, fromSlot);
            ensureSlotVisible(client, bankContainer, targetSlot);
            dragItem(client, bankContainer, fromSlot, targetSlot);
            log.accept("Moved id="+wantId+" from "+fromSlot+" -> "+targetSlot);
            Item tmp = items[targetSlot]; items[targetSlot] = items[fromSlot]; items[fromSlot] = tmp;
            sleep(rng, minDelayMs, maxDelayMs);
        }
    }

    static java.util.Set<Integer> visibleBankSlots(Client client) {
        java.util.Set<Integer> set = new java.util.LinkedHashSet<>();
        ItemContainer bank = client.getItemContainer(InventoryID.BANK);
        if (bank == null || bank.getItems() == null) return set;
        for (int i = 0; i < bank.getItems().length; i++) set.add(i);
        return set;
    }

    static int activeNativeTab(Client client) {
        try { return client.getVarbitValue(net.runelite.api.Varbits.CURRENT_BANK_TAB); } catch (Throwable t) { return -1; }
    }

    static java.util.Set<Integer> slotsInNativeTab(Client client, int tabId) {
        // Best-effort: when a native tab is selected, the bank view shows only that tab's items
        return visibleBankSlots(client);
    }

    static boolean selectNativeTab(Client client, int tabIndex) {
        try {
            // Tab 0 = main (All). Others are numbered left->right as 1..n
            Widget tabs = client.getWidget(WidgetInfo.BANK_TAB_CONTAINER);
            if (tabs == null) return false;
            Widget[] ch = tabs.getDynamicChildren();
            if (ch == null || ch.length == 0) return tabIndex == 0; // assume main selected

            int clickChild = -1;
            if (tabIndex == 0) {
                clickChild = -2; // parent fallback for main tab
            } else {
                int count = 0;
                for (Widget w : ch) {
                    if (w == null) continue;
                    if (w.getWidth() > 0 && w.getHeight() > 0) {
                        count++;
                        if (count == tabIndex) { clickChild = w.getIndex(); break; }
                    }
                }
            }
            if (clickChild == -1) return false;

            if (clickChild == -2) {
                client.invokeMenuAction("", "", 1, MenuAction.WIDGET_TYPE_1.getId(), tabs.getIndex(), tabs.getId());
            } else {
                client.invokeMenuAction("", "", 1, MenuAction.WIDGET_TYPE_1.getId(), clickChild, tabs.getId());
            }

            long start = System.currentTimeMillis();
            int expected = tabIndex;
            while (System.currentTimeMillis() - start < 500) {
                int cur = activeNativeTab(client);
                if (cur == expected || (tabIndex == 0 && (cur <= 0))) break;
                try { Thread.sleep(20); } catch (InterruptedException ignored) {}
            }
            return true;
        } catch (Throwable t) { return false; }
    }

    static void sleep(Random rng, int minMs, int maxMs) { try { int span = Math.max(0, maxMs - minMs); int d = Math.max(0, minMs + (span > 0 ? rng.nextInt(span + 1) : 0)); Thread.sleep(d); } catch (InterruptedException ignored) {} }

    // Auto-scroll to reveal a slot before dragging (best-effort; safe no-op if not needed)
    private static void ensureSlotVisible(Client client, Widget container, int slot) {
        if (container == null) return;
        try {
            final int itemsPerRow = 8; // bank grid
            final int row = Math.max(0, slot / itemsPerRow);
            final int rowHeight = 36; // approx
            final int targetY = row * rowHeight;
            int cur = container.getScrollY();
            int h = container.getScrollHeight();
            int vis = container.getHeight();
            if (targetY < cur || targetY > cur + vis - rowHeight) {
                int newY = Math.min(Math.max(0, targetY - rowHeight), Math.max(0, h - vis));
                container.setScrollY(newY);
            }
        } catch (Throwable ignored) { }
    }

    private static boolean slotHas(Item[] items, int slot, int id) { return slot < items.length && items[slot] != null && items[slot].getId() == id; }
    private static int findSlot(Item[] items, int id, int start) { for (int i=start; i<items.length; i++) if (items[i]!=null && items[i].getId()==id) return i; return -1; }
    private static void dragItem(Client client, Widget container, int from, int to) { int parentId = container.getId(); client.invokeMenuAction("", "", 1, MenuAction.WIDGET_TYPE_1.getId(), from, parentId); client.invokeMenuAction("", "", 1, MenuAction.WIDGET_TYPE_2.getId(), to, parentId); }
}
