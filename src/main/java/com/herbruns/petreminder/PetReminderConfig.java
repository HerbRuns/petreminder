package com.herbruns.petreminder;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("petreminder")
public interface PetReminderConfig extends Config
{
    @ConfigItem(
            keyName = "deathMessageEnabled",
            name = "Remind of pet loss on death",
            description = "If enabled, sends a chat message when you die",
            position = 1
    )
    default boolean deathMessageEnabled()
    {
        return false;
    }
}
