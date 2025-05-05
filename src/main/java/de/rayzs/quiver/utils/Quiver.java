package de.rayzs.quiver.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BundleMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.NumberConversions;

import de.rayzs.quiver.plugin.QuiverPlugin;

public class Quiver {

    private static final String DATA_SPLITTER_SYMBOl = ",", DATA_AMOUNT_SYMBOL = "=";

    private static final NamespacedKey QUIVER_DATA = new NamespacedKey(QuiverPlugin.getPlugin(), "arrows");
    
    private static final String ITEM_FILLED_DESCRIPTION_FORMAT = "%current%/%capacity% arrows";
    private static final String ITEM_FULL_DESCRIPTION_FORMAT = "Max capacity reached!";
    private static final String ITEM_EMPTY_DESCRIPTION_FORMAT = "Empty!";
    
    private static final String DEFAULT_ITEM_NAME = "Quiver";
    private static final int DEFAULT_MAX_CAPACITY = 64*5;
    
    private static final HashMap<ItemStack, QuiverItem> QUIVERS = new HashMap<>();
    private static final HashMap<Player, QuiverItem> OPEN_QUIVER_INVENTORIES = new HashMap<>();

    public static QuiverInventorySelection getClosestQuiver(Player player) {
        int slot = -1;
        
        Inventory playerInventory = player.getInventory();
        for (int i = 0; i < playerInventory.getSize(); i++) {
            ItemStack stack = playerInventory.getItem(i);
            QuiverItem quiver = getQuiver(stack);

            if (quiver != null) {
                return new QuiverInventorySelection(slot, quiver);
            }
        }

        return null;
    }

    public static QuiverItem getQuiver(ItemStack stack) {
        if (stack == null || !stack.getType().name().contains("BUNDLE"))
            return null;

        if (QUIVERS.containsKey(stack))
            return QUIVERS.get(stack);

        if (!stack.hasItemMeta())
            return null;

        ItemMeta meta = stack.getItemMeta();

        if (meta.getPersistentDataContainer() == null)
            return null;

        if (!meta.getPersistentDataContainer().has(QUIVER_DATA))
            return null;

        QuiverItem quiver = new QuiverItem(stack);
        QUIVERS.put(stack, quiver);
        return quiver;
    }

    public static QuiverItem getOpenQuiver(Player player) {
        return OPEN_QUIVER_INVENTORIES.get(player);
    }

    public static void removeOpenQuiver(Player player) {
        OPEN_QUIVER_INVENTORIES.remove(player);
    }
    
    public static ItemStack createEmptyQuiver(Material bundleMaterial) {
        ItemStack stack = new ItemStack(bundleMaterial);
        BundleMeta meta = (BundleMeta) stack.getItemMeta();

        if (meta == null) {
            System.err.println("Invalid bundle manterial!");
            return null;
        }

        meta.setHideTooltip(true);

        meta.setDisplayName(DEFAULT_ITEM_NAME);
        meta.getPersistentDataContainer().set(QUIVER_DATA, PersistentDataType.STRING, "/");
        meta.setLore(Collections.singletonList(ITEM_EMPTY_DESCRIPTION_FORMAT));
        
        stack.setItemMeta(meta);

        return stack;
    }

    private static final ItemStack[] turnStringToData(String data) {
        final Map<Material, Integer> stacks = new HashMap<>();

        if (!data.contains(DATA_SPLITTER_SYMBOl))
            return null;

        if (data.equals("/")) {
            return new ItemStack[] {};
        }

        final String[] dataSplit = data.split(DATA_SPLITTER_SYMBOl);

        for (String part : dataSplit) {
            String[] partSplit = part.split(DATA_AMOUNT_SYMBOL);

            Material material = Material.valueOf(partSplit[0]);
            int amount = NumberConversions.toInt(partSplit[1]);

            stacks.put(material, amount);
        }

        List<ItemStack> itemStacks = new ArrayList<>();
        for (Entry<Material,Integer> set : stacks.entrySet()) {
            int amount = set.getValue();

            while (amount >= 64) {
                amount -= 64;
                itemStacks.add(new ItemStack(set.getKey(), 64));
            }

            itemStacks.add(new ItemStack(set.getKey(), amount));
        }

        return itemStacks.toArray(new ItemStack[] {});
    }

