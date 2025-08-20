package net.runelite.client.plugins.microbot.grandexchangeflipper;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.time.Duration;
import javax.inject.Inject;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.LineComponent;

public class GrandExchangeFlipperOverlay extends OverlayPanel
{
    private final GrandExchangeFlipperScript script;

    @Inject
    public GrandExchangeFlipperOverlay(GrandExchangeFlipperScript script)
    {
        this.script = script;
        setPosition(OverlayPosition.TOP_LEFT);
    }

    @Override
    public Dimension render(Graphics2D graphics)
    {
        panelComponent.getChildren().clear();
        panelComponent.getChildren().add(LineComponent.builder()
                .left("Plugin Name:")
                .right("Grand Exchange Flipper")
                .build());
        panelComponent.getChildren().add(LineComponent.builder()
                .left("Plugin Version:")
                .right(String.valueOf(GrandExchangeFlipperScript.VERSION))
                .build());
        panelComponent.getChildren().add(LineComponent.builder()
                .left("Profit:")
                .right(String.valueOf(script.getProfit()))
                .build());
        panelComponent.getChildren().add(LineComponent.builder()
                .left("Run Time:")
                .right(format(script.getRuntime()))
                .build());
        return super.render(graphics);
    }

    private String format(Duration d)
    {
        long s = d.getSeconds();
        return String.format("%02d:%02d:%02d", s / 3600, (s % 3600) / 60, s % 60);
    }
}
