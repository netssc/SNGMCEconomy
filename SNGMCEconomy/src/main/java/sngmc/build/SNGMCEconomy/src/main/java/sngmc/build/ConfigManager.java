package sngmc.build;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.ChatColor;

import java.io.File;
import java.io.IOException;

public class ConfigManager {

    private FileConfiguration config;
    private File configFile;

    public void setupConfig() {
        SNGMCEconomy plugin = SNGMCEconomy.getInstance();
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdir();
        }

        configFile = new File(plugin.getDataFolder(), "config.yml");

        if (!configFile.exists()) {
            plugin.saveResource("config.yml", false);
        }

        config = YamlConfiguration.loadConfiguration(configFile);

        // Добавляем все сообщения по умолчанию
        config.addDefault("storage.type", "YAML"); // YAML или MYSQL
        config.addDefault("database.host", "localhost");
        config.addDefault("database.port", 3306);
        config.addDefault("database.database", "minecraft");
        config.addDefault("database.username", "root");
        config.addDefault("database.password", "");
        config.addDefault("messages.prefix", "&x&0&3&4&B&B&C&ls&x&1&1&5&C&C&4&lʏ&x&2&0&6&D&C&B&ls&x&2&E&7&E&D&3&lᴛ&x&3&D&9&0&D&B&lᴇ&x&4&B&A&1&E&3&lᴍ &x&6&8&C&3&F&2• &r&f");
        config.addDefault("messages.no-permission", "&cУ вас нет прав на использование этой команды!");
        config.addDefault("messages.player-not-found", "&cИгрок не найден или не в сети!");
        config.addDefault("messages.invalid-amount", "&cНеверная сумма!");
        config.addDefault("messages.not-enough-money", "&cУ вас недостаточно денег!");
        config.addDefault("messages.balance-self", "Ваш баланс: %balance%");
        config.addDefault("messages.balance-other", "Баланс игрока %player%: %balance%");
        config.addDefault("messages.pay-sent", "Вы отправили %amount% игроку %player%");
        config.addDefault("messages.pay-received", "Вы получили %amount% от игрока %player%");
        config.addDefault("messages.money-given", "Вы выдали %amount% игроку %player%");
        config.addDefault("messages.money-taken", "Вы забрали %amount% у игрока %player%");
        config.addDefault("messages.money-set", "Вы установили баланс %player% на %amount%");
        config.addDefault("messages.self-payment", "Вы не можете отправить деньги себе!");
        config.addDefault("messages.usagepay", "Использование: /pay <игрок> <сумма>");
        config.addDefault("messages.usagebalance", "Использование: /balance [игрок]");
        config.addDefault("messages.usageeco", "Использование: /eco <give|set|take> <игрок> <сумма>");
        config.addDefault("messages.transaction-success", "Транзакция успешно выполнена!");
        config.addDefault("messages.transaction-failed", "Ошибка транзакции!");
        config.addDefault("messages.negative-balance", "Баланс не может быть отрицательным!");

        config.options().copyDefaults(true);
        saveConfig();
    }

    public FileConfiguration getConfig() {
        return config;
    }

    public void saveConfig() {
        try {
            config.save(configFile);
        } catch (IOException e) {
            SNGMCEconomy.getInstance().getLogger().severe("Не удалось сохранить конфиг: " + e.getMessage());
        }
    }

    public void reloadConfig() {
        config = YamlConfiguration.loadConfiguration(configFile);
    }

    public String getMessage(String path) {
        String prefix = colorize(config.getString("messages.prefix", "&x&0&3&4&B&B&C&ls&x&1&1&5&C&C&4&lʏ&x&2&0&6&D&C&B&ls&x&2&E&7&E&D&3&lᴛ&x&3&D&9&0&D&B&lᴇ&x&4&B&A&1&E&3&lᴍ &x&6&8&C&3&F&2• &r&f"));
        String message = colorize(config.getString("messages." + path, "Сообщение не найдено: " + path));
        return prefix + message;
    }

    public String getMessage(String path, String... replacements) {
        String message = getMessage(path);
        for (int i = 0; i < replacements.length; i += 2) {
            if (i + 1 < replacements.length) {
                message = message.replace(replacements[i], replacements[i+1]);
            }
        }
        return message;
    }

    private String colorize(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }
}