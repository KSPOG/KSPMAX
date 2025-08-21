package net.runelite.client.plugins.microbot.geflipper;

import com.google.inject.Provides;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.util.ImageUtil;

import javax.inject.Inject;
import java.awt.AWTException;
import java.awt.image.BufferedImage;

@PluginDescriptor(
        name = PluginDescriptor.Default + "GE Flipper",
        description = "Grand Exchange flipper",
        tags = {"grand", "exchange", "flip", "microbot"}
)
public class GeFlipperPlugin extends Plugin {
    @Inject
    private GeFlipperConfig config;
    @Provides
    GeFlipperConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(GeFlipperConfig.class);
    }

    @Inject
    private OverlayManager overlayManager;
    @Inject
    private GeFlipperOverlay overlay;
    @Inject
    private GeFlipperScript script;
    @Inject
    private ClientToolbar clientToolbar;
    @Inject
    private GeFlipperPanel panel;

    private NavigationButton navButton;

    @Override
    protected void startUp() throws AWTException {
        panel.init();
        overlayManager.add(overlay);
        BufferedImage icon = ImageUtil.loadImageResource(getClass(), "flip.png");
        navButton = NavigationButton.builder()
                .tooltip("GE Flipper")
                .icon(icon)
                .priority(5)
                .panel(panel)
                .build();
        clientToolbar.addNavigation(navButton);
        script.run(config, panel);
    }

    @Override
    protected void shutDown() {
        script.shutdown();
        overlayManager.remove(overlay);
        if (navButton != null) {
            clientToolbar.removeNavigation(navButton);
        }
    }
}
