package org.wai.modules.titles;

import org.wai.modules.titles.listeners.PlayerListener;
import org.wai.WAIServerCore;
import org.bukkit.plugin.java.JavaPlugin;

public class TitlesModule {
    // Основной плагин WAIServerCore
    private final WAIServerCore plugin;

    // Менеджер титулов, который управляет всеми титулами игроков
    private final TitleManager titleManager;

    // Конструктор TitlesModule
    public TitlesModule(WAIServerCore plugin) {
        this.plugin = plugin; // Передаем основной плагин
        this.titleManager = new TitleManager(plugin); // Инициализируем менеджер титулов
    }

    // Метод для регистрации команд и событий
    public void registerCommandsAndEvents() {
        // Регистрируем команду /titles
        // TitleCommand - класс, который обрабатывает команду /titles
        plugin.getCommand("titles").setExecutor(new TitleCommand(titleManager));

        // Регистрируем команду /tradetitles
        // TradeTitlesCommand - класс, который обрабатывает команду /tradetitles
        plugin.getCommand("tradetitles").setExecutor(new TradeTitlesCommand(titleManager));

        // Регистрируем слушатель событий PlayerListener
        // PlayerListener - класс, который обрабатывает события, связанные с игроками (например, вход/выход)
        plugin.getServer().getPluginManager().registerEvents(
                new PlayerListener(titleManager), plugin
        );
    }

    // Геттер для TitleManager
    // Позволяет другим модулям получить доступ к менеджеру титулов
    public TitleManager getTitleManager() {
        return titleManager;
    }
}