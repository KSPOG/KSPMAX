package net.runelite.client.plugins.microbot.geflipper;

import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.breakhandler.BreakHandlerScript;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.ui.overlay.components.TitleComponent;

import javax.inject.Inject;
import java.awt.*;

public class GeFlipperOverlay extends OverlayPanel {
    private final GeFlipperScript script;

    @Inject
    GeFlipperOverlay(GeFlipperPlugin plugin, GeFlipperScript script) {
        super(plugin);
        this.script = script;
        setPosition(OverlayPosition.TOP_LEFT);
        setNaughty();
    }

    @Override
    public Dimension render(Graphics2D graphics) {
        panelComponent.getChildren().clear();
        panelComponent.setPreferredSize(new Dimension(200, 120));
        panelComponent.getChildren().add(TitleComponent.builder()
                .text("GE Flipper")
                .color(Color.ORANGE)
                .build());
        panelComponent.getChildren().add(LineComponent.builder().build());
        panelComponent.getChildren().add(LineComponent.builder()
                .left("Plugin Name:")
                .right("GE Flipper")
                .build());
        panelComponent.getChildren().add(LineComponent.builder()
                .left("Plugin Version:")
                .right(String.valueOf(GeFlipperScript.VERSION))
                .build());
        panelComponent.getChildren().add(LineComponent.builder()
                .left("Profit:")
                .right(script.getProfit() + " gp")
                .build());
        panelComponent.getChildren().add(LineComponent.builder()
                .left("Status:")
                .right(Microbot.status)
                .build());
        panelComponent.getChildren().add(LineComponent.builder()
                .left("Run Time:")
                .right(BreakHandlerScript.formatDuration(script.getRunTime()))
                .build());
        return super.render(graphics);
    }
}
