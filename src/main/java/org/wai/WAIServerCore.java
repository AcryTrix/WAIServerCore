package org.wai;

import org.bukkit.plugin.java.JavaPlugin;
import org.wai.config.ConfigManager;
import org.wai.database.DatabaseManager;
import org.wai.modules.*;
import org.wai.modules.titles.TitlesModule;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class WAIServerCore extends JavaPlugin {
		private ConfigManager configManager;
    private DatabaseManager databaseManager;

		private AltsModule altsModule;
		private AutoRestartModule autoRestartModule;
		private ElytraTrackerModule elytraTrackerModule;
		private EntityTrackerModule entityTrackerModule;
		private FishingModule fishingModule;
		private InvisibleFrameModule invisibleFrameModule;
		private LinkingModule linkingModule;
		private ModerActivationModule moderActivationModule;
		private MOTDModule motdModule;
		private PlayerInfoModule playerInfoModule;
		private ProfileModule profileModule;
		private ReportModule reportModule;
		private TitlesModule titlesModule;
		private SettingsMenu settingsMenu;
		private SleepSkipModule sleepSkipModule;
		private VapeModule vapeModule;
		private WebhookManager webhookManager;
    private WebhookManager reportWebhookManager;
    private WorldControlModule worldControlModule;

    @Override
    public void onEnable() {
        try {
            initializeConfig();
            initializeDatabase();
            initializeWebhooks();
            initializeModules();
            registerEvents();
            registerCommands();
            registerSettingsCommand();
            sendStartupMessage();
            getLogger().info("WAIServerCore успешно запущен!");
        } catch (Exception e) {
            getLogger().severe("Ошибка при запуске плагина: " + e.getMessage());
            e.printStackTrace();
            Bukkit.getPluginManager().disablePlugin(this);
        }
    }

    @Override
    public void onDisable() {
        try {
            if (databaseManager != null) {
                databaseManager.closeConnections();
            }
            if (autoRestartModule != null) {
                autoRestartModule.stop();
            }
            if (moderActivationModule != null) {
                moderActivationModule.stopCodeUpdateTask();
            }
        } catch (Exception e) {
            getLogger().severe("Ошибка при выключении плагина: " + e.getMessage());
        }
        getLogger().info("WAIServerCore отключен");
    }

    private void registerSettingsCommand() {
        getCommand("settings").setExecutor((sender, command, label, args) -> {
            if (sender instanceof Player) {
                settingsMenu.openSettingsMenu((Player) sender);
            } else {
                sender.sendMessage(ChatColor.RED + "Команда доступна только игрокам!");
            }
            return true;
        });
    }

    private void initializeConfig() {
        saveResource("config.toml", false);
        configManager = new ConfigManager(this);
        configManager.loadConfig();
    }

    private void initializeDatabase() {
        if (configManager == null) {
            throw new IllegalStateException("ConfigManager не инициализирован");
        }
        databaseManager = new DatabaseManager(this, configManager.getString("database.path"));
    }

    private void initializeWebhooks() {
        webhookManager = new WebhookManager(this, configManager.getString("discord.webhook_url"));
        reportWebhookManager = new WebhookManager(this, configManager.getString("discord.report_webhook_url"));
    }

    private void initializeModules() {
        if (databaseManager == null) {
            throw new IllegalStateException("DatabaseManager не инициализирован");
        }

        moderActivationModule = new ModerActivationModule(
                this,
                webhookManager,
                configManager
        );

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
        vapeModule = new VapeModule(this, configManager);
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
        registerSettingsCommand();
        vapeModule.registerCommandsAndEvents();

        this.getCommand("friend").setExecutor(new ProfileModule.FriendCommand(this));
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
