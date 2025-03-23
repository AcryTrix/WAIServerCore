package org.wai.modules.titles;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class TitlesModule {
    private final JavaPlugin plugin;
    private final TitleManager titleManager;

    public TitlesModule(JavaPlugin plugin) {
        this.plugin = plugin;
        this.titleManager = new TitleManager(plugin);
    }

    public void registerCommandsAndEvents() {
        Bukkit.getPluginManager().registerEvents(new PlayerListener(titleManager), plugin);
        plugin.getCommand("titles").setExecutor(new TitleCommand(titleManager));
        plugin.getCommand("tradetitles").setExecutor(new TradeTitlesCommand(titleManager));
    }

    public TitleManager getTitleManager() {
        return titleManager;
    }
}