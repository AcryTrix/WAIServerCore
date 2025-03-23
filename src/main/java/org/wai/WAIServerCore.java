package org.wai;

import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.wai.database.DatabaseManager;
import org.wai.modules.*;
import org.wai.modules.titles.TitlesModule;
import org.bukkit.ChatColor;

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
    private WebhookManager moderWebhookManager;
    private ProfileModule profileModule;
    private SettingsMenu settingsMenu;
    private WorldControlModule worldControlModule;
    private ElytraEnchantControlModule elytraControlModule;
    private ModerActivationModule moderActivationModule;
    private FishingModule fishingModule;

    @Override
    public void onEnable() {
        dbManager = new DatabaseManager(getLogger());

        linkingModule = new LinkingModule(this, dbManager.getLinksConnection());
        altsModule = new AltsModule(this, dbManager.getAltsConnection());
        motdModule = new MOTDModule(this);
        sleepSkipModule = new SleepSkipModule(this);
        playerInfoModule = new PlayerInfoModule(this, dbManager.getPlayerInfoConnection());
        titlesModule = new TitlesModule(this);
        profileModule = new ProfileModule(this);
        worldControlModule = new WorldControlModule(this);
        elytraControlModule = new ElytraEnchantControlModule(this);
        fishingModule = new FishingModule(this);

        saveDefaultConfig();
        String webhookUrl = getConfig().getString("discord-webhook-url", "");
        String moderWebhookUrl = getConfig().getString("moder-webhook-url", "");
        webhookManager = new WebhookManager(this, webhookUrl);
        moderWebhookManager = new WebhookManager(this, moderWebhookUrl);

        autoRestartModule = new AutoRestartModule(this, webhookManager);
        moderActivationModule = new ModerActivationModule(this, moderWebhookManager);

        settingsMenu = new SettingsMenu(this, titlesModule.getTitleManager());

        getServer().getPluginManager().registerEvents(motdModule, this);
        getServer().getPluginManager().registerEvents(sleepSkipModule, this);

        altsModule.registerCommandsAndEvents();
        autoRestartModule.start();
        playerInfoModule.registerCommandsAndEvents();
        titlesModule.registerCommandsAndEvents();
        worldControlModule.registerCommandsAndEvents();
        moderActivationModule.registerCommands();
        registerSettingsCommand();

        webhookManager.sendServerStartMessage();

        getLogger().info("WAIServerCore успешно запущен!");
    }

    @Override
    public void onDisable() {
        dbManager.closeConnections();
        if (autoRestartModule != null) autoRestartModule.stop();
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

    public TitlesModule getTitlesModule() {
        return titlesModule;
    }
}