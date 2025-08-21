package net.runelite.client.plugins.microbot.kspflip;

import com.google.inject.Provides;
import java.awt.AWTException;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.ui.overlay.OverlayManager;

@Slf4j
@PluginDescriptor(
    name = PluginDescriptor.Maxxin + "KSP Flipper",
    description = "Automated GE flipper with dynamic item scanning",
    tags = {"grand exchange", "flipping", "profit"},
    enabledByDefault = false
)
public class KSPFlipPlugin extends Plugin
{
    @Inject private Client client;
    @Inject private OverlayManager overlayManager;

    private KSPFlipOverlay overlay;
    private KSPFlipPanel panel;
    private KSPFlipScript script;

    @Inject private KSPFlipConfig config;

    @Provides
    KSPFlipConfig provideConfig(ConfigManager configManager)
    {
        return configManager.getConfig(KSPFlipConfig.class);
    }

    @Override
    protected void startUp() throws AWTException
    {
        overlay = new KSPFlipOverlay();
        panel = new KSPFlipPanel();
        overlayManager.add(overlay);

        script = new KSPFlipScript(panel, overlay);
        script.run();
    }

    @Override
    protected void shutDown()
    {
        overlayManager.remove(overlay);
        script.shutdown();
    }
}
