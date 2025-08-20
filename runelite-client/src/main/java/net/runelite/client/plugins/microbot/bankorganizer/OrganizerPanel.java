package net.runelite.client.plugins.microbot.bankorganizer;

import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.ui.PluginPanel;

import javax.swing.*;
import java.awt.*;

public class OrganizerPanel extends PluginPanel {
    private final OrganizerService service;

    private OrganizerPanel(OrganizerService service) {
        this.service = service;
        setLayout(new BorderLayout());
        setBackground(ColorScheme.DARK_GRAY_COLOR);

        JPanel top = new JPanel(new GridLayout(0,1,6,6));
        JButton scan = new JButton("Scan & Tag bank");
        JButton makeTabs = new JButton("Create/Update tabs");
        JButton arrange = new JButton("Arrange (virtual tags)");
        JButton arrangePhysical = new JButton("Arrange (physical)");
        JButton arrangePhysicalAll = new JButton("Arrange (physical) — All Tabs");
        JButton clear = new JButton("Clear preset tags");
        JButton combos = new JButton("Apply Built‑in Combos");
        JButton ruleCombos = new JButton("Apply Rule‑Editor Combos");
        JButton snapshot = new JButton("Snapshot → Dated JSON");
        JButton exportClip = new JButton("Export → Clipboard");
        JButton importClip = new JButton("Import ← Clipboard");
        JButton exportFile = new JButton("Export → File");
        JButton importFile = new JButton("Import ← File");
        JButton resetPresets = new JButton("Restore Default Presets");

        // Preset Pack controls
        JPanel packRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        JComboBox<BankOrganizerConfig.PresetPack> packBox = new JComboBox<>(BankOrganizerConfig.PresetPack.values());
        JButton packLoad = new JButton("Load Pack → Rules JSON");
        JButton packApply = new JButton("Apply Pack (no edit)");
        packRow.add(new JLabel("Preset Pack:"));
        packRow.add(packBox);
        packRow.add(packLoad);
        packRow.add(packApply);

        scan.addActionListener(e -> service.scanAndTag());
        makeTabs.addActionListener(e -> service.createOrUpdateTabs());
        arrange.addActionListener(e -> service.arrangeVirtual());
        arrangePhysical.addActionListener(e -> service.arrangePhysical());
        arrangePhysicalAll.addActionListener(e -> service.arrangePhysicalAllTabs());
        clear.addActionListener(e -> service.clearPresetTags());
        combos.addActionListener(e -> service.applyCombinationTags());
        ruleCombos.addActionListener(e -> service.applyRuleEditorCombos());
        snapshot.addActionListener(e -> service.snapshotToDatedFile());
        exportClip.addActionListener(e -> service.exportLayoutToClipboard());
        importClip.addActionListener(e -> service.importLayoutFromUser());
        exportFile.addActionListener(e -> service.exportLayoutToFile());
        importFile.addActionListener(e -> service.importLayoutFromFile());
        packLoad.addActionListener(e -> service.loadPresetPackToRulesJson((BankOrganizerConfig.PresetPack) packBox.getSelectedItem()));
        packApply.addActionListener(e -> service.applyPresetPack((BankOrganizerConfig.PresetPack) packBox.getSelectedItem()));
        resetPresets.addActionListener(e -> service.restoreDefaultRuleJson());

        top.add(scan); top.add(makeTabs); top.add(arrange); top.add(arrangePhysical); top.add(arrangePhysicalAll); top.add(clear);
        top.add(combos); top.add(ruleCombos); top.add(snapshot);
        top.add(packRow);
        top.add(resetPresets);
        top.add(exportClip); top.add(importClip); top.add(exportFile); top.add(importFile);

        add(top, BorderLayout.NORTH);
        JTextArea log = service.getLogArea();
        log.setEditable(false);
        add(new JScrollPane(log), BorderLayout.CENTER);
    }

    public static NavigationButton createNavButton(OrganizerService service) {
        OrganizerPanel panel = new OrganizerPanel(service);
        return NavigationButton.builder()
                .tooltip("Bank Organizer")
                .icon(OrganizerService.ICON)
                .priority(7)
                .panel(panel)
                .build();
    }
}
