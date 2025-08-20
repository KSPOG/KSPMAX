package net.runelite.client.plugins.microbot.bankorganizer;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.game.ItemManager;
import net.runelite.client.util.Text;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Singleton
public class OrganizerService {
    @Inject private ItemManager itemManager;

    private Client client;
    private BankOrganizerConfig config;
    private ConfigManager configManager;

    @Getter private final JTextArea logArea = new JTextArea();
    static final javax.swing.Icon ICON = new javax.swing.ImageIcon(new BufferedImage(16,16,BufferedImage.TYPE_INT_ARGB));

    private static final String BANK_TAGS_GROUP = "banktags";

    public void init(Client client, BankOrganizerConfig config, ConfigManager cfg) { this.client = client; this.config = config; this.configManager = cfg; logArea.setText(""); }
    public void reset() { logArea.setText(""); }

    // ==== Public actions ====
    public void scanAndTag() {
        if (!bankOpen()) { logln("Open your bank first."); return; }
        logln("Scanning bank items…");
        Map<Category, List<ItemStack>> base = categorizeCurrentBank();
        if (config.dryRun()) { base.forEach((c, list) -> logln(c.displayName()+": "+list.size()+" items")); return; }
        writeBankTags(base);
        logln("Tagging complete.");
    }

    public void createOrUpdateTabs() {
        if (config.dryRun()) { logln("[dry-run] Would ensure tab names via tags."); return; }
        ensureTagSeeds();
        logln("Tabs ensured. Use Bank Tags/Bank Tag Tabs to show them.");
    }

    public void arrangeVirtual() {
        if (!config.autoArrange()) { logln("Auto arrange is disabled in config."); return; }
        if (!bankOpen()) { logln("Open your bank first."); return; }
        Map<Category, List<ItemStack>> map = categorizeCurrentBank();
        List<ItemStack> order = desiredOrder(map);
        if (config.dryRun()) { logln("[dry-run] Would reorder (virtual) " + order.size() + " slots."); return; }
        MicrobotBankMover.reorderBank(client, order, this::logln, config.dragMinMs(), config.dragMaxMs());
    }

    public void arrangePhysical() {
        if (!bankOpen()) { logln("Open your bank first."); return; }
        Map<Category, List<ItemStack>> map = categorizeCurrentBank();
        List<ItemStack> fullOrder = desiredOrder(map);
        List<ItemStack> target;
        switch (config.arrangeScope()) {
            case WHOLE_BANK:
                target = fullOrder; break;
            case NATIVE_TABS:
                target = filterToNativeTab(fullOrder); break;
            case CURRENT_VIEW_ONLY:
            default:
                target = filterToCurrentView(fullOrder); break;
        }
        if (target.isEmpty()) { logln("Nothing to arrange for the selected scope."); return; }
        if (config.dryRun()) { logln("[dry-run] Would physically reorder " + target.size() + " slots ("+config.arrangeScope()+")."); return; }
        MicrobotBankMover.reorderBank(client, target, this::logln, config.dragMinMs(), config.dragMaxMs());
    }

    public void arrangePhysicalAllTabs() {
        if (!bankOpen()) { logln("Open your bank first."); return; }
        java.util.List<Integer> tabs = parseTabList(config.tabsToArrange(), config.includeMainTab());
        if (tabs.isEmpty()) { logln("No tabs selected to arrange."); return; }
        java.util.Random rng = new java.util.Random();
        int processed = 0;
        for (int tab : tabs) {
            if (!MicrobotBankMover.selectNativeTab(client, tab)) { logln("Could not select tab " + tab + ". Skipping."); continue; }
            try { Thread.sleep(config.tabSwitchMinMs() + rng.nextInt(Math.max(1, config.tabSwitchMaxMs() - config.tabSwitchMinMs() + 1))); } catch (InterruptedException ignored) {}
            Map<Category, List<ItemStack>> map = categorizeCurrentBank();
            List<ItemStack> order = desiredOrder(map);
            List<ItemStack> target = filterToNativeTab(order);
            if (target.isEmpty()) { logln("Tab " + tab + ": no items to arrange."); continue; }
            processed++;
            if (config.dryRun()) { logln("[dry-run] Tab " + tab + ": would reorder " + target.size() + " slots."); continue; }
            MicrobotBankMover.reorderBank(client, target, s -> logln("[Tab " + tab + "] " + s), config.dragMinMs(), config.dragMaxMs());
        }
        logln("Multi-tab arrange complete. Tabs processed: " + processed + ".");
    }

