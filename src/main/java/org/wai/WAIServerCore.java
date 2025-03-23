package org.wai;

import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.wai.config.ConfigManager;
import org.wai.database.DatabaseManager;
import org.wai.modules.*;
import org.wai.modules.titles.*;

public class WAIServerCore extends JavaPlugin {
    private ConfigManager configManager;
    private DatabaseManager dbManager;
    private LinkingModule linkingModule;
    private AltsModule altsModule;
    private AutoRestartModule autoRestartModule;
    private MOTDModule motdModule;
    private SleepSkipModule sleepSkipModule;
    private PlayerInfoModule playerInfoModule;
    private TitlesModule titlesModule;
    private WebhookManager webhookManager;
    private WebhookManager moderWebhookManager;
    private ProfileModule profileModule;
    private SettingsMenu settingsMenu;
    private WorldControlModule worldControlModule;
    private ElytraEnchantControlModule elytraControlModule;
    private ModerActivationModule moderActivationModule;
    private EntityTrackerModule entityTrackerModule;
    private ReportModule reportModule;

    @Override
    public void onEnable() {
        configManager = new ConfigManager(this);
        dbManager = new DatabaseManager(this, configManager.getString("database.path"));
        webhookManager = new WebhookManager(this, configManager.getString("discord.webhook_url"));
        moderWebhookManager = new WebhookManager(this, configManager.getString("discord.moder_webhook_url"));
        linkingModule = new LinkingModule(this, dbManager.getLinksConnection());
        altsModule = new AltsModule(this, dbManager.getAltsConnection());
        autoRestartModule = new AutoRestartModule(this, webhookManager, configManager);
        motdModule = new MOTDModule(this, configManager);
        sleepSkipModule = new SleepSkipModule(this);
        playerInfoModule = new PlayerInfoModule(this, dbManager.getPlayerInfoConnection());
        titlesModule = new TitlesModule(this);
        profileModule = new ProfileModule(this);
        settingsMenu = new SettingsMenu(this, titlesModule.getTitleManager());
        worldControlModule = new WorldControlModule(this, configManager);
        elytraControlModule = new ElytraEnchantControlModule(this);
        moderActivationModule = new ModerActivationModule(this, moderWebhookManager, configManager);
        entityTrackerModule = new EntityTrackerModule(this, configManager);
        reportModule = new ReportModule(this, new WebhookManager(this, configManager.getString("discord.report_webhook_url")));
        registerEventsAndCommands();
        webhookManager.sendServerStartMessage();
        autoRestartModule.start();
        entityTrackerModule.start();
        getLogger().info("§aWAIServerCore успешно запущен!");
    }

    @Override
    public void onDisable() {
        dbManager.closeConnections();
        autoRestartModule.stop();
        entityTrackerModule.stop();
        getLogger().info("§cWAIServerCore отключен");
    }

    private void registerEventsAndCommands() {
        getServer().getPluginManager().registerEvents(motdModule, this);
        getServer().getPluginManager().registerEvents(sleepSkipModule, this);
        getServer().getPluginManager().registerEvents(elytraControlModule, this);
        altsModule.registerCommandsAndEvents();
        playerInfoModule.registerCommandsAndEvents();
        titlesModule.registerCommandsAndEvents();
        worldControlModule.registerCommandsAndEvents();
        moderActivationModule.registerCommandsAndEvents();
        linkingModule.registerCommands();
        getCommand("settings").setExecutor((sender, command, label, args) -> {
            if (!(sender instanceof Player player)) {
                sender.sendMessage("§cЭта команда только для игроков!");
                return true;
            }
            settingsMenu.openSettingsMenu(player);
            return true;
        });
    }

    public TitlesModule getTitlesModule() {
        return titlesModule;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }
}
