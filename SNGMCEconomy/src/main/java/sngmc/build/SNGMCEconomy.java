package sngmc.build;

import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;
import net.milkbowl.vault.economy.Economy;
import sngmc.build.commands.BalanceCommand;
import sngmc.build.commands.EcoCommand;
import sngmc.build.commands.PayCommand;
import org.bukkit.ChatColor;
import org.bukkit.scheduler.BukkitRunnable;

public class SNGMCEconomy extends JavaPlugin {

    private static SNGMCEconomy instance;
    private EconomyCore economyCore;
    private ConfigManager configManager;
    private int autosaveTaskId;

    @Override
    public void onEnable() {
        instance = this;
        // Убираем стандартное сообщение Bukkit о включении плагина
        getLogger().setFilter(record -> !record.getMessage().contains("Enabling " + getDescription().getName()));

        configManager = new ConfigManager();
        configManager.setupConfig();

        economyCore = new EconomyCore();

        getServer().getServicesManager().register(Economy.class, economyCore, this, ServicePriority.Normal);

        new BalanceCommand(this);
        new PayCommand(this);
        new EcoCommand(this);

        // Запускаем автосохранение
        int autosaveInterval = configManager.getConfig().getInt("economy.autosave-interval", 300) * 20;
        autosaveTaskId = new BukkitRunnable() {
            @Override
            public void run() {
                economyCore.saveData();
                logInfo("Данные экономики автоматически сохранены");
            }
        }.runTaskTimer(this, autosaveInterval, autosaveInterval).getTaskId();

        logInfo("Плагин экономики включен");
    }

    @Override
    public void onDisable() {
        // Отменяем задачу автосохранения
        getServer().getScheduler().cancelTask(autosaveTaskId);

        economyCore.saveData();
        logInfo("Плагин экономики отключен");
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

    public void logInfo(String message) {
        String prefix = configManager.getConfig().getString("messages.prefix", "");
        String coloredMessage = ChatColor.translateAlternateColorCodes('&', prefix + message);
        getServer().getConsoleSender().sendMessage(coloredMessage);
    }

    public void logTransaction(String playerName, String action, String details) {
        String logMessage = String.format("[%s] %s: %s", playerName, action, details);
        logInfo(logMessage);
    }
}