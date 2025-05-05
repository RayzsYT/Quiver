package de.rayzs.quiver.plugin.listeners;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import de.rayzs.quiver.utils.Quiver;

public class QuiverInteractionListener implements Listener {
    
    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {

        if (! (event.getPlayer() instanceof Player player))
            return;

        Quiver.QuiverItem openQuiver = Quiver.getOpenQuiver(player);
        if (openQuiver == null)
            return;

        Inventory quiverInventory = openQuiver.getInventory();
        if (quiverInventory == null)
            return;

        if (event.getInventory() != quiverInventory)
            return;
        
        Quiver.removeOpenQuiver(player);

        int slot = -1;
        Inventory playerInventory = player.getInventory();
        for (int i = 0; i < playerInventory.getSize(); i++) {
            slot = i;
            if (openQuiver.compare(playerInventory.getItem(i))) {
                break;
            }
        }

        if (slot == -1)
            return;

        Location location = player.getLocation();
        for (int i = 0; i < quiverInventory.getSize(); i++) {
            ItemStack item = quiverInventory.getItem(i);
            if (item != null && !item.getType().name().contains("ARROW")) {
                quiverInventory.setItem(i, new ItemStack(Material.AIR));

                if (location != null)
                    player.getWorld().dropItem(location, item);
            }
        }

        openQuiver.update(quiverInventory, slot);
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        if (!event.getAction().name().contains("RIGHT"))
            return;

        if (item == null)
            return;

        Quiver.QuiverItem quiver = Quiver.getQuiver(item);
        if (quiver == null)
            return;

        quiver.openInventory(player);
    }

    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        
        Quiver.QuiverItem openQuiver = Quiver.getOpenQuiver(player);
        if (openQuiver == null)
            return;
        
        if (!event.getItemDrop().getItemStack().getType().name().contains("ARROW"))
            event.setCancelled(true);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (! (event.getWhoClicked() instanceof Player player))
            return;

        ItemStack clickingItem = event.getCurrentItem();
        ItemStack holdingItem = event.getCursor();

        Inventory clickedInventory = event.getClickedInventory();
        InventoryAction action = event.getAction();
        String actionName = action.name();

        Quiver.QuiverItem inventoryQuiver = Quiver.getOpenQuiver(player);
        Quiver.QuiverItem quiver = Quiver.getQuiver(clickingItem);
        Quiver.QuiverItem openQuiver = Quiver.getOpenQuiver(player);

        boolean inQuiverInventory = openQuiver != null;
        boolean holdingItemIsArrow = holdingItem != null && holdingItem.getType().name().contains("ARROW");
        boolean clickingItemIsArrow = clickingItem != null && clickingItem.getType().name().contains("ARROW");
    
        // If clicking item is a quiver
        if (quiver != null) {
            
            if (inQuiverInventory) {
                event.setCancelled(true);
                return;
            }

            if (action == InventoryAction.PICKUP_HALF) {
                player.closeInventory();
                quiver.openInventory(player);

                event.setCancelled(true);
                return;
            }

            if (holdingItem != null) {
                
                if (!holdingItemIsArrow) {
                    event.setCancelled(true);
                    return;
                }

                event.setCurrentItem(quiver.add(holdingItem));
                quiver.update(clickedInventory, event.getSlot());
                
                player.updateInventory();
                event.setCancelled(true);
                return;
            }

            System.out.println("Clicking on quiver with: " + action.name());
        }


        // If holding item is a quiver
        quiver = Quiver.getQuiver(holdingItem);
        if (quiver != null) {
            event.setCancelled(true);
            return;
        }

        // If inventory is quiver inventory
        if (openQuiver == null || openQuiver.getInventory() == null)
            return;

        Inventory openQuiverInventory = openQuiver.getInventory();
        
        if (openQuiverInventory == clickedInventory) {
            
            if (!holdingItemIsArrow || !clickingItemIsArrow) {
                event.setCancelled(true);
            }

            return;
        }


        if (actionName.contains("SWAP") || actionName.contains("HOTBAR") || actionName.contains("MOVE")) {

            if (!clickingItemIsArrow) {
                event.setCancelled(true);
            }

        }
    }

}