    public void clearPresetTags() {
        if (config.dryRun()) { logln("[dry-run] Would clear preset tags"); return; }
        for (Category c : CategoryRules.DEFAULT_ORDER) {
            configManager.unsetConfiguration(BANK_TAGS_GROUP, tagName(c));
        }
        logln("Cleared preset tags.");
    }

    // ==== Internals ====
    private boolean bankOpen() { return client != null && client.getItemContainer(InventoryID.BANK) != null; }

    private Map<String,String> readAllPresetTags() {
        Map<String,String> out = new LinkedHashMap<>();
        for (Category c : CategoryRules.DEFAULT_ORDER) {
            String key = tagName(c);
            String val = configManager.getConfiguration(BANK_TAGS_GROUP, key);
            if (val != null) out.put(key, val);
        }
        return out;
    }

    private void writeTagsMap(Map<String,String> map) {
        for (Map.Entry<String,String> e : map.entrySet())
            configManager.setConfiguration(BANK_TAGS_GROUP, e.getKey(), e.getValue());
    }

    private List<ItemStack> desiredOrder(Map<Category, List<ItemStack>> map) {
        List<ItemStack> order = new ArrayList<>();
        for (Category c : CategoryRules.DEFAULT_ORDER) order.addAll(sortWithin(map.getOrDefault(c, Collections.emptyList())));
        return order;
    }

    private List<ItemStack> filterToCurrentView(List<ItemStack> order) {
        Set<Integer> visibleSlots = MicrobotBankMover.visibleBankSlots(client);
        List<ItemStack> out = new ArrayList<>();
        for (ItemStack s : order) if (visibleSlots.contains(s.slot)) out.add(s);
        return out;
    }

    private List<ItemStack> filterToNativeTab(List<ItemStack> order) {
        int activeTab = MicrobotBankMover.activeNativeTab(client);
        if (activeTab <= 0) return filterToCurrentView(order);
        Set<Integer> tabSlots = MicrobotBankMover.slotsInNativeTab(client, activeTab);
        List<ItemStack> out = new ArrayList<>();
        for (ItemStack s : order) if (tabSlots.contains(s.slot)) out.add(s);
        return out;
    }

    private List<ItemStack> sortWithin(List<ItemStack> items) {
        BankOrganizerConfig.ArrangeOrder mode = config.arrangeOrder();
        switch (mode) {
            case GE_PRICE_DESC:
                return items.stream().sorted((a,b)->Integer.compare(gePrice(b.id), gePrice(a.id))).collect(Collectors.toList());
            case HA_VALUE_DESC:
                return items.stream().sorted((a,b)->Integer.compare(haValue(b.id), haValue(a.id))).collect(Collectors.toList());
            case WEAPON_ARMOUR_FIRST:
                return items.stream().sorted((a,b)->{
                    int aw = scoreWeaponArmour(b.id) - scoreWeaponArmour(a.id);
                    if (aw != 0) return aw;
                    return Text.standardize(itemName(a.id)).compareTo(Text.standardize(itemName(b.id)));
                }).collect(Collectors.toList());
            case ALPHA:
            default:
                return items.stream().sorted(java.util.Comparator.comparing(s->Text.standardize(itemName(s.id)))).collect(Collectors.toList());
        }
    }

