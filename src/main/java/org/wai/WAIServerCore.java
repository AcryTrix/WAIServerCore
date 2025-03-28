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
    private FishingModule fishingModule;
    private InvisibleFrameModule invisibleFrameModule;

    @Override
    public void onEnable() {
        // Initialize ConfigManager first since other modules depend on it
        configManager = new ConfigManager(this);

        // Initialize DatabaseManager
        dbManager = new DatabaseManager(this, configManager.getString("database.path"));

        // Initialize WebhookManagers
        webhookManager = new WebhookManager(this, configManager.getString("discord.webhook_url"));
        moderWebhookManager = new WebhookManager(this, configManager.getString("discord.moder_webhook_url"));

        // Initialize all modules
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
        fishingModule = new FishingModule(this);  // Initialize FishingModule
        invisibleFrameModule = new InvisibleFrameModule(this);  // Assuming this exists

        // Register events and commands
        registerEventsAndCommands();

        // Start modules that need to run periodically
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
        // Register event listeners
        getServer().getPluginManager().registerEvents(motdModule, this);
        getServer().getPluginManager().registerEvents(sleepSkipModule, this);
        getServer().getPluginManager().registerEvents(elytraControlModule, this);
        getServer().getPluginManager().registerEvents(fishingModule, this);  // Register FishingModule events
        getServer().getPluginManager().registerEvents(invisibleFrameModule, this);  // Register InvisibleFrameModule events

        // Register commands and events for modules that have their own methods
        altsModule.registerCommandsAndEvents();
        playerInfoModule.registerCommandsAndEvents();
        titlesModule.registerCommandsAndEvents();
        worldControlModule.registerCommandsAndEvents();
        moderActivationModule.registerCommandsAndEvents();
        linkingModule.registerCommands();

        // Register the /settings command
        getCommand("settings").setExecutor((sender, command, label, args) -> {
            if (!(sender instanceof Player player)) {
                sender.sendMessage("§cЭта команда только для игроков!");
                return true;
            }
            settingsMenu.openSettingsMenu(player);
            return true;
        });

        // Optional: Add a command for fishing module if desired (example below)
        /*
        getCommand("fishinfo").setExecutor((sender, command, label, args) -> {
            if (!(sender instanceof Player player)) {
                sender.sendMessage("§cЭта команда только для игроков!");
                return true;
            }
            player.sendMessage("§aFishing Mini-Game is active! Catch items by fishing.");
            return true;
        });
        */
    }

    // Getter methods for modules
    public TitlesModule getTitlesModule() {
        return titlesModule;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public FishingModule getFishingModule() {
        return fishingModule;
    }
}