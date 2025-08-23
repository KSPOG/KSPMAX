package net.runelite.client.plugins.microbot.experiencelamp;

import com.google.inject.Provides;
import java.awt.AWTException;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;

@PluginDescriptor(
    name = PluginDescriptor.Mocrosoft + "Experience Lamps",
    description = "Rubs experience lamps and applies them to a chosen skill",
    tags = {"microbot", "experience", "lamp"},
    enabledByDefault = false
)
@Slf4j
public class ExperienceLampPlugin extends Plugin
{
    @Inject
    private ExperienceLampConfig config;

    @Inject
    private ExperienceLampScript script;

    @Provides
    ExperienceLampConfig provideConfig(ConfigManager configManager)
    {
        return configManager.getConfig(ExperienceLampConfig.class);
    }

    @Override
    protected void startUp() throws AWTException
    {
        script.run(config);
    }

    @Override
    protected void shutDown()
    {
        script.shutdown();
    }
}
