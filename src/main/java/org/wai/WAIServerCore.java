package org.wai;

import org.bukkit.plugin.java.JavaPlugin;
import org.wai.database.DatabaseManager;
import org.wai.modules.AltsModule;
import org.wai.modules.AutoRestartModule;
import org.wai.modules.LinkingModule;
import org.wai.modules.MOTDModule;
import org.wai.modules.SleepSkipModule;
import org.wai.modules.PlayerInfoModule;
import org.wai.modules.titles.TitlesModule;
import org.wai.modules.WebhookManager;

public class WAIServerCore extends JavaPlugin {
    private DatabaseManager dbManager;
    private LinkingModule linkingModule;
    private AltsModule altsModule;
    private AutoRestartModule autoRestartModule;
    private MOTDModule motdModule;
    private SleepSkipModule sleepSkipModule;
    private PlayerInfoModule playerInfoModule;
    private TitlesModule titlesModule;
    private WebhookManager webhookManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        String webhookUrl = getConfig().getString("discord-webhook-url", "");
        webhookManager = new WebhookManager(this, webhookUrl);
        dbManager = new DatabaseManager(getLogger());
        linkingModule = new LinkingModule(this, dbManager.getLinksConnection());
        altsModule = new AltsModule(this, dbManager.getAltsConnection());
        autoRestartModule = new AutoRestartModule(this, webhookManager);
        motdModule = new MOTDModule(this);
        sleepSkipModule = new SleepSkipModule(this);
        playerInfoModule = new PlayerInfoModule(this, dbManager.getPlayerInfoConnection());
        titlesModule = new TitlesModule(this);
        getServer().getPluginManager().registerEvents(motdModule, this);
        getServer().getPluginManager().registerEvents(sleepSkipModule, this);
        altsModule.registerCommandsAndEvents();
        autoRestartModule.start();
        playerInfoModule.registerCommandsAndEvents();
        titlesModule.registerCommandsAndEvents();
        webhookManager.sendServerStartMessage();
        getLogger().info("§aWAIServerCore успешно запущен!");
    }

    @Override
    public void onDisable() {
        dbManager.closeConnections();
        if (autoRestartModule != null) autoRestartModule.stop();
        getLogger().info("§cWAIServerCore отключен");
    }

    public TitlesModule getTitlesModule() {
        return titlesModule;
    }
}