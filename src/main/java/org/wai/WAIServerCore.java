package org.wai;

import org.bukkit.plugin.java.JavaPlugin;
import org.wai.config.ConfigManager;
import org.wai.modules.titles.TitlesModule;
import org.wai.modules.*;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class WAIServerCore extends JavaPlugin {
    private ConfigManager configManager;
    private Connection connection;
    private WebhookManager webhookManager;
    private WebhookManager moderWebhookManager;
    private WebhookManager reportWebhookManager;
    private TitlesModule titlesModule;
    private FishingModule fishingModule;
    private ModerActivationModule moderActivationModule;
    private AltsModule altsModule;
    private AutoRestartModule autoRestartModule;
    private ElytraEnchantControlModule elytraEnchantControlModule;
    private EntityTrackerModule entityTrackerModule;
    private InvisibleFrameModule invisibleFrameModule;
    private LinkingModule linkingModule;
    private MOTDModule motdModule;
    private PlayerInfoModule playerInfoModule;
    private ProfileModule profileModule;
    private ReportModule reportModule;
    private SettingsMenu settingsMenu;
    private SleepSkipModule sleepSkipModule;
    private WorldControlModule worldControlModule;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        configManager = new ConfigManager(this);
        String dbPath = configManager.getString("database.path");
        try {
            connection = DriverManager.getConnection("jdbc:sqlite:" + dbPath);
        } catch (SQLException e) {
            getLogger().severe("Failed to connect to database: " + e.getMessage());
        }

        String webhookUrl = configManager.getString("discord.webhook_url");
        String moderWebhookUrl = configManager.getString("discord.moder_webhook_url");
        String reportWebhookUrl = configManager.getString("discord.report_webhook_url");

        webhookManager = new WebhookManager(this, webhookUrl);
        moderWebhookManager = new WebhookManager(this, moderWebhookUrl);
        reportWebhookManager = new WebhookManager(this, reportWebhookUrl);

        titlesModule = new TitlesModule(this);
        titlesModule.registerCommandsAndEvents();

        fishingModule = new FishingModule(this);
        getServer().getPluginManager().registerEvents(fishingModule, this);

        moderActivationModule = new ModerActivationModule(this, moderWebhookManager, configManager);
        moderActivationModule.registerCommandsAndEvents();

        altsModule = new AltsModule(this, connection);
        altsModule.registerCommandsAndEvents();

        autoRestartModule = new AutoRestartModule(this, webhookManager);
        autoRestartModule.start();
        getCommand("restartnow").setExecutor((sender, cmd, label, args) -> {
            autoRestartModule.instantRestart();
            return true;
        });

        elytraEnchantControlModule = new ElytraEnchantControlModule(this);

        entityTrackerModule = new EntityTrackerModule(this, configManager);
        entityTrackerModule.register();

        invisibleFrameModule = new InvisibleFrameModule(this);

        linkingModule = new LinkingModule(this, connection);
        linkingModule.registerCommands();

        motdModule = new MOTDModule(this, configManager);
        getServer().getPluginManager().registerEvents(motdModule, this);

        playerInfoModule = new PlayerInfoModule(this, connection);
        playerInfoModule.registerCommandsAndEvents();

        profileModule = new ProfileModule(this);

        reportModule = new ReportModule(this, reportWebhookManager);

        settingsMenu = new SettingsMenu(this, titlesModule.getTitleManager());

        sleepSkipModule = new SleepSkipModule(this);
        getServer().getPluginManager().registerEvents(sleepSkipModule, this);

        worldControlModule = new WorldControlModule(this);
        worldControlModule.registerCommandsAndEvents();

        webhookManager.sendServerStartMessage();
    }

    @Override
    public void onDisable() {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                getLogger().severe("Failed to close database connection: " + e.getMessage());
            }
        }
        moderActivationModule.stopCodeUpdateTask();
        autoRestartModule.stop();
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public TitlesModule getTitlesModule() {
        return titlesModule;
    }

    public Connection getConnection() {
        return connection;
    }
}