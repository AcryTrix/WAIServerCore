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
    private final Map<World, Integer> scheduledTasks = new HashMap<>();

    public SleepSkipModule(JavaPlugin plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onBedEnter(PlayerBedEnterEvent event) {
        if (event.getBedEnterResult() != PlayerBedEnterEvent.BedEnterResult.OK) return;
        World world = event.getPlayer().getWorld();
        if (world.getTime() < 12541) return;
        scheduleDelayedCheck(world);
    }

    @EventHandler
    public void onBedLeave(PlayerBedLeaveEvent event) {
        scheduleDelayedCheck(event.getPlayer().getWorld());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        scheduleDelayedCheck(event.getPlayer().getWorld());
    }

    private void scheduleDelayedCheck(World world) {
        if (scheduledTasks.containsKey(world)) {
            plugin.getServer().getScheduler().cancelTask(scheduledTasks.get(world));
            scheduledTasks.remove(world);
        }
        int taskId = plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> checkSleepConditions(world), 60L);
        scheduledTasks.put(world, taskId);
    }

    private void checkSleepConditions(World world) {
        int sleeping = (int) world.getPlayers().stream().filter(Player::isSleeping).count();
        int onlinePlayers = world.getPlayers().size();
        if (onlinePlayers == 0) return;
        int required = (int) Math.ceil(onlinePlayers / 2.0);
        String message = String.format("§e%d/%d игроков спят", sleeping, required);
        world.getPlayers().forEach(p -> p.sendActionBar(message));
        if (sleeping >= required && world.getTime() >= 12541) {
            world.setTime(0);
            world.setStorm(false);
            world.setThundering(false);
            world.getPlayers().forEach(p -> p.sendActionBar("§aНочь пропущена!"));
        }
    }
}