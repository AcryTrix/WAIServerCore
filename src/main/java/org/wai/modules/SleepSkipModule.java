package org.wai.modules;

import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerBedLeaveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;

public class SleepSkipModule implements Listener {
    private final JavaPlugin plugin;
    private final Map<World, Integer> sleepingPlayers = new HashMap<>();
    private final Map<World, Integer> scheduledTasks = new HashMap<>();

    public SleepSkipModule(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onBedEnter(PlayerBedEnterEvent event) {
        if (event.getBedEnterResult() != PlayerBedEnterEvent.BedEnterResult.OK) return;
        World world = event.getPlayer().getWorld();
        if (world.getTime() < 12541) return;

        sleepingPlayers.put(world, sleepingPlayers.getOrDefault(world, 0) + 1);
        scheduleCheck(world);
    }

    @EventHandler
    public void onBedLeave(PlayerBedLeaveEvent event) {
        updateSleepCounter(event.getPlayer().getWorld());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        updateSleepCounter(event.getPlayer().getWorld());
    }

    private void updateSleepCounter(World world) {
        int current = (int) world.getPlayers().stream().filter(Player::isSleeping).count();
        sleepingPlayers.put(world, current);
        scheduleCheck(world);
    }

    private void scheduleCheck(World world) {
        if (scheduledTasks.containsKey(world)) {
            plugin.getServer().getScheduler().cancelTask(scheduledTasks.get(world));
        }

        int taskId = plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> {
            checkSleepConditions(world);
            scheduledTasks.remove(world);
        }, 60L);

        scheduledTasks.put(world, taskId);
    }

    private void checkSleepConditions(World world) {
        if (world.getTime() < 12541) {
            sleepingPlayers.put(world, 0);
            return;
        }

        int onlinePlayers = world.getPlayers().size();
        if (onlinePlayers == 0) return;

        int required = (int) Math.ceil(onlinePlayers / 2.0);
        int sleeping = sleepingPlayers.getOrDefault(world, 0);

        String message = String.format("§e%d/%d игроков спят", sleeping, required);
        world.getPlayers().forEach(p -> p.sendActionBar(message));

        if (sleeping >= required) {
            world.setTime(0);
            world.setStorm(false);
            world.setThundering(false);
            sleepingPlayers.put(world, 0);
            world.getPlayers().forEach(p -> p.sendActionBar("§aНочь пропущена!"));
        }
    }
}