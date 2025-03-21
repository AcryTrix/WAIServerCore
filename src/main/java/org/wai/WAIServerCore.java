package org.wai;

import org.bukkit.plugin.java.JavaPlugin;
import org.wai.database.DatabaseManager;
import org.wai.modules.AltsModule;
import org.wai.modules.AutoRestartModule;
import org.wai.modules.LinkingModule;
import org.wai.modules.MOTDModule;

public class WAIServerCore extends JavaPlugin {
    private DatabaseManager dbManager;
    private LinkingModule linkingModule;
    private AltsModule altsModule;
    private AutoRestartModule autoRestartModule;
    private MOTDModule motdModule;

    @Override
    public void onEnable() {
        dbManager = new DatabaseManager(getLogger());
        linkingModule = new LinkingModule(this, dbManager.getLinksConnection());
        altsModule = new AltsModule(this, dbManager.getAltsConnection());
        autoRestartModule = new AutoRestartModule(this);
        motdModule = new MOTDModule(this);

        motdModule.registerCommandsAndEvents();
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