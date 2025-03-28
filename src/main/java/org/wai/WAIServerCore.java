package org.wai;

import org.bukkit.plugin.java.JavaPlugin;
import org.wai.config.ConfigManager;
import org.wai.database.DatabaseManager;
import org.wai.modules.*;
import org.wai.modules.titles.TitlesModule;

public class WAIServerCore extends JavaPlugin {
    private ConfigManager configManager;
    private DatabaseManager databaseManager;
    private WebhookManager webhookManager;
    private WebhookManager reportWebhookManager;
    private LinkingModule linkingModule;
    private AltsModule altsModule;
    private AutoRestartModule autoRestartModule;
    private MOTDModule motdModule;
    private SleepSkipModule sleepSkipModule;
    private PlayerInfoModule playerInfoModule;
    private TitlesModule titlesModule;
    private SettingsMenu settingsMenu;
    private ProfileModule profileModule;
    private ReportModule reportModule;
    private WorldControlModule worldControlModule;
    private FishingModule fishingModule;
    private EntityTrackerModule entityTrackerModule;
    private ModerActivationModule moderActivationModule;

    @Override
    public void onEnable() {
        initializeConfig();
        initializeDatabase();
        initializeWebhooks();
        initializeModules();
        registerEvents();
        registerCommands();
        sendStartupMessage();
        getLogger().info("WAIServerCore успешно запущен!");
    }

    @Override
    public void onDisable() {
        databaseManager.closeConnections();
        if (autoRestartModule != null) {
            autoRestartModule.stop();
        }
        if (moderActivationModule != null) {
            moderActivationModule.stopCodeUpdateTask();
        }
        getLogger().info("WAIServerCore отключен");
    }

    private void initializeConfig() {
        configManager = new ConfigManager(this);
    }

    private void initializeDatabase() {
        databaseManager = new DatabaseManager(this, configManager.getString("database.path"));
    }

    private void initializeWebhooks() {
        webhookManager = new WebhookManager(this, configManager.getString("discord.webhook_url"));
        reportWebhookManager = new WebhookManager(this, configManager.getString("discord.report_webhook_url"));
    }

    private void initializeModules() {
        moderActivationModule = new ModerActivationModule(this, new WebhookManager(this, configManager.getString("discord.moder_webhook_url")), configManager);
        linkingModule = new LinkingModule(this, databaseManager.getLinksConnection());
        altsModule = new AltsModule(this, databaseManager.getAltsConnection());
        autoRestartModule = new AutoRestartModule(this, webhookManager);
        motdModule = new MOTDModule(this, configManager);
        sleepSkipModule = new SleepSkipModule(this);
        playerInfoModule = new PlayerInfoModule(this, databaseManager.getPlayerInfoConnection());
        titlesModule = new TitlesModule(this);
        settingsMenu = new SettingsMenu(this, titlesModule.getTitleManager());
        profileModule = new ProfileModule(this);
        reportModule = new ReportModule(this, reportWebhookManager);
        worldControlModule = new WorldControlModule(this);
        fishingModule = new FishingModule(this);
        entityTrackerModule = new EntityTrackerModule(this, configManager);
    }

    private void registerEvents() {
        getServer().getPluginManager().registerEvents(motdModule, this);
        getServer().getPluginManager().registerEvents(sleepSkipModule, this);
        getServer().getPluginManager().registerEvents(settingsMenu, this);
        getServer().getPluginManager().registerEvents(profileModule, this);
        getServer().getPluginManager().registerEvents(worldControlModule, this);
        getServer().getPluginManager().registerEvents(fishingModule, this);
    }

    private void registerCommands() {
        linkingModule.registerCommands();
        altsModule.registerCommandsAndEvents();
        autoRestartModule.start();
        entityTrackerModule.register();
        playerInfoModule.registerCommandsAndEvents();
        titlesModule.registerCommandsAndEvents();
        worldControlModule.registerCommandsAndEvents();
        moderActivationModule.registerCommandsAndEvents();
    }

    private void sendStartupMessage() {
        webhookManager.sendServerStartMessage();
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public TitlesModule getTitlesModule() {
        return titlesModule;
    }
}