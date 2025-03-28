package org.wai.modules;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.plugin.java.JavaPlugin;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

    private final JavaPlugin plugin;
    private final WebhookManager webhookManager;
    private final String codeFilePath;
    private final List<String> activeModerators = new ArrayList<>();

        this.plugin = plugin;
        this.webhookManager = webhookManager;
        this.codeFilePath = plugin.getDataFolder() + "/current_code.txt";
        startCodeUpdateTask();
    }

        }
    }

    private String generateCode() {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        for (int i = 0; i < 6; i++) {
        }
        return code.toString();
    }

    private void saveCodeToFile(String code) {
        try {
        } catch (IOException e) {
    }
        }

            try {
                    activeModerators.add(player.getName());
            } catch (IOException e) {
            }
        }
            activeModerators.remove(player.getName());
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
                event.setCancelled(true);
        }
    }
}