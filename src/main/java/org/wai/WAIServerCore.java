package org.wai;

import org.bukkit.plugin.java.JavaPlugin;
import org.wai.database.DatabaseManager;
import org.wai.modules.AltsModule;
import org.wai.modules.AutoRestartModule;
import org.wai.modules.LinkingModule;
import org.wai.modules.ProfileModule;
import org.wai.modules.TradeModule;
import org.wai.modules.TradeCommandHandler;

public class WAIServerCore extends JavaPlugin {
    private DatabaseManager dbManager;
    private LinkingModule linkingModule;
    private AltsModule altsModule;
    private ProfileModule profileModule;
    private AutoRestartModule autoRestartModule;
    private TradeModule tradeModule;

    @Override
    public void onEnable() {
        dbManager = new DatabaseManager(getLogger());
        linkingModule = new LinkingModule(this, dbManager.getLinksConnection());
        altsModule = new AltsModule(this, dbManager.getAltsConnection());
        profileModule = new ProfileModule(this);
        autoRestartModule = new AutoRestartModule(this);
        tradeModule = new TradeModule(this); // Инициализация модуля обмена титулами

        // Регистрация команд для обмена титулами
        TradeCommandHandler tradeCommandHandler = new TradeCommandHandler(tradeModule);
        getCommand("tradeaccept").setExecutor(tradeCommandHandler);
        getCommand("tradedecline").setExecutor(tradeCommandHandler);

        altsModule.registerCommandsAndEvents();
        autoRestartModule.start();

        getLogger().info("WAIServerCore enabled!");
    }

    @Override
    public void onDisable() {
        dbManager.closeConnections();
        if (autoRestartModule != null) {
            autoRestartModule.stop();
        }
        getLogger().info("WAIServerCore disabled");
    }
}