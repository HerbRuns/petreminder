package com.herbruns.petreminder;

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
    private static final int CAT_ITEM_ID = 1555; // Regular cat icon

    private AsyncBufferedImage petImage;
    private boolean isPetOut = false;

    //private final Client client;
    private final ItemManager itemManager;

    @Inject
    public PetReminderOverlay(/*Client client, */ItemManager itemManager)
    {
        //this.client = client;
        this.itemManager = itemManager;

        petImage = itemManager.getImage(CAT_ITEM_ID, 1, false);

        setPosition(OverlayPosition.TOP_LEFT);
        setResizable(false);
        setMovable(true);
    }

    public void setIsPetOut(boolean petOut)
    {
        this.isPetOut = petOut;

        if (petOut)
        {
            // Always fetch the cat icon for display if we do not have it already
            if (petImage == null) {
                petImage = itemManager.getImage(CAT_ITEM_ID, 1, false);
            }
        }
        else
        {
            petImage = null;
        }
    }

    @Override
    public Dimension render(Graphics2D graphics)
    {
        panelComponent.getChildren().clear();

        if (!isPetOut || petImage == null)
        {
            return null; // nothing to draw
        }

        panelComponent.getChildren().add(new ImageComponent(petImage));
        return super.render(graphics);
    }
}
