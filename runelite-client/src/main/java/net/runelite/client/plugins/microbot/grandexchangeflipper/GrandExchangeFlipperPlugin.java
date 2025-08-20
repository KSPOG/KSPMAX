package net.runelite.client.plugins.microbot.grandexchangeflipper;

import com.google.inject.Provides;
import java.awt.AWTException;
import java.awt.image.BufferedImage;
import javax.inject.Inject;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.util.ImageUtil;

@PluginDescriptor(
        name = PluginDescriptor.Microbot + "GE Flipper",
        description = "Automatically flip items on the Grand Exchange",
        tags = {"grand", "exchange", "flip"},
        enabledByDefault = false
)
public class GrandExchangeFlipperPlugin extends Plugin
{
    @Inject
    private OverlayManager overlayManager;

    @Inject
    private GrandExchangeFlipperOverlay overlay;

    @Inject
    private GrandExchangeFlipperScript script;

    @Inject
    private GrandExchangeFlipperConfig config;

    @Inject
    private ClientToolbar clientToolbar;

    private NavigationButton navButton;

    @Inject
    private GrandExchangeFlipperPanel panel;

    @Provides
    GrandExchangeFlipperConfig provideConfig(ConfigManager configManager)
    {
        return configManager.getConfig(GrandExchangeFlipperConfig.class);
    }

    @Override
    protected void startUp() throws AWTException
    {
        overlayManager.add(overlay);
        final BufferedImage icon = ImageUtil.loadImageResource(GrandExchangeFlipperPlugin.class,
                "/net/runelite/client/plugins/grandexchange/ge_icon.png");
        navButton = NavigationButton.builder()
                .tooltip("GE Flipper")
                .icon(icon)
                .priority(8)
                .panel(panel)
                .build();
        clientToolbar.addNavigation(navButton);
        script.run(this, panel);
    }

    @Override
    protected void shutDown()
    {
        overlayManager.remove(overlay);
        clientToolbar.removeNavigation(navButton);
        script.shutdown();
    }
}
