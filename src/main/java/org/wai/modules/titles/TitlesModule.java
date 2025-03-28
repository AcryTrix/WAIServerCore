package org.wai.modules.titles;

import org.wai.WAIServerCore;

public class TitlesModule {
    private final WAIServerCore plugin;
    private final TitleManager titleManager;

    public TitlesModule(WAIServerCore plugin) {
        this.plugin = plugin;
        this.titleManager = new TitleManager(plugin);
    }

    public void registerCommandsAndEvents() {
        plugin.getCommand("titles").setExecutor(new TitleCommand(titleManager));
        plugin.getCommand("tradetitles").setExecutor(new TradeTitlesCommand(titleManager));
        plugin.getServer().getPluginManager().registerEvents(new PlayerListener(titleManager), plugin);
    }

    public TitleManager getTitleManager() {
        return titleManager;
    }
}