    private int scoreWeaponArmour(int id) {
        String n = itemManager.getItemComposition(id).getName().toLowerCase();
        if (n.contains("sword")||n.contains("bow")||n.contains("staff")||n.contains("2h")||n.contains("dagger")||n.contains("mace")||n.contains("scimitar")) return 3;
        if (n.contains("helm")||n.contains("body")||n.contains("plate")||n.contains("legs")||n.contains("shield")||n.contains("boots")||n.contains("gloves")||n.contains("cape")) return 2;
        return 0;
    }

    private String itemName(int id) { return itemManager.getItemComposition(id).getName(); }
    private int gePrice(int id) { try { return itemManager.getItemPrice(id); } catch (Exception e) { return 0; } }
    private int haValue(int id) { return itemManager.getItemComposition(id).getHaPrice(); }

    private void writeBankTags(Map<Category, List<ItemStack>> map) {
        for (Map.Entry<Category, List<ItemStack>> e : map.entrySet())
            saveTag(tagName(e.getKey()), toIdSet(e.getValue()));
    }
    private Set<Integer> toIdSet(List<ItemStack> items) { Set<Integer> ids = new LinkedHashSet<>(); for (ItemStack s: items) ids.add(s.id); return ids; }

    private String tagName(Category c) {
        String base = c.displayName();
        if (config.decorate()) base = c.emoji() + " " + base;
        if (config.prefixTabs()) base = String.format("%02d_", c.order()) + base;
        return base;
    }

    void saveTag(String tag, Set<Integer> ids) {
        String csv = ids.stream().map(String::valueOf).collect(Collectors.joining(","));
        configManager.setConfiguration(BANK_TAGS_GROUP, tag, csv);
        logln("Tag "+tag+" <= "+ids.size()+" ids");
    }

    private void ensureTagSeeds() {
        for (Category c : CategoryRules.DEFAULT_ORDER)
            if (configManager.getConfiguration(BANK_TAGS_GROUP, tagName(c)) == null)
                configManager.setConfiguration(BANK_TAGS_GROUP, tagName(c), "");
    }

    private Map<Category, List<ItemStack>> categorizeCurrentBank() {
        ItemContainer bank = client.getItemContainer(InventoryID.BANK);
        Map<Category, List<ItemStack>> map = new EnumMap<>(Category.class);
        if (bank==null) return map;
        Item[] items = bank.getItems();
        if (items==null) return map;
        for (int slot=0; slot<items.length; slot++) {
            Item it = items[slot];
            if (it==null||it.getId()<=0) continue;
            if (it.getId()==ItemID.BANK_FILLER && !config.includePlaceholders()) continue;
            ItemComposition comp = itemManager.getItemComposition(it.getId());
            Category c = classifyWithExtras(comp);
            map.computeIfAbsent(c, __->new ArrayList<>()).add(new ItemStack(it.getId(), slot, it.getQuantity()));
        }
        return map;
    }

    private Category classifyWithExtras(ItemComposition ic) {
        String n = ic.getName().toLowerCase();
        if (extraHit(n, config.extraKeywordsRaids())) return Category.RAIDS;
        if (extraHit(n, config.extraKeywordsSlayer())) return Category.SLAYER;
        if (extraHit(n, config.extraKeywordsBarrows())) return Category.BARROWS;
        return CategoryRules.classify(ic);
    }
    private boolean extraHit(String name, String csv) {
        if (csv==null||csv.trim().isEmpty()) return false;
        for (String t: csv.split(",")) { String s=t.trim().toLowerCase(); if(!s.isEmpty() && name.contains(s)) return true; }
        return false;
    }

