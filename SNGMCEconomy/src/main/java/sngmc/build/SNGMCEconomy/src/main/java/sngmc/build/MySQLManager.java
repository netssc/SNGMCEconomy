package sngmc.build;

import java.sql.*;
import java.util.Map;
import java.util.UUID;

public class MySQLManager {
    private final SNGMCEconomy plugin;
    private Connection connection;

    public MySQLManager(SNGMCEconomy plugin) {
        this.plugin = plugin;
    }

    public void connect() {
        try {
            String host = plugin.getConfigManager().getConfig().getString("database.host");
            int port = plugin.getConfigManager().getConfig().getInt("database.port");
            String database = plugin.getConfigManager().getConfig().getString("database.database");
            String username = plugin.getConfigManager().getConfig().getString("database.username");
            String password = plugin.getConfigManager().getConfig().getString("database.password");

            String url = "jdbc:mysql://" + host + ":" + port + "/" + database + "?useSSL=false&autoReconnect=true";
            connection = DriverManager.getConnection(url, username, password);

            createTables();
            plugin.logInfo("Успешное подключение к MySQL!");
        } catch (SQLException e) {
            plugin.getLogger().severe("Не удалось подключиться к MySQL: " + e.getMessage());
        }
    }

    private void createTables() throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS balances (" +
                    "uuid VARCHAR(36) PRIMARY KEY, " +
                    "balance DOUBLE NOT NULL)");

            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS transactions (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "transaction_time BIGINT NOT NULL, " +
                    "from_uuid VARCHAR(36), " +
                    "to_uuid VARCHAR(36), " +
                    "from_name VARCHAR(16), " +
                    "to_name VARCHAR(16), " +
                    "amount DOUBLE NOT NULL, " +
                    "type VARCHAR(20) NOT NULL)");
        }
    }

    public void saveBalances(Map<String, Double> balances) {
        try (PreparedStatement ps = connection.prepareStatement(
                "INSERT INTO balances (uuid, balance) VALUES (?, ?) " +
                        "ON DUPLICATE KEY UPDATE balance = ?")) {

            for (Map.Entry<String, Double> entry : balances.entrySet()) {
                ps.setString(1, entry.getKey());
                ps.setDouble(2, entry.getValue());
                ps.setDouble(3, entry.getValue());
                ps.addBatch();
            }
            ps.executeBatch();
        } catch (SQLException e) {
            plugin.getLogger().severe("Ошибка сохранения балансов в MySQL: " + e.getMessage());
        }
    }

    public void loadBalances(Map<String, Double> balances) {
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT uuid, balance FROM balances")) {

            while (rs.next()) {
                balances.put(rs.getString("uuid"), rs.getDouble("balance"));
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Ошибка загрузки балансов из MySQL: " + e.getMessage());
        }
    }

    public void logTransaction(String from, String to, double amount, String type, String fromName, String toName) {
        try (PreparedStatement ps = connection.prepareStatement(
                "INSERT INTO transactions (transaction_time, from_uuid, to_uuid, from_name, to_name, amount, type) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?)")) {

            ps.setLong(1, System.currentTimeMillis());
            ps.setString(2, from);
            ps.setString(3, to);
            ps.setString(4, fromName);
            ps.setString(5, toName);
            ps.setDouble(6, amount);
            ps.setString(7, type);
            ps.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().severe("Ошибка сохранения транзакции в MySQL: " + e.getMessage());
        }
    }

    public void disconnect() {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                plugin.getLogger().severe("Ошибка при отключении от MySQL: " + e.getMessage());
            }
        }
    }
}