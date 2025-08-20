package net.runelite.client.plugins.microbot.grandexchangeflipper;

import javax.swing.JLabel;
import javax.swing.JPanel;
import net.runelite.client.ui.PluginPanel;

public class GrandExchangeFlipperPanel extends PluginPanel
{
    private final JLabel latest = new JLabel("Latest: -");
    private final JLabel profit = new JLabel("Profit: 0");

    public GrandExchangeFlipperPanel()
    {
        super(false);
        JPanel container = new JPanel();
        container.add(latest);
        container.add(profit);
        add(container);
    }

    public void updateLatestFlip(String item, long flipProfit, long totalProfit)
    {
        latest.setText("Latest: " + item + " (" + flipProfit + ")");
        profit.setText("Profit: " + totalProfit);
    }
}
