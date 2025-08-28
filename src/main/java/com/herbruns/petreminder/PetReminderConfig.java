package com.herbruns.petreminder;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("petreminder")
public interface PetReminderConfig extends Config
{
    @ConfigItem(
            keyName = "defaultPetIcon",
            name = "Default Pet Icon",
            description = "Set's the default pet icon if the API fails to identify (1555 = Cat)",
            position = 1
    )
    default int defaultPetIcon()
    {
        return 1555;
    }

    @ConfigItem(
            keyName = "deathMessageEnabled",
            name = "Remind of pet loss on death",
            description = "If enabled, sends a chat message when you die",
            position = 2
    )
    default boolean deathMessageEnabled()
    {
        return false;
    }
}
