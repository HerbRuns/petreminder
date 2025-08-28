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

    private final int DEFAULT_ICON_ID = 1555; // cat
    private AsyncBufferedImage petImage;
    private boolean isPetOut = false;
    private boolean isPetOffScreen = false;

    // pet off-screen flashing
    private boolean flashState = false;
    private long lastFlashTime = 0;
    private final Color defaultColor = new Color(0,0,0,0);

    private final PetReminderConfig config;
    private final ItemManager itemManager;


    @Inject
    public PetReminderOverlay(ItemManager itemManager, PetReminderConfig config)
    {
        //this.client = client;
        this.itemManager = itemManager;
        this.config = config;

        if (!config.showCorrectPetIcon()) {
            lastKnownPetId = DEFAULT_ICON_ID;
        }

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
        // possible a wasted set of calls
        if (!config.showCorrectPetIcon()) {
            lastKnownPetId = DEFAULT_ICON_ID;
        }

        this.lastKnownPetId = lastKnownPetId;
        petImage = itemManager.getImage(lastKnownPetId, 1, false);
    }

    public void setIsPetOffScreen(boolean petOffScreen)
    {
        this.isPetOffScreen = petOffScreen;
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
        if (isPetOffScreen && config.flashIconWhenOffScreen())
        {
            long now = System.currentTimeMillis();
            if (now - lastFlashTime > config.iconFlashSpeed())
            {
                flashState = !flashState;
                lastFlashTime = now;
            }

            if (flashState)
            {
                // Fill red background behind the panel
                Color flashColor = config.iconFlashColor();
                panelComponent.setBackgroundColor(flashColor);
            }
            else
            {
                panelComponent.setBackgroundColor(defaultColor);
            }
        }
        else
        {
            panelComponent.setBackgroundColor(defaultColor);
        }

        panelComponent.getChildren().add(new ImageComponent(petImage));
        return super.render(graphics);
    }
}
