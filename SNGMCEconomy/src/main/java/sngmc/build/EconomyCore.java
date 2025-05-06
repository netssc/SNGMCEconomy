package sngmc.build;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

public class EconomyCore implements Economy {

    private final SNGMCEconomy plugin;
    private final ConfigManager config;
    private final DataManager dataManager;
    private final DecimalFormat moneyFormat;
    private final Map<String, Double> balances = new HashMap<>();

    public EconomyCore() {
        this.plugin = SNGMCEconomy.getInstance();
        this.config = plugin.getConfigManager();
        this.dataManager = new DataManager(plugin);
        this.dataManager.setup();
        this.dataManager.loadBalances(balances);

        int fractionalDigits = config.getConfig().getInt("economy.fractional-digits", 2);
        StringBuilder pattern = new StringBuilder("#,##0");
        if (fractionalDigits > 0) {
            pattern.append(".");
            for (int i = 0; i < fractionalDigits; i++) {
                pattern.append("0");
            }
        }
        this.moneyFormat = new DecimalFormat(pattern.toString());
    }

    public DataManager getDataManager() {
        return dataManager;
    }

    public void saveData() {
        dataManager.saveBalances(balances);
    }

    public double getPlayerBalance(String uuid) {
        return balances.getOrDefault(uuid, config.getConfig().getDouble("economy.starting-balance", 100.0));
    }

    public void setPlayerBalance(String uuid, double amount) {
        balances.put(uuid, amount);
    }

    public String formatMoney(double amount) {
        moneyFormat.setDecimalFormatSymbols(new java.text.DecimalFormatSymbols(java.util.Locale.US));
        String formatted = moneyFormat.format(amount);
        int main = (int) amount;
        String currencyMain = getCurrencyName(main);
        return formatted + " " + currencyMain;
    }

    private String getCurrencyName(int amount) {
        int lastDigit = amount % 10;
        int lastTwoDigits = amount % 100;

        if (lastTwoDigits >= 11 && lastTwoDigits <= 19) {
            return config.getConfig().getString("economy.currency-plural");
        }

        switch (lastDigit) {
            case 1: return config.getConfig().getString("economy.currency-singular");
            case 2:
            case 3:
            case 4: return config.getConfig().getString("economy.currency-plural");
            default: return config.getConfig().getString("economy.currency-plural");
        }
    }

    @Override
    public boolean isEnabled() {
        return plugin != null && plugin.isEnabled();
    }

    @Override
    public String getName() {
        return "SNGMCEconomy";
    }

    @Override
    public boolean hasBankSupport() {
        return false;
    }

    @Override
    public int fractionalDigits() {
        return config.getConfig().getInt("economy.fractional-digits", 2);
    }

    @Override
    public String format(double amount) {
        return formatMoney(amount);
    }

    @Override
    public String currencyNamePlural() {
        return config.getConfig().getString("economy.currency-plural");
    }

    @Override
    public String currencyNameSingular() {
        return config.getConfig().getString("economy.currency-singular");
    }

    @Override
    public boolean hasAccount(String playerName) {
        return true;
    }

    @Override
    public boolean hasAccount(OfflinePlayer player) {
        return true;
    }

    @Override
    public boolean hasAccount(String playerName, String worldName) {
        return hasAccount(playerName);
    }

    @Override
    public boolean hasAccount(OfflinePlayer player, String worldName) {
        return hasAccount(player);
    }

    @Override
    public double getBalance(String playerName) {
        Player player = plugin.getServer().getPlayer(playerName);
        return player != null ? getBalance(player) : 0;
    }

    @Override
    public double getBalance(OfflinePlayer player) {
        return getPlayerBalance(player.getUniqueId().toString());
    }

    @Override
    public double getBalance(String playerName, String world) {
        return getBalance(playerName);
    }

    @Override
    public double getBalance(OfflinePlayer player, String world) {
        return getBalance(player);
    }

    @Override
    public boolean has(String playerName, double amount) {
        return getBalance(playerName) >= amount;
    }

    @Override
    public boolean has(OfflinePlayer player, double amount) {
        return getBalance(player) >= amount;
    }

    @Override
    public boolean has(String playerName, String worldName, double amount) {
        return has(playerName, amount);
    }

