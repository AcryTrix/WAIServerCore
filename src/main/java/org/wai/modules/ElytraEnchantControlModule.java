package org.wai.modules;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public class ElytraEnchantControlModule implements Listener {
    private final JavaPlugin plugin;

    public ElytraEnchantControlModule(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onEnchant(EnchantItemEvent event) {
            event.getEnchantsToAdd().keySet().removeIf(enchant ->
                    enchant == Enchantment.MENDING || enchant == Enchantment.UNBREAKING);
        }
    }

    @EventHandler
    public void onAnvil(PrepareAnvilEvent event) {
        ItemStack result = event.getResult();
        if (result != null && result.getType() == Material.ELYTRA) {
                event.setResult(null);
            }
        }
    }
}