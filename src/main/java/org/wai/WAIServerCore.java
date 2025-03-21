package org.wai;

import org.bukkit.plugin.java.JavaPlugin;
import org.wai.database.DatabaseManager;
import org.wai.modules.AltsModule;
import org.wai.modules.AutoRestartModule;
import org.wai.modules.LinkingModule;

public class WAIServerCore extends JavaPlugin {
    private DatabaseManager dbManager;
    private LinkingModule linkingModule;
    private AltsModule altsModule;
    private AutoRestartModule autoRestartModule;

    @Override
    public void onEnable() {
        dbManager = new DatabaseManager(getLogger());
        linkingModule = new LinkingModule(this, dbManager.getLinksConnection());
        altsModule = new AltsModule(this, dbManager.getAltsConnection());
        autoRestartModule = new AutoRestartModule(this);

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