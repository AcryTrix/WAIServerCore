package org.wai.modules;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public class ElytraEnchantControlModule implements Listener {

    private final JavaPlugin plugin;

    public ElytraEnchantControlModule(JavaPlugin plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onEnchant(EnchantItemEvent event) {
        ItemStack item = event.getItem();
        if (item.getType() == Material.ELYTRA) {
            // Исправлено DURABILITY → UNBREAKING
            event.getEnchantsToAdd().keySet().removeIf(enchant ->
                    enchant == Enchantment.MENDING || enchant == Enchantment.UNBREAKING);
        }
    }

    @EventHandler
    public void onAnvil(PrepareAnvilEvent event) {
        ItemStack result = event.getResult();
        if (result != null && result.getType() == Material.ELYTRA) {
            // Исправлено DURABILITY → UNBREAKING
            boolean hasIllegal = result.getEnchantments().keySet().stream()
                    .anyMatch(enchant -> enchant == Enchantment.MENDING || enchant == Enchantment.UNBREAKING);
            if (hasIllegal) {
                event.setResult(null);
            }
        }
    }

    private boolean removeIllegalEnchants(ItemStack item) {
        boolean modified = false;
        if (item.containsEnchantment(Enchantment.MENDING)) {
            item.removeEnchantment(Enchantment.MENDING);
            modified = true;
        }
        // Исправлено DURABILITY → UNBREAKING
        if (item.containsEnchantment(Enchantment.UNBREAKING)) {
            item.removeEnchantment(Enchantment.UNBREAKING);
            modified = true;
        }
        return modified;
    }
}