    // Built-in combos & Rule-editor combos
    public void applyCombinationTags() {
        if (!bankOpen()) { logln("Open your bank first."); return; }
        Map<Category, List<ItemStack>> base = categorizeCurrentBank();
        Map<String, Set<Integer>> combos = ComboRules.buildBuiltInCombos(base, itemManager, config);
        if (config.dryRun()) { logln("[dry-run] Would write " + combos.size() + " combo tags"); return; }
        for (Map.Entry<String, Set<Integer>> e : combos.entrySet()) saveTag(e.getKey(), e.getValue());
        logln("Wrote " + combos.size() + " built‑in combo tags.");
    }

    public void applyRuleEditorCombos() {
        if (!bankOpen()) { logln("Open your bank first."); return; }
        Map<Category, List<ItemStack>> base = categorizeCurrentBank();
        Map<String, Set<Integer>> combos = ComboRules.buildRuleEditorCombos(base, itemManager, config.ruleEditorJson());
        if (combos.isEmpty()) { logln("No rule‑editor combos found (check your JSON)."); return; }
        if (config.dryRun()) { logln("[dry-run] Would write " + combos.size() + " rule‑editor combo tags"); return; }
        for (Map.Entry<String, Set<Integer>> e : combos.entrySet()) saveTag(e.getKey(), e.getValue());
        logln("Wrote " + combos.size() + " rule‑editor combo tags.");
    }

    // Preset Packs
    public void loadPresetPackToRulesJson(BankOrganizerConfig.PresetPack pack) {
        String json = PresetPacks.forPack(pack);
        configManager.setConfiguration("bankorganizer", "ruleEditorJson", json);
        logln("Loaded pack '"+pack+"' into Rules JSON (edit in config if you like).");
    }

    public void applyPresetPack(BankOrganizerConfig.PresetPack pack) {
        if (!bankOpen()) { logln("Open your bank first."); return; }
        String json = PresetPacks.forPack(pack);
        Map<Category, List<ItemStack>> base = categorizeCurrentBank();
        Map<String, Set<Integer>> combos = ComboRules.buildRuleEditorCombos(base, itemManager, json);
        if (combos.isEmpty()) { logln("Pack has no rules."); return; }
        if (config.dryRun()) { logln("[dry-run] Would write " + combos.size() + " pack tags"); return; }
        for (Map.Entry<String, Set<Integer>> e : combos.entrySet()) saveTag(e.getKey(), e.getValue());
        logln("Applied pack '"+pack+"' → " + combos.size() + " tags written.");
    }

    public void restoreDefaultRuleJson() {
        configManager.setConfiguration("bankorganizer", "ruleEditorJson", PresetPacks.DEFAULT_RULES_JSON);
        logln("Restored default Rules JSON presets.");
    }

