package net.runelite.client.plugins.microbot.bankorganizer;

import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;

import javax.inject.Inject;

@Slf4j
@PluginDescriptor(
        name = "Bank Organizer (Microbot)",
        description = "Organize your bank via tags + combos + rule editor + preset packs",
        tags = {"bank", "organize", "tags", "combos", "microbot"}
)
public class BankOrganizerPlugin extends Plugin {
    @Inject private Client client;
    @Inject private ClientToolbar clientToolbar;
    @Inject private ConfigManager configManager;
    @Inject private OrganizerService service;
    @Inject private BankOrganizerConfig config;

    private NavigationButton navBtn;

    @Provides BankOrganizerConfig provideConfig(ConfigManager cm) { return cm.getConfig(BankOrganizerConfig.class); }

    @Override protected void startUp() {
        service.init(client, config, configManager);
        navBtn = OrganizerPanel.createNavButton(service);
        clientToolbar.addNavigation(navBtn);
        log.info("Bank Organizer started");
    }
    @Override protected void shutDown() {
        if (navBtn != null) clientToolbar.removeNavigation(navBtn);
        service.reset();
        log.info("Bank Organizer stopped");
    }
}