    @Override
    public boolean has(OfflinePlayer player, String worldName, double amount) {
        return has(player, amount);
    }

    @Override
    public EconomyResponse withdrawPlayer(String playerName, double amount) {
        Player player = plugin.getServer().getPlayer(playerName);
        return player != null ? withdrawPlayer(player, amount) :
                new EconomyResponse(0, 0, EconomyResponse.ResponseType.FAILURE, "Player not online");
    }

    @Override
    public EconomyResponse withdrawPlayer(OfflinePlayer player, double amount) {
        if (amount < 0) {
            return new EconomyResponse(0, 0, EconomyResponse.ResponseType.FAILURE, "Cannot withdraw negative amount");
        }

        double balance = getBalance(player);
        if (balance < amount) {
            return new EconomyResponse(0, balance, EconomyResponse.ResponseType.FAILURE, "Insufficient funds");
        }

        double newBalance = balance - amount;
        setPlayerBalance(player.getUniqueId().toString(), newBalance);

        return new EconomyResponse(amount, newBalance, EconomyResponse.ResponseType.SUCCESS, "");
    }

    @Override
    public EconomyResponse withdrawPlayer(String playerName, String worldName, double amount) {
        return withdrawPlayer(playerName, amount);
    }

    @Override
    public EconomyResponse withdrawPlayer(OfflinePlayer player, String worldName, double amount) {
        return withdrawPlayer(player, amount);
    }

    @Override
    public EconomyResponse depositPlayer(String playerName, double amount) {
        Player player = plugin.getServer().getPlayer(playerName);
        return player != null ? depositPlayer(player, amount) :
                new EconomyResponse(0, 0, EconomyResponse.ResponseType.FAILURE, "Player not online");
    }

    @Override
    public EconomyResponse depositPlayer(OfflinePlayer player, double amount) {
        if (amount < 0) {
            return new EconomyResponse(0, 0, EconomyResponse.ResponseType.FAILURE, "Cannot deposit negative amount");
        }

        double balance = getBalance(player);
        double newBalance = balance + amount;
        setPlayerBalance(player.getUniqueId().toString(), newBalance);

        return new EconomyResponse(amount, newBalance, EconomyResponse.ResponseType.SUCCESS, "");
    }

    @Override
    public EconomyResponse depositPlayer(String playerName, String worldName, double amount) {
        return depositPlayer(playerName, amount);
    }

    @Override
    public EconomyResponse depositPlayer(OfflinePlayer player, String worldName, double amount) {
        return depositPlayer(player, amount);
    }

    @Override
    public boolean createPlayerAccount(String playerName) {
        Player player = plugin.getServer().getPlayer(playerName);
        return player != null && createPlayerAccount(player);
    }

    @Override
    public boolean createPlayerAccount(OfflinePlayer player) {
        if (!balances.containsKey(player.getUniqueId().toString())) {
            balances.put(player.getUniqueId().toString(),
                    config.getConfig().getDouble("economy.starting-balance", 100.0));
            return true;
        }
        return false;
    }

    @Override
    public boolean createPlayerAccount(String playerName, String worldName) {
        return createPlayerAccount(playerName);
    }

    @Override
    public boolean createPlayerAccount(OfflinePlayer player, String worldName) {
        return createPlayerAccount(player);
    }

    @Override public EconomyResponse createBank(String name, String player) { return null; }
    @Override public EconomyResponse createBank(String name, OfflinePlayer player) { return null; }
    @Override public EconomyResponse deleteBank(String name) { return null; }
    @Override public EconomyResponse bankBalance(String name) { return null; }
    @Override public EconomyResponse bankHas(String name, double amount) { return null; }
    @Override public EconomyResponse bankWithdraw(String name, double amount) { return null; }
    @Override public EconomyResponse bankDeposit(String name, double amount) { return null; }
    @Override public EconomyResponse isBankOwner(String name, String playerName) { return null; }
    @Override public EconomyResponse isBankOwner(String name, OfflinePlayer player) { return null; }
    @Override public EconomyResponse isBankMember(String name, String playerName) { return null; }
    @Override public EconomyResponse isBankMember(String name, OfflinePlayer player) { return null; }
    @Override public List<String> getBanks() { return null; }
}