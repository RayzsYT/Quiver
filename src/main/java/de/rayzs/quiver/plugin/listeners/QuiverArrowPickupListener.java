package de.rayzs.quiver.plugin.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerPickupArrowEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import de.rayzs.quiver.utils.Quiver;

public class QuiverArrowPickupListener implements Listener {

    @EventHandler
    public void onPlayerPickupArrow(PlayerPickupArrowEvent event) {
        if (event.isCancelled())
            return;

        Player player = event.getPlayer();
        Quiver.QuiverInventorySelection inventorySelection = Quiver.getClosestQuiver(player);

        if (inventorySelection == null || event.getItem() == null)
            return;

        Quiver.QuiverItem quiver = inventorySelection.getQuiver();
        ItemStack arrow = event.getItem().getItemStack();
        int amount = arrow.getAmount();

        quiver.add(arrow);
        quiver.update(player.getInventory(), inventorySelection.getSlot());

        event.setCancelled(true);
        event.getItem().remove();
    }
    
}
