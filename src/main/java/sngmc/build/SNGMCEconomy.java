package sngmc.build;

import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;
import net.milkbowl.vault.economy.Economy;
import sngmc.build.commands.BalanceCommand;
import sngmc.build.commands.PayCommand;
import sngmc.build.commands.SetMoneyCommand;
import sngmc.build.commands.GiveMoneyCommand;
import sngmc.build.commands.TakeMoneyCommand;

public class SNGMCEconomy extends JavaPlugin {

    private static SNGMCEconomy instance;
    private EconomyCore economyCore;
    private ConfigManager configManager;

    @Override
    public void onEnable() {
        instance = this;

        configManager = new ConfigManager();
        configManager.setupConfig();

        economyCore = new EconomyCore();

        getServer().getServicesManager().register(Economy.class, economyCore, this, ServicePriority.Normal);

        new BalanceCommand(this);
        new PayCommand(this);
        new SetMoneyCommand(this);
        new GiveMoneyCommand(this);
        new TakeMoneyCommand(this);

        getLogger().info("Плагин экономики успешно запущен!");
    }

    @Override
    public void onDisable() {
        economyCore.saveData();
        getLogger().info("Плагин экономики отключен");
    }

    public static SNGMCEconomy getInstance() {
        return instance;
    }

    public EconomyCore getEconomy() {
        return economyCore;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }
}