package com.herbruns.petreminder;

import com.google.inject.Provides;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.ActorDeath;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.ItemManager;
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
    private ItemManager itemManager;

    @Inject
    private Client client;

    @Inject
    private PetReminderConfig config;

    @Inject
    private OverlayManager overlayManager;

    @Inject
    private PetReminderOverlay overlay;

    private boolean isPetOut = false;
    private boolean isPetOffScreen = false;
    private boolean playerDidDie = false;

    private final int FOLLOWER_VARBIT = 447;

    @Override
    protected void startUp() throws Exception
    {
        overlay.setLastKnownPetId(config.defaultPetIcon());
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
        NPC followerNpc = null;

        if (followerNpcId != -1) {
            followerNpc = client.getFollower();
            if (followerNpc == null) {
                return;
            }

            String followerName = followerNpc.getName();
            if (followerName == null) {
                return;
            }

            int followerId = getItemIdByName(followerName);
            if (followerId == -1) {
                return;
            }

            if (followerId != followerNpcId) {
                client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", followerName + ": " + String.valueOf(followerId), null);
            }

            overlay.setLastKnownPetId(followerId);
            isPetOut = true;
        }
        else
        {
            isPetOut = false;
        }

        // now check if the pet is off-screen, but is following according to varbit
        if (isPetOut)
        {
            isPetOffScreen = isFollowerModelOnScreen(followerNpc);
            overlay.setIsPetOffScreen(isPetOffScreen);
        }

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

    private boolean isFollowerModelOnScreen(NPC npc)
    {
        if (npc == null || npc.getModel() == null)
        {
            return false;
        }

        Model model = npc.getModel();
        float[] verticesX = model.getVerticesX();
        float[] verticesY = model.getVerticesY();
        float[] verticesZ = model.getVerticesZ();

        LocalPoint lp = npc.getLocalLocation();
        int tileHeight = Perspective.getTileHeight(client, lp, client.getPlane());

        boolean anyOnScreen = false;

        for (int i = 0; i < verticesX.length; i++)
        {
            int vx = (int) (verticesX[i] + lp.getX());
            int vy = (int) (verticesZ[i] + lp.getY());
            int vz = (int) (verticesY[i] + tileHeight);

            Point canvasPoint = Perspective.localToCanvas(client, new LocalPoint(vx, vy), client.getPlane(), vz);
            if (canvasPoint != null)
            {
                if (canvasPoint.getX() >= 0 && canvasPoint.getX() <= client.getCanvasWidth()
                        && canvasPoint.getY() >= 0 && canvasPoint.getY() <= client.getCanvasHeight())
                {
                    anyOnScreen = true;
                    break;
                }
            }
        }

        return anyOnScreen;
    }

    public int getItemIdByName(String name)
    {
        // Normalize case for safety
        String searchName = name.toLowerCase();

        // ItemManager caches all item definitions
        for (int id = 0; id < Short.MAX_VALUE; id++)
        {
            ItemComposition comp = itemManager.getItemComposition(id);
            if (comp != null && comp.getName() != null && comp.getName().toLowerCase().equals(searchName))
            {
                return id;
            }
        }
        return -1; // not found
    }

    @Provides
    PetReminderConfig provideConfig(ConfigManager configManager)
    {
        return configManager.getConfig(PetReminderConfig.class);
    }
}
