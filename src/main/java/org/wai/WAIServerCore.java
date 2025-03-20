package org.wai;

import org.bukkit.plugin.java.JavaPlugin;
import org.wai.database.DatabaseManager;
import org.wai.modules.AltsModule;
import org.wai.modules.LinkingModule;

public class WAIServerCore extends JavaPlugin {
    private DatabaseManager dbManager;
    private LinkingModule linkingModule;
    private AltsModule altsModule;

    @Override
    public void onEnable() {
        dbManager = new DatabaseManager(getLogger());
        linkingModule = new LinkingModule(this, dbManager.getLinksConnection());
        altsModule = new AltsModule(this, dbManager.getAltsConnection());
        altsModule.registerCommandsAndEvents();
        getLogger().info("WAIServerCore enabled!");
    }

    @Override
    public void onDisable() {
        dbManager.closeConnections();
        getLogger().info("WAIServerCore disabled");
    }
}