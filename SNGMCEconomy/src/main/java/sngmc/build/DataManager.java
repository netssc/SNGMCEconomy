package sngmc.build;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;

public class DataManager {
    private final SNGMCEconomy plugin;
    private File dataFile;
    private FileConfiguration dataConfig;
    private File transactionsFile;
    private FileConfiguration transactionsConfig;
    private MySQLManager mySQLManager;
    private StorageType storageType;

    public DataManager(SNGMCEconomy plugin) {
        this.plugin = plugin;
        this.storageType = StorageType.valueOf(plugin.getConfigManager().getConfig().getString("storage.type", "YAML").toUpperCase());
    }

    public void setup() {
        if (storageType == StorageType.MYSQL) {
            this.mySQLManager = new MySQLManager(plugin);
            mySQLManager.connect();
        } else {
            setupYaml();
        }
    }

    private void setupYaml() {
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdir();
        }

        dataFile = new File(plugin.getDataFolder(), "balances.yml");
        if (!dataFile.exists()) {
            try {
                dataFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("Не удалось создать balances.yml!");
            }
        }
        dataConfig = YamlConfiguration.loadConfiguration(dataFile);

        transactionsFile = new File(plugin.getDataFolder(), "transactions.yml");
        if (!transactionsFile.exists()) {
            try {
                transactionsFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("Не удалось создать transactions.yml!");
            }
        }
        transactionsConfig = YamlConfiguration.loadConfiguration(transactionsFile);
    }

    public void saveBalances(Map<String, Double> balances) {
        if (storageType == StorageType.MYSQL) {
            mySQLManager.saveBalances(balances);
        } else {
            saveBalancesYaml(balances);
        }
    }

    private void saveBalancesYaml(Map<String, Double> balances) {
        for (Map.Entry<String, Double> entry : balances.entrySet()) {
            dataConfig.set("balances." + entry.getKey(), entry.getValue());
        }
        try {
            dataConfig.save(dataFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Не удалось сохранить balances.yml!");
        }
    }

    public void loadBalances(Map<String, Double> balances) {
        if (storageType == StorageType.MYSQL) {
            mySQLManager.loadBalances(balances);
        } else {
            loadBalancesYaml(balances);
        }
    }

    private void loadBalancesYaml(Map<String, Double> balances) {
        if (dataConfig.getConfigurationSection("balances") != null) {
            for (String uuid : dataConfig.getConfigurationSection("balances").getKeys(false)) {
                balances.put(uuid, dataConfig.getDouble("balances." + uuid));
            }
        }
    }

    public void logTransaction(String from, String to, double amount, String type, String fromName, String toName) {
        if (storageType == StorageType.MYSQL) {
            mySQLManager.logTransaction(from, to, amount, type, fromName, toName);
        } else {
            logTransactionYaml(from, to, amount, type, fromName, toName);
        }
    }

    private void logTransactionYaml(String from, String to, double amount, String type, String fromName, String toName) {
        long timestamp = System.currentTimeMillis();
        String transactionKey = "transactions." + timestamp;

        transactionsConfig.set(transactionKey + ".from", from);
        transactionsConfig.set(transactionKey + ".to", to);
        transactionsConfig.set(transactionKey + ".fromName", fromName);
        transactionsConfig.set(transactionKey + ".toName", toName);
        transactionsConfig.set(transactionKey + ".amount", amount);
        transactionsConfig.set(transactionKey + ".type", type);

        try {
            transactionsConfig.save(transactionsFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Не удалось сохранить транзакцию!");
        }

        // Логирование в консоль с никами игроков
        String action = "Транзакция";
        if (type.equals("ADMIN_GIVE")) {
            action = "Соврешена Выдача";
        } else if (type.equals("ADMIN_TAKE")) {
            action = "Совершено изьятие";
        } else if (type.equals("ADMIN_SET")) {
            action = "Изменен баланс";
        }

        String logMessage = String.format("%s -> %s: %s %s",
                fromName,
                toName,
                plugin.getEconomy().format(amount),
                type);
        plugin.logTransaction(fromName, action, logMessage);
    }

    public void shutdown() {
        if (storageType == StorageType.MYSQL && mySQLManager != null) {
            mySQLManager.disconnect();
        }
    }

    public enum StorageType {
        YAML, MYSQL
    }
}