    // Import/Export/Snapshot
    public void exportLayoutToClipboard() { try { copyToClipboard(toJson(readAllPresetTags())); logln("Exported layout to clipboard."); } catch (Exception ex) { logln("Export failed: "+ex.getMessage()); } }
    public void importLayoutFromUser() { try { String input = JOptionPane.showInputDialog(null, "Paste layout JSON:"); if (input==null||input.trim().isEmpty()) { logln("Import cancelled."); return; } writeTagsMap(fromJson(input.trim())); logln("Imported layout."); } catch (Exception ex) { logln("Import failed: "+ex.getMessage()); } }
    public void exportLayoutToFile() { try { String json = toJson(readAllPresetTags()); writeString(new File(config.layoutFilePath()), json); logln("Exported layout to file: "+new File(config.layoutFilePath()).getAbsolutePath()); } catch (Exception ex) { logln("Export failed: "+ex.getMessage()); } }
    public void importLayoutFromFile() { try { File f=new File(config.layoutFilePath()); if(!f.exists()){ logln("Import file not found: "+f.getAbsolutePath()); return;} writeTagsMap(fromJson(readString(f))); logln("Imported layout from file."); } catch (Exception ex) { logln("Import failed: "+ex.getMessage()); } }
    public void snapshotToDatedFile() { try { Map<String,String> map = readAllPresetTags(); String date = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()); File f = new File("bank_layout_"+date+".json"); writeString(f, toJson(map)); logln("Snapshot saved: "+f.getAbsolutePath()); } catch (Exception ex) { logln("Snapshot failed: "+ex.getMessage()); } }

    private void logln(String s) { logArea.append(s+"\n"); log.info(s); }

    static class ItemStack { final int id; final int slot; final int qty; ItemStack(int id,int slot,int qty){this.id=id;this.slot=slot;this.qty=qty;} }

    // Minimal JSON helpers (Map<String,String>)
    private static String toJson(Map<String,String> map) {
        StringBuilder sb=new StringBuilder();
        sb.append("{\n");
        boolean first=true;
        for (Map.Entry<String,String> e: map.entrySet()) {
            if(!first) sb.append(",\n");
            first=false;
            sb.append("  ").append(quote(e.getKey())).append(": ").append(quote(e.getValue()));
        }
        sb.append("\n}");
        return sb.toString();
    }
    private static Map<String,String> fromJson(String json) {
        Map<String,String> out=new LinkedHashMap<>();
        String inner=json.trim();
        if(inner.startsWith("{")&&inner.endsWith("}")) inner=inner.substring(1, inner.length()-1);
        if(inner.trim().isEmpty()) return out;
        java.util.List<String> pairs = new java.util.ArrayList<>();
        boolean inStr = false;
        StringBuilder cur = new StringBuilder();
        for (int i = 0; i < inner.length(); i++) {
            char c = inner.charAt(i);
            if (c == '"') { inStr = !inStr; }
            if (c == ',' && !inStr) {
                pairs.add(cur.toString());
                cur.setLength(0);
            } else {
                cur.append(c);
            }
        }
        if (cur.length() > 0) pairs.add(cur.toString());
        for (String pair : pairs) {
            String p = pair.trim();
            if (p.isEmpty()) continue;
            int idx = p.indexOf(':');
            if (idx < 0) continue;
            String k = unquote(p.substring(0, idx).trim());
            String v = unquote(p.substring(idx + 1).trim());
            out.put(k, v);
        }
        return out;
    }
    private static String quote(String s){ return "\""+s.replace("\\","\\\\").replace("\"","\\\"")+"\""; }
    private static String unquote(String s){ s=s.trim(); if(s.startsWith("\"")&&s.endsWith("\"")) s=s.substring(1,s.length()-1); return s.replace("\\\"","\"").replace("\\\\","\\"); }

    private static void writeString(File f, String s) throws IOException { try (FileWriter fw=new FileWriter(f)) { fw.write(s);} }
    private static String readString(File f) throws IOException { StringBuilder sb=new StringBuilder(); try(BufferedReader br=new BufferedReader(new FileReader(f))){ String line; while((line=br.readLine())!=null) sb.append(line).append('\n'); } return sb.toString(); }
    private static void copyToClipboard(String text) { try { java.awt.Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new java.awt.datatransfer.StringSelection(text), null);} catch(Exception ignored){} }

    private java.util.List<Integer> parseTabList(String spec, boolean includeMain) {
        java.util.LinkedHashSet<Integer> set = new java.util.LinkedHashSet<>();
        if (includeMain) set.add(0);
        if (spec == null || spec.trim().isEmpty()) return new java.util.ArrayList<>(set);
        for (String part : spec.split(",")) {
            String p = part.trim(); if (p.isEmpty()) continue;
            if (p.contains("-")) {
                String[] ab = p.split("-"); if (ab.length==2) {
                    try { int a = Integer.parseInt(ab[0].trim()); int b = Integer.parseInt(ab[1].trim()); if (a>b){int t=a;a=b;b=t;} for (int i=a;i<=b;i++) set.add(i);} catch (NumberFormatException ignored) {}
                }
            } else {
                try { set.add(Integer.parseInt(p)); } catch (NumberFormatException ignored) {}
            }
        }
        return new java.util.ArrayList<>(set);
    }
}
