package com.herbruns.petreminder;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;

import java.awt.*;

@ConfigGroup("petreminder")
public interface PetReminderConfig extends Config
{
    @ConfigSection(
            position = 1,
            name = "Icon Options",
            description = "Selection of options for the follower icon"
    )
    String iconOptionsSection = "iconOptionsSection";

    @ConfigItem(
            keyName = "showCorrectPetIcon",
            name = "Show Correct Pet Icon",
            description = "Show's the correct pet icon based on the deployed pet",
            position = 1,
            section = iconOptionsSection
    )
    default boolean showCorrectPetIcon()
    {
        return true;
    }

    @ConfigItem(
            keyName = "flashIconWhenOffScreen",
            name = "Flash When Off-Screen",
            description = "Flash the follower icon when pet falls off-screen",
            position = 2,
            section = iconOptionsSection
    )
    default boolean flashIconWhenOffScreen()
    {
        return true;
    }

    @ConfigItem(
            keyName = "iconFlashColor",
            name = "Flashing colour",
            description = "Colour used when icon is flashing",
            position = 3,
            section = iconOptionsSection
    )
    default Color iconFlashColor()
    {
        return new Color(255, 0, 0, 128);
    }

    @ConfigItem(
            keyName = "iconFlashSpeed",
            name = "Flash speed",
            description = "The speed (milliseconds) used for icon flash",
            position = 4,
            section = iconOptionsSection
    )
    default int iconFlashSpeed() { return 350; }

    @ConfigSection(
            position = 2,
            name = "Misc Options",
            description = "Selection of misc options for the reminder"
    )
    String miscOptionsSection = "miscOptionsSection";

    @ConfigItem(
            keyName = "deathMessageEnabled",
            name = "Remind of pet loss on death",
            description = "If enabled, sends a chat message when you die",
            position = 1,
            section = miscOptionsSection
    )
    default boolean deathMessageEnabled()
    {
        return false;
    }
}
