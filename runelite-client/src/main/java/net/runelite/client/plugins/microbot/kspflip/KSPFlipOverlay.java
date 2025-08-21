package net.runelite.client.plugins.microbot.kspflip;

import java.awt.Dimension;
import java.awt.Graphics2D;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.components.LineComponent;

public class KSPFlipOverlay extends OverlayPanel
{
    private String status = "Idle";
    private long profit = 0;
    private String runtime = "00:00:00";

    public void updateOverlay(long profit, String status, String runtime)
    {
        this.profit = profit;
        this.status = status;
        this.runtime = runtime;
    }

    @Override
    public Dimension render(Graphics2D graphics)
    {
        panelComponent.getChildren().clear();
        panelComponent.getChildren().add(LineComponent.builder().left("Plugin: KSP Flipper v1.0").build());
        panelComponent.getChildren().add(LineComponent.builder().left("Profit: " + profit).build());
        panelComponent.getChildren().add(LineComponent.builder().left("Status: " + status).build());
        panelComponent.getChildren().add(LineComponent.builder().left("Run Time: " + runtime).build());
        return super.render(graphics);
    }
}
