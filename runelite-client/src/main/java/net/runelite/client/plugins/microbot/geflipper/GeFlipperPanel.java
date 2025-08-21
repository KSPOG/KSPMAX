package net.runelite.client.plugins.microbot.geflipper;

import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.PluginPanel;

import javax.inject.Singleton;
import javax.swing.*;
import java.awt.*;

@Singleton
public class GeFlipperPanel extends PluginPanel {
    private final JLabel latestFlipLabel = new JLabel("Latest flip: none");
    private final JLabel profitLabel = new JLabel("Profit: 0 gp");

    public void init() {
        setLayout(new BorderLayout());
        setBackground(ColorScheme.DARK_GRAY_COLOR);
        JPanel info = new JPanel();
        info.setLayout(new GridLayout(0,1));
        info.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        info.add(latestFlipLabel);
        info.add(profitLabel);
        add(info, BorderLayout.NORTH);
    }

    public void updateLatestFlip(String text) {
        SwingUtilities.invokeLater(() -> latestFlipLabel.setText("Latest flip: " + text));
    }

    public void updateProfit(long profit) {
        SwingUtilities.invokeLater(() -> profitLabel.setText("Profit: " + profit + " gp"));
    }
}
