package org.wai;

import org.bukkit.plugin.java.JavaPlugin;
import org.wai.database.DatabaseManager;
import org.wai.modules.AltsModule;
import org.wai.modules.AutoRestartModule;
import org.wai.modules.LinkingModule;
import org.wai.modules.ProfileModule;
import org.wai.modules.titles.TitlesModule;

public class WAIServerCore extends JavaPlugin {
    private DatabaseManager dbManager;
    private LinkingModule linkingModule;
    private AltsModule altsModule;
    private AutoRestartModule autoRestartModule;
    private TitlesModule titlesModule;
    private ProfileModule profileModule;

    @Override
    public void onEnable() {
        try {
            // Инициализация DatabaseManager
            dbManager = new DatabaseManager(getLogger());

            // Инициализация модулей
            linkingModule = new LinkingModule(this, dbManager.getLinksConnection());
            altsModule = new AltsModule(this, dbManager.getAltsConnection());
            autoRestartModule = new AutoRestartModule(this);
            titlesModule = new TitlesModule(this); // Инициализация TitlesModule
            profileModule = new ProfileModule(this); // Инициализация ProfileModule

            // Логирование успешного запуска
            getLogger().info("WAIServerCore enabled!");
        } catch (Exception e) {
            getLogger().severe("Ошибка при запуске WAIServerCore: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void onDisable() {
        // Закрытие соединений с базой данных
        if (dbManager != null) {
            dbManager.closeConnections();
        }

        // Остановка модуля AutoRestartModule
        if (autoRestartModule != null) {
            autoRestartModule.stop();
        }

        // Логирование отключения
        getLogger().info("WAIServerCore disabled");
    }

    // Геттеры для модулей
    public TitlesModule getTitlesModule() {
        return titlesModule;
    }

    public ProfileModule getProfileModule() {
        return profileModule;
    }

    public LinkingModule getLinkingModule() {
        return linkingModule;
    }

    public AltsModule getAltsModule() {
        return altsModule;
    }

    public AutoRestartModule getAutoRestartModule() {
        return autoRestartModule;
    }
}