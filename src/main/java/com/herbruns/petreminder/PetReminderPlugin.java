package com.herbruns.petreminder;

import com.google.inject.Provides;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.events.GameStateChanged;
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
    private boolean playerDidDie = false;

    private final int FOLLOWER_VARBIT = 447;

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
    public void onGameTick(GameTick tick)
    {
        // PATCH:
        // rewritten to use VarPlayer value instead to track pet disappearing but still following
        // i.e going in and out of ToA, for example
        int followerNpcId = client.getVarpValue(FOLLOWER_VARBIT);
        boolean overlayCache = isPetOut;

        // You have a pet following
        isPetOut = followerNpcId != -1;

        // if we are not the same state as last tick, change the overlay
        if (overlayCache != isPetOut) {
            overlay.setIsPetOut(isPetOut);
        }
    }

    @Subscribe
    public void onActorDeath(ActorDeath event)
    {
        Actor actor = event.getActor();
        if (!(actor instanceof Player)) {
            return;
        }

        if (event.getActor() == client.getLocalPlayer())
        {
            playerDidDie = true;
        }
    }

    @Subscribe
    public void onGameStateChanged(GameStateChanged event)
    {
        if (event.getGameState() == GameState.LOGGED_IN && playerDidDie)
        {
            int followerNpcId = client.getVarpValue(FOLLOWER_VARBIT);
            if (followerNpcId == -1 && isPetOut)
            {
                if (config.deathMessageEnabled())
                {
                    String chatMsg = "Noooo, you forgot about your pet :(";
                    client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", chatMsg, null);
                }

                // force overwrite the var and overlay as death will guarantee loss
                // next tick will then start to monitor again for us automatically
                isPetOut = false;
                overlay.setIsPetOut(isPetOut);
            }
            playerDidDie = false;
        }
    }

    @Provides
    PetReminderConfig provideConfig(ConfigManager configManager)
    {
        return configManager.getConfig(PetReminderConfig.class);
    }
}
