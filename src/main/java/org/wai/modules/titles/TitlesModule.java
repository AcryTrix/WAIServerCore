package org.wai.modules.titles;

import org.wai.WAIServerCore;
import org.bukkit.plugin.PluginManager;

public class TitlesModule {
    private final WAIServerCore plugin;
    private final TitleManager titleManager;

    public TitlesModule(WAIServerCore plugin) {
        this.plugin = plugin;
        this.titleManager = new TitleManager(plugin);
    }

    public void registerCommandsAndEvents() {
        // Регистрация команды /titles
        plugin.getCommand("titles").setExecutor(new TitleCommand(titleManager));

        // Регистрация команды /tradetitles
        plugin.getCommand("tradetitles").setExecutor(new TradeTitlesCommand(titleManager));

        // Регистрация слушателя PlayerListener
        PluginManager pm = plugin.getServer().getPluginManager();
        pm.registerEvents(new PlayerListener(titleManager), plugin);
    }

    public TitleManager getTitleManager() {
        return titleManager;
    }
}