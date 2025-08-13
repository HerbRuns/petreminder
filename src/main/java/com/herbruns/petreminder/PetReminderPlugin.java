package com.herbruns.petreminder;

import com.google.inject.Provides;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.ActorDeath;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;

@Slf4j
@PluginDescriptor(
        name = "Pet Reminder",
        description = "Shows an overlay when you have a pet out",
        tags = {"pet", "reminder", "overlay"}
)
public class PetReminderPlugin extends Plugin
{
    @Inject
    private Client client;

    @Inject
    private PetReminderConfig config;

    @Inject
    private OverlayManager overlayManager;

    @Inject
    private PetReminderOverlay overlay;

    private boolean isPetOut = false;

    @Override
    protected void startUp() throws Exception
    {
        overlayManager.add(overlay);
    }

    @Override
    protected void shutDown() throws Exception
    {
        overlayManager.remove(overlay);
    }

    @Subscribe
    public void onGameTick(GameTick event)
    {
        NPC follower = client.getFollower();
        boolean hasPet = (follower != null);

        if (hasPet != isPetOut)
        {
            isPetOut = hasPet;
            overlay.setIsPetOut(isPetOut);
        }
    }

    @Subscribe
    public void onActorDeath(ActorDeath actorDeath) {
        Actor actor = actorDeath.getActor();

        if (!(actor instanceof Player)) {
            return;
        }

        Player player = (Player)actor;
        if (!player.equals(client.getLocalPlayer())) {
            return;
        }

        if (config.deathMessageEnabled())
        {
            String chatMsg = "Noooo, you forgot about your pet :(";
            client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", chatMsg, null);
        }
    }

    @Provides
    PetReminderConfig provideConfig(ConfigManager configManager)
    {
        return configManager.getConfig(PetReminderConfig.class);
    }
}