    private static final String turnDataToString(ItemStack[] items) {
        final StringBuilder builder = new StringBuilder();
        final Map<String, Integer> stacks = new HashMap<>();

        for (ItemStack stack : items) {
            Material material = stack.getType();
            String materialName = material.name();

            if (!materialName.contains("ARROW"))
                continue;
            
            stacks.put(materialName, stacks.getOrDefault(materialName, 0) + 1);
        }

        final Set<Entry<String, Integer>> entries = stacks.entrySet();
        final int max = entries.size() - 1;

        int i = 0;
        for (Entry<String, Integer> set :  entries) {
            
            builder
                .append(set.getKey())
                .append(DATA_AMOUNT_SYMBOL)
                .append(set.getValue()
            );

            if (i < max) {
                builder.append(DATA_SPLITTER_SYMBOl);
            }

            i++;
        }

        return builder.toString();
    }

    
    public static class QuiverInventorySelection {

        private final int slot;
        private final QuiverItem quiver;

        public QuiverInventorySelection(int slot, QuiverItem quiver) {
            this.slot = slot;
            this.quiver = quiver;
        }

        public int getSlot() {
            return slot;
        }

        public QuiverItem getQuiver() {
            return quiver;
        }

    }

    public static class QuiverItem {

        private final int max = DEFAULT_MAX_CAPACITY;
        private int amount;

        private ItemStack stack;
        private Inventory inventory;

        public QuiverItem() {
            this.amount = 0;
            this.stack = new ItemStack(Material.BUNDLE);
            
            updateItemInformation();
            updateInventory();
        }

        public QuiverItem(ItemStack stack) {
            this.stack = stack;

            updateInventory();
        }

        public Inventory getInventory() {
            return inventory;
        }

        public void openInventory(Player player) {
            Bukkit.getScheduler().scheduleSyncDelayedTask(QuiverPlugin.getPlugin(), () -> { 
                player.openInventory(inventory);
                OPEN_QUIVER_INVENTORIES.put(player, this);
            });
        }

        public ItemStack add(ItemStack arrows) {
            if (!arrows.getType().name().contains("ARROW") || amount >= max)
                return arrows;

            int stackAmount = arrows.getAmount();
            int combined = amount += stackAmount;
            if (combined > max) {
                arrows.setAmount(stackAmount - (combined - max));
                amount = max;
                
                updateInventory();                
                return arrows;
            }

            amount = combined;
            updateInventory();

            return new ItemStack(Material.AIR);
        }

        public boolean compare(ItemStack comparedStack) {
            return stack == comparedStack;
        }

        public void update(Inventory interactedInventory, int slot) {
            updateInventory();

            ItemStack newStack = updateItemInformation();
            inventory.setItem(slot, newStack);
        }

        private ItemStack updateItemInformation() {
            ItemStack newStack = stack.clone();
            ItemMeta meta = newStack.getItemMeta();

            meta.getPersistentDataContainer().set(QUIVER_DATA, PersistentDataType.STRING, turnDataToString(inventory.getContents()));

            String loreString = amount == 0 
                                ? ITEM_EMPTY_DESCRIPTION_FORMAT 
                                : amount >= max
                                ? ITEM_FULL_DESCRIPTION_FORMAT
                                : ITEM_FILLED_DESCRIPTION_FORMAT;

            loreString
                .replace("%current%", String.valueOf(amount))
                .replace("%capacity%", String.valueOf(max))
                .replace("\\n", "\n")
                .replace("&", "§");
            
            meta.setLore(Collections.singletonList(loreString));
            newStack.setItemMeta(meta);

            QUIVERS.remove(stack);
            QUIVERS.put(newStack, this);

            stack = newStack;
            return newStack;
        }

        private void updateInventory() {
            ItemMeta meta = stack.getItemMeta();

            if (meta == null || !meta.getPersistentDataContainer().has(QUIVER_DATA))
                return;
            
            ItemStack[] arrows = turnStringToData(meta.getPersistentDataContainer().get(QUIVER_DATA, PersistentDataType.STRING));

            int newAmount = 0;

            if (arrows != null) {
                for (ItemStack arrow : arrows) {
                    newAmount += arrow.getAmount();
                }
            }

            this.amount = newAmount;

            final String name = meta.hasDisplayName() 
                                ? stack.getItemMeta().getDisplayName() 
                                : DEFAULT_ITEM_NAME;

            inventory = Bukkit.createInventory(null, InventoryType.HOPPER, name);
            if (arrows != null)
                inventory.setContents(arrows);
        }

    }

}
