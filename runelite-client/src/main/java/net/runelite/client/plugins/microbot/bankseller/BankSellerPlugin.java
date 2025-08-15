package net.runelite.client.plugins.microbot.bankseller;

import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;

import javax.inject.Inject;
import java.awt.*;

@PluginDescriptor(
        name = PluginDescriptor.Default + "Bank Seller",
        description = "Withdraws tradeable items from the bank and sells them at the Grand Exchange",
        tags = {"bank", "sell", "ge"},
        enabledByDefault = false
)
@Slf4j
public class BankSellerPlugin extends Plugin {

    @Inject
    private BankSellerConfig config;

    @Provides
    BankSellerConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(BankSellerConfig.class);
    }

    @Inject
    private OverlayManager overlayManager;

    @Inject
    private BankSellerOverlay overlay;

    @Inject
    private BankSellerScript script;

    @Override
    protected void startUp() throws AWTException {
        if (overlayManager != null) {
            overlayManager.add(overlay);
        }
        script.run(config);
    }

    @Override
    protected void shutDown() {
        script.shutdown();
        overlayManager.remove(overlay);
    }

}

