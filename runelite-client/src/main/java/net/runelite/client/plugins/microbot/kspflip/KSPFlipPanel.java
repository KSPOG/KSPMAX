package net.runelite.client.plugins.microbot.kspflip;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import net.runelite.client.ui.PluginPanel;

public class KSPFlipPanel extends PluginPanel
{
    private final JLabel lastFlipLabel = new JLabel("Last Flip: - gp");

    public KSPFlipPanel()
    {
        setLayout(new BorderLayout());
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(0, 1));
        panel.add(lastFlipLabel);
        add(panel, BorderLayout.NORTH);
    }

    public void updateLastFlip(long profit)
    {
        lastFlipLabel.setText("Last Flip: " + profit + " gp");
    }
}
