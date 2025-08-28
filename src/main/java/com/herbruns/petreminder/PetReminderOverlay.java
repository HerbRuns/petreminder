package com.herbruns.petreminder;

import lombok.Getter;
import lombok.Setter;
import net.runelite.api.Client;
import net.runelite.client.game.ItemManager;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.ImageComponent;
import net.runelite.client.util.AsyncBufferedImage;

import javax.inject.Inject;
import java.awt.*;

public class PetReminderOverlay extends OverlayPanel
{
    @Getter
    private int lastKnownPetId;

    private AsyncBufferedImage petImage;
    private boolean isPetOut = false;
    private boolean isPetOffScreen = false;

    // pet off-screen flashing
    private boolean flashState = false;
    private long lastFlashTime = 0;
    private static final long FLASH_INTERVAL_MS = 500; // half second toggle

    //private final Client client;
    private final ItemManager itemManager;

    @Inject
    public PetReminderOverlay(ItemManager itemManager)
    {
        //this.client = client;
        this.itemManager = itemManager;

        petImage = itemManager.getImage(lastKnownPetId, 1, false);

        setPosition(OverlayPosition.TOP_LEFT);
        setResizable(false);
        setMovable(true);
    }

    public void setIsPetOut(boolean petOut)
    {
        this.isPetOut = petOut;

        if (petOut)
        {
            petImage = itemManager.getImage(lastKnownPetId, 1, false);
        }
        else
        {
            petImage = null;
        }
    }

    public void setLastKnownPetId(int lastKnownPetId)
    {
        this.lastKnownPetId = lastKnownPetId;
        petImage = itemManager.getImage(lastKnownPetId, 1, false);
    }

    public void setIsPetOffScreen(boolean petOffScreen)
    {
        this.isPetOffScreen = petOffScreen;
        this.flashState = false;
        this.lastFlashTime = 0;
    }

    @Override
    public Dimension render(Graphics2D graphics)
    {
        panelComponent.getChildren().clear();

        if (!isPetOut || petImage == null)
        {
            return null; // nothing to draw
        }

        // Flash background logic
        if (isPetOffScreen)
        {
            long now = System.currentTimeMillis();
            if (now - lastFlashTime > FLASH_INTERVAL_MS)
            {
                flashState = !flashState;
                lastFlashTime = now;
            }

            if (flashState)
            {
                // Fill red background behind the panel
                graphics.setColor(new Color(255, 0, 0, 128)); // translucent red
                graphics.fillRect(getBounds().x, getBounds().y, getBounds().width, getBounds().height);
            }
        }

        panelComponent.getChildren().add(new ImageComponent(petImage));
        return super.render(graphics);
    }
}
