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
    private ProfileModule profileModule;
    private SettingsMenu settingsMenu; // Модуль меню настроек
    private WorldControlModule worldControlModule;
    private ElytraEnchantControlModule elytraControlModule;

    @Override
    public void onEnable() {
        // Инициализация базы данных
        dbManager = new DatabaseManager(getLogger());

        // Инициализация модулей
        linkingModule = new LinkingModule(this, dbManager.getLinksConnection());
        altsModule = new AltsModule(this, dbManager.getAltsConnection());
        motdModule = new MOTDModule(this);
        sleepSkipModule = new SleepSkipModule(this);
        playerInfoModule = new PlayerInfoModule(this, dbManager.getPlayerInfoConnection());
        titlesModule = new TitlesModule(this);
        profileModule = new ProfileModule(this);
        worldControlModule = new WorldControlModule(this);
        elytraControlModule = new ElytraEnchantControlModule(this);

        // Инициализация WebhookManager
        saveDefaultConfig();
        String webhookUrl = getConfig().getString("discord-webhook-url", "");
        webhookManager = new WebhookManager(this, webhookUrl);
        autoRestartModule = new AutoRestartModule(this, webhookManager);
        worldControlModule.registerCommandsAndEvents();

        // Инициализация меню настроек
        settingsMenu = new SettingsMenu(this, titlesModule.getTitleManager());

        // Регистрация событий
        getServer().getPluginManager().registerEvents(motdModule, this);
        getServer().getPluginManager().registerEvents(sleepSkipModule, this);

        // Регистрация команд
        altsModule.registerCommandsAndEvents();
        autoRestartModule.start();
        playerInfoModule.registerCommandsAndEvents();
        titlesModule.registerCommandsAndEvents();
        registerSettingsCommand(); // Регистрация /settings

        // Уведомление о запуске
        webhookManager.sendServerStartMessage();
        getLogger().info("WAIServerCore успешно запущен!");
    }

    @Override
    public void onDisable() {
        // Завершение работы
        dbManager.closeConnections();
        if (autoRestartModule != null) autoRestartModule.stop();
        getLogger().info("WAIServerCore отключен");
    }

    // Регистрация команды /settings
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